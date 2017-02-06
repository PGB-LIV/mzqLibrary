package uk.ac.liv.pgb.mzqlib.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * Wrapper class from MzQuantML class.
 *
 * @author Da Qi
 * @since 10-Sep-2014 12:24:23
 */
public class MzQuantMLData {
    private final MzQuantMLSummary                     mzQuantMLSummary         = new MzQuantMLSummary();
    private final ObservableList<MzqAssayQuantLayer>   mzqAssayQuantLayerList   = FXCollections.observableArrayList();
    private final ObservableList<MzqFeatureQuantLayer> mzqFeatureQuantLayerList = FXCollections.observableArrayList();
    private MzQuantMLUnmarshaller                      mzQuantMLUnmarshaller;

    /**
     * Get MzQuantMLSummary.
     *
     * @return MzQuantMLSummary
     */
    public final MzQuantMLSummary getMzQuantMLSummary() {
        return mzQuantMLSummary;
    }

    /**
     * Set MzQuantMLSummary value.
     *
     * @param mzqSum MzQuantMLSummary
     */
    public final void setMzQuantMLSummary(final MzQuantMLSummary mzqSum) {
        mzQuantMLSummary.setProteinGroupListNumber(mzqSum.getProteinGroupListNumber());
        mzQuantMLSummary.setProteinListNumber(mzqSum.getProteinListNumber());
        mzQuantMLSummary.setPeptideListNumber(mzqSum.getPeptideListNumber());
        mzQuantMLSummary.setFeatureListNumber(mzqSum.getFeatureListNumber());
        mzQuantMLSummary.setSoftware(mzqSum.getSoftware());
    }

    /**
     * Get MzQuantMLUnmarshaller.
     *
     * @return MzQuantMLUnmarshaller
     */
    public final MzQuantMLUnmarshaller getMzQuantMLUnmarshaller() {
        return mzQuantMLUnmarshaller;
    }

    /**
     * Set MzQuantMLUnmarshaller value.
     *
     * @param um MzQuantMLUnmarshaller
     */
    public final void setMzQuantMLUnmarshaller(final MzQuantMLUnmarshaller um) {
        mzQuantMLUnmarshaller = um;
    }

    /**
     * Get list of MzqAssayQuantLayer.
     *
     * @return ObservableList&lt;MzqAssayQuantLayer&gt;
     */
    public final ObservableList<MzqAssayQuantLayer> getMzqAssayQuantLayerList() {
        return mzqAssayQuantLayerList;
    }

    /**
     * Set list of MzqAssayQuantLayer.
     *
     * @param mzqAQLList ObservableList&lt;MzqAssayQuantLayer&gt;
     */
    public final void setMzqAssayQuantLayerList(final ObservableList<MzqAssayQuantLayer> mzqAQLList) {
        mzqAssayQuantLayerList.clear();
        mzqAssayQuantLayerList.addAll(mzqAQLList);
    }

    /**
     * Get list of MzqFeatureQuantLayer.
     *
     * @return ObservableList&lt;MzqFeatureQuantLayer&gt;
     */
    public final ObservableList<MzqFeatureQuantLayer> getMzqFeatureQuantLayerList() {
        return mzqFeatureQuantLayerList;
    }

    /**
     * Set list of MzqFeatureQuantLayer.
     *
     * @param
     *                                 mzqFeatureQuantLayerList ObservableList&lt;MzqFeatureQuantLayer&gt;
     */
    public final void setMzqFeatureQuantLayerList(final ObservableList<MzqFeatureQuantLayer> mzqFeatureQuantLayerList) {
        this.mzqFeatureQuantLayerList.clear();
        this.mzqFeatureQuantLayerList.addAll(mzqFeatureQuantLayerList);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
