package uk.ac.liv.pgb.mzqlib.idmapper;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import gnu.trove.map.TIntObjectMap;

import uk.ac.liv.pgb.jmzqml.MzQuantMLElement;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Feature;
import uk.ac.liv.pgb.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RawFilesGroup;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SearchDatabase;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.pgb.mzqlib.idmapper.data.ExtendedFeature;
import uk.ac.liv.pgb.mzqlib.idmapper.data.FeatureSiiMatchManager;
import uk.ac.liv.pgb.mzqlib.idmapper.data.SIIData;
import uk.ac.liv.pgb.mzqlib.idmapper.data.Tolerance;
import uk.ac.liv.pgb.mzqlib.idmapper.data.Tolerance.ToleranceUnit;
import uk.ac.liv.pgb.mzqlib.utils.MzidToMzqElementConverter;

/**
 * The MzqProcessorFactory class parse the input mzQuantML file and create an
 * MzqProcessor instance.
 *
 * @author Da Qi
 * @since 24-Jun-2013 14:06:51
 */
public class MzqProcessorFactory {
    private static final MzqProcessorFactory instance  = new MzqProcessorFactory();
    private static final double              TOLERANCE = 0.1;

    /**
     * The method builds a MzqProcessor instance from an input mzQuantML
     * unmarshaller and other parameters.
     *
     * @param mzqUm        MzQuantMLUnmarshaller
     * @param rawToMzidMap map of raw file name to mzIdentML file name
     *
     * @return MzqProcessor
     *
     * @throws JAXBException jaxb exception
     * @throws IOException   io exception
     */
    public MzqProcessor buildMzqProcessor(final MzQuantMLUnmarshaller mzqUm, final Map<String, String> rawToMzidMap)
            throws JAXBException, IOException, JAXBException, JAXBException {
        return new MzqProcessorImpl(mzqUm, rawToMzidMap);
    }

    /**
     * The method builds a MzqProcessor instance from an input mzQuantML
     * unmarshaller and other parameters.
     *
     * @param mzqUm        MzQuantMLUnmarshaller
     * @param rawToMzidMap map of raw file name to mzIdentML file name
     * @param msTolerance  ms tolerance window
     *
     * @return MzqProcessor
     *
     * @throws JAXBException jaxb exception
     * @throws IOException   io exception
     */
    public MzqProcessor buildMzqProcessor(final MzQuantMLUnmarshaller mzqUm, final Map<String, String> rawToMzidMap,
                                          final Tolerance msTolerance)
            throws JAXBException, IOException {
        return new MzqProcessorImpl(mzqUm, rawToMzidMap, msTolerance);
    }

    /**
     * The method builds a MzqProcessor instance from an input mzQuantML
     * unmarshaller and other parameters.
     *
     * @param mzqUm        MzQuantMLUnmarshaller
     * @param rawToMzidMap map of raw file name to mzIdentML file name
     * @param mzWin        the m/z window of feature measured from left to right
     *                     in Da
     * @param rtWin        the retention time of feature measured from top to
     *                     bottom in
     *                     second
     *
     * @return MzqProcessor
     *
     * @throws JAXBException jaxb exception
     * @throws IOException   io exception
     */
    public MzqProcessor buildMzqProcessor(final MzQuantMLUnmarshaller mzqUm, final Map<String, String> rawToMzidMap,
                                          final double mzWin, final double rtWin)
            throws JAXBException, IOException {
        return new MzqProcessorImpl(mzqUm, rawToMzidMap, mzWin, rtWin);
    }

    /**
     * Determine if the test double falls in a range defined by both boundaries.
     *
     * @param test           the double number to be test
     * @param rangeBoundary1 one side of the range boundary regardless left or
     *                       right side
     * @param rangeBoundary2 the other side of the range boundary regardless
     *                       left
     *                       or right side
     *
     * @return true if test double falls in the given range; false otherwise
     */
    private static boolean isInRange(final double test, final double rangeBoundary1, final double rangeBoundary2) {
        if (rangeBoundary1 <= rangeBoundary2) {
            return test >= rangeBoundary1 && test <= rangeBoundary2;
        } else {
            return test <= rangeBoundary1 && test >= rangeBoundary2;
        }
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

    private static class MzqProcessorImpl implements MzqProcessor {
        private MzQuantMLUnmarshaller  mzqUm        = null;
        private FeatureSiiMatchManager matchManager = new FeatureSiiMatchManager();
        private SearchDatabase         searchDB;

        private MzqProcessorImpl(final MzQuantMLUnmarshaller mzqUm, final Map<String, String> rawToMzidMap)
                throws JAXBException, IOException {
            this(mzqUm, rawToMzidMap, new Tolerance(TOLERANCE, ToleranceUnit.DALTON), 1.0 / 3.0);
        }

        private MzqProcessorImpl(final MzQuantMLUnmarshaller mzqUm, final Map<String, String> rawToMzidMap,
                                 final Tolerance msTolerance)
                throws JAXBException, IOException {
            this(mzqUm, rawToMzidMap, msTolerance, 1.0 / 3.0);
        }

        private MzqProcessorImpl(final MzQuantMLUnmarshaller mzqUm, final Map<String, String> rawToMzidMap,
                                 final double mzWin, final double rtWin)
                throws JAXBException, IOException {
            this(mzqUm, rawToMzidMap, new Tolerance(mzWin / 2.0, ToleranceUnit.DALTON), rtWin);
        }

        /*
         * Constructor
         */
        private MzqProcessorImpl(final MzQuantMLUnmarshaller mzqUm, final Map<String, String> rawToMzidMap,
                                 final Tolerance msTolerance, final double rtWin)
                throws JAXBException, IOException {
            this.mzqUm = mzqUm;

            Iterator<FeatureList> itFeatureList = this.mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);

            if (itFeatureList == null) {
                throw new IllegalStateException("There is no FeatureList in the mzq file.");
            }

            while (itFeatureList.hasNext()) {
                FeatureList   ftList = itFeatureList.next();
                RawFilesGroup rg     = (RawFilesGroup) this.mzqUm.unmarshal(RawFilesGroup.class,
                                                                            ftList.getRawFilesGroupRef());
                String rawFileName = "";

                if (rg.getRawFile().get(0).getName() != null) {
                    rawFileName = rg.getRawFile().get(0).getName();
                } else if (rg.getRawFile().get(0).getLocation() != null) {
                    rawFileName = rg.getRawFile().get(0).getLocation();
                } else {
                    continue;
                }

                String mzidFileName = rawToMzidMap.get(rawFileName);

                if (mzidFileName == null || !(new File(mzidFileName).isFile())) {
                    throw new IllegalStateException("The raw file name \"" + rawFileName
                                                    + "\" which appears in mzq file cannot be found in rawToMzidMap "
                                                    + "or the related mzid file: \"" + mzidFileName
                                                    + "\" is not exist.\n");
                }

                // corresponding mzIdentML processor
                MzidProcessor                mzidProc    = MzidProcessorFactory.getInstance()
                                                                               .buildMzidProcessor(
                                                                                   new File(mzidFileName));
                TIntObjectMap<List<SIIData>> rtToSIIsMap = mzidProc.getRtToSIIsMap();

                rtToSIIsMap.valueCollection()
                           .stream()
                           .flatMap(p -> p.stream())
                           .forEach(
                               r -> {
                                   matchManager.registerIdentification(r, ftList.getId());
                               });

                if (searchDB == null) {
                    searchDB = MzidToMzqElementConverter.convertMzidSDBToMzqSDB(mzidProc.getSearchDatabase());
                }

                List<Feature> features = ftList.getFeature();

                for (Feature ft : features) {
                    matchManager.registerFeature(ft, ftList.getId());

                    ExtendedFeature exFt = new ExtendedFeature(ft, msTolerance, rtWin);

                    for (int i = (int) exFt.getBRT(); i <= (int) exFt.getURT(); i++) {
                        List<SIIData> siiDataList = rtToSIIsMap.get(i);

                        if (siiDataList != null) {
                            for (SIIData sd : siiDataList) {
                                double siiExpMz    = sd.getExperimentalMassToCharge();
                                double siiRt       = sd.getRetentionTime();
                                int    identCharge = sd.getCharge();

                                if (identCharge != Integer.parseInt(exFt.getCharge())) {
                                    continue;
                                }

                                // determine if rt and mz of the SIIData is in the mass trace of the feature
                                if (isInRange(siiExpMz, exFt.getLMZ(), exFt.getRMZ())
                                        && isInRange(siiRt, exFt.getBRT(), exFt.getURT())) {
                                    matchManager.registerMatch(ft, ftList.getId(), sd);
                                }
                            }
                        }
                    }
                }

                long flFeaturesCount                   = matchManager.getFeatureCount(ftList.getId());
                long flFeaturesWithMatchCount          = matchManager.getFeaturesWithMatchCount(ftList.getId());
                long flFeaturesWithoutMatchCount       = flFeaturesCount - flFeaturesWithMatchCount;
                long flFeaturesMultimapping            = matchManager.getFeaturesMultimappingCount(ftList.getId());
                long flIdentificationCount             = matchManager.getIdentificationsCount(ftList.getId());
                long flIdentificationsAssignedCount    = matchManager.getIdentificationsAssignedCount(ftList.getId());
                long flIdentificationsNotAssignedCount = flIdentificationCount - flIdentificationsAssignedCount;
                long flIdentificationMultimapping      =
                    matchManager.getIdentificationsMultimappingCount(ftList.getId());

                System.out.println("MzqProcessorFactory -- finish processing FeatureList: " + ftList.getId());
                System.out.println("Matched features: " + flFeaturesWithMatchCount + ". Unmatched features: "
                                   + flFeaturesWithoutMatchCount + ". Total features: " + flFeaturesCount
                                   + ". Multimapping features: " + flFeaturesMultimapping + ".");
                System.out.println("Assigned identifications: " + flIdentificationsAssignedCount
                                   + ". Unassigned identifications: " + flIdentificationsNotAssignedCount
                                   + ". Total identifications: " + flIdentificationCount + ".");
                System.out.println("Multimapping identifications: " + flIdentificationMultimapping);
            }

            long featuresCount                   = matchManager.getFeatureCount();
            long featuresWithMatchCount          = matchManager.getFeaturesWithMatchCount();
            long featuresWithoutMatchCount       = featuresCount - featuresWithMatchCount;
            long featuresMultimapping            = matchManager.getFeaturesMultimappingCount();
            long identificationCount             = matchManager.getIdentificationsCount();
            long identificationsAssignedCount    = matchManager.getIdentificationsAssignedCount();
            long identificationsNotAssignedCount = identificationCount - identificationsAssignedCount;
            long identificationMultimapping      = matchManager.getIdentificationsMultimappingCount();

            System.out.println("MzqProcessorFactory -- finish processing feature lists.");
            System.out.println("Matched features: " + featuresWithMatchCount + ". Unmatched features: "
                               + featuresWithoutMatchCount + ". Total features: " + featuresCount
                               + ". Multimapping features: " + featuresMultimapping + ".");
            System.out.println("Assigned identifications: " + identificationsAssignedCount
                               + ". Unassigned identifications: " + identificationsNotAssignedCount
                               + ". Total identifications: " + identificationCount + ".");
            System.out.println("Multimapping identifications: " + identificationMultimapping);
        }

        @Override
        public Map<String, List<SIIData>> getFeatureToSIIsMap() {
            return matchManager.getMatchMap();
        }

        @Override
        public SearchDatabase getSearchDatabase() {
            return searchDB;
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
