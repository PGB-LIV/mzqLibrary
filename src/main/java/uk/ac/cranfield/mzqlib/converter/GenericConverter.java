package uk.ac.cranfield.mzqlib.converter;

import uk.ac.cranfield.mzqlib.MzqLib;
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
    
    protected void addSvHeader(StringBuilder sb, String quantitationName, String prefix, String suffix) {
        for (String sv : MzqLib.data.getSvs()) {
            sb.append(prefix);
            sb.append(quantitationName);
            sb.append("_");
            sb.append(sv);
            sb.append(suffix);
        }
    }

    protected void addAssayHeader(StringBuilder sb, String quantitationName, String prefix, String suffix) {
        for (String assayID : MzqLib.data.getAssays()) {
            sb.append(prefix);
            sb.append(quantitationName);
            sb.append("_");
            sb.append(assayID);
            sb.append(suffix);
        }
    }
    
    protected void addRatioHeader(StringBuilder sb, String prefix, String suffix) {
        for (String ratioID : MzqLib.data.getRatios()) {
            sb.append(prefix);
            sb.append(ratioID);
            sb.append(suffix);
        }
    }
    
    protected void addSvValue(StringBuilder sb, QuantitationLevel obj, String seperator, String quantityName){
        for (String sv : MzqLib.data.getSvs()) {
            sb.append(seperator);
            sb.append(obj.getStudyVariableQuantity(quantityName, sv));
        }
    }

    protected void addAssayValue(StringBuilder sb, QuantitationLevel obj, String seperator, String quantityName){
        for (String assayID : MzqLib.data.getAssays()) {
            sb.append(seperator);
            sb.append(obj.getQuantity(quantityName, assayID));
        }
    }

    protected void addRatioValue(StringBuilder sb, QuantitationLevel obj, String seperator){
        for (String ratioID : MzqLib.data.getRatios()) {
            sb.append(seperator);
            sb.append(obj.getRatio(ratioID));
        }
    }
}
