
package uk.ac.liv.mzqlib.openms.converter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.AssayList;
import uk.ac.liv.jmzqml.model.mzqml.Cv;
import uk.ac.liv.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.model.mzqml.RawFilesGroup;

/**
 * The interface of ConsensusXMLProcessor that provides methods of creating mzQuantML elements from consensusxml file.
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 18-Mar-2014 15:17:10
 */
public interface ConsensusXMLProcessor {

    /**
     * Get PeptideConsensusList element.
     *
     * @return PeptideConsensusList
     */
    public PeptideConsensusList getPeptideConsensusList();

    /**
     * Get map of rawFilesGroup's id to FeatureList.
     *
     * @return Map<String, FeatureList>
     */
    public Map<String, FeatureList> getRawFilesGroupIdToFeatureListMap();

    /**
     * Get Cv element.
     *
     * @return Cv
     */
    public Cv getCv();

    /**
     * Get AssayList element.
     *
     * @return AssayList
     */
    public AssayList getAssayList();

    /**
     * Get map of rawFilesGroup to Assay.
     *
     * @return Map<String, Assay>
     */
    public Map<String, Assay> getRawFilesGroupAssayMap();

    /**
     * Get list of rawFilesGroup.
     *
     * @return List<RawFilesGroup>
     */
    public List<RawFilesGroup> getRawFilesGroupList();

    /**
     * Convert to mzQuantML file.
     *
     * @param outputFn output file name
     *
     * @throws IOException
     */
    public void convert(String outputFn)
            throws IOException;

}
