/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.idmapper;

import gnu.trove.map.TIntObjectMap;
import java.util.Map;
import uk.ac.ebi.jmzidml.model.mzidml.SearchDatabase;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 03-Mar-2014 16:55:27
 */
public interface MzidProcessor {

    /**
     * Get peptide to spectrumIdentificationItems map.
     * This is a one to many map with peptide sequence plus modifications as key and list of SII data as values.
     *
     * @return the peptide to spectrumIdentificationItems map.
     */
    public Map getPeptideModStringToSIIsMap();

    //public Map getPeptideModStringToProtAccessionsMap();
    /**
     * Get retention time (minute) to spectumIdentificationItems map.
     * This is a one to many map with retention time (round to minute) as key and SII data as values.
     *
     * @return retention time to spectrumIdentifications map.
     */
    public TIntObjectMap getRtToSIIsMap();

    public SearchDatabase getSearchDatabase();

}
