package uk.ac.cranfield.mzqlib.converter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
                addHeaderLine(sb, quantityName);
                for (ProteinData protein : proteins) {
                    if(protein.hasQuantitation(quantityName)||protein.hasSV(quantityName)){
                        sb.append(protein.getId());
                        for (String assayID : MzqLib.data.getAssays()) {
                            sb.append(",");
                            sb.append(protein.getQuantity(quantityName, assayID));
                        }
                        for (String sv : MzqLib.data.getSvs()) {
                            sb.append(",");
                            sb.append(protein.getStudyVariableQuantity(quantityName, sv));
                        }
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
            ArrayList<String> ratios = MzqLib.data.getRatios();
            if (!ratios.isEmpty()) {
                sb.append("Ratios");
                for (String ratioID : ratios) {
                    sb.append(",");
                    sb.append(ratioID);
                }
                sb.append("\n");
                for (ProteinData protein : proteins) {
                    if(protein.hasRatio()){
                        sb.append(protein.getId());
                        for (String ratioID : ratios) {
                            sb.append(",");
                            sb.append(protein.getRatio(ratioID));
                        }
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
                addHeaderLine(sb, quantityName);
                for (PeptideData peptide : peptides) {
                    if(peptide.hasQuantitation(quantityName)||peptide.hasSV(quantityName)){
                        sb.append(peptide.getId());
                        for (String assayID : MzqLib.data.getAssays()) {
                            sb.append(",");
                            sb.append(peptide.getQuantity(quantityName, assayID));
                        }
                        for (String sv : MzqLib.data.getSvs()) {
                            sb.append(",");
                            sb.append(peptide.getStudyVariableQuantity(quantityName, sv));
                        }
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
            ArrayList<String> ratios = MzqLib.data.getRatios();
            if (!ratios.isEmpty()) {
                sb.append("Ratios");
                for (String ratioID : ratios) {
                    sb.append(",");
                    sb.append(ratioID);
                }
                sb.append("\n");
                for (PeptideData peptide : peptides) {
                    if(peptide.hasRatio()){
                        sb.append(peptide.getId());
                        for (String ratioID : ratios) {
                            sb.append(",");
                            sb.append(peptide.getRatio(ratioID));
                        }
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
                addHeaderLine(sb, quantityName);
                for (FeatureData feature : features) {
                    if(feature.hasQuantitation(quantityName)||feature.hasSV(quantityName)){
                        sb.append(feature.getId());
                        for (String assayID : MzqLib.data.getAssays()) {
                            sb.append(",");
                            sb.append(feature.getQuantity(quantityName, assayID));
                        }
                        for (String sv : MzqLib.data.getSvs()) {
                            sb.append(",");
                            sb.append(feature.getStudyVariableQuantity(quantityName, sv));
                        }
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
            ArrayList<String> ratios = MzqLib.data.getRatios();
            if (!ratios.isEmpty()) {
                sb.append("Ratios");
                for (String ratioID : ratios) {
                    sb.append(",");
                    sb.append(ratioID);
                }
                sb.append("\n");
                for (FeatureData feature : features) {
                    if(feature.hasRatio()){
                        sb.append(feature.getId());
                        for (String ratioID : ratios) {
                            sb.append(",");
                            sb.append(feature.getRatio(ratioID));
                        }
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

    private void addHeaderLine(StringBuilder sb, String quantityName) {
        sb.append(quantityName);
        for (String assayID : MzqLib.data.getAssays()) {
            sb.append(",");
            sb.append(assayID);
        }
        for (String sv : MzqLib.data.getSvs()) {
            sb.append(",");
            sb.append(sv);
        }
        sb.append("\n");
    }
}
