/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.idmapper.data;

import java.util.List;
import uk.ac.liv.jmzqml.model.mzqml.Feature;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 06-Mar-2014 11:24:28
 */
public class ExtendedFeature extends Feature {

    private double lmz; // left boundary of mz
    private double rmz; // right boundary of mz
    private double urt; // up boundary of rt (min)
    private double brt; // bottom boundary of rt (min)
    private final Feature feature;

    public ExtendedFeature(Feature ft) {
        super();
        feature = ft;
        List<Double> massT = feature.getMassTrace();

        brt = massT.get(0);
        lmz = massT.get(1);
        urt = massT.get(2);
        rmz = massT.get(3);
    }

    public double getLMZ() {
        return lmz;
    }

    public double getRMZ() {
        return rmz;
    }

    public double getURT() {
        return urt;
    }

    public double getBRT() {
        return brt;
    }

}
