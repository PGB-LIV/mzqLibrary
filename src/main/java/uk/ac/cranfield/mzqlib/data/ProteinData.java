package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.liv.jmzqml.model.mzqml.Param;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
import uk.ac.liv.jmzqml.model.mzqml.SearchDatabase;
/**
 * The Protein class
 * @author Jun Fan@cranfield
 */
public class ProteinData extends QuantitationLevel{
    /**
     * the corresponding protein element in mzQuantML
     */
    private Protein protein;
    /**
     * the list of peptides
     */
    private HashMap<String,PeptideSequenceData> peptides;

    public ProteinData(Protein pro) {
        protein = pro;
        peptides = new HashMap<>();
    }
    /**
     * Get the protein mzQuantML element
     * @return the protein mzQuantML element
     */
    public Protein getProtein() {
        return protein;
    }
    /**
     * Get the accession of the protein
     * @return the accession
     */
    public String getAccession(){
        return protein.getAccession();
    }
    
    public String getId(){
        return protein.getId();
    }
    
    public String getSearchDatabase(){
//        Param databaseName = ((SearchDatabase)protein.getSearchDatabaseRef()).getDatabaseName();
        Param databaseName = ((SearchDatabase)protein.getSearchDatabase()).getDatabaseName();
        if(databaseName.getCvParam()!=null){
            return databaseName.getCvParam().getName();
        }else{
            return databaseName.getUserParam().getName();
        }
    }
    
    public String getSearchDatabaseVersion(){
        return ((SearchDatabase)protein.getSearchDatabase()).getVersion();
    }

    
    @Override
    public boolean equals(Object obj){
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof ProteinData)) {
            return false;
        }
        ProteinData pro = (ProteinData)obj;
        if(this.getAccession().endsWith(pro.getAccession())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.getAccession() != null ? this.getAccession().hashCode() : 0);
        return hash;
    }
    
    @Override
    public int getCount(){
        return peptides.size();
    }

    public ArrayList<PeptideData> getPeptides(){
        ArrayList<PeptideData> result = new ArrayList<>();
        for(PeptideSequenceData psData: peptides.values()){
            result.addAll(psData.getPeptides());
        }
        return result;
    }
    
    void addPeptide(PeptideData peptide) {
        String seq = peptide.getSeq();
        if(!peptides.containsKey(seq)){
            PeptideSequenceData psData = new PeptideSequenceData(seq);
            peptides.put(seq, psData);
        }
        PeptideSequenceData psData = peptides.get(seq);
        psData.addPeptideData(peptide);
    }
}
