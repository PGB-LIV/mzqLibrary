/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.pgb.mzqlib.progenesis.reader;

/**
 * This class stores the header constant strings from protein list file
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 02-Dec-2013 15:22:59
 */
public class ProteinListHeaders {

    /*
     * All use lower case
     */
 /*
     * These are the first row headers
     */

    public static final String NORMALIZED_ABUNDANCE = "normalized abundance";
    public static final String RAW_ABUNDANCE = "raw abundance";
    public static final String SPECTRAL_COUNTS = "spectral counts";
    /*
     * These are the third row headers
     */
    public static final String CONFIDENCE_SCORE = "confidence score";
    public static final String ANOVA = "anova (p)";
    public static final String MAX_FOLD_CHANGE = "max fold change";
}
