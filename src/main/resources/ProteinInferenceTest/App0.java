package com.mycompany.proteininferencetest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.bind.JAXBException;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.*;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList;

public class App {

    private static final Map<String, HashSet<String>> proteinToPeptide = new HashMap<String, HashSet<String>>();
    private static final Map<String, HashSet<String>> peptideToProtein = new HashMap<String, HashSet<String>>();
    private static final Map<String, List<String>> peptideAssayValues = new HashMap<String, List<String>>();

//    public static class IdOnlyX extends IdOnly {
//
//        @Override
//        public void setId(String value) {
//            id = value;
//        }
//    }
    public static void main(String[] args) throws JAXBException, InstantiationException, IllegalAccessException {

        /*
         * the parameters in the generated ProteinGroupList
         */
        String proteinGroupId = "prot_3";
        String assayQuantLayerId = "PGL_AQL3";
        String cvParamAssession = "MS:1001893";
        String cvParamId = "PSI-MS";
        String cvParamName = "Progenesis:protein raw abundance (add operation)";

//        String in_file = args[0];
//        String out_file = args[1];
        String in_file = "./CPTAC-study6_EvsB.mzq";
        String out_file = "./CPTAC-study6_EvsB_proteinAbundance_sorted.mzq";
//        String in_file = "test_itraq_revised.mzq"; 
//        String out_file = "test_itraq_proteinAbundance_temp.mzq";
//        String in_file = "CPTAC-Progenesis-small-example.mzq";
//        String out_file = "CPTAC-Progenesis-small-example_proteinAbundance.mzq";
//        String in_file = "test_grouping_data.mzq";
//        String out_file = "test_grouping_data_proteinAbundance.mzq";
//        String in_file = "ProteoSuite_xTracker_fourProteins_result.mzq";
//        String out_file = "ProteoSuite_xTracker_fourProteins_result_proteinAbundance.mzq";

        File mzqFile = new File(in_file);

        //create an mzQuantMLUnmarshaller 
        MzQuantMLUnmarshaller um = new MzQuantMLUnmarshaller(mzqFile);

        /*
         * MzQuantML file
         */
        MzQuantML mzq = um.unmarshal(MzQuantMLElement.MzQuantML);


        /*
         * PeptideConsensusList
         */
        PeptideConsensusList pepConList = um.unmarshal(MzQuantMLElement.PeptideConsensusList);

        List<PeptideConsensus> peptides = pepConList.getPeptideConsensus();
        /*
         * save the values of peptide assay values in a array list 
         * note that here is the raw data involved
         */
//        ArrayList<String> peptideValues = new ArrayList<String>();
//        for (PeptideConsensus peptide : peptides) {
//            String peptideRef = peptide.getId();
//            System.out.println("Peptide Reference: " + peptideRef);
//        }
        List<QuantLayer> assayQLs = pepConList.getAssayQuantLayer();

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
//        System.out.println("Peptide Assay Values: " + peptideAssayValues);

//      Iterator<PeptideConsensusList> pepConListIter = 
//        um.unmarshalHashSetFromXpath(MzQuantMLElement.PeptideConsensusList);
//        while (pepConListIter.hasNext()){
//            PeptideConsensusList pepConList = pepConListIter.next();
//            
//        }
        /*
         * ProteinList
         */
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
            }
//            System.out.println("Protein Accession: " + protein.getId());
//             System.out.println("Peptide Set: " + setOfPeptides);
        }

        /*
         * reverse proteinToPeptide for the peptide-to-protein map
         * 
         */
        for (Map.Entry<String, HashSet<String>> entry : proteinToPeptide.entrySet()) {
            for (String value : entry.getValue()) {
                if (!peptideToProtein.containsKey(value)) {
                    peptideToProtein.put(value, new HashSet<String>());
                }
                peptideToProtein.get(value).add(entry.getKey());
            }
        }

        System.out.println("protein to peptide: " + proteinToPeptide);
        System.out.println();
        System.out.println("protein to peptide + protein No.: " + proteinToPeptide.size());
        System.out.println();
        System.out.println("peptide to protein: " + peptideToProtein);
        System.out.println();
        System.out.println("peptide to protein + peptide No.: " + peptideToProtein.size());
        System.out.println();

        HashSet<String> uniquePeptides = GetUniquePeptides();
        HashSet<String> sameSetPeptides = GetSameSetPeptides();
        HashSet<String> subsetPeptides = GetSubsetPeptides();

        System.out.println("Unique Peptides: " + uniquePeptides);
        System.out.println();
        System.out.println("Unique Peptides No.: " + uniquePeptides.size());
        System.out.println();
        System.out.println("Same Set Peptides: " + sameSetPeptides);
        System.out.println();
        System.out.println("Same Set Peptides No.: " + sameSetPeptides.size());
        System.out.println();
        System.out.println("Subset Peptides: " + subsetPeptides);
        System.out.println();
        System.out.println("Subset Peptides No.: " + subsetPeptides.size());

        /*
         * calculate protein abundance
         */
        Map<String, List<String>> pa = ProteinAbundanceCalculation();
        System.out.println("protein number: " + proteinToPeptide.size());

//        for (Map.Entry<String, List<String>> entry : pa.entrySet()) {
//            System.out.println("protein group key: " + entry.getKey());
//            System.out.println("protein group values: " + entry.getValue());
//        }
        System.out.println("group size: " + pa.size());


        /*
         * Output with MzQuantML format
         */
        /*
         * Create ProteinGroupList class
         */
//        uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList protGroupList = 
//        uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList.class.newInstance();
        ProteinGroupList protGroupList = ProteinGroupList.class.newInstance();

        /*
         * Create ProteinGroup
         */
        List<ProteinGroup> protGroups = protGroupList.getProteinGroup();
        ProteinGroup protGroup = new ProteinGroup();
        protGroup.setId(proteinGroupId);
        protGroup.setSearchDatabase(null);
        protGroups.add(0, protGroup);

        /*
         * Create a new QuantLayer
         */
        List<QuantLayer> assayQuantLayers = protGroupList.getAssayQuantLayer();
        QuantLayer assayQuantLayer = new QuantLayer();
        assayQuantLayer.setId(assayQuantLayerId);


        /*
         * Create the part of DataType
         */
        CvParam cp = new CvParam();
        cp.setAccession(cvParamAssession);
        Cv cv = new Cv();
        cv.setId(cvParamId);
        cp.setCv(cv);
        cp.setName(cvParamName);
        CvParamRef cpr = new CvParamRef();
        cpr.setCvParam(cp);
        assayQuantLayer.setDataType(cpr);

        /*
         * Create the part of ColumnIndex
         */

        /*
         * Get the column indices from the QuantLayer in the original file
         * and then add these to the generated QuantLayer in ProteinGroup
         */
        QuantLayer assayQL = assayQLs.get(0);
        List<String> assayCI = assayQL.getColumnIndex();
        int nCI = assayCI.size();
        for (int i = 0; i < nCI; i++) {
            assayQuantLayer.getColumnIndex().add(assayCI.get(i));
        }


        /*
         * Create the part of DataMatrix
         */
        DataMatrix dm = new DataMatrix() {};

        //version0
//        for (Map.Entry<String, List<String>> entry : pa.entrySet()) {
//            List<String> valuelist = new ArrayList<String>();
//            /*
//             * remove the square brackets in the value list
//             */
//            String s = entry.getValue().toString();
//            s = s.substring(2, s.length() - 2);
//
//            valuelist.add(s);
//            Row row = new Row();
//            row.setObjectRef(entry.getKey());
//            row.getValue().addAll(valuelist);
//            dm.getRow().add(row);
//        }
//version1
//        for (Iterator entries = pa.entrySet().iterator(); entries.hasNext();) {
//            Entry entry = (Entry) entries.next();
//            List<String> valuelist = new ArrayList<String>();
//            String s = entry.getValue().toString();
//            s = s.substring(2, s.length() - 2);
//
//            valuelist.add(s);
//            Row row = new Row();
//            row.setObjectRef(entry.getKey().toString());
//            row.getValue().addAll(valuelist);
//            dm.getRow().add(row);
//        }
//version2
//        for (Entry entry : pa.entrySet()) {
//            List<String> valuelist = new ArrayList<String>();
//            String s = entry.getValue().toString();
//            s = s.substring(2, s.length() - 2);
//
//            valuelist.add(s);
//            Row row = new Row();
//            row.setObjectRef(entry.getKey().toString());
//            row.getValue().addAll(valuelist);
//            dm.getRow().add(row);
//        }
//version3
//        for (Iterator<Entry<String, List<String>>> it = pa.entrySet().iterator(); it.hasNext();) {
//            Entry entry = it.next();
//            List<String> valuelist = new ArrayList<String>();
//            String s = entry.getValue().toString();
//            s = s.substring(2, s.length() - 2);
//            valuelist.add(s);
//            Row row = new Row();
//            row.setObjectRef(entry.getKey().toString());
//            row.getValue().addAll(valuelist);
//            dm.getRow().add(row);
//        }
        /*
         * Alternatively, use tree map to sort the records in DataMatrix for output
         */
        Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(pa);
        DataMatrix dMatrix = sortedMap(treeMap, dm);

       /*
        * the data is input in the created QuantLayer
        */
        assayQuantLayer.setDataMatrix(dMatrix);
        assayQuantLayers.add(assayQuantLayer);

        /*
         * Produce the ProteinGroupList in the object mzq
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
    }

    /*
     * 
     */
    public static DataMatrix sortedMap(Map<String, List<String>> map, DataMatrix dM) {
        Set s = map.entrySet();

        for (Iterator it = s.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String value = entry.getValue().toString();
            value = value.substring(2, value.length() - 2);
            System.out.println(key + " => " + value);
            Row row = new Row();
            row.setObjectRef(entry.getKey().toString());
            row.getValue().add(value);
            dM.getRow().add(row);
        }
        return dM;
    }

    /*
     * Calculation of protein abundance
     */
    public static Map<String, List<String>> ProteinAbundanceCalculation() {
        Map<String, List<String>> proteinAbundance = new HashMap<String, List<String>>();
        HashSet<String> uniquePeptides = GetUniquePeptides();
        HashSet<String> sameSetPeptides = GetSameSetPeptides();
        HashSet<String> subsetPeptides = GetSubsetPeptides();

        /*
         * sum operation for abundance calculation
         */
        for (Map.Entry<String, HashSet<String>> entry : proteinToPeptide.entrySet()) {

            /*
             * get the dimension of peptide assay values
             */
            String pepSelect = entry.getValue().iterator().next().toString();
            List<String> pepVal = peptideAssayValues.get(pepSelect);
            int pepSize = pepVal.size();

//System.out.println("Peptide component number: " + pepSize);

            /*
             * create an array for calculating the abundance
             */
            double[] sumOfpepValues = new double[pepSize];
            for (int i = 0; i < pepSize; i++) {
                sumOfpepValues[i] = 0;
            }
//            System.out.println("Sum of peptide element values: " + Arrays.toString(sumOfpepValues));

//            System.out.println("Protein ID: " + entry.getKey());
            for (String peptide : entry.getValue()) {
                List<String> pepValues = peptideAssayValues.get(peptide);
                //judge which protein group the peptide belongs to
                if (uniquePeptides.contains(peptide)) {
                    for (int i = 0; i < pepValues.size(); i++) {
                        String componentValue = pepValues.get(i);

                        /*
                         * set NaN/Null to zero
                         */
                        double temp = (componentValue.equals("NaN") | componentValue.equals("Null"))
                                ? Double.parseDouble("0") : Double.parseDouble(componentValue);
                        sumOfpepValues[i] = sumOfpepValues[i] + temp;
                    }
                }

                if (sameSetPeptides.contains(peptide)) {
                    for (int i = 0; i < pepValues.size(); i++) {
                        String componentValue = pepValues.get(i);
                        double temp = (componentValue.equals("NaN") | componentValue.equals("Null"))
                                ? Double.parseDouble("0") : Double.parseDouble(componentValue);
                        sumOfpepValues[i] = sumOfpepValues[i] + temp;
                    }
                }

                if (subsetPeptides.contains(peptide)) {
                    for (int i = 0; i < pepValues.size(); i++) {
                        String componentValue = pepValues.get(i);
                        double temp = (componentValue.equals("NaN") | componentValue.equals("Null"))
                                ? Double.parseDouble("0") : Double.parseDouble(componentValue);
                        sumOfpepValues[i] = sumOfpepValues[i] + temp;
                    }
                }
            }
            List<String> proteinAbundanceList = Arrays.asList(Arrays.toString(sumOfpepValues));
            proteinAbundance.put(entry.getKey(), proteinAbundanceList);

//            System.out.println("Sum Values: " + Arrays.toString(sumOfpepValues));
        }

//        List<QuantLayer> abundanceQuantLayers = proteinList.getAssayQuantLayer();
        System.out.println("Protein abundance:" + proteinAbundance);

        return proteinAbundance;
    }

    public static HashSet<String> GetUniquePeptides() {
        HashSet<String> uniquePeptides = new HashSet<String>();

        for (Map.Entry<String, HashSet<String>> entry : peptideToProtein.entrySet()) {
            if (entry.getValue().size() == 1) {
                uniquePeptides.add(entry.getKey());
            }
        }
        return uniquePeptides;
    }

    public static HashSet<String> GetSameSetPeptides() {
        HashSet<String> sameSet = new HashSet<String>();
        for (Map.Entry<String, HashSet<String>> entry : peptideToProtein.entrySet()) {
            if (entry.getValue().size() == 1) {
                continue;
            }

            HashSet<String> peptides = new HashSet<String>();
            for (String protein : entry.getValue()) {
                for (String peptide : proteinToPeptide.get(protein)) {
                    peptides.add(peptide);
                }
            }
            boolean isComplete = true;
            for (String peptide : peptides) {
                for (String protein : entry.getValue()) {
                    if (peptideToProtein.get(peptide).contains(protein)) {
                        continue;
                    }

                    isComplete = false;
                    break;
                }
            }
            if (isComplete) {
                sameSet.add(entry.getKey());
            }
        }
        return sameSet;
    }

    public static HashSet<String> GetSubsetPeptides() {
        HashSet<String> subSet = new HashSet<String>();
        for (Map.Entry<String, HashSet<String>> entry : peptideToProtein.entrySet()) {
//            List<String> listProtein = (List<String>) entry.getValue();
            String largestProtein = entry.getValue().iterator().next().toString();
            HashSet<String> peptides = new HashSet<String>();
            HashSet<String> proteins = new HashSet<String>();
            proteins.add(largestProtein);
            int peptideCount = 0;
            int proteinCount = 0;
            do {
                peptideCount = peptides.size();
                proteinCount = proteins.size();

                for (String protein : proteins) {

                    if (proteinToPeptide.get(protein).size() > proteinToPeptide.get(largestProtein).size()) {
                        largestProtein = protein;
                    }

                    for (String peptide : proteinToPeptide.get(protein)) {
                        peptides.add(peptide);
                    }
                }

                for (String peptide : peptides) {
                    for (String protein : peptideToProtein.get(peptide)) {
                        proteins.add(protein);
                    }
                }
            } while (peptideCount != peptides.size() || proteinCount != proteins.size());

            if (proteins.size() == 1) {
                continue;
            }

            boolean isComplete = true;
            for (String peptide : peptides) {
                if (proteinToPeptide.get(largestProtein).contains(peptide)) {
                    continue;
                }
                isComplete = false;
                break;
            }

            if (!isComplete) {
                continue;
            }

            isComplete = true;
            for (String peptide : peptides) {
                for (String protein : proteins) {
                    if (peptideToProtein.get(peptide).contains(protein)) {
                        continue;
                    }

                    isComplete = false;
                    break;
                }
            }

            if (isComplete) {
                continue;
            }

            subSet.add(entry.getKey());
        }
        return subSet;
    }
//    public boolean IsProtein(String vertex) {
//        return proteinToPeptide.containsKey(vertex);
//    }
}
