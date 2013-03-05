package uk.ac.cranfield.mzqlib.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.cranfield.mzqlib.data.FeatureData;
import uk.ac.cranfield.mzqlib.data.MzqData;
import uk.ac.cranfield.mzqlib.data.PeptideData;
import uk.ac.cranfield.mzqlib.data.ProteinData;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.Software;

/**
 *
 * @author Jun Fan@cranfield
 */
public class HtmlConverter extends GenericConverter{
    private static final String SEPERATOR = "</td><td>";
    final private String TEMPLATE = "htmlTemplate.txt";
    static private HashMap<String,String> templates = new HashMap<String, String>();
    
    public HtmlConverter(String filename){
        super(filename);
    }

    @Override
    public void convert() {
        String outfile = getBaseFilename() + ".html";
        if(templates.isEmpty()){
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(HtmlConverter.class.getClassLoader().getResourceAsStream(TEMPLATE)));
                String line = "";
                String tag = "";
                StringBuilder sb = new StringBuilder();
                while((line=reader.readLine())!=null){
                    if(line.startsWith("#")){//tag line
                        if(tag.length()>0) {
                            sb.deleteCharAt(sb.length()-1);
                            sb.deleteCharAt(sb.length()-1);
                            templates.put(tag, sb.toString());
                        }
                        sb = new StringBuilder();
                        tag = line.substring(1);
                    }else{
                        sb.append(line);
                        sb.append("\n");
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.deleteCharAt(sb.length() - 1);
                templates.put(tag, sb.toString());
            }catch(IOException ioe){
                System.out.println("Can not find the template file");
                System.exit(0);
            }
        }

//        int assaySize = MzqLib.data.getAssays().size()*MzqLib.data.getQuantitationNames().size();
//        int svSize = MzqLib.data.getSvs().size()*MzqLib.data.getQuantitationNames().size();
        
        StringBuilder sb = new StringBuilder();
        sb.append(templates.get("MAIN_HEADER"));
        //add metadata
        //analysis summary
        sb.append("<h3>Analysis Summary</h3>\n<ul>\n");
        for (CvParam cv:MzqLib.data.getAnalysisSummary().getCvParam()){
            sb.append("<li><b>");
            sb.append(cv.getName());
            if(cv.getValue()!=null && cv.getValue().length()>1 && (!cv.getValue().equalsIgnoreCase("null"))){
                sb.append(":</b>");
                sb.append(cv.getValue());
            }else{
                sb.append("</b>");
            }
            sb.append("</li>\n");
        }
        sb.append("</ul>\n"); 
        
        //software
        sb.append("<h3>Software List</h3>\n<ul>\n");
        for (Software software:MzqLib.data.getSoftwareList().getSoftware()){
            sb.append("<li>");
            sb.append(software.getId());
            sb.append(" (Version: ");
            sb.append(software.getVersion());
            sb.append(")</li>\n");
        }
        sb.append("</ul>\n"); 
        //start the protein table
        sb.append(templates.get("PROTEIN_HEADER_START"));
        addHeader(MzqData.PROTEIN, sb);
        sb.append(templates.get("PROTEIN_HEADER_END"));
        
        HashSet<String> proteinGlobalNames = MzqLib.data.control.getElements(MzqData.PROTEIN, MzqData.GLOBAL);
        HashSet<String> peptideGlobalNames = MzqLib.data.control.getElements(MzqData.PEPTIDE, MzqData.GLOBAL);
        HashSet<String> featureGlobalNames = MzqLib.data.control.getElements(MzqData.FEATURE, MzqData.GLOBAL);
        int proteinAssaySize = MzqLib.data.control.getElements(MzqData.PROTEIN, MzqData.ASSAY).size() * MzqLib.data.getAssays().size();
        int proteinSvSize = MzqLib.data.control.getElements(MzqData.PROTEIN, MzqData.SV).size() * MzqLib.data.getSvs().size();
        int proteinRatioSize = 0;
        if (MzqLib.data.control.isRequired(MzqData.PROTEIN, MzqData.RATIO, MzqData.RATIO_STRING)){
            proteinRatioSize = MzqLib.data.getRatios().size();
        }
        //start from the first quantitation/sv column
        final int proteinTotalSize = proteinAssaySize + proteinSvSize + proteinRatioSize + proteinGlobalNames.size()+1; //+1 for the protein accession column
        int peptideAssaySize = MzqLib.data.control.getElements(MzqData.PEPTIDE, MzqData.ASSAY).size() * MzqLib.data.getAssays().size();
        int peptideSvSize = MzqLib.data.control.getElements(MzqData.PEPTIDE, MzqData.SV).size() * MzqLib.data.getSvs().size();
        int peptideRatioSize = 0;
        if (MzqLib.data.control.isRequired(MzqData.PEPTIDE, MzqData.RATIO, MzqData.RATIO_STRING)){
            peptideRatioSize = MzqLib.data.getRatios().size();
        }
        //start from modification column
        final int peptideTotalSize = peptideAssaySize + peptideSvSize + peptideRatioSize + peptideGlobalNames.size() + 2; //+2 for the peptide sequence and modification columns
        
        for(ProteinData protein:MzqLib.data.getProteins()){
            //it seems html does not like . which is fine in NCName
            String proteinID = protein.getAccession().replace(".", "_");
            sb.append(templates.get("PROTEIN_START_1"));
            sb.append(proteinID);
            sb.append(templates.get("PROTEIN_START_2"));
            sb.append(protein.getAccession());
            for (String quantityName : MzqLib.data.getQuantitationNames()) {
                addAssayValue(MzqData.PROTEIN, sb, protein, SEPERATOR, quantityName);
                addSvValue(MzqData.PROTEIN, sb, protein, SEPERATOR, quantityName);
            }
            addRatioValue(MzqData.PROTEIN, sb, protein, SEPERATOR);
            addGlobalValue(MzqData.PROTEIN, sb, protein, SEPERATOR, proteinGlobalNames);
            sb.append(templates.get("PROTEIN_END_1"));
            sb.append(proteinID);
            sb.append("-detail");
            sb.append(templates.get("PROTEIN_END_2"));
            sb.append("child-of-");
            sb.append(proteinID);
            sb.append(templates.get("PROTEIN_END_3"));
            sb.append(proteinTotalSize);
            sb.append(templates.get("PROTEIN_END_4"));
            addHeader(MzqData.PEPTIDE, sb);
            sb.append(templates.get("PROTEIN_END_5"));
            for(PeptideData peptide:protein.getPeptides()){
                String peptideID = proteinID +"-"+peptide.getSeq()+"-"+peptide.getPeptide().getModification().size();
                sb.append(templates.get("PEPTIDE_START_1"));
                sb.append(peptideID);
                sb.append(templates.get("PEPTIDE_START_2"));
                sb.append(peptide.getSeq());
                sb.append(SEPERATOR);
                sb.append(peptide.getPeptide().getModification().size());
                for (String quantityName : MzqLib.data.getQuantitationNames()) {
                    addAssayValue(MzqData.PEPTIDE, sb, peptide, SEPERATOR, quantityName);
                    addSvValue(MzqData.PEPTIDE, sb, peptide, SEPERATOR, quantityName);
                }
                addRatioValue(MzqData.PEPTIDE, sb, peptide, SEPERATOR);
                addGlobalValue(MzqData.PEPTIDE, sb, peptide, SEPERATOR, peptideGlobalNames);
                sb.append(templates.get("PEPTIDE_END_1"));
                sb.append(peptideID);
                sb.append("-detail");
                sb.append(templates.get("PEPTIDE_END_2"));
                sb.append("child-of-");
                sb.append(peptideID);
                sb.append(templates.get("PEPTIDE_END_3"));
                sb.append(peptideTotalSize);
                sb.append(templates.get("PEPTIDE_END_4"));
                addHeader(MzqData.FEATURE, sb);
                sb.append(templates.get("PEPTIDE_END_5"));
                for(FeatureData feature:peptide.getFeatures()){
                    sb.append(templates.get("FEATURE_START"));
                    sb.append(feature.getFeature().getCharge());
                    sb.append(SEPERATOR);
                    sb.append(feature.getFeature().getMz());
                    for(String quantityName : MzqLib.data.getQuantitationNames()) {
                        addAssayValue(MzqData.FEATURE, sb, feature, SEPERATOR, quantityName);
                        addSvValue(MzqData.FEATURE, sb, feature, SEPERATOR, quantityName);
                    }
                    addRatioValue(MzqData.FEATURE, sb, feature, SEPERATOR);
                    addGlobalValue(MzqData.FEATURE, sb, feature, SEPERATOR, featureGlobalNames);
                    sb.append(templates.get("FEATURE_END"));
                }
                sb.append(templates.get("PEPTIDE_FINISH"));
            }
            sb.append(templates.get("PROTEIN_FINISH"));
        }
        sb.append(templates.get("MAIN_FOOTER"));

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
            out.append(sb.toString());
            out.close();
        } catch (IOException e) {
            System.out.println("Problems while closing file " + outfile + "!\n" + e);
        }
        
    }

    private void addHeader(int level, StringBuilder sb) {
        for (String quantityName : MzqLib.data.getQuantitationNames()) {
            addAssayHeader(level, sb, quantityName, "<th>", "</th>");
            addSvHeader(level, sb, quantityName, "<th>", "</th>");
        }
        addRatioHeader(level, sb, "<th>", "</th>");
        HashSet<String> names = MzqLib.data.control.getElements(level, MzqData.GLOBAL);
        if (names.size()>0){
            addGlobalHeader(sb,"<th>", "</th>", names);
            sb.append("\n");
        }
    }
}
