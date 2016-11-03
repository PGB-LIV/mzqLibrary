
package uk.ac.liv.pgb.mzqlib.r;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 21-Nov-2014 10:34:23
 */
public enum RequiredPackages {

    //list of required R pakcages
    //R_JAVA("rJava"),
    /**
     *
     */
    GPLOTS("gplots");
    //R_COLOR_BREWER("RColorBrewer");
    //variables
    private final String packageName;

    private RequiredPackages(String pkName) {
        this.packageName = pkName;
    }

    /**
     * Get package name.
     *
     * @return package name.
     */
    public String getPackageName() {
        return packageName;
    }

}
