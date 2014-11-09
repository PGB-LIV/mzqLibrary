package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import java.util.Arrays;
import uk.ac.cranfield.mzqlib.MzqLib;
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
    private String modifications = "";
    /**
     * features are rawFilesGroup specific, which is corresponding to msrun 
     * so suitable for a hash map: keys are msrun id and values are the list of features
     */
//    private HashMap<String,ArrayList<FeatureData>> features;
    private ArrayList<FeatureData> features = new ArrayList<>();
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
        ArrayList<Integer> modIndice = new ArrayList<>();
        StringBuilder modSb = new StringBuilder();
        for (Modification mod:pc.getModification()){
            modSb.append(mod.getCvParam().get(0).getName());
            modSb.append(" ");
            int index = MzqLib.data.getModificationIndex(mod);
            modIndice.add(index);
        }
        if(modSb.length()>0) modSb.deleteCharAt(modSb.length()-1);
        modifications = modSb.toString();
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

    public String getModifications() {
        return modifications;
    }
    
    public PeptideConsensus getPeptide(){
        return peptide;
    }

    public ArrayList<FeatureData> getFeatures() {
        return features;
    }
    
    public ArrayList<FeatureData> getFeaturesWithCharge(int charge){
        ArrayList<FeatureData> values = new ArrayList<>();
        for(FeatureData feature:features){
            String value = feature.getFeature().getCharge();//<xsd:attribute name="charge" type="integerOrNullType" use="required">
            if(value!=null && !value.equalsIgnoreCase("null")){
                if(charge == Integer.parseInt(value)) {
                    values.add(feature);
                }
            }
        }
        return values;
    }
    
    public void mergeAnotherPeptideData(PeptideData another){
        this.features.addAll(another.getFeatures());
        if(!assignedByPeptideRef) {
            setAssignedByPeptideRef(another.isAssignedByPeptideRef());
        }
        this.peptide.getEvidenceRef().addAll(another.getPeptide().getEvidenceRef());
        //TODO more things need to be added here when merging two or more mzq files, e.g. searchDatabaseRef
    }
    
    public String getModString() {
        return modStr;
    }
    
    public int[] getCharges(){
        int[] charges = new int[peptide.getCharge().size()];
        for(int i=0;i<peptide.getCharge().size();i++){
            charges[i]=Integer.parseInt(peptide.getCharge().get(i));
        }
        return charges;
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
