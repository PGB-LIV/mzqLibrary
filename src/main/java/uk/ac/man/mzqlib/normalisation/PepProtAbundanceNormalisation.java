
package uk.ac.man.mzqlib.normalisation;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import uk.ac.liv.pgb.jmzqml.MzQuantMLElement;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AnalysisSummary;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Cv;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParamRef;
import uk.ac.liv.pgb.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.pgb.jmzqml.model.mzqml.IdOnly;
import uk.ac.liv.pgb.jmzqml.model.mzqml.MzQuantML;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Protein;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Row;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.man.mzqlib.normalisation.PepProtAbundanceNormalisation.ScaleFactorCalculationResult;

/**
 * This class is to normalise peptide or feature abundance.
 *
 * @author man-mqbsshz2
 * @version 0.2
 */
public class PepProtAbundanceNormalisation {

    private Map<String, List<String>> peptideAssayValues
            = new HashMap<>();
    private Map<String, List<String>> featureAssayValues
            = new HashMap<>();
    private Map<String, List<String>> assayVals
            = new HashMap<>();
    //private Map<String, List<String>> proteinAssayValues;

    final static double thresholdConfidence = 2;
    final static double coefficientMAD = 1.4826; //scale factor, 

    private int maximumThreads = 4;
    private final String in_file;
    private final String out_file;
    private String normalisedLevel;
    private String quantLayerType;
    private String inputDataTypeAccession;
    private String outputDataTypeAccession;
    private String tagDecoy;
//    private int assMin;
//    private int assMax;

    private final String cvParamId;
    private String outputDataTypeName;
    private String inputQuantLayerID;
    private String outputQuantLayerID;

//    private int referenceNumber;
    private final String userRef;
    //private String setType;
    //private Map<String, List<String>> scalingFactor;
    //private String preferedRef;

    private MzQuantMLUnmarshaller inFileUM;
    private int assNo;
    private boolean initted;
    private int pepSize;

    /**
     * give the maximum number of threads
     *
     * @param maximumThreads - Number of maximum threads
     */
    public void setMaximumThreads(final int maximumThreads) {
        this.maximumThreads = maximumThreads;
    }

    /**
     * give normalised level
     *
     * @param normLev - normalised level
     */
    public void setNormLevel(final String normLev) {
        normalisedLevel = normLev;
    }

    /**
     * set quant layer type
     *
     * @param qlt - quant layer type
     */
    public void setQuantLT(final String qlt) {
        quantLayerType = qlt;
    }

    /**
     * set input data type accession
     *
     * @param inDTA - data type accession for input
     */
    public void setInDTAcc(final String inDTA) {
        inputDataTypeAccession = inDTA;
    }

    /**
     * set output data type accession
     *
     * @param outDTA - data type accession for output
     */
    public void setOutDTAcc(final String outDTA) {
        outputDataTypeAccession = outDTA;
    }

    /**
     * set decoy tag
     *
     * @param td - decoy tag
     */
    public void setTagDecoy(final String td) {
        tagDecoy = td;
    }

    /**
     * set data type CV name for output
     *
     * @param outDTCN - output data type CV name
     */
    public void setOutputAssayDTCN(final String outDTCN) {
        outputDataTypeName = outDTCN;
    }

    /**
     * set ID type
     *
     * @param idType - ID type
     */
//    public void setIDType(String idType) {
//        this.setType = idType;
//    }
    /**
     * set unmarshalling file for mzQuantML object
     *
     * @param um - unmarshalling input file
     */
    public void setUMInfile(final MzQuantMLUnmarshaller um) {
        inFileUM = um;
    }

    /**
     * Constructor
     *
     * @param in_file                 - input mzq file
     * @param out_file                - output mzq file
     * @param normalisedLevel         - level for normalization, Ex. "peptide"
     * @param quantLayerType          - quant layer type, Ex. "AssayQuantLayer"
     * @param inputDataTypeAccession  - input accession in CV parameters, Ex.
     *                                "MS:1001893"
     * @param outputDataTypeAccession - output accession in CV parameters, Ex.
     *                                "MS:1001891"
     * @param outputDataTypeName      - output name in CV parameters
     * @param tagDecoy                - tag for decoy data
     * @param userRef                 - reference number that users prefer to
     *
     * @throws FileNotFoundException file not found exceptions.
     */
    public PepProtAbundanceNormalisation(final String in_file,
                                         final String out_file,
                                         final String normalisedLevel,
                                         final String quantLayerType,
                                         final String inputDataTypeAccession,
                                         final String outputDataTypeAccession,
                                         final String outputDataTypeName,
                                         final String tagDecoy,
                                         final String userRef)
            throws FileNotFoundException {

        this.in_file = in_file;
        this.out_file = out_file;
        this.normalisedLevel = normalisedLevel;
        this.inputDataTypeAccession = inputDataTypeAccession;
        this.outputDataTypeAccession = outputDataTypeAccession;
        this.outputDataTypeName = outputDataTypeName;
        this.quantLayerType = quantLayerType;
        this.tagDecoy = tagDecoy;
        this.userRef = userRef;
//        this.assMin = assMin;
//        this.assMax = assMax;

//        if (normalisedLevel.equalsIgnoreCase("peptide")) {
//            this.setType = "consensus";
//        } else if (normalisedLevel.equalsIgnoreCase("feature")) {
//            this.setType = "full";
//        }
//        outputDataTypeName = "Normalised " + normalisedLevel + " abundance";
        cvParamId = "PSI-MS";

//        normalisedPeptideAssayValues = new HashMap<String, List<String>>();
        //proteinAssayValues = new HashMap<>();
//        normalisedProteinAssayValues = new HashMap<String, List<String>>();
        String cvAccessionPrefix = "MS:";
        int cvAccssionLength = 10;
        int cvAccessionLastSevenNumMax = 1002437;

        int length1 = inputDataTypeAccession.length();
        int length2 = outputDataTypeAccession.length();
        String inputCvAccessionSuffix = inputDataTypeAccession.substring(3,
                                                                         inputDataTypeAccession.
                                                                         length()
                                                                         - 3);
        String outputCvAccessionSuffix = outputDataTypeAccession.substring(3,
                                                                           outputDataTypeAccession.
                                                                           length()
                                                                           - 3);
        String qlt1 = "AssayQuantLayer";
        String qlt2 = "RatioQuantLayer";
        String qlt3 = "StudyVariableQuantLayer";

        if (!(normalisedLevel.equalsIgnoreCase("peptide")) && !(normalisedLevel.
                equalsIgnoreCase("feature"))) {
            throw new IllegalArgumentException(
                    "Invalid Input Normalising Level Parameter!!! "
                    + normalisedLevel);
        }

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
                    + outputDataTypeAccession);
        }

        if (!(outputDataTypeAccession.substring(0, 3).equals(cvAccessionPrefix)
                && Integer.parseInt(outputCvAccessionSuffix) >= 0
                && Integer.parseInt(outputCvAccessionSuffix)
                <= cvAccessionLastSevenNumMax)) {
            throw new IllegalArgumentException(
                    "Wrong Output Protein Group CV Accession!!! "
                    + outputDataTypeAccession);
        }

        if (!(quantLayerType.equals(qlt1) || quantLayerType.equals(qlt2)
                || quantLayerType.equals(qlt3))) {
            throw new IllegalArgumentException("Invalid Quant Layer Type!!! "
                    + quantLayerType);
        }

    }

    /**
     * Initialisation for calculation
     */
    private void init() {

        boolean flag_labelFree = true;
        boolean flag_assVal = true;

        //scalingFactor = new HashMap<>();
        try {

            assNo = getAssNo();
            try {
                inFileUM = mzqFileInput();
            } catch (IllegalStateException ex) {
                System.out.println(
                        "*********************************************************");
                System.out.println(
                        "The mzq file is not found!!! Please check the input file.");
                System.err.println(ex);
                System.out.println(
                        "*********************************************************");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PepProtAbundanceNormalisation.class.getName()).log(
                    Level.SEVERE, null, ex);
        }

        //examine whether the data file comes from a label-free experiment.
        flag_labelFree = checkLFcvParam(inFileUM);

        if (flag_labelFree == false) {

            throw new IllegalStateException(
                    "The mzq file is not label-free!!! Please check the input file.");
        }

        inputQuantLayerID = getQLID();
        outputQuantLayerID = inputQuantLayerID + ".1";
        featureAssayValues = peptideAssayValue("full");
        peptideAssayValues = peptideAssayValue("consensus");

        if (featureAssayValues.isEmpty()) {
            flag_assVal = false;
        }

        if (peptideAssayValues.isEmpty()) {
            flag_assVal = false;
        }

        if (flag_assVal == false) {
            throw new IllegalStateException(
                    "The desired assay quant layer in the input file is not found!!!");
        }

        initted = true;
    }

    /**
     * multi-threading calculation
     *
     * @throws FileNotFoundException file not found exceptions.
     * @throws InterruptedException  interrupted exceptions.
     */
    public void multithreadingCalc()
            throws FileNotFoundException, InterruptedException {

//        Map<String, List<String>> assayVals = new HashMap<String, List<String>>();
        init();
        if (!initted) {
            throw new IllegalStateException("Initialisation is needed!");
        }

        if ("feature".equals(normalisedLevel)) {
            assayVals = featureAssayValues;
        } else if ("peptide".equals(normalisedLevel)) {
            assayVals = peptideAssayValues;
        }

        boolean flag = true;
        int ass_start = 1;
        int ass_end = assNo;
        Map<String, List<String>> normalisedPepAssayValTmp;
        Map<String, List<String>> normalisedFeatureAssayValTmp;
        List<String> sfv;

//        System.out.println(assayVals);
//        int pepSizeTmp = peptideAssayValues.entrySet().iterator().next().getValue().size();
        int pepSizeTmp = assayVals.entrySet().iterator().next().getValue().
                size();

//        multi-threading will start if no user reference is input.
        if (userRef == null) {
            Set<ScaleFactorCalculation> calculations = new HashSet<>();
            for (int ref = ass_start; ref <= ass_end; ref++) {
                calculations.add(new ScaleFactorCalculation(ref));
            }

            ExecutorService service = Executors.newFixedThreadPool(
                    maximumThreads);
            List<Future<ScaleFactorCalculationResult>> futures = null;
            try {
                futures = service.invokeAll(calculations);
            } catch (InterruptedException ex) {
//                Logger.getLogger(PepProtAbundanceNormalisation.class.getName()).
//                        log(Level.SEVERE, null, ex);
                throw ex;
            }

            Set<ScaleFactorCalculationResult> result = futures.stream().map(
                    mapResultFunction)
                    .collect(Collectors.toSet());
            if (result.stream().anyMatch(p -> p == null)) {
                throw new IllegalStateException(
                        "Error retrieving scaling factor calculations.");
            }

            int preferredReference = getPreferredReferenceFile(result);
            String[] scalingFactors = getScalingFactors(preferredReference,
                                                        assayVals);
            if (scalingFactors == null) {
                flag = false;
            }

//            System.out.println("Scaling factors: " + Arrays.toString(scalingFactors));
            Map<String, List<String>> normalisedValues = getNormalisationValues(
                    scalingFactors);

            //remove "null" entry
            normalisedValues.remove(null);

            outputMzqPeptideNormalisation(normalisedValues);

            service.shutdown();
        } else {
            normalisedPepAssayValTmp = normalisedAssayValue(userRef, assayVals);
            sfv = normalisedPepAssayValTmp.get("scalingfactor");

            if (sfv == null) {
                flag = false;
            } else {
                String[] sfValue = sfv.toArray(new String[pepSizeTmp]);

                //calculate the normalisation values of all features
                //Note: apply the scaling factors to all features although they are 
                //created depending on the choice of peptide or features.
                normalisedFeatureAssayValTmp = normalisedFeatureAssayValue(
                        sfValue);
                outputMzqPeptideNormalisation(normalisedFeatureAssayValTmp);
            }
        }

        System.out.println(
                "****************************************************");
        if (flag) {
            System.out.println(
                    "******** The pipeline does work successfully! *********");
            System.out.println(
                    "**** The normalisation result is output correctly! ****");
        } else {
            System.out.println(
                    "****** Some errors exist within the pipeline *******");
        }
        System.out.println(
                "****************************************************");
    }

    /**
     * get normalized values
     *
     * @param scalingFactors - scaling factors
     *
     * @return normalised assay values
     */
    public Map<String, List<String>> getNormalisationValues(
            final String[] scalingFactors) {
        Map<String, List<String>> normalisedFeatureAssayVal
                = normalisedFeatureAssayValue(scalingFactors);
        return normalisedFeatureAssayVal;
    }

    /**
     * get scaling factors
     *
     * @param referenceNumber - reference number
     *
     * @return scaling factors
     */
    private String[] getScalingFactors(final int referenceNumber,
                                       final Map<String, List<String>> AV) {
        Map<String, List<String>> normalisedPepAssayVal = normalisedAssayValue(
                String.valueOf(referenceNumber), AV);
        List<String> sf = normalisedPepAssayVal.get("scalingfactor");
        if (sf == null) {
            return null;
        }

        String[] sfValue = sf.toArray(new String[pepSize]);
        return sfValue;
    }

    /**
     * get preferred reference
     *
     * @param results - scaling factor calculation result
     *
     * @return reference number
     */
    private int getPreferredReferenceFile(
            final Set<ScaleFactorCalculationResult> results) {
        double[] std = new double[results.size()];

        for (ScaleFactorCalculationResult result : results) {
            List<String> sfvals = result.getScalingFactors();
            int ref = result.getReferenceNumber();

            double mean = sfvals.stream().mapToDouble(d -> Double.valueOf(d)).
                    average().getAsDouble();
            double sum_dev = sfvals.stream().mapToDouble(d -> Double.valueOf(d))
                    .map(d -> Math.pow(d - mean, 2)).sum();

            std[ref - 1] = Math.pow(sum_dev / (sfvals.size() - 1), 0.5);
        }

        double val_median = Utils.median(Arrays.copyOf(std, std.length));
        double tmp = Math.abs(std[0] - val_median);

        int refPreferred = 1;
        for (int i = 1; i < pepSize; i++) {
            if (Math.abs(std[i] - val_median) < tmp) {
                refPreferred = i + 1; //due to being the std array dimesion from zero
                tmp = Math.abs(std[i] - val_median);
            }
        }

        System.out.println("Preferred reference is " + refPreferred);

        return refPreferred;
    }

    /**
     * check control vocabulary parameters
     *
     * @param um - unmarshalled object
     *
     * @return true/false
     */
    private boolean checkLFcvParam(final MzQuantMLUnmarshaller um) {
        boolean flag_lf = false;
//        MzQuantML mzq = um.unmarshal(MzQuantMLElement.MzQuantML);
        AnalysisSummary analysisSummary = um.unmarshal(
                MzQuantMLElement.AnalysisSummary);
        List<CvParam> cvParas = analysisSummary.getCvParam();
        for (CvParam cvPara : cvParas) {
            String cvRef = cvPara.getAccession();

            if (cvRef.equalsIgnoreCase("MS:1001834")) {
                System.out.println(
                        "This is the label-free data. Normalisation processing continues ....");
                flag_lf = true;
                break;
            }
        }

        return flag_lf;
    }

    /**
     * obtain assay number
     *
     * @return assay number
     *
     * @throws FileNotFoundException
     */
    private int getAssNo()
            throws FileNotFoundException {
        int no = 0;
        try {

            MzQuantMLUnmarshaller inUm = mzqFileInput();
            PeptideConsensusList peptideConList = inUm.unmarshal(
                    MzQuantMLElement.PeptideConsensusList);
            List<QuantLayer<IdOnly>> assayQLs = peptideConList.
                    getAssayQuantLayer();

            for (QuantLayer assayQL : assayQLs) {
                if (assayQL.getDataType().getCvParam().getAccession().
                        equalsIgnoreCase(inputDataTypeAccession)) {
//                inputAssayQLID = assayQL.getId();
                    no = assayQL.getColumnIndex().size();
                    break;
                }
            }

        } catch (IllegalStateException ex) {
            Logger.getLogger(PepProtAbundanceNormalisation.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        return no;
    }

    /**
     * obtain the identifier of quant layer
     *
     * @return quant layer ID
     */
    private String getQLID() {
        PeptideConsensusList peptideConList = inFileUM.unmarshal(
                MzQuantMLElement.PeptideConsensusList);
        List<QuantLayer<IdOnly>> assayQLs = peptideConList.getAssayQuantLayer();
        String inAQLID = "";
        for (QuantLayer assayQL : assayQLs) {
            if (assayQL.getDataType().getCvParam().getAccession().
                    equalsIgnoreCase(inputDataTypeAccession)) {
                inAQLID = assayQL.getId();
//                assNo = assayQL.getColumnIndex().size();
                break;
            }
        }
        return inAQLID;
    }

    /**
     * unmarshal xml to java object
     *
     * @return Java object
     *
     * @throws IllegalStateException illegal state exceptions.
     * @throws FileNotFoundException file not found exceptions.
     */
    public MzQuantMLUnmarshaller mzqFileInput()
            throws IllegalStateException,
            FileNotFoundException {
        File mzqFile = new File(in_file);
        MzQuantMLUnmarshaller infileUm = new MzQuantMLUnmarshaller(mzqFile);
        return infileUm;
    }

    /**
     * obtain MzQuantML instance
     *
     * @param in_file_um - input file Java object
     *
     * @return mzq instance
     */
//    private MzQuantML mzq(MzQuantMLUnmarshaller in_file_um) {
//        MzQuantML mzq = in_file_um.unmarshal(MzQuantMLElement.MzQuantML);
//        return mzq;
//    }
    /**
     * obtain the peptide assay quant values
     *
     * @param st - giving the type of features (identified or nonidentifies)
     *
     * @return peptide assay values
     */
    private Map<String, List<String>> peptideAssayValue(final String st) {
//    private boolean PeptideAssayValue(MzQuantMLUnmarshaller in_file_um, String aql_id, String set_type) {
//        boolean first_list = false;

        Map<String, List<String>> assayValues
                = new HashMap<>();
        ProteinList protList = inFileUM.unmarshal(MzQuantMLElement.ProteinList);
        List<Protein> prots = protList.getProtein();

        PeptideConsensusList pepConList = inFileUM.unmarshal(
                MzQuantMLElement.PeptideConsensusList);
        List<PeptideConsensus> pepCons = pepConList.getPeptideConsensus();
        List<QuantLayer<IdOnly>> assayQLs = pepConList.getAssayQuantLayer();

        boolean foundQL = false;
        for (QuantLayer assayQL : assayQLs) {
            if (assayQL.getDataType().getCvParam().getAccession().
                    equalsIgnoreCase(inputDataTypeAccession)) {

                foundQL = true;
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
                    if (st.equalsIgnoreCase("full")) {

                        for (PeptideConsensus pepCon : pepCons) {

                            String pepId = pepCon.getId();
                            if (pepId.equalsIgnoreCase(peptideRef)) {
//                            System.out.println("Peptide ID: " + pepId);

//                                String pepSeq = pepCon.getPeptideSequence();
//                                if (StringUtils.isNotEmpty(pepSeq)) {
                                //remove the decoy-related peptides
                                for (Protein prot : prots) {
                                    boolean breakLoop = false;
                                    String protAcc = prot.getAccession();
                                    List<String> pepRefs = prot.
                                            getPeptideConsensusRefs();
                                    for (String pepRef : pepRefs) {
                                        if (pepRef.equalsIgnoreCase(pepId)
                                                && !(protAcc.contains(tagDecoy))) {
                                            //

                                            //original
//                                                peptideAssayValues.put(peptideRef, values);
                                            assayValues.put(peptideRef, values);
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

//                                }
                                break;
                            }
                        }
                        //for consensus ones only    
                    } else if (st.equalsIgnoreCase("consensus")) {

                        for (PeptideConsensus pepCon : pepCons) {

                            String pepId = pepCon.getId();
                            if (pepId.equalsIgnoreCase(peptideRef)) {
//                            System.out.println("Peptide ID: " + pepId);

                                String pepSeq = pepCon.getPeptideSequence();

                                if (StringUtils.isNotEmpty(pepSeq)) {

                                    //remove the decoy-related peptides
                                    for (Protein prot : prots) {
                                        boolean breakLoop = false;
                                        String protAcc = prot.getAccession();
                                        List<String> pepRefs = prot.
                                                getPeptideConsensusRefs();
                                        for (String pepRef : pepRefs) {
                                            if (pepRef.equalsIgnoreCase(pepId)
                                                    && !(protAcc.contains(
                                                            tagDecoy))) {
                                                //

                                                //original
//                                                peptideAssayValues.put(peptideRef, values);
                                                assayValues.put(peptideRef,
                                                                values);
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

                                }

                                break;
                            }
                        }
                    }

                }
                //use the first AQL encountered even if there are multiple AQLs with the same data type
//                first_list = true;
                break;
            }
        }

        if (!foundQL) {
            try {
                throw new IllegalStateException(
                        "The given quant layer is not found in the mzq file!!! "
                        + "Please check the input data type accession.");
            } catch (IllegalStateException ex) {
                Logger.getLogger(PepProtAbundanceNormalisation.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
//        System.out.println("Peptide Assay: " + peptideAssayValues);
//        return peptideAssayValues;
        return assayValues;
    }

    /**
     * obtain the protein assay values in the map of proteinAssayValues
     *
     * @param in_file_um       - input file Java object
     * @param inputProteinDTCA - protein CV accession
     *
     * @return true/false
     */
//    private boolean proteinAssayValue(MzQuantMLUnmarshaller in_file_um, String assayID) {
//
//        boolean first_list = false;
//        ProteinGroupList proGroupList = in_file_um.unmarshal(MzQuantMLElement.ProteinGroupList);
//        List<QuantLayer<IdOnly>> assayQLs = proGroupList.getAssayQuantLayer();
//        for (QuantLayer assayQL : assayQLs) {
////            System.out.println("Assay Quant Layer ID: " + assayQL.getId());
////            if ((assayQL.getDataType().getCvParam().getAccession()).equalsIgnoreCase(inputProteinDTCA)) {
//
//            if (assayQL.getId().equalsIgnoreCase(assayID)) {
//                DataMatrix assayDM = assayQL.getDataMatrix();
//                List<Row> rows = assayDM.getRow();
//                for (Row row : rows) {
//                    //get protein reference
//                    String proteinRef = row.getObjectRef();
//
//                    //get value String type
//                    List<String> values = row.getValue();
//
//                    proteinAssayValues.put(proteinRef, values);
//
//                }
////System.out.println("protein assay values: " + proteinAssayValues);
//                first_list = true;
//                break;
//            }
//        }
////        return proteinAssayValues;
//        return first_list;
//    }
    /**
     * calculate scaling factors based on the median absolute deviation (MAD)
     * algorithm.
     *
     * @param ref - reference number
     *
     * @return the map with the normalised values
     */
    private Map<String, List<String>> normalisedAssayValue(final String ref,
                                                           final Map<String, List<String>> PAV) {
        Map<String, List<String>> normalisedPAV
                = new HashMap<>();
        //Map<String, List<String>> ratioPAV = new HashMap<>();
//        Set<Entry<String, List<String>>> entrys = peptideAssayValues.entrySet();
        Set<Entry<String, List<String>>> entrys = PAV.entrySet();

        DecimalFormat df = new DecimalFormat(".000");
        double threshold_confidence = thresholdConfidence;
        double coef_mad = coefficientMAD;
//        int vSize = peptideAssayValues.entrySet().iterator().next().getValue().size();
        int vSize = PAV.entrySet().iterator().next().getValue().size();

//        int entryNo = peptideAssayValues.size();
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
        //String[] valArr_key = new String[entryNo];
        double[] refCol = new double[entryNo];
        int refNo = Integer.parseInt(ref) - 1;

        //adjust zeros and calculate the ratios
        for (Map.Entry<String, List<String>> entry : entrys) {
            int sig_ignore = 0;
            //List<String> ratioVals = new ArrayList<>();
            //String key = entry.getKey();
            String vRef = entry.getValue().get(refNo);

//            if (vRef.equalsIgnoreCase("null")) {
//                vRef = "0";
//            }
            if (vRef.equalsIgnoreCase("null")) {
                continue;
            }

            refCol[entryRow] = Double.parseDouble(vRef);
            for (int col = 0; col < vSize; col++) {
                String vj = entry.getValue().get(col);

//                if (vj.equalsIgnoreCase("null") || vj.equalsIgnoreCase("0")) {
////                    if (vj.equalsIgnoreCase("null")) {
//                    vj = "0.5";
//                }
                if (vj.equalsIgnoreCase("null") || vj.equalsIgnoreCase("0")) {
                    sig_ignore = 1;
                    break;
                }

//                double ratioVal = Double.parseDouble(vRef) / Double.parseDouble(
//                        vj);
                //ratioVals.add(col, Double.toString(ratioVal));
                valArr[entryRow][col] = Double.parseDouble(vj);
            }

            if (sig_ignore == 1) {
                continue;
            }

            //valArr_key[entryRow] = key;
            //ratioPAV.put(key, ratioVals);
            entryRow++;
        }

//calculate the scaling factor for each assay
        //int run = 0;
        for (int col = 0; col < vSize; col++) {
            int nonZero = 0;
            double[] objCol = new double[entryNo];
            double[] logRatio = new double[entryNo];
            for (int row = 0; row < entryNo; row++) {
                objCol[row] = valArr[row][col];

                if (Double.compare(refCol[row], 0.0) != 0 && Double.compare(
                        objCol[row], 0) != 0) {
                    logRatio[nonZero] = Math.log10(refCol[row] / objCol[row]);
//                    System.out.println("Log Ratio " + nonZero + " " + col + ": " + logRatio[nonZero]);
                    nonZero++;
                }

            }

            double[] logRatioTmp = new double[nonZero];
            double[] logRatioTmp1 = new double[nonZero];
            double[] logRatio_med = new double[nonZero];
            boolean check = true;
//            for (int k = 0; k < nonZero; k++) {
//                logRatioTmp[k] = logRatio[k];
//            }
            System.arraycopy(logRatio, 0, logRatioTmp, 0, logRatio.length);

            while (true) {
//                System.out.println("log ratio vector: " + Arrays.toString(logRatio));
                med = Utils.median(logRatioTmp);

                for (int i = 0; i < nonZero; i++) {
                    logRatio_med[i] = Math.abs(logRatioTmp[i] - med);
                }
                MAD = coef_mad * Utils.median(logRatio_med);
                upper = med + threshold_confidence * MAD;
                lower = med - threshold_confidence * MAD;

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

                logRatioTmp = (double[]) Utils.resizeArray(logRatioTmp,
                                                           nonZeroTmp);
                logRatio_med = (double[]) Utils.resizeArray(logRatio_med,
                                                            nonZeroTmp);
//                for (int i = 0; i < nonZeroTmp; i++) {
//                    logRatioTmp[i] = logRatioTmp1[i];
//                }
                System.arraycopy(logRatioTmp1, 0, logRatioTmp, 0,
                                 logRatioTmp1.length);

                logRatioTmp1 = (double[]) Utils.resizeArray(logRatioTmp1,
                                                            nonZeroTmp);
                nonZero = nonZeroTmp;
                if (check == true) {
                    scalingFactor[col] = Math.pow(10, Utils.mean(logRatioTmp));
                    //run++;
                    break;
                }
            }
            check = true;
        }

//normalising each entry
//        for (int row = 0; row < entryNo; row++) {
//            List<String> valArrRowList = new ArrayList<String>();
//            for (int col = 0; col < vSize; col++) {
//                double valArrTmp = valArr[row][col] * scalingFactor[col];
//                valArrRow[col] = df.format(valArrTmp);
//                valArrRowList.add(valArrRow[col]);
//            }
//            normalisedPAV.put(valArr_key[row], valArrRowList);
//        }
        //normalising the entries without the "null" value
        for (Map.Entry<String, List<String>> entry : entrys) {
            //int sig_ignore_null = 0;

            List<String> valArrRowList = new ArrayList<>();
            String key = entry.getKey();
            //List<String> values = entry.getValue();

            //remove entries with "null"
//            for (String val : values) {
//                if (val.equalsIgnoreCase("null")) {
//                    sig_ignore_null = 1;
//                }
//            }
//            //ignore the entry with the "null" element
//            if (sig_ignore_null == 1) {
//                continue;
//            }
            for (int col = 0; col < vSize; col++) {
                String value = entry.getValue().get(col);

                if (value.equalsIgnoreCase("null")) {
                    valArrRow[col] = "null";
                } else {
                    double valArrTmp = Double.parseDouble(value)
                            * scalingFactor[col];
                    valArrRow[col] = df.format(valArrTmp);
                }
                valArrRowList.add(valArrRow[col]);

            }

            normalisedPAV.put(key, valArrRowList);
        }

        //add scaleing factor
        String[] scale = new String[scalingFactor.length];
        for (int i = 0; i < scale.length; i++) {
            scale[i] = String.valueOf(scalingFactor[i]);
        }
        normalisedPAV.put("scalingfactor", Arrays.asList(scale));

        return normalisedPAV;
    }

    /**
     * normalising feature assay values using the scaling factors
     *
     * @param scalingFactor - scaling factors
     *
     * @return normalised feature assay value
     */
    private Map<String, List<String>> normalisedFeatureAssayValue(
            final String[] scalingFactor) {
        Map<String, List<String>> normalisedFAV
                = new HashMap<>();
        Set<Entry<String, List<String>>> featureEntrys = featureAssayValues.
                entrySet();

        DecimalFormat df = new DecimalFormat(".000");

        int vSize = featureAssayValues.entrySet().iterator().next().getValue().
                size();
        int entryNoFeature = featureAssayValues.size();
        int entryRow = 0;
//        double[][] valArrFeature = new double[entryNoFeature][vSize];
        String[][] valArrFeature = new String[entryNoFeature][vSize];
        String[] valArrRow = new String[vSize];
        String[] valArrFeature_key = new String[entryNoFeature];

        //adjust zeros and calculate the ratios
        for (Map.Entry<String, List<String>> entry : featureEntrys) {
//            List<String> ratioVals = new ArrayList<String>();
            String key = entry.getKey();
            int sig_ignore_feature_null = 0;

            for (int col = 0; col < vSize; col++) {
                String vj = entry.getValue().get(col);

//                if (vj.equalsIgnoreCase("null") || vj.equalsIgnoreCase("0")) {
////                    if (vj.equalsIgnoreCase("null")) {
//                    vj = "0.5";
//                }
                //ignore the entry with "null"
//                if (vj.equalsIgnoreCase("null") | vj.equalsIgnoreCase("nan")) {
//                    sig_ignore_feature_null = 1;
//                    break;
//                }
                //if the component is "null", it is set to zero
                if (vj.equalsIgnoreCase("null") || vj.equalsIgnoreCase("nan")) {
                    vj = "null";
                }

                valArrFeature[entryRow][col] = vj;
//                valArrFeature[entryRow][col] = Double.parseDouble(vj);
            }

            if (sig_ignore_feature_null == 1) {
                continue;
            }

            valArrFeature_key[entryRow] = key;
//            ratioPAV.put(key, ratioVals);
            entryRow++;
        }

        for (int row = 0; row < entryNoFeature; row++) {
            List<String> valArrRowList = new ArrayList<>();
            for (int col = 0; col < vSize; col++) {
                double scale = Double.valueOf(scalingFactor[col]);

                if (valArrFeature[row][col].equalsIgnoreCase("null")) {
                    valArrRow[col] = "null";
                } else {
                    double valArrTmp = Double.parseDouble(
                            valArrFeature[row][col]) * scale;
                    valArrRow[col] = df.format(valArrTmp);
                }
//                double valArrTmp = Double.parseDouble(valArrFeature[row][col]) * scale;
//                valArrRow[col] = df.format(valArrTmp);
                valArrRowList.add(valArrRow[col]);
            }
            normalisedFAV.put(valArrFeature_key[row], valArrRowList);
        }

        return normalisedFAV;
    }

    /**
     * output the normalised result
     *
     * @param normalisedPepAssayVal - normalised peptide assay values
     */
    private void outputMzqPeptideNormalisation(
            final Map<String, List<String>> normalisedPepAssayVal) {
        PeptideConsensusList pepConList = inFileUM.unmarshal(
                MzQuantMLElement.PeptideConsensusList);
        List<QuantLayer<IdOnly>> assayQLs = pepConList.getAssayQuantLayer();

        if (quantLayerType.equals("AssayQuantLayer")) {

            /**
             * Create the quant layer for raw peptide abundance
             */
//            QuantLayer newQL0 = new QuantLayer();
//            newQL0.setId(outputRawQuantLayerID);
//
//            /**
//             * Create the part of DataType
//             */
//            CvParam cvParam0 = new CvParam();
//            cvParam0.setAccession(outputRawDataTypeAccession);
//            Cv cv0 = new Cv();
//
//            cv0.setId(cvParamId);
//            cvParam0.setCv(cv0);
//
////                String cvParamNameRawPep = "Raw peptide abundance";
//            cvParam0.setName(outputRawDataTypeName);
//            CvParamRef cvParamRef0 = new CvParamRef();
//            cvParamRef0.setCvParam(cvParam0);
//            newQL0.setDataType(cvParamRef0);
            /**
             * Create the part of ColumnIndex
             */
            /**
             * Get the column indices from the QuantLayer in the original file
             * and then add these to the generated QuantLayer in ProteinGroup
             */
//            for (QuantLayer assayQL : assayQLs) {
//                if ((assayQL.getDataType().getCvParam().getAccession())
//                          .equalsIgnoreCase(inputDataTypeAccession)) {
//
//                    List<String> assayCI = (List<String>) assayQL.getColumnIndex();
//                    int nCI = assayCI.size();
//                    for (int i = 0; i < nCI; i++) {
//                        newQL0.getColumnIndex().add(assayCI.get(i));
////                            System.out.println("assayCI: " + assayCI.get(i));
//                    }
//
//                    break;
//                }
//            }
//
//            /**
//             * Create the part of DataMatrix
//             */
//            DataMatrix dm0 = new DataMatrix() {
//            };
//
//            /**
//             * make the records in order when outputing
//             */
//            Map<String, List<String>> rawTmp0 = new HashMap<String, List<String>>();
////                for (Map.Entry<String, List<String>> entry : pepAssVal.entrySet()) {
//            for (Map.Entry<String, List<String>> entry : peptideAssayValues.entrySet()) {
//                String key = entry.getKey();
//                List<String> values = entry.getValue();
////            String newKey = groupInOrd.get(key);
//                rawTmp0.put(key, values);
//
//            }
//
//            Map<String, List<String>> treeMap0 = new TreeMap<String, List<String>>(rawTmp0);
//            DataMatrix dMatrix0 = Utils.sortedMap(treeMap0, dm0);
//
//            newQL0.setDataMatrix(dMatrix0);
//            pepConList.getAssayQuantLayer().add(newQL0);
            /////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////
            //add the quant layer for normalized peptide abundance
            /**
             * Create a new quant layer
             */
//                List<QuantLayer> assayQLs = AssayQLs(infile_um);
            QuantLayer newQL = new QuantLayer();
            newQL.setId(outputQuantLayerID);

            /**
             * Create the part of DataType
             */
            CvParam cvParam1 = new CvParam();
            cvParam1.setAccession(outputDataTypeAccession);
            Cv cv = new Cv();

            cv.setId(cvParamId);
            cvParam1.setCv(cv);
            cvParam1.setName(outputDataTypeName);
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
                if (assayQL.getDataType().getCvParam().getAccession().
                        equalsIgnoreCase(inputDataTypeAccession)) {

                    List<String> assayCI = (List<String>) assayQL.
                            getColumnIndex();
                    int nCI = assayCI.size();
                    for (int i = 0; i < nCI; i++) {
                        newQL.getColumnIndex().add(assayCI.get(i));
                    }

                    break;
                }
            }

            /**
             * Create the part of DataMatrix
             */
            DataMatrix dm = new DataMatrix();

            // make the records in order when outputing
            Map<String, List<String>> treeMap
                    = new TreeMap<>();
            for (Map.Entry<String, List<String>> entry : normalisedPepAssayVal.
                    entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                treeMap.put(key, values);
            }

            DataMatrix dMatrix = Utils.sortedMap(treeMap, dm);
            newQL.setDataMatrix(dMatrix);
            pepConList.getAssayQuantLayer().add(newQL);

            MzQuantML mzq = inFileUM.unmarshal(MzQuantMLElement.MzQuantML);
            mzq.getPeptideConsensusList().clear();
            mzq.getPeptideConsensusList().add(pepConList);

            MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller(out_file);
            marshaller.marshall(mzq);
        }
    }

    /**
     * class for calculating scale factors
     */
    public class ScaleFactorCalculation implements
            Callable<ScaleFactorCalculationResult> {

        private final int referenceNumber;

        public ScaleFactorCalculation(final int referenceNumber) {
            this.referenceNumber = referenceNumber;
        }

        @Override
        public ScaleFactorCalculationResult call()
                throws Exception {

            Map<String, List<String>> scaleFactor = new HashMap<>();
            Map<String, List<String>> normalisedPepAssayVal;
            List<String> sf;

            if (quantLayerType.equalsIgnoreCase("AssayQuantLayer")
                    && (normalisedLevel.equalsIgnoreCase("peptide")
                    || normalisedLevel.equalsIgnoreCase("feature"))) {
//                    pepSize = peptideAssayValues.entrySet().iterator().next().getValue().size();
                pepSize = assayVals.entrySet().iterator().next().getValue().
                        size();

                if (!(referenceNumber >= 0) || !(referenceNumber <= pepSize)) {
                    throw new IllegalArgumentException(
                            "Wrongly select the reference number!!! "
                            + "It should be an integer in [1 " + pepSize
                            + "]");
                }

//                    normalisedPepAssayVal = normalisedAssayValue(Integer.toString(referenceNumber));
                normalisedPepAssayVal = normalisedAssayValue(Integer.
                        toString(referenceNumber), assayVals);
                sf = normalisedPepAssayVal.get("scalingfactor");
                scaleFactor.put("scalingfactor", sf);

                List<String> vals = scaleFactor.get("scalingfactor");

                return new ScaleFactorCalculationResult(referenceNumber,
                                                        vals);
            }
            return null;
        }

    }

    public static class ScaleFactorCalculationResult {

        private final int referenceNumber;
        private final List<String> scalingFactors;

        public ScaleFactorCalculationResult(final int referenceNumber,
                                            final List<String> scalingFactors) {
            this.referenceNumber = referenceNumber;
            this.scalingFactors = scalingFactors;
        }

        public int getReferenceNumber() {
            return this.referenceNumber;
        }

        public List<String> getScalingFactors() {
            return this.scalingFactors;
        }

    }

    private static final Function<Future<ScaleFactorCalculationResult>, ScaleFactorCalculationResult> mapResultFunction
            = new Function<Future<ScaleFactorCalculationResult>, ScaleFactorCalculationResult>() {

        @Override
        public ScaleFactorCalculationResult apply(
                final Future<ScaleFactorCalculationResult> t) {
            try {
                return t.get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(
                        PepProtAbundanceNormalisation.class.
                        getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

    };
}
