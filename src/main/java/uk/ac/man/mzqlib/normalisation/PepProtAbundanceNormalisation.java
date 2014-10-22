package uk.ac.man.mzqlib.normalisation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import static uk.ac.liv.jmzqml.MzQuantMLElement.PeptideConsensus;
import static uk.ac.liv.jmzqml.MzQuantMLElement.Protein;
import uk.ac.liv.jmzqml.model.mzqml.Cv;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.CvParamRef;
import uk.ac.liv.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.jmzqml.model.mzqml.IdOnly;
import uk.ac.liv.jmzqml.model.mzqml.MzQuantML;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
import uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList;
import uk.ac.liv.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.Row;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;
import static uk.ac.man.mzqlib.normalisation.Utils.Median;

/**
 * This class is to normalise the peptide and protein group abundance in terms
 * of users' need.
 *
 * @author man-mqbsshz2
 */
public class PepProtAbundanceNormalisation extends Thread {
//public class PepProtAbundanceNormalisation {

    private Map<String, List<String>> peptideAssayValues = new HashMap<String, List<String>>();
    private Map<String, List<String>> NormalisedPeptideAssayValues = new HashMap<String, List<String>>();

    private Map<String, List<String>> proteinAssayValues = new HashMap<String, List<String>>();
    private Map<String, List<String>> NormalisedProteinAssayValues = new HashMap<String, List<String>>();

    final static double thresholdConfidence = 2;
    final static double coefficientMAD = 1.4826; //scale factor, 

    private String in_file;
    private String out_file;

    private String inputPDTCA;
    private String cvParamAccession;
    private String cvParamId;
    private String cvParamName;
    private String outputAQLID;

    private String assayQIRawPep = "Pep_AQL.0";
    private String outputDTCARawPep = "MS:1001893";
    private String cvParamNameRawPep = "Progenesis:peptide raw abundance";
    private String normalisedLevel;
    private String quantLayerType;
    private String referenceNumber;
    private String setType;
    public static Map<String, List<String>> scalingFactor = new HashMap<String, List<String>>();
    private String preferedRef;
    private String regExDecoy;

    //EvsB, the assay number 9 is the reference. It is also used to other
    //such as EvsC or EvsD
//    PrintWriter outReference = new PrintWriter("referenceCPTAC.txt");
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
    public PepProtAbundanceNormalisation(String in_file, String out_file, String normalisedLevel,
            String outputCvName, String outputAssQLID, String inputDataTypeCvAcc,
            String outputCvAcc, String QLType, String refNo, String setType, String regExDecoy) throws FileNotFoundException {

        this.in_file = in_file;
        this.out_file = out_file;
        this.normalisedLevel = normalisedLevel;
        this.cvParamName = outputCvName;
        this.cvParamId = "PSI-MS";
        this.outputAQLID = outputAssQLID;
        this.inputPDTCA = inputDataTypeCvAcc;
        this.cvParamAccession = outputCvAcc;
        this.quantLayerType = QLType;
        this.referenceNumber = refNo;
        this.setType = setType;
        this.regExDecoy = regExDecoy;

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

//        IDSType = IDST;
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
        if (!(QLType.equals(qlt1) || QLType.equals(qlt2) || QLType.equals(qlt3))) {
            throw new IllegalArgumentException("Invalid Quant Layer Type!!! " + QLType);
        }
    }

    @Override
    public void run() {

        boolean file_flag = true;
        boolean pipeline_flag = true;

        MzQuantMLUnmarshaller infileum = null;

        try {
            infileum = MzqFileInput(this.in_file);
        } catch (IllegalStateException ex) {
            System.out.println("***************************************************************");
            System.out.println("The mzq file is not found!!! Please check the first input file.");
            System.err.println(ex);
            System.out.println("***************************************************************");
            file_flag = false;
            pipeline_flag = false;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PepProtAbundanceNormalisation.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (file_flag == true) {
            pipeline_flag = pipeline_executor(infileum, this.out_file, this.normalisedLevel,
                    this.outputAQLID, this.inputPDTCA, this.cvParamAccession, this.quantLayerType,
                    this.referenceNumber, this.setType, this.regExDecoy);
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
    private boolean pipeline_executor(MzQuantMLUnmarshaller infile_um, String outputFile,
            String normLevel, String outputAssQLID, String inDTCA, String outDTCA,
            String quantLayerType, String reference, String idSetType, String exDecoy) {

        boolean flag = true;
//        Map<String, List<String>> scaleFactor;

        Map<String, List<String>> scaleFactor = new HashMap<String, List<String>>();
        Map<String, List<String>> normalisedPepAssayVal;

        if (quantLayerType.equalsIgnoreCase("AssayQuantLayer")) {
            if (normLevel.equalsIgnoreCase("peptide")
                    || normLevel.equalsIgnoreCase("feature")
                    || normLevel.equalsIgnoreCase("feature-then-peptide")) {
                this.peptideAssayValues = PeptideAssayValue(infile_um, inDTCA, idSetType, exDecoy);
//            flag = PeptideAssayValue(infile_um, inputAssQLID, idSetType);
                if (this.peptideAssayValues == null) {
                    flag = false;
                }
//                        System.out.println("peptide assay entry set: " + flag);
                if (flag == false) {
                    System.out.println("****************************************************************************");
                    System.out.println("****** The desired assay quant layer in the input file is not found!!! *****");
                    System.out.println("************** Please check the input data type accession. *****************");
                    System.out.println("****************************************************************************");
                    return flag;
                }

                int pepSize = this.peptideAssayValues.entrySet().iterator().next().getValue().size();
//            System.out.println("peptide assay values No: " + pepSize);

                if (!(Integer.parseInt(reference) >= 0) || !(Integer.parseInt(reference) <= pepSize)) {
                    throw new IllegalArgumentException("Wrongly select the reference number!!! "
                            + "It should be an integer in [1 " + pepSize + "]");
                }

                normalisedPepAssayVal = NormalisedAssayValue(reference, peptideAssayValues);
                List<String> sf = normalisedPepAssayVal.get("scalingfactor");
                scaleFactor.put("scalingfactor", sf);
//            normalisedPepAssayVal.remove("scalingfactor");
//            OutputMzqPeptideNormalisation(infile_um, inputAssQLID, idSetType, outputFile, outputAssQLID,
//                    inDTCA, outDTCA, quantLayerType, normalisedPepAssayVal);

                if (!(scaleFactor == null)) {
                    flag = true;
                }
                List<String> vals = scaleFactor.get("scalingfactor");
                System.out.println("scale values: " + vals);
                System.out.println("Reference: " + reference);
                this.scalingFactor.put(reference, vals);

//            System.out.println("scalingFactor: " + this.scalingFactor);
                //all assays used are to calculate the proper one assay
//            double[] sfvals = new double[pepSize];
                /**
                 * here apply a median strategy to select the better assay
                 */
                if (this.scalingFactor.keySet().size() == pepSize) {
                    int refPrefered = 1;
                    List<String> sfvals;
                    double[] std = new double[pepSize];
                    double[] stdTmp = new double[pepSize]; //stdTmp used for avoiding the sorting issue
                    //keep the std array in the oridinal order

                    for (int i = 1; i <= pepSize; i++) {
                        double sum = 0;
                        double sum_dev = 0;
                        double mean = 0;
                        std[i - 1] = 0;

//                    System.out.println("values: " + this.scalingFactor);
                        sfvals = this.scalingFactor.get(Integer.toString(i));

//sfvals = scalingFactor.get("1");
//                    System.out.println("sfvalues: " + sfvals);
                        for (String sfval : sfvals) {
                            sum = sum + Double.valueOf(sfval);
                        }
                        mean = sum / pepSize;

                        for (String sfval : sfvals) {
                            sum_dev = sum_dev + Math.pow((Double.valueOf(sfval) - mean), 2);
                        }

                        std[i - 1] = Math.pow(sum_dev / (pepSize - 1), 0.5);

                        stdTmp[i - 1] = std[i - 1];
//                    System.out.println("std[" + i + "]: " + std[i-1]);

                    }

                    double val_median = Utils.Median(stdTmp);
//                int med = Utils.findMedian(std,0,std.length-1);
//                double val_median = std[med];

//                System.out.println("standard deviations: " + Arrays.toString(std));
//                 System.out.println("Tmp standard deviations: " + Arrays.toString(stdTmp));
//                System.out.println("median value: " + val_median);
                    double tmp = Math.abs(std[0] - val_median);
                    for (int i = 1; i < pepSize; i++) {
                        if (Math.abs(std[i] - val_median) < tmp) {
                            refPrefered = i + 1; //due to being the std array dimesion from zero
                            tmp = Math.abs(std[i] - val_median);
                        }

//                    System.out.println("standard deviations: " + std[i]);
                    }
                    this.preferedRef = String.valueOf(refPrefered);
                    System.out.println("Prefered reference assay: " + this.preferedRef);
//                System.out.println("Prefered reference: " + PepProtAbundanceNormalisation.preferedRef);

                    normalisedPepAssayVal = NormalisedAssayValue(this.preferedRef, peptideAssayValues);
                    normalisedPepAssayVal.remove("scalingfactor");

//                outputFile = outputFile + this.preferedRef + "_revDecoy.mzq";
                    OutputMzqPeptideNormalisation(infile_um, outputFile, outputAssQLID,
                            inDTCA, outDTCA, quantLayerType, normalisedPepAssayVal);

                }

//            System.out.println("scaling factor: " + scaleFactor);
            }

            if (normLevel.equalsIgnoreCase("protein")) {
//            flag = ProteinAssayValue(infile_um, inDTCA);

                flag = ProteinAssayValue(infile_um, outputAssQLID);
//            proteinAssayValues = ProteinAssayValue(infile_um, outputAssQLID);
//            if (proteinAssayValues == null) {
//                flag = false;
//            }
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

                flag = OutputMzqProteinNormalisation(infile_um, outputFile, outputAssQLID,
                        inDTCA, outDTCA, quantLayerType, reference);
            }

//        } else if (quantLayerType.equals("RatioQuantLayer")) {
//
//        } else if (quantLayerType.equals("StudyVariableQuantLayer")) {

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
    public static MzQuantMLUnmarshaller MzqFileInput(String infile) throws IllegalStateException,
            FileNotFoundException {
        File mzqFile = new File(infile);
        MzQuantMLUnmarshaller infileUm = new MzQuantMLUnmarshaller(mzqFile);
        return infileUm;
    }

    private MzQuantML Mzq(MzQuantMLUnmarshaller in_file_um) {
        MzQuantML mzq = in_file_um.unmarshal(MzQuantMLElement.MzQuantML);
        return mzq;
    }

    /**
     * obtain the peptide assay quant values for the map of peptideAssayValues
     * from the peptide quant layer in the input file.
     *
     * @param in_file_um
     * @param aql_id
     * @param setType
     * @return a boolean value
     */
    private Map<String, List<String>> PeptideAssayValue(MzQuantMLUnmarshaller in_file_um,
            String inputPeptideDTCA, String set_type, String reg_ex) {
//    private boolean PeptideAssayValue(MzQuantMLUnmarshaller in_file_um, String aql_id, String set_type) {
        boolean first_list = false;
        ProteinList protList = in_file_um.unmarshal(MzQuantMLElement.ProteinList);
        List<Protein> prots = protList.getProtein();

        PeptideConsensusList pepConList = in_file_um.unmarshal(MzQuantMLElement.PeptideConsensusList);
        List<PeptideConsensus> pepCons = pepConList.getPeptideConsensus();
        List<QuantLayer<IdOnly>> assayQLs = pepConList.getAssayQuantLayer();
        for (QuantLayer assayQL : assayQLs) {
            if ((assayQL.getDataType().getCvParam().getAccession()).equalsIgnoreCase(inputPeptideDTCA)) {

//            if (assayQL.getId().equalsIgnoreCase(aql_id)) {
//                System.out.println("AQL: " + assayQL.getId());
                DataMatrix assayDM = assayQL.getDataMatrix();
                List<Row> rows = assayDM.getRow();
                for (Row row : rows) {
                    //get peptide reference
                    String peptideRef = row.getObjectRef();
//System.out.println("peptide assay values: " + peptideRef);
                    //get value String type
                    List<String> values = row.getValue();

                    //for all IDs
                    if (set_type.equalsIgnoreCase("full")) {

                        this.peptideAssayValues.put(peptideRef, values);

                        //for consensus ones only    
                    } else if (set_type.equalsIgnoreCase("consensus")) {

                        for (PeptideConsensus pepCon : pepCons) {

                            String pepId = pepCon.getId();
                            if (pepId.equalsIgnoreCase(peptideRef)) {
//                            System.out.println("Peptide ID: " + pepId);

                                String pepSeq = pepCon.getPeptideSequence();

                                if (StringUtils.isNoneBlank(pepSeq)) {

                                    //remove the decoy-related peptides
                                    for (Protein prot : prots) {
                                        boolean breakLoop = false;
                                        String protAcc = prot.getAccession();
                                        List<String> pepRefs = prot.getPeptideConsensusRefs();
                                        for (String pepRef : pepRefs) {
                                            if (pepRef.equalsIgnoreCase(pepId) && !(protAcc.contains(reg_ex))) {
                                                //

                                                //original
                                                this.peptideAssayValues.put(peptideRef, values);
//                                    System.out.println("peptide assay values: " + peptideRef);
                                                //

                                                //remove the decoy-related peptides
                                                breakLoop = true;
                                                break;
                                            }
                                        }
                                        if (breakLoop) {
                                            break;
                                        }
                                    }
                                    //

                                    //original
                                }

                                break;
//                    System.out.println("Peptide ref.: " + row.getObjectRef());
//                System.out.println("Peptide raw abundance: " + values.toString());
                            }
                        }
                    }

                }
                //use the first AQL encountered even if there are multiple AQLs with the same data type
                first_list = true;
                break;
            }
        }

//        System.out.println("Peptide Assay: " + peptideAssayValues);
        return this.peptideAssayValues;
//        return first_list;
    }

    /**
     * obtain the protein assay values in the map of proteinAssayValues
     *
     * @param in_file_um
     * @param inputProteinDTCA
     * @return a boolean value
     */
//    private Map<String, List<String>> ProteinAssayValue(MzQuantMLUnmarshaller in_file_um, String assayID) {
    private boolean ProteinAssayValue(MzQuantMLUnmarshaller in_file_um, String assayID) {

        boolean first_list = false;
        ProteinGroupList proGroupList = in_file_um.unmarshal(MzQuantMLElement.ProteinGroupList);
        List<QuantLayer<IdOnly>> assayQLs = proGroupList.getAssayQuantLayer();
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
//        return proteinAssayValues;
        return first_list;
    }

    /**
     * carry out the calculation of normalisation based on the median absolute
     * deviation (MAD) algorithm.
     *
     * @param ref
     * @param PAV
     * @return the map with the normalised values
     */
    private Map<String, List<String>> NormalisedAssayValue(String ref, Map<String, List<String>> PAV) {

        Map<String, List<String>> normalisedPAV = new HashMap<String, List<String>>();
        Map<String, List<String>> ratioPAV = new HashMap<String, List<String>>();
        Set<Entry<String, List<String>>> entrys = PAV.entrySet();

//         System.out.println("PAV Entrys " + ": " + entrys);
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

//            outReference.println(vRef);
//            System.out.println("key " + entryRow + ": " + key);
//            System.out.println("Ref value " + entryRow + ": " + vRef);
//            if (vRef.equalsIgnoreCase("null") || vRef.equalsIgnoreCase("0")) {
            if (vRef.equalsIgnoreCase("null")) {
                vRef = "0";
            }

            refCol[entryRow] = Double.parseDouble(vRef);
            for (int col = 0; col < vSize; col++) {
                String vj = entry.getValue().get(col);

                if (vj.equalsIgnoreCase("null") || vj.equalsIgnoreCase("0")) {
//                    if (vj.equalsIgnoreCase("null")) {
                    vj = "0.5";
                }

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

//         outReference.close();
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
//            if (col == 3) {
//                System.out.println("col3: " + Arrays.toString(objCol));
//            }
//            if (col == 5) {
//                System.out.println("col5: " + Arrays.toString(objCol));
//            }

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

//        System.out.println("Scaling Factor: " + Arrays.toString(scalingFactor));
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

        //add scaleing factor
        String[] scale = new String[scalingFactor.length];
        for (int i = 0; i < scale.length; i++) {
            scale[i] = String.valueOf(scalingFactor[i]);
        }
        normalisedPAV.put("scalingfactor", Arrays.asList(scale));

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
    private void OutputMzqPeptideNormalisation(MzQuantMLUnmarshaller infile_um,
            String outFile, String outAQLID, String inputDTCA, String outputDTCA,
            String quantLT, Map<String, List<String>> normalisedPepAssayVal) {
//    private boolean OutputMzqPeptideNormalisation(MzQuantMLUnmarshaller infile_um, 
//            String inAssQLID, String idST, String outFile,
//            String outAQLID, String inputDTCA, String outputDTCA, String quantLT, String refAssay) {

//        System.out.println("peptide assay values: " + pepAssVal);
//        boolean flag = true;
//        Map<String, List<String>> scaleFactor;
//        scaleFactor = new HashMap<String, List<String>>();
//        Map<String, List<String>> normalisedPepAssayVal;
//        peptideAssayValues = PeptideAssayValue(infile_um, inAssQLID, idST);
//        if (peptideAssayValues == null) {
//            flag = false;
//        }
//        flag = PeptideAssayValue(infile_um, inAssQLID, idST);
//        if (flag == true) {
        PeptideConsensusList pepConList = infile_um.unmarshal(MzQuantMLElement.PeptideConsensusList);
        List<QuantLayer<IdOnly>> assayQLs = pepConList.getAssayQuantLayer();

//            normalisedPepAssayVal = NormalisedAssayValue(refAssay, pepAssVal);
//            List<String> sf = normalisedPepAssayVal.get("scalingfactor");
//            System.out.println("scaling factor included: " + normalisedPepAssayVal);
//            System.out.println("sf: " + sf);
//            scaleFactor.put("scalingfactor", sf);
//            normalisedPepAssayVal.remove("scalingfactor");
//            System.out.println("normalised peptide assay values: " + normalisedPepAssayVal);
//            normalisedPepAssayVal = NormalisedAssayValue(refAssay, peptideAssayValues);
        if (quantLT.equals("AssayQuantLayer")) {

            /**
             * Create the quant layer for raw peptide abundance
             */
            QuantLayer newQL0 = new QuantLayer();
            newQL0.setId(assayQIRawPep);

            /**
             * Create the part of DataType
             */
            CvParam cvParam0 = new CvParam();
            cvParam0.setAccession(this.outputDTCARawPep);
            Cv cv0 = new Cv();

            cv0.setId(this.cvParamId);
            cvParam0.setCv(cv0);

//                String cvParamNameRawPep = "Raw peptide abundance";
            cvParam0.setName(cvParamNameRawPep);
            CvParamRef cvParamRef0 = new CvParamRef();
            cvParamRef0.setCvParam(cvParam0);
            newQL0.setDataType(cvParamRef0);

            /**
             * Create the part of ColumnIndex
             */
            /**
             * Get the column indices from the QuantLayer in the original file
             * and then add these to the generated QuantLayer in ProteinGroup
             */
            for (QuantLayer assayQL : assayQLs) {
                if ((assayQL.getDataType().getCvParam().getAccession()).equalsIgnoreCase(inputDTCA)) {

                    List<String> assayCI = (List<String>) assayQL.getColumnIndex();
                    int nCI = assayCI.size();
                    for (int i = 0; i < nCI; i++) {
                        newQL0.getColumnIndex().add(assayCI.get(i));
//                            System.out.println("assayCI: " + assayCI.get(i));
                    }

                    break;
                }
            }

            /**
             * Create the part of DataMatrix
             */
            DataMatrix dm0 = new DataMatrix() {
            };

            /**
             * make the records in order when outputing
             */
            Map<String, List<String>> rawTmp0 = new HashMap<String, List<String>>();
//                for (Map.Entry<String, List<String>> entry : pepAssVal.entrySet()) {
            for (Map.Entry<String, List<String>> entry : peptideAssayValues.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
//            String newKey = groupInOrd.get(key);
                rawTmp0.put(key, values);

            }

            Map<String, List<String>> treeMap0 = new TreeMap<String, List<String>>(rawTmp0);
            DataMatrix dMatrix0 = Utils.SortedMap(treeMap0, dm0);

            newQL0.setDataMatrix(dMatrix0);
            pepConList.getAssayQuantLayer().add(newQL0);

            /////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////
            //add the quant layer for normalized peptide abundance
            /**
             * Create a new quant layer
             */
//                List<QuantLayer> assayQLs = AssayQLs(infile_um);
            QuantLayer newQL = new QuantLayer();
            newQL.setId(outAQLID);

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
             * Get the column indices from the QuantLayer in the original file
             * and then add these to the generated QuantLayer in ProteinGroup
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
//        } else {
//            System.out.println("Does not obtain Peptide Assay Raw Values correctly!!!");
//        }
//        return scaleFactor;
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
    private boolean OutputMzqProteinNormalisation(MzQuantMLUnmarshaller infile_um, String outFile,
            String assayQI, String inputDTCA, String outputDTCA, String quantLT, String refAssay) {

        boolean flag = true;
        Map<String, List<String>> normalisedProAssayVal;
        flag = ProteinAssayValue(infile_um, inputDTCA);
//        proteinAssayValues = ProteinAssayValue(infile_um, assayQI);
//        if (proteinAssayValues == null) {
//            flag = false;
//        }

        if (flag == true) {
            ProteinGroupList proGroList = infile_um.unmarshal(MzQuantMLElement.ProteinGroupList);
            List<QuantLayer<IdOnly>> assayQLs = proGroList.getAssayQuantLayer();
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
