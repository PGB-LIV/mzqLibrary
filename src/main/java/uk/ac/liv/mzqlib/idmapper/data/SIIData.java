
package uk.ac.liv.mzqlib.idmapper.data;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.ac.ebi.jmzidml.model.mzidml.*;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;

/**
 * Wrapper class for SpectrumIdentificationItem element.
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 03-Mar-2014 17:20:15
 */
public class SIIData implements Comparable<SIIData> {

    private final String id;
    private final String pepRef;
    private String sequence;
    private final double mzExperimental;
    private final double mzCalculated;
    private final int charge;
    private double rt = Double.NaN;
    private final int rank;
    private final boolean passTh;
    private final List peptideEvidenceRef;
    private final String peptideModString;
    private final MzIdentMLUnmarshaller um;
    private String mzidFn;

    /**
     * Constructor of SIIData base on parameters.
     *
     * @param sii    SpectrumIdentificationItem
     * @param umarsh MzIdentMLUnmarshaller
     */
    public SIIData(SpectrumIdentificationItem sii, MzIdentMLUnmarshaller umarsh) {

        this.um = umarsh;
        this.pepRef = sii.getPeptideRef();
        this.id = sii.getId();
        this.mzCalculated = sii.getCalculatedMassToCharge();
        this.mzExperimental = sii.getExperimentalMassToCharge();
        this.charge = sii.getChargeState();
        this.rank = sii.getRank();
        this.passTh = sii.isPassThreshold();
        this.peptideEvidenceRef = sii.getPeptideEvidenceRef();
        this.peptideModString = this.createPeptideModString();
    }

    /**
     * Get MzIdentMLUnmarshaller.
     *
     * @return MzIdentMLUnmarshaller
     */
    public MzIdentMLUnmarshaller getUnmarshaller() {
        return um;
    }

    /**
     * Get id of SpectrumIdentificationItem.
     *
     * @return id string
     */
    public String getId() {
        return id;
    }

    /**
     * Get peptide reference.
     *
     * @return peptide reference string
     */
    public String getPeptideRef() {
        return pepRef;
    }

    /**
     * Get calculated m/z.
     *
     * @return calculated m/z double
     */
    public double getCalculatedMassToCharge() {
        return mzCalculated;
    }

    /**
     * Get experimental m/z.
     *
     * @return experimental m/z double
     */
    public double getExperimentalMassToCharge() {
        return mzExperimental;
    }

    /**
     * Get charge.
     *
     * @return charge value
     */
    public int getCharge() {
        return charge;
    }

    /**
     * Get rank.
     *
     * @return rank value
     */
    public int getRank() {
        return rank;
    }

    /**
     * Get attribute passthreshold.
     *
     * @return boolean value of passthreshold
     */
    public boolean isPassThreshold() {
        return passTh;
    }

    /**
     * Get peptide evidence reference list.
     *
     * @return list of reference string
     */
    public List getPeptideEvidenceRef() {
        return peptideEvidenceRef;
    }

    /**
     * Get peptide modification string.
     *
     * @return peptide modification string
     */
    public String getPeptideModString() {
        return peptideModString;
    }

    /**
     * Get peptide sequence.
     *
     * @return sequence string
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Set retention time.
     *
     * @param rt retention time in double value
     */
    public void setRetentionTime(double rt) {
        this.rt = rt;
    }

    /**
     * Get retention time.
     *
     * @return retention time
     */
    public double getRetentionTime() {
        return this.rt;
    }

    @Override
    public int compareTo(SIIData compareSIIData) {
        String compareModString = compareSIIData.getPeptideModString();

        //ascending order
        return this.peptideModString.compareTo(compareModString);

        //descending order
        //return compareModString.compareTo(this.peptideModString);
    }

    /**
     * SIIData is comparable base on the retention time value.
     */
    public static Comparator<SIIData> SIIDataRTComparator = new Comparator<SIIData>() {

        @Override
        public int compare(SIIData siiData1, SIIData siiData2) {
            double rt1 = siiData1.getRetentionTime();
            double rt2 = siiData2.getRetentionTime();

            //ascending order
            return Double.compare(rt1, rt2);

            //descending order
            //return Double.compare(rt2, rt1);
        }

    };

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        SIIData rhs = (SIIData) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.getPeptideModString(), rhs.getPeptideModString())
                .append(getId(), rhs.getId())
                .append(getPeptideRef(), rhs.getPeptideRef())
                .append(getCalculatedMassToCharge(), rhs.getCalculatedMassToCharge())
                .append(getExperimentalMassToCharge(), rhs.getExperimentalMassToCharge())
                .append(getCharge(), rhs.getCharge())
                .append(getRank(), rhs.getRank())
                .append(isPassThreshold(), rhs.isPassThreshold())
                .isEquals();
    }

    @Override
    public int hashCode() {
        int hash = 59;
        return new HashCodeBuilder(hash, 37)
                .append(this.getPeptideModString())
                .append(getId())
                .append(getPeptideRef())
                .append(getCalculatedMassToCharge())
                .append(getExperimentalMassToCharge())
                .append(getCharge())
                .append(getRank())
                .append(isPassThreshold())
                .toHashCode();
    }

    /**
     * Create peptide mod string for this SIIData.
     * The modString contains peptide sequence and list of mods.
     * Each mod contains mod accession and mod name, plus monoisotopicMassDelta and location.
     * They all connected by '_'.
     *
     * @return
     */
    private String createPeptideModString() {
        StringBuffer modString = new StringBuffer();
        try {
            Peptide peptide = um.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.Peptide.class, pepRef);
            String pepSeq = peptide.getPeptideSequence();
            this.setSequence(pepSeq);
            List<Modification> mods = peptide.getModification();
            modString.append(pepSeq);
            modString.append('_');
            for (Modification mod : mods) {
                //modString = modString + mod.getLocation().toString() + "_";
                List<CvParam> cps = mod.getCvParam();
                for (CvParam cp : cps) {
                    modString = modString.append(cp.getAccession()).append('_')
                            .append(cp.getName()).append('_').append(mod.getMonoisotopicMassDelta())
                            .append('_').append(mod.getLocation()).append('_');
                }
            }
            return modString.substring(0, modString.length() - 1); //remove the last '_'
        }
        catch (JAXBException ex) {
            Logger.getLogger(SIIData.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Couldn't get peptide object: " + pepRef + " -- " + ex.getMessage());
            return modString.toString();
        }

    }

    /**
     * Get mzIdentML file name.
     *
     * @return the mzIdentML file name
     */
    public String getMzidFn() {
        return mzidFn;
    }

    /**
     * Set mzIdentML file name.
     *
     * @param mzidFn the mzIdentML file name
     */
    public void setMzidFn(String mzidFn) {
        this.mzidFn = mzidFn;
    }

    /**
     *
     * Set peptide sequence.
     *
     * @param sequence peptide sequence
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

}
