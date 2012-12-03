package uk.ac.cranfield.mzqlib;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jun Fan@cranfield
 */
public class GenericInferenceMethod {

    /**
     * assign the middle data structure by the quantitation values from the lower level
     */
    private static HashMap<String, ArrayList<Double>> assignMiddleStructure(ArrayList<String> assayIDs,ArrayList<HashMap<String, Double>> lowLevelQuant) {
        HashMap<String, ArrayList<Double>> tmp = initializeMiddleStructure(assayIDs);
        for (HashMap<String, Double> quant : lowLevelQuant) {
            for (String assayID : assayIDs) {
                if (quant.containsKey(assayID)) {
                    tmp.get(assayID).add(quant.get(assayID));
//                }else{
//                    tmp.get(assayID).add(0d);
                }
            }
        }
        return tmp;
    }
    /**
     * initialize the middle data structure, keys are assay IDs and values are the list of related values
     */
    private static HashMap<String, ArrayList<Double>> initializeMiddleStructure(ArrayList<String> assayIDs) {
        HashMap<String,ArrayList<Double>> tmp = new HashMap<String, ArrayList<Double>>();
        for(String assayID:assayIDs){
            ArrayList<Double> value = new ArrayList<Double>();
            tmp.put(assayID, value);
        }
        return tmp;
    }
    
    /**
     * initialize the ret value
     */
    private static HashMap<String,Double> initializeRet(ArrayList<String> names){
        HashMap<String,Double> ret = new HashMap<String, Double>();
        for(String name:names){
            ret.put(name, 0d);
        }
        return ret;
    }
    /**
     * The sum method
     * @param lowLevelQuant
     * @param assayIDs
     * @return 
     */
    public static HashMap<String,Double> sum(ArrayList<HashMap<String, Double>> lowLevelQuant, ArrayList<String> assayIDs) {
        HashMap<String,Double> ret = initializeRet(assayIDs);
        HashMap<String, ArrayList<Double>> tmp = assignMiddleStructure(assayIDs, lowLevelQuant);
        for(String assayID:assayIDs){
            ArrayList<Double> list = tmp.get(assayID);
            ret.put(assayID, Utils.sum(list));
        }
        return ret;
    }
    /**
     * The median method
     * @param lowLevelQuant
     * @param assayIDs
     * @return 
     */
    public static HashMap<String,Double> median(ArrayList<HashMap<String, Double>> lowLevelQuant, ArrayList<String> assayIDs){
        HashMap<String,Double> ret = initializeRet(assayIDs);
        HashMap<String, ArrayList<Double>> tmp = assignMiddleStructure(assayIDs, lowLevelQuant);
        //assayID, quantitation
        for(String assayID:assayIDs){
            ArrayList<Double> list = tmp.get(assayID);
            ret.put(assayID, Utils.median(list));
        }
        return ret;
    }
    /**
     * The mean method
     * @param lowLevelQuant
     * @param assayIDs
     * @return 
     */
    public static HashMap<String,Double> mean(ArrayList<HashMap<String, Double>> lowLevelQuant, ArrayList<String> assayIDs){
        HashMap<String,Double> ret = initializeRet(assayIDs);
        HashMap<String, ArrayList<Double>> tmp = assignMiddleStructure(assayIDs, lowLevelQuant);
        for(String assayID:assayIDs){
            ArrayList<Double> list = tmp.get(assayID);
            ret.put(assayID, Utils.sum(list)/list.size());
        }
        return ret;
    }
    /**
     * Calculate the weighted mean
     * @param lowLevelQuant
     * @param assayIDs
     * @param count  
     * @return 
     */
    public static HashMap<String,Double> weightedAverage(ArrayList<HashMap<String, Double>> lowLevelQuant, ArrayList<String> assayIDs, ArrayList<Integer> count){
        HashMap<String,Double> ret = initializeRet(assayIDs);
        HashMap<String, ArrayList<Double>> tmp = initializeMiddleStructure(assayIDs);
        for (int i = 0; i < lowLevelQuant.size(); i++) {
            HashMap<String, Double> quant = lowLevelQuant.get(i);
            for(String assayID:assayIDs){
                if(quant.containsKey(assayID)){
                    tmp.get(assayID).add(quant.get(assayID)*count.get(i));
                }
            }
        }
        
        int totalCount = 0;
        for(int abc:count){
            totalCount += abc;
        }
        
        for(String assayID:assayIDs){
            ArrayList<Double> list = tmp.get(assayID);
            int len = list.size();
            if(len == 0) continue;
            double sum = 0;
            for (Double value:list) {
                sum += value;
            }
            ret.put(assayID, sum/totalCount);
        }
        return ret;
    }
}
