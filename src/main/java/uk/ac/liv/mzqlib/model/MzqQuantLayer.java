
package uk.ac.liv.mzqlib.model;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.StringProperty;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 26-Jan-2015 15:34:59
 */
public abstract class MzqQuantLayer {

    protected MzQuantMLUnmarshaller mzqUm;
    protected StringProperty listId;
    protected StringProperty dataType;
    protected StringProperty quantLayerId;
    protected StringProperty quantLayerType;
    protected StringProperty listType;
    protected List<StringProperty> columnNames;
    protected List<MzqDataMatrixRow> dmRows;

    /**
     * @return the value of listId
     */
    public String getListId() {
        return listId.get();
    }

    /**
     *
     * @return the listId
     */
    public StringProperty listId() {
        return listId;
    }

    /**
     * @return the value of quantLayerId
     */
    public String getQuantLayerId() {
        return quantLayerId.get();
    }

    /**
     *
     * @return the quantLayerId
     */
    public StringProperty quantLayerId() {
        return quantLayerId;
    }

    /**
     * @return the value of quantLayerType
     */
    public String getQuantLayerType() {
        return quantLayerType.get();
    }

    /**
     *
     * @return the quantLayerType
     */
    public StringProperty quantLayerType() {
        return quantLayerType;
    }

    /**
     *
     * @return the value of listType
     */
    public String getListType() {
        return listType.get();
    }

    /**
     * @return the listType
     */
    public StringProperty listType() {
        return listType;
    }

    /**
     * @return the mzqUm
     */
    public MzQuantMLUnmarshaller getMzqUm() {
        return mzqUm;
    }

    /**
     * @return the value of dataType
     */
    public String getDataType() {
        return dataType.get();
    }

    /**
     *
     * @return the dataType
     */
    public StringProperty dataType() {
        return dataType;
    }

    /**
     * @return the columnNames
     */
    public List<StringProperty> getColumnNames() {
        if (columnNames == null) {
            columnNames = new ArrayList<>();
        }
        return columnNames;
    }

    /**
     * @param columnNames the columnNames to set
     */
    public void setColumnNames(
            List<StringProperty> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * @return the dmRows
     */
    public List<MzqDataMatrixRow> getDmRows() {
        if (dmRows == null) {
            dmRows = new ArrayList<>();
        }
        return dmRows;
    }

    /**
     * @param dmRows the dmRows to set
     */
    public void setDmRows(
            List<MzqDataMatrixRow> dmRows) {
        this.dmRows = dmRows;
    }

}
