package uk.ac.man.mzqlib.postprocessing;

import java.io.File;
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

public class ProteinAbundanceInference {

    private static Map<String, HashSet<String>> proteinToPeptide = new HashMap<String, HashSet<String>>();
    private static Map<String, HashSet<String>> peptideToProtein = new HashMap<String, HashSet<String>>();
    private static final Map<String, List<String>> peptideAssayValues = new HashMap<String, List<String>>();

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

    /*
     * other parameters
     */
    final static String proteinGroupList = "ProteinGroupList1";
//        final static String proteinGroupId = "prot_3";
    final static String searchDatabase = "SD1";
//    final static String assayQuantLayerId = "PGL_AQL3";
    final static String cvParamAssession = "MS:1001893";
    final static String cvParamId = "PSI-MS";
    final static String cvRef = "PSI-MS";

    final static String path = "./src/main/resources/";

//    final static String techName = "iTraq";
//    final static String cvParamName = techName + ": Protein Grouping Abundance (" + abunOperation + " operation)";
//        String in_file = "./CPTAC-study6_EvsB.mzq";
//        String out_file = "./CPTAC-study6_EvsB_proteinAbundance_sorted_v1.mzq";
//        String in_file = "test_itraq_revised.mzq"; 
//        String out_file = "test_itraq_proteinAbundance_temp.mzq";
//        final String in_file = "CPTAC-Progenesis-small-example.mzq";
//        final String out_file = "CPTAC-Progenesis-small-example_proteinAbundance1.mzq";
//    final static String in_file = "./src/main/resources/test_ksl_itraq_result.mzq";
//    final static String out_file = "./src/main/resources/test_ksl_itraq_result_proteinAbundance.mzq";
    //        final String in_file = "../itraq/ksl_itraq_result.mzq";
//        final String out_file = "../itraq/ksl_itraq_result_proteinAbundance.mzq";
//  final static String in_file = "test_grouping_data.mzq";
//  final static String out_file = "test_grouping_data_proteinAbundance_" + abunOperation + "new.mzq";
//    final static String in_file = "test_grouping_data_update.mzq";
//    final static String out_file = "test_grouping_data_update_proteinAbundance_" + abunOperation + "2.mzq";
//  final static String in_file = "ProteoSuite_xTracker_fourProteins_result.mzq";
//  final static String out_file = "ProteoSuite_xTracker_fourProteins_result_proteinAbundance.mzq";
    static String techName;
    static String assayQuantLayerId;
    static String cvParamName;
    static String abunOperation;
    static String in_file;
    static String out_file;

    ProteinAbundanceInference(String input, String output, String expMethod, String calOperation) {

        techName = expMethod;
        cvParamName = techName + ": Protein Grouping Abundance (" + abunOperation + " operation)";
        abunOperation = calOperation;
        in_file = input;
        out_file = output;
    }

    public static void main(String[] args) throws JAXBException, InstantiationException, IllegalAccessException {

        String infile = path + "test_grouping_data_update.mzq";
        String outfile = path + "test_grouping_data_update_PA2.mzq";
        String method = "iTraq";
        String operator = "sum";

//        ProteinAbundanceInference pai = new ProteinAbundanceInference("./src/main/resources/test_ksl_itraq_result.mzq",
//            "./src/main/resources/test_ksl_itraq_result_proteinAbundance.mzq", "iTraq");
        if (args.length != 4 & args.length != 0) {
            System.out.println("Please input all four parameters in order: input file with path, "
                    + "output file with path, method and operator.");
        } else if (args.length == 4) {
            infile = args[0];
            outfile = args[1];
            method = args[2];
            operator = args[3];
        }
//        ProteinAbundanceInference pai = new ProteinAbundanceInference(path + "test_grouping_data_update.mzq",
//                    path + "test_grouping_data_update_PA1.mzq", "iTraq", "sum");
        ProteinAbundanceInference pai = new ProteinAbundanceInference(infile, outfile, method, operator);

        Map<String, HashSet<String>> proteinToPeptide = pai.ProteinToPeptide(in_file);
        Map<String, String> proteinToAccession = pai.ProteinToAccession(in_file);
        Map<String, HashSet<String>> peptideToProtein = pai.PeptideToProtein(proteinToPeptide);

        Map<String, List<String>> peptideAssayValues = pai.PeptideAssayValues(in_file);

        List<QuantLayer> assayQLs = pai.AssayQLs(in_file);
        String assayQuantLayerId_pep = AssayQuantLayerId(in_file);
        assayQuantLayerId = "PGL_" + assayQuantLayerId_pep;
        MzQuantML mzq = pai.Mzq(in_file);

//        System.out.println("Protein to Peptide: " + proteinToPeptide);
//        System.out.println();
//        System.out.println("Protein to Peptide + Protein No.: " + proteinToPeptide.size());
//        System.out.println();
//        System.out.println("Peptide to Protein: " + peptideToProtein);
//        System.out.println();
//        System.out.println("Peptide to Protein + Peptide No.: " + peptideToProtein.size());
//        System.out.println();
//
//        System.out.println("Peptide Assay Values: " + peptideAssayValues);
//        System.out.println();
//
//        HashSet<String> us = ProteinGrouping.GetUniquePeptides(peptideToProtein);
//        System.out.println("Unique Peps: " + us);
//        System.out.println();
        uniSetGroup = ProteinGrouping.UniSetGrouping(peptideToProtein, proteinToPeptide);
        sameSetGroup = ProteinGrouping.SameSetGrouping(peptideToProtein, proteinToPeptide);
        subSetGroup = ProteinGrouping.SubSetGrouping(peptideToProtein, proteinToPeptide);

//        System.out.println("Unique Group: " + uniSetGroup);
//        System.out.println();
//        System.out.println("Unique Peptides No.: " + uniSetGroup.size());
//        System.out.println();
//        System.out.println("SameSet Group: " + sameSetGroup);
//        System.out.println();
//        System.out.println("SameSet Group Number: " + sameSetGroup.size());
//        System.out.println();
//        System.out.println("SubSet Group: " + subSetGroup);
//        System.out.println();
//        System.out.println("SubSet Group Number: " + subSetGroup.size());
        pai.MzqOutput(mzq, assayQLs, abunOperation, uniSetGroup, sameSetGroup, subSetGroup, assayQuantLayerId);
    }

    public static Map<String, List<String>> PeptideAssayValues(String in_file) {
        File mzqFile = new File(in_file);
        MzQuantMLUnmarshaller um = new MzQuantMLUnmarshaller(mzqFile);
//        MzQuantML mzq = um.unmarshal(MzQuantMLElement.MzQuantML);

        PeptideConsensusList pepConList = um.unmarshal(MzQuantMLElement.PeptideConsensusList);
//        List<PeptideConsensus> peptides = pepConList.getPeptideConsensus();

        List<QuantLayer> assayQLs = pepConList.getAssayQuantLayer();
        QuantLayer assayQL1 = assayQLs.get(1);
        assayQL1.getId();
        for (QuantLayer assayQL : assayQLs) {
            DataMatrix assayDM = assayQL.getDataMatrix();
            List<Row> rows = assayDM.getRow();
            for (Row row : rows) {
                //get peptide reference
                String peptide = row.getObjectRef();
                //get value String type
                List<String> values = row.getValue();

                peptideAssayValues.put(peptide, values);

//                System.out.println("Peptide ref.: " + row.getObjectRef());
//                System.out.println("Peptide raw abundance: " + values.toString());
            }
        }
        return peptideAssayValues;
    }

    public static Map<String, HashSet<String>> ProteinToPeptide(String in_file) {
        File mzqFile = new File(in_file);
        MzQuantMLUnmarshaller um = new MzQuantMLUnmarshaller(mzqFile);

        ProteinList protList = um.unmarshal(MzQuantMLElement.ProteinList);
        List<Protein> proteins = protList.getProtein();
        for (Protein protein : proteins) {
            List<String> pepConRefs = protein.getPeptideConsensusRefs();

            /* 
             * generate the protein-to-peptide map
             */
            HashSet<String> setOfPeptides = new HashSet<String>();
            for (String pepCon : pepConRefs) {
                setOfPeptides.add(pepCon);

                /*
                 * Accession or ID for protein
                 */
//                proteinToPeptide.put(protein.getAccession(), setOfPeptides);
                proteinToPeptide.put(protein.getId(), setOfPeptides);
//                proteinToAccession.put(protein.getId(), protein.getAccession());
            }
        }

        return proteinToPeptide;
    }

    public static Map<String, String> ProteinToAccession(String in_file1) {

        File mzqFile = new File(in_file1);
        MzQuantMLUnmarshaller um = new MzQuantMLUnmarshaller(mzqFile);

        ProteinList protList = um.unmarshal(MzQuantMLElement.ProteinList);
        List<Protein> proteins = protList.getProtein();
        for (Protein protein : proteins) {
            List<String> pepConRefs = protein.getPeptideConsensusRefs();

            /* 
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

    public static Map<String, HashSet<String>> PeptideToProtein(Map<String, HashSet<String>> protToPep) {

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

    public static List<QuantLayer> AssayQLs(String in_file1) {
        File mzqFile = new File(in_file1);
        MzQuantMLUnmarshaller um = new MzQuantMLUnmarshaller(mzqFile);
        PeptideConsensusList pepConList = um.unmarshal(MzQuantMLElement.PeptideConsensusList);
        List<QuantLayer> assayQLs = pepConList.getAssayQuantLayer();
        return assayQLs;
    }

    public static MzQuantML Mzq(String in_file1) {
        File mzqFile = new File(in_file1);
        MzQuantMLUnmarshaller um = new MzQuantMLUnmarshaller(mzqFile);
        MzQuantML mzq = um.unmarshal(MzQuantMLElement.MzQuantML);
        return mzq;
    }

    public static String AssayQuantLayerId(String in_file1) {
        String assayQLID;
        File mzqFile = new File(in_file1);
        MzQuantMLUnmarshaller um = new MzQuantMLUnmarshaller(mzqFile);
//        MzQuantML mzq = um.unmarshal(MzQuantMLElement.MzQuantML);
        PeptideConsensusList pepConList = um.unmarshal(MzQuantMLElement.PeptideConsensusList);

        List<QuantLayer> assayQLs = pepConList.getAssayQuantLayer();
        QuantLayer assayQL1 = assayQLs.get(1);
        assayQLID = assayQL1.getId();

        return assayQLID;

    }

    public static void MzqOutput(MzQuantML mzq, List<QuantLayer> assayQLs, String operation,
            Map<String, HashSet<String>> uniSetGr, Map<String, HashSet<String>> sameSetGr,
            Map<String, HashSet<String>> subSetGr, String assayQlId) {
        try {
            Map<String, List<String>> proteinAbundance = ProteinAbundanceCalculation(operation,
                    uniSetGr, sameSetGr, subSetGr);
            Map<String, String> groupInOrder = ProteinGrouping.GroupInOrder(proteinAbundance);
            //        uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList protGroupList = 
//        uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList.class.newInstance();
            ProteinGroupList protGroupList;
            protGroupList = ProteinGroupList.class.newInstance();
            protGroupList.setId(proteinGroupList);
            ProteinGroups(protGroupList, groupInOrder);

            List<QuantLayer> assayQuantLayers = protGroupList.getAssayQuantLayer();
            QuantLayer assayQuantLayer = new QuantLayer();
//            assayQuantLayer.setId(assayQuantLayerId);
            assayQuantLayer.setId(assayQlId);

            /*
             * Create the part of DataType
             */
            CvParam cvParam1 = new CvParam();
            cvParam1.setAccession(cvParamAssession);
            Cv cv = new Cv();
            cv.setId(cvParamId);
            cvParam1.setCv(cv);
            cvParam1.setName(cvParamName);
            CvParamRef cvParamRef1 = new CvParamRef();
            cvParamRef1.setCvParam(cvParam1);
            assayQuantLayer.setDataType(cvParamRef1);

            /*
             * Create the part of ColumnIndex
             */
            /*
             * Get the column indices from the QuantLayer in the original file
             * and then add these to the generated QuantLayer in ProteinGroup
             */
            QuantLayer assayQL = assayQLs.get(0);
            List<String> assayCI = (List<String>) assayQL.getColumnIndex();
            int nCI = assayCI.size();
            for (int i = 0; i < nCI; i++) {
                assayQuantLayer.getColumnIndex().add(assayCI.get(i));
            }

            /*
             * Create the part of DataMatrix
             */
            DataMatrix dm = new DataMatrix() {
            };

            /*
             make the records in order when outputing
             */
            Map<String, List<String>> proteinAbundanceTmp = new HashMap<String, List<String>>();
            for (Map.Entry<String, List<String>> entry : proteinAbundance.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                String newKey = groupInOrder.get(key);
                proteinAbundanceTmp.put(newKey, values);

            }

            /*
             * Alternatively, use tree map to sort the records in DataMatrix for output
             */
//        Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(pa);
//        DataMatrix dMatrix = SortedMap(treeMap, dm, groupInOrder);
            Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(proteinAbundanceTmp);
            DataMatrix dMatrix = Utils.SortedMap(treeMap, dm);
            /*
             * the data is input in the created QuantLayer
             */
            assayQuantLayer.setDataMatrix(dMatrix);
            assayQuantLayers.add(assayQuantLayer);

            /*
             * Create the ProteinGroupList in mzq
             */
            mzq.setProteinGroupList(protGroupList);

            /*
             * Marshall the created object to MzQuantML
             * The output of MzQuantML format file
             */
//  MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller("CPTAC-Progenesis-small-example_proteinAbundance.mzq");
//  MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller("test_grouping_data_proteinAbundance.mzq");
//  MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller("ProteoSuite_xTracker_fourProteins_result_proteinAbundance.mzq");
            MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller(out_file);

            marshaller.marshall(mzq);

//        System.out.println("Data Type/Accession: " + assayQuantLayer.getDataType().getCvParam().getAccession());
//        System.out.println("Data Type/CVRef: " + assayQuantLayer.getDataType().getCvParam().getCvRef());
//        System.out.println("Data Type/Name: " + assayQuantLayer.getDataType().getCvParam().getName());
        } catch (InstantiationException ex) {
            Logger.getLogger(ProteinAbundanceInference.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ProteinAbundanceInference.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /*
     * Calculation of protein abundance
     */
    public static Map<String, List<String>> ProteinAbundanceCalculation(String operation,
            Map<String, HashSet<String>> uniSetGr, Map<String, HashSet<String>> sameSetGr,
            Map<String, HashSet<String>> subSetGr) {
        Map<String, List<String>> proteinAbundance = new HashMap<String, List<String>>();
//        Map<String, HashSet<String>> uniSetGr = UniSetGrouping();
//        Map<String, HashSet<String>> sameSetGr = SameSetGrouping();
//        Map<String, HashSet<String>> subSetGr = SubSetGrouping();

//        int groupId = 0;
        int groupLeader = 0;
        DecimalFormat df = new DecimalFormat(".000");
        /*
         unique set case
         */
        for (Map.Entry<String, HashSet<String>> entry : uniSetGr.entrySet()) {
//            groupId++;
            /*
             * get the dimension of peptide assay values
             */
            String pepSelect = entry.getValue().iterator().next().toString();
            List<String> assayValTmp = peptideAssayValues.get(pepSelect);
            //number of assay values in each peptide
            int assaySize = assayValTmp.size();
            //number of peptides correponding to the protein
            int numPeptides = entry.getValue().size();

            /*
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
//            operationResult[assaySize + 1] = 0;
//            operationPepValue[assaySize] = 0;
//            operationPepValue[assaySize + 1] = 0;

//            System.out.println("Sum of peptide element values: " + Arrays.toString(sumOfpepValues));
//            System.out.println("Protein ID: " + entry.getKey());
            for (String peptide : entry.getValue()) {
                List<String> assayValues = peptideAssayValues.get(peptide);

                for (int j = 0; j < assayValues.size(); j++) {
                    String componentValue = assayValues.get(j);

                    /*
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

            /*
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
            proteinAbundance.put(entry.getKey(), proteinAbundanceList);
//            proteinAbundance.put("ProteinGroup" + groupId, proteinAbundanceList);
        }

        /*
         sameSet case
         */
        for (Map.Entry<String, HashSet<String>> entry : sameSetGr.entrySet()) {

//            groupId++;
            String pepSelect = entry.getValue().iterator().next().toString();
//            HashSet<String> proteins = peptideToProtein.get(pepSelect);
            List<String> assayValTmp = peptideAssayValues.get(pepSelect);
            //number of assay values in each peptide
            int assaySize = assayValTmp.size();
            //number of peptides correponding to the protein
            int numPeptides = entry.getValue().size();

            /*
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
//            operationResult[assaySize + 1] = 0;

            for (String peptide : entry.getValue()) {
                List<String> assayValues = peptideAssayValues.get(peptide);
//                System.out.println("assay value: " + assayValues);
                for (int j = 0; j < assayValues.size(); j++) {
                    String componentValue = assayValues.get(j);

                    /*
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
            /*
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
            proteinAbundance.put(entry.getKey(), proteinAbundanceList);
//proteinAbundance.put("ProteinGroup" + groupId, proteinAbundanceList);
        }

        /*
         subSet case
         */
        for (Map.Entry<String, HashSet<String>> entry : subSetGr.entrySet()) {
//            groupId++;
            String pepSelect = entry.getValue().iterator().next().toString();
//            HashSet<String> proteins = peptideToProtein.get(pepSelect);
            List<String> assayValTmp = peptideAssayValues.get(pepSelect);
            //number of assay values in each peptide
            int assaySize = assayValTmp.size();
            //number of peptides correponding to the protein
            int numPeptides = entry.getValue().size();

            /*
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
//            operationResult[assaySize + 1] = 0;

            for (String peptide : entry.getValue()) {
                List<String> assayValues = peptideAssayValues.get(peptide);
                for (int j = 0; j < assayValues.size(); j++) {
                    String componentValue = assayValues.get(j);

                    /*
                     * set NaN/Null to zero
                     */
                    double temp = (componentValue.equals("NaN") | componentValue.equals("nan")
                            | componentValue.equals("Null") | componentValue.equals("null"))
                            ? Double.parseDouble("0") : Double.parseDouble(componentValue);
                    matrixPepValue[tempNo][j] = temp;
                }
                tempNo++;
            }

            /*
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
            proteinAbundance.put(entry.getKey(), proteinAbundanceList);
//            proteinAbundance.put("ProteinGroup" + groupId, proteinAbundanceList);
        }

//        System.out.println("Protein abundance:" + proteinAbundance);
        return proteinAbundance;
    }

    public static List<ProteinGroup> ProteinGroups(ProteinGroupList protGL, Map<String, String> groupIO) {
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

//                    idenRefs.set(i, idenRef);
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

                        //anchor protein: protein with most peptides
                        int pepNo = 0;
                        for (String protId : protIds) {
                            int pepNoTmp = proteinToPeptide.get(protId).size();
                            if (pepNoTmp > pepNo) {
                                pepNo = pepNoTmp;
                                anchorProtein = protId;
                            }
                        }

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
        return protGrs;
    }
}
