/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.consensusxml.convertor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import uk.ac.liv.jmzqml.model.mzqml.*;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.mzqlib.consensusxml.convertor.jaxb.ConsensusElement;
import uk.ac.liv.mzqlib.consensusxml.convertor.jaxb.ConsensusElementList;
import uk.ac.liv.mzqlib.consensusxml.convertor.jaxb.ConsensusXML;
import uk.ac.liv.mzqlib.consensusxml.convertor.jaxb.Element;
import uk.ac.liv.mzqlib.consensusxml.convertor.jaxb.MapList;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 18-Mar-2014 15:17:53
 */
public class ConsensusXMLProcessorFactory {
    
    private static final ConsensusXMLProcessorFactory instance = new ConsensusXMLProcessorFactory();
    private static final String CvIDPSIMS = "PSI-MS";
    private static final String CvNamePSIMS = "Proteomics Standards Initiative Mass Spectrometry Vocabularies";
    private static final String CvUriPSIMS = "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/"
            + "mzML/controlledVocabulary/psi-ms.obo";
    private static final String CvVerPSIMS = "3.61.0";
    
    public ConsensusXMLProcessorFactory() {
    }
    
    public static ConsensusXMLProcessorFactory getInstance() {
        return instance;
    }
    
    public ConsensusXMLProcessor buildConsensusXMLProcessor(File xmlFile)
            throws JAXBException {
        return new ConsensusXMLProcessorImpl(xmlFile);
    }
    
    private class ConsensusXMLProcessorImpl implements ConsensusXMLProcessor {
        
        Unmarshaller unmarsh;
        //List<FeatureList> featureLists = new ArrayList();
        Map<String, FeatureList> rgIdToFeatureListMap = new HashMap<>();
        PeptideConsensusList pepConList = new PeptideConsensusList();
        AssayList assays = new AssayList();
        Cv cv;
        List<RawFilesGroup> rgList = new ArrayList();
        Map<String, Assay> rgIdToAssayMap = new HashMap<>();
        Map<String, RawFilesGroup> rgIdToRgObjectMap = new HashMap<>();
        /*
         * Constructor
         */
        
        private ConsensusXMLProcessorImpl(File xmlFile)
                throws JAXBException {
            JAXBContext context = JAXBContext.newInstance(new Class[]{ConsensusXML.class});
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
            ConsensusXML consensus = (ConsensusXML) unmarsh.unmarshal(xmlFile);
            
            ConsensusElementList conEleList = consensus.getConsensusElementList();
            
            List<ConsensusElement> conElements = conEleList.getConsensusElement();
            
            MapList mapList = consensus.getMapList();
            // number of mapping files
            int mapCount = (int) mapList.getCount();
            
            assays.setId("AssayList1");
            
            List<uk.ac.liv.mzqlib.consensusxml.convertor.jaxb.Map> maps = mapList.getMap();
            for (uk.ac.liv.mzqlib.consensusxml.convertor.jaxb.Map map : maps) {
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
            }
            
            QuantLayer assayQL = new QuantLayer();
            DataMatrix dm = new DataMatrix();
            String[] columnIndex = new String[mapCount];
            
            pepConList.setFinalResult(true);
            pepConList.setId("pepConList_consensusXML");
            
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
                List<Element> elements = conElement.getGroupedElementList().getElement();
                
                String[] values = new String[mapCount];
                
                
                
                for (int i = 0; i < mapCount; i++) {
                    values[i] = "null";
                }
                
                for (Element element : elements) {
                    
                    columnIndex[(int) element.getMap()] = "ass_" + (int) element.getMap();

                    // charge of feature
                    String ftCharge = element.getCharge().toString();

                    // raw files group Id of feature
                    String rawFilesGroupId = String.valueOf("rg_" + element.getMap());

                    // intensity of feature, goes to AssayQuantLayer
                    double intensity = element.getIt();
                    
                    values[(int) element.getMap()] = String.valueOf(intensity);

                    // M/Z of feature
                    double ftMz = element.getMz();

                    // id of feature
                    String ftId = "ft_" + element.getId();

                    // retention time of feature
                    double ftRt = element.getRt();
                    
                    FeatureList featureList = rgIdToFeatureListMap.get(rawFilesGroupId);
                    
                    if (featureList == null) {
                        featureList = new FeatureList();
                        rgIdToFeatureListMap.put(rawFilesGroupId, featureList);
                        featureList.setRawFilesGroup(rgIdToRgObjectMap.get(rawFilesGroupId));
                        featureList.setId("FList_" + element.getMap());
                    }

                    //new Feature
                    Feature feature = new Feature();
                    
                    feature.setCharge(ftCharge);
                    feature.setId(ftId);
                    feature.setMz(ftMz);
                    feature.setRt(String.valueOf(ftRt));

                    // add new feature to featureList
                    featureList.getFeature().add(feature);

                    // new EvidenceRef
                    EvidenceRef evdRef = new EvidenceRef();
                    
                    evdRef.setFeature(feature);
                    
                    evdRef.getAssayRefs().add(rgIdToAssayMap.get(rawFilesGroupId).getId());
                    
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

            //File file = new File("CPTAC_study6_2400_3600_FLUQT.consensusXML");
            //String output = "CPTAC_study6_2400_3600_FLUQT.consensusXML.mzq";
            FileWriter writer = new FileWriter(outputFileName);
            
            MzQuantMLMarshaller m = new MzQuantMLMarshaller();

            //ConsensusXMLProcessor conProc = ConsensusXMLProcessorFactory.getInstance().buildConsensusXMLProcessor(file);

            // XML header
            writer.write(m.createXmlHeader() + "\n");

            // mzQuantML start tag
            writer.write(m.createMzQuantMLStartTag("consensusXML" + System.currentTimeMillis()) + "\n");

            // CvList
            CvList cvs = new CvList();
            cvs.getCv().add(this.getCv());
            m.marshall(cvs, writer);
            writer.write("\n");

            // AnalysisSummary
            AnalysisSummary analysisSummary = new AnalysisSummary();
            analysisSummary.getParamGroup().add(m.createCvParam("LC-MS label-free quantitation analysis", "PSI-MS", "MS:1001834"));
            
            CvParam analysisSummaryCv = m.createCvParam("label-free raw feature quantitation", "PSI-MS", "MS:1002019");
            analysisSummaryCv.setValue("false");
            analysisSummary.getParamGroup().add(analysisSummaryCv);
            
            analysisSummaryCv = m.createCvParam("label-free peptide level quantitation", "PSI-MS", "MS:1002020");
            analysisSummaryCv.setValue("true");
            analysisSummary.getParamGroup().add(analysisSummaryCv);
            
            analysisSummaryCv = m.createCvParam("label-free protein level quantitation", "PSI-MS", "MS:1002021");
            analysisSummaryCv.setValue("false");
            analysisSummary.getParamGroup().add(analysisSummaryCv);
            
            analysisSummaryCv = m.createCvParam("label-free proteingroup level quantitation", "PSI-MS", "MS:1002022");
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
            software.getCvParam().add(m.createCvParam("TOPP software", "PSI-MS", "MS:1000752"));
            m.marshall(softwareList, writer);
            writer.write("\n");

            // AssayList
            m.marshall(this.getAssayList(), writer);
            writer.write("\n");

            // PeptideConsensusList
            m.marshall(this.getPeptideConsensusList(), writer);
            writer.write("\n");
            
            for (FeatureList ftList : this.getRawFilesGroupIdToFeatureListMap().values()) {
                m.marshall(ftList, writer);
                writer.write("\n");
            }

            // mzQuantML closing tag
            writer.write(m.createMzQuantMLClosingTag());
            
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException ex) {
                    Logger.getLogger(Convertor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
}
