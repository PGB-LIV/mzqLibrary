
package uk.ac.liv.pgb.mzqlib.model;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.StringProperty;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 26-Jan-2015 15:34:59
 */
public class MzqQuantLayer {

    /**
     * The input MzQuantMLUnmarshaller.
     */
    protected MzQuantMLUnmarshaller mzqUm;

    /**
     * The list id of the list which contains the QuantLayer.
     */
    protected StringProperty listId;

    /**
     * The data type of the QuantLayer from CV param.
     */
    protected StringProperty dataType;

    /**
     * The id of the QuantLayer.
     */
    protected StringProperty quantLayerId;

    /**
     * The type of QuantLayer, e.g AssayQuantLayer, GlobalQuantLayer, etc.
     */
    protected StringProperty quantLayerType;

    /**
     * The type of the list, e.g ProteinList, PeptideConsensusList, etc.
     */
    protected StringProperty listType;

    /**
     * The list of the names of QuantLayer columns.
     */
    protected List<StringProperty> columnNames;

    /**
     * The list of MzqDataMatrixRow of the QuantLayer.
     */
    protected List<MzqDataMatrixRow> dmRows;

    /**
     * Get the id of the list as String.
     *
     * @return the value of listId
     */
    public String getListId() {
        return listId.get();
    }

    /**
     * Get the id of the list as StringProperty.
     *
     * @return the listId
     */
    public StringProperty listId() {
        return listId;
    }

    /**
     * Get the id of the QuantLayer as String.
     *
     * @return the value of quantLayerId
     */
    public String getQuantLayerId() {
        return quantLayerId.get();
    }

    /**
     * Get the id of the QuantLayer as StringProperty.
     *
     * @return the quantLayerId
     */
    public StringProperty quantLayerId() {
        return quantLayerId;
    }

    /**
     * Get type of QuantLayer as String.
     *
     * @return the value of quantLayerType
     */
    public String getQuantLayerType() {
        return quantLayerType.get();
    }

    /**
     * Get type of QuantLayer as StringProperty.
     *
     * @return the quantLayerType
     */
    public StringProperty quantLayerType() {
        return quantLayerType;
    }

    /**
     * Get type of the list as String.
     *
     * @return the value of listType
     */
    public String getListType() {
        return listType.get();
    }

    /**
     * Get type of the list as StringProperty.
     *
     * @return the listType
     */
    public StringProperty listType() {
        return listType;
    }

    /**
     * Get input MzQuantMLUnmarshaller.
     *
     * @return the mzqUm
     */
    public MzQuantMLUnmarshaller getMzqUm() {
        return mzqUm;
    }

    /**
     * Get data type value of the QuantLayer as String.
     *
     * @return the value of dataType
     */
    public String getDataType() {
        return dataType.get();
    }

    /**
     * Get data type of the QuantLayer as StringProperty.
     *
     * @return the dataType
     */
    public StringProperty dataType() {
        return dataType;
    }

    /**
     * Get list of column names.
     *
     * @return the columnNames
     */
    public List<StringProperty> getColumnNames() {
        if (columnNames == null) {
            columnNames = new ArrayList<>();
        }
        return columnNames;
    }

    /**
     * Set list of column names.
     *
     * @param columnNames the columnNames to set
     */
    public void setColumnNames(
            List<StringProperty> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * Get list of MzqDataMatrixRow.
     *
     * @return the dmRows
     */
    public List<MzqDataMatrixRow> getDmRows() {
        if (dmRows == null) {
            dmRows = new ArrayList<>();
        }
        return dmRows;
    }

    /**
     * Set list of MzqDataMatrixRow.
     *
     * @param dmRows the dmRows to set
     */
    public void setDmRows(
            List<MzqDataMatrixRow> dmRows) {
        this.dmRows = dmRows;
    }

}
