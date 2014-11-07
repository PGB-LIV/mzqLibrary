/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.progenesis.reader;

/**
 * This class stores the header constant strings from feature list file
 * 
 * @author Da Qi
 * @institute University of Liverpool
 * @time 02-Dec-2013 15:02:27
 */
public class FeatureListHeaders {

    /*
     * All use lower case
     */
    /*
     * These are the first row headers
     */
    public static final String NORMALIZED_ABUNDANCE = "normalized abundance";
    public static final String RAW_ABUNDANCE = "raw abundance";
    public static final String INTENSITY = "intensity";
    public static final String SAMPLE_RETENTION_TIME = "sample retention time";
    /*
     * These are the third row headers
     */
    public static final String M_Z = "m/z";
    public static final String RETENTION_TIME = "retention time (min)";
    public static final String RETENTION_TIME_WINDOW = "retention time window (min)";
    public static final String CHARGE = "charge";
    public static final String SCORE = "score";
    public static final String MODIFICATIONS = "modifications";
    public static final String SEQUENCE = "sequence";
    public static final String PROTEIN = "protein";
    public static final String ACCESSION = "accession";
    public static final String USE_IN_QUANT = "use in quantitation";
}
