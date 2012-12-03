/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.plaf.basic.BasicBorders.RadioButtonBorder;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.AssayList;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
import uk.ac.liv.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.Ratio;
import uk.ac.liv.jmzqml.model.mzqml.RatioList;
import uk.ac.liv.jmzqml.model.mzqml.RatioQuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.Row;
import uk.ac.liv.jmzqml.model.mzqml.StudyVariable;
import uk.ac.liv.jmzqml.model.mzqml.StudyVariableList;

/**
 *
 * @author Jun Fan@cranfield
 */
public class MzqData {
    public static final String ARTIFICIAL = "artificial";
    private HashMap<String, ProteinData> proteins = new HashMap<String, ProteinData>();
    private ArrayList<String> ids = new ArrayList<String>();
    private ArrayList<String> assays = new ArrayList<String>();
    private ArrayList<String> svs = new ArrayList<String>();
    private ArrayList<String> ratios = new ArrayList<String>();
    private HashMap<String,CvParam> cvParams = new HashMap<String, CvParam>();
    private ArrayList<String> cvNames = new ArrayList<String>();
    private final int ASSAY = 1;
    private final int SV = 2;
    private final int PROTEIN = 11;
    private final int PEPTIDE = 12;
    private final int FEATURE = 13;
    
    public void addProteins(ProteinList proteinList) {
        //the data structure holds the relationship between the ids from the current proteinList (keys) and the ids from the existing protein list in the memory (values)
        //will be useful if merging multiple mzq files
        //this will be used to assign quantitation values etc.
        HashMap<String,String> localMapping = new HashMap<String, String>();
        //if the protein list is empty, create an artificial protein 
        if(proteinList == null){
            if(!proteins.containsKey(ARTIFICIAL)){
                Protein protein = new Protein();
                protein.setId(ARTIFICIAL);
                protein.setAccession(ARTIFICIAL);
                ProteinData proteinData = new ProteinData(protein);
                proteins.put(protein.getId(), proteinData);
                ids.add(protein.getId());
            }
            return;
        }
        for(Protein qprotein:proteinList.getProtein()){
            String id = existing(qprotein);
            if(id==null){//not matched to the existing proteins
                id = qprotein.getId();
                ProteinData newProtein = new ProteinData(qprotein);
                proteins.put(id, newProtein);
                ids.add(id);
            }
            localMapping.put(qprotein.getId(), id);
        }
        for(QuantLayer ql:proteinList.getAssayQuantLayer()){
            parseQuantLayer(ql,ASSAY,PROTEIN,localMapping);
        }
        for(QuantLayer ql:proteinList.getStudyVariableQuantLayer()){
            parseQuantLayer(ql,SV,PROTEIN,localMapping);
        }
        if(proteinList.getRatioQuantLayer()!=null) parseRatioQuantLayer(proteinList.getRatioQuantLayer(),PROTEIN,localMapping);
    }

    private void parseQuantLayer(QuantLayer ql, int type, int level, HashMap<String, String> localMapping){
        CvParam cvTerm = ql.getDataType().getCvParam();
        String quantityName = cvTerm.getName();
        
        if(!cvParams.containsKey(quantityName)){
            cvParams.put(quantityName, cvTerm);
            cvNames.add(quantityName);
        }
        ArrayList<String> assayIDs = new ArrayList<String>();
        for(Object obj:ql.getColumnIndex()){
            if(obj instanceof Assay){
                assayIDs.add(((Assay)obj).getId());
            }else if(obj instanceof StudyVariable){
                assayIDs.add(((StudyVariable)obj).getId());
            }else{
                System.out.println("In the protein list, the quant layer "+ql.getId()+" has an unrecognised assay/sv id "+obj.toString());
                System.exit(1);
            }
        }
        for(Row row:ql.getDataMatrix().getRow()){
            HashMap<String,Double> quantities = new HashMap<String, Double>();
            QuantitationLevel quantObj = determineQuantObj(level, row, localMapping);
            for (int i = 0; i < assayIDs.size(); i++) {
                String assayID = assayIDs.get(i);
                double value = Double.parseDouble(row.getValue().get(i));
                quantities.put(assayID, value);
            }
            if(type == ASSAY){
                quantObj.setQuantities(quantityName, quantities);
            }else{
                quantObj.setStudyVariables(quantityName, quantities);
            }
        }
    }

    private QuantitationLevel determineQuantObj(int level, Row row, HashMap<String, String> localMapping) {
        QuantitationLevel quantObj = null;
        switch(level){
            case PROTEIN:
                Protein protein = (Protein)row.getObjectRef();
                quantObj = proteins.get(localMapping.get(protein.getId()));
                break;
            case PEPTIDE:
                break;
            case FEATURE:
                break;
        }
        return quantObj;
    }

    private void parseRatioQuantLayer(RatioQuantLayer rql, int level, HashMap<String,String> localMapping) {
        ArrayList<String> ratioIDs = new ArrayList<String>();
        for(Object obj:rql.getColumnIndex()){
            Ratio ratio = (Ratio)obj;
            ratioIDs.add(ratio.getId());
        }
        for(Row row:rql.getDataMatrix().getRow()){
            QuantitationLevel quantObj = determineQuantObj(level, row, localMapping);
            for (int i = 0; i < ratioIDs.size(); i++) {
                String ratioID = ratioIDs.get(i);
                double value = Double.parseDouble(row.getValue().get(i));
                quantObj.setRatios(ratioID, value);
            }
        }
    }

    public void addPeptides(PeptideConsensusList pcList) {
    }

    public void addAssays(AssayList assayList) {
        for(Assay assay:assayList.getAssay()){
            assays.add(assay.getId());
        }
    }

    public void addStudyVariables(StudyVariableList studyVariableList) {
        if(studyVariableList == null) return;
        for(StudyVariable sv:studyVariableList.getStudyVariable()){
            svs.add(sv.getId());
        }
    }

    public void addRatios(RatioList ratioList) {
        if(ratioList == null) return;
        for(Ratio ratio:ratioList.getRatio()){
            ratios.add(ratio.getId());
        }
    }

    public ArrayList<ProteinData> getProteins() {
        ArrayList<ProteinData> values = new ArrayList<ProteinData>();
        for(String id:ids){
            values.add(proteins.get(id));
        }
        return values;
    }

    public ArrayList<String> getCvNames() {
        return cvNames;
    }

    public ArrayList<String> getAssays() {
        return assays;
    }

    public ArrayList<String> getSvs() {
        return svs;
    }

    public ArrayList<String> getRatios() {
        return ratios;
    }
    
    private String existing(Protein protein){
        //first search by id
        if(proteins.containsKey(protein.getId())) return protein.getId();
        //then by accession number
        for(ProteinData proteinData:proteins.values()){
            if(protein.getAccession().equalsIgnoreCase(proteinData.getAccession())) return proteinData.getProtein().getId();
        }
        //still not found
        return null;
    }
}
