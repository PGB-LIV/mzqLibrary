/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.idmapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import uk.ac.ebi.jmzidml.model.mzidml.*;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.*;
import uk.ac.liv.jmzqml.model.mzqml.AuditCollection;
import uk.ac.liv.jmzqml.model.mzqml.BibliographicReference;
import uk.ac.liv.jmzqml.model.mzqml.Cv;
import uk.ac.liv.jmzqml.model.mzqml.CvList;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.FileFormat;
import uk.ac.liv.jmzqml.model.mzqml.Modification;
import uk.ac.liv.jmzqml.model.mzqml.Provider;
import uk.ac.liv.jmzqml.model.mzqml.SearchDatabase;
import uk.ac.liv.jmzqml.model.mzqml.UserParam;
import uk.ac.liv.jmzqml.xml.io.*;
import uk.ac.liv.mzqlib.idmapper.data.SIIData;
import uk.ac.liv.mzqlib.utils.MzidToMzqElementConvertor;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 24-Jun-2013 14:06:09
 */
public class MzqMzIdMapperFactory {

    private static final MzqMzIdMapperFactory instance = new MzqMzIdMapperFactory();
    private static final String CvIDPSIMS = "PSI-MS";
    private static final String CvNamePSIMS = "Proteomics Standards Initiative Mass Spectrometry Vocabularies";
    private static final String CvUriPSIMS = "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/"
            + "mzML/controlledVocabulary/psi-ms.obo";
    private static final String CvVerPSIMS = "3.61.0";

    public MzqMzIdMapperFactory() {
    }

    public static MzqMzIdMapperFactory getInstance() {
        return instance;
    }

    public MzqMzIdMapper buildMzqMzIdMapper(MzQuantMLUnmarshaller mzqUm,
                                            Map rawToMzidMap)
            throws JAXBException {
        return new MzqMzIdMapperImpl(mzqUm, rawToMzidMap);
    }

    private class MzqMzIdMapperImpl implements MzqMzIdMapper {

        private MzqProcessor mzqProc = null;
        //private File mzqFile = null;
        private MzQuantMLUnmarshaller mzqUm = null;
        // Map of PeptideConsensus ID to possible peptide sequence list
        private Map<String, List<String>> pepConOldToPepSeqsMap = new HashMap<>();
        private Map<String, List<String>> protToPepSeqsMap = new HashMap<>();
        private Map<String, List<String>> pepConOldToPepModStringsMap = new HashMap<>();
        private Map<String, String> pepConOldIdToNewIdMap = new HashMap<>();
        private Map<String, String> rawToMzidMap = null;
        private Map<String, String> mzidFnToFileIdMap = new HashMap<>();
        List<PeptideConsensusList> pepConLists = new ArrayList();
        private Map<String, List<String>> pepConNewIdToProtAccsMap = new HashMap<>();
        private Map<String, List<String>> protAccToPepConNewIdsMap = new HashMap<>();
        private SearchDatabase searchDB = new SearchDatabase();

        /*
         * Constructor
         */
        private MzqMzIdMapperImpl(MzQuantMLUnmarshaller mzqUm,
                                  Map rawToMzidMap)
                throws JAXBException {

            this.mzqUm = mzqUm;

            this.rawToMzidMap = rawToMzidMap;

            this.mzqProc = MzqProcessorFactory.getInstance().buildMzqProcessor(mzqUm, rawToMzidMap);

            Map<String, List<SIIData>> featureToSIIsMap = this.mzqProc.getFeatureToSIIsMap();

            Iterator<PeptideConsensusList> itPepConList = mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.PeptideConsensusList);

            Map<String, List<SIIData>> combPepModStringToSIIsMap = this.mzqProc.getCombinedPepModStringToSIIsMap();

            Map<String, List<String>> pepConIdToProtAccsMap = new HashMap<>();

            int pepIdCount = 0; // new id count for pepCon

            int idFileCount = 0;

            while (itPepConList.hasNext()) {
                PeptideConsensusList pepConList = itPepConList.next();
                List<PeptideConsensus> pepCons = pepConList.getPeptideConsensus();

                // loop through to each PeptideConsensus
                for (PeptideConsensus pepCon : pepCons) {
                    if (pepCon.getId().equals("pep_GLFIIDDKGILR_3_182")) {
                        System.out.println("stop");
                    }
                    String pepConIdOld = pepCon.getId();
                    List<String> pepSeqList = pepConOldToPepSeqsMap.get(pepConIdOld);
                    List<String> pepModStringList = pepConOldToPepModStringsMap.get(pepConIdOld);

                    List<EvidenceRef> evdRefs = pepCon.getEvidenceRef();

                    // pepMod --> feature refs for each pepCon
                    Map<String, List<String>> pepModStringToFeaturesMap = new HashMap<>();
                    //loop through each EvidenceRef in the PeptideConsensus
                    //and build a siiDataToFeaturesMap;
                    for (EvidenceRef evdRef : evdRefs) {
                        String ftRef = evdRef.getFeatureRef();
                        List<SIIData> ftSIIDataList = featureToSIIsMap.get(ftRef);
                        List<String> ftList;
                        if (ftSIIDataList != null) {
                            for (SIIData sd : ftSIIDataList) {
                                ftList = pepModStringToFeaturesMap.get(sd.getPeptideModString());
                                if (ftList == null) {
                                    ftList = new ArrayList();
                                    pepModStringToFeaturesMap.put(sd.getPeptideModString(), ftList);
                                }
                                ftList.add(ftRef);
                            }
                        }
                    }

                    // sort siiDataToFeaturesMap by the number of feature in descending order
                    List<Entry<String, List<String>>> entryList = new ArrayList(pepModStringToFeaturesMap.entrySet());
                    Collections.sort(entryList, new Comparator<Entry<String, List<String>>>() {

                        @Override
                        // descending 
                        public int compare(Entry<String, List<String>> o1,
                                           Entry<String, List<String>> o2) {
                            List<String> key1 = o1.getValue();
                            List<String> key2 = o2.getValue();
                            return key2.size() - key1.size();
                        }

                    });

                    for (Entry<String, List<String>> entry : entryList) {
                        String pepModString = entry.getKey();
                        //SIIData sd = entry.getKey();
//                        if (pepSeqList == null) {
//                            pepSeqList = new ArrayList();
//                            pepConToPepSeqsMap.put(pepConId, pepSeqList);
//                        }
//                        pepSeqList.add(sd.getSequence());

                        if (pepModStringList == null) {
                            pepModStringList = new ArrayList();
                            pepConOldToPepModStringsMap.put(pepConIdOld, pepModStringList);
                        }
                        pepModStringList.add(pepModString);
                    }

                    /*
                     * ************************
                     * rewrite the pepCon
                     * ************************
                     */
                    if (pepModStringList != null && !pepModStringList.isEmpty()) {
                        SIIData siiData = combPepModStringToSIIsMap.get(pepModStringList.get(0)).get(0);
                        Peptide peptide = siiData.getUnmarshaller().unmarshal(uk.ac.ebi.jmzidml.model.mzidml.Peptide.class, siiData.getPeptideRef());

                        //assign new sequence to pepCon
                        pepCon.setPeptideSequence(peptide.getPeptideSequence());

                        // assign new Id to pepCon
                        String pepConIdNew = "pepCon_" + pepIdCount;
                        pepCon.setId(pepConIdNew);
                        pepConOldIdToNewIdMap.put(pepConIdOld, pepConIdNew);
                        pepIdCount++;

                        // Modification
                        if (peptide.getModification() != null && !peptide.getModification().isEmpty()) {
                            List<Modification> mzqMods = MzidToMzqElementConvertor.convertMzidModsToMzqMods(peptide.getModification());
                            pepCon.getModification().addAll(mzqMods);
                        }

                        // handle remain sequences

                        for (int i = 1; i < pepModStringList.size(); i++) {
                            UserParam userParam = new UserParam();

                            userParam.setValue(pepModStringList.get(i));
                            userParam.setName("Other identified sequence");
                            userParam.setType("String");
                            pepCon.getUserParam().add(userParam);
                        }

                        // handle EvidenceRef
                        evdRefs = pepCon.getEvidenceRef();
                        //TIntList listOfCharge = new TIntArrayList();
                        for (EvidenceRef evdRef : evdRefs) {
                            List<SIIData> siiDataList = featureToSIIsMap.get(evdRef.getFeatureRef());
                            //if (evdRef.getIdRefs().isEmpty()) {
                            evdRef.setIdRefs(null); //remove previous reference
                            //}
                            evdRef.setIdentificationFile(null); // reset IdentificationFile;

                            if (siiDataList != null) {
                                pepCon.getCharge().clear();
                                for (SIIData sd : siiDataList) {
                                    if (pepCon.getPeptideSequence().equalsIgnoreCase(sd.getSequence())) {
                                        //get and set identificationFile
                                        IdentificationFile idFile = new IdentificationFile();
                                        String id = mzidFnToFileIdMap.get(sd.getMzidFn());
                                        if (id == null) {
                                            id = "idfile_" + idFileCount;
                                            idFileCount++;
                                            mzidFnToFileIdMap.put(sd.getMzidFn(), id);
                                        }
                                        idFile.setId(id);
                                        evdRef.setIdentificationFile(idFile);
                                        evdRef.getIdRefs().add(sd.getId());
                                        pepCon.getCharge().add(String.valueOf(sd.getCharge()));
                                    }
                                }
                            }
                        }


                        // build pepConId to protein accessions map
                        List<PeptideEvidenceRef> pepEvdRefs = siiData.getPeptideEvidenceRef();
                        List<String> protAccs = pepConNewIdToProtAccsMap.get(pepConIdNew);
                        if (pepEvdRefs != null && !pepEvdRefs.isEmpty()) {
                            for (PeptideEvidenceRef pepEvdRef : pepEvdRefs) {
                                // String protAcc = pepEvdRef.getPeptideEvidence().getDBSequence().getAccession();
                                PeptideEvidence pepEvd = siiData.getUnmarshaller().unmarshal(uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence.class, pepEvdRef.getPeptideEvidenceRef());
                                DBSequence dbSeq = siiData.getUnmarshaller().unmarshal(uk.ac.ebi.jmzidml.model.mzidml.DBSequence.class, pepEvd.getDBSequenceRef());
                                String protAcc = dbSeq.getAccession();
                                if (protAccs == null) {
                                    protAccs = new ArrayList();
                                    pepConNewIdToProtAccsMap.put(pepConIdNew, protAccs);
                                }
                                protAccs.add(protAcc);
                            }
                        }
                    }
                    else {
                        // assign new Id to pepCon
                        String pepConIdNew = "pepCon_" + pepIdCount;
                        pepCon.setId(pepConIdNew);
                        pepConOldIdToNewIdMap.put(pepConIdOld, pepConIdNew);
                        pepIdCount++;

                        //pepCon.setPeptideSequence("");

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
                List<QuantLayer> assayQLs = pepConList.getAssayQuantLayer();
                for (QuantLayer assayQL : assayQLs) {
                    List<Row> rows = assayQL.getDataMatrix().getRow();
                    if (rows != null) {
                        for (Row row : rows) {
                            row.setObjectRef(pepConOldIdToNewIdMap.get(row.getObjectRef()));
                        }
                    }
                }

                List<GlobalQuantLayer> globalQLs = pepConList.getGlobalQuantLayer();
                for (GlobalQuantLayer globalQL : globalQLs) {
                    List<Row> rows = globalQL.getDataMatrix().getRow();
                    if (rows != null) {
                        for (Row row : rows) {
                            row.setObjectRef(pepConOldIdToNewIdMap.get(row.getObjectRef()));
                        }
                    }
                }

                List<QuantLayer> svQLs = pepConList.getStudyVariableQuantLayer();
                for (QuantLayer svQL : svQLs) {
                    List<Row> rows = svQL.getDataMatrix().getRow();
                    if (rows != null) {
                        for (Row row : rows) {
                            row.setObjectRef(pepConOldIdToNewIdMap.get(row.getObjectRef()));
                        }
                    }
                }

                RatioQuantLayer ratioQL = pepConList.getRatioQuantLayer();
                if (ratioQL != null) {
                    List<Row> rows = ratioQL.getDataMatrix().getRow();
                    if (rows != null) {
                        for (Row row : rows) {
                            row.setObjectRef(pepConOldIdToNewIdMap.get(row.getObjectRef()));
                        }
                    }
                }
                pepConLists.add(pepConList);
            }

            // build protAccToPepConNewIdMap
            buildProtAccToPepConNewIdsMap();
        }

        @Override
        public void createMappedFile(String output)
                throws JAXBException {

            // retrieve every attributes and elements from the mzQuantML file
            String mzqId = mzqUm.getMzQuantMLId();
            String mzqName = mzqUm.getMzQuantMLName();
            String mzqVersion = mzqUm.getMzQuantMLVersion();

            // three ways of unmarshalling an mzQuantML element: 
            CvList cvList = mzqUm.unmarshal(uk.ac.liv.jmzqml.model.mzqml.CvList.class); //1. class name
            Provider provider = mzqUm.unmarshal(MzQuantMLElement.Provider);
            AuditCollection ac = mzqUm.unmarshal(MzQuantMLElement.AuditCollection); //2. member of MzQuantMLElement
            AnalysisSummary as = mzqUm.unmarshal("/MzQuantML/AnalysisSummary"); //3a. XPath
            InputFiles inputFiles = mzqUm.unmarshal(MzQuantMLElement.InputFiles.getXpath()); //3b. XPath
            SoftwareList softList = mzqUm.unmarshal(MzQuantMLElement.SoftwareList);
            DataProcessingList dpList = mzqUm.unmarshal(MzQuantMLElement.DataProcessingList);
            Iterator<BibliographicReference> brIter =
                    mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.BibliographicReference);
            AssayList assayList = mzqUm.unmarshal(MzQuantMLElement.AssayList);
            StudyVariableList svList = mzqUm.unmarshal(MzQuantMLElement.StudyVariableList);
            RatioList ratioList = mzqUm.unmarshal(MzQuantMLElement.RatioList);
            ProteinGroupList protGrpList = mzqUm.unmarshal(MzQuantMLElement.ProteinGroupList);
            ProteinList protList = mzqUm.unmarshal(MzQuantMLElement.ProteinList);
            SmallMoleculeList smallMolList = mzqUm.unmarshal(MzQuantMLElement.SmallMoleculeList);
            Iterator<FeatureList> ftListIter = mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);


            MzQuantMLMarshaller marsh = new MzQuantMLMarshaller();

            //build a mzid file name to mzid file id map
            IdentificationFiles idFiles = new IdentificationFiles();
            //Map<String, String> mzidFnToFileIdMap = new HashMap<>();
            FileFormat ff = new FileFormat();

            Cv cv = new Cv();
            cv.setId(CvIDPSIMS);
            cv.setUri(CvUriPSIMS);
            cv.setFullName(CvNamePSIMS);
            cv.setVersion(CvVerPSIMS);

            CvParam cp = new CvParam();
            cp.setAccession("MS:1002073");
            cp.setName("mzIdentML format");
            cp.setCv(cv);
            ff.setCvParam(cp);

            //int count = 0;
            for (String mzidFn : rawToMzidMap.values()) {
                IdentificationFile idFile = new IdentificationFile();
                idFile.setFileFormat(ff);
                //String id = "idfile_" + count;
                idFile.setId(mzidFnToFileIdMap.get(mzidFn));
                //count++;
                File f = new File(mzidFn);
                idFile.setLocation(f.getAbsolutePath());
                //TODO; to check
                idFile.setName(mzidFn);
                //mzidFnToFileIdMap.put(mzidFn, id);

                idFiles.getIdentificationFile().add(idFile);
            }

            inputFiles.setIdentificationFiles(idFiles);

            //Map<String, List<SIIData>> featureToSIIsMap = this.mzqProc.getFeatureToSIIsMap();

            MzQuantMLMarshaller m = new MzQuantMLMarshaller();
            FileWriter writer = null;

            try {
                writer = new FileWriter(output);

                // XML header
                writer.write(m.createXmlHeader() + "\n");

                // mzQuantML start tag
                writer.write(m.createMzQuantMLStartTag(mzqId) + "\n");

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
                newProtList.setId(protList.getId());
                int protCount = 0;
                for (String protAcc : protAccToPepConNewIdsMap.keySet()) {
                    Protein protein = new Protein();
                    String protId = "prot_" + protCount;
                    protein.setId(protId);
                    protCount++;
                    protein.setAccession(protAcc);
                    List<String> pepConNewIds = protAccToPepConNewIdsMap.get(protAcc);
                    for (String pepConNewId : pepConNewIds) {
//                        PeptideConsensus pepCon = new PeptideConsensus();
//                        pepCon.setId(pepConNewId);
//                        protein.getPeptideConsensuses().add(pepCon);
                        protein.getPeptideConsensusRefs().add(pepConNewId);
                    }
                    newProtList.getProtein().add(protein);
                }
                m.marshall(newProtList, writer);
                writer.write("\n");

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
                writer.write(m.createMzQuantMLClosingTag());
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
            finally {
                if (writer != null) {
                    try {
                        writer.close();
                    }
                    catch (IOException ex) {
                        Logger.getLogger(MzqMzIdMapperFactory.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }

        private void buildProtAccToPepConNewIdsMap() {
            for (String pepConNewId : pepConNewIdToProtAccsMap.keySet()) {
                List<String> protAccs = pepConNewIdToProtAccsMap.get(pepConNewId);
                if (protAccs != null) {
                    for (String prot : protAccs) {
                        List<String> pepConNewIds = protAccToPepConNewIdsMap.get(prot);
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
