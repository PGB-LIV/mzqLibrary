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
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.AnalysisSummary;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

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

    public MzQuantMLSummary(MzQuantMLUnmarshaller um) {
        // Set techniquesUsed
        AnalysisSummary analySum = um.unmarshal(MzQuantMLElement.AnalysisSummary);
        List<CvParam> cvParams = analySum.getCvParam();
        this.techniquesUsed = new ArrayList<>();
        for (CvParam cp : cvParams) {
            if (!cp.getName().contains("level")) {
                this.techniquesUsed.add(new SimpleStringProperty(cp.getName()));
            }
        }

        int listNumber = 0;
        // Set numbers of protein group list   
        listNumber = um.getObjectCountForXpath("/MzQuantML/ProteinGroupList");
        this.proteinGroupListNumber = new SimpleIntegerProperty(listNumber == -1 ? 0 : listNumber);

        // Set numbers of protein list
        listNumber = um.getObjectCountForXpath("/MzQuantML/ProteinList");
        this.proteinListNumber = new SimpleIntegerProperty(listNumber == -1 ? 0 : listNumber);

        // Set numbers of peptide list
        listNumber = um.getObjectCountForXpath("/MzQuantML/PeptideConsensusList");
        this.peptideListNumber = new SimpleIntegerProperty(listNumber == -1 ? 0 : listNumber);

        // Set numbers of feature list
        listNumber = um.getObjectCountForXpath("/MzQuantML/FeatureList");
        this.featureListNumber = new SimpleIntegerProperty(listNumber == -1 ? 0 : listNumber);
    }

    public IntegerProperty proteinGroupListNumber() {
        return proteinGroupListNumber;
    }

    public int getProteinGroupListNumber() {
        return proteinGroupListNumber.get();
    }

    public IntegerProperty proteinListNumber() {
        return proteinListNumber;
    }

    public int getProteinListNumber() {
        return proteinListNumber.get();
    }

    public IntegerProperty peptideListNumber() {
        return peptideListNumber;
    }

    public int getPeptideListNumber() {
        return peptideListNumber.get();
    }

    public IntegerProperty featureListNumber() {
        return featureListNumber;
    }

    public int getFeatureListNumber() {
        return featureListNumber.get();
    }

}
