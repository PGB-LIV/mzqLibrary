
package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.liv.pgb.jmzqml.model.mzqml.Assay;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AssayList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Column;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Cv;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.pgb.jmzqml.model.mzqml.EvidenceRef;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Feature;
import uk.ac.liv.pgb.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.GlobalQuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.InputFiles;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Modification;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Protein;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Ratio;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RatioList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RatioQuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Row;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SoftwareList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.StudyVariable;
import uk.ac.liv.pgb.jmzqml.model.mzqml.StudyVariableList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AnalysisSummary;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroup;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroupList;

/**
 * MzqData contains all the data and metadata from mzq file.
 *
 * @author Jun Fan@qmul
 */
public class MzqData {

    /**
     * Constant ARITIFICAL.
     */
    public static final String ARTIFICIAL = "artificial";

    /**
     * Constant RATIO_STRING.
     */
    public static final String RATIO_STRING = "Ratio";

    /**
     * the map between protein group id and the corresponding protein group data
     */
    private final Map<String, ProteinGroupData> pgs = new HashMap<>();
    /**
     * the list of protein group ids
     */
    private final List<String> pgIds = new ArrayList<>();
    /**
     * the map between protein id and the corresponding protein data
     */
    private final HashMap<String, ProteinData> proteins = new HashMap<>();
    /**
     * the list of protein ids
     */
    private final List<String> proteinIds = new ArrayList<>();
    /**
     * the list of assayIDs
     */
    private final List<String> assayIDs = new ArrayList<>();
    /**
     * the list of assayIDs
     */
    private final List<Assay> assays = new ArrayList<>();
    /**
     * the list of study variables
     */
    private final List<StudyVariable> svs = new ArrayList<>();
    /**
     * the list of ratios
     */
    private final List<String> ratios = new ArrayList<>();

    //the map between the name of the quantitation value (e.g. intensity, peak area) and the corresponding CV terms 
    private final Map<String, CvParam> cvParams = new HashMap<>();
    //the list of quantitation names
    private final List<String> quantitationNames = new ArrayList<>();

    /**
     * the list of modification names. Ideally should be list of Modification
     * objects, due to lack of equal function in the class, use this alternative
     */
    private final List<String> modifications = new ArrayList<>();
    /**
     * populate when reading the peptide consensus list to contain all peptide
     * consensus elements the entry will be removed when assigned to the protein
     * which has 2 scenarios 1)read in the protein list while peptide_refs is
     * available 2)guess process according to the id strings, if not found,
     * assign to the ARTIFICIAL protein after loading one mzq file, this
     * structure will be empty
     */
    private final Map<String, PeptideData> peptides = new HashMap<>();
    private final Set<String> unsolvedPeptides = new HashSet<>();
    private final Map<String, FeatureData> unsolvedFeatures = new HashMap<>();
    private SoftwareList softwareList;
    private AnalysisSummary analysisSummary;
    private InputFiles inputFiles;
    private String mzqID;
    private String mzqName;
    private List<Cv> cvs;

    private boolean needAutoAssignment = true;

    /**
     * Constant ASSAY.
     */
    public static final int ASSAY = 1;

    /**
     * Constant.
     */
    public static final int SV = 2;

    /**
     * Constant.
     */
    public static final int RATIO = 3;

    /**
     * Constant.
     */
    public static final int GLOBAL = 4;

    /**
     * Constant.
     */
    public static final int PROTEIN_GROUP = 10;

    /**
     * Constant.
     */
    public static final int PROTEIN = 11;

    /**
     * Constant.
     */
    public static final int PEPTIDE = 12;

    /**
     * Constant.
     */
    public static final int FEATURE = 13;

    /**
     * MzqData control.
     */
    public MzqDataControl control = new MzqDataControl();

    /**
     * Get mzq file name.
     *
     * @return mzq file name.
     */
    public String getMzqName() {
        return mzqName;
    }

    /**
     * Set mzq file name.
     *
     * @param mzqName mzq file name.
     */
    public void setMzqName(final String mzqName) {
        this.mzqName = mzqName;
    }

    /**
     * Get mzq id.
     *
     * @return mzq id.
     */
    public String getMzqID() {
        return mzqID;
    }

    /**
     * Set mzq id.
     *
     * @param mzqID mzq id.
     */
    public void setMzqID(final String mzqID) {
        this.mzqID = mzqID;
    }

    /**
     * Get InputFiles.
     *
     * @return InputFiles.
     */
    public InputFiles getInputFiles() {
        return inputFiles;
    }

    /**
     * Set InputFiles.
     *
     * @param inputFiles inputFiles.
     */
    public void setInputFiles(final InputFiles inputFiles) {
        this.inputFiles = inputFiles;
    }

    /**
     * Get AnalysisSummary.
     *
     * @return AnalysisSummary.
     */
    public AnalysisSummary getAnalysisSummary() {
        return analysisSummary;
    }

    /**
     * Set AnalysisSummary.
     *
     * @param analysisSummary analysisSummary.
     */
    public void setAnalysisSummary(final AnalysisSummary analysisSummary) {
        this.analysisSummary = analysisSummary;
    }

    /**
     * Get SoftwareList.
     *
     * @return SoftwareList
     */
    public SoftwareList getSoftwareList() {
        return softwareList;
    }

    /**
     * Set SoftwareList
     *
     * @param softwareList softwareList
     */
    public void setSoftwareList(final SoftwareList softwareList) {
        this.softwareList = softwareList;
    }

    /**
     * Add list of FeatureList.
     *
     * @param featureLists a list of FeatureList.
     */
    public void addFeatures(final List<FeatureList> featureLists) {
        for (FeatureList featureList : featureLists) {
            String rfg = featureList.getRawFilesGroupRef();
            for (Feature feature : featureList.getFeature()) {
                FeatureData featureData = new FeatureData(feature);
                featureData.setRawFilesGroupRef(rfg);
                unsolvedFeatures.put(feature.getId(), featureData);
            }
            for (QuantLayer ql : featureList.getMS2AssayQuantLayer()) {
                parseQuantLayer(ql, ASSAY, FEATURE);
            }
            for (QuantLayer ql : featureList.getMS2StudyVariableQuantLayer()) {
                parseQuantLayer(ql, SV, FEATURE);
            }
            if (featureList.getMS2RatioQuantLayer() != null) {
                parseRatioQuantLayer(featureList.getMS2RatioQuantLayer(),
                                     FEATURE);
            }
            for (GlobalQuantLayer gql : featureList.getFeatureQuantLayer()) {
                parseGlobalQuantLayer(gql, FEATURE);
            }
        }
    }

    /**
     * Add list of peptideConsensus.
     *
     * @param pcList peptideConsensusList.
     */
    public void addPeptides(final PeptideConsensusList pcList) {
        for (PeptideConsensus pc : pcList.getPeptideConsensus()) {
            PeptideData peptide = new PeptideData(pc);
            peptides.put(pc.getId(), peptide);
            unsolvedPeptides.add(pc.getId());
        }
        for (QuantLayer ql : pcList.getAssayQuantLayer()) {
            parseQuantLayer(ql, ASSAY, PEPTIDE);
        }
        for (QuantLayer ql : pcList.getStudyVariableQuantLayer()) {
            parseQuantLayer(ql, SV, PEPTIDE);
        }
        if (pcList.getRatioQuantLayer() != null) {
            parseRatioQuantLayer(pcList.getRatioQuantLayer(), PEPTIDE);
        }
        for (GlobalQuantLayer gql : pcList.getGlobalQuantLayer()) {
            parseGlobalQuantLayer(gql, PEPTIDE);
        }
    }

    /**
     * Add ProteinGroupList
     *
     * @param pgList proteinGroupList
     */
    public void addProteinGroups(final ProteinGroupList pgList) {
        if (pgList == null) {
            return;
        }
        for (ProteinGroup pg : pgList.getProteinGroup()) {
            ProteinGroupData pgData = new ProteinGroupData(pg);
            String id = pg.getId();
            pgIds.add(id);
            pgs.put(id, pgData);
        }
        for (QuantLayer ql : pgList.getAssayQuantLayer()) {
            parseQuantLayer(ql, ASSAY, PROTEIN_GROUP);
        }
        for (QuantLayer ql : pgList.getStudyVariableQuantLayer()) {
            parseQuantLayer(ql, SV, PROTEIN_GROUP);
        }
        if (pgList.getRatioQuantLayer() != null) {
            parseRatioQuantLayer(pgList.getRatioQuantLayer(), PROTEIN_GROUP);
        }
        for (GlobalQuantLayer gql : pgList.getGlobalQuantLayer()) {
            parseGlobalQuantLayer(gql, PROTEIN_GROUP);
        }
    }

    /**
     * Add ProteinList.
     *
     * @param proteinList proteinList.
     */
    public void addProteins(final ProteinList proteinList) {
        //the data structure localMapping holds the relationship between the ids from the current proteinList (keys) and the ids from the existing protein list in the memory (values)
        //will be useful if merging multiple mzq files
        //this will be used to assign quantitation values etc.
//        HashMap<String,String> localMapping = new HashMap<String, String>();
        //if the protein list is empty, create an artificial protein 
        if (proteinList == null) {
            if (!proteins.containsKey(ARTIFICIAL)) {
                Protein protein = new Protein();
                protein.setId(ARTIFICIAL);
                protein.setAccession(ARTIFICIAL);
                ProteinData proteinData = new ProteinData(protein);
                proteins.put(protein.getId(), proteinData);
                proteinIds.add(protein.getId());
            }
            return;
        }

        for (Protein qprotein : proteinList.getProtein()) {
            //check whether this protein has been processed before (more likely from the previous mzq file)
            String id = existing(qprotein);
            if (id == null) {//not matched to the existing proteins
                id = qprotein.getId();
                ProteinData newProtein = new ProteinData(qprotein);
                proteins.put(id, newProtein);
                proteinIds.add(id);
            }
        }
        for (QuantLayer ql : proteinList.getAssayQuantLayer()) {
            parseQuantLayer(ql, ASSAY, PROTEIN);
        }
        for (QuantLayer ql : proteinList.getStudyVariableQuantLayer()) {
            parseQuantLayer(ql, SV, PROTEIN);
        }
        if (proteinList.getRatioQuantLayer() != null) {
            parseRatioQuantLayer(proteinList.getRatioQuantLayer(), PROTEIN);
        }
        for (GlobalQuantLayer gql : proteinList.getGlobalQuantLayer()) {
            parseGlobalQuantLayer(gql, PROTEIN);
        }
    }

    private QuantitationLevel determineQuantObj(final int level, final Row row) {
        QuantitationLevel quantObj = null;
        switch (level) {
            case PROTEIN_GROUP:
                quantObj = pgs.get(row.getObjectRef());
                break;
            case PROTEIN:
                quantObj = proteins.get(row.getObjectRef());
                break;
            case PEPTIDE:
                quantObj = peptides.get(row.getObjectRef());
                break;
            case FEATURE:
                quantObj = unsolvedFeatures.get(row.getObjectRef());
                break;
            default:
                break;
        }
        return quantObj;
    }

    private void parseQuantLayer(final QuantLayer ql, final int type,
                                 final int level) {
        CvParam cvTerm = ql.getDataType().getCvParam();
        String quantityName = cvTerm.getName();

        if (cvParams.containsKey(quantityName)) {
            if (!quantitationNames.contains(quantityName)) {
                quantitationNames.add(quantityName);//this could happen when the quantitation name is parsed in a global quant layer which only puts the cvParam into cvParams
            }
        } else {
            cvParams.put(quantityName, cvTerm);
            quantitationNames.add(quantityName);
        }
        List<String> ids = ql.getColumnIndex();
        control.addElement(level, type, quantityName);
        for (Row row : ql.getDataMatrix().getRow()) {
            HashMap<String, Double> quantities = new HashMap<>();
            QuantitationLevel quantObj = determineQuantObj(level, row);
            for (int i = 0; i < ids.size(); i++) {
                String assayID = ids.get(i);

                Double value = parseDoubleValue(row.getValue().get(i));
                quantities.put(assayID, value);

            }
            if (type == ASSAY) {
                quantObj.setQuantities(quantityName, quantities);
            } else {
                quantObj.setStudyVariables(quantityName, quantities);
            }
        }
    }

    private Double parseDoubleValue(final String str) {
        if (str == null) {
            return null;
        }
        if (str.equalsIgnoreCase("null")) {
            return null;
        }
        if (str.equalsIgnoreCase("INF")) {
            return Double.POSITIVE_INFINITY;
        }
        if (str.equalsIgnoreCase("NaN")) {
            return Double.NaN;
        }
        return Double.parseDouble(str);
    }

    private void parseRatioQuantLayer(final RatioQuantLayer rql, final int level) {
        List<String> ratioIDs = new ArrayList<>();
        for (String ratioID : rql.getColumnIndex()) {
            ratioIDs.add(ratioID);
            control.addElement(level, RATIO, RATIO_STRING);
        }
        for (Row row : rql.getDataMatrix().getRow()) {
            QuantitationLevel quantObj = determineQuantObj(level, row);
            for (int i = 0; i < ratioIDs.size(); i++) {
                String ratioID = ratioIDs.get(i);
                Double value = parseDoubleValue(row.getValue().get(i));
                quantObj.setRatios(ratioID, value);
            }
        }
    }

    private void parseGlobalQuantLayer(final GlobalQuantLayer gql,
                                       final int level) {
        List<String> columnIDs = new ArrayList<>();
        for (Column column : gql.getColumnDefinition().getColumn()) {
            final CvParam cvParam = column.getDataType().getCvParam();
            String name = cvParam.getName();
            columnIDs.add(name);
            control.addElement(level, GLOBAL, name);
            cvParams.put(name, cvParam);
        }
        for (Row row : gql.getDataMatrix().getRow()) {
            QuantitationLevel quantObj = determineQuantObj(level, row);
            for (int i = 0; i < columnIDs.size(); i++) {
                String columnID = columnIDs.get(i);
                Double value = parseDoubleValue(row.getValue().get(i));
                quantObj.setGlobal(columnID, value);
            }
        }
    }

    /**
     * Add AssayList.
     *
     * @param assayList assayList.
     */
    public void addAssays(final AssayList assayList) {
        for (Assay assay : assayList.getAssay()) {
            assays.add(assay);
            assayIDs.add(assay.getId());
        }
    }

    /**
     * Add StudyVariableList.
     *
     * @param studyVariableList studyVariableList.
     */
    public void addStudyVariables(final StudyVariableList studyVariableList) {
        if (studyVariableList == null) {
            return;
        }
        svs.addAll(studyVariableList.getStudyVariable());
    }

    /**
     * Add RatioList.
     *
     * @param ratioList ratioList.
     */
    public void addRatios(final RatioList ratioList) {
        if (ratioList == null) {
            return;
        }
        for (Ratio ratio : ratioList.getRatio()) {
            ratios.add(ratio.getId());
        }
    }

    /**
     * Get Modification index.
     *
     * @param mod input modification.
     *
     * @return the index of modification.
     */
    public int getModificationIndex(final Modification mod) {
        String modStr = getModificationString(mod);
        if (modifications.contains(modStr)) {
            return modifications.indexOf(modStr);
        }
        modifications.add(modStr);
        return modifications.size();
    }

    //for modification in PeptideConsensus, not modification in Label which is of ModParamType type
    private String getModificationString(final Modification mod) {
        //<xsd:element name="cvParam" type="CVParamType" minOccurs="1" maxOccurs="unbounded">           
        for (CvParam cv : mod.getCvParam()) {
            if (cv.getName() == null) {
                continue;
            } else {
                return cv.getName();
            }
        }
        return null;
    }

    /**
     * Get list of ProteinGroupData.
     *
     * @return list of ProteinGroupData.
     */
    public List<ProteinGroupData> getProteinGroups() {
        List<ProteinGroupData> values = new ArrayList<>();
        for (String id : pgIds) {
            values.add(pgs.get(id));
        }
        return values;
    }

    /**
     * Get list of ProteinData.
     *
     * @return list of ProteinData.
     */
    public List<ProteinData> getProteins() {
        List<ProteinData> values = new ArrayList<>();
        for (String id : proteinIds) {
            values.add(proteins.get(id));
        }
        return values;
    }

    /**
     * Get list of quantitation names.
     *
     * @return list of quantitation names.
     */
    public List<String> getQuantitationNames() {
        return quantitationNames;
    }

    /**
     * Get quantitation CvParam by name value.
     *
     * @param name name value.
     *
     * @return quantitation CvParam.
     */
    public CvParam getQuantitationCvParam(final String name) {
        if (cvParams.containsKey(name)) {
            return cvParams.get(name);
        }
        return null;
    }

    /**
     * Get list of assays.
     *
     * @return list of assays.
     */
    public List<Assay> getAssays() {
        return assays;
    }

    /**
     * Get list of assay ids.
     *
     * @return list of assay ids.
     */
    public List<String> getAssayIDs() {
        return assayIDs;
    }

    /**
     * Get list of study variables.
     *
     * @return list of study variables.
     */
    public List<StudyVariable> getSvs() {
        return svs;
    }

    /**
     * Get list of ratios.
     *
     * @return list of ratios.
     */
    public List<String> getRatios() {
        return ratios;
    }

    private String existing(final Protein protein) {
        //first search by id
        if (proteins.containsKey(protein.getId())) {
            return protein.getId();
        }
        //then by accession number
        for (ProteinData proteinData : proteins.values()) {
            if (protein.getAccession().equalsIgnoreCase(proteinData.
                    getAccession())) {
                return proteinData.getProtein().getId();
            }
        }
        //still not found
        return null;
    }

    /**
     * Get list of PeptideData.
     *
     * @return list of PeptideData.
     */
    public List<PeptideData> getPeptides() {
        List<PeptideData> values = new ArrayList<>();
        List<String> idList = new ArrayList<>();
        for (String id : peptides.keySet()) {
            idList.add(id);
        }
        Collections.sort(idList);
        for (String id : idList) {
            values.add(peptides.get(id));
        }
        return values;
    }

    /**
     * Get list of FeatureData.
     *
     * @return list of FeatureData.
     */
    public List<FeatureData> getFeatures() {
        List<FeatureData> values = new ArrayList<>();
        if (!needAutoAssignment) {
            List<String> idList = new ArrayList<>();
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

    /**
     * Get the flag of needAutoAssignment.
     *
     * @return ture if needs auto assignment.
     */
    public boolean needAutoAssignment() {
        return needAutoAssignment;
    }

    /**
     * Set the flag value for needAutoAssignment.
     *
     * @param needAutoAssignment needAutoAssignment.
     */
    public void setNeedAutoAssignment(final boolean needAutoAssignment) {
        this.needAutoAssignment = needAutoAssignment;
    }

    /**
     * Do auto assign.
     */
    public void autoAssign() {
        if (!needAutoAssignment) {
            return;
        }
        for (PeptideData peptide : peptides.values()) {
            for (EvidenceRef evidence : peptide.getPeptide().getEvidenceRef()) {
                FeatureData feature = unsolvedFeatures.get(evidence.
                        getFeatureRef());
                unsolvedFeatures.remove(evidence.getFeatureRef());
                peptide.addFeature(feature);
            }
        }
        for (ProteinData protein : proteins.values()) {
            for (String id : protein.getProtein().getPeptideConsensusRefs()) {
                PeptideData peptide = peptides.get(id);
                unsolvedPeptides.remove(id);
                //TODO not sure whether one peptide related to multiple proteins case will cause bug here
                peptide.setAssignedByPeptideRef(true);
                protein.addPeptide(peptide);
            }
        }
        //un-linked peptides according to the peptideconsensus_refs in protein
        for (String peptideID : unsolvedPeptides) {
            boolean guessed = false;
            for (ProteinData protein : proteins.values()) {
                if (peptideID.contains(protein.getId()) || peptideID.contains(
                        protein.getAccession())) {
                    guessed = true;
                    PeptideData peptide = peptides.get(peptideID);
                    protein.addPeptide(peptide);
                }
            }
            if (!guessed) {
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

    /**
     * Set CvList.
     *
     * @param cvList cvList.
     */
    public void setCvList(final CvList cvList) {
        cvs = cvList.getCv();
    }

    /**
     * Get list of Cvs.
     *
     * @return list of Cvs.
     */
    public List<Cv> getCvList() {
        return cvs;
    }

    /**
     * Get ProteinData by anchor protein name.
     *
     * @param anchorProteinStr anchor protein name.
     *
     * @return ProteinData.
     */
    public ProteinData getProtein(final String anchorProteinStr) {
        if (proteins.containsKey(anchorProteinStr)) {
            return proteins.get(anchorProteinStr);
        }
        return null;
    }

}
