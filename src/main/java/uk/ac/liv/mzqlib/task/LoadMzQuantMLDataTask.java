package uk.ac.liv.mzqlib.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.AnalysisSummary;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.jmzqml.model.mzqml.IdOnly;
import uk.ac.liv.jmzqml.model.mzqml.Modification;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
import uk.ac.liv.jmzqml.model.mzqml.ProteinGroup;
import uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList;
import uk.ac.liv.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.jmzqml.model.mzqml.ProteinRef;
import uk.ac.liv.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.Row;
import uk.ac.liv.jmzqml.model.mzqml.SmallMoleculeList;
import uk.ac.liv.jmzqml.model.mzqml.Software;
import uk.ac.liv.jmzqml.model.mzqml.SoftwareList;
import uk.ac.liv.jmzqml.model.mzqml.UserParam;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.mzqlib.model.MzQuantMLData;
import uk.ac.liv.mzqlib.model.MzQuantMLSummary;
import uk.ac.liv.mzqlib.model.MzqAssayQuantLayer;
import uk.ac.liv.mzqlib.model.MzqDataMatrixRow;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 09-Sep-2014 17:57:19
 */
public class LoadMzQuantMLDataTask extends Task<MzQuantMLData> {
    
    private final File mzqFile;
    
    public LoadMzQuantMLDataTask(File f) {
        mzqFile = f;
    }
    
    @Override
    protected MzQuantMLData call()
            throws Exception {
        
        MzQuantMLData mzqData = new MzQuantMLData();

        //
        // unmarshalling mzq file
        //
        updateMessage("Unmarshalling mzq file");
        
        MzQuantMLUnmarshaller mzqUm = new MzQuantMLUnmarshaller(mzqFile);
        
        mzqData.setMzQuantMLUnmarshaller(mzqUm);

        //
        // construct MzQuantMLSummary object
        //
        updateMessage("Loading file summary");
        
        MzQuantMLSummary mzqSum = new MzQuantMLSummary();

        // Set techniquesUsed
        AnalysisSummary analySum = mzqUm.unmarshal(MzQuantMLElement.AnalysisSummary);
        List<CvParam> cvParams = analySum.getCvParam();
        
        List<StringProperty> techList = new ArrayList<>();

        //parallel computing
        cvParams.stream().parallel().filter((cp) -> (!cp.getName().contains("level"))).forEach((cp) -> {
//TODO: how do we check the status or does the code need for checking in stream?
//            if (isCancelled()) {
//                updateMessage("Cancelled");
//                break;
//            }
            techList.add(new SimpleStringProperty(cp.getName()));
        });
        
        mzqSum.setTechniquesUsed(techList);
        
        int listNumber = 0;
        // Set numbers of protein group list   
        listNumber = mzqUm.getObjectCountForXpath("/MzQuantML/ProteinGroupList");
        mzqSum.setProteinGroupListNumber(listNumber == -1 ? 0 : listNumber);

        // Set numbers of protein list
        listNumber = mzqUm.getObjectCountForXpath("/MzQuantML/ProteinList");
        mzqSum.setProteinListNumber(listNumber == -1 ? 0 : listNumber);

        // Set numbers of peptide list
        listNumber = mzqUm.getObjectCountForXpath("/MzQuantML/PeptideConsensusList");
        mzqSum.setPeptideListNumber(listNumber == -1 ? 0 : listNumber);

        // Set numbers of feature list
        listNumber = mzqUm.getObjectCountForXpath("/MzQuantML/FeatureList");
        mzqSum.setFeatureListNumber(listNumber == -1 ? 0 : listNumber);

        // Set software
        SoftwareList softwareList = mzqUm.unmarshal(MzQuantMLElement.SoftwareList);
        StringBuilder softwareString = new StringBuilder();
        for (Software software : softwareList.getSoftware()) {
            
            for (CvParam cp : software.getCvParam()) {
                softwareString.append(cp.getName()).append(", ");
            }
            
            for (UserParam up : software.getUserParam()) {
                softwareString.append(up.getName()).append(", ");
            }
            
            softwareString.append("version ").append(software.getVersion());
        }
        
        mzqSum.setSoftware(new SimpleStringProperty(softwareString.toString()));
        
        mzqData.setMzQuantMLSummary(mzqSum);

        //
        // Construct a list of MzqAssayQuantLayer
        //          
        ObservableList<MzqAssayQuantLayer> assayQuantLayerList = FXCollections.observableArrayList();

        // Process protein group list
        ProteinGroupList protGrpList = mzqUm.unmarshal(MzQuantMLElement.ProteinGroupList);
        if (protGrpList != null) {
            
            updateMessage("Processing protein group list");//update message

            List<QuantLayer<IdOnly>> protGrpAssQLs = protGrpList.getAssayQuantLayer();
            for (QuantLayer assQL : protGrpAssQLs) {
                if (isCancelled()) {
                    updateMessage("Cancelled");
                    break;
                }
                String dataType = assQL.getDataType().getCvParam().getName();
                
                MzqAssayQuantLayer mzqAQL = new MzqAssayQuantLayer(mzqUm, protGrpList.getId(), assQL, "ProteinGroup", dataType);
                //set list of MzqDataMatrixRow
                List<Row> rows = assQL.getDataMatrix().getRow();
                
                for (Row row : rows) {
                    MzqDataMatrixRow mzqDMRow = new MzqDataMatrixRow();
                    ProteinGroup protGrp = mzqUm.unmarshal(uk.ac.liv.jmzqml.model.mzqml.ProteinGroup.class, row.getObjectRef()); //get ProteinGrouop object from objectRef
                    List<ProteinRef> protRefs = protGrp.getProteinRef();
                    //represent each row with accessions
                    StringBuilder proteinGroupAccession = new StringBuilder();
                    for (ProteinRef protRef : protRefs) {
                        Protein protein = mzqUm.unmarshal(uk.ac.liv.jmzqml.model.mzqml.Protein.class, protRef.getProteinRef());
                        proteinGroupAccession.append(protein.getAccession()).append(";");
                    }
                    mzqDMRow.setObjectId(new SimpleStringProperty(proteinGroupAccession.substring(0, proteinGroupAccession.length() - 2)));
                    mzqDMRow.setValues(row.getValue());
                    mzqAQL.getDmRows().add(mzqDMRow);
                }
                assayQuantLayerList.add(mzqAQL);
            }
        }

        // Process protein list
        ProteinList protList = mzqUm.unmarshal(MzQuantMLElement.ProteinList);
        if (protList != null) {
            
            updateMessage("Processing protein list");
            
            List<QuantLayer<IdOnly>> protAssQLs = protList.getAssayQuantLayer();
            for (QuantLayer assQL : protAssQLs) {
                if (isCancelled()) {
                    updateMessage("Cancelled");
                    break;
                }
                String dataType = assQL.getDataType().getCvParam().getName();
                
                MzqAssayQuantLayer mzqAQL = new MzqAssayQuantLayer(mzqUm, protList.getId(), assQL, "Protein", dataType);
                // set list of MzqDataMatrix
                List<Row> rows = assQL.getDataMatrix().getRow();
                for (Row row : rows) {
                    MzqDataMatrixRow mzqDMRow = new MzqDataMatrixRow();
                    Protein protein = mzqUm.unmarshal(uk.ac.liv.jmzqml.model.mzqml.Protein.class, row.getObjectRef());
                    mzqDMRow.setObjectId(new SimpleStringProperty(protein.getAccession()));
                    mzqDMRow.setValues(row.getValue());
                    mzqAQL.getDmRows().add(mzqDMRow);
                }
                assayQuantLayerList.add(mzqAQL);
            }
        }

        // Process peptide list
        Iterator<PeptideConsensusList> peptideListIter = mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.PeptideConsensusList);
        if (peptideListIter != null) {
            
            updateMessage("Processing peptide list");
            
            while (peptideListIter.hasNext()) {
                if (isCancelled()) {
                    updateMessage("Cancelled");
                    break;
                }
                PeptideConsensusList peptideList = peptideListIter.next();
                List<QuantLayer<IdOnly>> pepAssQLs = peptideList.getAssayQuantLayer();
                for (QuantLayer assQL : pepAssQLs) {
                    if (isCancelled()) {
                        updateMessage("Cancelled");
                        break;
                    }
                    String dataType = assQL.getDataType().getCvParam().getName();
                    
                    MzqAssayQuantLayer mzqAQL = new MzqAssayQuantLayer(mzqUm, peptideList.getId(), assQL, "PeptideConsensus", dataType);
                    // set list of MzqDataMatrix for peptide assay quant layer
                    List<Row> rows = assQL.getDataMatrix().getRow();
                    for (Row row : rows) {
                        MzqDataMatrixRow mzqDMRow = new MzqDataMatrixRow();
                        PeptideConsensus peptide = mzqUm.unmarshal(uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus.class, row.getObjectRef());
                        StringBuilder peptideString = new StringBuilder();
                        peptideString.append(peptide.getPeptideSequence());
                        if (!peptide.getModification().isEmpty()) {
                            peptideString.append(":");
                            for (Modification mod : peptide.getModification()) {
                                for (CvParam cp : mod.getCvParam()) {
                                    peptideString.append(cp.getName()).append(" ");
                                }
                                peptideString.append("@").append(mod.getLocation().toString()).append(" ;");
                            }
                            mzqDMRow.setObjectId(new SimpleStringProperty(peptideString.substring(0, peptideString.length() - 2)));
                        }
                        else {
                            mzqDMRow.setObjectId(new SimpleStringProperty(peptideString.toString()));
                        }
                        mzqDMRow.setValues(row.getValue());
                        mzqAQL.getDmRows().add(mzqDMRow);
                    }
                    assayQuantLayerList.add(mzqAQL);
                }
            }
        }

        // Process feature list
        Iterator<FeatureList> featureListIter = mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);
        if (featureListIter != null) {
            
            updateMessage("Processing feature list");
            
            while (featureListIter.hasNext()) {
                if (isCancelled()) {
                    updateMessage("Cancelled");
                    break;
                }
                FeatureList featureList = featureListIter.next();
                List<QuantLayer<IdOnly>> ftAssQLs = featureList.getMS2AssayQuantLayer();
                for (QuantLayer assQL : ftAssQLs) {
                    if (isCancelled()) {
                        updateMessage("Cancelled");
                        break;
                    }
                    String dataType = assQL.getDataType().getCvParam().getName();
                    
                    MzqAssayQuantLayer mzqAQL = new MzqAssayQuantLayer(mzqUm, featureList.getId(), assQL, "Feature", dataType);
                    // set list of MzqDataMatrix for feature assay quant layer
                    List<Row> rows = assQL.getDataMatrix().getRow();
                    for (Row row : rows) {
                        MzqDataMatrixRow mzqDMRow = new MzqDataMatrixRow();
                        
                        mzqDMRow.setObjectId(new SimpleStringProperty(row.getObjectRef()));
                        mzqDMRow.setValues(row.getValue());
                        mzqAQL.getDmRows().add(mzqDMRow);
                    }
                    
                    assayQuantLayerList.add(mzqAQL);
                }
            }
        }

        // Process small molecule list
        SmallMoleculeList smallMolList = mzqUm.unmarshal(MzQuantMLElement.SmallMoleculeList);
        if (smallMolList != null) {
            
            updateMessage("Processing small molecule list");
            
            List<QuantLayer<IdOnly>> smallMolAssQLs = smallMolList.getAssayQuantLayer();
            for (QuantLayer assQL : smallMolAssQLs) {
                if (isCancelled()) {
                    updateMessage("Cancelled");
                    break;
                }
                String dataType = assQL.getDataType().getCvParam().getName();
                
                MzqAssayQuantLayer mzqAQL = new MzqAssayQuantLayer(mzqUm, smallMolList.getId(), assQL, "SmallMolecule", dataType);
                // set list of MzqDataMatrix for small molecule assay quant layer
                List<Row> rows = assQL.getDataMatrix().getRow();
                for (Row row : rows) {
                    MzqDataMatrixRow mzqDMRow = new MzqDataMatrixRow();
                    
                    mzqDMRow.setObjectId(new SimpleStringProperty(row.getObjectRef()));
                    mzqDMRow.setValues(row.getValue());
                    mzqAQL.getDmRows().add(mzqDMRow);
                }
                assayQuantLayerList.add(mzqAQL);
            }
        }
        
        mzqData.setMzqQuantLayerList(assayQuantLayerList);
        
        updateMessage("Loading successfully");
        updateProgress(1, 1);
        
        return mzqData;
    }
    
}
