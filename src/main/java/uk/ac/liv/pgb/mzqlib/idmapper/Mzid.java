package uk.ac.liv.pgb.mzqlib.idmapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.ebi.jmzidml.MzIdentMLElement;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationResult;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;

/**
 * This is the wrapper class for mzIdentML file.
 *
 * @author Da Qi
 * @since 24-Jun-2013 14:07:02
 */
public class Mzid {
    private MzIdentMLUnmarshaller um;

    /**
     * Constructor of Mzid class.
     *
     * @param mzidUm MzIdentMLUnmarshaller instance
     */
    public Mzid(final MzIdentMLUnmarshaller mzidUm) {
        this.um = mzidUm;
    }

    /**
     * Get map of peptide sequence to SIIData.
     *
     * @return Map&lt;String, SIIData&gt;
     */
    public final Map<String, SIIData> getPeptideSIIData() {
        Map<String, SIIData>                   retMap  = new HashMap();
        Iterator<SpectrumIdentificationResult> sirIter =
            um.unmarshalCollectionFromXpath(MzIdentMLElement.SpectrumIdentificationResult);

        while (sirIter.hasNext()) {
            SpectrumIdentificationResult sir = sirIter.next();

            // retrive retention time from cvParam of SpectrumIdentificationResult -- MS:1000796
            double                           rt      = getRetentionTime(sir);
            List<SpectrumIdentificationItem> siiList = sir.getSpectrumIdentificationItem();

            for (SpectrumIdentificationItem sii : siiList) {

                // retrieve peptide sequence from each SpectrumIdentificationItem
                String pepSeq = sii.getPeptide().getPeptideSequence();

                // SIIData with retention time equals 0 as SII doesn't contain retention time
                SIIData siiData = getSIIData(sii);

                // set the retention time
                siiData.setRt(rt);

                if (retMap.get(pepSeq) == null) {
                    retMap.put(pepSeq, siiData);
                } else {
                    System.out.println("same peptide sequence has different ");
                }
            }
        }

        return retMap;
    }

    /**
     *
     * @param sir
     *
     * @return retention time in the cvParam of SpectrumIdentificationResult
     *         with accession="MS:1000796"
     */
    @SuppressWarnings("unused")
    private double getRetentionTime(final SpectrumIdentificationResult sir) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private SIIData getSIIData(final SpectrumIdentificationItem sii) {
        int     rank   = sii.getRank();
        boolean passTh = sii.isPassThreshold();

        if (rank == 1 && passTh) {
            String charge = String.valueOf(sii.getChargeState());
            double mz     = sii.getExperimentalMassToCharge();
            String id     = sii.getId();

            return new SIIData(charge, mz, 0, id);
        } else {
            return null;
        }
    }

    /**
     * Inner class SIIData to store information from SpectrumIdentificationItem.
     */
    public static class SIIData {

        /**
         * Charge
         */
        private String charge;

        /**
         * m/z
         */
        private double mz;

        /**
         * retention time
         */
        private double rt;

        /**
         * SpectrumIdentificationItem id
         */
        private String siiID;

        /**
         * Constructor of SIIData.
         *
         * @param charge charge
         * @param mz     m/z
         * @param rt     retention time
         * @param siiID  SpectrumIdentification id
         */
        public SIIData(final String charge, final double mz, final double rt, final String siiID) {
            this.charge = charge;
            this.mz     = mz;
            this.rt     = rt;
            this.siiID  = siiID;
        }

        /**
         * @return the charge
         */
        public final String getCharge() {
            return charge;
        }

        /**
         * @param charge the charge to set
         */
        public final void setCharge(final String charge) {
            this.charge = charge;
        }

        /**
         * @return the mz
         */
        public final double getMz() {
            return mz;
        }

        /**
         * @param mz the mz to set
         */
        public final void setMz(final double mz) {
            this.mz = mz;
        }

        /**
         * @return the rt
         */
        public final double getRt() {
            return rt;
        }

        /**
         * @param rt the rt to set
         */
        public final void setRt(final double rt) {
            this.rt = rt;
        }

        /**
         * @return the siiID
         */
        public final String getSiiID() {
            return siiID;
        }

        /**
         * @param siiID the siiID to set
         */
        public final void setSiiID(final String siiID) {
            this.siiID = siiID;
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
