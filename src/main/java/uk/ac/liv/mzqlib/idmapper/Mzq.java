
package uk.ac.liv.mzqlib.idmapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.EvidenceRef;
import uk.ac.liv.jmzqml.model.mzqml.Feature;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 24-Jun-2013 14:06:51
 */
public class Mzq {

    private MzQuantMLUnmarshaller um;

    public Mzq(MzQuantMLUnmarshaller mzqUm) {
        this.um = mzqUm;
    }

    /**
     *
     * @return a HashMap with PeptideConsensus id as key and a list of SimpleFeature as value. SimpleFeature contains charge,
     *         feature id, mz value and retention time window.
     *
     * @throws JAXBException
     */
    public HashMap<String, List<SimpleFeature>> getPepIdFeature()
            throws JAXBException {

        HashMap<String, List<SimpleFeature>> retMap = new HashMap();

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

    public static class SimpleFeature {

        public String charge;
        public String ftId;
        public double mz;
        public double retWin;

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
