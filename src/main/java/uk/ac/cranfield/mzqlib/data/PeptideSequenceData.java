/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cranfield.mzqlib.data;

import java.util.Collection;
import java.util.HashMap;

/**
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

    public PeptideSequenceData(String sequence) {
        this.sequence = sequence;
        peptides = new HashMap<String, PeptideData>();
    }

    public String getSequence() {
        return sequence;
    }
    
    public Collection<PeptideData> getPeptides(){
        return peptides.values();
    }
    
    public void addPeptideData(PeptideData peptide){
        String modStr = peptide.getModString();
        if(peptides.containsKey(modStr)){
            peptides.get(modStr).mergeAnotherPeptideData(peptide);
        }else{
            peptides.put(modStr, peptide);
        }
    }
}
