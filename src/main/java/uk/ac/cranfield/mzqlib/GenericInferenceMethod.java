//~ Formatted by Jindent --- http://www.jindent.com
package uk.ac.cranfield.mzqlib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jun Fan@cranfield
 */
public final class GenericInferenceMethod {
    private GenericInferenceMethod() {
    }

    /**
     * assign the middle data structure by the quantitation values from the
     * lower level
     */
    private static Map<String, List<Double>> assignMiddleStructure(final List<String> assayIDs,
                                                                   final List<Map<String, Double>> lowLevelQuant) {
        Map<String, List<Double>> tmp = initializeMiddleStructure(assayIDs);

        for (Map<String, Double> quant : lowLevelQuant) {
            for (String assayID : assayIDs) {
                if (quant.containsKey(assayID)) {
                    tmp.get(assayID).add(quant.get(assayID));

//                  }else{
//                      tmp.get(assayID).add(0d);
                }
            }
        }

        return tmp;
    }

    /**
     * initialize the middle data structure, keys are assay IDs and values are
     * the list of related values
     */
    private static Map<String, List<Double>> initializeMiddleStructure(final List<String> assayIDs) {
        Map<String, List<Double>> tmp = new HashMap<>();

        for (String assayID : assayIDs) {
            List<Double> value = new ArrayList<>();

            tmp.put(assayID, value);
        }

        return tmp;
    }

    /**
     * initialize the ret value
     */
    private static Map<String, Double> initializeRet(final List<String> names) {
        Map<String, Double> ret = new HashMap<>();

        for (String name : names) {
            ret.put(name, 0d);
        }

        return ret;
    }

    /**
     * The mean method
     *
     * @param lowLevelQuant low level quantlayer
     * @param assayIDs      id list of assay
     *
     * @return mean
     */
    public static Map<String, Double> mean(final List<Map<String, Double>> lowLevelQuant, final List<String> assayIDs) {
        Map<String, Double>       ret = initializeRet(assayIDs);
        Map<String, List<Double>> tmp = assignMiddleStructure(assayIDs, lowLevelQuant);

        for (String assayID : assayIDs) {
            List<Double> list = tmp.get(assayID);

            ret.put(assayID, Utils.sum(list) / list.size());
        }

        return ret;
    }

    /**
     * The median method
     *
     * @param lowLevelQuant low level quantlayer
     * @param assayIDs      id list of assay
     *
     * @return median
     */
    public static Map<String, Double> median(final List<Map<String, Double>> lowLevelQuant,
                                             final List<String> assayIDs) {
        Map<String, Double>       ret = initializeRet(assayIDs);
        Map<String, List<Double>> tmp = assignMiddleStructure(assayIDs, lowLevelQuant);

        // assayID, quantitation
        for (String assayID : assayIDs) {
            List<Double> list = tmp.get(assayID);

            ret.put(assayID, Utils.median(list));
        }

        return ret;
    }

    /**
     * The sum method
     *
     * @param lowLevelQuant low level quantlayer
     * @param assayIDs      id list of assay
     *
     * @return sum
     */
    public static Map<String, Double> sum(final List<Map<String, Double>> lowLevelQuant, final List<String> assayIDs) {
        Map<String, Double>       ret = initializeRet(assayIDs);
        Map<String, List<Double>> tmp = assignMiddleStructure(assayIDs, lowLevelQuant);

        for (String assayID : assayIDs) {
            List<Double> list = tmp.get(assayID);

            ret.put(assayID, Utils.sum(list));
        }

        return ret;
    }

    /**
     * Calculate the weighted mean
     *
     * @param lowLevelQuant low level quantlayer
     * @param assayIDs      id list of assay
     * @param count         count
     *
     * @return weighted mean
     */
    public static Map<String, Double> weightedAverage(final List<Map<String, Double>> lowLevelQuant,
                                                      final List<String> assayIDs, final List<Integer> count) {
        Map<String, Double>       ret = initializeRet(assayIDs);
        Map<String, List<Double>> tmp = initializeMiddleStructure(assayIDs);

        for (int i = 0; i < lowLevelQuant.size(); i++) {
            Map<String, Double> quant = lowLevelQuant.get(i);

            for (String assayID : assayIDs) {
                if (quant.containsKey(assayID)) {
                    tmp.get(assayID).add(quant.get(assayID) * count.get(i));
                }
            }
        }

        int totalCount = 0;

        for (int abc : count) {
            totalCount += abc;
        }

        for (String assayID : assayIDs) {
            List<Double> list = tmp.get(assayID);
            int          len  = list.size();

            if (len == 0) {
                continue;
            }

            double sum = 0;

            for (Double value : list) {
                sum += value;
            }

            ret.put(assayID, sum / totalCount);
        }

        return ret;
    }
}
