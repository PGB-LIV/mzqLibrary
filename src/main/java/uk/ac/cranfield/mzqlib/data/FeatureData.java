package uk.ac.cranfield.mzqlib.data;

import uk.ac.liv.jmzqml.model.mzqml.Feature;

/**
 *
 * @author Jun Fan@cranfield
 */
public class FeatureData extends QuantitationLevel{
    /**
     * Peptide ID, normally in the form of peptideSeq_modificationString
     */
    private String peptideID;
    
    private Feature feature;
    
    public FeatureData(Feature feature){
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }

    public String getPeptideID() {
        return peptideID;
    }

    public void setPeptideID(String peptideID) {
        this.peptideID = peptideID;
    }
    
    @Override
    public int getCount(){
        return 1;
    }
}
