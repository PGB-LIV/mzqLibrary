/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
    private final StringProperty quantLayerId;
    private final StringProperty quantLayerType = new SimpleStringProperty("AssayQuantLayer");
    private final ObjectProperty<QuantLayer> quantLayer;
    private final StringProperty rowObjectType; //a flag to indicate the type of object in the row, e.g. ProteinGroup, Protein, PeptideConsensus, etc.

    /**
     *
     * @param um         the unmarshaller of the mzq file
     * @param listId     the id of the list element (e.g. ProteinGroupList, ProteinList, PeptideConsensusList, etc.
     * @param quantLayer the QuantLayer object
     * @param objectType the object type of rows (e.g. Protein, ProteinGroup, etc.)
     */
    public MzqAssayQuantLayer(MzQuantMLUnmarshaller um, String listId,
                              QuantLayer quantLayer, String objectType) {
        this.mzqUm = um;
        this.listId = new SimpleStringProperty(listId);
        this.quantLayer = new SimpleObjectProperty<>(quantLayer);
        this.quantLayerId = new SimpleStringProperty(quantLayer.getId());
        this.rowObjectType = new SimpleStringProperty(objectType);
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
     * @return the value of quantLayer
     */
    public QuantLayer getQuantLayer() {
        return quantLayer.get();
    }

    /**
     *
     * @return the quantLayer
     */
    public ObjectProperty<QuantLayer> quantLayer() {
        return quantLayer;
    }

    /**
     *
     * @return the value of rowObjectType
     */
    public String getRowObjectType() {
        return rowObjectType.get();
    }

    /**
     * @return the rowObjectType
     */
    public StringProperty rowObjectType() {
        return rowObjectType;
    }

}
