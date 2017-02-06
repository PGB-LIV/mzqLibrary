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
 * @since 18-Jul-2014 02:13:36
 */
public class MzQuantMLSummary {
    private final IntegerProperty      proteinListNumber;
    private final List<StringProperty> techniquesUsed;
    private final IntegerProperty      proteinGroupListNumber;
    private final IntegerProperty      peptideListNumber;
    private final IntegerProperty      featureListNumber;
    private StringProperty             software;

    /**
     * Constructor of MzQuantMLSummary.
     */
    public MzQuantMLSummary() {
        this.proteinGroupListNumber = new SimpleIntegerProperty();
        this.techniquesUsed         = new ArrayList<>();
        this.proteinListNumber      = new SimpleIntegerProperty();
        this.peptideListNumber      = new SimpleIntegerProperty();
        this.featureListNumber      = new SimpleIntegerProperty();
        this.software               = new SimpleStringProperty();
    }

    /**
     * Get feature list number as IntegerProperty.
     *
     * @return feature list number
     */
    public final IntegerProperty featureListNumber() {
        return featureListNumber;
    }

    /**
     * Get peptide list number as IntegerProperty.
     *
     * @return peptide list number
     */
    public final IntegerProperty peptideListNumber() {
        return peptideListNumber;
    }

    /**
     * Get protein group list number as IntegerProperty.
     *
     * @return IntegerProperty
     */
    public final IntegerProperty proteinGroupListNumber() {
        return proteinGroupListNumber;
    }

    /**
     * Get protein list number as IntegerProperty.
     *
     * @return protein list number
     */
    public final IntegerProperty proteinListNumber() {
        return proteinListNumber;
    }

    /**
     * Get feature list number as int.
     *
     * @return feature list number
     */
    public final int getFeatureListNumber() {
        return featureListNumber.get();
    }

    /**
     * Set feature list number.
     *
     * @param num feature list number
     */
    public final void setFeatureListNumber(final int num) {
        this.featureListNumber.set(num);
    }

    /**
     * Get peptide list number as int.
     *
     * @return peptide list number
     */
    public final int getPeptideListNumber() {
        return peptideListNumber.get();
    }

    /**
     * Set peptide list number.
     *
     * @param num peptide list number
     */
    public final void setPeptideListNumber(final int num) {
        this.peptideListNumber.set(num);
    }

    /**
     * Get protein group list number as int.
     *
     * @return protein group list number
     */
    public final int getProteinGroupListNumber() {
        return proteinGroupListNumber.get();
    }

    /**
     * Set protein group list number.
     *
     * @param num protein group list number
     */
    public final void setProteinGroupListNumber(final int num) {
        this.proteinGroupListNumber.set(num);
    }

    /**
     * Get protein list number as int.
     *
     * @return protein list number.
     */
    public final int getProteinListNumber() {
        return proteinListNumber.get();
    }

    /**
     * Set protein list number.
     *
     * @param num protein list number
     */
    public final void setProteinListNumber(final int num) {
        this.proteinListNumber.set(num);
    }

    /**
     * Get software.
     *
     * @return the software
     */
    public final StringProperty getSoftware() {
        return software;
    }

    /**
     * Set software value.
     *
     * @param software the software to set
     */
    public final void setSoftware(final StringProperty software) {
        this.software = software;
    }

    /**
     * Set list of technique used in mzQuantML file.
     *
     * @param techList list of techniques.
     */
    public final void setTechniquesUsed(final List<StringProperty> techList) {
        this.techniquesUsed.clear();
        this.techniquesUsed.addAll(techList);
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
