package uk.ac.cranfield.mzqlib.data;

import uk.ac.liv.pgb.jmzqml.model.mzqml.Feature;

/**
 *
 * @author Jun Fan@cranfield
 */
public class FeatureData extends QuantitationLevel{
//    /**
//     * Peptide ID, normally in the form of peptideSeq_modificationString
//     */
//    private String peptideID;
    
    private Feature feature;
    private String rawFilesGroupRef;

    public String getRawFilesGroupRef() {
        return rawFilesGroupRef;
    }

    public void setRawFilesGroupRef(String rawFilesGroupRef) {
        this.rawFilesGroupRef = rawFilesGroupRef;
    }
    
    public FeatureData(Feature feature){
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }

//    public String getPeptideID() {
//        return peptideID;
//    }
//
//    public void setPeptideID(String peptideID) {
//        this.peptideID = peptideID;
//    }
    
    public String getId(){
        return feature.getId();
    }
    
    @Override
    public int getCount(){
        return 1;
    }
}
