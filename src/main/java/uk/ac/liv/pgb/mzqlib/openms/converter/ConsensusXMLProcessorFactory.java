
package uk.ac.liv.pgb.mzqlib.openms.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import uk.ac.liv.pgb.jmzqml.model.mzqml.*;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.pgb.mzqlib.openms.jaxb.ConsensusElement;
import uk.ac.liv.pgb.mzqlib.openms.jaxb.ConsensusElementList;
import uk.ac.liv.pgb.mzqlib.openms.jaxb.ConsensusXML;
import uk.ac.liv.pgb.mzqlib.openms.jaxb.Element;
import uk.ac.liv.pgb.mzqlib.openms.jaxb.FeatureMap;
import uk.ac.liv.pgb.mzqlib.openms.jaxb.FeatureType;
import uk.ac.liv.pgb.mzqlib.openms.jaxb.FeatureType.Convexhull;
import uk.ac.liv.pgb.mzqlib.openms.jaxb.FeatureType.Convexhull.Pt;
import uk.ac.liv.pgb.mzqlib.openms.jaxb.MapList;

/**
 * The ConsensusXMLProcessorFactory class parse the input consensusxml file and
 * create a ConsensusXMLProcessor instance.
 *
 * @author Da Qi
 * @since 18-Mar-2014 15:17:53
 */
public class ConsensusXMLProcessorFactory {

    private static final ConsensusXMLProcessorFactory instance
            = new ConsensusXMLProcessorFactory();
    private static final String CvIDPSIMS = "PSI-MS";
    private static final String CvNamePSIMS
            = "Proteomics Standards Initiative Mass Spectrometry Vocabularies";
    private static final String CvUriPSIMS
            = "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/"
            + "mzML/controlledVocabulary/psi-ms.obo";
    private static final String CvVerPSIMS = "3.61.0";

    private ConsensusXMLProcessorFactory() {
    }

    /**
     * The method provides a static instance of the class
     * ConsensusXMLProcessorFactory. User will call this method for accessing to
     * buildConsensusXMLProcessor method.
     *
     * @return the static instance of ConsensusXMLProcessorFactory
     */
    public static ConsensusXMLProcessorFactory getInstance() {
        return instance;
    }

    /**
     * The method builds a ConsensusXMLProcessor instance from input
     * consensusxml file.
     *
     * @param xmlFile the input consensusxml file
     *
     * @return the ConsensusXMLProcessor instance
     *
     * @throws JAXBException jaxb exception
     */
    public ConsensusXMLProcessor buildConsensusXMLProcessor(File xmlFile)
            throws JAXBException {
        return new ConsensusXMLProcessorImpl(xmlFile);
    }

    private static class ConsensusXMLProcessorImpl implements
            ConsensusXMLProcessor {

        protected Unmarshaller unmarsh;
        protected Map<String, FeatureList> rgIdToFeatureListMap
                = new HashMap<>();
        protected PeptideConsensusList pepConList = new PeptideConsensusList();
        protected AssayList assays = new AssayList();
        protected Cv cv;
        protected List<RawFilesGroup> rgList = new ArrayList();
        protected Map<String, Assay> rgIdToAssayMap = new HashMap<>();
        protected Map<String, RawFilesGroup> rgIdToRgObjectMap = new HashMap<>();
        private Map<Integer, Map<String, MzRtArea>> featureAreasPreAligned
                = new HashMap<>();
        private Map<Integer, Map<String, MzRtArea>> featureAreasPostAligned
                = new HashMap<>();
        private final JAXBContext context = JAXBContext.newInstance(new Class[]{
            FeatureMap.class});
        private Unmarshaller featureUnmarshaller = context.createUnmarshaller();
        private Map<File, Assay> filesToAssays = new HashMap<>();

        public ConsensusXMLProcessorImpl(File file)
                throws JAXBException {
            JAXBContext context = JAXBContext.newInstance(new Class[]{
                ConsensusXML.class});
            unmarsh = context.createUnmarshaller();

            cv = new Cv();
            cv.setId(CvIDPSIMS);
            cv.setUri(CvUriPSIMS);
            cv.setFullName(CvNamePSIMS);
            cv.setVersion(CvVerPSIMS);

            //unmodified label
            Label label = new Label();
            CvParam labelCvParam = new CvParam();
            labelCvParam.setAccession("MS:1002038");
            labelCvParam.setName("unlabeled sample");
            labelCvParam.setCv(cv);
            List<ModParam> modParams = label.getModification();
            ModParam modParam = new ModParam();
            modParam.setCvParam(labelCvParam);
            modParams.add(modParam);

            // root
            ConsensusXML consensus = (ConsensusXML) unmarsh.unmarshal(file);

            ConsensusElementList conEleList = consensus.
                    getConsensusElementList();

            List<ConsensusElement> conElements = conEleList.
                    getConsensusElement();

            MapList mapList = consensus.getMapList();
            // number of mapping files
            int mapCount = (int) mapList.getCount();

            assays.setId("AssayList1");

            List<uk.ac.liv.pgb.mzqlib.openms.jaxb.Map> maps = mapList.getMap();
            readFeatureXmlMzRtAreas(maps);
            for (uk.ac.liv.pgb.mzqlib.openms.jaxb.Map map : maps) {
                RawFilesGroup rg = new RawFilesGroup();

                // ID of RawFilesGroup
                String rgId = "rg_" + map.getId();
                rg.setId(rgId);

                // RawFile
                RawFile rawFile = new RawFile();
                rawFile.setId("raw_" + map.getUniqueId());
                //rawFile.setName(map.getName());
                rawFile.setLocation(map.getName());

                rg.getRawFile().add(rawFile);

                rgIdToRgObjectMap.put(rgId, rg);

                Assay assay = new Assay();

                // ID of Assay
                String assId = "ass_" + map.getId();
                assay.setId(assId);

                // label of Assay
                assay.setLabel(label);

                // set RawFilesGroup
                assay.setRawFilesGroup(rg);

                assays.getAssay().add(assay);

                rgList.add(rg);

                rgIdToAssayMap.put(rgId, assay);

                filesToAssays.put(new File(map.getName()), assay);
            }

            QuantLayer assayQL = new QuantLayer();
            DataMatrix dm = new DataMatrix();
            String[] columnIndex = new String[mapCount];

            pepConList.setFinalResult(true);
            pepConList.setId("pepConList_consensusXML");

            CvParam massTraceRectangleParam = new CvParam();
            massTraceRectangleParam.setAccession("MS:1001826");
            massTraceRectangleParam.setName("mass trace reporting: rectangles");
            massTraceRectangleParam.setCv(cv);

            for (ConsensusElement conElement : conElements) {

                PeptideConsensus pepCon = new PeptideConsensus();

                // Id of PeptideConsensus
                String pepConId = "pepCon_" + conElement.getId();
                pepCon.setId(pepConId);

                // charge of PeptideConsensus
                pepCon.getCharge().add(conElement.getCharge().toString());

                // add pepCon to pepConList
                pepConList.getPeptideConsensus().add(pepCon);

                // new row
                Row row = new Row();
                // set object ref (i.e: pepCon id)
                row.setObjectRef(pepConId);

                // groupedElementList to EvidencRefs
                List<Element> elements = conElement.getGroupedElementList().
                        getElement();

                String[] values = new String[mapCount];

                for (int i = 0; i < mapCount; i++) {
                    values[i] = "null";
                }

                for (Element element : elements) {

                    columnIndex[(int) element.getMap()] = "ass_"
                            + (int) element.getMap();

                    // charge of feature
                    String ftCharge = element.getCharge().toString();

                    // raw files group Id of feature
                    String rawFilesGroupId = String.valueOf("rg_" + element.
                            getMap());

                    // intensity of feature, goes to AssayQuantLayer
                    double intensity = element.getIt();

                    values[(int) element.getMap()] = String.valueOf(intensity);

                    // M/Z of feature
                    double ftMz = element.getMz();

                    // id of feature
                    String ftId = "ft_" + element.getId();

                    // retention time of feature
                    double ftRt = element.getRt();

                    FeatureList featureList = rgIdToFeatureListMap.get(
                            rawFilesGroupId);

                    if (featureList == null) {
                        featureList = new FeatureList();
                        rgIdToFeatureListMap.put(rawFilesGroupId, featureList);
                        featureList.setRawFilesGroup(rgIdToRgObjectMap.get(
                                rawFilesGroupId));
                        featureList.setId("FList_" + element.getMap());

                        featureList.getCvParam().add(massTraceRectangleParam);
                    }

                    //new Feature
                    Feature feature = new Feature();

                    feature.setCharge(ftCharge);
                    feature.setId(ftId);
                    feature.setMz(ftMz);
                    feature.setRt(String.valueOf(ftRt));

                    Map<String, MzRtArea> featureAreaPreAligned
                            = featureAreasPreAligned.get((int) element.getMap());
                    if (featureAreaPreAligned != null) {
                        MzRtArea featureArea = featureAreaPreAligned.get(
                                element.getId());
                        if (featureArea != null) {
                            feature.getMassTrace().addAll(featureArea.
                                    getMassTrace());
                            feature.setMz(featureArea.getMzCentroid());
                            feature.setRt(String.valueOf(featureArea.
                                    getRtCentroid()));
                        }
                    }

                    Map<String, MzRtArea> featureAreaPostAligned
                            = featureAreasPostAligned.
                            get((int) element.getMap());
                    if (featureAreaPostAligned != null) {
                        MzRtArea featureArea = featureAreaPostAligned.get(
                                element.getId());
                        if (featureArea != null) {
                            UserParam rtCentroidParam = new UserParam();
                            rtCentroidParam.setName(
                                    "Retention time centroid: post-alignment");
                            rtCentroidParam.setValue(String.valueOf(featureArea.
                                    getRtCentroid()));
                            UserParam massTraceParam = new UserParam();
                            massTraceParam.setName("Mass trace: post-alignment");
                            massTraceParam.setValue(String.join(" ",
                                                                featureArea.
                                                                getMassTrace().
                                                                stream().map(p
                                                                        -> String.
                                                                        valueOf(p)).
                                                                collect(Collectors.
                                                                        toList())));
                            feature.getUserParam().add(rtCentroidParam);
                            feature.getUserParam().add(massTraceParam);
                        }
                    }

                    // add new feature to featureList
                    featureList.getFeature().add(feature);

                    // new EvidenceRef
                    EvidenceRef evdRef = new EvidenceRef();

                    evdRef.setFeature(feature);

                    evdRef.getAssayRefs().add(rgIdToAssayMap.
                            get(rawFilesGroupId).getId());

                    pepCon.getEvidenceRef().add(evdRef);
                }
                // add row values
                row.getValue().addAll(Arrays.asList(values));

                // add row to DataMatrix
                dm.getRow().add(row);

            }

            assayQL.getColumnIndex().addAll(Arrays.asList(columnIndex));
            assayQL.setDataMatrix(dm);

            CvParamRef cpRef = new CvParamRef();
            CvParam cp = new CvParam();
            cp.setCv(cv);
            cp.setAccession("MS:1001840");
            cp.setName("LC-MS feature intensity");
            cpRef.setCvParam(cp);
            assayQL.setDataType(cpRef);
            assayQL.setId("AQL_intensity");
            pepConList.getAssayQuantLayer().add(assayQL);

        }

        private void readFeatureXmlMzRtAreas(
                List<uk.ac.liv.pgb.mzqlib.openms.jaxb.Map> featureXmlMaps)
                throws JAXBException {
            for (uk.ac.liv.pgb.mzqlib.openms.jaxb.Map featureXmlMap : featureXmlMaps) {
                String featureXmlLocation = featureXmlMap.getName();
                File featureXmlFile = new File(featureXmlLocation);
                if (!featureXmlFile.exists()) {
                    continue;
                }

                if (!featureXmlLocation.contains("_MAPC")) {
                    readSingleFeatureXmlMzRtAreas(featureXmlFile,
                                                  (int) featureXmlMap.getId(),
                                                  featureAreasPreAligned);
                } else {
                    readSingleFeatureXmlMzRtAreas(featureXmlFile,
                                                  (int) featureXmlMap.getId(),
                                                  featureAreasPostAligned);
                    File featureXmlUnalignedFile = new File(featureXmlLocation.
                            replace("_MAPC", ""));
                    if (featureXmlUnalignedFile.exists()) {
                        readSingleFeatureXmlMzRtAreas(featureXmlUnalignedFile,
                                                      (int) featureXmlMap.
                                                      getId(),
                                                      featureAreasPreAligned);
                    }
                }
            }
        }

        private void readSingleFeatureXmlMzRtAreas(File featureXmlFile,
                                                   int mapNumber,
                                                   Map<Integer, Map<String, MzRtArea>> mzRtAreas)
                throws JAXBException {
            FeatureMap featureMap = (FeatureMap) featureUnmarshaller.unmarshal(
                    featureXmlFile);
            Map<String, MzRtArea> featureAreas = new HashMap<>();
            for (FeatureType feature : featureMap.getFeatureList().getFeature()) {
                List<Pt> allPts = new LinkedList<>();
                for (Convexhull convexHull : feature.getConvexhull()) {
                    allPts.addAll(convexHull.getPt());
                }

                DoubleSummaryStatistics rtStats = allPts.stream().mapToDouble(p
                        -> p.getX()).summaryStatistics();
                DoubleSummaryStatistics mzStats = allPts.stream().mapToDouble(p
                        -> p.getY()).summaryStatistics();

                double mzCentroid = feature.getPosition().stream().filter(p
                        -> p.getDim().equals("1")).findFirst().get().getValue();
                double rtCentroid = feature.getPosition().stream().filter(p
                        -> p.getDim().equals("0")).findFirst().get().getValue()
                        / 60.0;
                MzRtArea featureArea = new MzRtArea(mzCentroid, rtCentroid);
                featureArea.getMassTrace().add(rtStats.getMin() / 60.0);
                featureArea.getMassTrace().add(mzStats.getMin());
                featureArea.getMassTrace().add(rtStats.getMax() / 60.0);
                featureArea.getMassTrace().add(mzStats.getMax());
                featureAreas.put(feature.getId().replace("f_", ""), featureArea);
            }

            mzRtAreas.put(mapNumber, featureAreas);
        }

        @Override
        public PeptideConsensusList getPeptideConsensusList() {
            return pepConList;
        }

        @Override
        public Map<String, FeatureList> getRawFilesGroupIdToFeatureListMap() {
            return rgIdToFeatureListMap;
        }

        @Override
        public Cv getCv() {
            return cv;
        }

        @Override
        public AssayList getAssayList() {
            return assays;
        }

        @Override
        public Map<String, Assay> getRawFilesGroupAssayMap() {
            return rgIdToAssayMap;
        }

        @Override
        public List<RawFilesGroup> getRawFilesGroupList() {
            return rgList;
        }

        @Override
        public void convert(String outputFileName)
                throws IOException {
            convert(outputFileName, new HashMap<>());
        }

        @Override
        public void convert(String outputFileName,
                            Map<String, ? extends Collection<File>> studyVariablesToFiles)
                throws IOException {
            //File file = new File("CPTAC_study6_2400_3600_FLUQT.consensusXML");
            //String output = "CPTAC_study6_2400_3600_FLUQT.consensusXML.mzq";
            FileOutputStream fos = new FileOutputStream(outputFileName);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");

            MzQuantMLMarshaller m = new MzQuantMLMarshaller();

            //ConsensusXMLProcessor conProc = ConsensusXMLProcessorFactory.getInstance().buildConsensusXMLProcessor(file);
            // XML header
            writer.write(m.createXmlHeader() + "\n");

            // mzQuantML start tag
            writer.write(m.createMzQuantMLStartTag("consensusXML" + System.
                    currentTimeMillis()) + "\n");

            // CvList
            CvList cvs = new CvList();
            cvs.getCv().add(this.getCv());
            m.marshall(cvs, writer);
            writer.write("\n");

            // AnalysisSummary
            AnalysisSummary analysisSummary = new AnalysisSummary();
            analysisSummary.getParamGroup().add(m.createCvParam(
                    "LC-MS label-free quantitation analysis", "PSI-MS",
                    "MS:1001834"));

            CvParam analysisSummaryCv = m.createCvParam(
                    "label-free raw feature quantitation", "PSI-MS",
                    "MS:1002019");
            analysisSummaryCv.setValue("false");
            analysisSummary.getParamGroup().add(analysisSummaryCv);

            analysisSummaryCv = m.createCvParam(
                    "label-free peptide level quantitation", "PSI-MS",
                    "MS:1002020");
            analysisSummaryCv.setValue("true");
            analysisSummary.getParamGroup().add(analysisSummaryCv);

            analysisSummaryCv = m.createCvParam(
                    "label-free protein level quantitation", "PSI-MS",
                    "MS:1002021");
            analysisSummaryCv.setValue("false");
            analysisSummary.getParamGroup().add(analysisSummaryCv);

            analysisSummaryCv = m.createCvParam(
                    "label-free proteingroup level quantitation", "PSI-MS",
                    "MS:1002022");
            analysisSummaryCv.setValue("false");
            analysisSummary.getParamGroup().add(analysisSummaryCv);

            m.marshall(analysisSummary, writer);
            writer.write("\n");

            // InputFiles
            InputFiles inputFiles = new InputFiles();
            inputFiles.getRawFilesGroup().addAll(this.getRawFilesGroupList());
            m.marshall(inputFiles, writer);
            writer.write("\n");

            // SoftwareList
            SoftwareList softwareList = new SoftwareList();
            Software software = new Software();
            softwareList.getSoftware().add(software);
            software.setId("OpenMS");
            software.setVersion("1.11.1");
            software.getCvParam().add(m.createCvParam("TOPP software", "PSI-MS",
                                                      "MS:1000752"));
            m.marshall(softwareList, writer);
            writer.write("\n");

            // AssayList
            m.marshall(this.getAssayList(), writer);
            writer.write("\n");

            // StudyVariableList
            if (studyVariablesToFiles != null && studyVariablesToFiles.size()
                    > 0) {
                StudyVariableList studyVariables = new StudyVariableList();
                for (Entry<String, ? extends Collection<File>> entry
                        : studyVariablesToFiles.entrySet()) {
                    StudyVariable variable = new StudyVariable();
                    variable.setId("std_var_" + entry.getKey());
                    variable.setName(entry.getKey());
                    List<Assay> matchedAssays = entry.getValue().stream().map(
                            file -> filesToAssays.get(file)).collect(Collectors.
                                    toList());
                    variable.setAssays(matchedAssays);
                    studyVariables.getStudyVariable().add(variable);
                }

                m.marshall(studyVariables, writer);
                writer.write("\n");
            }

            // PeptideConsensusList
            m.marshall(this.getPeptideConsensusList(), writer);
            writer.write("\n");

            for (FeatureList ftList : this.getRawFilesGroupIdToFeatureListMap().
                    values()) {
                m.marshall(ftList, writer);
                writer.write("\n");
            }

            // mzQuantML closing tag
            writer.write(m.createMzQuantMLClosingTag());

            writer.close();
        }

    }

    private static class MzRtArea {

        private double mzCentroid;
        private double rtCentroid;
        private List<Double> massTrace = new LinkedList<>();

        public MzRtArea(double mzCentroid, double rtCentroid) {
            this.mzCentroid = mzCentroid;
            this.rtCentroid = rtCentroid;
        }

        public double getMzCentroid() {
            return this.mzCentroid;
        }

        public double getRtCentroid() {
            return this.rtCentroid;
        }

        public List<Double> getMassTrace() {
            return this.massTrace;
        }

    }

}
