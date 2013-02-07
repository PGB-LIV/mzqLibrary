/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.liv.jmzqml.model.MzQuantMLObject;

/**
 *
 * @author Jun Fan@cranfield
 */
public class MzqDataControl {
    
    private HashMap<Integer,MzqDataControlElement> proteinLevel = new HashMap<Integer, MzqDataControlElement>();
    private HashMap<Integer,MzqDataControlElement> peptideLevel = new HashMap<Integer, MzqDataControlElement>();
    private HashMap<Integer,MzqDataControlElement> featureLevel = new HashMap<Integer, MzqDataControlElement>();
    
    public void addElement(int level, int type, MzQuantMLObject element){
        MzqDataControlElement controlElement = getControlElement(level, type);
        controlElement.addElement(element);
    }
    
    public boolean isRequired(int level, int type){
        return getControlElement(level, type).isRequired();
    }

    //have not fully decided yet, the difficulty comes from the multiple quantity properties
//    public ArrayList<MzQuantMLObject> getElements(int level, int type){
//        return getControlElement(level,type).getElements();
//    }
            
    private MzqDataControlElement getControlElement(int level, int type){
        HashMap<Integer,MzqDataControlElement> map = null;
        switch(level){
            case MzqData.PROTEIN:
                map = proteinLevel;
                break;
            case MzqData.PEPTIDE:
                map = peptideLevel;
                break;
            case MzqData.FEATURE:
                map = featureLevel;
                break;
        }
        if(map == null) {
            System.out.println("Unrecognized quantitation level, program exits in MzqDataControl.java");
            System.exit(0);
        }
        
        if(!map.containsKey(type)){
             MzqDataControlElement controlElement = new MzqDataControlElement();
             map.put(type, controlElement);
        }
        return map.get(type);
    }
}

class MzqDataControlElement{
    private ArrayList<MzQuantMLObject> elements = new ArrayList<MzQuantMLObject>();
    boolean isRequired(){
        if (elements.isEmpty()) return false;
        return true;
    }
    
    ArrayList<MzQuantMLObject> getElements(){
        return elements;
    }
    
    void addElement(MzQuantMLObject element){
        elements.add(element);
    }
}
