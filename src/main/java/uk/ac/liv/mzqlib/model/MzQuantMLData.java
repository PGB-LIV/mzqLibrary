
package uk.ac.liv.mzqlib.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 10-Sep-2014 12:24:23
 */
public class MzQuantMLData {

    private final MzQuantMLSummary mzQuantMLSummary = new MzQuantMLSummary();
    private final ObservableList<MzqAssayQuantLayer> mzqAssayQuantLayerList = FXCollections.observableArrayList();
    private MzQuantMLUnmarshaller mzQuantMLUnmarshaller;

    public MzQuantMLData() {

    }

    public void setMzQuantMLSummary(MzQuantMLSummary mzqSum) {
        mzQuantMLSummary.setProteinGroupListNumber(mzqSum.getProteinGroupListNumber());
        mzQuantMLSummary.setProteinListNumber(mzqSum.getProteinListNumber());
        mzQuantMLSummary.setPeptideListNumber(mzqSum.getPeptideListNumber());
        mzQuantMLSummary.setFeatureListNumber(mzqSum.getFeatureListNumber());
    }

    public void setMzqQuantLayerList(
            ObservableList<MzqAssayQuantLayer> mzqAQLList) {
        mzqAssayQuantLayerList.clear();
        mzqAssayQuantLayerList.addAll(mzqAQLList);
    }

    public MzQuantMLSummary getMzQuantMLSummary() {
        return mzQuantMLSummary;
    }

    public ObservableList<MzqAssayQuantLayer> getMzqAssayQuantLayerList() {
        return mzqAssayQuantLayerList;
    }

    public void setMzQuantMLUnmarshaller(MzQuantMLUnmarshaller um) {
        mzQuantMLUnmarshaller = um;
    }

    public MzQuantMLUnmarshaller getMzQuantMLUnmarshaller() {
        return mzQuantMLUnmarshaller;
    }

}
