
package uk.ac.liv.mzqlib.model;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 12-Sep-2014 11:16:22
 */
public class HeatMapParam {

    private double min;
    private double max;
    private int rowNumber;
    private String matrix;

    /**
     * Constructor of HeatMapParam
     *
     * @param min    minimum value of the matrix for heat map
     * @param max    maximum value of the matrix for heat map
     * @param number number of entities
     * @param x      the String used to create an R matrix
     */
    public HeatMapParam(double min, double max, int number, String x) {
        this.min = min;
        this.max = max;
        this.rowNumber = number;
        this.matrix = x;
    }

    public HeatMapParam() {
        this(0, 0, 0, "");
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
        this.matrix = new String(matrix);
    }

}
