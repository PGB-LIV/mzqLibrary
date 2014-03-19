/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.idmapper;

import gnu.trove.map.TIntObjectMap;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.Feature;
import uk.ac.liv.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.jmzqml.model.mzqml.FileFormat;
import uk.ac.liv.jmzqml.model.mzqml.RawFilesGroup;
import uk.ac.liv.jmzqml.model.mzqml.SearchDatabase;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.mzqlib.idmapper.data.ExtendedFeature;
import uk.ac.liv.mzqlib.idmapper.data.SIIData;
import uk.ac.liv.mzqlib.utils.MzidToMzqElementConvertor;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 24-Jun-2013 14:06:51
 */
public class MzqProcessorFactory {

    private static final MzqProcessorFactory instance = new MzqProcessorFactory();

    private MzqProcessorFactory() {
    }

    public static MzqProcessorFactory getInstance() {
        return instance;
    }

    public MzqProcessor buildMzqProcessor(MzQuantMLUnmarshaller mzqUm,
                                          Map rawToMzidMap)
            throws JAXBException {
        return new MzqProcessorImpl(mzqUm, rawToMzidMap);
    }

    private class MzqProcessorImpl implements MzqProcessor {

        //private File mzqFile = null;
        private MzQuantMLUnmarshaller mzqUm = null;
        //Map of feature id (Stirng) to list of SIIData
        private Map<String, List<SIIData>> featureToSIIsMap = null;
        //Map of Peptide mod string to list of SIIData from all mzidProcs
        private Map<String, List<SIIData>> combinedPepModStringToSIIsMap = null;
        //Map of Peptide mod string to list of protein accessions
        private Map<String, List<String>> combPepModStringToProtAccessionsMap = null;
        private SearchDatabase searchDB;

        /*
         * Constructor
         */
        private MzqProcessorImpl(MzQuantMLUnmarshaller mzqUm,
                                 Map<String, String> rawToMzidMap)
                throws JAXBException {

            this.mzqUm = mzqUm;

            featureToSIIsMap = new HashMap<>();
            combinedPepModStringToSIIsMap = new HashMap<>();

            Iterator<FeatureList> itFeatureList = this.mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);
            while (itFeatureList.hasNext()) {
                FeatureList ftList = itFeatureList.next();
                RawFilesGroup rg = (RawFilesGroup) this.mzqUm.unmarshal(uk.ac.liv.jmzqml.model.mzqml.RawFilesGroup.class, ftList.getRawFilesGroupRef());
                String rawFn = rg.getRawFile().get(0).getName();
                String mzidFileName = rawToMzidMap.get(rawFn);

                // corresponding mzIdentML processor 
                MzidProcessor mzidProc = MzidProcessorFactory.getInstance().buildMzidProcessor(new File(mzidFileName));

                TIntObjectMap<List<SIIData>> rtToSIIsMap = mzidProc.getRtToSIIsMap();

                if (searchDB == null) {
                    searchDB = MzidToMzqElementConvertor.convertMzidSDBToMzqSDB(mzidProc.getSearchDatabase());
                }

                List<Feature> features = ftList.getFeature();
                for (Feature ft : features) {
                    List<SIIData> ftSIIDataList = featureToSIIsMap.get(ft.getId());

                    ExtendedFeature exFt = new ExtendedFeature(ft);

                    // try up RT first 
                    int upperRt = (int) exFt.getURT();

                    List<SIIData> siiDataList = rtToSIIsMap.get(upperRt);

                    if (siiDataList != null) {
                        for (SIIData sd : siiDataList) {
                            double siiExpMz = sd.getExperimentalMassToCharge();
                            double siiRt = sd.getRetentionTime();
                            // determine if rt and mz of the SIIData is in the mass trace of the feature
                            if (isInRange(siiExpMz, exFt.getLMZ(), exFt.getRMZ()) && isInRange(siiRt, exFt.getBRT(), exFt.getURT())) {
                                if (ftSIIDataList == null) {
                                    ftSIIDataList = new ArrayList();
                                    featureToSIIsMap.put(ft.getId(), ftSIIDataList);
                                }
                                ftSIIDataList.add(sd);
                            }
                        }
                    }

                    // try bottom RT second if different from up RT
                    // for example: (35.433-36.112) will give both 35 and 36 a try
                    //              (35.022-35.913) will only give 35 a try
                    int bottomRt = (int) exFt.getBRT();
                    if (bottomRt != upperRt) {
                        siiDataList = rtToSIIsMap.get(bottomRt);

                        if (siiDataList != null) {
                            for (SIIData sd : siiDataList) {
                                double siiExpMz = sd.getExperimentalMassToCharge();
                                double siiRt = sd.getRetentionTime();
                                // determine if rt and mz of the SIIData is in the mass trace of the feature
                                if (isInRange(siiExpMz, exFt.getLMZ(), exFt.getRMZ()) && isInRange(siiRt, exFt.getBRT(), exFt.getURT())) {
                                    if (ftSIIDataList == null) {
                                        ftSIIDataList = new ArrayList();
                                        featureToSIIsMap.put(ft.getId(), ftSIIDataList);
                                    }
                                    ftSIIDataList.add(sd);
                                }
                            }
                        }
                    }

                    // build combinedPepModStringToSIIsMap;
                    Map<String, List<SIIData>> pepModStringToSIIsMap = mzidProc.getPeptideModStringToSIIsMap();

                    for (String pepModString : pepModStringToSIIsMap.keySet()) {
                        List<SIIData> combSIIsList = combinedPepModStringToSIIsMap.get(pepModString);
                        if (combSIIsList == null) {
                            combSIIsList = new ArrayList();
                            combinedPepModStringToSIIsMap.put(pepModString, combSIIsList);
                            //Deliberately bring this inside the loop to reduce memory usage
                            combSIIsList.addAll(pepModStringToSIIsMap.get(pepModString));
                        }
                        //combSIIsList.addAll(pepModStringToSIIsMap.get(pepModString));
                    }

                    //build combinedPepModStringToProtAccessionsMap;
//                    Map<String, List<String>> pepModStringToProtAccsMap = mzidProc.getPeptideModStringToProtAccessionsMap();
//
//                    for (String pepMod : pepModStringToProtAccsMap.keySet()) {
//                        if (pepModStringToProtAccsMap.get(pepMod) != null) {
//                            List<String> protAccList = combPepModStringToProtAccessionsMap.get(pepMod);
//                            if (protAccList == null) {
//                                protAccList = new ArrayList();
//                                combPepModStringToProtAccessionsMap.put(pepMod, protAccList);
//                            }
//                            protAccList.addAll(pepModStringToProtAccsMap.get(pepMod));
//                        }
//                    }
                }
                System.out.println("MzqProcessorFactory -- finish processing FeatureList: " + ftList.getId());
            }
        }

        @Override
        public Map getFeatureToSIIsMap() {
            return featureToSIIsMap;
        }

        @Override
        public Map getCombinedPepModStringToSIIsMap() {
            return combinedPepModStringToSIIsMap;
        }

        @Override
        public Map getCombPepModStringToProtAccessionsMap() {
            if (combPepModStringToProtAccessionsMap == null) {
                combPepModStringToProtAccessionsMap = new HashMap<>();
            }
            return combPepModStringToProtAccessionsMap;
        }

        @Override
        public SearchDatabase getSearchDatabase() {
            return searchDB;
        }

    }

    /**
     * Determine if the test double falls in a range defined by both boundaries.
     *
     * @param test           the double number to be test
     * @param rangeBoundary1 one side of the range boundary regardless left or right side
     * @param rangeBoundary2 the other side of the range boundary regardless left or right side
     *
     * @return true if test double falls in the given range; false otherwise
     */
    private boolean isInRange(double test, double rangeBoundary1,
                              double rangeBoundary2) {
        if (rangeBoundary1 <= rangeBoundary2) {
            return test >= rangeBoundary1 && test <= rangeBoundary2;
        }
        else {
            return test <= rangeBoundary1 && test >= rangeBoundary2;
        }
    }

}
