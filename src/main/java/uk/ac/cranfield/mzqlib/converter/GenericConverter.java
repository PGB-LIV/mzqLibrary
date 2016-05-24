
package uk.ac.cranfield.mzqlib.converter;

import java.util.HashSet;
import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.cranfield.mzqlib.data.MzqData;
import uk.ac.cranfield.mzqlib.data.QuantitationLevel;
import uk.ac.liv.pgb.jmzqml.model.mzqml.StudyVariable;

/**
 *
 * @author Jun Fan@cranfield
 */
abstract public class GenericConverter {

    String filename;
    String outfile;

    public GenericConverter(String filename, String outputFile) {
        this.filename = filename;
        this.outfile = outputFile;
    }

    String getBaseFilename() {
        int idx = filename.lastIndexOf('.');
        return filename.substring(0, idx);
    }

    abstract public void convert();

    protected void addSvHeader(int level, StringBuilder sb,
                               String quantitationName, String prefix,
                               String suffix) {
        if (MzqLib.data.control.isRequired(level, MzqData.SV, quantitationName)) {
            for (StudyVariable sv : MzqLib.data.getSvs()) {
                sb.append(prefix);
                sb.append(quantitationName);
                sb.append("_");
                sb.append(sv.getId());
                sb.append(suffix);
            }
        }
    }

    protected void addAssayHeader(int level, StringBuilder sb,
                                  String quantitationName, String prefix,
                                  String suffix) {
        if (MzqLib.data.control.isRequired(level, MzqData.ASSAY,
                                           quantitationName)) {
            for (String assayID : MzqLib.data.getAssayIDs()) {
                sb.append(prefix);
                sb.append(quantitationName);
                sb.append("_");
                sb.append(assayID);
                sb.append(suffix);
            }
        }
    }

    protected void addRatioHeader(int level, StringBuilder sb, String prefix,
                                  String suffix) {
        if (MzqLib.data.control.isRequired(level, MzqData.RATIO,
                                           MzqData.RATIO_STRING)) {
            for (String ratioID : MzqLib.data.getRatios()) {
                sb.append(prefix);
                sb.append(ratioID);
                sb.append(suffix);
            }
        }
    }

    protected void addGlobalHeader(StringBuilder sb, String prefix,
                                   String suffix, HashSet<String> names) {
        for (String columnID : names) {
            sb.append(prefix);
            sb.append(columnID);
            sb.append(suffix);
        }
    }

//    private void appendValue(StringBuilder sb,Double value){
//        
//    }
    protected void addSvValue(int level, StringBuilder sb, QuantitationLevel obj,
                              String seperator, String quantityName) {
        if (MzqLib.data.control.isRequired(level, MzqData.SV, quantityName)) {
            for (StudyVariable sv : MzqLib.data.getSvs()) {
                sb.append(seperator);
                Double value = obj.getStudyVariableQuantity(quantityName, sv.
                                                            getId());
                appendValue(sb, value);
            }
        }
    }

    protected void addAssayValue(int level, StringBuilder sb,
                                 QuantitationLevel obj, String seperator,
                                 String quantityName) {
        if (MzqLib.data.control.isRequired(level, MzqData.ASSAY, quantityName)) {
            for (String assayID : MzqLib.data.getAssayIDs()) {
                sb.append(seperator);
                Double value = obj.getQuantity(quantityName, assayID);
                appendValue(sb, value);
            }
        }
    }

    private void appendValue(StringBuilder sb, Double value) {
        if (value == null) {
            sb.append("null");
        } else {
            sb.append(value.toString());
        }
    }

    protected void addRatioValue(int level, StringBuilder sb,
                                 QuantitationLevel obj, String seperator) {
        if (MzqLib.data.control.isRequired(level, MzqData.RATIO,
                                           MzqData.RATIO_STRING)) {
            for (String ratioID : MzqLib.data.getRatios()) {
                sb.append(seperator);
                Double value = obj.getRatio(ratioID);
                appendValue(sb, value);
            }
        }
    }

    protected void addGlobalValue(int level, StringBuilder sb,
                                  QuantitationLevel obj, String seperator,
                                  HashSet<String> names) {
        for (String columnID : names) {
            sb.append(seperator);
            Double value = obj.getGlobal(columnID);
            appendValue(sb, value);
        }
    }

}
