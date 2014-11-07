package uk.ac.liv.mzqlib.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 12-Sep-2014 11:16:22
 */
public class HeatMapParam {

    private double min;
    private double max;
    private double logMin;
    private double logMax;
    private int rowNumber;
    private String matrix;
    private String logMatrix;
    private List<String> rowNames;
    private List<String> colNames;

    /**
     * Constructor of HeatMapParam
     *
     * @param min    minimum value of the matrix for heat map
     * @param max    maximum value of the matrix for heat map
     * @param number number of entities
     * @param x      the String used to create an R matrix
     * @param rowN   the list of row names of data.frame
     * @param colN   the list of column names of data.frame
     */
    public HeatMapParam(double min, double max, int number, String x,
                        List<String> rowN, List<String> colN) {
        this.min = min;
        this.max = max;
        this.rowNumber = number;
        this.matrix = x;
        this.rowNames = new ArrayList<>(rowN);
        this.colNames = new ArrayList<>(colN);
    }

    public HeatMapParam() {
        this(0, 0, 0, "", new ArrayList<>(), new ArrayList<>());
    }

    /**
     * @return the min
     */
    public double getMin() {
        return min;
    }

    /**
     * @param min the min to set
     */
    public void setMin(double min) {
        this.min = min;
    }

    /**
     * @return the max
     */
    public double getMax() {
        return max;
    }

    /**
     * @param max the max to set
     */
    public void setMax(double max) {
        this.max = max;
    }

    /**
     * @return the rowNumber
     */
    public int getRowNumber() {
        return rowNumber;
    }

    /**
     * @param rowNumber the rowNumber to set
     */
    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    /**
     * @return the matrix
     */
    public String getMatrix() {
        return matrix;
    }

    /**
     * @param matrix the matrix to set
     */
    public void setMatrix(String matrix) {
        this.matrix = matrix;
    }

    /**
     * @return the rowNames
     */
    public List<String> getRowNames() {
        return rowNames;
    }

    /**
     * @param rowNames the rowNames to set
     */
    public void setRowNames(List<String> rowNames) {
        this.rowNames = rowNames;
    }

    /**
     * @return the colNames
     */
    public List<String> getColNames() {
        return colNames;
    }

    /**
     * @param colNames the colNames to set
     */
    public void setColNames(List<String> colNames) {
        this.colNames = colNames;
    }

    /**
     * @return the logMatrix
     */
    public String getLogMatrix() {
        return logMatrix;
    }

    /**
     * @param logMatrix the logMatrix to set
     */
    public void setLogMatrix(String logMatrix) {
        this.logMatrix = logMatrix;
    }

    /**
     * @return the logMin
     */
    public double getLogMin() {
        return logMin;
    }

    /**
     * @param logMin the logMin to set
     */
    public void setLogMin(double logMin) {
        this.logMin = logMin;
    }

    /**
     * @return the logMax
     */
    public double getLogMax() {
        return logMax;
    }

    /**
     * @param logMax the logMax to set
     */
    public void setLogMax(double logMax) {
        this.logMax = logMax;
    }

}
