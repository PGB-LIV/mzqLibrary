
package uk.ac.cranfield.mzqlib.data;

import java.util.HashMap;
import java.lang.reflect.*;
import java.util.ArrayList;
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

    /**
     * The first map: keys are quantitation name
     * The second map: keys are assay names and the values are the actual values
     * (quantities, study variables or ratios)
     */
    private Map<String, Map<String, Double>> quantities
            = new HashMap<>();
    private Map<String, Map<String, Double>> studyVariables
            = new HashMap<>();
    private Map<String, Double> ratios = new HashMap<>();
    private Map<String, Double> globals = new HashMap<>();
    private boolean hasRatio = false;
    private boolean hasGlobal = false;

    static private QuantitationMisc misc = new QuantitationMisc();

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
     * Get quantity value.
     *
     * @param name    name.
     * @param assayID assay id.
     *
     * @return the quantity value.
     */
    public Double getQuantity(String name, String assayID) {
        if (quantities.containsKey(name) && quantities.get(name).containsKey(
                assayID)) {
            return quantities.get(name).get(assayID);
        }
        return null;
    }

    /**
     * Get quantity value map.
     *
     * @param name name.
     *
     * @return quantity value map.
     */
    public Map<String, Double> getQuantities(String name) {
        return quantities.get(name);
    }

    /**
     * Get study variable quantity value map.
     *
     * @param name name.
     *
     * @return quantity value map.
     */
    public Map<String, Double> getStudyVariableQuantities(String name) {
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
    public Double getStudyVariableQuantity(String name, String sv) {
        if (studyVariables.containsKey(name) && studyVariables.get(name).
                containsKey(sv)) {
            return studyVariables.get(name).get(sv);
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
    public Double getRatio(String name) {
        if (ratios.containsKey(name)) {
            return ratios.get(name);
        }
        return null;
    }

    /**
     * Get global value.
     *
     * @param name name.
     *
     * @return global value.
     */
    public Double getGlobal(String name) {
        if (globals.containsKey(name)) {
            return globals.get(name);
        }
        return null;
    }

    /**
     * Set quantity map.
     *
     * @param name        name.
     * @param quantitions quantity map.
     */
    public void setQuantities(String name, Map<String, Double> quantitions) {
        quantities.put(name, quantitions);
    }

    /**
     * Set study variable value map.
     *
     * @param name     name.
     * @param svValues study variable value map.
     */
    public void setStudyVariables(String name, Map<String, Double> svValues) {
        studyVariables.put(name, svValues);
    }

    /**
     * Set ratio value.
     *
     * @param name  name.
     * @param ratio ratio value.
     */
    public void setRatios(String name, Double ratio) {
        ratios.put(name, ratio);
        hasRatio = true;
    }

    /**
     * Set global value.
     *
     * @param name  name.
     * @param ratio global value.
     */
    public void setGlobal(String name, Double ratio) {
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
    public boolean hasQuantitation(String quantitationName) {
        if (quantities.containsKey(quantitationName)) {
            return true;
        }
        return false;
    }

    /**
     * Get if contain study variable or not.
     *
     * @param quantitationName name.
     *
     * @return true if specified quantitation has study variables.
     */
    public boolean hasSV(String quantitationName) {
        if (studyVariables.containsKey(quantitationName)) {
            return true;
        }
        return false;
    }

    /**
     * Get if contain ratio.
     *
     * @return true if ratio exists.
     */
    public boolean hasRatio() {
        return hasRatio;
    }

    /**
     * Get if contain global quantitation.
     *
     * @return true if global quantitation exists.
     */
    public boolean hasGlobal() {
        return hasGlobal;
    }

    /**
     * Calculate quantitation values.
     *
     * @param quantitationNames name.
     * @param assayIDs          assay id.
     * @param type              type.
     */
    public void calculateQuantitation(Set<String> quantitationNames,
                                      List<String> assayIDs, int type) {
        try {
            Method m = this.getClass().getDeclaredMethod(misc.getMethodName(
                    this.getClass()), new Class[0]);
            List<QuantitationLevel> results
                    = (List<QuantitationLevel>) m.invoke(this,
                                                         new Object[0]);
            List<Integer> count = new ArrayList<>();
            for (String name : quantitationNames) {
                List<Map<String, Double>> lowerLevelQuant
                        = new ArrayList<>();
                for (QuantitationLevel one : results) {
                    if (type == WEIGHTED_AVERAGE) {
                        count.add(one.getCount());
                    }
                    lowerLevelQuant.add(one.getQuantities(name));
                }
                Map<String, Double> quan = null;
                switch (type) {
                    case SUM:
                        quan = GenericInferenceMethod.sum(lowerLevelQuant,
                                                          assayIDs);
                        break;
                    case MEAN:
                        quan = GenericInferenceMethod.mean(lowerLevelQuant,
                                                           assayIDs);
                        break;
                    case MEDIAN:
                        quan = GenericInferenceMethod.median(lowerLevelQuant,
                                                             assayIDs);
                        break;
                    case WEIGHTED_AVERAGE:
                        quan = GenericInferenceMethod.weightedAverage(
                                lowerLevelQuant, assayIDs, count);
                        break;
                    default:
                        throw new IllegalStateException(
                                "Unrecognized quantitation method, exit");
                }
                this.setQuantities(name, quan);
            }
        } catch (IllegalAccessException | IllegalArgumentException |
                InvocationTargetException | NoSuchMethodException |
                SecurityException ex) {
            Logger.getLogger(QuantitationLevel.class.getName()).
                    log(Level.SEVERE, null, ex);
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

//    public void calculateForSV(){
//        //calculate quantitation for SV
//        for (String name:quantities.keySet()){
//            HashMap<String,Double> values = new HashMap<String, Double>();
//            for(StudyVariable sv:xTracker.study.getMzQuantML().getStudyVariableList().getStudyVariable()){
//                ArrayList<Double> assayValues = new ArrayList<Double>();
//                for(Object obj:sv.getAssayRefs()){
//                    assayValues.add(quantities.get(name).get(((Assay)obj).getId()));
//                }
//                double result = 0d;
//                
//                switch (xTracker.SV_ASSAY_INFERENCE) {
//                    case MEAN:
//                    case WEIGHTED_AVERAGE:
//                        result = Utils.mean(assayValues);
//                        break;
//                    case MEDIAN:
//                        result = Utils.median(assayValues);
//                        break;
//                    case SUM:
//                        result = Utils.sum(assayValues);
//                        break;
//                    default:
//                        System.out.println("Unrecognized calculation method for SV calculation");
//                        System.exit(1);
//                }
//                values.put(sv.getId(), result);
//            }
//            studyVariables.put(name, values);
//        }
//        //calculate ratio for SV
//        for (String name : quantities.keySet()) {
//            HashMap <String,Double> ratioValues = ratios.get(name);
//            if (ratioValues == null) ratioValues = new HashMap<String, Double>();
//            for (xRatio ratio : xTracker.study.getRatios()) {
//                if(ratio.getType().equals(xRatio.ASSAY)) continue;
//            //ratio for SV
//                HashMap <String,Double> svValues = studyVariables.get(name);
//                double numerator = svValues.get(ratio.getNumerator());
//                double denominator = svValues.get(ratio.getDenominator());
//                double ratioValue;
//                if (denominator == 0) {
//                    ratioValue = Double.NaN;
//                } else {
//                    ratioValue = numerator / denominator;
//                }
//                ratioValues.put(ratio.getId(), ratioValue);
//            }
//            ratios.put(name, ratioValues);
//        }
//    }
//    public void calculateRatioDirectlyFromQuantities(HashSet<String> quantitationNames){
//        for (String quantitationName : quantitationNames) {
//            HashMap<String, Double> ratioValues = new HashMap<String, Double>();
//            for (xRatio ratio : xTracker.study.getRatios()) {
//                if (ratio.getType().equals(xRatio.STUDY_VARIABLE)) continue;
//                double numerator = getQuantity(quantitationName, ratio.getNumerator());
//                double denominator = getQuantity(quantitationName, ratio.getDenominator());
//                double ratioValue;
//                if (denominator == 0) {
//                    ratioValue = Double.NaN;
//                } else {
//                    ratioValue = numerator / denominator;
//                }
//                ratioValues.put(ratio.getId(), ratioValue);
//            }
//            setRatios(quantitationName, ratioValues);
//        }
//    }
//    public void calculateRatioFromPeptideRatios(HashSet<String> quantitationNames){
//        if(this instanceof ProteinData){
//            ProteinData pro = (ProteinData)this;
//            ArrayList<PeptideData> peptides = pro.getAllPeptides();
//            ArrayList<String> ratioIDs = new ArrayList<String>();
//            for (xRatio ratio : xTracker.study.getRatios()) {
//                if (ratio.getType().equals(xRatio.ASSAY)) ratioIDs.add(ratio.getId());
//            }
//            for (String quantitationName : quantitationNames) {
//
//                ArrayList<HashMap<String, Double>> ratioValues = new ArrayList<HashMap<String, Double>>();
//                for(PeptideData peptide:peptides){
//                    ratioValues.add(peptide.getRatios(quantitationName));
//                }
//                HashMap<String,Double> proRatios = GenericInferenceMethod.median(ratioValues, ratioIDs);
//                setRatios(quantitationName, proRatios);
//            }
//        }
//    }
}

class QuantitationMisc {

    private Map<Class, String> methods = new HashMap<>();

    public QuantitationMisc() {
        methods.put(ProteinData.class, "getPeptides");
        methods.put(PeptideData.class, "getFeatures");
//        methods.put(ProteinData.class, "getAllPeptides");
//        methods.put(xPeptideConsensus.class, "getPeptides");
//        methods.put(PeptideData.class, "getAllFeatures");
//        methods.put(FeatureData.class, "getIdentifications");
    }

    String getMethodName(Class clazz) {
//        if(clazz == PeptideData.class && xTracker.study.getPipelineType() == Study.MS2_TYPE) return "getAllIdentifications";
        return methods.get(clazz);
    }

}
