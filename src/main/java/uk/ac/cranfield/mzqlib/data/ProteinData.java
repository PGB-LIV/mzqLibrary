package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
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
    private ArrayList<String> peptides;

    public ProteinData(Protein pro) {
        protein = pro;
        peptides = new ArrayList<String>();
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
    /**
     * Get all peptids
     * @return 
     */
    public ArrayList<String> getPeptides() {
        return peptides;
    }
    
    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(!(obj instanceof ProteinData)) return false;
        ProteinData pro = (ProteinData)obj;
        if(this.getAccession().endsWith(pro.getAccession())) return true;
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
}
