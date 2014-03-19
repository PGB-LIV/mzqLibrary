/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.consensusxml.convertor;

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
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 18-Mar-2014 15:17:10
 */
public interface ConsensusXMLProcessor {

    public PeptideConsensusList getPeptideConsensusList();

    public Map<String, FeatureList> getRawFilesGroupIdToFeatureListMap();

    public Cv getCv();

    public AssayList getAssayList();

    public Map<String, Assay> getRawFilesGroupAssayMap();

    public List<RawFilesGroup> getRawFilesGroupList();

    public void convert(String outputFn)
            throws IOException;

}
