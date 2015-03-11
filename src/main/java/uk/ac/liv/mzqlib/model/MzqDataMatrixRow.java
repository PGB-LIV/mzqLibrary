
package uk.ac.liv.mzqlib.model;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 29-Jul-2014 12:34:12
 */
public class MzqDataMatrixRow {

    private StringProperty objectId;
    private StringProperty objectValue; // Sequence or accession
    private List<StringProperty> values;

    /**
     * Constructor of MzqDataMatrixRow.
     */
    public MzqDataMatrixRow() {
        objectId = new SimpleStringProperty("");
        objectValue = new SimpleStringProperty("");
        values = FXCollections.observableArrayList();
    }

    /**
     * Get object id as StringProperty.
     *
     * @return the objectId
     */
    public StringProperty ObjectId() {
        return objectId;
    }

    /**
     * Get object id as String.
     *
     * @return the value of objectId
     */
    public String getObjectId() {
        return objectId.get();
    }

    /**
     * Set the list of values.
     *
     * @return the values
     */
    public List<StringProperty> Values() {
        return values;
    }

    /**
     * Get value from specified position.
     *
     * @param i the position of the value
     *
     * @return the value of specific position in the list
     */
    public StringProperty Value(int i) {
        return values.get(i);
    }

    /**
     * Set object id.
     *
     * @param objectId the objectId to set
     */
    public void setObjectId(StringProperty objectId) {
        this.objectId = objectId;
    }

    /**
     * Set list of values.
     *
     * @param values the values to set
     */
    public void setValues(
            List<String> values) {
        for (String value : values) {
            this.values.add(new SimpleStringProperty(value));
        }
    }

    /**
     * Get object value as StringProperty.
     *
     * @return the objectValue
     */
    public StringProperty getObjectValue() {
        return objectValue;
    }

    /**
     * Set object value.
     *
     * @param objectValue the objectValue to set
     */
    public void setObjectValue(StringProperty objectValue) {
        this.objectValue = objectValue;
    }

}
