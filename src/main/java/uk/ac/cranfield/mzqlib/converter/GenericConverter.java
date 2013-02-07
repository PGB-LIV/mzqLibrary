package uk.ac.cranfield.mzqlib.converter;

import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.cranfield.mzqlib.data.MzqData;
import uk.ac.cranfield.mzqlib.data.QuantitationLevel;

/**
 *
 * @author Jun Fan@cranfield
 */
abstract public class GenericConverter {
    String filename;
    public GenericConverter(String filename){
        this.filename = filename;
    }
    
    String getBaseFilename(){
        int idx = filename.lastIndexOf(".");
        return filename.substring(0,idx);
    }
    
    abstract public void convert();
    
    protected void addSvHeader(int level, StringBuilder sb, String quantitationName, String prefix, String suffix) {
        if(MzqLib.data.control.isRequired(level, MzqData.SV)){
            for (String sv : MzqLib.data.getSvs()) {
                sb.append(prefix);
                sb.append(quantitationName);
                sb.append("_");
                sb.append(sv);
                sb.append(suffix);
            }
        }
    }

    protected void addAssayHeader(int level, StringBuilder sb, String quantitationName, String prefix, String suffix) {
        if(MzqLib.data.control.isRequired(level, MzqData.ASSAY)){
            for (String assayID : MzqLib.data.getAssays()) {
                sb.append(prefix);
                sb.append(quantitationName);
                sb.append("_");
                sb.append(assayID);
                sb.append(suffix);
            }
        }
    }
    
    protected void addRatioHeader(int level, StringBuilder sb, String prefix, String suffix) {
        if(MzqLib.data.control.isRequired(level, MzqData.RATIO)){
            for (String ratioID : MzqLib.data.getRatios()) {
                sb.append(prefix);
                sb.append(ratioID);
                sb.append(suffix);
            }
        }
    }
    
    protected void addSvValue(int level, StringBuilder sb, QuantitationLevel obj, String seperator, String quantityName){
        if(MzqLib.data.control.isRequired(level, MzqData.SV)){
            for (String sv : MzqLib.data.getSvs()) {
                sb.append(seperator);
                sb.append(obj.getStudyVariableQuantity(quantityName, sv));
            }
        }
    }

    protected void addAssayValue(int level, StringBuilder sb, QuantitationLevel obj, String seperator, String quantityName){
        if(MzqLib.data.control.isRequired(level, MzqData.ASSAY)){
            for (String assayID : MzqLib.data.getAssays()) {
                sb.append(seperator);
                sb.append(obj.getQuantity(quantityName, assayID));
            }
        }
    }

    protected void addRatioValue(int level, StringBuilder sb, QuantitationLevel obj, String seperator){
        if(MzqLib.data.control.isRequired(level, MzqData.RATIO)){
            for (String ratioID : MzqLib.data.getRatios()) {
                sb.append(seperator);
                sb.append(obj.getRatio(ratioID));
            }
        }
    }
}
