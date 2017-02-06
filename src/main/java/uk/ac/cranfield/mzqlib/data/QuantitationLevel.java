package uk.ac.cranfield.mzqlib.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.cranfield.mzqlib.GenericInferenceMethod;

/**
 * QuantitationLevel class.
 *
 * @author Jun Fan@cranfield
 */
public class QuantitationLevel {
    private static QuantitationMisc misc = new QuantitationMisc();

    /**
     * Constant.
     */
    public static final int SUM = 0;

    /**
     * Constant.
     */
    public static final int MEAN = 1;

    /**
     * Constant.
     */
    public static final int MEDIAN = 2;

    /**
     * Constant.
     */
    public static final int WEIGHTED_AVERAGE = 3;

    /**
     * The first map: keys are quantitation name
     * The second map: keys are assay names and the values are the actual values
     * (quantities, study variables or ratios)
     */
    private Map<String, Map<String, Double>> quantities     = new HashMap<>();
    private Map<String, Map<String, Double>> studyVariables = new HashMap<>();
    private Map<String, Double>              ratios         = new HashMap<>();
    private Map<String, Double>              globals        = new HashMap<>();
    private boolean                          hasRatio       = false;
    private boolean                          hasGlobal      = false;

    /**
     * Calculate quantitation values.
     *
     * @param quantitationNames name.
     * @param assayIDs          assay id.
     * @param type              type.
     */
    public final void calculateQuantitation(final Set<String> quantitationNames, final List<String> assayIDs,
                                            final int type) {
        try {
            Method                  m       = this.getClass()
                                                  .getDeclaredMethod(misc.getMethodName(this.getClass()), new Class[0]);
            List<QuantitationLevel> results = (List<QuantitationLevel>) m.invoke(this, new Object[0]);
            List<Integer>           count   = new ArrayList<>();

            for (String name : quantitationNames) {
                List<Map<String, Double>> lowerLevelQuant = new ArrayList<>();

                for (QuantitationLevel one : results) {
                    if (type == WEIGHTED_AVERAGE) {
                        count.add(one.getCount());
                    }

                    lowerLevelQuant.add(one.getQuantities(name));
                }

                Map<String, Double> quan = null;

                switch (type) {
                case SUM :
                    quan = GenericInferenceMethod.sum(lowerLevelQuant, assayIDs);

                    break;

                case MEAN :
                    quan = GenericInferenceMethod.mean(lowerLevelQuant, assayIDs);

                    break;

                case MEDIAN :
                    quan = GenericInferenceMethod.median(lowerLevelQuant, assayIDs);

                    break;

                case WEIGHTED_AVERAGE :
                    quan = GenericInferenceMethod.weightedAverage(lowerLevelQuant, assayIDs, count);

                    break;

                default :
                    throw new IllegalStateException("Unrecognized quantitation method, exit");
                }

                this.setQuantities(name, quan);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                 | SecurityException ex) {
            Logger.getLogger(QuantitationLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get count.
     *
     * @return count.
     */
    public int getCount() {
        return 1;
    }

    /**
     * Get global value.
     *
     * @param name name.
     *
     * @return global value.
     */
    public final Double getGlobal(final String name) {
        if (globals.containsKey(name)) {
            return globals.get(name);
        }

        return null;
    }

    /**
     * Get if contain global quantitation.
     *
     * @return true if global quantitation exists.
     */
    public final boolean hasGlobal() {
        return hasGlobal;
    }

    /**
     * Set global value.
     *
     * @param name  name.
     * @param ratio global value.
     */
    public final void setGlobal(final String name, final Double ratio) {
        globals.put(name, ratio);
        hasGlobal = true;
    }

    /**
     * Get if has quantitation value or not.
     *
     * @param quantitationName name.
     *
     * @return true if specified quantitation exists.
     */
    public final boolean hasQuantitation(final String quantitationName) {
        if (quantities.containsKey(quantitationName)) {
            return true;
        }

        return false;
    }

    /**
     * Get quantity value map.
     *
     * @param name name.
     *
     * @return quantity value map.
     */
    public final Map<String, Double> getQuantities(final String name) {
        return quantities.get(name);
    }

    /**
     * Set quantity map.
     *
     * @param name        name.
     * @param quantitions quantity map.
     */
    public final void setQuantities(final String name, final Map<String, Double> quantitions) {
        quantities.put(name, quantitions);
    }

    /**
     * Get quantity value.
     *
     * @param name    name.
     * @param assayID assay id.
     *
     * @return the quantity value.
     */
    public final Double getQuantity(final String name, final String assayID) {
        if (quantities.containsKey(name) && quantities.get(name).containsKey(assayID)) {
            return quantities.get(name).get(assayID);
        }

        return null;
    }

    /**
     * Get ratio value.
     *
     * @param name name.
     *
     * @return ratio value.
     */
    public final Double getRatio(final String name) {
        if (ratios.containsKey(name)) {
            return ratios.get(name);
        }

        return null;
    }

    /**
     * Get if contain ratio.
     *
     * @return true if ratio exists.
     */
    public final boolean hasRatio() {
        return hasRatio;
    }

    /**
     * Set ratio value.
     *
     * @param name  name.
     * @param ratio ratio value.
     */
    public final void setRatios(final String name, final Double ratio) {
        ratios.put(name, ratio);
        hasRatio = true;
    }

    /**
     * Get if contain study variable or not.
     *
     * @param quantitationName name.
     *
     * @return true if specified quantitation has study variables.
     */
    public final boolean hasSV(final String quantitationName) {
        if (studyVariables.containsKey(quantitationName)) {
            return true;
        }

        return false;
    }

    /**
     * Get study variable quantity value map.
     *
     * @param name name.
     *
     * @return quantity value map.
     */
    public final Map<String, Double> getStudyVariableQuantities(final String name) {
        return studyVariables.get(name);
    }

    /**
     * Get study variable quantity value.
     *
     * @param name name.
     * @param sv   study variable.
     *
     * @return study variable value.
     */
    public final Double getStudyVariableQuantity(final String name, final String sv) {
        if (studyVariables.containsKey(name) && studyVariables.get(name).containsKey(sv)) {
            return studyVariables.get(name).get(sv);
        }

        return null;
    }

    /**
     * Set study variable value map.
     *
     * @param name     name.
     * @param svValues study variable value map.
     */
    public final void setStudyVariables(final String name, final Map<String, Double> svValues) {
        studyVariables.put(name, svValues);
    }
}


class QuantitationMisc {
    private final Map<Class, String> methods = new HashMap<>();

    QuantitationMisc() {
        methods.put(ProteinData.class, "getPeptides");
        methods.put(PeptideData.class, "getFeatures");

//      methods.put(ProteinData.class, "getAllPeptides");
//      methods.put(xPeptideConsensus.class, "getPeptides");
//      methods.put(PeptideData.class, "getAllFeatures");
//      methods.put(FeatureData.class, "getIdentifications");
    }

    String getMethodName(final Class clazz) {

//      if(clazz == PeptideData.class && xTracker.study.getPipelineType() == Study.MS2_TYPE) return "getAllIdentifications";
        return methods.get(clazz);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
