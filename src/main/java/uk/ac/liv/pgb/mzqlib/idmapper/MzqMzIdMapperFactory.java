
package uk.ac.liv.pgb.mzqlib.idmapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import uk.ac.ebi.jmzidml.model.mzidml.DBSequence;
import uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence;
import uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidenceRef;
import uk.ac.liv.pgb.jmzqml.MzQuantMLElement;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AuditCollection;
import uk.ac.liv.pgb.jmzqml.model.mzqml.BibliographicReference;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Cv;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.pgb.jmzqml.model.mzqml.FileFormat;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Modification;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Provider;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SearchDatabase;
import uk.ac.liv.pgb.jmzqml.model.mzqml.UserParam;
import uk.ac.liv.pgb.jmzqml.xml.io.*;
import uk.ac.liv.pgb.mzqlib.constants.MzqDataConstants;
import uk.ac.liv.pgb.mzqlib.idmapper.data.SIIData;
import uk.ac.liv.pgb.mzqlib.idmapper.data.Tolerance;
import uk.ac.liv.pgb.mzqlib.idmapper.data.Tolerance.ToleranceUnit;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AnalysisSummary;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AssayList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.DataProcessingList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.EvidenceRef;
import uk.ac.liv.pgb.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.GlobalQuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.IdOnly;
import uk.ac.liv.pgb.jmzqml.model.mzqml.IdentificationFile;
import uk.ac.liv.pgb.jmzqml.model.mzqml.IdentificationFiles;
import uk.ac.liv.pgb.jmzqml.model.mzqml.InputFiles;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Protein;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RatioList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RatioQuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Row;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SmallMoleculeList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SoftwareList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.StudyVariableList;

/**
 * The MzqMzIdMapperFactory class create an MzqMzIdMapper instance from
 * mzQuantML file and map of raw file name to mzIdentML file name.
 *
 * @author Da Qi
 * @since 24-Jun-2013 14:06:09
 */
public class MzqMzIdMapperFactory {

    private static final MzqMzIdMapperFactory instance
            = new MzqMzIdMapperFactory();

    private MzqMzIdMapperFactory() {
    }

    /**
     * The method provides a static instance of the class MzqMzIdMapperFactory.
     * User will call this method for accessing to buildMzqMzIdMapper method.
     *
     * @return instance of MzqMzIdMapperFactory
     */
    public static MzqMzIdMapperFactory getInstance() {
        return instance;
    }

    /**
     * The method builds a MzqMzidMapper instance from an input mzQuantML
     * unmarshaller and a parameter string.
     *
     * @param mzqUm           MzQuantMLUnmarshaller
     * @param rawToMzidString String contains the relationship between raw file
     *                        names and corresponding mzIdentML files. The raw file name and mzIdentML
     *                        file name are separated by semi-column(;) and arranged in alternative
     *                        manner.
     *
     * @return MzqMzIdMapper
     *
     * @throws JAXBException jaxb exception
     * @throws IOException   io exception
     */
    public MzqMzIdMapper buildMzqMzIdMapper(MzQuantMLUnmarshaller mzqUm,
                                            String rawToMzidString)
            throws JAXBException, IOException {
        return buildMzqMzIdMapper(mzqUm, rawToMzidString, new Tolerance(0.1,
                                                                        ToleranceUnit.DALTON));
    }

    /**
     * The method builds a MzqMzidMapper instance from an input mzQuantML
     * unmarshaller and a parameter string.
     *
     * @param mzqUm           MzQuantMLUnmarshaller
     * @param rawToMzidString String contains the relationship between raw file
     *                        names and corresponding mzIdentML files. The raw file name and mzIdentML
     *                        file name are separated by semi-column(;) and arranged in alternative
     *                        manner.
     * @param msTolerance     ms tolerance window
     *
     * @return MzqMzIdMapper
     *
     * @throws JAXBException jaxb exception
     * @throws IOException   io exception
     */
    public MzqMzIdMapper buildMzqMzIdMapper(MzQuantMLUnmarshaller mzqUm,
                                            String rawToMzidString,
                                            Tolerance msTolerance)
            throws JAXBException, IOException {
        String[] rawToMzidMapArray = rawToMzidString.split(";");
        Map<String, String> rawToMzidMap = new HashMap<>();
        if (rawToMzidMapArray.length % 2 != 0) {
            //System.err.println("Expected raw file name and mzid file in pairs: " + rawToMzidMap);
            throw new RuntimeException(
                    "Expected raw file name and mzid file in pairs: "
                    + rawToMzidMap);
        } else {
            // Build rawToMzidMap
            for (int i = 0; i < rawToMzidMapArray.length; i++) {
                String rawFile = rawToMzidMapArray[i].trim();
                String mzidFile = rawToMzidMapArray[i + 1].trim();
                if (mzidFile.toLowerCase(Locale.ENGLISH).endsWith("mzid")) {
                    rawToMzidMap.put(rawFile, mzidFile);
                } else {
                    throw new RuntimeException(
                            "There is a non mzid file in the argument.");
                }
                i++;
            }
        }
        return new MzqMzIdMapperImpl(mzqUm, rawToMzidMap, msTolerance);
    }

    /**
     * The method builds a MzqMzidMapper instance from an input mzQuantML
     * unmarshaller and a parameter map.
     *
     * @param mzqUm        MzQuantMLUnmarshaller
     * @param rawToMzidMap map of raw file to mzIdentML file
     *
     * @return MzqMzIdMapper
     *
     * @throws JAXBException jaxb exception
     * @throws IOException   io exception
     */
    public MzqMzIdMapper buildMzqMzIdMapper(MzQuantMLUnmarshaller mzqUm,
                                            Map<String, String> rawToMzidMap)
            throws JAXBException, IOException {
        return buildMzqMzIdMapper(mzqUm, rawToMzidMap, new Tolerance(0.1,
                                                                     ToleranceUnit.DALTON));
    }

    /**
     * The method builds a MzqMzidMapper instance from an input mzQuantML
     * unmarshaller and a parameter map.
     *
     * @param mzqUm        MzQuantMLUnmarshaller
     * @param rawToMzidMap map of raw file to mzIdentML file
     * @param msTolerance  ms tolerance window
     *
     * @return MzqMzIdMapper
     *
     * @throws JAXBException jaxb exception
     * @throws IOException   io exception
     */
    public MzqMzIdMapper buildMzqMzIdMapper(MzQuantMLUnmarshaller mzqUm,
                                            Map<String, String> rawToMzidMap,
                                            Tolerance msTolerance)
            throws JAXBException, IOException {
        return new MzqMzIdMapperImpl(mzqUm, rawToMzidMap, msTolerance);
    }

    private static class MzqMzIdMapperImpl implements MzqMzIdMapper {

        private MzqProcessor mzqProc = null;
        //private File mzqFile = null;
        private MzQuantMLUnmarshaller mzqUm = null;
        // Map of PeptideConsensus ID to possible peptide sequence list
        //private Map<String, List<String>> pepConOldToPepSeqsMap = new HashMap<>();
        //private Map<String, List<String>> protToPepSeqsMap = new HashMap<>();
        private Map<String, List<String>> pepConOldToPepModStringsMap
                = new HashMap<>();
        private Map<String, String> pepConOldIdToNewIdMap = new HashMap<>();
        private Map<String, String> rawToMzidMap = new HashMap<>();
        private Map<String, String> mzidFnToFileIdMap = new HashMap<>();
        private List<PeptideConsensusList> pepConLists = new ArrayList();
        private Map<String, List<String>> pepConNewIdToProtAccsMap
                = new HashMap<>();
        private Map<String, List<String>> protAccToPepConNewIdsMap
                = new HashMap<>();
        private SearchDatabase searchDB = new SearchDatabase();

        /*
         * Constructor
         */
        private MzqMzIdMapperImpl(MzQuantMLUnmarshaller mzqUm,
                                  Map<String, String> rawToMzidMap,
                                  Tolerance msTolerance)
                throws JAXBException, IOException {

            this.mzqUm = mzqUm;

            this.rawToMzidMap = rawToMzidMap;

            this.mzqProc = MzqProcessorFactory.getInstance().buildMzqProcessor(
                    mzqUm, rawToMzidMap, msTolerance);

            Map<String, List<SIIData>> featureToSIIsMap = this.mzqProc.
                    getFeatureToSIIsMap();

            Iterator<PeptideConsensusList> itPepConList = mzqUm.
                    unmarshalCollectionFromXpath(
                            MzQuantMLElement.PeptideConsensusList);

            //Map<String, List<SIIData>> combPepModStringToSIIsMap = this.mzqProc.getCombinedPepModStringToSIIsMap();
            //Map<String, List<String>> pepConIdToProtAccsMap = new HashMap<>();
            int pepIdCount = 0; // new id count for pepCon

            int idFileCount = 0;

            searchDB = this.mzqProc.getSearchDatabase();

            if (itPepConList == null) {
                throw new RuntimeException(
                        "There is no PeptideConsensusList in the mzq file.");
            }
            while (itPepConList.hasNext()) {
                PeptideConsensusList pepConList = itPepConList.next();
                List<PeptideConsensus> pepCons = pepConList.
                        getPeptideConsensus();

                // loop through to each PeptideConsensus in mzq
                for (PeptideConsensus pepCon : pepCons) {

                    int countMatchId = 0; //"count of canonical identifications" with value being the number of assays in which the ID for the PeptideSequence is found
                    int countNonMatchId = 0; //"count of non-matching identifications" with value being the number of IDs matching a different sequence
                    String pepConIdOld = pepCon.getId();

                    List<String> pepModStringList = pepConOldToPepModStringsMap.
                            get(pepConIdOld);

                    List<EvidenceRef> evdRefs = pepCon.getEvidenceRef();

                    // pepModString --> feature refs list for each pepCon
                    // Each pepCon can have any number of entries, each entry reference to one posible peptide identification with list of feature evidence
                    // each feature evidence could supportted by more than one spectrum identfication items
                    Map<String, List<String>> pepModStringToFeaturesMap
                            = new HashMap<>();
                    //loop through each EvidenceRef in the PeptideConsensus
                    //for each feature ref in evidence ref, find list of SIIData from featureToSIIsMap
                    //get peptideModString from each SIIData
                    //and build a pepModStringToFeaturesMap;
                    //also build a pepModStringToSIIsMap;
                    //TODO: check if every SII in each entry of pepModSTringToSIIsMap reperenets the same protein?
                    Map<String, List<SIIData>> pepModStringToSIIsMap
                            = new HashMap<>();
                    for (EvidenceRef evdRef : evdRefs) {
                        String ftRef = evdRef.getFeatureRef();
                        List<SIIData> ftSIIDataList = featureToSIIsMap.
                                get(ftRef);
                        List<String> ftList;
                        List<SIIData> pepSIIList;
                        if (ftSIIDataList != null) {
                            for (SIIData sd : ftSIIDataList) {
                                ftList = pepModStringToFeaturesMap.get(sd.
                                        getPeptideModString());
                                if (ftList == null) {
                                    ftList = new ArrayList();
                                    pepModStringToFeaturesMap.put(sd.
                                            getPeptideModString(), ftList);
                                }
                                ftList.add(ftRef);

                                pepSIIList = pepModStringToSIIsMap.get(sd.
                                        getPeptideModString());
                                if (pepSIIList == null) {
                                    pepSIIList = new ArrayList();
                                    pepModStringToSIIsMap.put(sd.
                                            getPeptideModString(), pepSIIList);
                                }
                                pepSIIList.add(sd);
                            }
                        }
                    }

                    // sort pepModStringToFeaturesMap by the number of feature in descending order
                    // each peptide modString in pepModStringToFeatureMap.keySet() can represent the identification of the pepCon
                    // only that has the most number of consensus features is asigned as major identification
                    List<Entry<String, List<String>>> entryList = new ArrayList(
                            pepModStringToFeaturesMap.entrySet());
                    Collections.sort(entryList, new ValueListSizeComparator());

                    for (Entry<String, List<String>> entry : entryList) {
                        String pepModString = entry.getKey();

                        if (pepModStringList == null) {
                            pepModStringList = new ArrayList();
                            pepConOldToPepModStringsMap.put(pepConIdOld,
                                                            pepModStringList);
                        }
                        pepModStringList.add(pepModString);
                    }

                    /*
                     * ************************
                     * rewrite the pepCon including giving new IDs
                     * ************************
                     */
                    if (pepModStringList != null && !pepModStringList.isEmpty()) {

                        String pepModString = pepModStringList.get(0); //get the modString of major peptide sequence
                        //SIIData siiData = combPepModStringToSIIsMap.get(pepModStringList.get(0)).get(0);
                        //Peptide peptide = siiData.getUnmarshaller().unmarshal(uk.ac.ebi.jmzidml.model.mzidml.Peptide.class, siiData.getPeptideRef());
                        String seq = pepModString;
                        if (pepModString.contains("_")) {
                            seq = pepModString.substring(0, pepModString.
                                                         indexOf('_'));
                        }
//                        if (!peptide.getPeptideSequence().equals(seq)) {
//                            System.out.println("unequal peptide sequence: from pepModString \"" + seq + "\"; from siiData \"" + peptide.getPeptideSequence() + "\".");
//                        }
                        //assign new sequence to pepCon
                        pepCon.setPeptideSequence(seq);

                        // set SearchDatabase
                        pepCon.setSearchDatabase(searchDB);

                        // assign new Id to pepCon
                        String pepConIdNew = "pepCon_" + pepIdCount;
                        pepCon.setId(pepConIdNew);
                        pepConOldIdToNewIdMap.put(pepConIdOld, pepConIdNew);
                        pepIdCount++;

                        //handle peptide modification from pepModString from mzid files
                        if (pepModString.contains("_")) {
                            String[] temp = pepModString.split("_", 2);
                            String modString = temp[1];
                            String[] mods = modString.split("_"); //modString contains mod accession and name, plus monoisotopicMassDelta and location.;
                            pepCon.getModification().clear(); // clear the old modifications if they exist
                            for (int i = 0; i < mods.length; i++) {
                                Modification pepMod = new Modification();
                                pepMod.setAvgMassDelta(Double.valueOf(
                                        mods[i + 2]));
                                pepMod.setLocation(Integer.valueOf(mods[i + 3]));
                                CvParam modCv = MzQuantMLMarshaller.
                                        createCvParam(mods[i + 1], " ", mods[i]);
                                pepMod.getCvParam().add(modCv);
                                i = i + 3;
                                pepCon.getModification().add(pepMod);
                            }
                        }

                        // handle remain sequences
                        for (int i = 1; i < pepModStringList.size(); i++) {
                            UserParam userParam = new UserParam();

                            userParam.setValue(pepModStringList.get(i));
                            userParam.setName(
                                    "Other identified sequence with modification string");
                            userParam.setType("String");
                            pepCon.getUserParam().add(userParam);

                            //calculate count of non-matching identifications
                            List<SIIData> sds = pepModStringToSIIsMap.get(
                                    pepModStringList.get(i));
                            countNonMatchId += sds.size();
                        }

                        // handle EvidenceRef
                        evdRefs = pepCon.getEvidenceRef();
                        //TIntList listOfCharge = new TIntArrayList();
                        for (EvidenceRef evdRef : evdRefs) {
                            List<SIIData> siiDataList = featureToSIIsMap.get(
                                    evdRef.getFeatureRef());
                            //if (evdRef.getIdRefs().isEmpty()) {
                            evdRef.setIdRefs(null); //remove previous reference
                            //}
                            evdRef.setIdentificationFile(null); // reset IdentificationFile;

                            if (siiDataList != null) {
                                pepCon.getCharge().clear();
                                for (SIIData sd : siiDataList) {
                                    if (pepCon.getPeptideSequence().
                                            equalsIgnoreCase(sd.getSequence())) {
                                        //get and set identificationFile
                                        IdentificationFile idFile
                                                = new IdentificationFile();
                                        String id = mzidFnToFileIdMap.get(sd.
                                                getMzidFn());
                                        if (id == null) {
                                            id = "idfile_" + idFileCount;
                                            idFileCount++;
                                            mzidFnToFileIdMap.
                                                    put(sd.getMzidFn(), id);
                                        }
                                        idFile.setId(id);
                                        evdRef.setIdentificationFile(idFile);
                                        evdRef.getIdRefs().add(sd.getId());
                                        pepCon.getCharge().add(String.valueOf(
                                                sd.getCharge()));
                                        countMatchId++;
                                    }
                                }
                            }
                        }

                        //add two userParam to pepCon
                        UserParam userParam1 = new UserParam();

                        userParam1.setValue(String.valueOf(countMatchId));
                        userParam1.setName("count of canonical identifications");
                        userParam1.setType("Int");
                        pepCon.getUserParam().add(userParam1);

                        UserParam userParam2 = new UserParam();
                        userParam2.setValue(String.valueOf(countNonMatchId));
                        userParam2.setName(
                                "count of non-matching identifications");
                        userParam2.setType("Int");
                        pepCon.getUserParam().add(userParam2);

                        // build pepConId to protein accessions map
                        SIIData siiData = pepModStringToSIIsMap.
                                get(pepModString).get(0);
                        List<PeptideEvidenceRef> pepEvdRefs = siiData.
                                getPeptideEvidenceRef();
                        List<String> protAccs = pepConNewIdToProtAccsMap.get(
                                pepConIdNew);
                        if (pepEvdRefs != null && !pepEvdRefs.isEmpty()) {
                            for (PeptideEvidenceRef pepEvdRef : pepEvdRefs) {
                                // String protAcc = pepEvdRef.getPeptideEvidence().getDBSequence().getAccession();
                                PeptideEvidence pepEvd = siiData.
                                        getUnmarshaller().unmarshal(
                                                PeptideEvidence.class,
                                                pepEvdRef.
                                                getPeptideEvidenceRef());
                                DBSequence dbSeq = siiData.getUnmarshaller().
                                        unmarshal(
                                                DBSequence.class,
                                                pepEvd.getDBSequenceRef());
                                String protAcc = dbSeq.getAccession();
                                if (protAccs == null) {
                                    protAccs = new ArrayList();
                                    pepConNewIdToProtAccsMap.put(pepConIdNew,
                                                                 protAccs);
                                }
                                protAccs.add(protAcc);
                            }
                        }
                    } else {
                        // assign new Id to pepCon
                        String pepConIdNew = "pepCon_" + pepIdCount;
                        pepCon.setId(pepConIdNew);
                        pepConOldIdToNewIdMap.put(pepConIdOld, pepConIdNew);
                        pepIdCount++;

                        // set SearchDatabase
                        pepCon.setSearchDatabase(searchDB);

                        // handle EvidenceRef
                        // remove previous idRefs as no consensus exist for this peptide
                        evdRefs = pepCon.getEvidenceRef();
                        for (EvidenceRef evdRef : evdRefs) {
                            evdRef.setIdRefs(null); //remove previous reference
                            evdRef.setIdentificationFile(null); // reset IdentificationFile;                       
                        }
                        UserParam userParam = new UserParam();
                        //userParam.setValue();
                        userParam.setName("No consensus exist in this peptide");
                        userParam.setType("String");
                        pepCon.getUserParam().add(userParam);
                    }
                }

                // change the object id in each row
                List<QuantLayer<IdOnly>> assayQLs = pepConList.
                        getAssayQuantLayer();
                for (QuantLayer assayQL : assayQLs) {
                    List<Row> rows = assayQL.getDataMatrix().getRow();
                    if (rows != null) {
                        for (Row row : rows) {
                            row.setObjectRef(pepConOldIdToNewIdMap.get(row.
                                    getObjectRef()));
                        }
                    }
                }

                List<GlobalQuantLayer> globalQLs = pepConList.
                        getGlobalQuantLayer();
                for (GlobalQuantLayer globalQL : globalQLs) {
                    List<Row> rows = globalQL.getDataMatrix().getRow();
                    if (rows != null) {
                        for (Row row : rows) {
                            row.setObjectRef(pepConOldIdToNewIdMap.get(row.
                                    getObjectRef()));
                        }
                    }
                }

                List<QuantLayer<IdOnly>> svQLs = pepConList.
                        getStudyVariableQuantLayer();
                for (QuantLayer svQL : svQLs) {
                    List<Row> rows = svQL.getDataMatrix().getRow();
                    if (rows != null) {
                        for (Row row : rows) {
                            row.setObjectRef(pepConOldIdToNewIdMap.get(row.
                                    getObjectRef()));
                        }
                    }
                }

                RatioQuantLayer ratioQL = pepConList.getRatioQuantLayer();
                if (ratioQL != null) {
                    List<Row> rows = ratioQL.getDataMatrix().getRow();
                    if (rows != null) {
                        for (Row row : rows) {
                            row.setObjectRef(pepConOldIdToNewIdMap.get(row.
                                    getObjectRef()));
                        }
                    }
                }
                pepConLists.add(pepConList);
            }

            // build protAccToPepConNewIdMap
            buildProtAccToPepConNewIdsMap();
        }

        static class ValueListSizeComparator implements
                Comparator<Entry<String, List<String>>>, Serializable {

            private final static long serialVersionUID = 30L;

            @Override
            // descending 
            public int compare(Entry<String, List<String>> o1,
                               Entry<String, List<String>> o2) {
                List<String> key1 = o1.getValue();
                List<String> key2 = o2.getValue();
                return key2.size() - key1.size();
            }

        }

        @Override
        public void createMappedFile(File outputFile)
                throws JAXBException, IOException {

            // retrieve every attributes and elements from the mzQuantML file
            String mzqId = mzqUm.getMzQuantMLId();
            //String mzqName = mzqUm.getMzQuantMLName();
            //String mzqVersion = mzqUm.getMzQuantMLVersion();

            // three ways of unmarshalling an mzQuantML element: 
            CvList cvList = mzqUm.unmarshal(
                    CvList.class); //1. class name
            Provider provider = mzqUm.unmarshal(MzQuantMLElement.Provider);
            AuditCollection ac = mzqUm.unmarshal(
                    MzQuantMLElement.AuditCollection); //2. member of MzQuantMLElement
            AnalysisSummary as = mzqUm.unmarshal("/MzQuantML/AnalysisSummary"); //3a. XPath
            InputFiles inputFiles = mzqUm.unmarshal(MzQuantMLElement.InputFiles.
                    getXpath()); //3b. XPath
            SoftwareList softList = mzqUm.unmarshal(
                    MzQuantMLElement.SoftwareList);
            DataProcessingList dpList = mzqUm.unmarshal(
                    MzQuantMLElement.DataProcessingList);
            Iterator<BibliographicReference> brIter
                    = mzqUm.unmarshalCollectionFromXpath(
                            MzQuantMLElement.BibliographicReference);
            AssayList assayList = mzqUm.unmarshal(MzQuantMLElement.AssayList);
            StudyVariableList svList = mzqUm.unmarshal(
                    MzQuantMLElement.StudyVariableList);
            RatioList ratioList = mzqUm.unmarshal(MzQuantMLElement.RatioList);
            //ProteinGroupList protGrpList = mzqUm.unmarshal(MzQuantMLElement.ProteinGroupList);
            ProteinList protList = mzqUm.unmarshal(MzQuantMLElement.ProteinList);
            SmallMoleculeList smallMolList = mzqUm.unmarshal(
                    MzQuantMLElement.SmallMoleculeList);
            Iterator<FeatureList> ftListIter = mzqUm.
                    unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);

            //build a mzid file name to mzid file id map
            IdentificationFiles idFiles = new IdentificationFiles();
            //Map<String, String> mzidFnToFileIdMap = new HashMap<>();
            FileFormat ff = new FileFormat();

            Cv cv = new Cv();
            cv.setId(MzqDataConstants.CvIDPSIMS);
            cv.setUri(MzqDataConstants.CvUriPSIMS);
            cv.setFullName(MzqDataConstants.CvNamePSIMS);
            cv.setVersion(MzqDataConstants.CvVerPSIMS);

            CvParam cp = new CvParam();
            cp.setAccession("MS:1002073");
            cp.setName("mzIdentML format");
            cp.setCv(cv);
            ff.setCvParam(cp);

            //int count = 0;
            for (String mzidFileName : rawToMzidMap.values()) {
                IdentificationFile idFile = new IdentificationFile();
                idFile.setFileFormat(ff);
                //String id = "idfile_" + count;                
                String id = mzidFnToFileIdMap.get(new File(mzidFileName).
                        getName());
                idFile.setId(id);
                //count++;

                idFile.setLocation(new File(mzidFileName).getCanonicalPath());
                //TODO; to check
                idFile.setName(mzidFileName);
                //mzidFnToFileIdMap.put(mzidFn, id);

                idFiles.getIdentificationFile().add(idFile);
            }

            //Map<String, List<SIIData>> featureToSIIsMap = this.mzqProc.getFeatureToSIIsMap();
            MzQuantMLMarshaller m = new MzQuantMLMarshaller();
            OutputStreamWriter writer = null;

            try {
                FileOutputStream fos = new FileOutputStream(outputFile);
                writer = new OutputStreamWriter(fos, "UTF-8");

                // XML header
                writer.write(MzQuantMLMarshaller.createXmlHeader() + "\n");

                // mzQuantML start tag
                writer.write(MzQuantMLMarshaller.createMzQuantMLStartTag(mzqId)
                        + "\n");

                if (cvList != null) {
                    m.marshall(cvList, writer);
                    writer.write("\n");
                }
                if (provider != null) {
                    m.marshall(provider, writer);
                    writer.write("\n");
                }
                if (ac != null) {
                    m.marshall(ac, writer);
                    writer.write("\n");
                }
                if (as != null) {
                    m.marshall(as, writer);
                    writer.write("\n");
                }
                if (inputFiles != null) {
                    inputFiles.setIdentificationFiles(idFiles);
                    inputFiles.getSearchDatabase().clear();
                    inputFiles.getSearchDatabase().add(searchDB);
                    m.marshall(inputFiles, writer);
                    writer.write("\n");
                }
                if (softList != null) {
                    m.marshall(softList, writer);
                    writer.write("\n");
                }
                if (dpList != null) {
                    m.marshall(dpList, writer);
                    writer.write("\n");
                }
                while (brIter.hasNext()) {
                    BibliographicReference bibRef = brIter.next();
                    m.marshall(bibRef, writer);
                    writer.write("\n");
                }
                if (assayList != null) {
                    m.marshall(assayList, writer);
                    writer.write("\n");
                }
                if (svList != null) {
                    m.marshall(svList, writer);
                    writer.write("\n");
                }
                if (ratioList != null) {
                    m.marshall(ratioList, writer);
                    writer.write("\n");
                }

                // new ProteinList
                ProteinList newProtList = new ProteinList();
                if (protList == null) {
                    newProtList.setId("ProteinList1");
                } else {
                    newProtList.setId(protList.getId());
                }

                int protCount = 0;
                for (Entry<String, List<String>> entry
                        : protAccToPepConNewIdsMap.entrySet()) {
                    String protAcc = entry.getKey();
                    Protein protein = new Protein();
                    protein.setSearchDatabase(searchDB);
                    String protId = "prot_" + protCount;
                    protein.setId(protId);
                    protCount++;
                    protein.setAccession(protAcc);
                    List<String> pepConNewIds = entry.getValue();
                    for (String pepConNewId : pepConNewIds) {
//                        PeptideConsensus pepCon = new PeptideConsensus();
//                        pepCon.setId(pepConNewId);
//                        protein.getPeptideConsensuses().add(pepCon);
                        protein.getPeptideConsensusRefs().add(pepConNewId);
                    }
                    newProtList.getProtein().add(protein);
                }

                if (!protAccToPepConNewIdsMap.isEmpty()) {
                    m.marshall(newProtList, writer);
                    writer.write("\n");
                }

                for (PeptideConsensusList pepConList : this.pepConLists) {
                    m.marshall(pepConList, writer);
                    writer.write("\n");
                }

                if (smallMolList != null) {
                    m.marshall(smallMolList, writer);
                    writer.write("\n");
                }

                while (ftListIter.hasNext()) {
                    FeatureList ftList = ftListIter.next();
                    m.marshall(ftList, writer);
                    writer.write("\n");
                }
                writer.write(MzQuantMLMarshaller.createMzQuantMLClosingTag());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ex) {
                        Logger.getLogger(MzqMzIdMapperFactory.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                }
            }

        }

        private void buildProtAccToPepConNewIdsMap() {
            for (Entry<String, List<String>> entry : pepConNewIdToProtAccsMap.
                    entrySet()) {
                String pepConNewId = entry.getKey();
                List<String> protAccs = entry.getValue();
                if (protAccs != null) {
                    for (String prot : protAccs) {
                        List<String> pepConNewIds = protAccToPepConNewIdsMap.
                                get(prot);
                        if (pepConNewIds == null) {
                            pepConNewIds = new ArrayList();
                            protAccToPepConNewIdsMap.put(prot, pepConNewIds);
                        }
                        pepConNewIds.add(pepConNewId);
                    }
                }
            }
        }

    }

}
