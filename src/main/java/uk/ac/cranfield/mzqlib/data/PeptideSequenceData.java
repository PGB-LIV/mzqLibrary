/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class PeptideSequenceData {
    private String sequence;
//    private
    private ArrayList<String> protein = new ArrayList<String>();
    private HashMap<String, PeptideData> peptides = new HashMap<String, PeptideData>();

    public ArrayList<String> getProtein() {
        return protein;
    }
    
    public void assignProtein(String proteinID){
        protein.add(proteinID);
    }
    public PeptideSequenceData(String sequence) {
        this.sequence = sequence;
    }

    public String getSequence() {
        return sequence;
    }
}
