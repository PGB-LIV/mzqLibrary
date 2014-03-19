/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.idmapper;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import uk.ac.ebi.jmzidml.MzIdentMLElement;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.SearchDatabase;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationResult;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;
import uk.ac.liv.mzqlib.idmapper.data.SIIData;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 24-Jun-2013 14:07:02
 */
public class MzidProcessorFactory {

    //MzIdentMLUnmarshaller um;
    private static final MzidProcessorFactory instance = new MzidProcessorFactory();

    private MzidProcessorFactory() {
    }

//    public MzidProcessorFactory(MzIdentMLUnmarshaller mzidUm) {
//        this.um = mzidUm;
//    }
    public static MzidProcessorFactory getInstance() {
        return instance;
    }

    public MzidProcessor buildMzidProcessor(File mzidFile) {
        return new MzidProcessorImpl(mzidFile);
    }

    private class MzidProcessorImpl implements MzidProcessor {

        private File mzidFile = null;
        private MzIdentMLUnmarshaller umarsh = null;
        private Map<String, List<SIIData>> pepModStringToSIIsMap = new HashMap<>();
        private Map<String, List<String>> pepModStringToProtAccsMap;
        //private Map<SIIData, String> SIIToPeptideMap = new HashMap<SIIData, String>();
        private TIntObjectMap<List<SIIData>> RtToSIIsMap = new TIntObjectHashMap<>();
        private SearchDatabase searchDB;
        /*
         * Constructor
         */

        private MzidProcessorImpl(File mzidFile) {
            if (mzidFile == null) {
                throw new IllegalStateException("mzIdentML file must not be null");
            }
            if (!mzidFile.exists()) {
                throw new IllegalStateException("mzIdentML file does not exist: " + mzidFile.getAbsolutePath());
            }

            this.mzidFile = mzidFile;

            this.umarsh = new MzIdentMLUnmarshaller(this.mzidFile);

            searchDB = umarsh.unmarshal(MzIdentMLElement.SearchDatabase);

            Iterator<SpectrumIdentificationResult> itSIR = umarsh.unmarshalCollectionFromXpath(MzIdentMLElement.SpectrumIdentificationResult);

            while (itSIR.hasNext()) {
                SpectrumIdentificationResult sir = itSIR.next();

                // get retention time from cvParam "spectrum title", return Double.NaN if no "spectrum title"
                double rt = getRetentionTime(sir);

                if (rt == Double.NaN) {
                    throw new IllegalStateException("Cannot find retention time information in SpectrumIdentificationResult \"" + sir.getId() + "\"");
                }

                List<SpectrumIdentificationItem> siis = sir.getSpectrumIdentificationItem();

                for (SpectrumIdentificationItem sii : siis) {
                    SIIData sd = new SIIData(sii, this.umarsh);

                    // generate peptide mod string
                    //String pepModString = createPeptideModString(umarsh, sii.getPeptideRef());

                    //sd.setPeptideModString(pepModString);
                    String pepModString = sd.getPeptideModString();

                    String pep[] = pepModString.split("_", 2);

                    sd.setSequence(pep[0]);

                    // set the retention time before adding SIIData into list
                    sd.setRetentionTime(rt);

                    // set the mzid file name to each SIIData
                    sd.setMzidFn(this.mzidFile.getName());

                    // set the conditions: rank = 1 and passThreshold = true
                    if (sd.getRank() == 1 && sd.isPassThreshold()) {

                        //String pepModString = sd.getPeptideModString();
                        List<SIIData> pepSiiDataList;
                        if (pepModString != null) {
                            pepSiiDataList = pepModStringToSIIsMap.get(pepModString);
                            if (pepSiiDataList == null) {
                                pepSiiDataList = new ArrayList();
                                pepModStringToSIIsMap.put(pepModString, pepSiiDataList);
                            }
                            pepSiiDataList.add(sd);
                        }

                        int intRt = (int) rt;
                        List<SIIData> rtSiiDataList;
                        if (rt != Double.NaN) {
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

//        @Override
//        public Map getPeptideModStringToProtAccessionsMap() {
//            if (pepModStringToProtAccsMap == null) {
//                pepModStringToProtAccsMap = new HashMap<>();
//            }
//
//            for (String pepMod : pepModStringToSIIsMap.keySet()) {
//                List<String> protAccList = pepModStringToProtAccsMap.get(pepMod);
//
//                List<SIIData> siiDataList = pepModStringToSIIsMap.get(pepMod);
//                if (siiDataList != null) {
//                    for (SIIData sd : siiDataList) {
//                        List<PeptideEvidenceRef> pepEviRefs = sd.getPeptideEvidenceRef();
//                        if (pepEviRefs != null) {
//                            for (PeptideEvidenceRef pepEviRef : pepEviRefs) {
//                                String protAcc = pepEviRef.getPeptideEvidence().getDBSequence().getAccession();
//                                if (protAccList == null) {
//                                    protAccList = new ArrayList();
//                                    pepModStringToProtAccsMap.put(pepMod, protAccList);
//                                }
//                                protAccList.add(protAcc);
//                            }
//                        }
//                    }
//                }
//
//            }
//
//            return pepModStringToProtAccsMap;
//        }
        @Override
        public TIntObjectMap getRtToSIIsMap() {
            return RtToSIIsMap;
        }

    }

//    private String createPeptideModString(MzIdentMLUnmarshaller um,
//                                          String pepRef) {
//        String modString = null;
//        try {
//            Peptide peptide = um.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.Peptide.class, pepRef);
//            String pepSeq = peptide.getPeptideSequence();
//            //this.setSequence(pepSeq);
//            List<Modification> mods = peptide.getModification();
//            modString = "_";
//            for (Modification mod : mods) {
//                modString = modString + mod.getLocation().toString() + "_";
//                List<CvParam> cps = mod.getCvParam();
//                for (CvParam cp : cps) {
//                    modString = modString + cp.getAccession() + "_" + cp.getName() + "_";
//                }
//            }
//            modString = pepSeq + modString;
//            return modString;
//        }
//        catch (JAXBException ex) {
//            Logger.getLogger(SIIData.class.getName()).log(Level.SEVERE, null, ex);
//            System.out.println("Couldn't get peptide object: " + pepRef + " -- " + ex.getMessage());
//            return modString;
//        }
//
//    }
    /**
     *
     * @param sir
     *
     * @return retention time (in minute) in the cvParam of SpectrumIdentificationResult with accession="MS:1000016"
     */
    private double getRetentionTime(SpectrumIdentificationResult sir) {
        double rt = Double.NaN;

        List<CvParam> cvParams = sir.getCvParam();
        for (CvParam cp : cvParams) {
            if (cp.getAccession().equals("MS:1000016")) {
                String value = cp.getValue();
                String unit = cp.getUnitName();
                switch (unit.toLowerCase()) {
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
