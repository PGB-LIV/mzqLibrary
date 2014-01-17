package com.mycompany.proteininferencetest;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.bind.JAXBException;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.*;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

public class App {

    private static final Map<String, HashSet<String>> proteinToPeptide = new HashMap<String, HashSet<String>>();
    private static final Map<String, HashSet<String>> peptideToProtein = new HashMap<String, HashSet<String>>();
    private static final Map<String, List<String>> peptideAssayValues = new HashMap<String, List<String>>();

    private static final Map<String, String> proteinToAccession = new HashMap<String, String>();

    private static Map<String, HashSet<String>> sameSetGroup = new HashMap<String, HashSet<String>>();
    private static Map<String, HashSet<String>> subSetGroup = new HashMap<String, HashSet<String>>();
    private static Map<String, HashSet<String>> uniSetGroup = new HashMap<String, HashSet<String>>();

//    private static Map<String, HashSet<String>> proteinToPeptideTmp = new HashMap<String, HashSet<String>>();
//    private static Map<String, HashSet<String>> peptideToProteinTmp = new HashMap<String, HashSet<String>>();

    public static void main(String[] args) throws JAXBException, InstantiationException, IllegalAccessException {

        /*
         * the parameters in the generated ProteinGroupList
         */
        /*
         * calculation method for protein abundance
         */
        final String abunOperation = "sum";
//        final String abunOperation = "mean";
//        final String abunOperation = "median";

        /*
         * other parameters
         */
        final String proteinGroupList = "ProteinGroupList1";
//        final String proteinGroupId = "prot_3";
        final String searchDatabase = "SD1";
        final String assayQuantLayerId = "PGL_AQL3";
        final String cvParamAssession = "MS:1001893";
        final String cvParamId = "PSI-MS";
        final String cvRef = "PSI-MS";
        final String techName = "iTraq";
        final String cvParamName = techName + ": Protein Grouping Abundance (" + abunOperation + " operation)";

//        String in_file = args[0];
//        String out_file = args[1];
//        String in_file = "./CPTAC-study6_EvsB.mzq";
//        String out_file = "./CPTAC-study6_EvsB_proteinAbundance_sorted_v1.mzq";
//        String in_file = "test_itraq_revised.mzq"; 
//        String out_file = "test_itraq_proteinAbundance_temp.mzq";
//        final String in_file = "CPTAC-Progenesis-small-example.mzq";
//        final String out_file = "CPTAC-Progenesis-small-example_proteinAbundance1.mzq";
        final String in_file = "../itraq/test_ksl_itraq_result.mzq";
        final String out_file = "../itraq/test_ksl_itraq_result_proteinAbundance.mzq";
//        String in_file = "test_grouping_data.mzq";
//        String out_file = "test_grouping_data_proteinAbundance_" + abunOperation + "new.mzq";
//        String in_file = "test_grouping_data_update.mzq";
//        String out_file = "test_grouping_data_update_proteinAbundance_" + abunOperation + ".mzq";

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
                proteinToAccession.put(protein.getId(), protein.getAccession());
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

        System.out.println("Protein to Peptide: " + proteinToPeptide);
        System.out.println();
        System.out.println("Protein to Peptide + Protein No.: " + proteinToPeptide.size());
        System.out.println();
        System.out.println("Peptide to Protein: " + peptideToProtein);
        System.out.println();
        System.out.println("Peptide to Protein + Peptide No.: " + peptideToProtein.size());
        System.out.println();

//        Map<String, HashSet<String>> pp1 = peptideToProtein;
//        Map<String, HashSet<String>> pp2 = proteinToPeptide;
//        HashSet<String> ss = GetSameSetPeptides(pp1,pp2);
//        System.out.println("sameSet: " + ss);
//        HashSet<String> uniquePeptides = GetUniquePeptides();

        /*
         obtain protein groups
         */
//        HashSet<String> sameSetTmp = GetSameSetPeptides(peptideToProtein, proteinToPeptide);
//        HashSet<String> subSetTmp = GetSubsetPeptides(peptideToProtein, proteinToPeptide);
//
//        proteinToPeptideTmp = proteinToPeptide;
//        peptideToProteinTmp = peptideToProtein;
//        int sameSetGroupNo = 0;
//        while (sameSetTmp.isEmpty()) {
//            sameSetGroupNo++;
//            sameSetGroup.put("sameSetGroup" + sameSetGroupNo, sameSetTmp);
//
//            for (Map.Entry<String, HashSet<String>> entry : proteinToPeptide.entrySet()) {
//                if (sameSetTmp.equals(entry.getValue())) {
//                    proteinToPeptideTmp.remove(entry.getKey());
//                }
//            }
//
//            for (Map.Entry<String, HashSet<String>> entry : peptideToProtein.entrySet()) {
//                if (sameSetTmp.contains(entry.getKey())) {
//                    peptideToProteinTmp.remove(entry.getKey());
//                }
//            }
//            sameSetTmp = GetSameSetPeptides(peptideToProteinTmp, proteinToPeptideTmp);
//        }
//
//        int subSetGroupNo = 0;
//        while (subSetTmp.isEmpty()) {
//            subSetGroupNo++;
//            subSetGroup.put("subSetGroup" + subSetGroupNo, subSetTmp);
//
//            for (Map.Entry<String, HashSet<String>> entry : proteinToPeptide.entrySet()) {
//                if (subSetTmp.contains(entry.getValue().toString())) {
//                    proteinToPeptideTmp.remove(entry.getKey());
//                }
//            }
//
//            for (Map.Entry<String, HashSet<String>> entry : peptideToProtein.entrySet()) {
//                if (subSetTmp.contains(entry.getKey())) {
//                    peptideToProteinTmp.remove(entry.getKey());
//                }
//            }
//            subSetTmp = GetSubsetPeptides(peptideToProteinTmp, proteinToPeptideTmp);
//        }
        HashSet<String> us = GetUniquePeptides(peptideToProtein);
        System.out.println("Unique Peps: " + us);
        System.out.println();

        uniSetGroup = UniSetGrouping();
        sameSetGroup = SameSetGrouping();
        subSetGroup = SubSetGrouping();

        System.out.println("Unique Group: " + uniSetGroup);
        System.out.println();
        System.out.println("Unique Peptides No.: " + uniSetGroup.size());
        System.out.println();
        System.out.println("SameSet Group: " + sameSetGroup);
        System.out.println();
        System.out.println("SameSet Group Number: " + sameSetGroup.size());
        System.out.println();
        System.out.println("SubSet Group: " + subSetGroup);
        System.out.println();
        System.out.println("SubSet Group Number: " + subSetGroup.size());

        /*
         * obtain protein abundance
         */
        Map<String, List<String>> proteinAbundance = ProteinAbundanceCalculation(abunOperation,
                uniSetGroup, sameSetGroup, subSetGroup);
//        System.out.println("Protein No.: " + proteinToPeptide.size());
        System.out.println("Protein aboundance: " + proteinAbundance);

        Map<String, String> groupInOrder = GroupInOrder(proteinAbundance);

//        for (Map.Entry<String, List<String>> entry : proteinAbundance.entrySet()) {
//            System.out.println("protein group key: " + entry.getKey());
//            System.out.println("protein group values: " + entry.getValue());
//        }
        System.out.println("Group No.: " + proteinAbundance.size());

        System.out.println("Group in Order: " + groupInOrder);
        /*
         * Output with MzQuantML format
         */
        /*
         * Create ProteinGroupList class
         */
//        uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList protGroupList = 
//        uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList.class.newInstance();
        ProteinGroupList protGroupList = ProteinGroupList.class.newInstance();

        protGroupList.setId(proteinGroupList);

        /*
         * Create ProteinGroup
         */
        List<ProteinGroup> protGroups = protGroupList.getProteinGroup();
        for (int i = 1; i < groupInOrder.size() + 1; i++) {

            for (Map.Entry<String, String> entry : groupInOrder.entrySet()) {

                if (entry.getValue().equals("ProteinGroup" + i)) {

                    String proteinGroupId = entry.getValue();
                    String proteinGroupIdOri = entry.getKey();

                    System.out.println("Protein Group ID: " + proteinGroupId);

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
                        System.out.println("Protein Accession: " + prot.getAccession());
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
                            System.out.println("Protein Accession: " + prot.getAccession());
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
                            System.out.println("Protein Accession: " + prot.getAccession());
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

                    protGroups.add(i - 1, protGroup);

                }
            }
        }


        /*
         * Create a new QuantLayer
         */
        List<QuantLayer> assayQuantLayers = protGroupList.getAssayQuantLayer();
        QuantLayer assayQuantLayer = new QuantLayer();
        assayQuantLayer.setId(assayQuantLayerId);


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
        DataMatrix dMatrix = SortedMap(treeMap, dm);
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
    }

    /*
     * Make the records of DataMatrix in order
     */
//    public static DataMatrix SortedMap(Map<String, List<String>> map, DataMatrix dM, Map<String, String> gIo) {
    public static DataMatrix SortedMap(Map<String, List<String>> map, DataMatrix dM) {
        Set s = map.entrySet();

        for (Iterator it = s.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
//            String key = (String) entry.getKey();
            String value = entry.getValue().toString();
            //remove the double quotation marks in front and rear
            value = value.substring(1, value.length() - 2).replaceAll("[\\,]", " ");
            //remove the last 5 characters of groupId
            value = value.substring(0, value.length() - 5);

//            System.out.println(key + " => " + value);
            Row row = new Row();

            row.setObjectRef(entry.getKey().toString());
//            System.out.println("sKey: " + sKey);
//            String sKey = entry.getKey().toString();
//            row.setObjectRef(gIo.get(sKey));

            row.getValue().add(value);
            dM.getRow().add(row);
        }
        return dM;
    }

    public static Map<String, HashSet<String>> SameSetGrouping() {
        HashSet<String> sameSetTmp = GetSameSetPeptides(peptideToProtein, proteinToPeptide);
//        proteinToPeptideTmp = proteinToPeptide;
//        peptideToProteinTmp = peptideToProtein;
        int sameSetGroupNo = 0;
        while (!sameSetTmp.isEmpty()) {
            sameSetGroupNo++;
            String pepTmpSel = sameSetTmp.iterator().next().toString();
            HashSet<String> proSetTmp = peptideToProtein.get(pepTmpSel);
            String proTmpSel = proSetTmp.iterator().next().toString();
            HashSet<String> pepSetTmp = proteinToPeptide.get(proTmpSel);

            sameSetGroup.put("SameSetGroup" + sameSetGroupNo, pepSetTmp);

            Iterator<String> sameSetTmpItr = sameSetTmp.iterator();
            while (sameSetTmpItr.hasNext()) {
                String pep = sameSetTmpItr.next();
                if (pepSetTmp.contains(pep)) {
                    sameSetTmpItr.remove();
                }
            }

//            for (Iterator<Map.Entry<String, HashSet<String>>> it = proteinToPeptideTmp.entrySet().iterator(); it.hasNext();) {
//                Map.Entry<String, HashSet<String>> entry = it.next();
//                if (sameSetTmp.equals(entry.getValue())) {
////                    proteinToPeptideTmp.remove(entry.getKey());
//                    it.remove();
//                }
//            }
//
//            for (Iterator<Map.Entry<String, HashSet<String>>> it = peptideToProteinTmp.entrySet().iterator(); it.hasNext();) {
//                Map.Entry<String, HashSet<String>> entry = it.next();
//                if (sameSetTmp.contains(entry.getKey())) {
////                    peptideToProteinTmp.remove(entry.getKey());
//                    it.remove();
//                }
//            }
//            sameSetTmp = GetSameSetPeptides(peptideToProteinTmp, proteinToPeptideTmp);
        }
        return sameSetGroup;
    }

    public static Map<String, HashSet<String>> SubSetGrouping() {
        HashSet<String> subSetTmp = GetSubsetPeptides(peptideToProtein, proteinToPeptide);
//        proteinToPeptideTmp = proteinToPeptide;
//        peptideToProteinTmp = peptideToProtein;
        int subSetGroupNo = 0;
        String pepTmpSel = null;
        while (!subSetTmp.isEmpty()) {
            subSetGroupNo++;

            for (String pep : subSetTmp) {
                if (peptideToProtein.get(pep).size() == 1) {
                    pepTmpSel = pep;
                    break;
                }
            }

            HashSet<String> proSetTmp = peptideToProtein.get(pepTmpSel);
            String proTmpSel = proSetTmp.iterator().next().toString();
            HashSet<String> pepSetTmp = proteinToPeptide.get(proTmpSel);

            subSetGroup.put("SubSetGroup" + subSetGroupNo, pepSetTmp);

            System.out.println("pro Set Tmp: " + proSetTmp);
            System.out.println("pro Tmp Sel: " + proTmpSel);
            System.out.println("pep Set Tmp: " + pepSetTmp);
            System.out.println("Sub Set Group: " + subSetGroup);

            Iterator<String> subSetTmpItr = subSetTmp.iterator();
            while (subSetTmpItr.hasNext()) {
                String pep = subSetTmpItr.next();
                if (pepSetTmp.contains(pep)) {
                    subSetTmpItr.remove();
                }
            }
        }
        return subSetGroup;
    }

    public static Map<String, HashSet<String>> UniSetGrouping() {
        Map<String, HashSet<String>> uniSetGroup0 = new HashMap<String, HashSet<String>>();
        HashSet<String> uniSetTmp = GetUniquePeptides(peptideToProtein);

        //remove the uniques in subSet from uniSetTmp
        HashSet<String> subSet0 = GetSubsetPeptides(peptideToProtein, proteinToPeptide);
        Iterator<String> uniSetTmpItr = uniSetTmp.iterator();
        while (uniSetTmpItr.hasNext()) {
            String pep = uniSetTmpItr.next();
            if (subSet0.contains(pep)) {
                uniSetTmpItr.remove();
            }
        }

//        System.out.println("Unique uniSetTmp: " + uniSetTmp);
        int uniSetGroupNo = 0;
        while (!uniSetTmp.isEmpty()) {
            String pepTmp = uniSetTmp.iterator().next().toString();
            HashSet<String> uniSetTmp1 = uniSetTmp;
            uniSetGroupNo++;

            HashSet<String> proTmp = peptideToProtein.get(pepTmp);
            //obtain pepSetTmp, cannot directly use proTmp
            Iterator<String> proTmpItr = proTmp.iterator();
            HashSet<String> pepSetTmp = proteinToPeptide.get(proTmpItr.next());

            HashSet<String> uniSetTmpJoined = new HashSet<String>();
//            System.out.println("Pro Temp: " + proTmp);
//            System.out.println("Pep Temp: " + pepSetTmp);
            for (String pepTmp1 : pepSetTmp) {
                if (uniSetTmp1.contains(pepTmp1)) {
                    uniSetTmpJoined.add(pepTmp1);
                }
            }
            uniSetGroup0.put("UniSetGroup" + uniSetGroupNo, uniSetTmpJoined);
//                    System.out.println("UniSetGroup: " + uniSetGroup0);
//                    System.out.println("UniSetTmp1: " + uniSetTmp1);
            Iterator<String> uniSetTmp1Itr = uniSetTmp1.iterator();
            while (uniSetTmp1Itr.hasNext()) {
                String pep = uniSetTmp1Itr.next();
                if (uniSetTmpJoined.contains(pep)) {
                    uniSetTmp1Itr.remove();
                }
            }
            uniSetTmp = uniSetTmp1;
//            System.out.println("uni Set Temp: " + uniSetTmp);

        }

        return uniSetGroup0;

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

        int groupId = 0;
        int groupLeader = 0;
        DecimalFormat df = new DecimalFormat(".000");
        /*
         unique set case
         */
        for (Map.Entry<String, HashSet<String>> entry : uniSetGr.entrySet()) {
            groupId++;
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
                operationPepValue = ColumnSum(matrixPepValue);
            } else if (operation.equals("mean")) {
                operationPepValue = ColumnSum(matrixPepValue);
                for (int i = 0; i < operationPepValue.length; i++) {
                    operationPepValue[i] = operationPepValue[i] / numPeptides;
                }
            } else if (operation.equals("median")) {
                double[] tmp = new double[numPeptides];
                for (int j = 0; j < operationPepValue.length; j++) {
                    for (int i = 0; i < numPeptides; i++) {
                        tmp[i] = matrixPepValue[i][j];
                    }
                    operationPepValue[j] = Median(tmp);
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

            groupId++;
            String pepSelect = entry.getValue().iterator().next().toString();
            HashSet<String> proteins = peptideToProtein.get(pepSelect);
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
                operationPepValue = ColumnSum(matrixPepValue);
            } else if (operation.equals("mean")) {
                operationPepValue = ColumnSum(matrixPepValue);
                for (int i = 0; i < operationPepValue.length; i++) {
                    operationPepValue[i] = operationPepValue[i] / numPeptides;
                }
            } else if (operation.equals("median")) {
                double[] tmp = new double[numPeptides];
                for (int j = 0; j < operationPepValue.length; j++) {
                    for (int i = 0; i < numPeptides; i++) {
                        tmp[i] = matrixPepValue[i][j];
                    }
                    operationPepValue[j] = Median(tmp);
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
            groupId++;
            String pepSelect = entry.getValue().iterator().next().toString();
            HashSet<String> proteins = peptideToProtein.get(pepSelect);
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
                operationPepValue = ColumnSum(matrixPepValue);
            } else if (operation.equals("mean")) {
                operationPepValue = ColumnSum(matrixPepValue);
                for (int i = 0; i < operationPepValue.length; i++) {
                    operationPepValue[i] = operationPepValue[i] / numPeptides;
                }
            } else if (operation.equals("median")) {
                double[] tmp = new double[numPeptides];
                for (int j = 0; j < operationPepValue.length; j++) {
                    for (int i = 0; i < numPeptides; i++) {
                        tmp[i] = matrixPepValue[i][j];
                    }
                    operationPepValue[j] = Median(tmp);
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

    public static Map<String, String> GroupInOrder(Map<String, List<String>> pa) {
        Map<String, String> groupInOrder = new HashMap<String, String>();

        int no = 0;
        String num;
        for (Map.Entry<String, List<String>> entry : pa.entrySet()) {
            String key = entry.getKey();
            if (key.contains("UniSetGroup")) {
                no++;
                num = key.substring(11, key.length());
                groupInOrder.put(key, "ProteinGroup" + num);

            }
        }

        for (Map.Entry<String, List<String>> entry : pa.entrySet()) {
            String key = entry.getKey();
            if (key.contains("SameSetGroup")) {
                no++;
//                num = key.substring(12, key.length());
//                int numi = Integer.parseInt(num) + no;

//                String nums = Integer.toString(numi);
                groupInOrder.put(key, "ProteinGroup" + no);

            }
        }

        for (Map.Entry<String, List<String>> entry : pa.entrySet()) {
            String key = entry.getKey();
            if (key.contains("SubSetGroup")) {
                no++;
//                num = key.substring(11, key.length());
//                int numi = Integer.parseInt(num) + no;

//                String nums = Integer.toString(numi);
                groupInOrder.put(key, "ProteinGroup" + no);
            }
        }

        return groupInOrder;
    }

    public static double[] ColumnSum(double[][] arr) {
        int index = 0;
        double[] temp = new double[arr[index].length];
        for (int i = 0; i < arr[0].length; i++) {
            double sum = 0;
            for (double[] arr1 : arr) {
                sum += arr1[i];
            }

//            for (int j = 0; j < arr.length; j++) {
//                sum += arr[j][i];
//            }
            temp[index] = sum;
//      System.out.println("Index is: " + index + " Sum is: " + sum);
            index++;
        }
        return temp;
    }

    public static double Median(double[] d) {
        Arrays.sort(d);
        int middle = d.length / 2;
        if (d.length % 2 == 0) {
            double left = d[middle - 1];
            double right = d[middle];
            return (left + right) / 2;
        } else {
            return d[middle];
        }
    }

    public static HashSet<String> GetUniquePeptides(Map<String, HashSet<String>> pepToPro) {
        HashSet<String> uniquePeptides = new HashSet<String>();

        for (Map.Entry<String, HashSet<String>> entry : pepToPro.entrySet()) {
            if (entry.getValue().size() == 1) {
                uniquePeptides.add(entry.getKey());
            }
        }
        return uniquePeptides;
    }

    public static HashSet<String> GetSameSetPeptides(Map<String, HashSet<String>> pepToPro, Map<String, HashSet<String>> proToPep) {
        HashSet<String> sameSet = new HashSet<String>();
        for (Map.Entry<String, HashSet<String>> entry : pepToPro.entrySet()) {
            if (entry.getValue().size() == 1) {
                continue;
            }

            HashSet<String> peptides = new HashSet<String>();
            for (String protein : entry.getValue()) {
                for (String peptide : proToPep.get(protein)) {
                    peptides.add(peptide);
                }
            }
            boolean isComplete = true;
            for (String peptide : peptides) {
                for (String protein : entry.getValue()) {
                    if (pepToPro.get(peptide).contains(protein)) {
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

    public static HashSet<String> GetSameSetPeptides1() {
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

    public static HashSet<String> GetSubsetPeptides(Map<String, HashSet<String>> pepToPro, Map<String, HashSet<String>> proToPep) {
        HashSet<String> subSet = new HashSet<String>();
        for (Map.Entry<String, HashSet<String>> entry : pepToPro.entrySet()) {
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

                    if (proToPep.get(protein).size() > proToPep.get(largestProtein).size()) {
                        largestProtein = protein;
                    }

                    for (String peptide : proToPep.get(protein)) {
                        peptides.add(peptide);
                    }
                }

                for (String peptide : peptides) {
                    for (String protein : pepToPro.get(peptide)) {
                        proteins.add(protein);
                    }
                }
            } while (peptideCount != peptides.size() || proteinCount != proteins.size());

            if (proteins.size() == 1) {
                continue;
            }

            boolean isComplete = true;
            for (String peptide : peptides) {
                if (proToPep.get(largestProtein).contains(peptide)) {
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
                    if (pepToPro.get(peptide).contains(protein)) {
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
