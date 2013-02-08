package uk.ac.cranfield.mzqlib.converter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.cranfield.mzqlib.data.FeatureData;
import uk.ac.cranfield.mzqlib.data.MzqData;
import uk.ac.cranfield.mzqlib.data.PeptideData;
import uk.ac.cranfield.mzqlib.data.ProteinData;

/**
 *
 * @author Jun Fan@cranfield
 */
public class CsvConverter extends GenericConverter {
    final static String SEPERATOR = ",";

    public CsvConverter(String filename) {
        super(filename);
    }

    @Override
    public void convert() {
        String outfile = getBaseFilename() + ".csv";
        StringBuilder sb = new StringBuilder();
        //deal with proteins
        ArrayList<ProteinData> proteins = MzqLib.data.getProteins();
        //not just the protein artificially created
        if (proteins.size() > 1 || !proteins.get(0).getAccession().equals(MzqData.ARTIFICIAL)) {
            sb.append("Proteins\n");
            for (String quantityName : MzqLib.data.getQuantitationNames()) {
                addHeaderLine(MzqData.PROTEIN, sb, quantityName);
                for (ProteinData protein : proteins) {
                    if(protein.hasQuantitation(quantityName)||protein.hasSV(quantityName)){
                        sb.append(protein.getId());
                        addAssayValue(MzqData.PROTEIN, sb, protein, SEPERATOR, quantityName);
                        addSvValue(MzqData.PROTEIN, sb, protein, SEPERATOR, quantityName);
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
//            ArrayList<String> ratios = MzqLib.data.getRatios();
//            if (!ratios.isEmpty()) {
            if (MzqLib.data.control.isRequired(MzqData.PROTEIN, MzqData.RATIO, MzqData.RATIO_STRING)) {
                sb.append("Ratios");
                addRatioHeader(MzqData.PROTEIN, sb, SEPERATOR, "");
                sb.append("\n");
                for (ProteinData protein : proteins) {
                    if(protein.hasRatio()){
                        sb.append(protein.getId());
                        addRatioValue(MzqData.PROTEIN, sb, protein, SEPERATOR);
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
            HashSet<String> globals = MzqLib.data.control.getElements(MzqData.PROTEIN, MzqData.GLOBAL);
            if(globals.size()>0){
                sb.append("Global");
                addGlobalHeader(sb, SEPERATOR, "", globals);
                sb.append("\n");
                for (ProteinData protein : proteins) {
                    if(protein.hasGlobal()){
                        sb.append(protein.getId());
                        addGlobalValue(MzqData.PROTEIN, sb, protein, SEPERATOR, globals);
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
        }
        sb.append("\n");
        //deal with peptide
        ArrayList<PeptideData> peptides = MzqLib.data.getPeptides();
        if (peptides.size() > 1 ) {
            sb.append("Peptides\n");
            for (String quantityName : MzqLib.data.getQuantitationNames()) {
                addHeaderLine(MzqData.PEPTIDE, sb, quantityName);
                for (PeptideData peptide : peptides) {
                    if(peptide.hasQuantitation(quantityName)||peptide.hasSV(quantityName)){
                        sb.append(peptide.getId());
                        addAssayValue(MzqData.PEPTIDE, sb, peptide, SEPERATOR, quantityName);
                        addSvValue(MzqData.PEPTIDE, sb, peptide, SEPERATOR, quantityName);
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
//            ArrayList<String> ratios = MzqLib.data.getRatios();
//            if (!ratios.isEmpty()) {
            if (MzqLib.data.control.isRequired(MzqData.PEPTIDE, MzqData.RATIO, MzqData.RATIO_STRING)) {
                sb.append("Ratios");
                addRatioHeader(MzqData.PEPTIDE, sb, SEPERATOR, "");
                sb.append("\n");
                for (PeptideData peptide : peptides) {
                    if(peptide.hasRatio()){
                        sb.append(peptide.getId());
                        addRatioValue(MzqData.PEPTIDE, sb, peptide, SEPERATOR);
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
            HashSet<String> globals = MzqLib.data.control.getElements(MzqData.PEPTIDE, MzqData.GLOBAL);
            if(globals.size()>0){
                sb.append("Global");
                addGlobalHeader(sb, SEPERATOR, "", globals);
                sb.append("\n");
                for (PeptideData peptide : peptides) {
                    if(peptide.hasGlobal()){
                        sb.append(peptide.getId());
                        addGlobalValue(MzqData.PEPTIDE, sb, peptide, SEPERATOR, globals);
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
        }
        //deal with features
        ArrayList<FeatureData> features = MzqLib.data.getFeatures();
        if (features.size() > 1 ) {
            sb.append("Features\n");
            for (String quantityName : MzqLib.data.getQuantitationNames()) {
                addHeaderLine(MzqData.FEATURE, sb, quantityName);
                for (FeatureData feature : features) {
                    if(feature.hasQuantitation(quantityName)||feature.hasSV(quantityName)){
                        sb.append(feature.getId());
                        addAssayValue(MzqData.FEATURE, sb, feature, SEPERATOR, quantityName);
                        addSvValue(MzqData.FEATURE, sb, feature, SEPERATOR, quantityName);
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
//            ArrayList<String> ratios = MzqLib.data.getRatios();
//            if (!ratios.isEmpty()) {
            if (MzqLib.data.control.isRequired(MzqData.FEATURE, MzqData.RATIO, MzqData.RATIO_STRING)) {
                sb.append("Ratios");
                addRatioHeader(MzqData.FEATURE, sb, SEPERATOR, "");
                sb.append("\n");
                for (FeatureData feature : features) {
                    if(feature.hasRatio()){
                        sb.append(feature.getId());
                        addRatioValue(MzqData.FEATURE, sb, feature, SEPERATOR);
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
            HashSet<String> globals = MzqLib.data.control.getElements(MzqData.FEATURE, MzqData.GLOBAL);
            if(globals.size()>0){
                sb.append("Global");
                addGlobalHeader(sb, SEPERATOR, "", globals);
                sb.append("\n");
                for (FeatureData feature : features) {
                    if(feature.hasGlobal()){
                        sb.append(feature.getId());
                        addGlobalValue(MzqData.FEATURE, sb, feature, SEPERATOR, globals);
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
        }
//        System.out.println(sb.toString());
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
            out.append(sb.toString());
            out.close();
        } catch (IOException e) {
            System.out.println("Problems while closing file " + outfile + "!\n" + e);
        }
    }

    private void addHeaderLine(int level, StringBuilder sb, String quantityName) {
        if (MzqLib.data.control.isRequired(level, MzqData.ASSAY, quantityName)||MzqLib.data.control.isRequired(level, MzqData.SV, quantityName)) {
            sb.append("Entity");
            addAssayHeader(level, sb, quantityName, SEPERATOR, "");
            addSvHeader(level, sb, quantityName, SEPERATOR, "");
            sb.append("\n");
        }
    }
}
