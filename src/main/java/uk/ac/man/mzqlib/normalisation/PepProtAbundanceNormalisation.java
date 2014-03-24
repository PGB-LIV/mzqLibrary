package uk.ac.man.mzqlib.normalisation;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.Cv;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.CvParamRef;
import uk.ac.liv.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.jmzqml.model.mzqml.MzQuantML;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList;
import uk.ac.liv.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.Row;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 * This class is to normalise the peptide and protein group abundance in terms
 * of users' need.
 *
 * @author man-mqbsshz2
 */
public class PepProtAbundanceNormalisation {

    private static final Map<String, List<String>> peptideAssayValues = new HashMap<String, List<String>>();
    private static final Map<String, List<String>> NormalisedPeptideAssayValues = new HashMap<String, List<String>>();

    private static final Map<String, List<String>> proteinAssayValues = new HashMap<String, List<String>>();
    private static final Map<String, List<String>> NormalisedProteinAssayValues = new HashMap<String, List<String>>();

    final static String path = "./src/main/resources/example/";
    final static double thresholdConfidence = 2;
    final static double coefficientMAD = 1.4826;

    static String in_file;
    static String out_file;
    static String techName;
    static String inputPDTCA;
    static String cvParamAccession;
    static String cvParamId;
    static String cvParamName;
    static String assayQuantLayerId;

    /**
     * The constructor
     *
     * @param input
     * @param output
     * @param level
     * @param outputCvName
     * @param assQuantLayerID
     * @param inputDataTypeCvAcc
     * @param outputCvAcc
     * @param QuantLayerType
     * @param refNo
     * @throws FileNotFoundException
     */
    public PepProtAbundanceNormalisation(String input, String output, String level,
            String outputCvName, String assQuantLayerID, String inputDataTypeCvAcc, String outputCvAcc,
            String QuantLayerType, String refNo) throws FileNotFoundException {

        String cvAccessionPrefix = "MS:";
        int cvAccssionLength = 10;
        int cvAccessionLastSevenNumMax = 1002437;

        int length1 = inputDataTypeCvAcc.length();
        int length2 = outputCvAcc.length();
        String inputCvAccessionSuffix = inputDataTypeCvAcc.substring(3, inputDataTypeCvAcc.length() - 3);
        String outputCvAccessionSuffix = outputCvAcc.substring(3, outputCvAcc.length() - 3);
        String qlt1 = "AssayQuantLayer";
        String qlt2 = "RatioQuantLayer";
        String qlt3 = "StudyVariableQuantLayer";

        if (!(length1 == cvAccssionLength)) {
            throw new IllegalArgumentException("Invalid Input Peptide Datatype Parameter!!! " + inputDataTypeCvAcc);
        }

        if (!(inputDataTypeCvAcc.substring(0, 3).equals(cvAccessionPrefix)) || !(Integer.parseInt(inputCvAccessionSuffix) >= 0)
                || !(Integer.parseInt(inputCvAccessionSuffix) <= cvAccessionLastSevenNumMax)) {
            throw new IllegalArgumentException("Wrong Input Peptide Datatype CV Accession!!! " + inputDataTypeCvAcc);
        }

        if (!(length2 == cvAccssionLength)) {
            throw new IllegalArgumentException("Invalid Output Protein Group CV Accession!!! " + outputCvAcc);
        }
        if (!(outputCvAcc.substring(0, 3).equals(cvAccessionPrefix) && (Integer.parseInt(outputCvAccessionSuffix) >= 0)
                && (Integer.parseInt(outputCvAccessionSuffix) <= cvAccessionLastSevenNumMax))) {
            throw new IllegalArgumentException("Wrong Output Protein Group CV Accession!!! " + outputCvAcc);
        }
//        if (!textHasContext(outputProteinGroupCvName)) {
//            throw new IllegalArgumentException("Invalid Output Protein Group CV Name!!! " + outputProteinGroupCvName);
//        }
        if (!(QuantLayerType.equals(qlt1) || QuantLayerType.equals(qlt2) || QuantLayerType.equals(qlt3))) {
            throw new IllegalArgumentException("Invalid Quant Layer Type!!! " + QuantLayerType);
        }

        boolean file_flag = true;
        boolean pipeline_flag = true;
        in_file = input;
        out_file = output;
        cvParamId = "PSI-MS";
//        assayQuantLayerId = assQuantLayerID;
        inputPDTCA = inputDataTypeCvAcc; //raw values: MS:1001893
        cvParamAccession = outputCvAcc;
        cvParamName = outputCvName;

        MzQuantMLUnmarshaller infile_um = null;

        try {
            infile_um = MzqFileInput(in_file);
        } catch (IllegalStateException ex) {
            System.out.println("***************************************************************");
            System.out.println("The mzq file is not found!!! Please check the first input file.");
            System.err.println(ex);
            System.out.println("***************************************************************");
            file_flag = false;
            pipeline_flag = false;
        }

        if (file_flag == true) {
            pipeline_flag = pipeline_executor(infile_um, out_file, level, assQuantLayerID, inputPDTCA, cvParamAccession,
                    QuantLayerType, refNo);
        }

        System.out.println("****************************************************");
        if (pipeline_flag) {
            System.out.println("******** The pipeline does work successfully! *********");
            System.out.println("**** The normalisation result is output correctly! ****");
        } else {
            System.out.println("****** Some errors exist within the pipeline *******");
        }
        System.out.println("****************************************************");

    }

    /**
     *
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
//        String infile = path + "test_grouping_data_update.mzq";
//        String outfile = path + "test_grouping_data_update_normalised.mzq";

//        String infile = path + "test_grouping_data_update_pa.mzq";
//        String outfile = path + "test_grouping_data_update_pa_normalised.mzq";
        String infile = path + "CPTAC-study6_EvsB_mzqFile.mzq";
//        String outfile = path + "CPTAC-study6_EvsB_mzqFile_peptide_normalised.mzq";
        String outfile = path + "CPTAC-study6_EvsB_mzqFile_protein_normalised.mzq";

        //peptide level
//        String processingLevel = "peptide";
//        String inputPepProtDTCA = "MS:1001893"; //peptide
//        String assayQLID = "Pep_AQL0";
        
        //protein group level
        String processingLevel = "protein";
        String inputPepProtDTCA = "MS:1001890"; //protein
        String assayQLID = "ProtGrp_AQL2";

//        String method = "Label-free";
        String cvName = "Normalised " + processingLevel + " abundance";

        String outputPepProtDTCA = "MS:1001891";
        String quantLT = "AssayQuantLayer";
//        String refNumber = "9";
        String refNumber = "5"; //CPTAC

        if (args.length != 9 & args.length != 0) {
            System.out.println("Please input all nine parameters in order: input file and "
                    + "output file with path, method, processing level, control variable name"
                    + ", assay quantlayer identifier, input datatype cvAccession, output datatype"
                    + "cvAccession, QuantLayerType and reference number.");
        } else if (args.length == 9) {
            infile = args[0];
            outfile = args[1];
//            method = args[2];
            processingLevel = args[2];
            cvName = args[3];
            assayQLID = args[4];
            inputPepProtDTCA = args[5];
            outputPepProtDTCA = args[6];
            quantLT = args[7];
            refNumber = args[8];
        }

        new PepProtAbundanceNormalisation(infile, outfile, processingLevel, cvName, assayQLID,
                inputPepProtDTCA, outputPepProtDTCA, quantLT, refNumber);
    }

    /**
     * This executor is to execute the main steps in the normalisation.
     *
     * @param infile_um
     * @param outputFile
     * @param normalisedLevel
     * @param assayQuantId
     * @param inDTCA
     * @param outDTCA
     * @param quantLayerType
     * @param reference
     * @return a boolean value to show whether the pipeline is successfully
     * carried out.
     */
    private boolean pipeline_executor(MzQuantMLUnmarshaller infile_um, String outputFile, String normalisedLevel,
            String assayQuantId, String inDTCA, String outDTCA, String quantLayerType, String reference) {

        boolean flag = true;

        if (normalisedLevel == "peptide") {
            flag = PeptideAssayValue(infile_um, inDTCA);
            if (flag == false) {
                System.out.println("****************************************************************************");
                System.out.println("****** The desired assay quant layer in the input file is not found!!! *****");
                System.out.println("************** Please check the input data type accession. *****************");
                System.out.println("****************************************************************************");
                return flag;
            }

            int pepSize = peptideAssayValues.entrySet().iterator().next().getValue().size();
            System.out.println("peptide assay values No: " + pepSize);

            if (!(Integer.parseInt(reference) >= 0) || !(Integer.parseInt(reference) <= pepSize)) {
                throw new IllegalArgumentException("Wrongly select the reference number!!! "
                        + "It should be an integer in [1 " + pepSize + "]");
            }
            flag = OutputMzqPeptideNormalisation(infile_um, outputFile, assayQuantId,
                    inDTCA, outDTCA, quantLayerType, reference);
        }

        if (normalisedLevel == "protein") {
//            flag = ProteinAssayValue(infile_um, inDTCA);

            flag = ProteinAssayValue(infile_um, assayQuantId);
            if (flag == false) {
                System.out.println("****************************************************************************");
                System.out.println("******* The desired assay quant layer in the input file is not found!!! ****");
                System.out.println("************** Please check the input data type accession. *****************");
                System.out.println("****************************************************************************");
                return flag;
            }

            int proSize = proteinAssayValues.entrySet().iterator().next().getValue().size();

            if (!(Integer.parseInt(reference) >= 0) || !(Integer.parseInt(reference) <= proSize)) {
                throw new IllegalArgumentException("Wrongly select the reference number!!! "
                        + "It should be an integer in [1 " + proSize + "]");
            }

            flag = OutputMzqProteinNormalisation(infile_um, outputFile, assayQuantId,
                    inDTCA, outDTCA, quantLayerType, reference);
        }

        return flag;

    }

    /**
     * convert the input file to the mzq format
     *
     * @param infile
     * @return
     * @throws IllegalStateException
     * @throws FileNotFoundException
     */
    private static MzQuantMLUnmarshaller MzqFileInput(String infile) throws IllegalStateException, FileNotFoundException {
        File mzqFile = new File(infile);
        MzQuantMLUnmarshaller infile_um = new MzQuantMLUnmarshaller(mzqFile);
        return infile_um;
    }

    private MzQuantML Mzq(MzQuantMLUnmarshaller in_file_um) {
        MzQuantML mzq = in_file_um.unmarshal(MzQuantMLElement.MzQuantML);
        return mzq;
    }

    /**
     * obtain the peptide assay quant values in the map of peptideAssayValues
     *
     * @param in_file_um
     * @param inputPeptideDTCA
     * @return a boolean value
     */
    private boolean PeptideAssayValue(MzQuantMLUnmarshaller in_file_um, String inputPeptideDTCA) {
        boolean first_list = false;
        PeptideConsensusList pepConList = in_file_um.unmarshal(MzQuantMLElement.PeptideConsensusList);
        List<QuantLayer> assayQLs = pepConList.getAssayQuantLayer();
        for (QuantLayer assayQL : assayQLs) {
            if ((assayQL.getDataType().getCvParam().getAccession()).equalsIgnoreCase(inputPeptideDTCA)) {

                DataMatrix assayDM = assayQL.getDataMatrix();
                List<Row> rows = assayDM.getRow();
                for (Row row : rows) {
                    //get peptide reference
                    String peptideRef = row.getObjectRef();

                    //get value String type
                    List<String> values = row.getValue();

                    peptideAssayValues.put(peptideRef, values);

//                    System.out.println("Peptide ref.: " + row.getObjectRef());
//                System.out.println("Peptide raw abundance: " + values.toString());
                }
                //use the first AQL encountered even if there are multiple AQLs with the same data type
                first_list = true;
                break;
            }
        }
        return first_list;
    }

    /**
     * obtain the protein assay values in the map of proteinAssayValues
     *
     * @param in_file_um
     * @param inputProteinDTCA
     * @return a boolean value
     */
    private boolean ProteinAssayValue(MzQuantMLUnmarshaller in_file_um, String assayID) {
        boolean first_list = false;
        ProteinGroupList proGroupList = in_file_um.unmarshal(MzQuantMLElement.ProteinGroupList);
        List<QuantLayer> assayQLs = proGroupList.getAssayQuantLayer();
        for (QuantLayer assayQL : assayQLs) {
//            System.out.println("Assay Quant Layer ID: " + assayQL.getId());
//            if ((assayQL.getDataType().getCvParam().getAccession()).equalsIgnoreCase(inputProteinDTCA)) {
            
            if (assayQL.getId().equalsIgnoreCase(assayID)) {
                DataMatrix assayDM = assayQL.getDataMatrix();
                List<Row> rows = assayDM.getRow();
                for (Row row : rows) {
                    //get protein reference
                    String proteinRef = row.getObjectRef();

                    //get value String type
                    List<String> values = row.getValue();

                    proteinAssayValues.put(proteinRef, values);

                }
//System.out.println("protein assay values: " + proteinAssayValues);
                first_list = true;
                break;
            }
        }
        return first_list;
    }

    /**
     * carry out the calculation of normalisation based on the MAD algorithm.
     *
     * @param ref
     * @param PAV
     * @return the map with the normalised values
     */
    private Map<String, List<String>> NormalisedAssayValue(String ref, Map<String, List<String>> PAV) {

        Map<String, List<String>> normalisedPAV = new HashMap<String, List<String>>();
        Map<String, List<String>> ratioPAV = new HashMap<String, List<String>>();
        Set<Entry<String, List<String>>> entrys = PAV.entrySet();

        DecimalFormat df = new DecimalFormat(".000");
        double threshold_confidence = thresholdConfidence;
        double coef_mad = coefficientMAD;
        int vSize = PAV.entrySet().iterator().next().getValue().size();
//        System.out.println("vSize: " + vSize);
        int entryNo = PAV.size();
        double[] scalingFactor = new double[vSize];
        int entryRow = 0;
        double med = 0.0;
        double MAD = 0;
        double upper = 0;
        double lower = 0;
//        System.out.println("entry no: " + entryNo);
        double[][] valArr = new double[entryNo][vSize];
        String[] valArrRow = new String[vSize];
        String[] valArr_key = new String[entryNo];
        double[] refCol = new double[entryNo];
        int refNo = Integer.parseInt(ref) - 1;

        for (Map.Entry<String, List<String>> entry : entrys) {
            List<String> ratioVals = new ArrayList<String>();
            String key = entry.getKey();
            String vRef = entry.getValue().get(refNo);
//            System.out.println("Ref " + entryRow + ": " + vRef);
            refCol[entryRow] = Double.parseDouble(vRef);
            for (int col = 0; col < vSize; col++) {
                String vj = entry.getValue().get(col);
//                System.out.println("vj " + entryRow + " " + col + ": " + vj);
                double ratioVal = Double.parseDouble(vRef) / Double.parseDouble(vj);

//                System.out.println("Ratio Values: " + ratioVal);
                ratioVals.add(col, Double.toString(ratioVal));
                valArr[entryRow][col] = Double.parseDouble(vj);
            }
//            System.out.println("val Array: " + Arrays.toString(valArr[i]));
//            System.out.println("key: " + key + "  ratio Vals: " + ratioVals);
            valArr_key[entryRow] = key;
            ratioPAV.put(key, ratioVals);
            entryRow++;
        }
//        System.out.println("ratio PAV: " + ratioPAV);
//        System.out.println("Assay Size: " + vSize);

        int run = 0;
        for (int col = 0; col < vSize; col++) {
            int nonZero = 0;
            double[] objCol = new double[entryNo];
            double[] logRatio = new double[entryNo];
            for (int row = 0; row < entryNo; row++) {
                objCol[row] = valArr[row][col];
//                System.out.println("ref column: " + refCol[row]);
//                System.out.println("object column: " + objCol[row]);
                if (!(refCol[row] == 0) && !(objCol[row] == 0)) {
                    logRatio[nonZero] = Math.log10(refCol[row] / objCol[row]);
//                    System.out.println("Log Ratio " + nonZero + " " + col + ": " + logRatio[nonZero]);
                    nonZero++;
                }
            }

//            System.out.println("k " + k);
            double[] logRatioTmp = new double[nonZero];
            double[] logRatioTmp1 = new double[nonZero];
            double[] logRatio_med = new double[nonZero];
            boolean check = true;
            for (int k = 0; k < nonZero; k++) {
                logRatioTmp[k] = logRatio[k];
            }

            while (true) {
//                System.out.println("log ratio vector: " + Arrays.toString(logRatio));
                med = Utils.Median(logRatioTmp);

                for (int i = 0; i < nonZero; i++) {
                    logRatio_med[i] = Math.abs(logRatioTmp[i] - med);
                }
                MAD = coef_mad * Utils.Median(logRatio_med);
                upper = med + threshold_confidence * MAD;
                lower = med - threshold_confidence * MAD;

//                System.out.println("upper: " + upper);
//                System.out.println("lower: " + lower);
//                System.out.println("log Ratio Tmp: " + Arrays.toString(logRatioTmp));
                int nonZeroTmp = 0;
                for (int i = 0; i < nonZero; i++) {
                    if (logRatioTmp[i] <= upper && logRatioTmp[i] >= lower) {
                        logRatioTmp1[nonZeroTmp] = logRatioTmp[i];
                        // avoid the deadlock
                        if (nonZeroTmp == nonZero - 1) {
                            check = true;
                        }
                        nonZeroTmp++;

                    } else {
                        check = false;
                    }
                }
//                System.out.println("log Ratio Tmp1: " + Arrays.toString(logRatioTmp1));
//                System.out.println("check: " + check);

                logRatioTmp = (double[]) Utils.resizeArray(logRatioTmp, nonZeroTmp);
                logRatio_med = (double[]) Utils.resizeArray(logRatio_med, nonZeroTmp);
                for (int i = 0; i < nonZeroTmp; i++) {
                    logRatioTmp[i] = logRatioTmp1[i];
                }

                logRatioTmp1 = (double[]) Utils.resizeArray(logRatioTmp1, nonZeroTmp);
                nonZero = nonZeroTmp;
                if (check == true) {
                    scalingFactor[col] = Math.pow(10, Utils.mean(logRatioTmp));
//                    System.out.println("Scaling Factor " + j + ": " + scalingFactor[j]);
                    run++;
                    break;
                }
//           System.out.println("kk: " + kk);      
//           System.out.println("Run: " + run);     
            }
            check = true;
        }

        System.out.println("Scaling Factor: " + Arrays.toString(scalingFactor));

        for (int row = 0; row < entryNo; row++) {
            List<String> valArrRowList = new ArrayList<String>();
            for (int col = 0; col < vSize; col++) {
//                System.out.println("ii: " + ii);
                double valArrTmp = valArr[row][col] * scalingFactor[col];

                valArrRow[col] = df.format(valArrTmp);
//                valArrRow[col] = Double.toString(valArrTmp);

                valArrRowList.add(valArrRow[col]);
            }
            normalisedPAV.put(valArr_key[row], valArrRowList);
        }

//        System.out.println("normalisedPAV: " + normalisedPAV);
        return normalisedPAV;
    }

    /**
     * output the peptide result.
     *
     * @param infile_um
     * @param outFile
     * @param assayQI
     * @param inputDTCA
     * @param outputDTCA
     * @param quantLT
     * @param refAssay
     * @return
     */
    private boolean OutputMzqPeptideNormalisation(MzQuantMLUnmarshaller infile_um, String outFile, String assayQI,
            String inputDTCA, String outputDTCA, String quantLT, String refAssay) {

        boolean flag = false;
        Map<String, List<String>> normalisedPepAssayVal;
        flag = PeptideAssayValue(infile_um, inputDTCA);

        if (flag == true) {
            PeptideConsensusList pepConList = infile_um.unmarshal(MzQuantMLElement.PeptideConsensusList);
            List<QuantLayer> assayQLs = pepConList.getAssayQuantLayer();
            normalisedPepAssayVal = NormalisedAssayValue(refAssay, peptideAssayValues);

            if (quantLT.equals("AssayQuantLayer")) {

//                List<QuantLayer> assayQLs = AssayQLs(infile_um);
                QuantLayer newQL = new QuantLayer();
                newQL.setId(assayQI);

                /**
                 * Create the part of DataType
                 */
                CvParam cvParam1 = new CvParam();
                cvParam1.setAccession(outputDTCA);
                Cv cv = new Cv();

                cv.setId(cvParamId);
                cvParam1.setCv(cv);
                cvParam1.setName(cvParamName);
                CvParamRef cvParamRef1 = new CvParamRef();
                cvParamRef1.setCvParam(cvParam1);
                newQL.setDataType(cvParamRef1);

                /**
                 * Create the part of ColumnIndex
                 */
                /**
                 * Get the column indices from the QuantLayer in the original
                 * file and then add these to the generated QuantLayer in
                 * ProteinGroup
                 */
                for (QuantLayer assayQL : assayQLs) {
                    if ((assayQL.getDataType().getCvParam().getAccession()).equalsIgnoreCase(inputDTCA)) {

                        List<String> assayCI = (List<String>) assayQL.getColumnIndex();
                        int nCI = assayCI.size();
                        for (int i = 0; i < nCI; i++) {
                            newQL.getColumnIndex().add(assayCI.get(i));
//                            System.out.println("assayCI: " + assayCI.get(i));
                        }

                        break;
                    }
                }

                /**
                 * Create the part of DataMatrix
                 */
                DataMatrix dm = new DataMatrix() {
                };

                /**
                 * make the records in order when outputing
                 */
                Map<String, List<String>> normalisedTmp = new HashMap<String, List<String>>();
                for (Map.Entry<String, List<String>> entry : normalisedPepAssayVal.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
//            String newKey = groupInOrd.get(key);
                    normalisedTmp.put(key, values);

                }

                Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(normalisedTmp);
                DataMatrix dMatrix = Utils.SortedMap(treeMap, dm);

                newQL.setDataMatrix(dMatrix);
                pepConList.getAssayQuantLayer().add(newQL);

                MzQuantML mzq = infile_um.unmarshal(MzQuantMLElement.MzQuantML);
                mzq.getPeptideConsensusList().clear();
                mzq.getPeptideConsensusList().add(pepConList);

                MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller(outFile);
                marshaller.marshall(mzq);
            }
        } else {
            System.out.println("Does not obtain Peptide Assay Raw Values correctly!!!");
        }
        return flag;
    }

    /**
     * output the protein result
     *
     * @param infile_um
     * @param outFile
     * @param assayQI
     * @param inputDTCA
     * @param outputDTCA
     * @param quantLT
     * @param refAssay
     * @return
     */
    private boolean OutputMzqProteinNormalisation(MzQuantMLUnmarshaller infile_um, String outFile, String assayQI,
            String inputDTCA, String outputDTCA, String quantLT, String refAssay) {

        boolean flag = false;
        Map<String, List<String>> normalisedProAssayVal;
//        flag = ProteinAssayValue(infile_um, inputDTCA);
        flag = ProteinAssayValue(infile_um, assayQI);

        if (flag == true) {
            ProteinGroupList proGroList = infile_um.unmarshal(MzQuantMLElement.ProteinGroupList);
            List<QuantLayer> assayQLs = proGroList.getAssayQuantLayer();
            normalisedProAssayVal = NormalisedAssayValue(refAssay, proteinAssayValues);

            if (quantLT.equals("AssayQuantLayer")) {

//                List<QuantLayer> assayQLs = AssayQLs(infile_um);
                QuantLayer newQL = new QuantLayer();
                newQL.setId(assayQI);

                /**
                 * Create the part of DataType
                 */
                CvParam cvParam1 = new CvParam();
                cvParam1.setAccession(outputDTCA);
                Cv cv = new Cv();

                cv.setId(cvParamId);
                cvParam1.setCv(cv);
                cvParam1.setName(cvParamName);
                CvParamRef cvParamRef1 = new CvParamRef();
                cvParamRef1.setCvParam(cvParam1);
                newQL.setDataType(cvParamRef1);

                /**
                 * Create the part of ColumnIndex
                 */
                /**
                 * Get the column indices from the QuantLayer in the original
                 * file and then add these to the generated QuantLayer in
                 * ProteinGroup
                 */
                for (QuantLayer assayQL : assayQLs) {
                    if ((assayQL.getDataType().getCvParam().getAccession()).equalsIgnoreCase(inputDTCA)) {

                        List<String> assayCI = (List<String>) assayQL.getColumnIndex();
                        int nCI = assayCI.size();
                        for (int i = 0; i < nCI; i++) {
                            newQL.getColumnIndex().add(assayCI.get(i));
//                            System.out.println("assayCI: " + assayCI.get(i));
                        }

                        break;
                    }
                }

                /**
                 * Create the part of DataMatrix
                 */
                DataMatrix dm = new DataMatrix() {
                };

                /**
                 * make the records in order when outputing
                 */
                Map<String, List<String>> normalisedTmp = new HashMap<String, List<String>>();
                for (Map.Entry<String, List<String>> entry : normalisedProAssayVal.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
                    normalisedTmp.put(key, values);

                }

                Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(normalisedTmp);
                DataMatrix dMatrix = Utils.SortedMap(treeMap, dm);

                newQL.setDataMatrix(dMatrix);
                proGroList.getAssayQuantLayer().add(newQL);

                MzQuantML mzq = infile_um.unmarshal(MzQuantMLElement.MzQuantML);
                mzq.setProteinGroupList(proGroList);

                MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller(outFile);
                marshaller.marshall(mzq);
            }
        } else {
            System.out.println("Does not obtain Protein Group Assay Raw Values correctly!!!");
        }
        return flag;
    }

}
