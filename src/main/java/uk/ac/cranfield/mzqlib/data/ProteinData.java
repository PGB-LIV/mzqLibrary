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
    private final int HASH_PRIME_ONE = 7;
    private final int HASH_PRIME_TWO = 17;

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
        protein  = pro;
        peptides = new HashMap<>();
    }

    public final void addPeptide(final PeptideData peptide) {
        String seq = peptide.getSeq();

        if (!peptides.containsKey(seq)) {
            PeptideSequenceData psData = new PeptideSequenceData(seq);

            peptides.put(seq, psData);
        }

        PeptideSequenceData psData = peptides.get(seq);

        psData.addPeptideData(peptide);
    }

    /**
     * Equal method.
     *
     * @param obj the object to be compared with.
     *
     * @return true if two objects are equal.
     */
    @Override
    public final boolean equals(final Object obj) {
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
    public final int hashCode() {
        int hash = HASH_PRIME_ONE;

        hash = HASH_PRIME_TWO * hash + (this.getAccession() != null
                                        ? this.getAccession().hashCode()
                                        : 0);

        return hash;
    }

    /**
     * Get the accession of the protein.
     *
     * @return the accession.
     */
    public final String getAccession() {
        return protein.getAccession();
    }

    /**
     * Get number of peptides in the Protein.
     *
     * @return peptide size.
     */
    @Override
    public final int getCount() {
        return peptides.size();
    }

    /**
     * Get the protein id.
     *
     * @return protein id.
     */
    public final String getId() {
        return protein.getId();
    }

    /**
     * Get list of PeptideData.
     *
     * @return list of PeptideData.
     */
    public final List<PeptideData> getPeptides() {
        List<PeptideData> result = new ArrayList<>();

        for (PeptideSequenceData psData : peptides.values()) {
            result.addAll(psData.getPeptides());
        }

        return result;
    }

    /**
     * Get the protein mzQuantML element.
     *
     * @return the protein mzQuantML element.
     */
    public final Protein getProtein() {
        return protein;
    }

    /**
     * Get the SearchDatabase.
     *
     * @return searchDatabase.
     */
    public final String getSearchDatabase() {

//      Param databaseName = ((SearchDatabase)protein.getSearchDatabaseRef()).getDatabaseName();
        Param databaseName = ((SearchDatabase) protein.getSearchDatabase()).getDatabaseName();

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
    public final String getSearchDatabaseVersion() {
        return ((SearchDatabase) protein.getSearchDatabase()).getVersion();
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
