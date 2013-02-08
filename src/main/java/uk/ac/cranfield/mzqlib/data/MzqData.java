/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.AssayList;
import uk.ac.liv.jmzqml.model.mzqml.Column;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.EvidenceRef;
import uk.ac.liv.jmzqml.model.mzqml.Feature;
import uk.ac.liv.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.jmzqml.model.mzqml.GlobalQuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.InputFiles;
import uk.ac.liv.jmzqml.model.mzqml.Modification;
import uk.ac.liv.jmzqml.model.mzqml.ParamList;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
import uk.ac.liv.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.Ratio;
import uk.ac.liv.jmzqml.model.mzqml.RatioList;
import uk.ac.liv.jmzqml.model.mzqml.RatioQuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.Row;
import uk.ac.liv.jmzqml.model.mzqml.SoftwareList;
import uk.ac.liv.jmzqml.model.mzqml.StudyVariable;
import uk.ac.liv.jmzqml.model.mzqml.StudyVariableList;

/**
 *
 * @author Jun Fan@cranfield
 */
public class MzqData {
    public static final String ARTIFICIAL = "artificial";
    public static final String RATIO_STRING = "Ratio";
    /**
     * The list of mzq files loaded, not useful at all now as currently focused on one file only
     */
    private ArrayList<String> mzqFiles = new ArrayList<String>();
    /**
     * the map between protein id and the corresponding protein data
     */
    private HashMap<String, ProteinData> proteins = new HashMap<String, ProteinData>();
    /**
     * the list of protein ids
     */
    private ArrayList<String> proteinIds = new ArrayList<String>();
    /**
     * the list of assays
     */
    private ArrayList<String> assays = new ArrayList<String>();
    /**
     * the list of study variables
     */
    private ArrayList<String> svs = new ArrayList<String>();
    /**
     * the list of ratios
     */
    private ArrayList<String> ratios = new ArrayList<String>();
//    private HashMap<String, PeptideData> peptides = new HashMap<String, PeptideData>();
    //the map between the name of the quantitation value (e.g. intensity, peak area) and the corresponding CV terms 
    private HashMap<String,CvParam> cvParams = new HashMap<String, CvParam>();
    //the list of quantitation names
    private ArrayList<String> quantitationNames = new ArrayList<String>();
//    private HashMap<String,Character> modificationIndice = new HashMap<String, Character>(); 
//    private ArrayList<Modification> modifications = new ArrayList<Modification>();
    /**
     * the list of modification names. Ideally should be list of Modification objects, due to lack of equal function in the class, use this alternative
     */
    private ArrayList<String> modifications = new ArrayList<String>();
    /**
     * populate when reading the peptide consensus list to contain all peptide consensus elements
     * the entry will be removed when assigned to the protein which has 2 scenarios 
     * 1)read in the protein list while peptide_refs is available
     * 2)guess process according to the id strings, if not found, assign to the ARTIFICIAL protein
     * after loading one mzq file, this structure will be empty
     */
    private HashMap<String,PeptideData> peptides = new HashMap<String, PeptideData>();
    private HashSet<String> unsolvedPeptides = new HashSet<String>();
    
    private HashMap<String,FeatureData> unsolvedFeatures = new HashMap<String, FeatureData>();
    
    private SoftwareList softwareList;
    private ParamList analysisSummary;
    private InputFiles inputFiles;

    private boolean needAutoAssignment = true;
    public static final int ASSAY = 1;
    public static final int SV = 2;
    public static final int RATIO = 3;
    public static final int GLOBAL = 4;
    public static final int PROTEIN = 11;
    public static final int PEPTIDE = 12;
    public static final int FEATURE = 13;
    public MzqDataControl control = new MzqDataControl();
    
    public InputFiles getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(InputFiles inputFiles) {
        this.inputFiles = inputFiles;
    }

    public ParamList getAnalysisSummary() {
        return analysisSummary;
    }

    public void setAnalysisSummary(ParamList analysisSummary) {
        this.analysisSummary = analysisSummary;
    }

    public SoftwareList getSoftwareList() {
        return softwareList;
    }

    public void setSoftwareList(SoftwareList softwareList) {
        this.softwareList = softwareList;
    }
    
    public int addMzqFiles(String mzqFile){
        if(mzqFiles.contains(mzqFile)) return -1;
        mzqFiles.add(mzqFile);
        return mzqFiles.size()-1;
    }
    
    public void addFeatures(List<FeatureList> featureLists) {
        for(FeatureList featureList:featureLists){
            for(Feature feature:featureList.getFeature()){
                FeatureData featureData = new FeatureData(feature);
                unsolvedFeatures.put(feature.getId(), featureData);
            }
            for(QuantLayer ql:featureList.getMS2AssayQuantLayer()){
                parseQuantLayer(ql, ASSAY, FEATURE);
            }
            for(QuantLayer ql:featureList.getMS2StudyVariableQuantLayer()){
                parseQuantLayer(ql, SV, FEATURE);
            }
            if(featureList.getMS2RatioQuantLayer()!=null) parseRatioQuantLayer(featureList.getMS2RatioQuantLayer(), FEATURE);
            for(GlobalQuantLayer gql:featureList.getFeatureQuantLayer()){
                parseGlobalQuantLayer(gql, FEATURE);
            }
        }
    }

    public void addPeptides(PeptideConsensusList pcList) {
        for(PeptideConsensus pc:pcList.getPeptideConsensus()){
            PeptideData peptide = new PeptideData(pc);
            peptides.put(pc.getId(), peptide);
            unsolvedPeptides.add(pc.getId());
        }
        for(QuantLayer ql:pcList.getAssayQuantLayer()){
//            parseQuantLayer(ql,ASSAY,PROTEIN,localMapping);
            parseQuantLayer(ql,ASSAY,PEPTIDE);
        }
        for(QuantLayer ql:pcList.getStudyVariableQuantLayer()){
//            parseQuantLayer(ql,SV,PROTEIN,localMapping);
            parseQuantLayer(ql,SV,PEPTIDE);
        }
//        if(proteinList.getRatioQuantLayer()!=null) parseRatioQuantLayer(proteinList.getRatioQuantLayer(),PROTEIN,localMapping);
        if(pcList.getRatioQuantLayer()!=null) parseRatioQuantLayer(pcList.getRatioQuantLayer(),PEPTIDE);
        for(GlobalQuantLayer gql:pcList.getGlobalQuantLayer()){
            parseGlobalQuantLayer(gql, PEPTIDE);
        }
    }

    public void addProteins(ProteinList proteinList) {
        //the data structure localMapping holds the relationship between the ids from the current proteinList (keys) and the ids from the existing protein list in the memory (values)
        //will be useful if merging multiple mzq files
        //this will be used to assign quantitation values etc.
//        HashMap<String,String> localMapping = new HashMap<String, String>();
        //if the protein list is empty, create an artificial protein 
        if(proteinList == null){
            if(!proteins.containsKey(ARTIFICIAL)){
                Protein protein = new Protein();
                protein.setId(ARTIFICIAL);
                protein.setAccession(ARTIFICIAL);
                ProteinData proteinData = new ProteinData(protein);
                proteins.put(protein.getId(), proteinData);
                proteinIds.add(protein.getId());
            }
            return;
        }
        for(Protein qprotein:proteinList.getProtein()){
            //check whether this protein has been processed before (more likely from the previous mzq file)
            String id = existing(qprotein);
            if(id==null){//not matched to the existing proteins
                id = qprotein.getId();
                ProteinData newProtein = new ProteinData(qprotein);
                proteins.put(id, newProtein);
                proteinIds.add(id);
            }
//            localMapping.put(qprotein.getId(), id);
        }
        for(QuantLayer ql:proteinList.getAssayQuantLayer()){
//            parseQuantLayer(ql,ASSAY,PROTEIN,localMapping);
            parseQuantLayer(ql,ASSAY,PROTEIN);
        }
        for(QuantLayer ql:proteinList.getStudyVariableQuantLayer()){
//            parseQuantLayer(ql,SV,PROTEIN,localMapping);
            parseQuantLayer(ql,SV,PROTEIN);
        }
//        if(proteinList.getRatioQuantLayer()!=null) parseRatioQuantLayer(proteinList.getRatioQuantLayer(),PROTEIN,localMapping);
        if(proteinList.getRatioQuantLayer()!=null) parseRatioQuantLayer(proteinList.getRatioQuantLayer(),PROTEIN);
        for(GlobalQuantLayer gql:proteinList.getGlobalQuantLayer()){
            parseGlobalQuantLayer(gql, PROTEIN);
        }
    }
    
    private QuantitationLevel determineQuantObj(int level, Row row) {
//    private QuantitationLevel determineQuantObj(int level, Row row, HashMap<String, String> localMapping) {
        QuantitationLevel quantObj = null;
        switch(level){
            case PROTEIN:
                Protein protein = (Protein)row.getObjectRef();
//                quantObj = proteins.get(localMapping.get(protein.getId()));
                quantObj = proteins.get(protein.getId());
                break;
            case PEPTIDE:
                PeptideConsensus pc = (PeptideConsensus)row.getObjectRef();
                quantObj = peptides.get(pc.getId());
                break;
            case FEATURE:
                Feature feature = (Feature)row.getObjectRef();
                quantObj = unsolvedFeatures.get(feature.getId());
                break;
        }
        return quantObj;
    }

//    private void parseQuantLayer(QuantLayer ql, int type, int level, HashMap<String, String> localMapping){
    private void parseQuantLayer(QuantLayer ql, int type, int level){
        CvParam cvTerm = ql.getDataType().getCvParam();
        String quantityName = cvTerm.getName();
        
        if(!cvParams.containsKey(quantityName)){
            cvParams.put(quantityName, cvTerm);
            quantitationNames.add(quantityName);
        }
        ArrayList<String> assayIDs = new ArrayList<String>();
        for(Object obj:ql.getColumnIndex()){
            if(obj instanceof Assay){
                Assay assay = (Assay)obj;
                assayIDs.add(assay.getId());
                control.addElement(level, type, quantityName);
            }else if(obj instanceof StudyVariable){
                StudyVariable sv = (StudyVariable)obj;
                assayIDs.add(sv.getId());
                control.addElement(level, type, quantityName);
            }else{
                System.out.println("In the protein list, the quant layer "+ql.getId()+" has an unrecognised assay/sv id "+obj.toString());
                System.exit(1);
            }
        }
        for(Row row:ql.getDataMatrix().getRow()){
            HashMap<String,Double> quantities = new HashMap<String, Double>();
//            QuantitationLevel quantObj = determineQuantObj(level, row, localMapping);
            QuantitationLevel quantObj = determineQuantObj(level, row);
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

//    private void parseRatioQuantLayer(RatioQuantLayer rql, int level, HashMap<String,String> localMapping) {
    private void parseRatioQuantLayer(RatioQuantLayer rql, int level) {
        ArrayList<String> ratioIDs = new ArrayList<String>();
        for(Object obj:rql.getColumnIndex()){
            Ratio ratio = (Ratio)obj;
            ratioIDs.add(ratio.getId());
            control.addElement(level, RATIO, RATIO_STRING);
        }
        for(Row row:rql.getDataMatrix().getRow()){
//            QuantitationLevel quantObj = determineQuantObj(level, row, localMapping);
            QuantitationLevel quantObj = determineQuantObj(level, row);
            for (int i = 0; i < ratioIDs.size(); i++) {
                String ratioID = ratioIDs.get(i);
                double value = Double.parseDouble(row.getValue().get(i));
                quantObj.setRatios(ratioID, value);
            }
        }
    }

    private void parseGlobalQuantLayer(GlobalQuantLayer gql, int level) {
        ArrayList<String> columnIDs = new ArrayList<String>();
        for(Column column:gql.getColumnDefinition().getColumn()){
            String name = column.getDataType().getCvParam().getName();
            columnIDs.add(name);
            control.addElement(level, GLOBAL, name);
        }
        for(Row row:gql.getDataMatrix().getRow()){
            QuantitationLevel quantObj = determineQuantObj(level, row);
            for (int i = 0; i < columnIDs.size(); i++) {
                String columnID = columnIDs.get(i);
                double value = Double.parseDouble(row.getValue().get(i));
                quantObj.setGlobal(columnID, value);
            }
        }
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

    public int getModificationIndex(Modification mod) {
        String modStr = getModificationString(mod);
        if(modifications.contains(modStr)){
            return modifications.indexOf(modStr);
        }
        modifications.add(modStr);
        return modifications.size();
    }
    //for modification in PeptideConsensus, not modification in Label which is of ModParamType type
    private String getModificationString(Modification mod){
//        <xsd:element name="cvParam" type="CVParamType" minOccurs="1" maxOccurs="unbounded">
        for(CvParam cv:mod.getCvParam()){
            return cv.getName();
        }
        return null;
    }

    public ArrayList<ProteinData> getProteins() {
        ArrayList<ProteinData> values = new ArrayList<ProteinData>();
        for(String id:proteinIds){
            values.add(proteins.get(id));
        }
        return values;
    }

    public ArrayList<String> getQuantitationNames() {
        return quantitationNames;
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

    public ArrayList<PeptideData> getPeptides() {
        ArrayList<PeptideData> values = new ArrayList<PeptideData>();
        ArrayList<String> idList = new ArrayList<String>();
        for (String id : peptides.keySet()) {
            idList.add(id);
        }
        Collections.sort(idList);
        for (String id : idList) {
            values.add(peptides.get(id));
        }
        return values;
    }

    public ArrayList<FeatureData> getFeatures() {
        ArrayList<FeatureData> values = new ArrayList<FeatureData>();
        if(needAutoAssignment){
            
        }else{
            ArrayList<String> idList = new ArrayList<String>();
            for (String id : unsolvedFeatures.keySet()) {
                idList.add(id);
            }
            Collections.sort(idList);
            for (String id : idList) {
                values.add(unsolvedFeatures.get(id));
            }
        }
        return values;
    }

    public boolean needAutoAssignment() {
        return needAutoAssignment;
    }

    public void setNeedAutoAssignment(boolean needAutoAssignment) {
        this.needAutoAssignment = needAutoAssignment;
    }

    public void autoAssign() {
        if(!needAutoAssignment) return;
        for (PeptideData peptide: peptides.values()){
            for(EvidenceRef evidence:peptide.getPeptide().getEvidenceRef()){
                String id = ((Feature)evidence.getFeatureRef()).getId();
                FeatureData feature = unsolvedFeatures.get(id);
                unsolvedFeatures.remove(id);
                peptide.addFeature(feature);
            }
        }
        for(ProteinData protein:proteins.values()){
            for(Object obj:protein.getProtein().getPeptideConsensusRefs()){
                String id = ((PeptideConsensus)obj).getId();
                PeptideData peptide = peptides.get(id);
                unsolvedPeptides.remove(id);
                //TODO not sure whether one peptide related to multiple proteins case will cause bug here
                peptide.setAssignedByPeptideRef(true);
                protein.addPeptide(peptide);
            }
        }
        //un-linked peptides according to the peptideconsensus_refs in protein
        for(String peptideID: unsolvedPeptides){
            boolean guessed = false;
            for(ProteinData protein:proteins.values()){
                if(peptideID.contains(protein.getId())||peptideID.contains(protein.getAccession())){
                    guessed = true;
                    PeptideData peptide = peptides.get(peptideID);
                    protein.addPeptide(peptide);
                }
            }
            if(!guessed){
                if (!proteins.containsKey(ARTIFICIAL)) {
                    Protein protein = new Protein();
                    protein.setId(ARTIFICIAL);
                    protein.setAccession(ARTIFICIAL);
                    ProteinData proteinData = new ProteinData(protein);
                    proteins.put(protein.getId(), proteinData);
                    proteinIds.add(protein.getId());
                }
                ProteinData protein = proteins.get(ARTIFICIAL);
                PeptideData peptide = peptides.get(peptideID);
                protein.addPeptide(peptide);
            }
        }
    }
}
