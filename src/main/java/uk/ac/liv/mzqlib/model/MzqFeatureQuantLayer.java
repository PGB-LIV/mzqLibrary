
package uk.ac.liv.mzqlib.model;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Column;
import uk.ac.liv.pgb.jmzqml.model.mzqml.GlobalQuantLayer;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.mzqlib.constants.MzqDataConstants;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 26-Jan-2015 15:25:29
 */
public class MzqFeatureQuantLayer extends MzqQuantLayer {

    /**
     * Constructor of MzqFeatureQuantLayer.
     *
     * @param um           the input MzQuantMLUnmarshaller
     * @param listId       the specific id of list
     * @param ftQuantLayer the GlobalQuantLayer
     */
    public MzqFeatureQuantLayer(MzQuantMLUnmarshaller um, String listId,
                                GlobalQuantLayer ftQuantLayer) {

        this.mzqUm = um;
        this.quantLayerType = new SimpleStringProperty("FeatureQuantLayer");
        this.listId = new SimpleStringProperty(listId);
        this.quantLayerId = new SimpleStringProperty(ftQuantLayer.getId());
        this.listType = new SimpleStringProperty(MzqDataConstants.FEATURE_LIST_TYPE);
        this.dataType = new SimpleStringProperty("List in column titles");

        //set column names
        this.columnNames = new ArrayList<>();
        List<Column> columns = ftQuantLayer.getColumnDefinition().getColumn();
        for (Column col : columns) {
            String name = col.getDataType().getCvParam().getName();
            this.columnNames.add(new SimpleStringProperty(name));
        }
    }

}
