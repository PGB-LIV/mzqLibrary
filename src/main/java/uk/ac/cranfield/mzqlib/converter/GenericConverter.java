package uk.ac.cranfield.mzqlib.converter;

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
public abstract class GenericConverter {
    private String filename;
    private String outfile;

    /**
     * Constructor.
     *
     * @param filename   input mzq file name.
     * @param outputFile output file name.
     */
    public GenericConverter(final String filename, final String outputFile) {
        this.filename = filename;
        this.outfile  = outputFile;
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
    protected final void addAssayHeader(final int level, final StringBuilder sb, final String quantitationName,
                                        final String prefix, final String suffix) {
        if (MzqLib.DATA.getControl().isRequired(level, MzqData.ASSAY, quantitationName)) {
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
     * Utility method to add assay values.
     *
     * @param level        level of data, such as peptide, protein, etc.
     * @param sb           StringBuilder.
     * @param obj          quantitation level.
     * @param seperator    file separator.
     * @param quantityName quantity name.
     */
    protected final void addAssayValue(final int level, final StringBuilder sb, final QuantitationLevel obj,
                                       final String seperator, final String quantityName) {
        if (MzqLib.DATA.getControl().isRequired(level, MzqData.ASSAY, quantityName)) {
            for (String assayID : MzqLib.DATA.getAssayIDs()) {
                sb.append(seperator);

                Double value = obj.getQuantity(quantityName, assayID);

                appendValue(sb, value);
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
    protected final void addGlobalHeader(final StringBuilder sb, final String prefix, final String suffix,
                                         final Set<String> names) {
        for (String columnID : names) {
            sb.append(prefix);
            sb.append(columnID);
            sb.append(suffix);
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
    protected final void addGlobalValue(final int level, final StringBuilder sb, final QuantitationLevel obj,
                                        final String seperator, final Set<String> names) {
        for (String columnID : names) {
            sb.append(seperator);

            Double value = obj.getGlobal(columnID);

            appendValue(sb, value);
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
    protected final void addRatioHeader(final int level, final StringBuilder sb, final String prefix,
                                        final String suffix) {
        if (MzqLib.DATA.getControl().isRequired(level, MzqData.RATIO, MzqData.RATIO_STRING)) {
            for (String ratioID : MzqLib.DATA.getRatios()) {
                sb.append(prefix);
                sb.append(ratioID);
                sb.append(suffix);
            }
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
    protected final void addRatioValue(final int level, final StringBuilder sb, final QuantitationLevel obj,
                                       final String seperator) {
        if (MzqLib.DATA.getControl().isRequired(level, MzqData.RATIO, MzqData.RATIO_STRING)) {
            for (String ratioID : MzqLib.DATA.getRatios()) {
                sb.append(seperator);

                Double value = obj.getRatio(ratioID);

                appendValue(sb, value);
            }
        }
    }

    /**
     * Utility method to add study variable header.
     *
     * @param level            level of data, such as peptide, protein, etc.
     * @param sb               StringBuilder.
     * @param quantitationName quantitation name.
     * @param prefix           prefix of the study variable.
     * @param suffix           suffix of the study variable.
     */
    protected final void addSvHeader(final int level, final StringBuilder sb, final String quantitationName,
                                     final String prefix, final String suffix) {
        if (MzqLib.DATA.getControl().isRequired(level, MzqData.SV, quantitationName)) {
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
     * Utility method to add study variable values.
     *
     * @param level        level of data, such as peptide, protein, etc.
     * @param sb           StringBuilder.
     * @param obj          quantitation level.
     * @param seperator    file separator.
     * @param quantityName quantity name.
     */
    protected final void addSvValue(final int level, final StringBuilder sb, final QuantitationLevel obj,
                                    final String seperator, final String quantityName) {
        if (MzqLib.DATA.getControl().isRequired(level, MzqData.SV, quantityName)) {
            for (StudyVariable sv : MzqLib.DATA.getSvs()) {
                sb.append(seperator);

                Double value = obj.getStudyVariableQuantity(quantityName, sv.getId());

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
    private void appendValue(final StringBuilder sb, final Double value) {
        if (value == null) {
            sb.append("null");
        } else {
            sb.append(value.toString());
        }
    }

    /**
     * Abstract convert method.
     */
    public abstract void convert();

    final String getBaseFilename() {
        int idx = filename.lastIndexOf('.');

        return filename.substring(0, idx);
    }

    /**
     * @return the filename
     */
    public final String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public final void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the outfile
     */
    public final String getOutfile() {
        return outfile;
    }

    /**
     * @param outfile the outfile to set
     */
    public final void setOutfile(String outfile) {
        this.outfile = outfile;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
