package uk.ac.liv.pgb.mzqlib.idmapper.data;

/**
 * Tolerance class.
 *
 * @author SPerkins
 */
public class Tolerance {
    private double        tolerance;
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

    /**
     * Constructor.
     *
     * @param tolerance tolerance value.
     * @param unit      unit.
     */
    public Tolerance(final double tolerance, final ToleranceUnit unit) {
        this.tolerance = tolerance;
        this.unit      = unit;
    }

    /**
     * Get tolerance value.
     *
     * @return value.
     */
    public final double getTolerance() {
        return tolerance;
    }

    /**
     * Set tolerance value.
     *
     * @param tolerance value.
     */
    public final void setTolerance(final double tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * Get unit.
     *
     * @return tolerance unit.
     */
    public final ToleranceUnit getUnit() {
        return unit;
    }

    /**
     * Set unit.
     *
     * @param unit tolerance unit.
     */
    public final void setUnit(final ToleranceUnit unit) {
        this.unit = unit;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
