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
import java.util.Set;

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
    public static Map<String, String> groupInOrder(Map<String, List<String>> pa) {
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
     * @param pepToProt - peptide-to-protein map
     * @param protToPep - protein-to-peptide map
     * @return a map of SameSetGroup-to-peptides
     */
    public static Map<String, HashSet<String>> sameSetGrouping(Map<String, HashSet<String>> pepToProt,
            Map<String, HashSet<String>> protToPep) {
        HashSet<String> sameSetTmp = getSameSetPeptides(pepToProt, protToPep);

        int sameSetGroupNo = 0;
        while (!sameSetTmp.isEmpty()) {
            sameSetGroupNo++;
            String pepTmpSel = sameSetTmp.iterator().next().toString();
            HashSet<String> proSetTmp = pepToProt.get(pepTmpSel);

            String proTmpSel = proSetTmp.iterator().next().toString();
            HashSet<String> pepSetTmp = protToPep.get(proTmpSel);

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
     * @param pepToProt - peptide-to-protein map
     * @param protToPep - protein-to-peptide map
     * @return a map of SubSetGroups-to-peptides
     */
    public static Map<String, HashSet<String>> subSetGrouping(Map<String, HashSet<String>> pepToProt,
            Map<String, HashSet<String>> protToPep) {
        HashSet<String> subSetTmp = getSubsetPeptides(pepToProt, protToPep);

        int subSetGroupNo = 0;
        String pepTmpSel = null;
        while (!subSetTmp.isEmpty()) {
            subSetGroupNo++;

            HashSet<String> pepSetTmp = new HashSet<String>();
            pepTmpSel = subSetTmp.iterator().next().toString();

            HashSet<String> proSetTmp = pepToProt.get(pepTmpSel);

            Iterator<String> proteinsPepSel = proSetTmp.iterator();
            
            while (proteinsPepSel.hasNext()) {

                String proteinPepSel = proteinsPepSel.next();
                HashSet<String> pepSetTmp0 = protToPep.get(proteinPepSel);
                pepSetTmp.addAll(pepSetTmp0);

            }

            subSetGr.put("SubSetGroup" + subSetGroupNo, pepSetTmp);

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
     * @param pepToProt - peptide-to-protein map
     * @param protToPep - protein-to-peptide map
     * @return a map of UniSetGroups-to-peptides
     */
    public static Map<String, HashSet<String>> uniSetGrouping(Map<String, HashSet<String>> pepToProt,
            Map<String, HashSet<String>> protToPep) {
//        Map<String, HashSet<String>> uniSetGroup0 = new HashMap<String, HashSet<String>>();
        HashSet<String> uniSetTmp = getUniquePeptides(pepToProt);

        //remove the uniques in subSet from uniSetTmp
        HashSet<String> subSet0 = getSubsetPeptides(pepToProt, protToPep);
        Iterator<String> uniSetTmpItr = uniSetTmp.iterator();
        while (uniSetTmpItr.hasNext()) {
            String pep = uniSetTmpItr.next();
            if (subSet0.contains(pep)) {
                uniSetTmpItr.remove();
            }
        }

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

            for (String pepTmp1 : pepSetTmp) {
                if (uniSetTmp1.contains(pepTmp1)) {
                    uniSetTmpJoined.add(pepTmp1);
                }
            }
            uniSetGr.put("UniSetGroup" + uniSetGroupNo, uniSetTmpJoined);

            Iterator<String> uniSetTmp1Itr = uniSetTmp1.iterator();
            while (uniSetTmp1Itr.hasNext()) {
                String pep = uniSetTmp1Itr.next();
                if (uniSetTmpJoined.contains(pep)) {
                    uniSetTmp1Itr.remove();
                }
            }
            uniSetTmp = uniSetTmp1;
        }

        return uniSetGr;

    }

    /**
     * get unique peptides from the map of peptide-to-protein
     *
     * @param pepToPro - peptide-to-protein
     * @return a hashSet for unique peptides
     */
    private static HashSet<String> getUniquePeptides(Map<String, HashSet<String>> pepToPro) {
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
     * @param pepToPro - peptide-to-protein
     * @param proToPep - protein-to-peptide
     * @return a hashSet for sameSet peptides
     */
    private static HashSet<String> getSameSetPeptides(Map<String, HashSet<String>> pepToPro,
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
     * @param pepToPro - peptide-to-protein
     * @param proToPep - protein-to-peptide
     * @return a map for subSet peptides
     */
    private static HashSet<String> getSubsetPeptides(Map<String, HashSet<String>> pepToPro,
            Map<String, HashSet<String>> proToPep) {
        HashSet<String> subSet = new HashSet<String>();
        for (Map.Entry<String, HashSet<String>> entry : pepToPro.entrySet()) {

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
