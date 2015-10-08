
package uk.ac.liv.mzqlib.idmapper;

import gnu.trove.map.TIntObjectMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.Feature;
import uk.ac.liv.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.jmzqml.model.mzqml.RawFilesGroup;
import uk.ac.liv.jmzqml.model.mzqml.SearchDatabase;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.mzqlib.idmapper.data.ExtendedFeature;
import uk.ac.liv.mzqlib.idmapper.data.SIIData;
import uk.ac.liv.mzqlib.utils.MzidToMzqElementConverter;

/**
 * The MzqProcessorFactory class parse the input mzQuantML file and create an MzqProcessor instance.
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 24-Jun-2013 14:06:51
 */
public class MzqProcessorFactory {

    private static final MzqProcessorFactory instance = new MzqProcessorFactory();

    private MzqProcessorFactory() {
    }

    /**
     * The method provides a static instance of the class MzqProcessorFactory.
     * User will call this method for accessing to buildMzqProcessor method.
     *
     * @return the instance of MzqProcessorFactor
     */
    public static MzqProcessorFactory getInstance() {
        return instance;
    }

    /**
     * The method builds a MzqProcessor instance from an input mzQuantML unmarshaller and other parameters.
     *
     * @param mzqUm        MzQuantMLUnmarshaller
     * @param rawToMzidMap map of raw file name to mzIdentML file name
     * @param mzWin        the m/z window of feature measured from left to right in Da
     * @param rtWin        the retention time of feature measured from top to bottom in second
     *
     * @return MzqProcessor
     *
     * @throws JAXBException
     * @throws IOException
     */
    public MzqProcessor buildMzqProcessor(MzQuantMLUnmarshaller mzqUm,
                                          Map<String, String> rawToMzidMap,
                                          double mzWin, double rtWin)
            throws JAXBException, IOException {
        return new MzqProcessorImpl(mzqUm, rawToMzidMap, mzWin, rtWin);
    }

    /**
     * The method builds a MzqProcessor instance from an input mzQuantML unmarshaller and other parameters.
     *
     * @param mzqUm        MzQuantMLUnmarshaller
     * @param rawToMzidMap map of raw file name to mzIdentML file name
     *
     * @return MzqProcessor
     *
     * @throws JAXBException
     * @throws IOException
     */
    public MzqProcessor buildMzqProcessor(MzQuantMLUnmarshaller mzqUm,
                                          Map<String, String> rawToMzidMap)
            throws JAXBException, IOException {
        return new MzqProcessorImpl(mzqUm, rawToMzidMap);
    }

    private class MzqProcessorImpl implements MzqProcessor {

        private MzQuantMLUnmarshaller mzqUm = null;
        //Map of feature id (Stirng) to list of SIIData
        private Map<String, List<SIIData>> featureToSIIsMap = new HashMap<>();
        private SearchDatabase searchDB;

        /*
         * Constructor
         */
        private MzqProcessorImpl(MzQuantMLUnmarshaller mzqUm,
                                 Map<String, String> rawToMzidMap, double mzWin,
                                 double rtWin)
                throws JAXBException, IOException {

            this.mzqUm = mzqUm;

            Iterator<FeatureList> itFeatureList = this.mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);
            if (itFeatureList == null) {
                throw new RuntimeException("There is no FeatureList in the mzq file.");
            }
            
            
            int featuresWithMatch = 0;
            int featuresWithoutMatch = 0;
            int identificationsAssignedCount = 0;
            int identificationsUnassignedCount = 0;            
            while (itFeatureList.hasNext()) {
                FeatureList ftList = itFeatureList.next();
                RawFilesGroup rg = (RawFilesGroup) this.mzqUm.unmarshal(uk.ac.liv.jmzqml.model.mzqml.RawFilesGroup.class, ftList.getRawFilesGroupRef());

                String rawFileName = "";
                if (rg.getRawFile().get(0).getName() != null) {
                    rawFileName = rg.getRawFile().get(0).getName();
                }
                else if (rg.getRawFile().get(0).getLocation() != null) {
                    rawFileName = rg.getRawFile().get(0).getLocation();
                }
                else {
                    continue;
                }

                String mzidFileName = rawToMzidMap.get(rawFileName);
                if (mzidFileName == null || !(new File(mzidFileName).isFile())) {
                    throw new RuntimeException("The raw file name \"" + rawFileName + "\" which appears in mzq file cannot be found in rawToMzidMap "
                            + "or the related mzid file: \"" + mzidFileName + "\" is not exist.\n");
                }

                // corresponding mzIdentML processor 
                MzidProcessor mzidProc = MzidProcessorFactory.getInstance().buildMzidProcessor(new File(mzidFileName));

                TIntObjectMap<List<SIIData>> rtToSIIsMap = mzidProc.getRtToSIIsMap();
                
                // Let's work out how many identifications there are.
                int flIdentificationCount = rtToSIIsMap.valueCollection().stream().mapToInt(p -> p.size()).sum();                
                int flIdentificationsAssignedCount = 0;
                if (searchDB == null) {
                    searchDB = MzidToMzqElementConverter.convertMzidSDBToMzqSDB(mzidProc.getSearchDatabase());
                }

                List<Feature> features = ftList.getFeature();
                
                int flFeaturesWithMatch = 0;
                int flFeaturesWithoutMatch = 0;
                
                for (Feature ft : features) {
                    List<SIIData> ftSIIDataList = featureToSIIsMap.get(ft.getId());

                    ExtendedFeature exFt = new ExtendedFeature(ft, mzWin, rtWin);

                    for (int i = (int) exFt.getBRT(); i <= (int) exFt.getURT(); i++) {
                        List<SIIData> siiDataList = rtToSIIsMap.get(i);
                        if (siiDataList != null) {
                            for (SIIData sd : siiDataList) {
                                double siiExpMz = sd.getExperimentalMassToCharge();
                                double siiRt = sd.getRetentionTime();
                                int identCharge = sd.getCharge();
                                if (identCharge != Integer.parseInt(exFt.getCharge())) {
                                    continue;
                                }

                                // determine if rt and mz of the SIIData is in the mass trace of the feature                                
                                if (isInRange(siiExpMz, exFt.getLMZ(), exFt.getRMZ()) && isInRange(siiRt, exFt.getBRT(), exFt.getURT())) {
                                    if (ftSIIDataList == null) {
                                        ftSIIDataList = new ArrayList<>();
                                        featureToSIIsMap.put(ft.getId(), ftSIIDataList);
                                    }
                                    
                                    ftSIIDataList.add(sd);
                                    flIdentificationsAssignedCount++;
                                }
                            }
                        }
                    }
                    
                    if (ftSIIDataList == null || ftSIIDataList.isEmpty()) {
                        flFeaturesWithoutMatch++;
                    } else {
                        flFeaturesWithMatch++;
                    }
                }
                System.out.println("MzqProcessorFactory -- finish processing FeatureList: " + ftList.getId());
                System.out.println("Matched features: " + flFeaturesWithMatch + ". Unmatched features: " + flFeaturesWithoutMatch + ". Total features: " + (flFeaturesWithMatch+flFeaturesWithoutMatch) + ".");
                System.out.println("Assigned identifications: " + flIdentificationsAssignedCount + ". Unassigned identifications: " + (flIdentificationCount - flIdentificationsAssignedCount) + ". Total identifications: " + flIdentificationCount + ".");
                featuresWithMatch += flFeaturesWithMatch;
                featuresWithoutMatch += flFeaturesWithoutMatch;
                identificationsAssignedCount += flIdentificationsAssignedCount;
                identificationsUnassignedCount += flIdentificationCount - flIdentificationsAssignedCount;
            }
            
            System.out.println("MzqProcessorFactory -- finish processing feature lists.");
            System.out.println("Matched features: " + featuresWithMatch + ". Unmatched features: " + featuresWithoutMatch + ". Total features: " + (featuresWithMatch+featuresWithoutMatch) + ".");
            System.out.println("Assigned identifications: " + identificationsAssignedCount + ". Unassigned identifications: " + identificationsUnassignedCount + ". Total identifications: " + (identificationsAssignedCount+identificationsUnassignedCount) + ".");
        }

        private MzqProcessorImpl(MzQuantMLUnmarshaller mzqUm,
                                 Map<String, String> rawToMzidMap)
                throws JAXBException, IOException {
            this(mzqUm, rawToMzidMap, 0.2, 1.0/3.0);
        }

        @Override
        public Map getFeatureToSIIsMap() {
            return featureToSIIsMap;
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
