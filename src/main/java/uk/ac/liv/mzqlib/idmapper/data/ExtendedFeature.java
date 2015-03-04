
package uk.ac.liv.mzqlib.idmapper.data;

import java.util.List;
import uk.ac.liv.jmzqml.model.mzqml.AbstractParam;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.Feature;
import uk.ac.liv.jmzqml.model.mzqml.RawFile;
import uk.ac.liv.jmzqml.model.mzqml.UserParam;

/**
 * Subclass of Feature
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 06-Mar-2014 11:24:28
 */
public class ExtendedFeature extends Feature {

    private final double lmz; // left boundary of mz
    private final double rmz; // right boundary of mz
    private final double urt; // up boundary of rt (min)
    private final double brt; // bottom boundary of rt (min)
    private final Feature feature;

    /**
     * Constructor of ExtendedFeature base on Feature
     *
     * @param ft Feature
     */
    public ExtendedFeature(Feature ft) {
        super();
        feature = ft;
        List<Double> massT = feature.getMassTrace();

        if (massT == null || massT.isEmpty()) {
            brt = (Double.valueOf(ft.getRt()) - 10.0) / 60.0;
            //brt = Double.valueOf(ft.getRt()) - 10;
            lmz = ft.getMz() - 0.05;
            urt = (Double.valueOf(ft.getRt()) + 10.0) / 60.0;
            //urt = Double.valueOf(ft.getRt()) + 10;
            rmz = ft.getMz() + 0.05;
        }
        else {
            brt = massT.get(0);
            lmz = massT.get(1);
            urt = massT.get(2);
            rmz = massT.get(3);
        }
    }

    /**
     * Get left boundary m/z of the feature.
     *
     * @return double value of m/z
     */
    public double getLMZ() {
        return lmz;
    }

    /**
     * Get right boundary m/z of the feature.
     *
     * @return double value of m/z
     */
    public double getRMZ() {
        return rmz;
    }

    /**
     * Get up boundary retention time of the feature.
     *
     * @return double value of retention time
     */
    public double getURT() {
        return urt;
    }

    /**
     * Get bottom boundary retention time of the feature.
     *
     * @return double value of retention time
     */
    public double getBRT() {
        return brt;
    }

    @Override
    public String getCharge() {
        return feature.getCharge();
    }

    @Override
    public RawFile getRawFile() {
        return feature.getRawFile();
    }

    @Override
    public String getId() {
        return feature.getId();
    }

    @Override
    public List<Double> getMassTrace() {
        return feature.getMassTrace();
    }

    @Override
    public List<AbstractParam> getParamGroup() {
        return feature.getParamGroup();
    }

    @Override
    public String getRt() {
        return feature.getRt();
    }

    @Override
    public double getMz() {
        return feature.getMz();
    }

    @Override
    public String getChromatogramRefs() {
        return feature.getChromatogramRefs();
    }

    @Override
    public String getSpectrumRefs() {
        return feature.getSpectrumRefs();
    }

    @Override
    public String getRawFileRef() {
        return feature.getRawFileRef();
    }

    @Override
    public List<CvParam> getCvParam() {
        return feature.getCvParam();
    }

    @Override
    public List<UserParam> getUserParam() {
        return feature.getUserParam();
    }

}
