/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.idmapper.data;

/**
 *
 * @author SPerkins
 */
public class Tolerance {

    private double tolerance;

    public Tolerance(double tolerance, ToleranceUnit unit) {
        this.tolerance = tolerance;
        this.unit = unit;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public ToleranceUnit getUnit() {
        return unit;
    }

    public void setUnit(ToleranceUnit unit) {
        this.unit = unit;
    }

    private ToleranceUnit unit;

    public enum ToleranceUnit {

        PPM,
        DALTON
    }

}
