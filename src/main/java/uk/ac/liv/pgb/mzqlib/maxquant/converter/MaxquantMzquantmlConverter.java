
package uk.ac.liv.pgb.mzqlib.maxquant.converter;

import static uk.ac.liv.pgb.mzqlib.utils.Utils.addSearchDBToInputFiles;
import static uk.ac.liv.pgb.mzqlib.utils.Utils.setSearchDB;

import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.map.TIntObjectMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.math.NumberUtils;

import uk.ac.liv.pgb.jmzqml.model.mzqml.AnalysisSummary;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Assay;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AssayList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AuditCollection;
import uk.ac.liv.pgb.jmzqml.model.mzqml.BibliographicReference;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Column;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ColumnDefinition;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Cv;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParamRef;
import uk.ac.liv.pgb.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.pgb.jmzqml.model.mzqml.DataProcessing;
import uk.ac.liv.pgb.jmzqml.model.mzqml.DataProcessingList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.EvidenceRef;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Feature;
import uk.ac.liv.pgb.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.GlobalQuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.InputFiles;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Label;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ModParam;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ParamList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProcessingMethod;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Protein;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroupList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Ratio;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RatioList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RatioQuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RawFile;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RawFilesGroup;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Row;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SearchDatabase;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SmallMoleculeList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Software;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SoftwareList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.StudyVariable;
import uk.ac.liv.pgb.jmzqml.model.mzqml.StudyVariableList;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLMarshaller;

/**
 * Convert MaxQuant result files into mzQuantML format.
 *
 * @author Da Qi
 */
public class MaxquantMzquantmlConverter {

    /**
     * @param args the command line arguments
     */
    private static final String MAXQUANT_VERSION = "1.2.0.18"; // NOPMD
    private final MaxquantFilesReader maxRd;

    private static final String SEARCH_DATABASE_LOCATION
            = "sgd_orfs_plus_ups_prots.fasta";
    private static final String SEARCH_DATABASE_NAME
            = "sgd_orfs_plus_ups_prots.fasta";

    // MzQuantML elements
    private CvList cvList = null;
    private AuditCollection ac = null;
    private AnalysisSummary as = null;
    private InputFiles inputFiles = null;
    private SoftwareList softList = null;
    private DataProcessingList dpList = null;
    private List<BibliographicReference> brList = null;
    private AssayList assayList = null;
    private StudyVariableList svList = null;
    private RatioList ratioList = null;
    private ProteinGroupList protGrpList = null;
    private ProteinList protList = null;
    private List<PeptideConsensusList> pepConListList = null;
    private List<FeatureList> ftListList = null;
    private SmallMoleculeList smallMolList = null;
    private Cv cv;
    private Cv cvUnimod;
    private Cv cvMod;
    private Label label; // for unlabel sample

    // internal variables
    private Map<String, String> rawFileNameIdMap;
    private SearchDatabase db;
    private Software software;
    private Map<String, String> assayNameIdMap;
    private Map<String, List<Feature>> peptideFeaturesMap;
    private Map<String, String> featureAssNameMap;
    private TIntObjectMap<List<String>> evidenceMap;

    // Constructor
    /**
     * Constructor taking file folder as parameter.
     * The required MaxQuant result files must be in the folder.
     *
     * @param inputFolder path of the input folder
     *
     * @throws IOException io exception
     */
    public MaxquantMzquantmlConverter(final String inputFolder)
            throws IOException {
        this.maxRd = new MaxquantFilesReader(inputFolder);
    }

    /**
     * Constructor taking required individual file names as parameters.
     *
     * @param evidenceFn                   evidence.txt
     * @param peptidesFn                   peptides.txt
     * @param proteinGroupsFn              proteingroup.txt
     * @param experimentalDesignTemplateFn experimentalDesignTemplate.txt
     * @param summaryFn                    summary.txt
     *
     * @throws IOException io exception
     */
    public MaxquantMzquantmlConverter(final String evidenceFn,
                                      final String peptidesFn,
                                      final String proteinGroupsFn,
                                      final String experimentalDesignTemplateFn,
                                      final String summaryFn)
            throws IOException {

        File evidenceFile;
        File peptidesFile;
        File proteinGroupsFile;
        File experimentalDesignTemplateFile;
        File summaryFile;
        // Initialise evidenceFile
        if (!evidenceFn.isEmpty()) {
            evidenceFile = new File(evidenceFn);
        } else {
            evidenceFile = null;
        }

        // Initialise peptidesFile
        if (!peptidesFn.isEmpty()) {
            peptidesFile = new File(peptidesFn);
        } else {
            peptidesFile = null;
        }

        // Initialise proteinGroupsFile
        if (!proteinGroupsFn.isEmpty()) {
            proteinGroupsFile = new File(proteinGroupsFn);
        } else {
            proteinGroupsFile = null;
        }

        // Initialise experimentalDesignTemplateFile
        if (!experimentalDesignTemplateFn.isEmpty()) {
            experimentalDesignTemplateFile = new File(
                    experimentalDesignTemplateFn);
        } else {
            experimentalDesignTemplateFile = null;
        }

        // Initialise parametersFile
        if (!summaryFn.isEmpty()) {
            summaryFile = new File(summaryFn);
        } else {
            summaryFile = null;
        }
        this.maxRd = new MaxquantFilesReader(evidenceFile,
                                             peptidesFile,
                                             proteinGroupsFile,
                                             experimentalDesignTemplateFile,
                                             summaryFile);

    }

    private void createCvList() {
        cvList = new CvList();
        List<Cv> cvs = cvList.getCv();
        // psi-ms
        cv = new Cv();
        cv.setId("PSI-MS");
        cv.setUri(
                "http://psidev.cvs.sourceforge.net/viewvc/*checkout*/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo");
        cv.setFullName(
                "Proteomics Standards Initiative Mass Spectrometry Vocabularies");
        cv.setVersion("2.25.0");
        cvs.add(cv);

        //unimod
        cvUnimod = new Cv();
        cvUnimod.setId("UNIMOD");
        cvUnimod.setUri("http://www.unimod.org/obo/unimod.obo");
        cvUnimod.setFullName("Unimod");
        cvs.add(cvUnimod);

        //psi-mod
        cvMod = new Cv();
        cvMod.setId("PSI-MOD");
        cvMod.setUri(
                "http://psidev.cvs.sourceforge.net/psidev/psi/mod/data/PSI-MOD.obo");
        cvMod.setFullName(
                "Proteomics Standards Initiative Protein Modifications Vocabularies");
        cvs.add(cvMod);

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

        if (maxRd.isLabelFree()) {

            as.getParamGroup().add(createCvParam(
                    "LC-MS label-free quantitation analysis", "PSI-MS",
                    "MS:1001834"));

            CvParam analysisSummaryCv = createCvParam(
                    "label-free raw feature quantitation", "PSI-MS",
                    "MS:1002019");
            analysisSummaryCv.setValue("true");
            as.getParamGroup().add(analysisSummaryCv);

            analysisSummaryCv = createCvParam(
                    "label-free peptide level quantitation", "PSI-MS",
                    "MS:1002020");
            analysisSummaryCv.setValue("true");
            as.getParamGroup().add(analysisSummaryCv);

            analysisSummaryCv = createCvParam(
                    "label-free protein level quantitation", "PSI-MS",
                    "MS:1002021");
            analysisSummaryCv.setValue("true");
            as.getParamGroup().add(analysisSummaryCv);

            analysisSummaryCv = createCvParam(
                    "label-free proteingroup level quantitation", "PSI-MS",
                    "MS:1002022");
            analysisSummaryCv.setValue("false");
            as.getParamGroup().add(analysisSummaryCv);
        } else {
            as.getParamGroup().add(createCvParam("MS1 label-based analysis",
                                                 "PSI-MS", "MS:1002018"));

            CvParam analysisSummaryCv = createCvParam(
                    "MS1 label-based raw feature quantitation", "PSI-MS",
                    "MS:1002001");
            analysisSummaryCv.setValue("true");
            as.getParamGroup().add(analysisSummaryCv);

            analysisSummaryCv = createCvParam(
                    "MS1 label-based peptide level quantitation", "PSI-MS",
                    "MS:1002002");
            analysisSummaryCv.setValue("true");
            as.getParamGroup().add(analysisSummaryCv);

            analysisSummaryCv = createCvParam(
                    "MS1 label-based protein level quantitation", "PSI-MS",
                    "MS:1002003");
            analysisSummaryCv.setValue("true");
            as.getParamGroup().add(analysisSummaryCv);

            analysisSummaryCv = createCvParam(
                    "MS1 label-based proteingroup level quantitation", "PSI-MS",
                    "MS:1002004");
            analysisSummaryCv.setValue("false");
            as.getParamGroup().add(analysisSummaryCv);
        }
    }

//    private void createAuditCollection() {
//        ac = new AuditCollection();
//
//        Organization uol = new Organization();
//        uol.setId("ORG_UOL");
//        uol.setName("University of Liverpool");
//
//        Person andy = new Person();
//        andy.setFirstName("Andy");
//        andy.setLastName("Jones");
//
//        Affiliation aff = new Affiliation();
//        aff.setOrganization(uol);
//        andy.getAffiliation().add(aff);
//        andy.setId("PERS_ARJ");
//        ac.getPerson().add(andy);
//
//        Person ddq = new Person();
//        ddq.setFirstName("Da");
//        ddq.setLastName("Qi");
//        ddq.setId("PERS_DQ");
//        ddq.getAffiliation().add(aff);
//        ac.getPerson().add(ddq);
//
//        // the schema require person before organization
//        ac.getOrganization().add(uol);
//    }
    private void createSoftwareList() {
        softList = new SoftwareList();
        software = new Software();
        softList.getSoftware().add(software);
        software.setId("MaxQuant");
        software.setVersion(MAXQUANT_VERSION);
        software.getCvParam().add(createCvParam("MaxQuant", "PSI-MS",
                                                "MS:1001583"));
    }

    private void createInputFiles() {
        inputFiles = new InputFiles();
        List<RawFilesGroup> rawFilesGroupList = inputFiles.getRawFilesGroup();

        Iterator iRaw = maxRd.getRawFileList().iterator();
        rawFileNameIdMap = new HashMap<>();
        Map<String, List<String>> rgIdrawIdMap = new HashMap<>();
        int rawI = 0;
        while (iRaw.hasNext()) {
            String rawFn = (String) iRaw.next();
            String rawId = "raw_" + Integer.toString(rawI);
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

            String rgId = "rg_" + Integer.toString(rawI);
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

            rawI++;
        }

        //add search databases
        addSearchDBToInputFiles(inputFiles, db);
    }

    private void createAssayList() {
        assayList = new AssayList();
        assayList.setId("AssayList_1");
        List<Assay> assays = assayList.getAssay();
        Iterator iAss = maxRd.getAssayList().iterator();
        assayNameIdMap = new HashMap<>();
        int assI = 0;
        while (iAss.hasNext()) {
            Assay assay = new Assay();
            String assName = (String) iAss.next();
            String assId = "ass_" + Integer.toString(assI);
            assay.setId(assId);
            assay.setName(assName);
            assayNameIdMap.put(assName, assId);

            //Object rawFilesGroupRef = assay.getRawFilesGroupRef();
            /*
             * find the corresponding rawFilesGroup from inputFiles
             */
            RawFilesGroup rawFilesGroup = new RawFilesGroup();
            String rawFileName = (String) maxRd.getAssayRawFileMap().
                    get(assName);
            rawFilesGroup.setId("rg_" + rawFileNameIdMap.get(rawFileName).
                    substring(4));

            assay.setRawFilesGroup(rawFilesGroup);

            //label free example
            if (maxRd.isLabelFree()) {
                assay.setLabel(label);
                assays.add(assay);
            } else {                  //non label free example
                //TODO: this is fixed label modification just used for example file
                //TODO: need to find a better way to form this later
                //find out if it is light or heavy label assay
                if (assName.toLowerCase(Locale.ENGLISH).contains("light")) {
                    assay.setLabel(label);
                    assays.add(assay);
                } else if (assName.toLowerCase(Locale.ENGLISH).contains("heavy")) {
                    Label labelHeavy = new Label();
                    CvParam labelLysine = new CvParam();
                    labelLysine.setAccession("MOD:00582");
                    labelLysine.setName("6x(13)C,2x(15)N labeled L-lysine");
                    labelLysine.setCv(cvMod);
                    labelLysine.setValue("Lys8");

                    CvParam labelArginine = new CvParam();
                    labelArginine.setAccession("MOD:00587");
                    labelArginine.setName("6x(13)C,4x(15)N labeled L-arginine");
                    labelArginine.setCv(cvMod);
                    labelArginine.setValue("Arg10");

                    ModParam modParamLysine = new ModParam();
                    modParamLysine.setCvParam(labelLysine);
                    labelHeavy.getModification().add(modParamLysine);

                    ModParam modParamArginine = new ModParam();
                    modParamArginine.setCvParam(labelArginine);
                    labelHeavy.getModification().add(modParamArginine);

                    assay.setLabel(labelHeavy);
                    assays.add(assay);
                }
                assI++;
            }
        }
    }

    private void createStudyVariableList() {
        svList = new StudyVariableList();
        List<StudyVariable> studyVariables = svList.getStudyVariable();
        Iterator iStudyGroup = maxRd.getStudyGroupMap().entrySet().iterator();
        while (iStudyGroup.hasNext()) {
            StudyVariable studyVariable = new StudyVariable();
            Map.Entry<String, List<String>> entry
                    = (Map.Entry<String, List<String>>) iStudyGroup.next();
            String key = entry.getKey();

            studyVariable.setName(key);
            studyVariable.setId("SV_" + key);
            List<String> value = entry.getValue();
            Iterator iV = value.iterator();
            List<String> assayRefList = studyVariable.getAssayRefs();
            while (iV.hasNext()) {
                Assay assay = new Assay();
                String assayName = (String) iV.next();
                String id = assayNameIdMap.get(assayName);
                assay.setId(id);
                assayRefList.add(id);
            }
            CvParam cvParam = createCvParam("technical replicate", "PSI-MS",
                                            "MS:1001808");
            studyVariable.getCvParam().add(cvParam);
            studyVariables.add(studyVariable);
        }
    }

    private void createRatioList() {
        ratioList = new RatioList();
        List<String> ratioTitles = (List<String>) maxRd.getPeptideRatioMap().
                get("0");
        for (int i = 0; i < ratioTitles.size(); i++) {
            Ratio ratio = new Ratio();
            String ratioTitle = (String) ratioTitles.get(i);
            ratio.setId(ratioTitle.replace(' ', '_').replace('/', '_'));

            ParamList ratioCalculations = new ParamList();
            ratioCalculations.getCvParam().add(createCvParam(
                    "simple ratio of two values", "PSI-MS", "MS:1001848"));
            ratio.setRatioCalculation(ratioCalculations);

            CvParamRef denomRef = new CvParamRef();
            denomRef.setCvParam(createCvParam("MaxQuant:feature intensity",
                                              "PSI-MS", "MS:1001903"));
            ratio.setDenominatorDataType(denomRef);

            CvParamRef numerRef = new CvParamRef();
            numerRef.setCvParam(createCvParam("MaxQuant:feature intensity",
                                              "PSI-MS", "MS:1001903"));
            ratio.setNumeratorDataType(numerRef);

            // TODO: a not very smart way to map denominator_ref and numerator_ref
            // TODO: to StudyVariable or Assay.
            // TODO: This is only fixed to H/L ratio
            // TODO: need to re-code
            List<String> primeStudyVars = maxRd.getPrimeStudyVariableList();
            for (String primeStudyVar : primeStudyVars) {
                if (ratioTitle.contains(primeStudyVar)) {
                    for (StudyVariable sv : svList.getStudyVariable()) {
                        if (sv.getId().contains(primeStudyVar + "_H")) {
                            ratio.setNumerator(sv);
                        } else if (sv.getId().contains(primeStudyVar + "_L")) {
                            ratio.setDenominator(sv);
                        }
                    }
                }
            }
            ratioList.getRatio().add(ratio);
        }
    }

    private void createProteinList() {
        Map<String, List<String>> proteinPeptidesMap = maxRd.
                getProteinPeptidesMap();

        protList = new ProteinList();
        List<Protein> proteins = protList.getProtein();

        /*
         * get the protein list from proteinPeptidesMap.keySet()
         */
        List<String> protAccList = new ArrayList(proteinPeptidesMap.keySet());
        Iterator iProt = protAccList.iterator();
        Map<String, String> proteinAccessionIdMap = new HashMap<>();

        while (iProt.hasNext()) {

            String protAccession = (String) iProt.next();

            List<String> pepSequences = proteinPeptidesMap.get(protAccession);
            int id = protAccList.indexOf(protAccession);
            String protId = "prot_" + Integer.toString(id);

            Protein protein = new Protein();
            protein.setId(protId);
            protein.setAccession(protAccession);
            proteinAccessionIdMap.put(protAccession, protId);
            protein.setSearchDatabase(db);

            if (pepSequences != null) {
                Iterator iPep = pepSequences.iterator();
                List<String> peptideConsensusRefList = protein.
                        getPeptideConsensusRefs();
                List<String> pepIds = new ArrayList<>();
                while (iPep.hasNext()) {
                    PeptideConsensus peptideConsensus = new PeptideConsensus();
                    String pepSeq = (String) iPep.next();
                    if (!pepSeq.isEmpty()) {
                        String pepId = "pep_" + pepSeq;
                        peptideConsensus.setId(pepId);
                        peptideConsensus.setPeptideSequence(pepSeq);

                        // avoid duplicate peptide existing in one protein
                        if (!pepIds.contains(pepId)) {
                            peptideConsensusRefList.add(pepId);
                            pepIds.add(pepId);
                        }
                    }
                }
            }
            proteins.add(protein);
        }

        /*
         * add GlobleQuantLayer to ProteinList row type is protein ref column
         * types are .......
         */
        // AssayQuantLayer for protein intensity if it is label free example
        if (maxRd.isLabelFree()) {
            QuantLayer assayQLProtInt = new QuantLayer();
            assayQLProtInt.setId("Prot_Assay_QL1");
            CvParamRef cvParamRefProtInt = new CvParamRef();
            cvParamRefProtInt.setCvParam(createCvParam(
                    "MaxQuant:feature intensity", "PSI-MS", "MS:1001903"));
            assayQLProtInt.setDataType(cvParamRefProtInt);

            Iterator iAss = maxRd.getAssayList().iterator();
            while (iAss.hasNext()) {
                String assName = (String) iAss.next();
                String assId = assayNameIdMap.get(assName);
                Assay assay = new Assay();
                assay.setId(assId);
                assayQLProtInt.getColumnIndex().add(assId);
            }

            DataMatrix protIntDM = new DataMatrix();
            TIntObjectIterator<TDoubleList> iProtInt = maxRd.
                    getProteinIntensityMap().iterator();
            while (iProtInt.hasNext()) {
                iProtInt.advance();
                int key = iProtInt.key();
                String protAcc = maxRd.getMajorityProteinIDMap().get(key);
                String proteinId = proteinAccessionIdMap.get(protAcc);
                if (proteinId != null) {
                    Protein protein = new Protein();
                    protein.setId(proteinId);
                    Row row = new Row();
                    row.setObject(protein);

                    TDoubleList value = iProtInt.value();
                    TDoubleIterator iV = value.iterator();

                    while (iV.hasNext()) {
                        String protIntV = String.valueOf(iV.next());
                        row.getValue().add(protIntV);
                    }
                    protIntDM.getRow().add(row);
                }
            }
            assayQLProtInt.setDataMatrix(protIntDM);
            protList.getAssayQuantLayer().add(assayQLProtInt);
        }

        // StudyVariableQuantLayer for protein intensity if it is NOT label free example
        if (!maxRd.isLabelFree()) {
            //int labelNum = maxRd.getLabelNumber();
            QuantLayer assayQLProtInt = new QuantLayer();
            assayQLProtInt.setId("Prot_StudyVariable_QL1");
            CvParamRef cvParamRefProtInt = new CvParamRef();
            cvParamRefProtInt.setCvParam(createCvParam(
                    "MaxQuant:feature intensity", "PSI-MS", "MS:1001903"));
            assayQLProtInt.setDataType(cvParamRefProtInt);

            Iterator iPrimeStu = maxRd.getPrimeStudyVariableList().iterator();
            while (iPrimeStu.hasNext()) {
                String studyVariableName = (String) iPrimeStu.next();

                StudyVariable studyVariableL = new StudyVariable();
                studyVariableL.setId("SV_" + studyVariableName + "_L");
                assayQLProtInt.getColumnIndex().add(studyVariableL.getId());

                StudyVariable studyVariableH = new StudyVariable();
                studyVariableH.setId("SV_" + studyVariableName + "_H");
                assayQLProtInt.getColumnIndex().add(studyVariableH.getId());
            }

            DataMatrix protIntDM = new DataMatrix();
            TIntObjectIterator<TDoubleList> iProtInt = maxRd.
                    getProteinIntensityMap().iterator();
            while (iProtInt.hasNext()) {
                iProtInt.advance();
                int key = iProtInt.key();
                String protAcc = maxRd.getMajorityProteinIDMap().get(key);
                String proteinId = proteinAccessionIdMap.get(protAcc);
                if (proteinId != null) {
                    Protein protein = new Protein();
                    protein.setId(proteinId);
                    Row row = new Row();
                    row.setObject(protein);

                    TDoubleList value = iProtInt.value();
                    TDoubleIterator iV = value.iterator();
                    while (iV.hasNext()) {
                        String protIntV = String.valueOf(iV.next());
                        row.getValue().add(protIntV);
                    }
                    protIntDM.getRow().add(row);
                }
            }
            assayQLProtInt.setDataMatrix(protIntDM);
            protList.getStudyVariableQuantLayer().add(assayQLProtInt);
        }

        //AssayQuantLayer for unique peptides if it is label-free example
        if (maxRd.isLabelFree()) {
            QuantLayer assayQLProtUniqpep = new QuantLayer();
            assayQLProtUniqpep.setId("Prot_Assay_QL2");
            CvParamRef cvParamRefProtUniqpep = new CvParamRef();
            cvParamRefProtUniqpep.setCvParam(createCvParam(
                    "MaxQuant:peptide counts (unique)", "PSI-MS", "MS:1001897"));
            assayQLProtUniqpep.setDataType(cvParamRefProtUniqpep);

            Iterator iAss = maxRd.getAssayList().iterator();
            while (iAss.hasNext()) {
                String assName = (String) iAss.next();
                String assId = assayNameIdMap.get(assName);
                Assay assay = new Assay();
                assay.setId(assId);
                assayQLProtUniqpep.getColumnIndex().add(assId);
            }

            DataMatrix protUniqPepDM = new DataMatrix();
            TIntObjectIterator<TIntList> iProtUniqPep = maxRd.
                    getProteinUniquePeptiedsMap().iterator();
            while (iProtUniqPep.hasNext()) {
                iProtUniqPep.advance();
                int key = iProtUniqPep.key();
                String protAcc = maxRd.getMajorityProteinIDMap().get(key);
                String proteinId = proteinAccessionIdMap.get(protAcc);
                if (proteinId != null) {
                    Protein protein = new Protein();
                    protein.setId(proteinId);
                    Row row = new Row();
                    row.setObject(protein);

                    TIntList value = iProtUniqPep.value();
                    TIntIterator iV = value.iterator();
                    while (iV.hasNext()) {
                        String protUniqPepV = String.valueOf(iV.next());
                        row.getValue().add(protUniqPepV);
                    }
                    protUniqPepDM.getRow().add(row);
                }
            }
            assayQLProtUniqpep.setDataMatrix(protUniqPepDM);
            protList.getAssayQuantLayer().add(assayQLProtUniqpep);
        }

        protList.setId("ProtL1");
    }

    private void createFeatureListList() {
        ftListList = new ArrayList();
        Map<String, FeatureList> rgIdFeatureListMap = new HashMap<>();
        peptideFeaturesMap = new HashMap<>();
        Map<String, List<String>> peptideAssaysMap = new HashMap<>();
        featureAssNameMap = new HashMap<>();

        evidenceMap = maxRd.getEvidenceMap();
        TIntObjectIterator<List<String>> iEvd = evidenceMap.iterator();
        while (iEvd.hasNext()) {
            iEvd.advance();
            int key = iEvd.key();
            List<String> value = iEvd.value();

            //TODO: revisit key_H
            // @key_H is the id number for heavy label created artificial
            String keyH = String.valueOf(key + evidenceMap.size());

            if (maxRd.isLabelFree()) {

                Feature feature = new Feature();
                /*
                 * The positions for features in label free example are:
                 * (0)Sequence,
                 * (1)Modifications,
                 * (2)Leading Proteins,
                 * (3)Raw File,
                 * (4)Experiment,
                 * (5)Charge,
                 * (6)m/z,
                 * (7)Retention Time,
                 * (8)Intensity.
                 */
                String reten = value.get(7);
                String mz = value.get(6);
                String chr = value.get(5);
                String rfn = value.get(3);
                String intensity = value.get(8);
                if (intensity.isEmpty()) {
                    intensity = "0";
                }

                feature.setMz(Double.parseDouble(mz));
                feature.setRt(reten);
                feature.setCharge(chr);

                String ftId = "ft_" + key;
                feature.setId(ftId);

                // create peptide sequence to feture HashMap: peptideFeaturesMap
                String pepSeq = value.get(0);
                if (pepSeq != null) {
                    List<Feature> fList = peptideFeaturesMap.get(pepSeq);
                    if (fList == null) {
                        fList = new ArrayList<>();
                        peptideFeaturesMap.put(pepSeq, fList);
                    }
                    fList.add(feature);
                }

                String rgId = "rg_" + rawFileNameIdMap.get(rfn + ".raw").
                        substring(4);

                // create peptide sequence to assay id HashMap: peptideAssaysMap
                String assayId = assayNameIdMap.get(rfn);
                if (pepSeq != null) {
                    List<String> aList = peptideAssaysMap.get(pepSeq);
                    if (aList == null) {
                        aList = new ArrayList<>();
                        peptideAssaysMap.put(pepSeq, aList);
                    }
                    if (!aList.contains(assayId)) {
                        aList.add(assayId);
                    }
                }

                FeatureList featureList = rgIdFeatureListMap.get(rgId);
                if (featureList == null) {
                    featureList = new FeatureList();
                    RawFilesGroup rawFilesGroup = new RawFilesGroup();
                    rawFilesGroup.setId(rgId);
                    featureList.setRawFilesGroup(rawFilesGroup);
                    String fListId = "Flist_" + rgId.substring(3);
                    featureList.setId(fListId);
                    rgIdFeatureListMap.put(rgId, featureList);
                    ftListList.add(featureList);

                    // create feature QuantLayer for label free
                    List<GlobalQuantLayer> featureQuantLayerList = featureList.
                            getFeatureQuantLayer();
                    GlobalQuantLayer featureQuantLayer = new GlobalQuantLayer();
                    featureQuantLayerList.add(featureQuantLayer);
                    featureQuantLayer.setId("FQL_" + fListId.substring(6));
                    ColumnDefinition featureColumnIndex = new ColumnDefinition();
                    featureQuantLayer.setColumnDefinition(featureColumnIndex);

                    // cvParam for intensity
                    Column featureColumnInt
                            = createColumn(0, createCvParam(
                                           "MaxQuant:feature intensity",
                                           "PSI-MS", "MS:1001903"));
                    featureColumnIndex.getColumn().add(featureColumnInt);

                    Row row = new Row();
                    row.setObject(feature);
                    row.getValue().add(intensity);

                    DataMatrix dataMatrix = new DataMatrix();
                    dataMatrix.getRow().add(row);
                    featureQuantLayer.setDataMatrix(dataMatrix);
                } else {
                    Row row = new Row();
                    row.setObject(feature);
                    row.getValue().add(intensity);
                    featureList.getFeatureQuantLayer().get(0).getDataMatrix().
                            getRow().add(row);
                }
                featureList.getFeature().add(feature);
                featureAssNameMap.put(feature.getId(), rfn);
            } else {
                /*
                 * not label free
                 */
                Feature featureL = new Feature();
                Feature featureH = new Feature();
                /*
                 * The positions for features in non label free example are:
                 * (0)Sequence,
                 * (1)Modifications,
                 * (2)Leading Proteins,
                 * (3)Raw File,
                 * (4)Experiment,
                 * (5)Charge,
                 * (6)m/z,
                 * (7)Retention Time,
                 * (8)Intensity,
                 * (9)Intensity L,
                 * (10)Intensity H.
                 */
                String reten = value.get(7);
                String mz = value.get(6);
                String chr = value.get(5);
                String rfn = value.get(3);
                String intensityL = value.get(9);
                if (intensityL.isEmpty()) {
                    intensityL = "0";
                }
                String intensityH = value.get(10);
                if (intensityH.isEmpty()) {
                    intensityH = "0";
                }

                // light label feature
                featureL.setMz(Double.parseDouble(mz));
                featureL.setRt(reten);
                featureL.setCharge(chr);

                String ftIdL = "ft_" + key;
                featureL.setId(ftIdL);

                // heavey label feature
                //TODO: this mass shift is artificial
                double massShift = 18;
                double mzH = Double.parseDouble(mz) + massShift / Double.
                        parseDouble(chr);
                featureH.setMz(mzH);

                //TODO: retention time is also artifical
                featureH.setRt(reten);

                featureH.setCharge(chr);

                String ftIdH = "ft_" + keyH;
                featureH.setId(ftIdH);

                // create peptide sequence to feture HashMap: peptideFeaturesMap
                String pepSeq = value.get(0);
                if (pepSeq != null) {
                    List<Feature> fList = peptideFeaturesMap.get(pepSeq);
                    if (fList == null) {
                        fList = new ArrayList<>();
                        peptideFeaturesMap.put(pepSeq, fList);
                    }
                    fList.add(featureL);
                    fList.add(featureH);
                }

                String rgId = "rg_" + rawFileNameIdMap.get(rfn + ".raw").
                        substring(4);

                String assayId = assayNameIdMap.get(rfn + "_Light");
                if (pepSeq != null) {
                    List<String> aList = peptideAssaysMap.get(pepSeq);
                    if (aList == null) {
                        aList = new ArrayList<>();
                        peptideAssaysMap.put(pepSeq, aList);
                    }

                    if (!aList.contains(assayId)) {
                        aList.add(assayId);
                    }
                }

                assayId = assayNameIdMap.get(rfn + "_Heavy");
                if (pepSeq != null) {
                    List<String> aList = peptideAssaysMap.get(pepSeq);
                    if (aList == null) {
                        aList = new ArrayList<>();
                        peptideAssaysMap.put(pepSeq, aList);
                    }

                    if (!aList.contains(assayId)) {
                        aList.add(assayId);
                    }
                }

                FeatureList featureList = rgIdFeatureListMap.get(rgId);
                if (featureList == null) {
                    featureList = new FeatureList();
                    RawFilesGroup rawFilesGroup = new RawFilesGroup();
                    rawFilesGroup.setId(rgId);
                    featureList.setRawFilesGroup(rawFilesGroup);
                    String fListId = "Flist_" + rgId.substring(3);
                    featureList.setId(fListId);
                    rgIdFeatureListMap.put(rgId, featureList);
                    ftListList.add(featureList);

                    // create feature QuantLayer for non label free
                    List<GlobalQuantLayer> featureQuantLayerList = featureList.
                            getFeatureQuantLayer();
                    GlobalQuantLayer featureQuantLayer = new GlobalQuantLayer();
                    featureQuantLayerList.add(featureQuantLayer);
                    featureQuantLayer.setId("FQL_" + fListId.substring(6));
                    ColumnDefinition featureColumnIndex = new ColumnDefinition();
                    featureQuantLayer.setColumnDefinition(featureColumnIndex);

                    // cvParam for intensity
                    Column featureColumnInt
                            = createColumn(0, createCvParam(
                                           "MaxQuant:feature intensity",
                                           "PSI-MS", "MS:1001903"));
                    featureColumnIndex.getColumn().add(featureColumnInt);

                    Row rowL = new Row();
                    rowL.setObject(featureL);
                    rowL.getValue().add(intensityL);

                    //TODO: this is artificial row for heavy label
                    Row rowH = new Row();
                    rowH.setObject(featureH);
                    rowH.getValue().add(intensityH);

                    DataMatrix dataMatrix = new DataMatrix();
                    dataMatrix.getRow().add(rowL);
                    dataMatrix.getRow().add(rowH);
                    featureQuantLayer.setDataMatrix(dataMatrix);
                } else {
                    Row rowL = new Row();
                    rowL.setObject(featureL);
                    rowL.getValue().add(intensityL);
                    featureList.getFeatureQuantLayer().get(0).getDataMatrix().
                            getRow().add(rowL);

                    //TODO: this is artificial row for heavy label
                    Row rowH = new Row();
                    rowH.setObject(featureH);
                    rowH.getValue().add(intensityH);
                    featureList.getFeatureQuantLayer().get(0).getDataMatrix().
                            getRow().add(rowH);
                }
                featureList.getFeature().add(featureL);
                featureList.getFeature().add(featureH);
                featureAssNameMap.put(featureL.getId(), rfn + "_Light");
                featureAssNameMap.put(featureH.getId(), rfn + "_Heavy");
            } // end for non label free
        }
    }

    private void createPeptideListList() {
        pepConListList = new ArrayList();
        PeptideConsensusList peptideConsensusList = new PeptideConsensusList();

        List<PeptideConsensus> peptideList = peptideConsensusList.
                getPeptideConsensus();

        Iterator iPep = maxRd.getPeptideList().iterator();
        Map<String, List<String>> peptideFeatureIdsMap = maxRd.
                getPeptideEvidenceIdsMap();

        DataMatrix pepIntDM = new DataMatrix();
        DataMatrix pepRatioDM = new DataMatrix();

        while (iPep.hasNext()) {
            PeptideConsensus peptideConsensus = new PeptideConsensus();
            String key = (String) iPep.next();

            if (!key.isEmpty()) {
                peptideConsensus.setId("pep_" + key);
                peptideConsensus.setPeptideSequence(key);
                peptideConsensus.setSearchDatabase(db);

                List<String> chrKeys = peptideFeatureIdsMap.get(key);
                if (!chrKeys.isEmpty()) {

                    Iterator<String> iChr = chrKeys.iterator();
                    while (iChr.hasNext()) {
                        String chrKey = iChr.next();
                        String charge = null;
                        if (NumberUtils.isNumber(chrKey) && evidenceMap.get(
                                Integer.parseInt(chrKey)) != null) {
                            charge = evidenceMap.get(Integer.
                                    parseInt(chrKey)).get(5);
                        }

                        if (!peptideConsensus.getCharge().contains(charge)) {
                            peptideConsensus.getCharge().add(charge);
                        }
//                            peptideConsensus.setId("pep_" + key);
//                            peptideConsensus.setPeptideSequence(key);
//                            peptideConsensus.setSearchDatabase(db);
                    }
                }
                // add Feature_refs to individual peptideConsensus
                List<Feature> fList = peptideFeaturesMap.get(key);
                if (fList != null) {
                    Iterator iFList = fList.iterator();
                    while (iFList.hasNext()) {
                        Feature f = (Feature) iFList.next();

                        EvidenceRef evRef = new EvidenceRef();
                        evRef.setFeature(f);
                        peptideConsensus.getEvidenceRef().add(evRef);

                        //add assay_refs
                        String assN = featureAssNameMap.get(f.getId());
                        String assId = assayNameIdMap.get(assN);

                        Assay tempAssay = new Assay();
                        tempAssay.setId(assId);
                        tempAssay.setName(assN);
                        evRef.getAssays().add(tempAssay);
                    }

                    // create DataMatrix of AssayQuantLayer for intensity
                    List<String> intKeys = (ArrayList) maxRd.
                            getPeptideIntensityMap().get(key);
                    Iterator iInt = intKeys.iterator();
                    Row row = new Row();
                    row.setObject(peptideConsensus);
                    while (iInt.hasNext()) {
                        String intKey = (String) iInt.next();
                        row.getValue().add(intKey);

                        //TODO: this is artificial (duplicate) intensity value for heavy label assay
                        if (!maxRd.isLabelFree()) {
                            row.getValue().add(intKey);
                        }
                    }
                    pepIntDM.getRow().add(row);
                }

                // create DataMatrix of RatioQuantLayer for non label free example
                if (!maxRd.isLabelFree()) {
                    Map<String, List<String>> peptideToRatioMap = maxRd.
                            getPeptideRatioMap();
                    List<String> ratioValues = peptideToRatioMap.get(key);
                    Row rowRatio = new Row();
                    rowRatio.setObject(peptideConsensus);
                    rowRatio.getValue().addAll(ratioValues);
                    pepRatioDM.getRow().add(rowRatio);
                }

                peptideList.add(peptideConsensus);
            }
        }

        /*
         * add AssayQuantLayer to peptides
         */
        QuantLayer pepAQLInt = new QuantLayer();
        pepAQLInt.setId("Pep_AQL_1");
        CvParamRef cvParamRefPepAqlInt = new CvParamRef();
        cvParamRefPepAqlInt.setCvParam(createCvParam(
                "MaxQuant:feature intensity", "PSI-MS", "MS:1001903"));
        pepAQLInt.setDataType(cvParamRefPepAqlInt);

        Iterator iAss = maxRd.getAssayList().iterator();
        while (iAss.hasNext()) {
            String assName = (String) iAss.next();
            String assId = assayNameIdMap.get(assName);
            Assay assay = new Assay();
            assay.setId(assId);
            pepAQLInt.getColumnIndex().add(assId);
        }
        pepAQLInt.setDataMatrix(pepIntDM);
        peptideConsensusList.getAssayQuantLayer().add(pepAQLInt);

        /*
         * add RatioQuantLayer to peptides if not lable free
         */
        if (!maxRd.isLabelFree()) {
            RatioQuantLayer pepRQL = new RatioQuantLayer();
            pepRQL.setId("Pep_RQL_1");

            pepRQL.setDataMatrix(pepRatioDM);
            for (Ratio ratio : ratioList.getRatio()) {
                pepRQL.getColumnIndex().add(ratio.getId());
            }

            peptideConsensusList.setRatioQuantLayer(pepRQL);
        }

        peptideConsensusList.setId("PepList1");
        peptideConsensusList.setFinalResult(true);

        pepConListList.add(peptideConsensusList);
    }

    private void createDataProcessingList() {
        dpList = new DataProcessingList();
        DataProcessing dataProcessing = new DataProcessing();
        dataProcessing.setId("feature_quantification");
        dataProcessing.setSoftware(software);
        dataProcessing.setOrder(BigInteger.ONE);
        ProcessingMethod processingMethod = new ProcessingMethod();
        processingMethod.setOrder(BigInteger.ONE);
        processingMethod.getParamGroup().add(createCvParam(
                "quantification data processing", "PSI-MS", "MS:1001861"));
        dataProcessing.getProcessingMethod().add(processingMethod);

        dpList.getDataProcessing().add(dataProcessing);
    }

    private void writeMzqFile(final String out) {
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
            String mzqId;
            if (maxRd.isLabelFree()) {
                mzqId = "MaxQuant-Label-Free-" + String.valueOf(day) + String.
                        valueOf(month) + String.valueOf(year);
            } else {
                mzqId = "MaxQuant-SILAC-" + String.valueOf(day) + String.
                        valueOf(month) + String.valueOf(year);
            }
            writer.write(MzQuantMLMarshaller.createMzQuantMLStartTag(mzqId)
                    + "\n");

            if (cvList != null) {
                mzqMsh.marshall(cvList, writer);
                writer.write("\n");
            }
            if (ac != null) {
                mzqMsh.marshall(ac, writer);
                writer.write("\n");
            }
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
            if (dpList != null) {
                mzqMsh.marshall(dpList, writer);
                writer.write("\n");
            }
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
            if (protGrpList != null) {
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
            if (smallMolList != null) {
                mzqMsh.marshall(smallMolList, writer);
                writer.write("\n");
            }

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

    /**
     * Convert to mzQuantML format with output file name.
     *
     * @param outputFn output file name
     *
     * @throws IOException io exception
     */
    public final void convert(final String outputFn)
            throws IOException {

        /**
         * ****************************************************************
         * start marshalling MzQuantML file
         ******************************************************************
         */
//        MzQuantML qml = new MzQuantML();
//
//        String version = "1.0.0";
//        qml.setVersion(version);
//
//        Calendar rightnow = Calendar.getInstance();
//        qml.setCreationDate(rightnow);
        db = setSearchDB("SD1", SEARCH_DATABASE_LOCATION, SEARCH_DATABASE_NAME);

        createCvList();

        createAnalysisSummary();

        //createAuditCollection();
        //
        createInputFiles();

        createAssayList();

        createStudyVariableList();

        if (!maxRd.isLabelFree()) {
            createRatioList();
        }

        createProteinList();

        createFeatureListList();

        createPeptideListList();

        createSoftwareList();

        createDataProcessingList();

        writeMzqFile(outputFn);
    }

    private static CvParam createCvParam(final String name, final String cvRef,
                                         final String accession) {
        CvParam cp = new CvParam();
        cp.setName(name);
        Cv cv = new Cv();
        cv.setId(cvRef);
        cp.setCv(cv);
        cp.setAccession(accession);
        return cp;
    }

    private static Column createColumn(final long index, final CvParam cvParam) {
        Column column = new Column();
        column.setIndex(BigInteger.valueOf(index));
        CvParamRef cvParamRef = new CvParamRef();
        cvParamRef.setCvParam(cvParam);
        column.setDataType(cvParamRef);
        return column;
    }

}
