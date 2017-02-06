package uk.ac.liv.pgb.mzqlib.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The class to store the parameters for plotting heat map.
 *
 * @author Da Qi
 * @since 12-Sep-2014 11:16:22
 */
public class HeatMapParam {
    private double       min;
    private double       max;
    private double       logMin;
    private double       logMax;
    private int          rowNumber;
    private String       matrix;
    private String       logMatrix;
    private List<String> rowNames;
    private List<String> colNames;

    /**
     * Constructor of HeatMapParam.
     */
    public HeatMapParam() {
        this(0, 0, 0, "", new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Constructor of HeatMapParam.
     *
     * @param min    minimum value of the matrix for heat map
     * @param max    maximum value of the matrix for heat map
     * @param number number of entities
     * @param x      the String used to create an R matrix
     * @param rowN   the list of row names of data.frame
     * @param colN   the list of column names of data.frame
     */
    public HeatMapParam(final double min, final double max, final int number, final String x, final List<String> rowN,
                        final List<String> colN) {
        this.min       = min;
        this.max       = max;
        this.rowNumber = number;
        this.matrix    = x;
        this.rowNames  = new ArrayList<>(rowN);
        this.colNames  = new ArrayList<>(colN);
    }

    /**
     * Get the list of column names.
     *
     * @return the colNames
     */
    public final List<String> getColNames() {
        return colNames;
    }

    /**
     * Set the list of column names.
     *
     * @param colNames the colNames to set
     */
    public final void setColNames(final List<String> colNames) {
        this.colNames = colNames;
    }

    /**
     * Get the log values of the whole matrix in a single string.
     *
     * @return the logMatrix
     */
    public final String getLogMatrix() {
        return logMatrix;
    }

    /**
     * Set the log values of the matrix string.
     *
     * @param logMatrix the logMatrix to set
     */
    public final void setLogMatrix(final String logMatrix) {
        this.logMatrix = logMatrix;
    }

    /**
     * Get the maximum value of log value of matrix.
     *
     * @return the logMax
     */
    public final double getLogMax() {
        return logMax;
    }

    /**
     * Set the maximum log value.
     *
     * @param logMax the logMax to set
     */
    public final void setLogMax(final double logMax) {
        this.logMax = logMax;
    }

    /**
     * Get the minimum value of log value of matrix.
     *
     * @return the logMin
     */
    public final double getLogMin() {
        return logMin;
    }

    /**
     * Set the minimum log value.
     *
     * @param logMin the logMin to set
     */
    public final void setLogMin(final double logMin) {
        this.logMin = logMin;
    }

    /**
     * Get the whole matrix in a single string.
     *
     * @return the matrix string
     */
    public final String getMatrix() {
        return matrix;
    }

    /**
     * Set the matrix string.
     *
     * @param matrix the matrix to set
     */
    public final void setMatrix(final String matrix) {
        this.matrix = matrix;
    }

    /**
     * Get the maximum value of the matrix.
     *
     * @return the max
     */
    public final double getMax() {
        return max;
    }

    /**
     * Set the maximum value.
     *
     * @param max the max to set
     */
    public final void setMax(final double max) {
        this.max = max;
    }

    /**
     * Get the minimum value of the matrix.
     *
     * @return the min
     */
    public final double getMin() {
        return min;
    }

    /**
     * Set the minimum value.
     *
     * @param min the min to set
     */
    public final void setMin(final double min) {
        this.min = min;
    }

    /**
     * Get the list of row names.
     *
     * @return the rowNames
     */
    public final List<String> getRowNames() {
        return rowNames;
    }

    /**
     * Set the list of row names.
     *
     * @param rowNames the rowNames to set
     */
    public final void setRowNames(final List<String> rowNames) {
        this.rowNames = rowNames;
    }

    /**
     * Get the number of rows in the matrix.
     *
     * @return the rowNumber
     */
    public final int getRowNumber() {
        return rowNumber;
    }

    /**
     * Set the number of rows.
     *
     * @param rowNumber the rowNumber to set
     */
    public final void setRowNumber(final int rowNumber) {
        this.rowNumber = rowNumber;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
