
package uk.ac.man.mzqlib.postprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;

import uk.ac.liv.pgb.jmzqml.MzQuantMLElement;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Cv;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParamRef;
import uk.ac.liv.pgb.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.pgb.jmzqml.model.mzqml.IdOnly;
import uk.ac.liv.pgb.jmzqml.model.mzqml.IdentificationFile;
import uk.ac.liv.pgb.jmzqml.model.mzqml.IdentificationRef;
import uk.ac.liv.pgb.jmzqml.model.mzqml.MzQuantML;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Protein;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroup;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroupList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinRef;
import uk.ac.liv.pgb.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SearchDatabase;

import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 * This code is created for calculating protein abundance inference
 *
 * @author man-mqbsshz2
 * @version 0.2
 */
public final class ProteinAbundanceInference {

    private static final Map<String, Set<String>> proteinToPeptide
            = new HashMap<>();
    private static final Map<String, Set<String>> peptideToProtein
            = new HashMap<>();

    private static final Map<String, String> proteinToAccession
            = new HashMap<>();

    private static Map<String, Set<String>> sameSetGroup = new HashMap<>();
    private static Map<String, Set<String>> subSetGroup = new HashMap<>();
    private static Map<String, Set<String>> uniSetGroup = new HashMap<>();

    final static String path = "./src/main/resources/";
    final static String proteinGroupList = "ProteinGroupList11";
    final static String searchDatabase = "SD1";
    final static String cvParamId = "PSI-MS";
    final static String cvRef = "PSI-MS";

    private String in_file;
    private String outputProteinGroupDTAccession;
    private String outputRawProteinGroupDTAccession;
    private String outputProteinGroupDTName;
    private String outputRawProteinGroupDTName;

    static boolean conflictPeptideExcluded;

    /**
     * set output protein group datatype accession
     *
     * @param pgdta - datatype accession
     */
    public void setOutPGDTA(final String pgdta) {
        outputProteinGroupDTAccession = pgdta;
    }

    /**
     * set output protein group datatype name
     *
     * @param pgdtn - datatype name
     */
    public void setOutPGDTN(final String pgdtn) {
        outputProteinGroupDTName = pgdtn;
    }

    /**
     * set output raw protein group datatype accession
     *
     * @param rpgdta - datatype accession
     */
    public void setOutRawPGDTA(final String rpgdta) {
        outputRawProteinGroupDTAccession = rpgdta;
    }

    /**
     * set output raw protein group datatype name
     *
     * @param rpgdtn - datatype name
     */
    public void setOutRawPGDTN(final String rpgdtn) {
        outputRawProteinGroupDTName = rpgdtn;
    }

    /**
     * constructor
     *
     * @param in_file                          - input file
     * @param abundanceOperation               - calculation operator
     * @param inputDataTypeAccession           - input datatype accession
     * @param inputRawDataTypeAccession        - input raw datatype accession
     * @param outputProteinGroupDTAccession    - output protein group datatype
     *                                         accession
     * @param outputProteinGroupDTName         - output protein group datatype
     *                                         name
     * @param outputRawProteinGroupDTAccession - output raw protein group
     *                                         datatype accession
     * @param outputRawProteinGroupDTName      - output raw protein group
     *                                         datatype
     *                                         name
     * @param QuantLayerType                   - quant layer type
     *
     * @throws FileNotFoundException file not found exceptions.
     */
    public ProteinAbundanceInference(final String in_file,
                                     final String abundanceOperation,
                                     final String inputDataTypeAccession,
                                     final String outputProteinGroupDTAccession,
                                     final String outputProteinGroupDTName,
                                     final String outputRawProteinGroupDTAccession,
                                     final String outputRawProteinGroupDTName,
                                     final String QuantLayerType)
            throws FileNotFoundException {

        String cvAccessionPrefix = "MS:";
        int cvAccssionLength = 10;
        int cvAccessionLastSevenNumMax = 1002437;

        int length1 = inputDataTypeAccession.length();
        int length2 = outputProteinGroupDTAccession.length();
        String inputCvAccessionSuffix = inputDataTypeAccession.substring(3,
                                                                         inputDataTypeAccession.
                                                                         length()
                                                                         - 3);
        String outputCvAccessionSuffix = outputProteinGroupDTAccession.
                substring(3, outputProteinGroupDTAccession.length() - 3);
        String qlt1 = "AssayQuantLayer";
        String qlt2 = "RatioQuantLayer";
        String qlt3 = "StudyVariableQuantLayer";
        String co1 = "sum";
        String co2 = "mean";
        String co3 = "median";

        if (!(length1 == cvAccssionLength)) {
            throw new IllegalArgumentException(
                    "Invalid Input Peptide Datatype Parameter!!! "
                    + inputDataTypeAccession);
        }

        if (!(inputDataTypeAccession.substring(0, 3).equals(cvAccessionPrefix))
                || !(Integer.parseInt(inputCvAccessionSuffix) >= 0)
                || !(Integer.parseInt(inputCvAccessionSuffix)
                <= cvAccessionLastSevenNumMax)) {
            throw new IllegalArgumentException(
                    "Wrong Input Peptide Datatype CV Accession!!! "
                    + inputDataTypeAccession);
        }

        if (!(length2 == cvAccssionLength)) {
            throw new IllegalArgumentException(
                    "Invalid Output Protein Group CV Accession!!! "
                    + outputProteinGroupDTAccession);
        }

        if (!(outputProteinGroupDTAccession.substring(0, 3).equals(
                cvAccessionPrefix) && Integer.parseInt(outputCvAccessionSuffix)
                >= 0
                && Integer.parseInt(outputCvAccessionSuffix)
                <= cvAccessionLastSevenNumMax)) {
            throw new IllegalArgumentException(
                    "Wrong Output Protein Group CV Accession!!! "
                    + outputProteinGroupDTAccession);
        }

        if (!textHasContext(outputProteinGroupDTName)) {
            throw new IllegalArgumentException(
                    "Invalid Output Protein Group CV Name!!! "
                    + outputProteinGroupDTName);
        }

//        if (!textHasContext(QuantLayerType)) {
//            throw new IllegalArgumentException("Invalid Quant Layer Type!!!");
//        }
        if (!(QuantLayerType.equals(qlt1) || QuantLayerType.equals(qlt2)
                || QuantLayerType.equals(qlt3))) {
            throw new IllegalArgumentException("Invalid Quant Layer Type!!! "
                    + QuantLayerType);
        }

        if (!(abundanceOperation.equals(co1) || abundanceOperation.equals(co2)
                || abundanceOperation.equals(co3))) {
            throw new IllegalArgumentException("Method slected is not correct: "
                    + abundanceOperation);
        }

        this.in_file = in_file;
        this.outputProteinGroupDTAccession = outputProteinGroupDTAccession;
        this.outputProteinGroupDTName = outputProteinGroupDTName;
        this.outputRawProteinGroupDTAccession = outputRawProteinGroupDTAccession;
        this.outputRawProteinGroupDTName = outputRawProteinGroupDTName;

    }

    /**
     * constructor with the option for skipping the conflicting peptides.
     *
     * @param in_file                          - input file
     * @param abundanceOperation               - calculation operator
     * @param inputDataTypeAccession           - input datatype accession
     * @param inputRawDataTypeAccession        - input raw datatype accession
     * @param outputProteinGroupDTAccession    - output protein group datatype
     *                                         accession
     * @param outputProteinGroupDTName         - output protein group datatype
     *                                         name
     * @param outputRawProteinGroupDTAccession - output raw protein group
     *                                         datatype accession
     * @param outputRawProteinGroupDTName      - output raw protein group
     *                                         datatype
     *                                         name
     * @param QuantLayerType                   - quant layer type
     * @param conflictPeptideExcluded          - remove conflicting peptides
     *
     * @throws FileNotFoundException file not found exceptions.
     */
    public ProteinAbundanceInference(final String in_file,
                                     final String abundanceOperation,
                                     final String inputDataTypeAccession,
                                     final String inputRawDataTypeAccession,
                                     final String outputProteinGroupDTAccession,
                                     final String outputProteinGroupDTName,
                                     final String outputRawProteinGroupDTAccession,
                                     final String outputRawProteinGroupDTName,
                                     final String QuantLayerType,
                                     final boolean conflictPeptideExcluded)
            throws FileNotFoundException {

        String cvAccessionPrefix = "MS:";
        int cvAccssionLength = 10;
        int cvAccessionLastSevenNumMax = 1002437;

        int length1 = inputDataTypeAccession.length();
        int length2 = outputProteinGroupDTAccession.length();
        String inputCvAccessionSuffix = inputDataTypeAccession.substring(3,
                                                                         inputDataTypeAccession.
                                                                         length()
                                                                         - 3);
        String outputCvAccessionSuffix = outputProteinGroupDTAccession.
                substring(3, outputProteinGroupDTAccession.length() - 3);
        String qlt1 = "AssayQuantLayer";
        String qlt2 = "RatioQuantLayer";
        String qlt3 = "StudyVariableQuantLayer";
        String co1 = "sum";
        String co2 = "mean";
        String co3 = "median";

        if (!(length1 == cvAccssionLength)) {
            throw new IllegalArgumentException(
                    "Invalid Input Peptide Datatype Parameter!!! "
                    + inputDataTypeAccession);
        }

        if (!(inputDataTypeAccession.substring(0, 3).equals(cvAccessionPrefix))
                || !(Integer.parseInt(inputCvAccessionSuffix) >= 0)
                || !(Integer.parseInt(inputCvAccessionSuffix)
                <= cvAccessionLastSevenNumMax)) {
            throw new IllegalArgumentException(
                    "Wrong Input Peptide Datatype CV Accession!!! "
                    + inputDataTypeAccession);
        }

        if (!(length2 == cvAccssionLength)) {
            throw new IllegalArgumentException(
                    "Invalid Output Protein Group CV Accession!!! "
                    + outputProteinGroupDTAccession);
        }

        if (!(outputProteinGroupDTAccession.substring(0, 3).equals(
                cvAccessionPrefix) && Integer.parseInt(outputCvAccessionSuffix)
                >= 0
                && Integer.parseInt(outputCvAccessionSuffix)
                <= cvAccessionLastSevenNumMax)) {
            throw new IllegalArgumentException(
                    "Wrong Output Protein Group CV Accession!!! "
                    + outputProteinGroupDTAccession);
        }

        if (!textHasContext(outputProteinGroupDTName)) {
            throw new IllegalArgumentException(
                    "Invalid Output Protein Group CV Name!!! "
                    + outputProteinGroupDTName);
        }

//        if (!textHasContext(QuantLayerType)) {
//            throw new IllegalArgumentException("Invalid Quant Layer Type!!!");
//        }
        if (!(QuantLayerType.equals(qlt1) || QuantLayerType.equals(qlt2)
                || QuantLayerType.equals(qlt3))) {
            throw new IllegalArgumentException("Invalid Quant Layer Type!!! "
                    + QuantLayerType);
        }

        if (!(abundanceOperation.equals(co1) || abundanceOperation.equals(co2)
                || abundanceOperation.equals(co3))) {
            throw new IllegalArgumentException("Method slected is not correct: "
                    + abundanceOperation);
        }

        this.in_file = in_file;
        this.outputProteinGroupDTAccession = outputProteinGroupDTAccession;
        this.outputProteinGroupDTName = outputProteinGroupDTName;
        this.outputRawProteinGroupDTAccession = outputRawProteinGroupDTAccession;
        this.outputRawProteinGroupDTName = outputRawProteinGroupDTName;

        ProteinAbundanceInference.conflictPeptideExcluded
                = conflictPeptideExcluded;

    }

    /**
     * main method
     *
     * @param args - parameters for command line
     *
     * @throws JAXBException          jaxb exceptions.
     * @throws InstantiationException instantiation exceptions.
     * @throws IllegalAccessException illegal access exceptions.
     * @throws IllegalStateException  illegal state exceptions.
     * @throws FileNotFoundException  file not found exception.
     */
    public static void main(final String[] args)
            throws JAXBException, InstantiationException,
            IllegalAccessException, IllegalStateException, FileNotFoundException {

        String quantLT = "AssayQuantLayer";

//        String refNo = "RefAssay2";
//        String filter = "qvalue001";
//        String infile = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\IdenOutcomes\\Filtered_AJ_" + filter
//                + "\\mzq\\consensusonly\\test\\unlabeled_result_FLUQT_mapped_" + filter
//                + "_peptideNormalization_medianSelectedRefAssay_25Oct.mzq";
//
//        String outfile = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\Idenoutcomes\\Filtered_AJ_" + filter
//                + "\\mzq\\consensusonly\\test\\unlabeled_result_FLUQT_mapped_" + filter
//                + "_peptideNormalized" + refNo + "_protein_inference_revDecoyTest_25Oct_samesetOrdered.mzq";
//        String infile = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\"
//                + "IdenOutcomes\\CPTAC_EvsB\\qvalue001\\mzq\\"
//                + "unlabeled_result_FLUQT_mapped_normalised_peptide_speciesNormalised.mzq";
//                + "unlabeled_result_FLUQT_mapped_normalised_peptide.mzq";
//                + "unlabeled_result_FLUQT_mapped_normalised_simon.mzq";
//                + "unlabeled_result_FLUQT_mapped_speciesNormalised_peptide_1_0.mzq";
        String infile
                = "C:\\Manchester\\result\\CPTAC_Study6_Progenesis_result\\study6\\CPTAC_study6\\"
                + "data\\CPTAC_EvsB_unlabeled_result_FLUQT__mapped_v0.3.5_peptide_normalised.mzq";

//        String outfile = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\"
//                + "IdenOutcomes\\CPTAC_EvsB\\qvalue001\\mzq\\"
//                + "unlabeled_result_FLUQT_mapped_normalised_peptide_speciesNormalised_proteinInference.mzq";
//                + "unlabeled_result_FLUQT_mapped_normalised_peptide_proteinInference.mzq";
//                + "unlabeled_result_FLUQT_mapped_normalised_simon_proteinInference.mzq";
//                + "unlabeled_result_FLUQT_mapped_speciesNormalised_peptide_proteinInference_1_0.mzq";

        String operator = "sum"; //"median", "mean"

        String inputPeptideDTCA = null;
        String inputRawPeptideDTCA = null;
        String outputProteinGCA = null;
        String outputProteinGCN = null;
        String outputRawProteinGCA = null;
        String outputRawProteinGCN = null;
        boolean signalConflictPeptideExcluded = true; //conflicting peptides excluded

        inputPeptideDTCA = "MS:1001891"; //Progenesis:peptide normalised abundance
        inputRawPeptideDTCA = "MS:1001840";  //"MS:1001893"
        outputProteinGCA = "MS:1001890"; //Progenesis:protein normalised abundance
        outputProteinGCN = "Progenesis: protein normalised abundance";
        outputRawProteinGCA = "MS:1001892";
        outputRawProteinGCN = "Progenesis: protein raw abundance";

        //simon data
//        inputPeptideDTCA = "MS:1001850"; //Progenesis:peptide normalised abundance
//        inputRawPeptideDTCA = "MS:1001840";
//        outputProteinGCA = "MS:1002518"; //Progenesis:protein normalised abundance
//        outputProteinGCN = "Progenesis: protein normalised abundance";
//        outputRawProteinGCA = "MS:1002519";
//        outputRawProteinGCN = "Progenesis: protein raw abundance";
        if (args.length != 11 && args.length != 0) {
            System.out.println(
                    "Please input all eight parameters in order: input file, "
                    + "output file, quant layer type, input normalised peptide datatype CV accession,"
                    + "input raw peptide datatype CV accession, output protein group CV accession,"
                    + "output protein group CV name, output raw protein group CV accession, "
                    + "output raw protein group CV name" + "operator.");
        } else if (args.length == 11) {
            infile = args[0];
            operator = args[2];
            inputPeptideDTCA = args[3];
            inputRawPeptideDTCA = args[4];
            outputProteinGCA = args[5];
            outputProteinGCN = args[6];
            outputRawProteinGCA = args[7];
            outputRawProteinGCN = args[8];
            quantLT = args[9];
            signalConflictPeptideExcluded = Integer.parseInt(args[10]) != 0;
        }

        if (signalConflictPeptideExcluded) {
            ProteinAbundanceInference pai
                    = new ProteinAbundanceInference(infile, operator,
                                                    inputPeptideDTCA,
                                                    inputRawPeptideDTCA,
                                                    outputProteinGCA,
                                                    outputProteinGCN,
                                                    outputRawProteinGCA,
                                                    outputRawProteinGCN, quantLT,
                                                    signalConflictPeptideExcluded);
            pai.proteinInference(conflictPeptideExcluded);
        } else {
            ProteinAbundanceInference pai
                    = new ProteinAbundanceInference(infile, operator,
                                                    inputPeptideDTCA,
                                                    outputProteinGCA,
                                                    outputProteinGCN,
                                                    outputRawProteinGCA,
                                                    outputRawProteinGCN, quantLT);
            pai.proteinInference();
        }
    }

    /**
     * protein inference method
     *
     * @throws FileNotFoundException file not found exceptions.
     */
    public void proteinInference()
            throws FileNotFoundException {
        proteinInference(true);
    }

    /**
     * protein inference method with the parameter of conflicting signal
     *
     * @param signalConflict: with/without conflicting peptides
     *
     * @throws FileNotFoundException file not found exceptions.
     */
    public void proteinInference(final boolean signalConflict)
            throws FileNotFoundException {

        MzQuantMLUnmarshaller infile_um;
        try {
            infile_um = mzqFileInput(in_file);
            //remove the previous protein group list if existing
            checkProteinGroupList(infile_um);
//            pipeline_flag = pipeline_executor(infile_um, signalConflict);  // This function needs to be revisited as it doesn't do anything
        } catch (IllegalStateException ex) {
            System.out.println(
                    "****************************************************");
            System.out.println(
                    "The mzq file is not found!!! Please check the input.");
            System.err.println(ex);
            System.out.println(
                    "****************************************************");
        }

        System.out.println(
                "****************************************************");
            System.out.println(
                    "******************** The pipeline does work successfully! *********************");
            if (signalConflict) {
                System.out.println(
                        "**** The protein abundance is calculated by removing conflicting pepConsensuses! ****");
            }
    }


    /**
     * examine if there is a protein group list. If existing, remove it.
     *
     * @param um - unmarshalled mzq file
     */
    private void checkProteinGroupList(final MzQuantMLUnmarshaller um) {
        MzQuantML mzq = um.unmarshal(MzQuantMLElement.MzQuantML);
        ProteinGroupList protGrList = um.unmarshal(
                MzQuantMLElement.ProteinGroupList);
        if (protGrList != null) {
            mzq.setProteinGroupList(null);

            MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller(in_file);
            marshaller.marshall(mzq);
        }
    }

    /**
     * String examination
     *
     * @param aText - a character string
     *
     * @return - true/false
     */
    private boolean textHasContext(final String aText) {
        String EMPTY_STRING = "";
        return aText != null && !aText.trim().equals(EMPTY_STRING);
    }

    /**
     * unmarshal mzq to java object
     *
     * @param infile - mzq file
     *
     * @return - unmarshalled mzq Java object file
     *
     * @throws IllegalStateException illegal state exceptions.
     * @throws FileNotFoundException file not found exceptions.
     */
    public static MzQuantMLUnmarshaller mzqFileInput(final String infile)
            throws IllegalStateException, FileNotFoundException {
        File mzqFile = new File(infile);
        MzQuantMLUnmarshaller infile_um = new MzQuantMLUnmarshaller(mzqFile);
        return infile_um;
    }

    /**
     * get protein group list from the map of ordered groups
     *
     * @param protGL  - protein group list
     * @param groupIO - ordered protein by alphabet
     *
     * @return - protein group
     */
    private void proteinGroups(final ProteinGroupList protGL,
                               final Map<String, String> groupIO) {
        List<ProteinGroup> protGrs = protGL.getProteinGroup();
        for (int i = 1; i < groupIO.size() + 1; i++) {
            for (Map.Entry<String, String> entry : groupIO.entrySet()) {
                if (entry.getValue().equals("ProteinGroup" + i)) {
                    String proteinGroupId = entry.getValue();
                    String proteinGroupIdOri = entry.getKey();

//                    System.out.println("Protein Group ID: " + proteinGroupId);
                    ProteinGroup protGroup = new ProteinGroup();
                    protGroup.setId(proteinGroupId);

                    SearchDatabase searchDb = new SearchDatabase();
                    searchDb.setId(searchDatabase);
                    protGroup.setSearchDatabase(searchDb);

                    List<IdentificationRef> idenRefs = protGroup.
                            getIdentificationRef();
                    IdentificationRef idenRef = new IdentificationRef();
                    IdentificationFile idenFile = new IdentificationFile();
                    idenFile.setId("IdenFile");
                    idenFile.setName("fasf");
                    idenRef.setIdentificationFile(idenFile);

                    idenRefs.add(idenRef);

                    List<ProteinRef> protRefs = protGroup.getProteinRef();
                    String anchorProtein = null;

                    if (proteinGroupIdOri.contains("UniSetGroup")) {
                        String pepSel = uniSetGroup.get(proteinGroupIdOri).
                                iterator().next();
                        String protId = peptideToProtein.get(pepSel).iterator().
                                next();

                        ProteinRef protRef = new ProteinRef();
                        Protein prot = new Protein();

                        prot.setId(protId);
                        prot.setAccession(proteinToAccession.get(protId));
                        protRef.setProtein(prot);

                        List<CvParam> cvParams = protRef.getCvParam();

                        CvParam cvParam = new CvParam();
//                        cvParam.setAccession(prot.getAccession());
                        cvParam.setAccession("MS:1001591");
//                        System.out.println("Protein Accession: " + prot.getAccession());
                        Cv cv = new Cv();
                        cv.setId(cvRef);
                        cvParam.setCv(cv);

                        Set<String> setPeptides = uniSetGroup.get(
                                proteinGroupIdOri);
                        String pepTmp = setPeptides.iterator().next();
                        anchorProtein = peptideToProtein.get(pepTmp).iterator().
                                next();

//                        cvParam.setName(anchorProtein);
                        cvParam.setName("anchor protein");
                        //CvParamRef cvParamRef = new CvParamRef();
                        //cvParamRef.setCvParam(cvParam);
                        cvParams.add(cvParam);
                        protRefs.add(protRef);
                    }

                    if (proteinGroupIdOri.contains("SameSetGroup")) {
                        String pepSel = sameSetGroup.get(proteinGroupIdOri).
                                iterator().next();
//                        System.out.println("peptide selection: " + pepSel);
                        Set<String> protsId = peptideToProtein.get(pepSel);
                        //sort proteins
//                        Set<String> protIds = new TreeSet<String>(protsId);
                        Set<String> protIds = new TreeSet<>(protsId);
                        Iterator<String> protIdsIter = protIds.iterator();

                        int sig = 0;
                        while (protIdsIter.hasNext()) {
//                        for (String protId : protIds) {
                            String protId = protIdsIter.next();

                            //get the first protein
                            if (sig == 0) {
                                sig = 1;
                            } else {
                                sig = -1;
                            }
                            ProteinRef protRef = new ProteinRef();
                            Protein prot = new Protein();

                            prot.setId(protId);
                            prot.setAccession(proteinToAccession.get(protId));
                            protRef.setProtein(prot);

                            List<CvParam> cvParams = protRef.getCvParam();

                            CvParam cvParam = new CvParam();
//                            cvParam.setAccession(prot.getAccession());
                            if (sig == 1) {
                                cvParam.setAccession("MS:1001591");
                            } else {
                                cvParam.setAccession("MS:1001594");
                            }
//                            System.out.println("Protein Accession: " + prot.getAccession());
                            Cv cv = new Cv();
                            cv.setId(cvRef);
                            cvParam.setCv(cv);

//                            if (sig == 1) {
//                                anchorProtein = protId;
//                            }
//                            cvParam.setName(anchorProtein);
                            if (sig == 1) {
                                cvParam.setName("anchor protein");
                            } else {
                                cvParam.setName("sequence same-set protein");
                            }
                            //CvParamRef cvParamRef = new CvParamRef();
                            //cvParamRef.setCvParam(cvParam);
                            cvParams.add(cvParam);
                            protRefs.add(protRef);
                        }
                    }

                    if (proteinGroupIdOri.contains("SubSetGroup")) {
                        String pepSel = subSetGroup.get(proteinGroupIdOri).
                                iterator().next();

                        Set<String> protsId = peptideToProtein.get(pepSel);
                        //sort proteins
                        Set<String> protIds = new TreeSet<String>(protsId);
//                        if (proteinGroupIdOri.equalsIgnoreCase("SubSetGroup1")) {
//                        System.out.println( proteinGroupIdOri + " Peptides: " + subSetGroup.get(proteinGroupIdOri));
//                            System.out.println( proteinGroupIdOri + " IDs: " + protsId);
//                             System.out.println( proteinGroupIdOri + protIds);
//                        }
                        //anchor protein: protein with most peptides
                        int pepNo = 0;
                        for (String protId : protIds) {
                            int pepNoTmp = proteinToPeptide.get(protId).size();

                            if (pepNoTmp > pepNo) {
                                pepNo = pepNoTmp;
                                anchorProtein = protId;
                            }
                        }

//                            System.out.println("Anchor Protein: " + proteinGroupIdOri + anchorProtein);
                        for (String protId : protIds) {

                            ProteinRef protRef = new ProteinRef();
                            Protein prot = new Protein();

                            prot.setId(protId);
                            prot.setAccession(proteinToAccession.get(protId));
                            protRef.setProtein(prot);

                            List<CvParam> cvParams = protRef.getCvParam();

                            CvParam cvParam = new CvParam();
//                            cvParam.setAccession(prot.getAccession());
                            if (protId.equals(anchorProtein)) {
                                cvParam.setAccession("MS:1001591");
                            } else {
                                cvParam.setAccession("MS:1001596");
                            }
//                            System.out.println("Protein Accession: " + prot.getAccession());
                            Cv cv = new Cv();
                            cv.setId(cvRef);
                            cvParam.setCv(cv);

//                            cvParam.setName(anchorProtein);
                            if (protId.equals(anchorProtein)) {
                                cvParam.setName("anchor protein");
                            } else {
                                cvParam.setName("sequence sub-set protein");
                            }
                            //CvParamRef cvParamRef = new CvParamRef();
                            //cvParamRef.setCvParam(cvParam);
                            cvParams.add(cvParam);
                            protRefs.add(protRef);
                        }
                    }
                    protGrs.add(i - 1, protGroup);
                }
            }
        }
    }

    /**
     * create assay quant layers using the information of CvParams, calculated
     * abundances and grouping results.
     *
     * @param protGroList  - protein group list
     * @param aQLs         - assay quant layers
     * @param assayQI      - assay quant layer ID
     * @param inputPepDTCA - input peptide datatype accession
     * @param protAbun     - protein abundance
     * @param groupInOrd   - ordered protein group
     *
     * @return a boolean value for confirming whether the layers of assay quant
     *         are created correctly
     */
    private boolean assayQuantLayers(final ProteinGroupList protGroList,
                                     final List<QuantLayer<IdOnly>> aQLs,
                                     final String assayQI,
                                     final String inPQLID,
                                     final Map<String, List<String>> protAbun,
                                     final Map<String, String> groupInOrd,
                                     final String cvPA,
                                     final String cvPI,
                                     final String cvPN) {
        boolean first_layer = false;
        List<QuantLayer<IdOnly>> assayQuantLayers = protGroList.
                getAssayQuantLayer();
        QuantLayer assayQuantLayer = new QuantLayer();
        assayQuantLayer.setId(assayQI);

        /**
         * Create the part of DataType
         */
        CvParam cvParam1 = new CvParam();
        cvParam1.setAccession(cvPA);
        Cv cv = new Cv();
        cv.setId(cvPI);
        cvParam1.setCv(cv);
        cvParam1.setName(cvPN);
        CvParamRef cvParamRef1 = new CvParamRef();
        cvParamRef1.setCvParam(cvParam1);
        assayQuantLayer.setDataType(cvParamRef1);

        /**
         * Create the part of ColumnIndex
         */
        /**
         * Get the column indices from the QuantLayer in the original file and
         * then add these to the generated QuantLayer in ProteinGroup
         */
        for (QuantLayer assayQL : aQLs) {
            if (assayQL.getId().equalsIgnoreCase(inPQLID)) {

                List<String> assayCI = (List<String>) assayQL.getColumnIndex();
                int nCI = assayCI.size();
                for (int i = 0; i < nCI; i++) {
                    assayQuantLayer.getColumnIndex().add(assayCI.get(i));
                }
                //use the first APL encountered even if having multiple APLs with the same datatype
                first_layer = true;
                break;
            }
        }
        if (first_layer == false) {
            throw new IllegalStateException(
                    "The desired assay quant layer is not found!!! "
                    + "Please check the input data type accession.");
        }

        /**
         * Create the part of DataMatrix
         */
        DataMatrix dm = new DataMatrix();

        /**
         * make the records in order when outputing
         */
        Map<String, List<String>> proteinAbundanceTmp
                = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : protAbun.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            String newKey = groupInOrd.get(key);
            proteinAbundanceTmp.put(newKey, values);

        }

        /**
         * Alternatively, use tree map to sort the records in DataMatrix for
         * output
         */
//        Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(pa);
//        DataMatrix dMatrix = SortedMap(treeMap, dm, groupInOrder);
        Map<String, List<String>> treeMap = new TreeMap<>(
                proteinAbundanceTmp);
        DataMatrix dMatrix = Utils.sortedMap(treeMap, dm);

        /**
         * the data is input in the created QuantLayer
         */
        assayQuantLayer.setDataMatrix(dMatrix);
        assayQuantLayers.add(assayQuantLayer);
        return first_layer;
    }

}
