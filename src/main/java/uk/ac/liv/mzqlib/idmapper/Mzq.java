
package uk.ac.liv.mzqlib.idmapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.EvidenceRef;
import uk.ac.liv.jmzqml.model.mzqml.Feature;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 * The wrapper class for mzQuantML file.
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 24-Jun-2013 14:06:51
 */
public class Mzq {

    private MzQuantMLUnmarshaller um;

    /**
     * Constructor of Mzq class.
     *
     * @param mzqUm the instance of MzQuantMLUnmarshaller
     */
    public Mzq(MzQuantMLUnmarshaller mzqUm) {
        this.um = mzqUm;
    }

    /**
     * Get map of PeptideConsensus id to list of SimpleFeature.
     *
     * @return Map<String, List<SimpleFeature>>
     *
     * @throws JAXBException
     */
    public Map<String, List<SimpleFeature>> getPepIdFeature()
            throws JAXBException {

        Map<String, List<SimpleFeature>> retMap = new HashMap();

        Iterator<PeptideConsensusList> pepConListIter = this.um.unmarshalCollectionFromXpath(MzQuantMLElement.PeptideConsensusList);
        while (pepConListIter.hasNext()) {
            PeptideConsensusList pepConList = pepConListIter.next();
            List<PeptideConsensus> pepCons = pepConList.getPeptideConsensus();

            for (PeptideConsensus pepCon : pepCons) {
                String pepId = pepCon.getId();
                String charge = pepCon.getCharge().get(0);

                List<EvidenceRef> eviRefList = pepCon.getEvidenceRef();
                for (EvidenceRef eviRef : eviRefList) {

                    String ftRef = eviRef.getFeatureRef();

                    Feature ft = this.um.unmarshal(uk.ac.liv.jmzqml.model.mzqml.Feature.class, ftRef);
                    double mz = ft.getMz();
                    double rw = this.getRetentionWindow(ft.getMassTrace());

                    List<SimpleFeature> sfList = retMap.get(pepId);
                    if (sfList == null) {
                        // initialise a new list of SimpleFeature
                        sfList = new ArrayList();
                        retMap.put(pepId, sfList);
                    }
                    SimpleFeature sf = new SimpleFeature(charge, ftRef, mz, rw);
                    sfList.add(sf);
                }
            }
        }
        return retMap;
    }

    /**
     * The inner static class SimpleFeature.
     *
     * SimpleFeature contains charge,feature id, m/z value and retention time window.
     */
    public static class SimpleFeature {

        /**
         * Feature charge
         */
        public String charge;

        /**
         * Feature ID
         */
        public String ftId;

        /**
         * Feature m/z
         */
        public double mz;

        /**
         * Feature retention time window
         */
        public double retWin;

        /**
         * Constructor of SimpleFeature by initial values.
         *
         * @param chr  feature charge
         * @param ftId feature id
         * @param mz   feature m/z
         * @param rw   feature retention time window
         */
        public SimpleFeature(String chr, String ftId, double mz, double rw) {
            this.charge = chr;
            this.ftId = ftId;
            this.mz = mz;
            this.retWin = rw;
        }

    }

    private double getRetentionWindow(List<Double> massTrace) {
        return Math.abs(massTrace.get(0) - massTrace.get(2));
    }

}
