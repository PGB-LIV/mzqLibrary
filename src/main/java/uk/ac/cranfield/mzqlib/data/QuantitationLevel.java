
package uk.ac.cranfield.mzqlib.data;

import java.util.HashMap;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.cranfield.mzqlib.GenericInferenceMethod;

/**
 *
 * @author Jun Fan@cranfield
 */
public class QuantitationLevel {

    /**
     * The first map: keys are quantitation name
     * The second map: keys are assay names and the values are the actual values
     * (quantities, study variables or ratios)
     */
    private HashMap<String, HashMap<String, Double>> quantities
            = new HashMap<>();
    private HashMap<String, HashMap<String, Double>> studyVariables
            = new HashMap<>();
    private HashMap<String, Double> ratios = new HashMap<>();
    private HashMap<String, Double> globals = new HashMap<>();
//    private HashSet<String> quantitationFlags = new HashSet<String>();
//    private HashSet<String> svFlags = new HashSet<String>();
    private boolean hasRatio = false;
    private boolean hasGlobal = false;

    static private QuantitationMisc misc = new QuantitationMisc();
    public static final int SUM = 0;
    public static final int MEAN = 1;
    public static final int MEDIAN = 2;
    public static final int WEIGHTED_AVERAGE = 3;

    public Double getQuantity(String name, String assayID) {
        if (quantities.containsKey(name) && quantities.get(name).containsKey(
                assayID)) {
            return quantities.get(name).get(assayID);
        }
        return null;
    }

    public HashMap<String, Double> getQuantities(String name) {
        return quantities.get(name);
    }

    public HashMap<String, Double> getStudyVariableQuantities(String name) {
        return studyVariables.get(name);
    }

    public Double getStudyVariableQuantity(String name, String sv) {
        if (studyVariables.containsKey(name) && studyVariables.get(name).
                containsKey(sv)) {
            return studyVariables.get(name).get(sv);
        }
        return null;
    }

    public Double getRatio(String name) {
        if (ratios.containsKey(name)) {
            return ratios.get(name);
        }
        return null;
    }

    public Double getGlobal(String name) {
        if (globals.containsKey(name)) {
            return globals.get(name);
        }
        return null;
    }

    public void setQuantities(String name, HashMap<String, Double> quantitions) {
        quantities.put(name, quantitions);
    }

    public void setStudyVariables(String name, HashMap<String, Double> svValues) {
        studyVariables.put(name, svValues);
    }

    public void setRatios(String name, Double ratio) {
        ratios.put(name, ratio);
        hasRatio = true;
    }

    public void setGlobal(String name, Double ratio) {
        globals.put(name, ratio);
        hasGlobal = true;
    }

    public boolean hasQuantitation(String quantitationName) {
        if (quantities.containsKey(quantitationName)) {
            return true;
        }
        return false;
    }

    public boolean hasSV(String quantitationName) {
        if (studyVariables.containsKey(quantitationName)) {
            return true;
        }
        return false;
    }

    public boolean hasRatio() {
        return hasRatio;
    }

    public boolean hasGlobal() {
        return hasGlobal;
    }

    public void calculateQuantitation(Set<String> quantitationNames,
                                      ArrayList<String> assayIDs, int type) {
        try {
            Method m = this.getClass().getDeclaredMethod(misc.getMethodName(
                    this.getClass()), new Class[0]);
            ArrayList<QuantitationLevel> results
                    = (ArrayList<QuantitationLevel>) m.invoke(this,
                                                              new Object[0]);
            ArrayList<Integer> count = new ArrayList<>();
            for (String name : quantitationNames) {
                ArrayList<HashMap<String, Double>> lowerLevelQuant
                        = new ArrayList<>();
                for (QuantitationLevel one : results) {
                    if (type == WEIGHTED_AVERAGE) {
                        count.add(one.getCount());
                    }
                    lowerLevelQuant.add(one.getQuantities(name));
                }
                HashMap<String, Double> quan = null;
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

    private HashMap<Class, String> methods = new HashMap<>();

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
