package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.liv.jmzqml.model.mzqml.EvidenceRef;
import uk.ac.liv.jmzqml.model.mzqml.Modification;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;

/**
 *
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class PeptideData extends QuantitationLevel {
    private PeptideConsensus peptide;
//    private String peptideID;
    private String modStr = "";
    /**
     * features are rawFilesGroup specific, which is corresponding to msrun 
     * so suitable for a hash map: keys are msrun id and values are the list of features
     */
//    private HashMap<String,ArrayList<FeatureData>> features;
    private ArrayList<FeatureData> features = new ArrayList<FeatureData>();
    private boolean assignedByPeptideRef = false;

    public boolean isAssignedByPeptideRef() {
        return assignedByPeptideRef;
    }

    public void setAssignedByPeptideRef(boolean assignedByPeptideRef) {
        this.assignedByPeptideRef = assignedByPeptideRef;
    }

    public PeptideData(PeptideConsensus pc) {
        peptide = pc;
//        ArrayList<Character> modIndice = new ArrayList<Character>();
        ArrayList<Integer> modIndice = new ArrayList<Integer>();
        for (Modification mod:pc.getModification()){
            int index = MzqLib.data.getModificationIndex(mod);
            modIndice.add(index);
        }
        Integer[] arr = new Integer[modIndice.size()];
        modIndice.toArray(arr);
        Arrays.sort(arr);
        modStr = Arrays.toString(arr);
    }
    /**
     * Get the peptide sequence
     * @return 
     */
    public String getSeq() {
        return peptide.getPeptideSequence();
    }
    
    public String getId(){
        return peptide.getId();
    }
    
    public PeptideConsensus getPeptide(){
        return peptide;
    }

    public ArrayList<FeatureData> getFeatures() {
        return features;
    }
    
    public void mergeAnotherPeptideData(PeptideData another){
        this.features.addAll(another.getFeatures());
        if(!assignedByPeptideRef) setAssignedByPeptideRef(another.isAssignedByPeptideRef());
        this.peptide.getEvidenceRef().addAll(another.getPeptide().getEvidenceRef());
        //TODO more things need to be added here when merging two or more mzq files, e.g. searchDatabaseRef
    }
    
    public String getModString() {
        return modStr;
    }
    
    
//    /**
//     * Get all features for a specific msrun
//     * @param msrun
//     * @return 
//     */
//    public ArrayList<FeatureData> getFeatures(String msrun) {
//        if(features.containsKey(msrun)){
//            return features.get(msrun);
//        }
//        return null;
//    }
//    /**
//     * Get all features from all runs
//     * @return 
//     */
//    public ArrayList<FeatureData> getAllFeatures(){
//        ArrayList<FeatureData> ret = new ArrayList<FeatureData>();
//        for(ArrayList<FeatureData> one:features.values()){
//            ret.addAll(one);
//        }
//        return ret;
//    }

    @Override
    public int getCount(){
//        return getAllFeatures().size();
        return features.size();
    }

    void addFeature(FeatureData feature) {
        features.add(feature);
    }
}
