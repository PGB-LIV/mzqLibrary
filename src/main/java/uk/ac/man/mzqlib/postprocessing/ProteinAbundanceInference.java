package uk.ac.man.mzqlib.postprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import static java.lang.System.exit;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import uk.ac.liv.jmzqml.MzQuantMLElement;
//import static uk.ac.liv.jmzqml.MzQuantMLElement.MzQuantML;
import uk.ac.liv.jmzqml.model.mzqml.*;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 * This code is created for calculating protein abundance in terms of peptide
 * grouping.
 *
 * @author man-mqbsshz2
 * @version 0.1
 */
public final class ProteinAbundanceInference {

    private static final Map<String, HashSet<String>> proteinToPeptide = new HashMap<String, HashSet<String>>();
    private static final Map<String, HashSet<String>> peptideToProtein = new HashMap<String, HashSet<String>>();
    private Map<String, List<String>> peptideAssayValues = new HashMap<String, List<String>>();
    private static final Map<String, String> proteinToAccession = new HashMap<String, String>();

    private static Map<String, HashSet<String>> sameSetGroup = new HashMap<String, HashSet<String>>();
    private static Map<String, HashSet<String>> subSetGroup = new HashMap<String, HashSet<String>>();
    private static Map<String, HashSet<String>> uniSetGroup = new HashMap<String, HashSet<String>>();

    /*
     * the parameters in the generated ProteinGroupList
     */
    /*
     * calculation method for protein abundance
     */
//    final static String abunOperation = "sum";
    //        final String abunOperation = "mean";
//        final String abunOperation = "median";
    /**
     * other parameters
     */
    final static String proteinGroupList = "ProteinGroupList1";
//        final static String proteinGroupId = "prot_3";
    final static String searchDatabase = "SD1";
//    final static String assayQuantLayerId = "PGL_AQL3";
//    final static String cvParamAccession = "MS:1001893";
    final static String cvParamId = "PSI-MS";
    final static String cvRef = "PSI-MS";

    final static String path = "./src/main/resources/";

    public String in_file;
    public String out_file;
    static String quantLayerType;
    static String techName;
    static String abunOperation;
    static String inputPDTCA;
    static String inputRawPDTCA;
    static String cvParamAccession;
    static String cvParamName;
    static String outputAssayQuantLayerID;
    static String outputRawAssayQuantLayerID;

   /**
     * Constructor: can be externally called for execution
     *
     * @param input
     * @param output
     * @param expMethod
     * @param calOperation
     * @param inputPeptideDataTypeCvAcc
     * @param outputProteinGroupCvAcc
     * @param outputProteinGroupCvName
     * @param QuantLayerType
     * @throws java.io.FileNotFoundException
     */
    public ProteinAbundanceInference(String input, String output, String calOperation,
            String inputPeptideDataTypeCvAcc, String inputRawPeptideDataTypeCvAcc, String outputProteinGroupCvAcc, String outputProteinGroupCvName, String QuantLayerType) throws FileNotFoundException {

        String cvAccessionPrefix = "MS:";
        int cvAccssionLength = 10;
        int cvAccessionLastSevenNumMax = 1002437;

        int length1 = inputPeptideDataTypeCvAcc.length();
        int length2 = outputProteinGroupCvAcc.length();
        String inputCvAccessionSuffix = inputPeptideDataTypeCvAcc.substring(3, inputPeptideDataTypeCvAcc.length() - 3);
        String outputCvAccessionSuffix = outputProteinGroupCvAcc.substring(3, outputProteinGroupCvAcc.length() - 3);
        String qlt1 = "AssayQuantLayer";
        String qlt2 = "RatioQuantLayer";
        String qlt3 = "StudyVariableQuantLayer";
        String co1 = "sum";
        String co2 = "mean";
        String co3 = "median";

        if (!(length1 == cvAccssionLength)) {
            throw new IllegalArgumentException("Invalid Input Peptide Datatype Parameter!!! " + inputPeptideDataTypeCvAcc);
        }

        if (!(inputPeptideDataTypeCvAcc.substring(0, 3).equals(cvAccessionPrefix)) || !(Integer.parseInt(inputCvAccessionSuffix) >= 0)
                || !(Integer.parseInt(inputCvAccessionSuffix) <= cvAccessionLastSevenNumMax)) {
            throw new IllegalArgumentException("Wrong Input Peptide Datatype CV Accession!!! " + inputPeptideDataTypeCvAcc);
        }

        if (!(length2 == cvAccssionLength)) {
            throw new IllegalArgumentException("Invalid Output Protein Group CV Accession!!! " + outputProteinGroupCvAcc);
        }

        if (!(outputProteinGroupCvAcc.substring(0, 3).equals(cvAccessionPrefix) && (Integer.parseInt(outputCvAccessionSuffix) >= 0)
                && (Integer.parseInt(outputCvAccessionSuffix) <= cvAccessionLastSevenNumMax))) {
            throw new IllegalArgumentException("Wrong Output Protein Group CV Accession!!! " + outputProteinGroupCvAcc);
        }

        if (!textHasContext(outputProteinGroupCvName)) {
            throw new IllegalArgumentException("Invalid Output Protein Group CV Name!!! " + outputProteinGroupCvName);
        }

//        if (!textHasContext(QuantLayerType)) {
//            throw new IllegalArgumentException("Invalid Quant Layer Type!!!");
//        }
        if (!(QuantLayerType.equals(qlt1) || QuantLayerType.equals(qlt2) || QuantLayerType.equals(qlt3))) {
            throw new IllegalArgumentException("Invalid Quant Layer Type!!! " + QuantLayerType);
        }

        if (!(calOperation.equals(co1) || calOperation.equals(co2) || calOperation.equals(co3))) {
            throw new IllegalArgumentException("Method slected is not correct: " + calOperation);
        }

        in_file = input;
        out_file = output;
        abunOperation = calOperation;
        inputPDTCA = inputPeptideDataTypeCvAcc;
        inputRawPDTCA = inputRawPeptideDataTypeCvAcc;
        cvParamAccession = outputProteinGroupCvAcc;
        cvParamName = outputProteinGroupCvName;
        quantLayerType = QuantLayerType;

    }

    public static void main(String[] args) throws JAXBException, InstantiationException,
            IllegalAccessException, IllegalStateException, FileNotFoundException {

        String quantLT = "AssayQuantLayer";

        String refNo = "RefAssay2";
        String filter = "qvalue001";

        String infile = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\IdenOutcomes\\Filtered_AJ_" + filter
                + "\\mzq\\consensusonly\\test\\unlabeled_result_FLUQT_mapped_" + filter
                + "_peptideNormalization_medianSelectedRefAssay.mzq";
//                + "_peptideNormalized" + refNo + "_revDecoy.mzq";
        String outfile = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\Idenoutcomes\\Filtered_AJ_" + filter
                + "\\mzq\\consensusonly\\test\\unlabeled_result_FLUQT_mapped_" + filter
                + "_peptideNormalized" + refNo + "_protein_inference_revDecoyTest_raw1.mzq";

        String operator = "sum"; //"median", "mean"

//        String dataType = "normalised"; //"raw"
        String inputNormalisedPeptideDTCA = null;
        String inputRawPeptideDTCA = null;
//        String inputPepQuantLID = null;
        String outputProteinGCA = null;
        String outputProteinGCN = null;
        
//        MzQuantMLUnmarshaller umInfile = MzqFileInput(infile);

//        if (dataType.equalsIgnoreCase("normalised")) {

            inputNormalisedPeptideDTCA = "MS:1001891"; //Progenesis:peptide normalised abundance
            inputRawPeptideDTCA = "MS:1001893";
            outputProteinGCA = "MS:1001890"; //Progenesis:protein normalised abundance
            outputProteinGCN = "Progenesis: protein normalised abundance";

//        } else if (dataType.equalsIgnoreCase("raw")) {
//            inputPeptideDTCA = "MS:1001893";
//            inputRawPeptideDTCA = "MS:1001893";
////            inputPepQuantLID = "Pep_AQL.0"; //raw peptide abundance
//            outputProteinGCA = "MS:1001890";
//            outputProteinGCN = "Progenesis: protein normalised abundance with raw peptide abundance";
//        }
        
        if (args.length != 8 && args.length != 0) {
            System.out.println("Please input all four parameters in order: input file with path, "
                    + "output file with path, operator, data type");
        } else if (args.length == 8) {
            infile = args[0];
            outfile = args[1];
            quantLT = args[2];
            inputNormalisedPeptideDTCA = args[3];
            inputRawPeptideDTCA = args[4];
            outputProteinGCA = args[5];
            outputProteinGCN = args[6];
            operator = args[7];
//            dataType = args[6];
        }

        ProteinAbundanceInference pai = new ProteinAbundanceInference(infile, outfile, operator,
                inputNormalisedPeptideDTCA, inputRawPeptideDTCA, outputProteinGCA, outputProteinGCN, quantLT);
        pai.proteinInference(inputPDTCA, inputRawPDTCA, quantLayerType);

    }

    public void proteinInference(String inputPepDTCA, String inputRawPepDTCA, String QuantLayerType) throws FileNotFoundException {

        boolean file_flag = true;
        boolean pipeline_flag = true;

        MzQuantMLUnmarshaller infile_um = null;
        try {
            infile_um = MzqFileInput(this.in_file);
        } catch (IllegalStateException ex) {
            System.out.println("****************************************************");
            System.out.println("The mzq file is not found!!! Please check the input.");
            System.err.println(ex);
            System.out.println("****************************************************");
            file_flag = false;
            pipeline_flag = false;
        }

        if (file_flag == true) {
            pipeline_flag = pipeline_executor(infile_um, this.out_file, inputPepDTCA, inputRawPepDTCA, QuantLayerType);
        }

        System.out.println("****************************************************");
        if (pipeline_flag) {
            System.out.println("******* The pipeline does work successfully! *******");
            System.out.println("**** The protein abundance is output correctly! ****");
        } else {
            System.out.println("****** Some errors exist within the pipeline *******");
        }
        System.out.println("****************************************************");
    }

    /**
     * execute the main steps in the pipeline Note that peptideAssayValues need
     * to be obtained in terms of users' input, then we subsequently get
     * proteinToPeptide, proteinToAccession and proteinToProtein
     *
     * @param infile_um
     * @param outputFile
     * @param inputPepDTCA
     * @return a boolean value for checking whether the pipeline is correctly
     * implemented
     */
    public boolean pipeline_executor(MzQuantMLUnmarshaller infile_um, String outputFile, String inputDTCA, String inputRawDTCA, String quantLayerType) {

        boolean flag = true;
        String inputAssayQLID = "";
        String inputRawAssayQLID = "";
        Map<String, List<String>> peptideRawAssayValues = new HashMap<String, List<String>>();
//        String inputRawPQLID = "AQL_intensity";
        peptideRawAssayValues = PeptideAssayValues(infile_um, inputRawDTCA);
        peptideAssayValues = PeptideAssayValues(infile_um, inputDTCA);

        if (peptideAssayValues == null) {
            flag = false;
        }

        if (flag == false) {
            System.out.println("****************************************************");
            System.out.println("*** The desired assay quant layer is not found!!! **");
            System.out.println("**** Please check the input data type accession. ***");
            System.out.println("****************************************************");
            return flag;
        }

//        System.out.println("Peptide Assay Values: " + peptideAssayValues);
        ProteinToPeptide(infile_um, peptideAssayValues);
        ProteinToAccession(infile_um);
        PeptideToProtein(proteinToPeptide);

        if (quantLayerType.equals("AssayQuantLayer")) {
            List<QuantLayer<IdOnly>> assayQLs = AssayQLs(infile_um);

//            String assayQuantLayerId_pep = ; //AssayQuantLayerId(infile_um, inputPepDTCA);
//            for (QuantLayer assayQL : assayQLs) {
//                if ((assayQL.getDataType().getCvParam().getAccession()).equalsIgnoreCase(inputDTCA)) {
//                    inputAssayQLID = assayQL.getId();
////                    assNo = assayQL.getColumnIndex().size();
//                    break;
//                }
//            }

            inputAssayQLID = AssayQuantLayerId(infile_um, inputDTCA);
            outputAssayQuantLayerID = "PGL_" + inputAssayQLID;
            inputRawAssayQLID = AssayQuantLayerId(infile_um, inputRawDTCA);
            outputRawAssayQuantLayerID = "PGL_raw_" + inputRawAssayQLID;

            MzQuantML mzq = Mzq(infile_um);

//            System.out.println("protein to peptide: " + proteinToPeptide);
//            System.out.println("peptide to protein: " + peptideToProtein);
//        List<QuantLayer> ratioQLs = RatioQLs(infile_um);
//        List<QuantLayer> studyVariableQLs = StudyVariableQLs(infile_um);
            uniSetGroup = ProteinGrouping.UniSetGrouping(peptideToProtein, proteinToPeptide);
//            System.out.println("Uni set group: " + uniSetGroup);

            sameSetGroup = ProteinGrouping.SameSetGrouping(peptideToProtein, proteinToPeptide);
//            System.out.println("Same set group: " + sameSetGroup);

            subSetGroup = ProteinGrouping.SubSetGrouping(peptideToProtein, proteinToPeptide);

//            System.out.println("Same set group: " + sameSetGroup);
//            System.out.println("Sub set group: " + subSetGroup);
            Map<String, List<String>> proteinAbundance = ProteinAbundanceCalculation(abunOperation,
                    uniSetGroup, sameSetGroup, subSetGroup, peptideAssayValues);
            Map<String, List<String>> rawProteinAbundance = ProteinAbundanceCalculation(abunOperation,
                    uniSetGroup, sameSetGroup, subSetGroup, peptideRawAssayValues);

            MzqOutput(mzq, assayQLs, abunOperation, uniSetGroup, sameSetGroup,
                    subSetGroup, outputAssayQuantLayerID, inputAssayQLID, outputRawAssayQuantLayerID, 
                    inputRawAssayQLID, outputFile, proteinAbundance, rawProteinAbundance);

//        } else if (quantLayerType.equals("RatioQuantLayer")) {
//
//        } else if (quantLayerType.equals("StudyVariableQuantLayer")) {
//
//            List<QuantLayer> studyVariableQLs = StudyVariableQLs(infile_um);
//            String assayQuantLayerId_pep = inputPQLID; //AssayQuantLayerId(infile_um, inputPepDTCA);
//            outputAssayQuantLayerID = "PGL_" + assayQuantLayerId_pep;
//
//            MzQuantML mzq = Mzq(infile_um);
//
//            uniSetGroup = ProteinGrouping.UniSetGrouping(peptideToProtein, proteinToPeptide);
//            sameSetGroup = ProteinGrouping.SameSetGrouping(peptideToProtein, proteinToPeptide);
//            subSetGroup = ProteinGrouping.SubSetGrouping(peptideToProtein, proteinToPeptide);
//
//           
//            
//            MzqOutput(mzq, studyVariableQLs, abunOperation, uniSetGroup, sameSetGroup,
//                    subSetGroup, outputAssayQuantLayerID, inputPQLID, outputFile,
//                    proteinAbundance, rawProteinAbundance);
        }
        return flag;
    }

    /**
     * returns true if aText is non-null and has visible content
     *
     * @param aText
     * @return
     */
    private boolean textHasContext(String aText) {
        String EMPTY_STRING = "";
        return (aText != null) && (!aText.trim().equals(EMPTY_STRING));
    }

    /**
     * prepare the input file for unmarshalling later
     *
     * @param infile
     * @return a generated MzQuantML unmarsshalling object from input file
     * @throws FileNotFoundException
     */
    public static MzQuantMLUnmarshaller MzqFileInput(String infile) throws IllegalStateException, FileNotFoundException {
        File mzqFile = new File(infile);
        MzQuantMLUnmarshaller infile_um = new MzQuantMLUnmarshaller(mzqFile);
//        MzQuantMLUnmarshaller infile_um = new MzQuantMLUnmarshaller(new File(infile));

        return infile_um;
    }

    /**
     * get the peptide references of the assay quant layer which users are
     * interested at by inputing InputPeptideDataTypeCvAccession. Note: the
     * first layer encountered is obtained if there are multiple layers with the
     * same data type
     *
     * @param in_file_um
     * @param inputPeptideDTCA
     * @return a boolean value for checking whether the peptide assay value list
     * is obtained correctly
     */
    private Map<String, List<String>> PeptideAssayValues(MzQuantMLUnmarshaller in_file_um, String inputPepDTCA) {
        boolean first_list = false;
//        File mzqFile = new File(in_file);
//        MzQuantMLUnmarshaller um = new MzQuantMLUnmarshaller(mzqFile);
        Map<String, List<String>> peptideAV = new HashMap<String, List<String>>();

        PeptideConsensusList pepConList = in_file_um.unmarshal(MzQuantMLElement.PeptideConsensusList);
        List<QuantLayer<IdOnly>> assayQLs = pepConList.getAssayQuantLayer();
        for (QuantLayer assayQL : assayQLs) {
            if ((assayQL.getDataType().getCvParam().getAccession()).equalsIgnoreCase(inputPepDTCA)) {

                DataMatrix assayDM = assayQL.getDataMatrix();
                List<Row> rows = assayDM.getRow();
                for (Row row : rows) {
                    //get peptide reference
                    String peptideRef = row.getObjectRef();

                    //get value String type
                    List<String> values = row.getValue();

                    peptideAV.put(peptideRef, values);

//                    System.out.println("Peptide ref.: " + row.getObjectRef());
//                System.out.println("Peptide raw abundance: " + values.toString());
                }
                //use the first AQL encountered even if there are multiple AQLs with the same data type
//                System.out.println("Peptide Assay Values: " + peptideAssayValues);
                first_list = true;
                break;
            }
        }
        return peptideAV;
    }

    /**
     * obtain the map of Protein to Peptide, which the peptides are the existing
     * ones in the quant layer identified by the user
     *
     * @param in_file_um
     * @return a map from protein to peptide
     */
    private Map<String, HashSet<String>> ProteinToPeptide(MzQuantMLUnmarshaller in_file_um, Map<String, List<String>> pepAssVal) {
//    private Map<String, HashSet<String>> ProteinToPeptide(String in_file, String inputPeptideDTCA) {
//        Map<String, HashSet<String>> proteinToPep = new HashMap<String, HashSet<String>>();
//        Map<String, List<String>> pepAV = PeptideAssayValues(in_file, inputPeptideDTCA);
        Set<String> peptideList = pepAssVal.keySet();
        ProteinList protList = in_file_um.unmarshal(MzQuantMLElement.ProteinList);
        List<Protein> proteins = protList.getProtein();
        for (Protein protein : proteins) {

            //remove the decoy proteins
            String protAccession = protein.getAccession();
            if (!protAccession.contains("XXX_")) {
                //    

                List<String> pepConRefs = protein.getPeptideConsensusRefs();

                /**
                 * generate the protein-to-peptide map
                 */
                HashSet<String> setOfPeptides = new HashSet<String>();
                for (String pepCon : pepConRefs) {
                    //examine whether pepCon is in peptideList (given assayQuantLayer)
                    if (peptideList.contains(pepCon)) {

                        setOfPeptides.add(pepCon);

                        /**
                         * Accession or ID for protein
                         */
//                proteinToPeptide.put(protein.getAccession(), setOfPeptides);
                        proteinToPeptide.put(protein.getId(), setOfPeptides);
//                proteinToAccession.put(protein.getId(), protein.getAccession());
                    }
                }
            }
        }
        return proteinToPeptide;
    }

    /**
     *
     * @param in_file_um
     * @return a map from protein to its accession
     */
    private Map<String, String> ProteinToAccession(MzQuantMLUnmarshaller in_file_um) {

        ProteinList protList = in_file_um.unmarshal(MzQuantMLElement.ProteinList);
        List<Protein> proteins = protList.getProtein();
        for (Protein protein : proteins) {
            List<String> pepConRefs = protein.getPeptideConsensusRefs();

            /**
             * generate the protein-to-peptide map
             */
            HashSet<String> setOfPeptides = new HashSet<String>();
            for (String pepCon : pepConRefs) {
                setOfPeptides.add(pepCon);

                /*
                 * Accession or ID for protein
                 */
                proteinToAccession.put(protein.getId(), protein.getAccession());
            }
        }
        return proteinToAccession;
    }

    /**
     * obtain the map of Peptide-to-Protein from the map of Protein-to-Peptide
     *
     * @param protToPep
     * @return a map from peptide to protein in terms of the map of
     * protein-to-peptide
     */
    private Map<String, HashSet<String>> PeptideToProtein(Map<String, HashSet<String>> protToPep) {

        for (Map.Entry<String, HashSet<String>> entry : protToPep.entrySet()) {
            for (String value : entry.getValue()) {
                if (!peptideToProtein.containsKey(value)) {
                    peptideToProtein.put(value, new HashSet<String>());
                }
                peptideToProtein.get(value).add(entry.getKey());
            }
        }

        return peptideToProtein;
    }

    /**
     * obtain the assay quant layer list
     *
     * @param in_file_um
     * @return
     */
    private List<QuantLayer<IdOnly>> AssayQLs(MzQuantMLUnmarshaller in_file_um) {

        PeptideConsensusList pepConList = in_file_um.unmarshal(MzQuantMLElement.PeptideConsensusList);
        List<QuantLayer<IdOnly>> assayQLs = pepConList.getAssayQuantLayer();
        return assayQLs;
    }

    /**
     *
     * @param in_file_um
     * @return
     */
    private List<QuantLayer<IdOnly>> StudyVariableQLs(MzQuantMLUnmarshaller in_file_um) {

        PeptideConsensusList pepConList = in_file_um.unmarshal(MzQuantMLElement.PeptideConsensusList);
        List<QuantLayer<IdOnly>> sVQLs = pepConList.getStudyVariableQuantLayer();
        return sVQLs;
    }

    private RatioQuantLayer RatioQLs(MzQuantMLUnmarshaller in_file_um) {

        PeptideConsensusList pepConList = in_file_um.unmarshal(MzQuantMLElement.PeptideConsensusList);
        RatioQuantLayer ratioQLs = pepConList.getRatioQuantLayer();
        return ratioQLs;
    }

    /**
     * get the object of MzQuantML from the input file
     *
     * @param in_file1
     * @return
     */
    private MzQuantML Mzq(MzQuantMLUnmarshaller in_file_um) {

        MzQuantML mzq = in_file_um.unmarshal(MzQuantMLElement.MzQuantML);
        return mzq;
    }

    /**
     * get the identifier of assay quant layer
     *
     * @param in_file_um
     * @param inputPeptideDTCA
     * @return
     */
    private static String AssayQuantLayerId(MzQuantMLUnmarshaller in_file_um, String inputPeptideDTCA) {

        String assayQLID = null;
        PeptideConsensusList pepConList = in_file_um.unmarshal(MzQuantMLElement.PeptideConsensusList);

        List<QuantLayer<IdOnly>> assayQLs = pepConList.getAssayQuantLayer();

        for (QuantLayer assayQL : assayQLs) {
            if ((assayQL.getDataType().getCvParam().getAccession()).equalsIgnoreCase(inputPeptideDTCA)) {
                assayQLID = assayQL.getId();
                break;
            }
        }
        return assayQLID;

    }

    /**
     * output the pipeline result (protein groups, assay quant layers) with the
     * mzQuantML standard format
     *
     * @param mzq
     * @param assayQLs
     * @param operation
     * @param uniSetGr
     * @param sameSetGr
     * @param subSetGr
     * @param assayQlId
     * @param in_file1
     * @param inputPeptideDTCA
     * @return a boolean value for checking whether the output of MZQ format is
     * valid
     */
    private boolean MzqOutput(MzQuantML mzq, List<QuantLayer<IdOnly>> assayQLs, String operation,
            Map<String, HashSet<String>> uniSetGr, Map<String, HashSet<String>> sameSetGr,
            Map<String, HashSet<String>> subSetGr, String assayQlId, String inPepQLID, String outRawPQlId, String inRawPepQLID, 
            String outFile, Map<String, List<String>> protAbundance, Map<String, List<String>> rawProtAbundance) {

        boolean flag = true;
//        Map<String, List<String>> proteinAbundance = ProteinAbundanceCalculation(operation,
//                uniSetGr, sameSetGr, subSetGr, pepAssayVals);
        Map<String, String> groupInOrder = ProteinGrouping.GroupInOrder(protAbundance);

        Map<String, String> rawGroupInOrder = ProteinGrouping.GroupInOrder(rawProtAbundance);
//        System.out.println("Group in Order:" + groupInOrder.entrySet());
//        Map<String, String> rawGroupInOrder = ProteinGrouping.GroupInOrder(rawProtAbundance);

        //        uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList protGroupList = 
//        uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList.class.newInstance();
        ProteinGroupList protGroupList = null;
        try {
            protGroupList = ProteinGroupList.class.newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(ProteinAbundanceInference.class.getName()).log(Level.SEVERE, null, ex);
//            flag = false;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ProteinAbundanceInference.class.getName()).log(Level.SEVERE, null, ex);
//            flag = false;
        }

        protGroupList.setId(proteinGroupList);

        //create protein groups in ProteinGroupList
        ProteinGroups(protGroupList, groupInOrder);

        //create assay quant layers for raw peptide abundances
        flag = AssayQuantLayers(protGroupList, assayQLs, outRawPQlId, inRawPepQLID,
                rawProtAbundance, rawGroupInOrder, "MS:1001892", cvParamId, "Progenesis: protein raw abundance");

        //create assay quant layers in ProteinGroupList for normalised peptide abundances
        flag = AssayQuantLayers(protGroupList, assayQLs, assayQlId, inPepQLID,
                protAbundance, groupInOrder, cvParamAccession, cvParamId, cvParamName);

        /**
         * Create the ProteinGroupList in mzq
         */
        mzq.setProteinGroupList(protGroupList);

        /**
         * Marshall the created object to MzQuantML The output of MzQuantML
         * format file
         */
//  MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller("CPTAC-Progenesis-small-example_proteinAbundance.mzq");
//  MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller("test_grouping_data_proteinAbundance.mzq");
//  MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller("ProteoSuite_xTracker_fourProteins_result_proteinAbundance.mzq");
        MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller(outFile);
        marshaller.marshall(mzq);

//        System.out.println("Data Type/Accession: " + assayQuantLayer.getDataType().getCvParam().getAccession());
//        System.out.println("Data Type/CVRef: " + assayQuantLayer.getDataType().getCvParam().getCvRef());
//        System.out.println("Data Type/Name: " + assayQuantLayer.getDataType().getCvParam().getName());
        return flag;

    }

    /**
     * calculate the protein abundance with the grouping result
     *
     * @param operation
     * @param uniSetGr
     * @param sameSetGr
     * @param subSetGr
     * @param infile
     * @param inputPepDTCA
     * @return a map of protein-to-abundance
     */
    private Map<String, List<String>> ProteinAbundanceCalculation(String operation,
            Map<String, HashSet<String>> uniSetGr, Map<String, HashSet<String>> sameSetGr,
            Map<String, HashSet<String>> subSetGr, Map<String, List<String>> pepAssVal) {

        Map<String, List<String>> protAbu = new HashMap<String, List<String>>();
//        int groupId = 0;
        int groupLeader = 0;

        DecimalFormat df = new DecimalFormat(".000");
//        Set<String> peptideList = peptideAssayValues.keySet();
//        System.out.println("Pep List: " + peptideList);

        /**
         * unique set case
         */
        for (Map.Entry<String, HashSet<String>> entry : uniSetGr.entrySet()) {
//            groupId++;
            /**
             * get the dimension of peptide assay values
             */

            String pepSelect = entry.getValue().iterator().next().toString();
            List<String> assayValTmp = pepAssVal.get(pepSelect);
            //number of assay values in each peptide
            int assaySize = assayValTmp.size();
            //number of peptides correponding to the protein
            int numPeptides = entry.getValue().size();

            /**
             * create an array for calculating the abundance
             */
            double[] operationResult = new double[assaySize + 1];
            double[] operationPepValue = new double[assaySize];
            double[][] matrixPepValue = new double[numPeptides][assaySize];
            int tempNo = 0;

            /**
             * initialization of operationOfpepValues, matrixPepValues
             */
            for (int i = 0; i < assaySize; i++) {
//                sumOfpepValues[i] = 0;

                operationPepValue[i] = 0;
                operationResult[i] = 0;
                for (int j = 0; j < numPeptides; j++) {
                    matrixPepValue[j][i] = 0;
                }
            }
            operationResult[assaySize] = 0;
//            operationResult[assaySize + 1] = 0;
//            operationPepValue[assaySize] = 0;
//            operationPepValue[assaySize + 1] = 0;

//            System.out.println("Sum of peptide element values: " + Arrays.toString(sumOfpepValues));
//            System.out.println("Protein ID: " + entry.getKey());
            for (String peptide : entry.getValue()) {
                List<String> assayValues = pepAssVal.get(peptide);

                for (int j = 0; j < assayValues.size(); j++) {
                    String componentValue = assayValues.get(j);

                    /**
                     * set NaN/Null to zero
                     */
                    double temp = (componentValue.equals("NaN") | componentValue.equals("nan")
                            | componentValue.equals("Null") | componentValue.equals("null"))
                                    ? Double.parseDouble("0") : Double.parseDouble(componentValue);
//                        sumOfpepValues[j] = sumOfpepValues[j] + temp;
                    matrixPepValue[tempNo][j] = temp;
                }
                tempNo++;
            }

            /**
             * calculation the abundance according to operation method
             */
            if (operation.equals("sum")) {
                operationPepValue = Utils.ColumnSum(matrixPepValue);
            } else if (operation.equals("mean")) {
                operationPepValue = Utils.ColumnSum(matrixPepValue);
                for (int i = 0; i < operationPepValue.length; i++) {
                    operationPepValue[i] = operationPepValue[i] / numPeptides;
                }
            } else if (operation.equals("median")) {
                double[] tmp = new double[numPeptides];
                for (int j = 0; j < operationPepValue.length; j++) {
                    for (int i = 0; i < numPeptides; i++) {
                        tmp[i] = matrixPepValue[i][j];
                    }
                    operationPepValue[j] = Utils.Median(tmp);
                }
            }

            System.arraycopy(operationPepValue, 0, operationResult, 0, assaySize);
            operationResult[assaySize] = groupLeader;
//            operationResult[assaySize + 1] = groupId;

            String[] operationResultFormat = new String[assaySize + 1];
            for (int i = 0; i < assaySize + 1; i++) {
                operationResultFormat[i] = df.format(operationResult[i]);
            }

//            System.out.println("format: " + Arrays.asList(operationResultFormat));
//            List<String> proteinAbundanceList = Arrays.asList(Arrays.toString(operationResult));
            List<String> proteinAbundanceList = Arrays.asList(operationResultFormat);
            protAbu.put(entry.getKey(), proteinAbundanceList);
//            proteinAbundance.put("ProteinGroup" + groupId, proteinAbundanceList);
//            }
        }

        /**
         * sameSet case
         */
        for (Map.Entry<String, HashSet<String>> entry : sameSetGr.entrySet()) {

//            groupId++;
            String pepSelect = entry.getValue().iterator().next().toString();
//            System.out.println("Pep Selected: " + pepSelect);

            List<String> assayValTmp = pepAssVal.get(pepSelect);
//            System.out.println("Assay Val Tmp: " + assayValTmp);

            //number of assay values in each peptide
            int assaySize = assayValTmp.size();
            //number of peptides correponding to the protein
            int numPeptides = entry.getValue().size();

            /**
             * create an array for calculating the abundance
             */
            double[] operationResult = new double[assaySize + 1];
            double[] operationPepValue = new double[assaySize];
            double[][] matrixPepValue = new double[numPeptides][assaySize];
            int tempNo = 0;

            /**
             * initialization of operationOfpepValues, matrixPepValues
             */
            for (int i = 0; i < assaySize; i++) {
//                sumOfpepValues[i] = 0;

                operationPepValue[i] = 0;
                operationResult[i] = 0;
                for (int j = 0; j < numPeptides; j++) {
                    matrixPepValue[j][i] = 0;
                }
            }
            operationResult[assaySize] = 0;
//            operationResult[assaySize + 1] = 0;

            for (String peptide : entry.getValue()) {
                List<String> assayValues = pepAssVal.get(peptide);
//                System.out.println("assay value: " + assayValues);
                for (int j = 0; j < assayValues.size(); j++) {
                    String componentValue = assayValues.get(j);

                    /**
                     * set NaN/Null to zero
                     */
                    double temp = (componentValue.equals("NaN") | componentValue.equals("nan")
                            | componentValue.equals("Null") | componentValue.equals("null"))
                                    ? Double.parseDouble("0") : Double.parseDouble(componentValue);
                    matrixPepValue[tempNo][j] = temp;
                }
                tempNo++;
            }

//System.out.println("Same Set Peptides: " + entry.getValue());
            /**
             * calculation the abundance according to operation method
             */
            if (operation.equals("sum")) {
                operationPepValue = Utils.ColumnSum(matrixPepValue);
            } else if (operation.equals("mean")) {
                operationPepValue = Utils.ColumnSum(matrixPepValue);
                for (int i = 0; i < operationPepValue.length; i++) {
                    operationPepValue[i] = operationPepValue[i] / numPeptides;
                }
            } else if (operation.equals("median")) {
                double[] tmp = new double[numPeptides];
                for (int j = 0; j < operationPepValue.length; j++) {
                    for (int i = 0; i < numPeptides; i++) {
                        tmp[i] = matrixPepValue[i][j];
                    }
                    operationPepValue[j] = Utils.Median(tmp);
                }
            }
            System.arraycopy(operationPepValue, 0, operationResult, 0, assaySize);
            operationResult[assaySize] = groupLeader;
//            operationResult[assaySize + 1] = groupId;

            String[] operationResultFormat = new String[assaySize + 1];
            for (int i = 0; i < assaySize + 1; i++) {
                operationResultFormat[i] = df.format(operationResult[i]);
            }
            List<String> proteinAbundanceList = Arrays.asList(operationResultFormat);

//            List<String> proteinAbundanceList = Arrays.asList(Arrays.toString(operationResult));
            protAbu.put(entry.getKey(), proteinAbundanceList);
//proteinAbundance.put("ProteinGroup" + groupId, proteinAbundanceList);
//            }
        }

        /**
         * subSet case
         */
        for (Map.Entry<String, HashSet<String>> entry : subSetGr.entrySet()) {

//            groupId++;
            String pepSelect = entry.getValue().iterator().next().toString();
//            HashSet<String> proteins = peptideToProtein.get(pepSelect);
            List<String> assayValTmp = pepAssVal.get(pepSelect);
            //number of assay values in each peptide
            int assaySize = assayValTmp.size();
            //number of peptides correponding to the protein
            int numPeptides = entry.getValue().size();

            /**
             * create an array for calculating the abundance
             */
            double[] operationResult = new double[assaySize + 1];
            double[] operationPepValue = new double[assaySize];
            double[][] matrixPepValue = new double[numPeptides][assaySize];
            int tempNo = 0;

            /*
             * initialization of operationOfpepValues, matrixPepValues
             */
            for (int i = 0; i < assaySize; i++) {
//                sumOfpepValues[i] = 0;

                operationPepValue[i] = 0;
                operationResult[i] = 0;
                for (int j = 0; j < numPeptides; j++) {
                    matrixPepValue[j][i] = 0;
                }
            }
            operationResult[assaySize] = 0;

            for (String peptide : entry.getValue()) {
                List<String> assayValues = pepAssVal.get(peptide);
                for (int j = 0; j < assayValues.size(); j++) {
                    String componentValue = assayValues.get(j);

                    /**
                     * set NaN/Null to zero
                     */
                    double temp = (componentValue.equals("NaN") | componentValue.equals("nan")
                            | componentValue.equals("Null") | componentValue.equals("null"))
                                    ? Double.parseDouble("0") : Double.parseDouble(componentValue);
                    matrixPepValue[tempNo][j] = temp;
                }
                tempNo++;
            }

            /**
             * calculation the abundance according to operation method
             */
            if (operation.equals("sum")) {
                operationPepValue = Utils.ColumnSum(matrixPepValue);
            } else if (operation.equals("mean")) {
                operationPepValue = Utils.ColumnSum(matrixPepValue);
                for (int i = 0; i < operationPepValue.length; i++) {
                    operationPepValue[i] = operationPepValue[i] / numPeptides;
                }
            } else if (operation.equals("median")) {
                double[] tmp = new double[numPeptides];
                for (int j = 0; j < operationPepValue.length; j++) {
                    for (int i = 0; i < numPeptides; i++) {
                        tmp[i] = matrixPepValue[i][j];
                    }
                    operationPepValue[j] = Utils.Median(tmp);
                }
            }
            System.arraycopy(operationPepValue, 0, operationResult, 0, assaySize);
            operationResult[assaySize] = groupLeader;
//            operationResult[assaySize + 1] = groupId;

            String[] operationResultFormat = new String[assaySize + 1];
            for (int i = 0; i < assaySize + 1; i++) {
                operationResultFormat[i] = df.format(operationResult[i]);
            }
            List<String> proteinAbundanceList = Arrays.asList(operationResultFormat);

            protAbu.put(entry.getKey(), proteinAbundanceList);
        }

//        System.out.println("Protein abundance:" + proteinAbundance);
        return protAbu;
    }

    /**
     * get protein group list from the map of ordered groups
     *
     * @param protGL
     * @param groupIO
     * @return
     */
    private void ProteinGroups(ProteinGroupList protGL, Map<String, String> groupIO) {
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

                    List<IdentificationRef> idenRefs = protGroup.getIdentificationRef();
                    IdentificationRef idenRef = new IdentificationRef();
                    IdentificationFile idenFile = new IdentificationFile();
                    idenFile.setId("IdenFile");
                    idenFile.setName("fasf");
                    idenRef.setIdentificationFile(idenFile);

                    idenRefs.add(idenRef);

                    List<ProteinRef> protRefs = protGroup.getProteinRef();
                    String anchorProtein = null;

                    if (proteinGroupIdOri.contains("UniSetGroup")) {
                        String pepSel = uniSetGroup.get(proteinGroupIdOri).iterator().next().toString();
                        String protId = peptideToProtein.get(pepSel).iterator().next().toString();

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

                        HashSet<String> setPeptides = uniSetGroup.get(proteinGroupIdOri);
                        String pepTmp = setPeptides.iterator().next();
                        anchorProtein = peptideToProtein.get(pepTmp).iterator().next();

//                        cvParam.setName(anchorProtein);
                        cvParam.setName("anchor protein");
                        CvParamRef cvParamRef = new CvParamRef();
                        cvParamRef.setCvParam(cvParam);
                        cvParams.add(cvParam);
                        protRefs.add(protRef);
                    }

                    if (proteinGroupIdOri.contains("SameSetGroup")) {
                        String pepSel = sameSetGroup.get(proteinGroupIdOri).iterator().next().toString();
//                        System.out.println("peptide selection: " + pepSel);
                        Set<String> protsId = peptideToProtein.get(pepSel);
                        //sort proteins
                        Set<String> protIds = new TreeSet<String>(protsId);

                        int sig = 0;
                        for (String protId : protIds) {
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
                            CvParamRef cvParamRef = new CvParamRef();
                            cvParamRef.setCvParam(cvParam);
                            cvParams.add(cvParam);
                            protRefs.add(protRef);
                        }
                    }

                    if (proteinGroupIdOri.contains("SubSetGroup")) {
                        String pepSel = subSetGroup.get(proteinGroupIdOri).iterator().next().toString();

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
                            CvParamRef cvParamRef = new CvParamRef();
                            cvParamRef.setCvParam(cvParam);
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
     * @param protGroList
     * @param aQLs
     * @param assayQI
     * @param inputPepDTCA
     * @param protAbun
     * @param groupInOrd
     * @return a boolean value for checking whether the layers of assay quant
     * are created correctly
     */
    private boolean AssayQuantLayers(ProteinGroupList protGroList, List<QuantLayer<IdOnly>> aQLs, String assayQI,
            String inPQLID, Map<String, List<String>> protAbun, Map<String, String> groupInOrd,
            String cvPA, String cvPI, String cvPN) {
        boolean first_layer = false;
        List<QuantLayer<IdOnly>> assayQuantLayers = protGroList.getAssayQuantLayer();
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
            System.out.println("The desired assay quant layer is not found!!! "
                    + "Please check the input data type accession.");
            exit(0);
        }

        /**
         * Create the part of DataMatrix
         */
        DataMatrix dm = new DataMatrix() {
        };

        /**
         * make the records in order when outputing
         */
        Map<String, List<String>> proteinAbundanceTmp = new HashMap<String, List<String>>();
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
        Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(proteinAbundanceTmp);
        DataMatrix dMatrix = Utils.SortedMap(treeMap, dm);

        /**
         * the data is input in the created QuantLayer
         */
        assayQuantLayer.setDataMatrix(dMatrix);
        assayQuantLayers.add(assayQuantLayer);
        return first_layer;
    }

}
