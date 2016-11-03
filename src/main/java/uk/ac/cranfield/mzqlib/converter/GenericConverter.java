
package uk.ac.cranfield.mzqlib.converter;

import java.util.HashSet;
import java.util.Set;

import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.cranfield.mzqlib.data.MzqData;
import uk.ac.cranfield.mzqlib.data.QuantitationLevel;
import uk.ac.liv.pgb.jmzqml.model.mzqml.StudyVariable;

/**
 * Generic converter to be extended into different format converters.
 *
 * @author Jun Fan@cranfield
 */
abstract public class GenericConverter {

    String filename;
    String outfile;

    /**
     * Constructor.
     *
     * @param filename   input mzq file name.
     * @param outputFile output file name.
     */
    public GenericConverter(String filename, String outputFile) {
        this.filename = filename;
        this.outfile = outputFile;
    }

    String getBaseFilename() {
        int idx = filename.lastIndexOf('.');
        return filename.substring(0, idx);
    }

    /**
     * Abstract convert method.
     */
    abstract public void convert();

    /**
     * Utility method to add study variable header.
     *
     * @param level            level of data, such as peptide, protein, etc.
     * @param sb               StringBuilder.
     * @param quantitationName quantitation name.
     * @param prefix           prefix of the study variable.
     * @param suffix           suffix of the study variable.
     */
    protected void addSvHeader(int level, StringBuilder sb,
                               String quantitationName, String prefix,
                               String suffix) {
        if (MzqLib.DATA.control.isRequired(level, MzqData.SV, quantitationName)) {
            for (StudyVariable sv : MzqLib.DATA.getSvs()) {
                sb.append(prefix);
                sb.append(quantitationName);
                sb.append("_");
                sb.append(sv.getId());
                sb.append(suffix);
            }
        }
    }

    /**
     * Utility method to add assay header.
     *
     * @param level            level of data, such as peptide, protein, etc.
     * @param sb               StringBuilder.
     * @param quantitationName quantitation name.
     * @param prefix           prefix of the assay.
     * @param suffix           suffix of the assay.
     */
    protected void addAssayHeader(int level, StringBuilder sb,
                                  String quantitationName, String prefix,
                                  String suffix) {
        if (MzqLib.DATA.control.isRequired(level, MzqData.ASSAY,
                                           quantitationName)) {
            for (String assayID : MzqLib.DATA.getAssayIDs()) {
                sb.append(prefix);
                sb.append(quantitationName);
                sb.append("_");
                sb.append(assayID);
                sb.append(suffix);
            }
        }
    }

    /**
     * Utility method to add ratio header
     *
     * @param level  level of data, such as peptide, protein, etc.
     * @param sb     StringBuilder.
     * @param prefix prefix of the ratio.
     * @param suffix suffix of the ratio.
     */
    protected void addRatioHeader(int level, StringBuilder sb, String prefix,
                                  String suffix) {
        if (MzqLib.DATA.control.isRequired(level, MzqData.RATIO,
                                           MzqData.RATIO_STRING)) {
            for (String ratioID : MzqLib.DATA.getRatios()) {
                sb.append(prefix);
                sb.append(ratioID);
                sb.append(suffix);
            }
        }
    }

    /**
     * Utility method to add global quantitation header.
     *
     * @param sb     String builder.
     * @param prefix prefix of global quantitation.
     * @param suffix suffix of global quantitation.
     * @param names  set of names.
     */
    protected void addGlobalHeader(StringBuilder sb, String prefix,
                                   String suffix, Set<String> names) {
        for (String columnID : names) {
            sb.append(prefix);
            sb.append(columnID);
            sb.append(suffix);
        }
    }

    /**
     * Utility method to add study variable values.
     *
     * @param level        level of data, such as peptide, protein, etc.
     * @param sb           StringBuilder.
     * @param obj          quantitation level.
     * @param seperator    file separator.
     * @param quantityName quantity name.
     */
    protected void addSvValue(int level, StringBuilder sb, QuantitationLevel obj,
                              String seperator, String quantityName) {
        if (MzqLib.DATA.control.isRequired(level, MzqData.SV, quantityName)) {
            for (StudyVariable sv : MzqLib.DATA.getSvs()) {
                sb.append(seperator);
                Double value = obj.getStudyVariableQuantity(quantityName, sv.
                                                            getId());
                appendValue(sb, value);
            }
        }
    }

    /**
     * Utility method to add assay values.
     *
     * @param level        level of data, such as peptide, protein, etc.
     * @param sb           StringBuilder.
     * @param obj          quantitation level.
     * @param seperator    file separator.
     * @param quantityName quantity name.
     */
    protected void addAssayValue(int level, StringBuilder sb,
                                 QuantitationLevel obj, String seperator,
                                 String quantityName) {
        if (MzqLib.DATA.control.isRequired(level, MzqData.ASSAY, quantityName)) {
            for (String assayID : MzqLib.DATA.getAssayIDs()) {
                sb.append(seperator);
                Double value = obj.getQuantity(quantityName, assayID);
                appendValue(sb, value);
            }
        }
    }

    /**
     * Append values.
     *
     * @param sb    StringBilder.
     * @param value the double value to be appended.
     */
    private void appendValue(StringBuilder sb, Double value) {
        if (value == null) {
            sb.append("null");
        } else {
            sb.append(value.toString());
        }
    }

    /**
     * Utility method to add ratio value.
     *
     * @param level     level of data, such as peptide, protein, etc.
     * @param sb        StringBuilder.
     * @param obj       quantitation level.
     * @param seperator file separator.
     */
    protected void addRatioValue(int level, StringBuilder sb,
                                 QuantitationLevel obj, String seperator) {
        if (MzqLib.DATA.control.isRequired(level, MzqData.RATIO,
                                           MzqData.RATIO_STRING)) {
            for (String ratioID : MzqLib.DATA.getRatios()) {
                sb.append(seperator);
                Double value = obj.getRatio(ratioID);
                appendValue(sb, value);
            }
        }
    }

    /**
     * Utility to add global quantitation values.
     *
     * @param level     level of data, such as peptide, protein, etc.
     * @param sb        StringBuilder.
     * @param obj       quantitation level.
     * @param seperator file separator.
     * @param names     set of names.
     */
    protected void addGlobalValue(int level, StringBuilder sb,
                                  QuantitationLevel obj, String seperator,
                                  Set<String> names) {
        for (String columnID : names) {
            sb.append(seperator);
            Double value = obj.getGlobal(columnID);
            appendValue(sb, value);
        }
    }

}
