package uk.ac.liv.pgb.mzqlib.progenesis.reader;

/**
 * This class stores the header constant strings from protein list file
 *
 * @author Da Qi
 * @since 02-Dec-2013 15:22:59
 */
public class ProteinListHeaders {

    /*
     * All use lower case
     */

    /*
     *    These are the first row headers
     */

    /**
     * Constant.
     */
    public static final String NORMALIZED_ABUNDANCE = "normalized abundance";

    /**
     * Constant.
     */
    public static final String RAW_ABUNDANCE = "raw abundance";

    /**
     * Constant.
     */
    public static final String SPECTRAL_COUNTS = "spectral counts";

    /*
     * These are the third row headers
     */

    /**
     * Constant.
     */
    public static final String CONFIDENCE_SCORE = "confidence score";

    /**
     * Constant.
     */
    public static final String ANOVA = "anova (p)";

    /**
     * Constant.
     */
    public static final String MAX_FOLD_CHANGE = "max fold change";

    private ProteinListHeaders() {
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
