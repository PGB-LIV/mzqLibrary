package uk.ac.liv.pgb.mzqlib.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;

import uk.ac.liv.pgb.jmzqml.model.mzqml.Column;
import uk.ac.liv.pgb.jmzqml.model.mzqml.GlobalQuantLayer;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.pgb.mzqlib.constants.MzqDataConstants;

/**
 *
 * @author Da Qi
 * @since 26-Jan-2015 15:25:29
 */
public class MzqFeatureQuantLayer extends MzqQuantLayer {

    /**
     * Constructor of MzqFeatureQuantLayer.
     *
     * @param um           the input MzQuantMLUnmarshaller
     * @param listId       the specific id of list
     * @param ftQuantLayer the GlobalQuantLayer
     */
    public MzqFeatureQuantLayer(final MzQuantMLUnmarshaller um, final String listId,
                                final GlobalQuantLayer ftQuantLayer) {
        setMzqUm(um);
        setQuantLayerType(new SimpleStringProperty("FeatureQuantLayer"));
        setListId(new SimpleStringProperty(listId));
        setQuantLayerId(new SimpleStringProperty(ftQuantLayer.getId()));
        setListType(new SimpleStringProperty(MzqDataConstants.FEATURE_LIST_TYPE));
        setDataType(new SimpleStringProperty("List in column titles"));

        // set column names
        setColumnNames(new ArrayList<>());

        List<Column> columns = ftQuantLayer.getColumnDefinition().getColumn();

        for (Column col : columns) {
            String name = col.getDataType().getCvParam().getName();

            getColumnNames().add(new SimpleStringProperty(name));
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
