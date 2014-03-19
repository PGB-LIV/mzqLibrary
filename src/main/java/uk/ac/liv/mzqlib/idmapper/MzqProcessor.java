/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.idmapper;

import java.util.Map;
import uk.ac.liv.jmzqml.model.mzqml.SearchDatabase;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 05-Mar-2014 11:25:22
 */
public interface MzqProcessor {

    public Map getFeatureToSIIsMap();

    public Map getCombPepModStringToProtAccessionsMap();

    public Map getCombinedPepModStringToSIIsMap();
    
    public SearchDatabase getSearchDatabase();

}