package uk.ac.liv.pgb.mzqlib.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.StringProperty;

import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * @author Da Qi
 * @since 26-Jan-2015 15:34:59
 */
public class MzqQuantLayer {

    /**
     * The input MzQuantMLUnmarshaller.
     */
    private MzQuantMLUnmarshaller mzqUm;

    /**
     * The list id of the list which contains the QuantLayer.
     */
    private StringProperty listId;

    /**
     * The data type of the QuantLayer from CV param.
     */
    private StringProperty dataType;

    /**
     * The id of the QuantLayer.
     */
    private StringProperty quantLayerId;

    /**
     * The type of QuantLayer, e.g AssayQuantLayer, GlobalQuantLayer, etc.
     */
    private StringProperty quantLayerType;

    /**
     * The type of the list, e.g ProteinList, PeptideConsensusList, etc.
     */
    private StringProperty listType;

    /**
     * The list of the names of QuantLayer columns.
     */
    private List<StringProperty> columnNames;

    /**
     * The list of MzqDataMatrixRow of the QuantLayer.
     */
    private List<MzqDataMatrixRow> dmRows;

    /**
     * Get data type of the QuantLayer as StringProperty.
     *
     * @return the dataType
     */
    public final StringProperty dataType() {
        return dataType;
    }

    /**
     * Get the id of the list as StringProperty.
     *
     * @return the listId
     */
    public final StringProperty listId() {
        return listId;
    }

    /**
     * Get type of the list as StringProperty.
     *
     * @return the listType
     */
    public final StringProperty listType() {
        return listType;
    }

    /**
     * Get the id of the QuantLayer as StringProperty.
     *
     * @return the quantLayerId
     */
    public final StringProperty quantLayerId() {
        return quantLayerId;
    }

    /**
     * Get type of QuantLayer as StringProperty.
     *
     * @return the quantLayerType
     */
    public final StringProperty quantLayerType() {
        return quantLayerType;
    }

    /**
     * Get list of column names.
     *
     * @return the columnNames
     */
    public final List<StringProperty> getColumnNames() {
        if (columnNames == null) {
            setColumnNames(new ArrayList<>());
        }

        return columnNames;
    }

    /**
     * Set list of column names.
     *
     * @param columnNames the columnNames to set
     */
    public final void setColumnNames(final List<StringProperty> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * Get data type value of the QuantLayer as String.
     *
     * @return the value of dataType
     */
    public final String getDataType() {
        return dataType.get();
    }

    /**
     * @param dataType the dataType to set
     */
    public final void setDataType(StringProperty dataType) {
        this.dataType = dataType;
    }

    /**
     * Get list of MzqDataMatrixRow.
     *
     * @return the dmRows
     */
    public final List<MzqDataMatrixRow> getDmRows() {
        if (dmRows == null) {
            setDmRows(new ArrayList<>());
        }

        return dmRows;
    }

    /**
     * Set list of MzqDataMatrixRow.
     *
     * @param dmRows the dmRows to set
     */
    public final void setDmRows(final List<MzqDataMatrixRow> dmRows) {
        this.dmRows = dmRows;
    }

    /**
     * Get the id of the list as String.
     *
     * @return the value of listId
     */
    public final String getListId() {
        return listId.get();
    }

    /**
     * @param listId the listId to set
     */
    public final void setListId(StringProperty listId) {
        this.listId = listId;
    }

    /**
     * Get type of the list as String.
     *
     * @return the value of listType
     */
    public final String getListType() {
        return listType.get();
    }

    /**
     * @param listType the listType to set
     */
    public final void setListType(StringProperty listType) {
        this.listType = listType;
    }

    /**
     * Get input MzQuantMLUnmarshaller.
     *
     * @return the mzqUm
     */
    public final MzQuantMLUnmarshaller getMzqUm() {
        return mzqUm;
    }

    /**
     * @param mzqUm the mzqUm to set
     */
    public final void setMzqUm(MzQuantMLUnmarshaller mzqUm) {
        this.mzqUm = mzqUm;
    }

    /**
     * Get the id of the QuantLayer as String.
     *
     * @return the value of quantLayerId
     */
    public final String getQuantLayerId() {
        return quantLayerId.get();
    }

    /**
     * @param quantLayerId the quantLayerId to set
     */
    public final void setQuantLayerId(StringProperty quantLayerId) {
        this.quantLayerId = quantLayerId;
    }

    /**
     * Get type of QuantLayer as String.
     *
     * @return the value of quantLayerType
     */
    public final String getQuantLayerType() {
        return quantLayerType.get();
    }

    /**
     * @param quantLayerType the quantLayerType to set
     */
    public final void setQuantLayerType(StringProperty quantLayerType) {
        this.quantLayerType = quantLayerType;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
