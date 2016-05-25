
package uk.ac.liv.mzqlib.idmapper;

import java.util.Map;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SearchDatabase;

/**
 * Interface of MzqProcessor
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 05-Mar-2014 11:25:22
 */
public interface MzqProcessor {

    /**
     * Get map of Feature to SIIData list.
     *
     * @return Map
     */
    Map getFeatureToSIIsMap();

    /**
     * Get search database.
     *
     * @return SearchDatabase
     */
    SearchDatabase getSearchDatabase();

}
