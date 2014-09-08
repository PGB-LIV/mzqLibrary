/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
    private List<StringProperty> values;

    public MzqDataMatrixRow() {
        objectId = new SimpleStringProperty("");
        values = FXCollections.observableArrayList();
    }

    /**
     * @return the objectId
     */
    public StringProperty ObjectId() {
        return objectId;
    }

    /**
     *
     * @return the value of objectId
     */
    public String getObjetId() {
        return objectId.get();
    }

    /**
     * @return the values
     */
    public List<StringProperty> Values() {
        return values;
    }

    /**
     *
     * @param i the position of the value
     *
     * @return the value of specific position in the list
     */
    public StringProperty Value(int i) {
        return values.get(i);
    }

    /**
     * @param objectId the objectId to set
     */
    public void setObjectId(StringProperty objectId) {
        this.objectId = objectId;
    }

    /**
     * @param values the values to set
     */
    public void setValues(
            List<String> values) {
        for (String value : values) {
            this.values.add(new SimpleStringProperty(value));
        }
    }

}
