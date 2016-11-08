
package uk.ac.cranfield.mzqlib.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.cranfield.mzqlib.data.FeatureData;
import uk.ac.cranfield.mzqlib.data.MzqData;
import uk.ac.cranfield.mzqlib.data.PeptideData;
import uk.ac.cranfield.mzqlib.data.ProteinData;
import uk.ac.cranfield.mzqlib.data.ProteinGroupData;
import uk.ac.cranfield.mzqlib.data.QuantitationLevel;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Assay;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Software;
import uk.ac.liv.pgb.jmzqml.model.mzqml.StudyVariable;

/**
 * XlsConverter is to convert mzq file to xls file.
 *
 * @author Jun Fan
 */
public class XlsConverter extends GenericConverter {

//    final static String SEPERATOR = "";
    private final WritableCellFormat boldFormat;
    private final WritableCellFormat normalFormat;
    private final int FONT11 = 11;
    private final int FONT10 = 10;
    private final int PEPTIDE_SHEET = 3;
    private final int FEATURE_SHEET = 4;

    /**
     * Constructor.
     *
     * @param filename   input mzq file name.
     * @param outputFile output xls file name.
     */
    public XlsConverter(final String filename, final String outputFile) {
        super(filename, outputFile);
        WritableFont boldFont = new WritableFont(WritableFont.ARIAL, FONT11,
                                                 WritableFont.BOLD);
        boldFormat = new WritableCellFormat(boldFont);
        WritableFont normalFont = new WritableFont(WritableFont.ARIAL, FONT10);
        normalFormat = new WritableCellFormat(normalFont);
    }

    /**
     * Convert method. Convert mzq file to xls file.
     * Override the method in GenericConverter.
     */
    @Override
    public void convert() {
        if (outfile.length() == 0) {
            outfile = getBaseFilename() + ".xls";
        }

        try {
            WritableWorkbook wb = Workbook.createWorkbook(new File(outfile));
            wb.createSheet("metadata", 0);
            wb.createSheet("protein groups", 1);
            wb.createSheet("proteins", 2);
            wb.createSheet("peptides", PEPTIDE_SHEET);
            wb.createSheet("features", FEATURE_SHEET);

            WritableSheet metaSheet = wb.getSheet("metadata");
            outputMetadata(metaSheet);

            WritableSheet pgSheet = wb.getSheet("protein groups");
            ArrayList<QuantitationLevel> pgs = new ArrayList<>();
            for (ProteinGroupData pg : MzqLib.DATA.getProteinGroups()) {
                pgs.add(pg);
            }
            if (!pgs.isEmpty()) {
                int index = 0;
                index = outputAssayAndSV(MzqData.PROTEIN_GROUP, pgSheet, pgs);
                if (MzqLib.DATA.control.isRequired(MzqData.PROTEIN_GROUP,
                                                   MzqData.RATIO,
                                                   MzqData.RATIO_STRING)) {
                    index = outputRatio(MzqData.PROTEIN_GROUP, pgSheet, pgs,
                                        index);
                }
                if (!MzqLib.DATA.control.getElements(MzqData.PROTEIN_GROUP,
                                                     MzqData.GLOBAL).isEmpty()) {
                    outputGlobal(MzqData.PROTEIN_GROUP, pgSheet, pgs, index);
                }
            }
            WritableSheet proteinSheet = wb.getSheet("proteins");
            ArrayList<QuantitationLevel> proteins = new ArrayList<>();
            for (ProteinData protein : MzqLib.DATA.getProteins()) {
                proteins.add(protein);
            }
            //not just the protein artificially created
            if (proteins.size() > 1 || !MzqLib.DATA.getProteins().get(0).
                    getAccession().equals(MzqData.ARTIFICIAL)) {
                int index = 0;
                index
                        = outputAssayAndSV(MzqData.PROTEIN, proteinSheet,
                                           proteins);
                if (MzqLib.DATA.control.isRequired(MzqData.PROTEIN,
                                                   MzqData.RATIO,
                                                   MzqData.RATIO_STRING)) {
                    index = outputRatio(MzqData.PROTEIN, proteinSheet, proteins,
                                        index);
                }
                if (!MzqLib.DATA.control.getElements(MzqData.PROTEIN,
                                                     MzqData.GLOBAL).isEmpty()) {
                    outputGlobal(MzqData.PROTEIN, proteinSheet, proteins, index);
                }
            }

            WritableSheet peptideSheet = wb.getSheet("peptides");
            ArrayList<QuantitationLevel> peptides = new ArrayList<>();
            for (PeptideData peptide : MzqLib.DATA.getPeptides()) {
                peptides.add(peptide);
            }
            if (!peptides.isEmpty()) {
                int index = 0;
                index
                        = outputAssayAndSV(MzqData.PEPTIDE, peptideSheet,
                                           peptides);
                if (MzqLib.DATA.control.isRequired(MzqData.PEPTIDE,
                                                   MzqData.RATIO,
                                                   MzqData.RATIO_STRING)) {
                    outputRatio(MzqData.PEPTIDE, peptideSheet, peptides, index);
                }
                if (!MzqLib.DATA.control.getElements(MzqData.PEPTIDE,
                                                     MzqData.GLOBAL).isEmpty()) {
                    outputGlobal(MzqData.PEPTIDE, peptideSheet, peptides, index);
                }
            }

            WritableSheet featureSheet = wb.getSheet("features");
            ArrayList<QuantitationLevel> features = new ArrayList<>();
            for (FeatureData feature : MzqLib.DATA.getFeatures()) {
                features.add(feature);
            }
            if (!features.isEmpty()) {
                int index = 0;
                index
                        = outputAssayAndSV(MzqData.FEATURE, featureSheet,
                                           features);
                if (MzqLib.DATA.control.isRequired(MzqData.FEATURE,
                                                   MzqData.RATIO,
                                                   MzqData.RATIO_STRING)) {
                    outputRatio(MzqData.FEATURE, featureSheet, features, index);
                }
                if (!MzqLib.DATA.control.getElements(MzqData.FEATURE,
                                                     MzqData.GLOBAL).isEmpty()) {
                    outputGlobal(MzqData.FEATURE, featureSheet, features, index);
                }
            }

            wb.write();
            wb.close();
        } catch (WriteException | IOException ex) {
            Logger.getLogger(XlsConverter.class.getName()).log(Level.SEVERE,
                                                               null, ex);
        }
    }

    private int outputAssayAndSV(final int level, final WritableSheet sheet,
                                 final List<QuantitationLevel> objects)
            throws NumberFormatException, WriteException {
        //reference to CsvConverter
        //when sb.append("\n"), it should be rowCount++ and colCount=1 here
        //when sb.append(SEPERATOR), it should be colCount++ here except every new line colCount should be reset
        int rowCount = 0;
        int colCount = 1;
        for (String quantityName : MzqLib.DATA.getQuantitationNames()) {
            if (MzqLib.DATA.control.isRequired(level, MzqData.ASSAY,
                                               quantityName)
                    || MzqLib.DATA.control.isRequired(level, MzqData.SV,
                                                      quantityName)) {
                sheet.addCell(new Label(0, rowCount, quantityName, boldFormat));
                if (MzqLib.DATA.control.isRequired(level, MzqData.ASSAY,
                                                   quantityName)) {
                    for (String assayID : MzqLib.DATA.getAssayIDs()) {
                        sheet.addCell(new Label(colCount, rowCount, assayID,
                                                boldFormat));
                        colCount++;
                    }
                }
                if (MzqLib.DATA.control.isRequired(level, MzqData.SV,
                                                   quantityName)) {
                    for (StudyVariable sv : MzqLib.DATA.getSvs()) {
                        sheet.addCell(new Label(colCount, rowCount, sv.getId(),
                                                boldFormat));
                        colCount++;
                    }
                }
                rowCount++;
                colCount = 1;
            }
            for (QuantitationLevel obj : objects) {
                if (obj.hasQuantitation(quantityName) || obj.hasSV(quantityName)) {
                    printQuantitationLevel(level, sheet, rowCount, obj);
                    if (MzqLib.DATA.control.isRequired(level, MzqData.ASSAY,
                                                       quantityName)) {
                        for (String assayID : MzqLib.DATA.getAssayIDs()) {
                            Double value = obj.
                                    getQuantity(quantityName, assayID);
                            printValue(value, sheet, colCount, rowCount);
                            colCount++;
                        }
                    }
                    if (MzqLib.DATA.control.isRequired(level, MzqData.SV,
                                                       quantityName)) {
                        for (StudyVariable sv : MzqLib.DATA.getSvs()) {
                            Double value = obj.getStudyVariableQuantity(
                                    quantityName, sv.getId());
                            printValue(value, sheet, colCount, rowCount);
                            colCount++;
                        }
                    }
                    rowCount++;
                    colCount = 1;
                }
            }
            rowCount++;
        }
        return rowCount;
    }

    private void printValue(final Double value, final WritableSheet sheet,
                            final int colCount,
                            final int rowCount)
            throws WriteException, NumberFormatException {
        if (value == null) {
            sheet.addCell(new Label(colCount, rowCount, "null", normalFormat));
        } else {
            sheet.addCell(new Number(colCount, rowCount, value, normalFormat));
        }
    }

    private void printQuantitationLevel(final int level,
                                        final WritableSheet sheet,
                                        final int rowCount,
                                        final QuantitationLevel obj)
            throws WriteException {
        switch (level) {
            case MzqData.PROTEIN_GROUP:
                if (obj instanceof ProteinGroupData) {
                    sheet.addCell(new Label(0, rowCount,
                                            ((ProteinGroupData) obj).getId(),
                                            normalFormat));
                }
                break;
            case MzqData.PROTEIN:
                if (obj instanceof ProteinData) {
                    sheet.addCell(new Label(0, rowCount, ((ProteinData) obj).
                                            getId(), normalFormat));
                }
                break;
            case MzqData.PEPTIDE:
                if (obj instanceof PeptideData) {
                    sheet.addCell(new Label(0, rowCount, ((PeptideData) obj).
                                            getId(), normalFormat));
                }
                break;
            case MzqData.FEATURE:
                if (obj instanceof FeatureData) {
                    sheet.addCell(new Label(0, rowCount, ((FeatureData) obj).
                                            getId(), normalFormat));
                }
                break;
            default:
                break;
        }
    }

    private int outputRatio(final int level, final WritableSheet sheet,
                            final List<QuantitationLevel> objects, int rowCount)
            throws NumberFormatException, WriteException {
        sheet.addCell(new Label(0, rowCount, "Ratios", boldFormat));
        int colCount = 1;
        for (String ratioID : MzqLib.DATA.getRatios()) {
            sheet.addCell(new Label(colCount, rowCount, ratioID, boldFormat));
            colCount++;
        }
        rowCount++;
        colCount = 1;

        for (QuantitationLevel object : objects) {
            if (object.hasRatio()) {
                printQuantitationLevel(level, sheet, rowCount, object);
                for (String ratioID : MzqLib.DATA.getRatios()) {
                    Double value = object.getRatio(ratioID);
                    printValue(value, sheet, colCount, rowCount);
                    colCount++;
                }
                rowCount++;
                colCount = 1;
            }
        }
        return rowCount;
    }

    private void outputGlobal(final int level, final WritableSheet sheet,
                              final List<QuantitationLevel> objects,
                              int rowCount)
            throws NumberFormatException, WriteException {
        sheet.addCell(new Label(0, rowCount, "Global", boldFormat));
        int colCount = 1;
        for (String columnID : MzqLib.DATA.control.getElements(level,
                                                               MzqData.GLOBAL)) {
            sheet.addCell(new Label(colCount, rowCount, columnID, boldFormat));
            colCount++;
        }
        rowCount++;
        colCount = 1;
        for (QuantitationLevel object : objects) {
            if (object.hasGlobal()) {
                printQuantitationLevel(level, sheet, rowCount, object);
                for (String columnID : MzqLib.DATA.control.getElements(level,
                                                                       MzqData.GLOBAL)) {
                    Double value = object.getGlobal(columnID);
                    printValue(value, sheet, colCount, rowCount);
                    colCount++;
                }
                rowCount++;
                colCount = 1;
            }
        }
    }

    private void outputMetadata(final WritableSheet metaSheet)
            throws NumberFormatException, WriteException {
        int rowCount = 0;
        //analysis summary
        metaSheet.
                addCell(new Label(0, rowCount, "Analysis Summary", boldFormat));
        rowCount++;
        for (CvParam cv : MzqLib.DATA.getAnalysisSummary().getCvParam()) {
            metaSheet.addCell(new Label(0, rowCount, cv.getName(), boldFormat));
            if (cv.getValue() != null && cv.getValue().length() > 1 && !cv.
                    getValue().equalsIgnoreCase("null")) {
                metaSheet.addCell(new Label(1, rowCount, cv.getValue(),
                                            normalFormat));
            }
            rowCount++;
        }
        rowCount++;
        //software
        metaSheet.addCell(new Label(0, rowCount, "Software List", boldFormat));
        rowCount++;
        for (Software software : MzqLib.DATA.getSoftwareList().getSoftware()) {
            StringBuilder sb = new StringBuilder();
            sb.append(software.getId());
            sb.append(" (Version: ");
            sb.append(software.getVersion());
            sb.append(")");
            metaSheet.addCell(
                    new Label(0, rowCount, sb.toString(), normalFormat));
            rowCount++;
        }
        rowCount++;
        //Assays
        metaSheet.addCell(new Label(0, rowCount, "Assays", boldFormat));
        rowCount++;
        for (Assay assay : MzqLib.DATA.getAssays()) {
            metaSheet.addCell(new Label(0, rowCount, assay.getId(), boldFormat));
            metaSheet.addCell(new Label(1, rowCount, assay.getName(),
                                        normalFormat));
            rowCount++;
        }
        rowCount++;
    }

}
