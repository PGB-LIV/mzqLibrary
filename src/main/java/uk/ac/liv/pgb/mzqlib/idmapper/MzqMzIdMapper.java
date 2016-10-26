
package uk.ac.liv.pgb.mzqlib.idmapper;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;

/**
 * Interface of MzqMzidMapper.
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 06-Mar-2014 12:11:43
 */
public interface MzqMzIdMapper {

    /**
     * Generate the output mzQuantML file from input mzQuantML file and related
     * mzIdentML files.
     * The mzIdentML files provide spectrum identification item as the
     * EvidenceRef based on the matching of feature retention time and m/z
     *
     * @param outputFile output file name
     *
     * @throws JAXBException       jaxb exception
     * @throws java.io.IOException io exception
     */
    void createMappedFile(File outputFile)
            throws JAXBException, IOException;

}
