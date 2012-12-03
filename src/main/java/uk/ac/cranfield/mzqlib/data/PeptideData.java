package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;

/**
 *
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class PeptideData extends QuantitationLevel {
    private String seq;
    PeptideConsensus peptide;
    private String peptideID;
    /**
     * features are rawFilesGroup specific, which is corresponding to msrun 
     * so suitable for a hash map: keys are msrun id and values are the list of features
     */
    private HashMap<String,ArrayList<FeatureData>> features;

    public PeptideData(PeptideConsensus pc) {
        peptide = pc;
    }
    
    /**
     * Get the peptide sequence
     * @return 
     */
    public String getSeq() {
        return peptide.getPeptideSequence();
    }
    /**
     * Get all features for a specific msrun
     * @param msrun
     * @return 
     */
    public ArrayList<FeatureData> getFeatures(String msrun) {
        if(features.containsKey(msrun)){
            return features.get(msrun);
        }
        return null;
    }
    /**
     * Get all features from all runs
     * @return 
     */
    public ArrayList<FeatureData> getAllFeatures(){
        ArrayList<FeatureData> ret = new ArrayList<FeatureData>();
        for(ArrayList<FeatureData> one:features.values()){
            ret.addAll(one);
        }
        return ret;
    }

    @Override
    public int getCount(){
        return getAllFeatures().size();
    }
}
