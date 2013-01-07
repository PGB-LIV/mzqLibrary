package uk.ac.cranfield.mzqlib.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.cranfield.mzqlib.data.FeatureData;
import uk.ac.cranfield.mzqlib.data.PeptideData;
import uk.ac.cranfield.mzqlib.data.ProteinData;

/**
 *
 * @author Jun Fan@cranfield
 */
public class HtmlConverter extends GenericConverter{
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

        StringBuilder sb = new StringBuilder();
        sb.append(templates.get("MAIN_HEADER"));
        for(ProteinData protein:MzqLib.data.getProteins()){
            sb.append(templates.get("PROTEIN_START"));
            sb.append(protein.getAccession());
            sb.append(templates.get("PROTEIN_END"));
            for(PeptideData peptide:protein.getPeptides()){
                sb.append(templates.get("PEPTIDE_START"));
                sb.append(peptide.getId());
                sb.append(templates.get("PEPTIDE_END"));
                for(FeatureData feature:peptide.getFeatures()){
                    sb.append(templates.get("FEATURE_START"));
                    sb.append(feature.getId());
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
}
