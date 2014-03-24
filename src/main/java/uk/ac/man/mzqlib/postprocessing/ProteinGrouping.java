/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.man.mzqlib.postprocessing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * implement the relevant methods in protein grouping
 *
 * @author man-mqbsshz2
 */
public class ProteinGrouping {

    private static final Map<String, HashSet<String>> sameSetGr = new HashMap<String, HashSet<String>>();
    private static final Map<String, HashSet<String>> subSetGr = new HashMap<String, HashSet<String>>();
    private static final Map<String, HashSet<String>> uniSetGr = new HashMap<String, HashSet<String>>();

    /**
     * set the protein groups in order in terms of the grouping results
     *
     * @param pa: protein abundance
     * @return the ordered groups
     */
    public static Map<String, String> GroupInOrder(Map<String, List<String>> pa) {
        Map<String, String> groupIO = new HashMap<String, String>();

        int no = 0;
        String num;
        for (Map.Entry<String, List<String>> entry : pa.entrySet()) {
            String key = entry.getKey();
            if (key.contains("UniSetGroup")) {
                no++;
                num = key.substring(11, key.length());
                groupIO.put(key, "ProteinGroup" + num);

            }
        }

        for (Map.Entry<String, List<String>> entry : pa.entrySet()) {
            String key = entry.getKey();
            if (key.contains("SameSetGroup")) {
                no++;
//                num = key.substring(12, key.length());
//                int numi = Integer.parseInt(num) + no;

//                String nums = Integer.toString(numi);
                groupIO.put(key, "ProteinGroup" + no);

            }
        }

        for (Map.Entry<String, List<String>> entry : pa.entrySet()) {
            String key = entry.getKey();
            if (key.contains("SubSetGroup")) {
                no++;
//                num = key.substring(11, key.length());
//                int numi = Integer.parseInt(num) + no;

//                String nums = Integer.toString(numi);
                groupIO.put(key, "ProteinGroup" + no);
            }
        }

        return groupIO;
    }

    /**
     * get the sameSet groups
     *
     * @param pepToProt
     * @param protToPep
     * @return a map of SameSetGroup-to-peptides
     */
    public static Map<String, HashSet<String>> SameSetGrouping(Map<String, HashSet<String>> pepToProt,
            Map<String, HashSet<String>> protToPep) {
        HashSet<String> sameSetTmp = GetSameSetPeptides(pepToProt, protToPep);
//        System.out.println("Same set: " + sameSetTmp);
//        proteinToPeptideTmp = proteinToPeptide;
//        peptideToProteinTmp = peptideToProtein;
        int sameSetGroupNo = 0;
        while (!sameSetTmp.isEmpty()) {
            sameSetGroupNo++;
            String pepTmpSel = sameSetTmp.iterator().next().toString();
            HashSet<String> proSetTmp = pepToProt.get(pepTmpSel);
            
//            System.out.println("protein set tmp: " + proSetTmp);
            String proTmpSel = proSetTmp.iterator().next().toString();
            HashSet<String> pepSetTmp = protToPep.get(proTmpSel);

//            System.out.println("peptide set tmp: " + pepSetTmp);
            
            sameSetGr.put("SameSetGroup" + sameSetGroupNo, pepSetTmp);

            Iterator<String> sameSetTmpItr = sameSetTmp.iterator();
            while (sameSetTmpItr.hasNext()) {
                String pep = sameSetTmpItr.next();
                if (pepSetTmp.contains(pep)) {
                    sameSetTmpItr.remove();
                }
            }
        }
        return sameSetGr;
    }

    /**
     * get the subSet groups
     *
     * @param pepToProt
     * @param protToPep
     * @return a map of SubSetGroups-to-peptides
     */
    public static Map<String, HashSet<String>> SubSetGrouping(Map<String, HashSet<String>> pepToProt,
            Map<String, HashSet<String>> protToPep) {
        HashSet<String> subSetTmp = GetSubsetPeptides(pepToProt, protToPep);
        System.out.println("Sub Set Tmp: " + subSetTmp);
        
//        proteinToPeptideTmp = proteinToPeptide;
//        peptideToProteinTmp = peptideToProtein;
        int subSetGroupNo = 0;
        String pepTmpSel = null;
        while (!subSetTmp.isEmpty()) {
            subSetGroupNo++;

//            for (String pep : subSetTmp) {
//                if (pepToProt.get(pep).size() == 1) {
//                    pepTmpSel = pep;
//                    break;
//                }
//            }
            
            pepTmpSel = subSetTmp.iterator().next().toString();

            HashSet<String> proSetTmp = pepToProt.get(pepTmpSel);
            String proTmpSel = proSetTmp.iterator().next().toString();
            HashSet<String> pepSetTmp = protToPep.get(proTmpSel);

            subSetGr.put("SubSetGroup" + subSetGroupNo, pepSetTmp);

//            System.out.println("pro Set Tmp: " + proSetTmp);
//            System.out.println("pro Tmp Sel: " + proTmpSel);
//            System.out.println("pep Set Tmp: " + pepSetTmp);
//            System.out.println("Sub Set Group: " + subSetGr);
            Iterator<String> subSetTmpItr = subSetTmp.iterator();
            while (subSetTmpItr.hasNext()) {
                String pep = subSetTmpItr.next();
                if (pepSetTmp.contains(pep)) {
                    subSetTmpItr.remove();
                }
            }
        }
        return subSetGr;
    }

    /**
     * get the uniSet groups
     *
     * @param pepToProt
     * @param protToPep
     * @return a map of UniSetGroups-to-peptides
     */
    public static Map<String, HashSet<String>> UniSetGrouping(Map<String, HashSet<String>> pepToProt,
            Map<String, HashSet<String>> protToPep) {
//        Map<String, HashSet<String>> uniSetGroup0 = new HashMap<String, HashSet<String>>();
        HashSet<String> uniSetTmp = GetUniquePeptides(pepToProt);

        //remove the uniques in subSet from uniSetTmp
        HashSet<String> subSet0 = GetSubsetPeptides(pepToProt, protToPep);
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

            HashSet<String> proTmp = pepToProt.get(pepTmp);
            //obtain pepSetTmp, cannot directly use proTmp
            Iterator<String> proTmpItr = proTmp.iterator();
            HashSet<String> pepSetTmp = protToPep.get(proTmpItr.next());

            HashSet<String> uniSetTmpJoined = new HashSet<String>();
//            System.out.println("Pro Temp: " + proTmp);
//            System.out.println("Pep Temp: " + pepSetTmp);
            for (String pepTmp1 : pepSetTmp) {
                if (uniSetTmp1.contains(pepTmp1)) {
                    uniSetTmpJoined.add(pepTmp1);
                }
            }
            uniSetGr.put("UniSetGroup" + uniSetGroupNo, uniSetTmpJoined);
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

        return uniSetGr;

    }

    /**
     * get unique peptides from the map of peptide-to-protein
     *
     * @param pepToPro
     * @return a hashSet for unique peptides
     */
    private static HashSet<String> GetUniquePeptides(Map<String, HashSet<String>> pepToPro) {
        HashSet<String> uniquePeptides = new HashSet<String>();

        for (Map.Entry<String, HashSet<String>> entry : pepToPro.entrySet()) {
            if (entry.getValue().size() == 1) {
                uniquePeptides.add(entry.getKey());
            }
        }
        return uniquePeptides;
    }

    /**
     * get the peptides belonging to SameSet from the map of peptide-to-protein
     *
     * @param pepToPro
     * @param proToPep
     * @return a hashSet for sameSet peptides
     */
    private static HashSet<String> GetSameSetPeptides(Map<String, HashSet<String>> pepToPro,
            Map<String, HashSet<String>> proToPep) {
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

    /**
     * get the peptides belonging to SubSet from the map of peptide-to-protein
     *
     * @param pepToPro
     * @param proToPep
     * @return a map for subSet peptides
     */
    private static HashSet<String> GetSubsetPeptides(Map<String, HashSet<String>> pepToPro,
            Map<String, HashSet<String>> proToPep) {
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
}
