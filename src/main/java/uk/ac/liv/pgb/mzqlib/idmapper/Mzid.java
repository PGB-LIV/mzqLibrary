
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

    MzIdentMLUnmarshaller um;

    /**
     * Constructor of Mzid class.
     *
     * @param mzidUm MzIdentMLUnmarshaller instance
     */
    public Mzid(MzIdentMLUnmarshaller mzidUm) {
        this.um = mzidUm;
    }

    /**
     * Get map of peptide sequence to SIIData.
     *
     * @return Map&lt;String, SIIData&gt;
     */
    public Map<String, SIIData> getPeptideSIIData() {
        Map<String, SIIData> retMap = new HashMap();

        Iterator<SpectrumIdentificationResult> sirIter = um.
                unmarshalCollectionFromXpath(
                        MzIdentMLElement.SpectrumIdentificationResult);
        while (sirIter.hasNext()) {
            SpectrumIdentificationResult sir = sirIter.next();

            // retrive retention time from cvParam of SpectrumIdentificationResult -- MS:1000796
            double rt = getRetentionTime(sir);
            List<SpectrumIdentificationItem> siiList = sir.
                    getSpectrumIdentificationItem();
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

    private SIIData getSIIData(SpectrumIdentificationItem sii) {

        int rank = sii.getRank();
        boolean passTh = sii.isPassThreshold();
        if (rank == 1 && passTh) {
            String charge = String.valueOf(sii.getChargeState());
            double mz = sii.getExperimentalMassToCharge();
            String id = sii.getId();
            return new SIIData(charge, mz, 0, id);
        } else {
            return null;
        }
    }

    /**
     * 
     * @param sir
     *
     * @return retention time in the cvParam of SpectrumIdentificationResult
     *         with accession="MS:1000796"
     */
    private double getRetentionTime(SpectrumIdentificationResult sir) {
        throw new UnsupportedOperationException("Not yet implemented");
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
        public SIIData(String charge, double mz, double rt, String siiID) {
            this.charge = charge;
            this.mz = mz;
            this.rt = rt;
            this.siiID = siiID;
        }

        /**
         * @return the charge
         */
        public String getCharge() {
            return charge;
        }

        /**
         * @param charge the charge to set
         */
        public void setCharge(String charge) {
            this.charge = charge;
        }

        /**
         * @return the mz
         */
        public double getMz() {
            return mz;
        }

        /**
         * @param mz the mz to set
         */
        public void setMz(double mz) {
            this.mz = mz;
        }

        /**
         * @return the rt
         */
        public double getRt() {
            return rt;
        }

        /**
         * @param rt the rt to set
         */
        public void setRt(double rt) {
            this.rt = rt;
        }

        /**
         * @return the siiID
         */
        public String getSiiID() {
            return siiID;
        }

        /**
         * @param siiID the siiID to set
         */
        public void setSiiID(String siiID) {
            this.siiID = siiID;
        }

    }

}
