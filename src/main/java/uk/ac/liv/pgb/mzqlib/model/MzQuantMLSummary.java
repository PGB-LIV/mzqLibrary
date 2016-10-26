
package uk.ac.liv.pgb.mzqlib.model;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Wrapper class of AnalysisSummary class.
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

    /**
     * Constructor of MzQuantMLSummary.
     */
    public MzQuantMLSummary() {
        this.proteinGroupListNumber = new SimpleIntegerProperty();
        this.techniquesUsed = new ArrayList<>();
        this.proteinListNumber = new SimpleIntegerProperty();
        this.peptideListNumber = new SimpleIntegerProperty();
        this.featureListNumber = new SimpleIntegerProperty();
        this.software = new SimpleStringProperty();
    }

    /**
     * Set list of technique used in mzQuantML file.
     *
     * @param List<StringProperty>
     */
    public void setTechniquesUsed(List<StringProperty> techList) {
        this.techniquesUsed.clear();
        this.techniquesUsed.addAll(techList);
    }

    /**
     * Get protein group list number as IntegerProperty.
     *
     * @return IntegerProperty
     */
    public IntegerProperty proteinGroupListNumber() {
        return proteinGroupListNumber;
    }

    /**
     * Get protein group list number as int.
     *
     * @return protein group list number
     */
    public int getProteinGroupListNumber() {
        return proteinGroupListNumber.get();
    }

    /**
     * Set protein group list number.
     *
     * @param num protein group list number
     */
    public void setProteinGroupListNumber(int num) {
        this.proteinGroupListNumber.set(num);
    }

    /**
     * Get protein list number as IntegerProperty.
     *
     * @return protein list number
     */
    public IntegerProperty proteinListNumber() {
        return proteinListNumber;
    }

    /**
     * Get protein list number as int.
     *
     * @return protein list number.
     */
    public int getProteinListNumber() {
        return proteinListNumber.get();
    }

    /**
     * Set protein list number.
     *
     * @param num protein list number
     */
    public void setProteinListNumber(int num) {
        this.proteinListNumber.set(num);
    }

    /**
     * Get peptide list number as IntegerProperty.
     *
     * @return peptide list number
     */
    public IntegerProperty peptideListNumber() {
        return peptideListNumber;
    }

    /**
     * Get peptide list number as int.
     *
     * @return peptide list number
     */
    public int getPeptideListNumber() {
        return peptideListNumber.get();
    }

    /**
     * Set peptide list number.
     *
     * @param num peptide list number
     */
    public void setPeptideListNumber(int num) {
        this.peptideListNumber.set(num);
    }

    /**
     * Get feature list number as IntegerProperty.
     *
     * @return feature list number
     */
    public IntegerProperty featureListNumber() {
        return featureListNumber;
    }

    /**
     * Get feature list number as int.
     *
     * @return feature list number
     */
    public int getFeatureListNumber() {
        return featureListNumber.get();
    }

    /**
     * Set feature list number.
     *
     * @param num feature list number
     */
    public void setFeatureListNumber(int num) {
        this.featureListNumber.set(num);
    }

    /**
     * Get software.
     *
     * @return the software
     */
    public StringProperty getSoftware() {
        return software;
    }

    /**
     * Set software value.
     *
     * @param software the software to set
     */
    public void setSoftware(StringProperty software) {
        this.software = software;
    }

}
