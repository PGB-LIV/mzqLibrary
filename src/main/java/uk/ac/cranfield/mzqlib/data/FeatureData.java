
package uk.ac.cranfield.mzqlib.data;

import uk.ac.liv.pgb.jmzqml.model.mzqml.Feature;

/**
 * FeatureData contains all the data about Feature.
 *
 * @author Jun Fan@cranfield
 */
public class FeatureData extends QuantitationLevel {
//    /**
//     * Peptide ID, normally in the form of peptideSeq_modificationString
//     */
//    private String peptideID;

    private final Feature feature;
    private String rawFilesGroupRef;

    /**
     * Get RawFilesGroupRef.
     *
     * @return rawFilesGroupRef.
     */
    public String getRawFilesGroupRef() {
        return rawFilesGroupRef;
    }

    /**
     * Set RawFilesGroupRef.
     *
     * @param rawFilesGroupRef rawFilesGroupRef.
     */
    public void setRawFilesGroupRef(final String rawFilesGroupRef) {
        this.rawFilesGroupRef = rawFilesGroupRef;
    }

    /**
     * Constructor.
     *
     * @param feature Feature.
     */
    public FeatureData(final Feature feature) {
        this.feature = feature;
    }

    /**
     * Get Feature.
     *
     * @return Feature.
     */
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
    /**
     * Get feature id.
     *
     * @return feature id.
     */
    public String getId() {
        return feature.getId();
    }

    /**
     * Get feature count.
     *
     * @return feature count.
     */
    @Override
    public int getCount() {
        return 1;
    }

}
