
package uk.ac.liv.pgb.mzqlib.idmapper;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import uk.ac.ebi.jmzidml.MzIdentMLElement;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.SearchDatabase;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationResult;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;
import uk.ac.liv.pgb.mzqlib.idmapper.data.SIIData;

/**
 * The MzidProcessorFactory class parse the input mzIdentML file and create an
 * MzidProcessor instance.
 *
 * @author Da Qi
 * @since 24-Jun-2013 14:07:02
 */
public class MzidProcessorFactory {

    //MzIdentMLUnmarshaller um;
    private static final MzidProcessorFactory instance
            = new MzidProcessorFactory();

    private MzidProcessorFactory() {
    }

//    public MzidProcessorFactory(MzIdentMLUnmarshaller mzidUm) {
//        this.um = mzidUm;
//    }
    /**
     * The method provides a static instance of the class MzidProcessorFactory.
     * User will call this method for accessing to buildMzidProcessor method.
     *
     * @return the static instance of MzidProcessorFactory
     */
    public static MzidProcessorFactory getInstance() {
        return instance;
    }

    /**
     * The method builds a MzidProcessor instance from an input mzIdentML file.
     *
     * @param mzidFile input mzIdentML file
     *
     * @return the MzidProcessor instance
     */
    public MzidProcessor buildMzidProcessor(final File mzidFile) {
        return new MzidProcessorImpl(mzidFile);
    }

    private static class MzidProcessorImpl implements MzidProcessor {

        private File mzidFile = null;
        private MzIdentMLUnmarshaller umarsh = null;
        private final Map<String, List<SIIData>> pepModStringToSIIsMap
                = new HashMap<>();
        private final TIntObjectMap<List<SIIData>> RtToSIIsMap
                = new TIntObjectHashMap<>();
        private SearchDatabase searchDB;

        /*
         * Constructor
         */
        private MzidProcessorImpl(final File mzidFile) {
            if (mzidFile == null) {
                throw new IllegalStateException(
                        "mzIdentML file must not be null");
            }
            if (!mzidFile.exists()) {
                throw new IllegalStateException(
                        "mzIdentML file does not exist: " + mzidFile.
                        getAbsolutePath());
            }

            this.mzidFile = mzidFile;

            this.umarsh = new MzIdentMLUnmarshaller(this.mzidFile);

            searchDB = umarsh.unmarshal(MzIdentMLElement.SearchDatabase);

            Iterator<SpectrumIdentificationResult> itSIR = umarsh.
                    unmarshalCollectionFromXpath(
                            MzIdentMLElement.SpectrumIdentificationResult);

            while (itSIR.hasNext()) {
                SpectrumIdentificationResult sir = itSIR.next();

                // get retention time from cvParam "spectrum title", return Double.NaN if no "spectrum title"
                double rt = getRetentionTime(sir);

                if (Double.isNaN(rt)) {
                    throw new IllegalStateException(
                            "Cannot find retention time information in SpectrumIdentificationResult \""
                            + sir.getId() + "\"");
                }

                List<SpectrumIdentificationItem> siis = sir.
                        getSpectrumIdentificationItem();

                for (SpectrumIdentificationItem sii : siis) {
                    SIIData sd = new SIIData(sii, this.umarsh);

                    // generate peptide mod string
                    //String pepModString = createPeptideModString(umarsh, sii.getPeptideRef());
                    //sd.setPeptideModString(pepModString);
                    String pepModString = sd.getPeptideModString();

                    String pep[] = pepModString.split("_", 2); // separate modString into two parts: peptide sequence and mods

                    sd.setSequence(pep[0]);

                    // set the retention time before adding SIIData into list
                    sd.setRetentionTime(rt);

                    // set the mzid file name to each SIIData
                    sd.setMzidFn(this.mzidFile.getName());

                    // set the conditions: rank = 1 and passThreshold = true
                    if (sd.getRank() == 1 && sd.isPassThreshold()) {

                        //String pepModString = sd.getPeptideModString();
                        List<SIIData> pepSiiDataList;

                        pepSiiDataList = pepModStringToSIIsMap.get(pepModString);
                        if (pepSiiDataList == null) {
                            pepSiiDataList = new ArrayList();
                            pepModStringToSIIsMap.put(pepModString,
                                                      pepSiiDataList);
                        }
                        pepSiiDataList.add(sd);

                        int intRt = (int) rt;
                        List<SIIData> rtSiiDataList;
                        if (!Double.isNaN(rt)) {
                            rtSiiDataList = RtToSIIsMap.get(intRt);
                            if (rtSiiDataList == null) {
                                rtSiiDataList = new ArrayList();
                                RtToSIIsMap.put(intRt, rtSiiDataList);
                            }
                            rtSiiDataList.add(sd);
                        }
                    }
                }
            }

        }

        @Override
        public SearchDatabase getSearchDatabase() {
            return searchDB;
        }

        @Override
        public Map getPeptideModStringToSIIsMap() {
            return pepModStringToSIIsMap;
        }

        @Override
        public TIntObjectMap getRtToSIIsMap() {
            return RtToSIIsMap;
        }

    }

    /**
     *
     * @param sir
     *
     * @return retention time (in minute) in the cvParam of
     *         SpectrumIdentificationResult with accession="MS:1000016"
     */
    private static double getRetentionTime(
            final SpectrumIdentificationResult sir) {
        double rt = Double.NaN;

        List<CvParam> cvParams = sir.getCvParam();
        for (CvParam cp : cvParams) {
            if (cp.getAccession().equals("MS:1000016")) {
                String value = cp.getValue();
                String unit = cp.getUnitName();
                if (unit == null || unit.equals("")) {
                    return rt;
                }

                switch (unit.toLowerCase(Locale.ENGLISH)) {
                    case "second":
                        return Double.parseDouble(value) / 60;
                    case "minute":
                        return Double.parseDouble(value);
                    case "hour": // rare case?
                        return Double.parseDouble(value) * 60;
                    default:
                        return rt;
                }
            }
        }

        return rt;
    }

}
