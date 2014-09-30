/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.liv.mzqlib.model;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 18-Jul-2014 02:13:36
 */
public class MzQuantMLSummary {

    private final IntegerProperty proteinListNumber;
    private final List<StringProperty> techniquesUsed;
    private final IntegerProperty proteinGroupListNumber;
    private final IntegerProperty peptideListNumber;
    private final IntegerProperty featureListNumber;
    private StringProperty software;

    public MzQuantMLSummary() {
        this.proteinGroupListNumber = new SimpleIntegerProperty();
        this.techniquesUsed = new ArrayList<>();
        this.proteinListNumber = new SimpleIntegerProperty();
        this.peptideListNumber = new SimpleIntegerProperty();
        this.featureListNumber = new SimpleIntegerProperty();
        this.software = new SimpleStringProperty();
    }

    public void setTechniquesUsed(List<StringProperty> techList) {
        this.techniquesUsed.clear();
        this.techniquesUsed.addAll(techList);
    }

    public IntegerProperty proteinGroupListNumber() {
        return proteinGroupListNumber;
    }

    public int getProteinGroupListNumber() {
        return proteinGroupListNumber.get();
    }

    public void setProteinGroupListNumber(int num) {
        this.proteinGroupListNumber.set(num);
    }

    public IntegerProperty proteinListNumber() {
        return proteinListNumber;
    }

    public int getProteinListNumber() {
        return proteinListNumber.get();
    }

    public void setProteinListNumber(int num) {
        this.proteinListNumber.set(num);
    }

    public IntegerProperty peptideListNumber() {
        return peptideListNumber;
    }

    public int getPeptideListNumber() {
        return peptideListNumber.get();
    }

    public void setPeptideListNumber(int num) {
        this.peptideListNumber.set(num);
    }

    public IntegerProperty featureListNumber() {
        return featureListNumber;
    }

    public int getFeatureListNumber() {
        return featureListNumber.get();
    }

    public void setFeatureListNumber(int num) {
        this.featureListNumber.set(num);
    }

    /**
     * @return the software
     */
    public StringProperty getSoftware() {
        return software;
    }

    /**
     * @param software the software to set
     */
    public void setSoftware(StringProperty software) {
        this.software = software;
    }

}
