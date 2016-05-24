
package uk.ac.liv.mzqlib.progenesis.converter;

import au.com.bytecode.opencsv.CSVReader;
import gnu.trove.list.TDoubleList;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TDoubleProcedure;
import gnu.trove.procedure.TIntDoubleProcedure;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.lang3.mutable.MutableInt;

import uk.ac.liv.pgb.jmzqml.model.mzqml.*;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.mzqlib.constants.MzqDataConstants;
import uk.ac.liv.mzqlib.maxquant.converter.MaxquantMzquantmlConverter;
import uk.ac.liv.mzqlib.progenesis.reader.ProgenesisFeatureListReader;
import uk.ac.liv.mzqlib.progenesis.reader.ProgenesisProteinListReader;

/**
 *
 * @author Da Qi
 */
public class ProgenMzquantmlConverter {

    private static final Boolean RawFeatureQuantitation = Boolean.FALSE;
    private static final Boolean PeptideLevelQuantitation = Boolean.TRUE;
    private static final Boolean ProteinLevelQuantitation = Boolean.FALSE;
    private static final Boolean ProteinGroupLevelQuantitation = Boolean.TRUE;
    private static final String RAW_ONLY = "raw";
    private static final String NORM_ONLY = "norm";
    //private final String RAW_PLUS_NORM = "both";
    //
    private static final String CvIDPSIMS = "PSI-MS";
    private static final String CvNamePSIMS
            = "Proteomics Standards Initiative Mass Spectrometry Vocabularies";
    private static final String CvUriPSIMS
            = "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/"
            + "mzML/controlledVocabulary/psi-ms.obo";
    private static final String CvVerPSIMS = "2.25.0";
    private static final String CvIDUNIMOD = "UNIMOD";
    private static final String CvNameUNIMOD = "Unimod";
    private static final String CvUriUNIMOD
            = "http://www.unimod.org/obo/unimod.obo";

    // MzQuantML elements
    private CvList cvList = null;
//    private AuditCollection ac;
    private AnalysisSummary as = null;
    private InputFiles inputFiles = null;
    private SoftwareList softList = null;
//    private DataProcessingList dpList;
    private List<BibliographicReference> brList = null;
    private AssayList assayList = null;
    private StudyVariableList svList = null;
    //private RatioList ratioList = null;
    private ProteinGroupList protGrpList = null;
    private ProteinList protList = null;
    private List<PeptideConsensusList> pepConListList = null;
    private List<FeatureList> ftListList = null;
//    private SmallMoleculeList smallMolList;

    //
    final DecimalFormat format = new DecimalFormat("#.###");
    List<String> assayListFrReader;
    Set<String> proteinList; //list of protein accessions from either feature list or protein list; 
    Software software;
    Map<String, String> rawFileNameIdMap;
    Label label;
    Map<String, Set<String>> studyGroupMap;
    Map<String, String> assayNameIdMap;
    final TIntObjectMap<String> proteinAccessionsMap = new TIntObjectHashMap<>();
    Map<String, Set<String>> proteinPeptidesMap;
    SearchDatabase db;
    Map<String, TIntSet> peptideMap;
    TIntObjectMap<Boolean> useInQuantMap;
    TIntObjectMap<String> flIndexMap;
    TIntIntMap chrMap;
    Map<String, String> proteinGroupNameIdMap;
    Map<String, String> proteinAccessionIdMap;
    TIntDoubleMap confidenceMap;
    TIntDoubleMap anovaMap;
    TIntDoubleMap mfcMap;
    TIntObjectMap<TDoubleList> normAbMap; // from protein list
    TIntObjectMap<TDoubleList> rawAbMap; // from protein list
    TIntObjectMap<TDoubleList> nabMap; // from feature list
    TIntObjectMap<TDoubleList> rabMap; // from feature list
    TIntObjectMap<TDoubleList> retenMap; // from feature list
    TIntDoubleMap masterRtMap; // from feature list
    TIntDoubleMap mzMap; // from feature list
    TIntDoubleMap rtWinMap;
    TIntDoubleMap scoreMap;
    TIntObjectMap<String> modificationMap;
    TIntObjectMap<String> peptideDupMap;
    Map<String, List<Feature>> peptideFeaturesMap;
    Map<String, String> featureAssNameMap;
    Map<String, List<String>> seqPsmidMap;
    Map<String, String> psmidAssMap;

    //
    private String flFn;
    private String plFn;
    private String idFn;
    private char separator;

    /**
     * Constructor of ProgenMzquantmlConverter with input parameters.
     *
     * @param flFn feature/peptide result file name, e.g feature.csv or
     *             peptide.csv
     * @param plFn protein result file name
     * @param idFn mzIdentML file name
     */
    public ProgenMzquantmlConverter(String flFn, String plFn, String idFn) {
        this(flFn, plFn, idFn, ',');
    }

    /**
     * Constructor of ProgenMzquantmlConverter with input parameters.
     *
     * @param flFn feature/peptide result file name, e.g feature.csv or
     *             peptide.csv
     * @param plFn protein result file name
     * @param idFn mzIdentML file name
     * @param sep  separator of input files
     */
    public ProgenMzquantmlConverter(String flFn, String plFn, String idFn,
                                    char sep) {
        this.flFn = flFn;
        this.plFn = plFn;
        this.idFn = idFn;
        this.separator = sep;
    }

    private void createBibliographicReferenceList() {

        //TODO: this is only for example file, remove it for generic use
        brList = new ArrayList<>();

        BibliographicReference bibliRef = new BibliographicReference();
        bibliRef.setAuthors(
                "D. Qi, P. Brownridge, D. Xia, K. Mackay, F. F. Gonzalez-Galarza, J. Kenyani, V. Harman, R. J. Beynon and A. R. Jones");
        bibliRef.setDoi("doi:10.1089/omi.2012.0042");
        bibliRef.setId("BF_DQ1");
        bibliRef.setIssue("9");
        bibliRef.setPages("489-495");
        bibliRef.setTitle(
                "A software toolkit and interface for performing stable isotope labelling and top3 quantification using Progenesis LC-MS");
        bibliRef.setVolume("16");
        bibliRef.setPublication("OMICS: A Journal of Integrative Biology");
        bibliRef.setYear(2012);

        brList.add(bibliRef);
    }

    private void createCvList() {

        cvList = new CvList();
        List<Cv> cvs = cvList.getCv();

        // psi-ms
        Cv cv = new Cv();
        cv.setId(CvIDPSIMS);
        cv.setUri(CvUriPSIMS);
        cv.setFullName(CvNamePSIMS);
        cv.setVersion(CvVerPSIMS);
        cvs.add(cv);

        //unimod
        Cv cv_unimod = new Cv();
        cv_unimod.setId(CvIDUNIMOD);
        cv_unimod.setUri(CvUriUNIMOD);
        cv_unimod.setFullName(CvNameUNIMOD);
        cvs.add(cv_unimod);

        label = new Label();
        CvParam labelCvParam = new CvParam();
        labelCvParam.setAccession("MS:1002038");
        labelCvParam.setName("unlabeled sample");
        labelCvParam.setCv(cv);
        List<ModParam> modParams = label.getModification();
        ModParam modParam = new ModParam();
        modParam.setCvParam(labelCvParam);
        modParams.add(modParam);
    }

    private void createAnalysisSummary() {

        as = new AnalysisSummary();
        as.getParamGroup().add(createCvParam(MzqDataConstants.LABEL_FREE,
                                             "PSI-MS",
                                             MzqDataConstants.LABEL_FREE_ACCESSION));

        CvParam analysisSummaryCv = createCvParam(
                "label-free raw feature quantitation", "PSI-MS", "MS:1002019");
        analysisSummaryCv.setValue(RawFeatureQuantitation.toString());
        as.getParamGroup().add(analysisSummaryCv);

        analysisSummaryCv = createCvParam(
                "label-free peptide level quantitation", "PSI-MS", "MS:1002020");
        analysisSummaryCv.setValue(PeptideLevelQuantitation.toString());
        as.getParamGroup().add(analysisSummaryCv);

        analysisSummaryCv = createCvParam(
                "label-free protein level quantitation", "PSI-MS", "MS:1002021");
        analysisSummaryCv.setValue(ProteinLevelQuantitation.toString());
        as.getParamGroup().add(analysisSummaryCv);

        analysisSummaryCv = createCvParam(
                "label-free proteingroup level quantitation", "PSI-MS",
                "MS:1002022");
        analysisSummaryCv.setValue(ProteinGroupLevelQuantitation.toString());
        as.getParamGroup().add(analysisSummaryCv);

    }

    private void createInputFiles()
            throws FileNotFoundException, IOException {

        inputFiles = new InputFiles();
        List<RawFilesGroup> rawFilesGroupList = inputFiles.getRawFilesGroup();

        rawFileNameIdMap = new HashMap<>();
        Map<String, List<String>> rgIdrawIdMap = new HashMap<>();
        int raw_i = 0;
        for (String ass : assayListFrReader) {
            String rawFn = ass + ".raw";
            String rawId = "raw_" + Integer.toString(raw_i);
            RawFilesGroup rawFilesGroup = new RawFilesGroup();
            List<RawFile> rawFilesList = rawFilesGroup.getRawFile();
            RawFile rawFile = new RawFile();
            rawFile.setName(rawFn);
            rawFile.setId(rawId);
            /*
             * make up some raw file locations as we don't know the real
             * location from progenesis files
             */
            rawFile.setLocation("../msmsdata/" + rawFn);
            rawFileNameIdMap.put(rawFn, rawId);
            rawFilesList.add(rawFile);

            String rgId = "rg_" + Integer.toString(raw_i);
            rawFilesGroup.setId(rgId);
            rawFilesGroupList.add(rawFilesGroup);

            /*
             * build rgIdrawIdMap raw files group id as key raw file ids
             * (ArrayList) as value
             */
            List<String> rawIds = rgIdrawIdMap.get(rgId);
            if (rawIds == null) {
                rawIds = new ArrayList<>();
                rgIdrawIdMap.put(rgId, rawIds);
            }
            rawIds.add(rawId);

            raw_i++;
        }

        //add search databases
        List<SearchDatabase> searchDBs = inputFiles.getSearchDatabase();
        db = new SearchDatabase();
        db.setId("SD1");
        db.setLocation("sgd_orfs_plus_ups_prots.fasta");
        searchDBs.add(db);
        Param dbName = new Param();
        db.setDatabaseName(dbName);
        UserParam dbNameParam = new UserParam();
        dbNameParam.setName("sgd_orfs_plus_ups_prots.fasta");
        dbName.setParam(dbNameParam);

        //add identificationfiles
        IdentificationFiles idFiles = new IdentificationFiles();
        List<IdentificationFile> idFileList = idFiles.getIdentificationFile();

        //read identification csv file
        final Map<String, String> psmidSeqMap = new HashMap<>();
        final Map<String, String> psmidModMap = new HashMap<>();
        final Map<String, String> psmidChrMap = new HashMap<>();
        final Map<String, String> psmidMzMap = new HashMap<>();
        psmidAssMap = new HashMap<>();
        seqPsmidMap = new HashMap<>();

        if (!idFn.isEmpty()) {
            IdentificationFile idFile = new IdentificationFile();
            idFile.setId("idfile_1");
            idFile.setName(idFn.substring(idFn.lastIndexOf('\\') + 1));
            idFile.setSearchDatabase(db);
            idFile.setLocation(idFn);
            idFileList.add(idFile);

            FileInputStream fis = new FileInputStream(idFn);
            BufferedReader id_br = new BufferedReader(new InputStreamReader(fis,
                                                                            "UTF-8"));
            CSVReader reader = new CSVReader(id_br);
            String nextLine[];
            reader.readNext();
            /*
             * Raw data location, Spectrum ID, Spectrum Title, Retention
             * Time(s), PSM_ID, rank, Pass Threshold, Calc m/z, Exp m/z, Charge,
             * Sequence, Modifications, Mascot:score, Mascot:expectation value,
             * proteinacc_start_stop_pre_post_;, Is decoy
             */
            while ((nextLine = reader.readNext()) != null) {
                psmidSeqMap.put(nextLine[4], nextLine[10]);
                psmidModMap.put(nextLine[4], nextLine[11]);
                psmidChrMap.put(nextLine[4], nextLine[9]);
                psmidMzMap.put(nextLine[4], nextLine[7]); //Calc m/z
                psmidAssMap.put(nextLine[4], nextLine[2].substring(0,
                                                                   nextLine[2].
                                                                   indexOf('.')));
            }
            //build sequence to psm_id(s) map

            for (Map.Entry<String, String> psmEntry : psmidSeqMap.entrySet()) {
                String psmid = psmEntry.getKey();
                String seq = psmEntry.getValue();
                if (!seq.isEmpty()) {
                    List<String> psmids = seqPsmidMap.get(seq);
                    if (psmids == null) {
                        psmids = new ArrayList<>();
                        seqPsmidMap.put(seq, psmids);
                    }
                    psmids.add(psmid);
                }
            }
        }

        //get psmid to oxidation hashmap
//        HashMap<String, ModificationType> psmidOxidationMap = new HashMap<String, ModificationType>();
//        for (String psmid : psmidModMap.keySet()) {
//            String modification = psmidModMap.get(psmid);
//            if (modification.toLowerCase().contains("oxidationfragment")) {
//                String oxidation = modification.substring(modification.toLowerCase().indexOf("ox"),
//                        modification.indexOf(":", modification.toLowerCase().indexOf("ox")) + 2);
//                ModificationType mod = new ModificationType();
//                mod.setMonoisotopicMassDelta(Double.valueOf(15.994915));
//                mod.getResidues().add("M");
//                Integer location = Integer.valueOf(oxidation.substring(oxidation.lastIndexOf(":") + 1));
//                mod.setLocation(location);
//                mod.getCvParam().add(createCvParam("Oxidation", "UNIMOD", "UNIMOD:35"));
//                psmidOxidationMap.put(psmid, mod);
//            }
//        }
        if (!idFiles.getIdentificationFile().isEmpty()) {
            inputFiles.setIdentificationFiles(idFiles);
        }
    }

    private void createSoftwareList() {

        softList = new SoftwareList();
        software = new Software();
        softList.getSoftware().add(software);
        software.setId("Progenesis");
        software.setVersion("2.3");
        software.getCvParam().add(createCvParam("Progenesis LC-MS", "PSI-MS",
                                                "MS:1001830"));
    }

    private void createDataProcessingList() {

        DataProcessingList dataProcessingList = new DataProcessingList();
        DataProcessing dataProcessing = new DataProcessing();
        dataProcessing.setId("DP1");
        dataProcessing.setSoftware(software);
        dataProcessing.setOrder(BigInteger.ONE);

        ProcessingMethod processingMethod1 = new ProcessingMethod();
        processingMethod1.setOrder(BigInteger.ONE);
        processingMethod1.getParamGroup().add(createCvParam(
                "Progenesis automatic alignment", "PSI-MS", "MS:1001865"));
        dataProcessing.getProcessingMethod().add(processingMethod1);

        ProcessingMethod processingMethod2 = new ProcessingMethod();
        processingMethod2.setOrder(BigInteger.valueOf(2));
        processingMethod2.getParamGroup().add(createCvParam(
                "Progenesis normalization", "PSI-MS", "MS:1001867"));
        dataProcessing.getProcessingMethod().add(processingMethod2);

        dataProcessingList.getDataProcessing().add(dataProcessing);
    }

    private void createAssayList() {
        assayList = new AssayList();
        assayList.setId("AssayList_1");
        final List<Assay> assays = assayList.getAssay();
        assayNameIdMap = new HashMap<>();
        int ass_i = 0;
        for (String assName : assayListFrReader) {
            Assay assay = new Assay();
            String assId = "ass_" + Integer.toString(ass_i);
            assay.setId(assId);
            assay.setName(assName);
            assayNameIdMap.put(assName, assId);

            /*
             * find the corresponding rawFilesGroup from inputFiles
             */
            RawFilesGroup rawFilesGroup = new RawFilesGroup();
            rawFilesGroup.setId("rg_" + rawFileNameIdMap.get(assName + ".raw").
                    substring(4));
            assay.setRawFilesGroup(rawFilesGroup);
            //rawFilesGroupRef.add(rawFilesGroup);    
            assay.setLabel(label);
            assays.add(assay);
            ass_i++;
        }
    }

    private void createStudyVariableList() {
        svList = new StudyVariableList();
        List<StudyVariable> studyVariabs = svList.getStudyVariable();
        for (Entry<String, Set<String>> entry : studyGroupMap.entrySet()) {
            StudyVariable studyVariable = new StudyVariable();
            String key = entry.getKey();

            studyVariable.setName(key);
            studyVariable.setId("SV_" + key);
            Set<String> values = entry.getValue();
            List<Assay> sv_assays = studyVariable.getAssays();
            for (String assayName : values) {
                Assay assay = new Assay();

                String id = assayNameIdMap.get(assayName);
                assay.setId(id);
                assay.setName(assayName);
                //assay.setLabel(label);
                sv_assays.add(assay);
            }
            List<String> sv_assayRefs = studyVariable.getAssayRefs();
            for (Assay ass : sv_assays) {
                sv_assayRefs.add(ass.getId());
            }

            //studyVariable.setCvParam(cvParam);
            studyVariable.getParamGroup().add(createCvParam(
                    "technical replicate", "PSI-MS", "MS:1001808"));

            studyVariabs.add(studyVariable);
        }
    }

    private void createProteinList(String rawPlusNorm)
            throws IOException {
        protList = new ProteinList();
        List<Protein> proteins = protList.getProtein();

        final Set<String> plProteinL = new HashSet<>(); //proteins from protein list file

        proteinAccessionsMap.forEachEntry(new TIntObjectProcedure<String>() {

            @Override
            public boolean execute(int id, String accession) {

                Set<String> accessionSet = new HashSet(Arrays.asList(accession.
                        split(";")));
                for (String acc : accessionSet) {
                    plProteinL.add(acc);
                }
                return true;
            }

        });

        Set<String> flProteinL = proteinPeptidesMap.keySet(); // proteins from feature list file

        // decide which proteins to be used as proteinList
//        if (plProteinL.containsAll(flProteinL)) {
//            //System.out.println("protein list contains feature list");
//            proteinList = flProteinL;
//        }
//        else if (flProteinL.containsAll(plProteinL)) {
//            //System.out.println("feature list contains protein list");
//            proteinList = plProteinL;
//        }
//        else {
//            //System.out.println("feature list and protein list overlap");
//            flProteinL.retainAll(plProteinL);
//            proteinList = flProteinL;
//        }
        flProteinL.retainAll(plProteinL);
        proteinList = flProteinL;

        //List<String> flProteinList = new ArrayList(flProteinL);
        //List<String> plProteinList = new ArrayList(plProteinL);
        //List<String> proteinArrayList = new ArrayList(proteinList);
//        int max = flProteinL.size();
////        if (max < plProteinL.size()) {
////            max = plProteinL.size();
////        }
//        if (max < proteinList.size()) {
//            max = proteinList.size();
//        }

        /*
         * get the protein list from proteinPeptidesMap.keySet()
         */
        proteinAccessionIdMap = new HashMap<>();
        int index = 0;
        for (String accession : proteinList) {

            Set<String> pepSequences = proteinPeptidesMap.get(accession);

            String protId = "prot_" + index;
            index++;

            Protein protein = new Protein();
            protein.setId(protId);
            protein.setAccession(accession);
            proteinAccessionIdMap.put(accession, protId);
            protein.setSearchDatabase(db);

            if (pepSequences != null) {
                final List<String> peptideConsensusRefList = protein.
                        getPeptideConsensusRefs();
                final Set<String> pepIds = new HashSet<>();
                for (final String pepSeq : pepSequences) {
                    if (!pepSeq.isEmpty()) {
                        TIntSet keys = peptideMap.get(pepSeq);

                        keys.forEach(new TIntProcedure() {

                            @Override
                            public boolean execute(int id) {
                                //only the peptide with true value of use in quantitation column to be added in protein
                                Boolean useInQuant = useInQuantMap.get(id);
                                if (useInQuant == null || useInQuant.equals(
                                        Boolean.TRUE)) {
                                    String index = flIndexMap.get(id);
                                    int chr = chrMap.get(id);
                                    String pepId = "pep_" + pepSeq + "_" + chr
                                            + "_" + index;
                                    PeptideConsensus peptideConsensus
                                            = new PeptideConsensus();
                                    peptideConsensus.setId(pepId);
                                    peptideConsensus.setPeptideSequence(pepSeq);
                                    if (!pepIds.contains(pepId)) {
                                        peptideConsensusRefList.add(pepId);
                                        pepIds.add(pepId);
                                    }
                                }
                                return true;
                            }

                        });
                    }
                }
            }
            //if the protein contain all the peptides with false use in quantitation, discard this protein
            if (!protein.getPeptideConsensusRefs().isEmpty()) {
                proteins.add(protein);
            } else {
                proteinAccessionIdMap.remove(accession); //remove protein entity which doesn't contain any peptide evidence
            }
        }

        protList.setId("ProtList1");
    }

    private void createProteinGroupList(String rawPlusNorm) {

        protGrpList = new ProteinGroupList();
        final List<ProteinGroup> proteinGrps = protGrpList.getProteinGroup();
        proteinGroupNameIdMap = new HashMap<>();

        // new proteinGroup to proteins hashmap
        final Map<String, Set<String>> protGrpProteinMap = new HashMap<>();

        proteinAccessionsMap.forEachEntry(new TIntObjectProcedure<String>() {

            int protGroupId = 0; // start counting the id of proteinGroup

            @Override
            public boolean execute(int index, String accession) {
                // split the group accession (if not, single accession will keep as one string) into list of individual accession
                Set<String> protsInGroup = new HashSet(Arrays.asList(accession.
                        split(";")));

                if (!protsInGroup.isEmpty()) {

                    // new proteinGroup element
                    ProteinGroup protGrp = new ProteinGroup();

                    protGrp.setSearchDatabase(db);
                    List<ProteinRef> protRefList = protGrp.getProteinRef();
                    for (String prot : protsInGroup) {

                        if (proteinAccessionIdMap.keySet().contains(prot)) {
                            // get protein id from proteinAccessionIdMap
                            String protId = proteinAccessionIdMap.get(prot);

                            // new Protein;
                            Protein protein = new Protein();
                            if (protId != null) {
                                protein.setAccession(prot);
                                protein.setId(protId);

                                // new ProteinRef
                                ProteinRef protRef = new ProteinRef();
                                protRef.setProtein(protein);
                                protRefList.add(protRef);
                            }
                        }
                    }
                    if (!protGrp.getProteinRef().isEmpty()) {
                        Set<String> prots = new HashSet<>();
                        for (ProteinRef ref : protGrp.getProteinRef()) {
                            prots.add(ref.getProteinRef());
                        }
                        // make sure protGrpProteinMap does not contain the accession key
                        if (protGrpProteinMap.get(accession) == null) {
                            protGrpProteinMap.put(accession, prots);
                        }

                        // set proteinGroup id attribute
                        protGrp.setId("protGrp_" + protGroupId);
                        protGroupId++;
                        // make sure proteinGroupNameIdMap does not contain the accession key
                        if (proteinGroupNameIdMap.get(accession) == null) {
                            proteinGroupNameIdMap.
                                    put(accession, protGrp.getId());
                        }

                        proteinGrps.add(protGrp);
                    }
                }
                return true;
            }

        });

        /*
         * add GlobleQuantLayer to ProteinGroupList row type is proteinGroup ref
         * column
         * types are confidence, anova, max fold change
         */
        GlobalQuantLayer protGroupGlobalQuantLayer = new GlobalQuantLayer();
        protGroupGlobalQuantLayer.setId("ProteinGroup_GQL");
        ColumnDefinition protGrpColumnIndex = new ColumnDefinition();
        protGroupGlobalQuantLayer.setColumnDefinition(protGrpColumnIndex);

        // first column index : Confidence score
        if (!confidenceMap.isEmpty()) {
            Column scoreCol = new Column();
            CvParamRef scoreCvRef = new CvParamRef();
            scoreCol.setIndex(BigInteger.ZERO);
            CvParam scoreCv = createCvParam("confidence score", "PSI-MS",
                                            "MS:1001193");
            scoreCvRef.setCvParam(scoreCv);
            scoreCol.setDataType(scoreCvRef);
            protGrpColumnIndex.getColumn().add(scoreCol);
        }

        // second column index: anova
        if (!anovaMap.isEmpty()) {
            Column anovaCol = new Column();
            CvParamRef anovaCvRef = new CvParamRef();
            if (!confidenceMap.isEmpty()) {
                anovaCol.setIndex(BigInteger.ONE);
            } else {
                anovaCol.setIndex(BigInteger.ZERO);
            }
            CvParam anovaCv = createCvParam("ANOVA p-value", "PSI-MS",
                                            "MS:1001854");
            anovaCvRef.setCvParam(anovaCv);
            anovaCol.setDataType(anovaCvRef);
            protGrpColumnIndex.getColumn().add(anovaCol);
        }

        // third column index: max fold change
        if (!mfcMap.isEmpty()) {
            Column mfcCol = new Column();
            CvParamRef mfcCvRef = new CvParamRef();
            if (!confidenceMap.isEmpty() && !anovaMap.isEmpty()) {
                mfcCol.setIndex(BigInteger.valueOf(2));
            } else if ((!confidenceMap.isEmpty() && anovaMap.isEmpty())
                    || (confidenceMap.isEmpty() && !anovaMap.isEmpty())) {
                mfcCol.setIndex(BigInteger.ONE);
            } else {
                mfcCol.setIndex(BigInteger.ZERO);
            }
            CvParam mfcCv = createCvParam("max fold change", "PSI-MS",
                                          "MS:1001853");
            mfcCvRef.setCvParam(mfcCv);
            mfcCol.setDataType(mfcCvRef);
            protGrpColumnIndex.getColumn().add(mfcCol);
        }

        final DataMatrix protGrpQLDM = new DataMatrix();

        if (!confidenceMap.isEmpty()) {

            confidenceMap.forEachEntry(new TIntDoubleProcedure() {

                @Override
                public boolean execute(int id, double value) {
                    String protGrpAccession = proteinAccessionsMap.get(id);

                    ProteinGroup protGrp = new ProteinGroup();
                    if (proteinGroupNameIdMap.get(protGrpAccession) != null) {
                        protGrp.setId(proteinGroupNameIdMap.
                                get(protGrpAccession));
                        Row row = new Row();
                        row.setObject(protGrp);
                        row.getValue().add(String.valueOf(value));
                        double anova;
                        double mfc;
                        if (!anovaMap.isEmpty()) {
                            anova = anovaMap.get(id);
                            row.getValue().add(String.valueOf(anova));
                        }

                        if (!mfcMap.isEmpty()) {
                            mfc = mfcMap.get(id);
                            String mfcStr = String.valueOf(mfc);
                            row.getValue().add(mfcStr.equals("Infinity") ? "INF"
                                    : mfcStr);
                        }
                        protGrpQLDM.getRow().add(row);
                    }
                    return true;
                }

            });
        } //end if (!confidenceMap.isEmpty())

        protGroupGlobalQuantLayer.setDataMatrix(protGrpQLDM);

        protGrpList.getGlobalQuantLayer().add(protGroupGlobalQuantLayer);

        /*
         * add two AssayQuantLayer to ProteinGroupList
         * one records normalised abundance
         * the other records raw abundance
         */
        if (!plFn.isEmpty()) {
            // AssayQuantLayer for normalised abundance
            QuantLayer assayQL_nab = new QuantLayer();
            assayQL_nab.setId("ProtGrp_AQL1");
            CvParamRef cvParamRef_nab = new CvParamRef();

            cvParamRef_nab.setCvParam(createCvParam(
                    "Progenesis:protein group normalised abundance",
                    "PSI-MS", "MS:1002518"));
            assayQL_nab.setDataType(cvParamRef_nab);

            for (String assName : assayListFrReader) {
                String assId = assayNameIdMap.get(assName);
                Assay assay = new Assay();
                assay.setId(assId);
                assay.setLabel(label);
                assayQL_nab.getColumnIndex().add(assId);
            }

            final DataMatrix nabDM = new DataMatrix();

            normAbMap.forEachEntry(new TIntObjectProcedure<TDoubleList>() {

                @Override
                public boolean execute(int id, TDoubleList normAbValues) {
                    String protGrpAccession = proteinAccessionsMap.get(id);
                    ProteinGroup protGrp = new ProteinGroup();
                    if (proteinGroupNameIdMap.get(protGrpAccession) != null) {
                        protGrp.setId(proteinGroupNameIdMap.
                                get(protGrpAccession));
                        final Row row = new Row();
                        row.setObject(protGrp);

                        normAbValues.forEach(new TDoubleProcedure() {

                            @Override
                            public boolean execute(double value) {
                                row.getValue().add(format.format(value));
                                return true;
                            }

                        });

                        nabDM.getRow().add(row);
                    }
                    return true;
                }

            });

            assayQL_nab.setDataMatrix(nabDM);

            if (rawPlusNorm == null || !rawPlusNorm.equalsIgnoreCase(
                    this.RAW_ONLY)) {
                protGrpList.getAssayQuantLayer().add(assayQL_nab);
            }

            // AssayQuantLayer for raw abundance
            QuantLayer assayQL_rab = new QuantLayer();
            assayQL_rab.setId("ProtGrp_AQL2");
            CvParamRef cvParamRef_rab = new CvParamRef();

            cvParamRef_rab.setCvParam(createCvParam(
                    "Progenesis:protein group raw abundance",
                    "PSI-MS", "MS:1002519"));
            assayQL_rab.setDataType(cvParamRef_rab);

            for (String assName : assayListFrReader) {
                String assId = assayNameIdMap.get(assName);
                Assay assay = new Assay();
                assay.setId(assId);
                assay.setLabel(label);
                assayQL_rab.getColumnIndex().add(assId);
            }

            final DataMatrix rabDM = new DataMatrix();

            rawAbMap.forEachEntry(new TIntObjectProcedure<TDoubleList>() {

                @Override
                public boolean execute(int id, TDoubleList rawAbValues) {
                    String protGrpAccession = proteinAccessionsMap.get(id);

                    ProteinGroup protGrp = new ProteinGroup();
                    if (proteinGroupNameIdMap.get(protGrpAccession) != null) {
                        protGrp.setId(proteinGroupNameIdMap.
                                get(protGrpAccession));
                        final Row row = new Row();
                        row.setObject(protGrp);

                        rawAbValues.forEach(new TDoubleProcedure() {

                            @Override
                            public boolean execute(double value) {
                                row.getValue().add(format.format(value));
                                return true;
                            }

                        });

                        rabDM.getRow().add(row);
                    }
                    return true;
                }

            });

            assayQL_rab.setDataMatrix(rabDM);

            if (rawPlusNorm == null || !rawPlusNorm.equalsIgnoreCase(
                    this.NORM_ONLY)) {
                protGrpList.getAssayQuantLayer().add(assayQL_rab);
            }
        } // end of AssayQuantLayer to ProteinList

        protGrpList.setId("ProteinGroupList1");
    }

    private void createFeatureListList(String rawPlusNorm) {

        ftListList = new ArrayList<>();

        final Map<String, FeatureList> rgIdFeatureListMap = new HashMap<>();
        peptideFeaturesMap = new HashMap<>();
        final Map<String, List<Assay>> peptideAssaysMap = new HashMap<>();
        final Map<String, Feature> featureMap = new HashMap<>();
        featureAssNameMap = new HashMap<>();

        final MutableInt keyId = new MutableInt(0);
        if (!nabMap.isEmpty()) {
            nabMap.forEachEntry(new TIntObjectProcedure<TDoubleList>() {

                @Override
                public boolean execute(final int id, TDoubleList nabValues) {
                    final MutableInt pos = new MutableInt(0);
                    //final int index = id;
                    nabValues.forEach(new TDoubleProcedure() {

                        @Override
                        public boolean execute(double value) {
                            Feature feature = new Feature();
                            //Double normAb = nabV;
                            double mz = mzMap.get(id);
                            int chr = chrMap.get(id);
                            //Double rawAb = rabMap.get(key).get(pos);
                            double reten;

                            // If the file doen't contain 'Sample retention time (min)' column, use the master retention time instead 'Retention time (min)'.
                            if (!retenMap.isEmpty()) {
                                reten = retenMap.get(id).get(pos.intValue());
                            } else {
                                reten = masterRtMap.get(id);
                            }
                            double rtWin = rtWinMap.get(id);

                            MassTrace mt = new MassTrace(mz, chr, reten, rtWin,
                                                         format);

                            feature.setMz(Double.valueOf(format.format(mz)));
                            feature.setRt(format.format(reten));
                            feature.setCharge(String.valueOf(chr));
                            String ft_id = "ft_" + keyId.toString();
                            feature.setId(ft_id);

                            //add mass trace
                            feature.getMassTrace().addAll(mt.
                                    getMassTraceDoubleList());

                            featureMap.put(ft_id, feature);

                            // create peptide id to feture HashMap: peptideFeaturesMap
                            String pepSeq = peptideDupMap.get(id);

                            String indexStr = flIndexMap.get(id);
                            String pep_id = "pep_" + pepSeq + "_" + chr + "_"
                                    + indexStr;
                            if (pepSeq != null) {

                                List<Feature> fList = peptideFeaturesMap.get(
                                        pep_id);
                                if (fList == null) {
                                    fList = new ArrayList<>();
                                    peptideFeaturesMap.put(pep_id, fList);
                                }
                                fList.add(feature);
                            }

                            String assName = assayList.getAssay().get(pos.
                                    intValue()).getName();
                            String rgId = "rg_" + rawFileNameIdMap.get(assName
                                    + ".raw").substring(4);

                            // create peptide sequence to assay id HashMap: peptideAssayMap
                            String assayId = assayNameIdMap.get(assName);
                            if (pepSeq != null) {
                                List<Assay> aList = peptideAssaysMap.get(pepSeq);
                                if (aList == null) {
                                    aList = new ArrayList<>();
                                    peptideAssaysMap.put(pepSeq, aList);
                                }

                                List<String> assayIds = getAssayIdList(aList);
                                if (!assayIds.contains(assayId)) {
                                    Assay tempAssay = new Assay();
                                    tempAssay.setId(assayId);
                                    aList.add(tempAssay);
                                }
                            }

                            FeatureList features = rgIdFeatureListMap.get(rgId);
                            if (features == null) {
                                features = new FeatureList();
                                RawFilesGroup rawFilesGroup
                                        = new RawFilesGroup();
                                rawFilesGroup.setId(rgId);
                                features.setRawFilesGroup(rawFilesGroup);
                                String fListId = "Flist" + rgId.substring(3);
                                features.setId(fListId);
                                rgIdFeatureListMap.put(rgId, features);
                                features.getParamGroup().add(createCvParam(
                                        "mass trace reporting: rectangles",
                                        "PSI-MS", "MS:1001826"));
                                ftListList.add(features);
                            } else {
                            }
                            features.getFeature().add(feature);
                            featureAssNameMap.put(feature.getId(), assName);
                            pos.add(1);
                            keyId.add(1);
                            return true;
                        }

                    });
                    return true;
                }

            });
        } else if (!rabMap.isEmpty()) {
            rabMap.forEachEntry(new TIntObjectProcedure<TDoubleList>() {

                @Override
                public boolean execute(final int id, TDoubleList rabValues) {
                    final MutableInt pos = new MutableInt(0);

                    rabValues.forEach(new TDoubleProcedure() {

                        @Override
                        public boolean execute(double value) {
                            Feature feature = new Feature();
                            //Double rawAb = rabV;
                            double mz = mzMap.get(id);
                            int chr = chrMap.get(id);
                            //double normAb = nabMap.get(id).get(pos.intValue());
                            double reten;

                            // If the file doen't contain 'Sample retention time (min)' column, use the master retention time instead 'Retention time (min)'.
                            if (!retenMap.isEmpty()) {
                                reten = retenMap.get(id).get(pos.intValue());
                            } else {
                                reten = masterRtMap.get(id);
                            }
                            Double rtWin = rtWinMap.get(id);

                            MassTrace mt = new MassTrace(mz, chr, reten, rtWin,
                                                         format);

                            feature.setMz(Double.valueOf(format.format(mz)));
                            feature.setRt(format.format(reten));
                            feature.setCharge(String.valueOf(chr));
                            String ft_id = "ft_" + keyId.toString();
                            feature.setId(ft_id);

                            //add mass trace
                            feature.getMassTrace().addAll(mt.
                                    getMassTraceDoubleList());

                            featureMap.put(ft_id, feature);

                            // create peptide id to feture HashMap: peptideFeaturesMap
                            String pepSeq = peptideDupMap.get(id);

                            String indexStr = flIndexMap.get(id);
                            String pep_id = "pep_" + pepSeq + "_" + chr + "_"
                                    + indexStr;

                            if (pepSeq != null) {

                                List<Feature> fList = peptideFeaturesMap.get(
                                        pep_id);
                                if (fList == null) {
                                    fList = new ArrayList<>();
                                    peptideFeaturesMap.put(pep_id, fList);
                                }
                                fList.add(feature);
                            }

                            String assName = assayList.getAssay().get(pos.
                                    intValue()).getName();
                            String rgId = "rg_" + rawFileNameIdMap.get(assName
                                    + ".raw").substring(4);

                            // create peptide sequence to assay id HashMap: peptideAssayMap
                            String assayId = assayNameIdMap.get(assName);
                            if (pepSeq != null) {
                                List<Assay> aList = peptideAssaysMap.get(pepSeq);
                                if (aList == null) {
                                    aList = new ArrayList<>();
                                    peptideAssaysMap.put(pepSeq, aList);
                                }

                                List<String> assayIds = getAssayIdList(aList);
                                if (!assayIds.contains(assayId)) {
                                    Assay tempAssay = new Assay();
                                    tempAssay.setId(assayId);
                                    aList.add(tempAssay);
                                }
                            }

                            FeatureList features = rgIdFeatureListMap.get(rgId);
                            if (features == null) {
                                features = new FeatureList();
                                RawFilesGroup rawFilesGroup
                                        = new RawFilesGroup();
                                rawFilesGroup.setId(rgId);
                                features.setRawFilesGroup(rawFilesGroup);
                                String fListId = "Flist" + rgId.substring(3);
                                features.setId(fListId);
                                rgIdFeatureListMap.put(rgId, features);
                                features.getParamGroup().add(createCvParam(
                                        "mass trace reporting: rectangles",
                                        "PSI-MS", "MS:1001826"));
                                ftListList.add(features);
                            } else {
                            }
                            features.getFeature().add(feature);
                            featureAssNameMap.put(feature.getId(), assName);
                            pos.add(1);
                            keyId.add(1);

                            return true;
                        }

                    });
                    return true;
                }

            });

        } else if (!retenMap.isEmpty()) {
            retenMap.forEachEntry(new TIntObjectProcedure<TDoubleList>() {

                @Override
                public boolean execute(final int id, TDoubleList retValues) {
                    final MutableInt pos = new MutableInt(0);
                    retValues.forEach(new TDoubleProcedure() {

                        @Override
                        public boolean execute(double value) {
                            Feature feature = new Feature();

                            double mz = mzMap.get(id);
                            int chr = chrMap.get(id);
                            double rtWin = rtWinMap.get(id);

                            MassTrace mt = new MassTrace(mz, chr, value, rtWin,
                                                         format);

                            feature.setMz(Double.valueOf(format.format(mz)));
                            feature.setRt(format.format(value));
                            feature.setCharge(String.valueOf(chr));
                            String ft_id = "ft_" + keyId.toString();
                            feature.setId(ft_id);

                            //add mass trace
                            feature.getMassTrace().addAll(mt.
                                    getMassTraceDoubleList());

                            featureMap.put(ft_id, feature);

                            // create peptide id to feture HashMap: peptideFeaturesMap
                            String pepSeq = peptideDupMap.get(id);
                            String indexStr = flIndexMap.get(id);
                            String pep_id = "pep_" + pepSeq + "_" + chr + "_"
                                    + indexStr;
                            if (pepSeq != null) {

                                List<Feature> fList = peptideFeaturesMap.get(
                                        pep_id);
                                if (fList == null) {
                                    fList = new ArrayList<>();
                                    peptideFeaturesMap.put(pep_id, fList);
                                }
                                fList.add(feature);
                            }

                            String assName = assayList.getAssay().get(pos.
                                    intValue()).getName();
                            String rgId = "rg_" + rawFileNameIdMap.get(assName
                                    + ".raw").substring(4);

                            // create peptide sequence to assay id HashMap: peptideAssayMap
                            String assayId = assayNameIdMap.get(assName);
                            if (pepSeq != null) {
                                List<Assay> aList = peptideAssaysMap.get(pepSeq);
                                if (aList == null) {
                                    aList = new ArrayList<>();
                                    peptideAssaysMap.put(pepSeq, aList);
                                }

                                List<String> assayIds = getAssayIdList(aList);
                                if (!assayIds.contains(assayId)) {
                                    Assay tempAssay = new Assay();
                                    tempAssay.setId(assayId);
                                    aList.add(tempAssay);
                                }
                            }

                            FeatureList features = rgIdFeatureListMap.get(rgId);
                            if (features == null) {
                                features = new FeatureList();
                                RawFilesGroup rawFilesGroup
                                        = new RawFilesGroup();
                                rawFilesGroup.setId(rgId);
                                features.setRawFilesGroup(rawFilesGroup);
                                String fListId = "Flist" + rgId.substring(3);
                                features.setId(fListId);
                                rgIdFeatureListMap.put(rgId, features);
                                features.getParamGroup().add(createCvParam(
                                        "mass trace reporting: rectangles",
                                        "PSI-MS", "MS:1001826"));
                                ftListList.add(features);

                                // create feature QuantLayer
//                                List<GlobalQuantLayerType> featureQuantLayerList = features.getFeatureQuantLayer();
//                                GlobalQuantLayerType featureQuantLayer = new GlobalQuantLayerType();
//                                featureQuantLayerList.add(featureQuantLayer);
//                                featureQuantLayer.setId("FQL" + Integer.toString(keyId));
//                                ColumnDefinitionType featureColumnIndex = new ColumnDefinitionType();
//                                featureQuantLayer.setColumnDefinition(featureColumnIndex);
//
//                                // cvParam for raw abundance
//                                ColumnType featureColumn_raw
//                                        = createColumn(0, createCvParam("raw abundance", "PSI-MS", "MS:000000"));
//                                featureColumnIndex.getColumn().add(featureColumn_raw);
//
//                                // cvParam for normalised abundance
//                                ColumnType featureColumn_norm
//                                        = createColumn(1, createCvParam("normalized abundance", "PSI-MS", "MS:000000"));
//                                featureColumnIndex.getColumn().add(featureColumn_norm);
//
//                                RowType row = new RowType();
//                                row.setObjectRef(feature);
//                                row.getValue().add(rawAb);
//                                row.getValue().add(normAb);
//                                DataMatrixType dataMatrix = new DataMatrixType();
//                                dataMatrix.getRow().add(row);
//                                featureQuantLayer.setDataMatrix(dataMatrix);
                            } else {
//                                RowType row = new RowType();
//                                row.setObjectRef(feature);
//                                row.getValue().add(rawAb);
//                                row.getValue().add(normAb);
//                                features.getFeatureQuantLayer().get(0).getDataMatrix().getRow().add(row);
                            }
                            features.getFeature().add(feature);
                            featureAssNameMap.put(feature.getId(), assName);
                            pos.add(1);
                            keyId.add(1);
                            return true;
                        }

                    });
                    return true;
                }

            });
        } else {
            throw new IllegalStateException(
                    "There is no normalized abundance, raw abundance, and sample retention time measurement in the feature list file!");
        }
    }

    private void createPeptideListList(String rawPlusNorm) {
        pepConListList = new ArrayList<>();
        PeptideConsensusList peptideConsensusList = new PeptideConsensusList();
        List<PeptideConsensus> peptideConsensuses = peptideConsensusList.
                getPeptideConsensus();
        final DataMatrix pep_NabDM = new DataMatrix();
        final DataMatrix pep_RabDM = new DataMatrix();
        final DataMatrix pep_scoreDM = new DataMatrix();

        for (final String seq : peptideMap.keySet()) {
            if (!seq.isEmpty()) {
                TIntSet keys = peptideMap.get(seq);

                final Set<String> tempPepList = new HashSet<>();
                keys.forEach(new TIntProcedure() {

                    @Override
                    public boolean execute(int key) {
                        if (useInQuantMap.get(key) == null || useInQuantMap.get(
                                key).equals(Boolean.TRUE)) {
                            PeptideConsensus peptideConsensus
                                    = new PeptideConsensus();

                            String indexStr = flIndexMap.get(key);
                            Integer chr = chrMap.get(key);
                            peptideConsensus.getCharge().add(chr.toString());

                            String pep_id = "pep_" + seq + "_" + chr + "_"
                                    + indexStr;

                            if (!tempPepList.contains(pep_id)) {
                                tempPepList.add(pep_id);
                            }

                            peptideConsensus.setId(pep_id);
                            peptideConsensus.setPeptideSequence(seq);
                            peptideConsensus.setSearchDatabase(db);

                            // add EvidenceRef to individual peptideConsensus
                            List<Feature> fList = peptideFeaturesMap.get(pep_id);
                            for (Feature f : fList) {
                                EvidenceRef evRef = new EvidenceRef();
                                evRef.setFeature(f);
                                peptideConsensus.getEvidenceRef().add(evRef);

                                //add assay_refs
                                String assN = featureAssNameMap.get(f.getId());
                                String ass_id = assayNameIdMap.get(assN);

                                Assay tempAssay = new Assay();
                                tempAssay.setId(ass_id);
                                tempAssay.setName(assN);
                                evRef.getAssayRefs().add(ass_id);

                                //add pms id refs
                                List<String> id_refs = seqPsmidMap.get(seq);

                                if (id_refs != null) {
                                    for (String psmid : id_refs) {
                                        String assayN = psmidAssMap.get(psmid);
                                        if (assayN.equals(assN)) {
                                            evRef.getIdRefs().add(psmid);
                                            //add identificationFile_ref
                                            evRef.setIdentificationFile(
                                                    inputFiles.
                                                    getIdentificationFiles().
                                                    getIdentificationFile().get(
                                                            0));
                                        }
                                    }
                                }
                            }

                            Integer modKey = key;
                            String modification = modificationMap.get(modKey);
                            if (modification != null) {
                                String[] modifications = modification.replace(
                                        '|', ';').split(";");
                                for (String mod : modifications) {
                                    if (mod.toLowerCase(Locale.ENGLISH).
                                            contains("oxidation")) {
                                        Integer location = Integer.valueOf(mod.
                                                substring(mod.indexOf('[') + 1,
                                                          mod.indexOf(']')));
                                        Modification modObj = new Modification();
                                        modObj.setMonoisotopicMassDelta(
                                                15.994915);
                                        modObj.setLocation(location);
                                        modObj.getCvParam().add(createCvParam(
                                                "Oxidation", "UNIMOD",
                                                "UNIMOD:35"));
                                        peptideConsensus.getModification().add(
                                                modObj);
                                    }
                                }
                            }

                            // create DataMatrixType of AssayQuantLayer for both normalized and raw abundance
                            // normalized abundance
                            if (!nabMap.isEmpty()) {
                                Integer nabKey = key;
                                final Row nabrow = new Row();
                                nabrow.setObject(peptideConsensus);
                                TDoubleList nabValues = nabMap.get(nabKey);
                                nabValues.forEach(new TDoubleProcedure() {

                                    @Override
                                    public boolean execute(double value) {
                                        nabrow.getValue().add(format.format(
                                                value));
                                        return true;
                                    }

                                });
                                pep_NabDM.getRow().add(nabrow);
                            }

                            // raw abundance
                            if (!rabMap.isEmpty()) {
                                Integer rabKey = key;
                                final Row rabrow = new Row();
                                rabrow.setObject(peptideConsensus);
                                TDoubleList rabValues = rabMap.get(rabKey);
                                rabValues.forEach(new TDoubleProcedure() {

                                    @Override
                                    public boolean execute(double value) {
                                        rabrow.getValue().add(format.format(
                                                value));
                                        return true;
                                    }

                                });

                                pep_RabDM.getRow().add(rabrow);
                            }

                            // create datamatrix for global quant layer Mascot ion score
                            if (!scoreMap.isEmpty()) {
                                Integer scoreKey = key;
                                if (scoreMap.get(scoreKey) != scoreMap.
                                        getNoEntryValue()) {
                                    Row row = new Row();
                                    row.setObject(peptideConsensus);
                                    row.getValue().add(String.valueOf(scoreMap.
                                            get(scoreKey)));
                                    pep_scoreDM.getRow().add(row);
                                }
                            }

                            peptideConsensuses.add(peptideConsensus);
                        }
                        return true;
                    }

                });

            }
        }

        /*
         * add mascot ion score global quant layer
         */
        if (!scoreMap.isEmpty()) {
            GlobalQuantLayer pepGQL_score = new GlobalQuantLayer();

            pepGQL_score.setId("Pep_GQL1");
            ColumnDefinition columnDef = new ColumnDefinition();

            pepGQL_score.setColumnDefinition(columnDef);

            Column column = new Column();

            column.setIndex(BigInteger.ZERO);

            columnDef.getColumn()
                    .add(column);
            CvParamRef col_dt = new CvParamRef();

            column.setDataType(col_dt);

            col_dt.setCvParam(createCvParam("Mascot:score", "PSI-MS",
                                            "MS:1001171"));
            pepGQL_score.setDataMatrix(pep_scoreDM);

            peptideConsensusList.getGlobalQuantLayer()
                    .add(pepGQL_score);
        }
        /*
         * add peptide AssayQuantLayer for normalized abundance, raw abundance,
         * etc.
         */

        // normalized abundance AssayQuantLayer  pepAQL_nab
        if (!nabMap.isEmpty() && (rawPlusNorm == null || !rawPlusNorm.
                equalsIgnoreCase(this.RAW_ONLY))) {
            QuantLayer pepAQL_nab = new QuantLayer();

            pepAQL_nab.setId("Pep_AQL1");
            CvParamRef cvParamRef_pep_aql_nab = new CvParamRef();

            cvParamRef_pep_aql_nab.setCvParam(createCvParam(
                    "Progenesis:peptide normalised abundance",
                    "PSI-MS", "MS:1001891"));
            pepAQL_nab.setDataType(cvParamRef_pep_aql_nab);

            for (String assName : assayListFrReader) {
                String assId = assayNameIdMap.get(assName);
                Assay assay = new Assay();
                assay.setId(assId);
                assay.setLabel(label);
                pepAQL_nab.getColumnIndex().add(assId);
            }

            pepAQL_nab.setDataMatrix(pep_NabDM);

            peptideConsensusList.getAssayQuantLayer().add(pepAQL_nab);
        }

        // raw abundance AssayQuantLayer pepAQL_rab
        if (!rabMap.isEmpty() && (rawPlusNorm == null || !rawPlusNorm.
                equalsIgnoreCase(this.NORM_ONLY))) {
            QuantLayer pepAQL_rab = new QuantLayer();

            pepAQL_rab.setId("Pep_AQL2");
            CvParamRef cvParamRef_pep_aql_rab = new CvParamRef();

            cvParamRef_pep_aql_rab.setCvParam(createCvParam(
                    "Progenesis:peptide raw abundance",
                    "PSI-MS", "MS:1001893"));
            pepAQL_rab.setDataType(cvParamRef_pep_aql_rab);

            for (String assName : assayListFrReader) {
                String assId = assayNameIdMap.get(assName);
                Assay assay = new Assay();
                assay.setId(assId);
                assay.setLabel(label);
                pepAQL_rab.getColumnIndex().add(assId);
            }

            pepAQL_rab.setDataMatrix(pep_RabDM);

            peptideConsensusList.getAssayQuantLayer().add(pepAQL_rab);
        }

        peptideConsensusList.setId("PepList1");
        peptideConsensusList.setFinalResult(true);

        pepConListList.add(peptideConsensusList);
    }

    /**
     * Create output mzQuantML file with output parameters.
     * The protGrpList flag decides if output file contains ProteinGroupList or
     * not.
     * The rawPlusNorm flag decides which type of abundance QuantLayer is in the
     * output file.
     * If rawPlusNorm equals NORM_ONLY or RAW_ONLY, only normalised abundance or
     * raw abundance values are output.
     * Otherwise (including rawPlusNorm is null), any available abundance values
     * are output.
     *
     * @param outFn       output file name
     * @param protGrpList flag indicates if output ProteinGroupList or not
     * @param rawPlusNorm flag indicates which type of abundance is in the
     *                    output file.
     *
     * @throws IOException
     * @throws DatatypeConfigurationException
     */
    public void convert(String outFn, boolean protGrpList, String rawPlusNorm)
            throws IOException, DatatypeConfigurationException {

        assayListFrReader = new ArrayList<>();
        nabMap = new TIntObjectHashMap<>();
        rabMap = new TIntObjectHashMap<>();
        retenMap = new TIntObjectHashMap<>();
        masterRtMap = new TIntDoubleHashMap();
        mzMap = new TIntDoubleHashMap();
        chrMap = new TIntIntHashMap();
        studyGroupMap = new HashMap<>();
        proteinPeptidesMap = new HashMap<>();
        peptideMap = new HashMap<>();
        flIndexMap = new TIntObjectHashMap<>();
        peptideDupMap = new TIntObjectHashMap<>();
        useInQuantMap = new TIntObjectHashMap<>();
        confidenceMap = new TIntDoubleHashMap();
        anovaMap = new TIntDoubleHashMap();
        mfcMap = new TIntDoubleHashMap();
        normAbMap = new TIntObjectHashMap<>();
        rawAbMap = new TIntObjectHashMap<>();
        rtWinMap = new TIntDoubleHashMap();
        scoreMap = new TIntDoubleHashMap();
        modificationMap = new TIntObjectHashMap<>();

        //proteinAccessionsMap = new TIntObjectHashMap<>();
        if (!flFn.isEmpty()) {
            /**
             * parsing feature list file
             */
            FileInputStream fis = new FileInputStream(flFn);
            try (ProgenesisFeatureListReader pflr
                    = new ProgenesisFeatureListReader(new InputStreamReader(fis,
                                                                            "UTF-8"),
                                                      separator)) {
                nabMap.putAll(pflr.getNormalizedAbundanceMap());
                rabMap.putAll(pflr.getRawAbundanceMap());
                retenMap.putAll(pflr.getRetentionTimeMap());
                mzMap.putAll(pflr.getMassOverChargeMap());
                chrMap.putAll(pflr.getChargeMap());
                masterRtMap.putAll(pflr.getMasterRTDuplicateMap());
                rtWinMap.putAll(pflr.getRtWindowMap());
                scoreMap.putAll(pflr.getScoreMap());
                modificationMap.putAll(pflr.getModificationMap());
                studyGroupMap = pflr.getStudyGroupMap();
                proteinPeptidesMap = pflr.getProteinPeptidesMap();
                peptideMap = pflr.getPeptideMap();
                peptideDupMap.putAll(pflr.getPeptideDuplicateMap());
                useInQuantMap.putAll(pflr.getUseInQuantMap());
                assayListFrReader = pflr.getAssayList();
                flIndexMap.putAll(pflr.getIndexMap());
            }
        }
        if (!plFn.isEmpty()) {
            /**
             * parsing protein list file
             */
            try (ProgenesisProteinListReader pplr
                    = new ProgenesisProteinListReader(new FileReader(plFn),
                                                      separator)) {
                proteinAccessionsMap.putAll(pplr.getInexMap());
                confidenceMap = pplr.getConfidenceMap();
                anovaMap.putAll(pplr.getAnovaMap());
                mfcMap.putAll(pplr.getMaxFoldChangeMap());
                normAbMap = pplr.getNormalizedAbundanceMap();
                rawAbMap = pplr.getRawAbundanceMap();
                if (flFn.isEmpty()) {
                    assayListFrReader = pplr.getAssayList();
                    studyGroupMap = pplr.getStudyGroupMap();
                }
            }
        }

        createCvList();

        createAnalysisSummary();

        createBibliographicReferenceList();

        createInputFiles();

        createAssayList();

        createStudyVariableList();

        createProteinList(rawPlusNorm);

        createProteinGroupList(rawPlusNorm);

        createFeatureListList(rawPlusNorm);

        createPeptideListList(rawPlusNorm);

        createSoftwareList();

        createDataProcessingList();

        writeMzqFile(outFn, protGrpList);
    }

    private void writeMzqFile(String out, boolean pgl) {
        OutputStreamWriter writer = null;

        try {
            MzQuantMLMarshaller mzqMsh = new MzQuantMLMarshaller();
            FileOutputStream fos = new FileOutputStream(out);
            writer = new OutputStreamWriter(fos, "UTF-8");

            // XML header
            writer.write(MzQuantMLMarshaller.createXmlHeader() + "\n");

            // mzQuantML start tag
            Calendar rightnow = Calendar.getInstance();
            int day = rightnow.get(Calendar.DATE);
            int month = rightnow.get(Calendar.MONTH) + 1;
            int year = rightnow.get(Calendar.YEAR);

            /*
             * set mzQuantML id
             */
            String mzqId = "Progenesis-Label-Free-" + String.valueOf(day)
                    + String.valueOf(month) + String.valueOf(year);

            writer.write(MzQuantMLMarshaller.createMzQuantMLStartTag(mzqId)
                    + "\n");

            if (cvList != null) {
                mzqMsh.marshall(cvList, writer);
                writer.write("\n");
            }
//            if (ac != null) {
//                mzqMsh.marshall(ac, writer);
//                writer.write("\n");
//            }
            if (as != null) {
                mzqMsh.marshall(as, writer);
                writer.write("\n");
            }
            if (inputFiles != null) {
                mzqMsh.marshall(inputFiles, writer);
                writer.write("\n");
            }
            if (softList != null) {
                mzqMsh.marshall(softList, writer);
                writer.write("\n");
            }
//            if (dpList != null) {
//                mzqMsh.marshall(dpList, writer);
//                writer.write("\n");
//            }
            if (brList != null) {
                for (BibliographicReference bibRef : brList) {
                    mzqMsh.marshall(bibRef, writer);
                    writer.write("\n");
                }
            }
            if (assayList != null) {
                mzqMsh.marshall(assayList, writer);
                writer.write("\n");
            }
            if (svList != null) {
                mzqMsh.marshall(svList, writer);
                writer.write("\n");
            }

            if (protGrpList != null && pgl) {
                mzqMsh.marshall(protGrpList, writer);
                writer.write("\n");
            }
            if (protList != null) {
                mzqMsh.marshall(protList, writer);
                writer.write("\n");
            }
            if (pepConListList != null) {
                for (PeptideConsensusList pepConList : pepConListList) {
                    mzqMsh.marshall(pepConList, writer);
                    writer.write("\n");
                }
            }
            if (ftListList != null) {
                for (FeatureList ftList : ftListList) {
                    mzqMsh.marshall(ftList, writer);
                    writer.write("\n");
                }
            }
//            if (smallMolList != null) {
//                mzqMsh.marshall(smallMolList, writer);
//                writer.write("\n");
//            }

            writer.write(MzQuantMLMarshaller.createMzQuantMLClosingTag());
        } catch (IOException ex) {
            Logger.getLogger(MaxquantMzquantmlConverter.class.getName()).log(
                    Level.SEVERE, null, ex);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MaxquantMzquantmlConverter.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
    }

    // create a CVParamType instance
    private static CvParam createCvParam(String name, String cvRef,
                                         String accession) {
        CvParam cp = new CvParam();
        cp.setName(name);
        Cv cv = new Cv();
        cv.setId(cvRef);
        cp.setCv(cv);
        cp.setAccession(accession);
        return cp;
    }

    private static List<String> getAssayIdList(List<Assay> assays) {
        List<String> assayIds = new ArrayList<>();
        for (Assay assay : assays) {
            assayIds.add(assay.getId());
        }
        return assayIds;
    }

}
