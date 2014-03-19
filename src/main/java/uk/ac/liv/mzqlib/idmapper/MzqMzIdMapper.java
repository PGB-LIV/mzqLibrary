/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.idmapper;

import javax.xml.bind.JAXBException;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 06-Mar-2014 12:11:43
 */
public interface MzqMzIdMapper {

//    public Map getPeptideConsensusToPeptideSequencesMap();
    /**
     * Generate the output mzQuantML file from input mzQuantML file and related mzIdentML files.
     * The mzIdentML files provide spectrum identification item as the EvidenceRef based on the matching of feature retention time and m/z
     *
     * @param output output file name
     *
     * @throws JAXBException
     */
    public void createMappedFile(String output)
            throws JAXBException;

}
