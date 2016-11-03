
package uk.ac.liv.pgb.mzqlib.idmapper.data;

/**
 * Tolerance class.
 *
 * @author SPerkins
 */
public class Tolerance {

    private double tolerance;

    /**
     * Constructor.
     *
     * @param tolerance tolerance value.
     * @param unit      unit.
     */
    public Tolerance(double tolerance, ToleranceUnit unit) {
        this.tolerance = tolerance;
        this.unit = unit;
    }

    /**
     * Get tolerance value.
     *
     * @return value.
     */
    public double getTolerance() {
        return tolerance;
    }

    /**
     * Set tolerance value.
     *
     * @param tolerance value.
     */
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * Get unit.
     *
     * @return tolerance unit.
     */
    public ToleranceUnit getUnit() {
        return unit;
    }

    /**
     * Set unit.
     *
     * @param unit tolerance unit.
     */
    public void setUnit(ToleranceUnit unit) {
        this.unit = unit;
    }

    private ToleranceUnit unit;

    /**
     * Tolerance unit enumerate class.
     */
    public enum ToleranceUnit {

        /**
         * PPM.
         */
        PPM,

        /**
         * DALTON.
         */
        DALTON
    }

}
