/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.idmapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import uk.ac.ebi.jmzidml.MzIdentMLElement;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationResult;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 24-Jun-2013 14:07:02
 */
public class Mzid {

    MzIdentMLUnmarshaller um;

    public Mzid(MzIdentMLUnmarshaller mzidUm) {
        this.um = mzidUm;
    }

    public HashMap<String, SIIData> getPeptideSIIData() {
        HashMap<String, SIIData> retMap = new HashMap();

        Iterator<SpectrumIdentificationResult> sirIter = um.unmarshalCollectionFromXpath(MzIdentMLElement.SpectrumIdentificationResult);
        while (sirIter.hasNext()) {
            SpectrumIdentificationResult sir = sirIter.next();

            // retrive retention time from cvParam of SpectrumIdentificationResult -- MS:1000796
            double rt = getRetentionTime(sir);
            List<SpectrumIdentificationItem> siiList = sir.getSpectrumIdentificationItem();
            for (SpectrumIdentificationItem sii : siiList) {
                // retrieve peptide sequence from each SpectrumIdentificationItem
                String pepSeq = sii.getPeptide().getPeptideSequence();

                // SIIData with retention time equals 0 as SII doesn't contain retention time
                SIIData siiData = getSIIData(sii);

                // set the retention time
                siiData.rt = rt;

                if (retMap.get(pepSeq) == null) {
                    retMap.put(pepSeq, siiData);
                }
                else {
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
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param sir
     *
     * @return retention time in the cvParam of SpectrumIdentificationResult with accession="MS:1000796"
     */
    private double getRetentionTime(SpectrumIdentificationResult sir) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static class SIIData {

        public String charge;
        public double mz;
        public double rt;
        public String siiID;

        public SIIData(String charge, double mz, double rt, String siiID) {
            this.charge = charge;
            this.mz = mz;
            this.rt = rt;
            this.siiID = siiID;
        }

    }

}
