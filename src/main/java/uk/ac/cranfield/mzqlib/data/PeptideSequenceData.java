
package uk.ac.cranfield.mzqlib.data;

import java.util.Collection;
import java.util.HashMap;

/**
 * PeptideSequenceData.
 *
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class PeptideSequenceData {

    private String sequence;
//    private ArrayList<String> protein = new ArrayList<String>();
    private HashMap<String, PeptideData> peptides;

//    public ArrayList<String> getProtein() {
//        return protein;
//    }
//    
//    public void assignProtein(String proteinID){
//        protein.add(proteinID);
//    }
    /**
     * Constructor of PeptideSequenceData.
     *
     * @param sequence peptide sequence.
     */
    public PeptideSequenceData(String sequence) {
        this.sequence = sequence;
        peptides = new HashMap<>();
    }

    /**
     * Get peptide sequence.
     *
     * @return sequence.
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Get collection of PeptideData.
     *
     * @return collection of PeptideData.
     */
    public Collection<PeptideData> getPeptides() {
        return peptides.values();
    }

    /**
     * Add PeptideData to this class.
     *
     * @param peptide new PeptideData.
     */
    public void addPeptideData(PeptideData peptide) {
        String modStr = peptide.getModString();
        if (peptides.containsKey(modStr)) {
            peptides.get(modStr).mergeAnotherPeptideData(peptide);
        } else {
            peptides.put(modStr, peptide);
        }
    }

}
