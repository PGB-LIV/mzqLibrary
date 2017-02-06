package uk.ac.liv.pgb.mzqlib.idmapper;

import java.util.Map;

import gnu.trove.map.TIntObjectMap;

import uk.ac.ebi.jmzidml.model.mzidml.SearchDatabase;

/**
 * Interface of MzidProcessor
 *
 * @author Da Qi
 * @since 03-Mar-2014 16:55:27
 */
public interface MzidProcessor {

    /**
     * Get peptide to spectrumIdentificationItems map.
     * This is a one to many map with peptide sequence plus modifications as key
     * and list of SII data as values.
     *
     * @return the peptide to spectrumIdentificationItems map.
     */
    Map getPeptideModStringToSIIsMap();

    // public Map getPeptideModStringToProtAccessionsMap();

    /**
     * Get retention time (minute) to spectumIdentificationItems map.
     * This is a one to many map with retention time (round to minute) as key
     * and SII data as values.
     *
     * @return retention time to spectrumIdentifications map.
     */
    TIntObjectMap getRtToSIIsMap();

    /**
     * Get SearchDatabase element.
     *
     * @return SearchDatabase
     */
    SearchDatabase getSearchDatabase();
}
//~ Formatted by Jindent --- http://www.jindent.com
