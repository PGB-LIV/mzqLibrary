package uk.ac.liv.mzqlib.model;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.xml.bind.JAXBException;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 23-Jul-2014 15:16:04
 */
public class MzqAssayQuantLayer {

    private final MzQuantMLUnmarshaller mzqUm;
    private final StringProperty listId;
    private final StringProperty dataType;
    private final StringProperty quantLayerId;
    private final StringProperty quantLayerType = new SimpleStringProperty("AssayQuantLayer");
    //private final ObjectProperty<QuantLayer> quantLayer;
    private List<StringProperty> columnNames;
    private List<MzqDataMatrixRow> dmRows;
    private final StringProperty listType; //a flag to indicate the type of the list, e.g. ProteinGroupList, ProteinList, PeptideConsensusList, etc.

    /**
     *
     * @param um         the unmarshaller of the mzq file
     * @param listId     the id of the list element (e.g. ProteinGroupList, ProteinList, PeptideConsensusList, etc.
     * @param quantLayer the QuantLayer object
     * @param listType   the list type (e.g. Protein, ProteinGroup, etc.)
     * @param dataType   the data type of the quant layer
     *
     * @throws javax.xml.bind.JAXBException
     */
    public MzqAssayQuantLayer(MzQuantMLUnmarshaller um, String listId,
                              QuantLayer quantLayer,
                              String listType,
                              String dataType)
            throws JAXBException {
        this.mzqUm = um;
        this.listId = new SimpleStringProperty(listId);
        this.quantLayerId = new SimpleStringProperty(quantLayer.getId());
        this.listType = new SimpleStringProperty(listType);
        this.dataType = new SimpleStringProperty(dataType);

        //set column names 
        this.columnNames = new ArrayList<>();
        List<String> columnIndex = quantLayer.getColumnIndex();
        for (String colId : columnIndex) {
            Assay assay = mzqUm.unmarshal(uk.ac.liv.jmzqml.model.mzqml.Assay.class, colId);
            this.columnNames.add(new SimpleStringProperty(assay.getName()));
        }
    }

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
