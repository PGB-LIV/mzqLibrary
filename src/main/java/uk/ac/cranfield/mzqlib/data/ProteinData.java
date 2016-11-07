
package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.liv.pgb.jmzqml.model.mzqml.Param;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Protein;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SearchDatabase;

/**
 * The Protein class
 *
 * @author Jun Fan@cranfield
 */
public class ProteinData extends QuantitationLevel {

    /**
     * the corresponding protein element in mzQuantML
     */
    private final Protein protein;
    /**
     * the list of peptides
     */
    private final Map<String, PeptideSequenceData> peptides;

    /**
     * Constructor of ProteinData class.
     *
     * @param pro input Protein.
     */
    public ProteinData(final Protein pro) {
        protein = pro;
        peptides = new HashMap<>();
    }

    /**
     * Get the protein mzQuantML element.
     *
     * @return the protein mzQuantML element.
     */
    public Protein getProtein() {
        return protein;
    }

    /**
     * Get the accession of the protein.
     *
     * @return the accession.
     */
    public String getAccession() {
        return protein.getAccession();
    }

    /**
     * Get the protein id.
     *
     * @return protein id.
     */
    public String getId() {
        return protein.getId();
    }

    /**
     * Get the SearchDatabase.
     *
     * @return searchDatabase.
     */
    public String getSearchDatabase() {
//        Param databaseName = ((SearchDatabase)protein.getSearchDatabaseRef()).getDatabaseName();
        Param databaseName = ((SearchDatabase) protein.getSearchDatabase()).
                getDatabaseName();
        if (databaseName.getCvParam() != null) {
            return databaseName.getCvParam().getName();
        } else {
            return databaseName.getUserParam().getName();
        }
    }

    /**
     * Get version of SearchDatabase.
     *
     * @return version.
     */
    public String getSearchDatabaseVersion() {
        return ((SearchDatabase) protein.getSearchDatabase()).getVersion();
    }

    /**
     * Equal method.
     *
     * @param obj the object to be compared with.
     *
     * @return true if two objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProteinData)) {
            return false;
        }
        ProteinData pro = (ProteinData) obj;
        if (this.getAccession().endsWith(pro.getAccession())) {
            return true;
        }
        return false;
    }

    /**
     * Override hashCode method.
     *
     * @return hash code.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.getAccession() != null ? this.getAccession().
                hashCode() : 0);
        return hash;
    }

    /**
     * Get number of peptides in the Protein.
     *
     * @return peptide size.
     */
    @Override
    public int getCount() {
        return peptides.size();
    }

    /**
     * Get list of PeptideData.
     *
     * @return list of PeptideData.
     */
    public List<PeptideData> getPeptides() {
        List<PeptideData> result = new ArrayList<>();
        for (PeptideSequenceData psData : peptides.values()) {
            result.addAll(psData.getPeptides());
        }
        return result;
    }

    void addPeptide(final PeptideData peptide) {
        String seq = peptide.getSeq();
        if (!peptides.containsKey(seq)) {
            PeptideSequenceData psData = new PeptideSequenceData(seq);
            peptides.put(seq, psData);
        }
        PeptideSequenceData psData = peptides.get(seq);
        psData.addPeptideData(peptide);
    }

}
