package uk.ac.cranfield.mzqlib.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * PeptideSequenceData.
 *
 * @author Jun Fan
 */
public class PeptideSequenceData {
    private final String                   sequence;
    private final Map<String, PeptideData> peptides;

    /**
     * Constructor of PeptideSequenceData.
     *
     * @param sequence peptide sequence.
     */
    public PeptideSequenceData(final String sequence) {
        this.sequence = sequence;
        peptides      = new HashMap<>();
    }

    /**
     * Add PeptideData to this class.
     *
     * @param peptide new PeptideData.
     */
    public final void addPeptideData(final PeptideData peptide) {
        String modStr = peptide.getModString();

        if (peptides.containsKey(modStr)) {
            peptides.get(modStr).mergeAnotherPeptideData(peptide);
        } else {
            peptides.put(modStr, peptide);
        }
    }

    /**
     * Get collection of PeptideData.
     *
     * @return collection of PeptideData.
     */
    public final Collection<PeptideData> getPeptides() {
        return peptides.values();
    }

    /**
     * Get peptide sequence.
     *
     * @return sequence.
     */
    public final String getSequence() {
        return sequence;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
