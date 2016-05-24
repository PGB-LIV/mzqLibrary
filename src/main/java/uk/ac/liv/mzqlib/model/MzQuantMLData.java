
package uk.ac.liv.mzqlib.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * Wrapper class from MzQuantML class.
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 10-Sep-2014 12:24:23
 */
public class MzQuantMLData {

    private final MzQuantMLSummary mzQuantMLSummary = new MzQuantMLSummary();
    private final ObservableList<MzqAssayQuantLayer> mzqAssayQuantLayerList
            = FXCollections.observableArrayList();
    private final ObservableList<MzqFeatureQuantLayer> mzqFeatureQuantLayerList
            = FXCollections.observableArrayList();
    private MzQuantMLUnmarshaller mzQuantMLUnmarshaller;

    /**
     * Constructor of MzQuantMLData.
     */
    public MzQuantMLData() {

    }

    /**
     * Set MzQuantMLSummary value.
     *
     * @param mzqSum MzQuantMLSummary
     */
    public void setMzQuantMLSummary(MzQuantMLSummary mzqSum) {
        mzQuantMLSummary.setProteinGroupListNumber(mzqSum.
                getProteinGroupListNumber());
        mzQuantMLSummary.setProteinListNumber(mzqSum.getProteinListNumber());
        mzQuantMLSummary.setPeptideListNumber(mzqSum.getPeptideListNumber());
        mzQuantMLSummary.setFeatureListNumber(mzqSum.getFeatureListNumber());
        mzQuantMLSummary.setSoftware(mzqSum.getSoftware());
    }

    /**
     * Set list of MzqAssayQuantLayer.
     *
     * @param mzqAQLList ObservableList<MzqAssayQuantLayer>
     */
    public void setMzqAssayQuantLayerList(
            ObservableList<MzqAssayQuantLayer> mzqAQLList) {
        mzqAssayQuantLayerList.clear();
        mzqAssayQuantLayerList.addAll(mzqAQLList);
    }

    /**
     * Get MzQuantMLSummary.
     *
     * @return MzQuantMLSummary
     */
    public MzQuantMLSummary getMzQuantMLSummary() {
        return mzQuantMLSummary;
    }

    /**
     * Get list of MzqAssayQuantLayer.
     *
     * @return ObservableList<MzqAssayQuantLayer>
     */
    public ObservableList<MzqAssayQuantLayer> getMzqAssayQuantLayerList() {
        return mzqAssayQuantLayerList;
    }

    /**
     * Set MzQuantMLUnmarshaller value.
     *
     * @param um MzQuantMLUnmarshaller
     */
    public void setMzQuantMLUnmarshaller(MzQuantMLUnmarshaller um) {
        mzQuantMLUnmarshaller = um;
    }

    /**
     * Get MzQuantMLUnmarshaller.
     *
     * @return MzQuantMLUnmarshaller
     */
    public MzQuantMLUnmarshaller getMzQuantMLUnmarshaller() {
        return mzQuantMLUnmarshaller;
    }

    /**
     * Get list of MzqFeatureQuantLayer.
     *
     * @return ObservableList<MzqFeatureQuantLayer>
     */
    public ObservableList<MzqFeatureQuantLayer> getMzqFeatureQuantLayerList() {
        return mzqFeatureQuantLayerList;
    }

    /**
     * Set list of MzqFeatureQuantLayer.
     *
     * @param mzqFeatureQuantLayerList ObservableList<MzqFeatureQuantLayer>
     */
    public void setMzqFeatureQuantLayerList(
            ObservableList<MzqFeatureQuantLayer> mzqFeatureQuantLayerList) {
        this.mzqFeatureQuantLayerList.clear();
        this.mzqFeatureQuantLayerList.addAll(mzqFeatureQuantLayerList);
    }

}
