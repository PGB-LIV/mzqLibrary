package uk.ac.liv.pgb.mzqlib.openms.converter;

import java.io.File;
import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.liv.pgb.jmzqml.model.mzqml.Assay;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AssayList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Cv;
import uk.ac.liv.pgb.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RawFilesGroup;

/**
 * The interface of ConsensusXMLProcessor that provides methods of creating
 * mzQuantML elements from consensusxml file.
 *
 * @author Da Qi
 * @since 18-Mar-2014 15:17:10
 */
public interface ConsensusXMLProcessor {

    /**
     * Convert to mzQuantML file.
     *
     * @param outputFn output file name
     *
     * @throws IOException io exception
     */
    void convert(String outputFn) throws IOException;

    /**
     * Convert method.
     *
     * @param outputFn              output file name.
     * @param studyVariablesToFiles study variables to files map.
     *
     * @throws IOException io exception.
     */
    void convert(String outputFn, Map<String, ? extends Collection<File>> studyVariablesToFiles) throws IOException;

    /**
     * Get AssayList element.
     *
     * @return AssayList
     */
    AssayList getAssayList();

    /**
     * Get Cv element.
     *
     * @return Cv
     */
    Cv getCv();

    /**
     * Get PeptideConsensusList element.
     *
     * @return PeptideConsensusList
     */
    PeptideConsensusList getPeptideConsensusList();

    /**
     * Get map of rawFilesGroup to Assay.
     *
     * @return Map&lt;String, Assay&gt;
     */
    Map<String, Assay> getRawFilesGroupAssayMap();

    /**
     * Get map of rawFilesGroup's id to FeatureList.
     *
     * @return Map&lt;String, FeatureList&gt;
     */
    Map<String, FeatureList> getRawFilesGroupIdToFeatureListMap();

    /**
     * Get list of rawFilesGroup.
     *
     * @return List&lt;RawFilesGroup&gt;
     */
    List<RawFilesGroup> getRawFilesGroupList();
}


//~ Formatted by Jindent --- http://www.jindent.com
