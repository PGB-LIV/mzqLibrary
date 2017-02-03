package uk.ac.cranfield.mzqlib.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Modification;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensus;

/**
 * PeptideData contains all the data about PeptideConsensus.
 *
 * @author Jun Fan
 */
public class PeptideData extends QuantitationLevel {

//  private String peptideID;
    private String modStr        = "";
    private String modifications = "";

    /**
     * features are rawFilesGroup specific, which is corresponding to msrun
     * so suitable for a hash map: keys are msrun id and values are the list of
     * features
     */

//  private HashMap<String,ArrayList<FeatureData>> features;
    private final List<FeatureData> features             = new ArrayList<>();
    private boolean                 assignedByPeptideRef = false;
    private final PeptideConsensus  peptide;

    /**
     * Constructor of PeptideData.
     *
     * @param pc PeptiedConsensus value.
     */
    public PeptideData(final PeptideConsensus pc) {
        peptide = pc;

//      ArrayList<Character> modIndice = new ArrayList<Character>();
        List<Integer> modIndice = new ArrayList<>();
        StringBuilder modSb     = new StringBuilder();

        for (Modification mod : pc.getModification()) {
            modSb.append(mod.getCvParam().get(0).getName());
            modSb.append(" ");

            int index = MzqLib.DATA.getModificationIndex(mod);

            modIndice.add(index);
        }

        if (modSb.length() > 0) {
            modSb.deleteCharAt(modSb.length() - 1);
        }

        modifications = modSb.toString();

        Integer[] arr = new Integer[modIndice.size()];

        modIndice.toArray(arr);
        Arrays.sort(arr);
        modStr = Arrays.toString(arr);
    }

    void addFeature(final FeatureData feature) {
        features.add(feature);
    }

    /**
     * Merge to PeptideDatas.
     *
     * @param another the second PeptideData to be merged.
     */
    public void mergeAnotherPeptideData(final PeptideData another) {
        this.features.addAll(another.getFeatures());

        if (!assignedByPeptideRef) {
            setAssignedByPeptideRef(another.isAssignedByPeptideRef());
        }

        this.peptide.getEvidenceRef().addAll(another.getPeptide().getEvidenceRef());

        // TODO more things need to be added here when merging two or more mzq files, e.g. searchDatabaseRef
    }

    /**
     * Get the value of assignedBypeptideRef.
     *
     * @return assignedByPeptideRef.
     */
    public boolean isAssignedByPeptideRef() {
        return assignedByPeptideRef;
    }

    /**
     * Set the value of assignedByPeptideRef.
     *
     * @param assignedByPeptideRef assignedByPeptideRef.
     */
    public void setAssignedByPeptideRef(final boolean assignedByPeptideRef) {
        this.assignedByPeptideRef = assignedByPeptideRef;
    }

    /**
     * Get array of charges.
     *
     * @return array of charges.
     */
    public int[] getCharges() {
        int[] charges = new int[peptide.getCharge().size()];

        for (int i = 0; i < peptide.getCharge().size(); i++) {
            charges[i] = Integer.parseInt(peptide.getCharge().get(i));
        }

        return charges;
    }

    /**
     * Get number of features.
     *
     * @return size.
     */
    @Override
    public int getCount() {

//      return getAllFeatures().size();
        return features.size();
    }

    /**
     * Get list of FeatureData.
     *
     * @return list of FeatureData.
     */
    public List<FeatureData> getFeatures() {
        return features;
    }

    /**
     * Get list of FeatureData with specified charge.
     *
     * @param charge charge state.
     *
     * @return list of FeatureData.
     */
    public List<FeatureData> getFeaturesWithCharge(final int charge) {
        List<FeatureData> values = new ArrayList<>();

        for (FeatureData feature : features) {
            String value = feature.getFeature().getCharge();    // <xsd:attribute name="charge" type="integerOrNullType" use="required">

            if (value != null &&!value.equalsIgnoreCase("null") && charge == Integer.parseInt(value)) {
                values.add(feature);
            }
        }

        return values;
    }

    /**
     * Get peptide id.
     *
     * @return peptide id.
     */
    public String getId() {
        return peptide.getId();
    }

    /**
     * Get modification string.
     *
     * @return modification string.
     */
    public String getModString() {
        return modStr;
    }

    /**
     * Get modifications.
     *
     * @return modifications.
     */
    public String getModifications() {
        return modifications;
    }

    /**
     * Get PeptideConsensus.
     *
     * @return PeptideConsensus.
     */
    public PeptideConsensus getPeptide() {
        return peptide;
    }

    /**
     * Get the peptide sequence.
     *
     * @return peptide sequence string.
     */
    public String getSeq() {
        return peptide.getPeptideSequence();
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
