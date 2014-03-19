/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
    private List peptideEvidenceRef;
    private String peptideModString;
    //private SpectrumIdentificationItem sii;
    private MzIdentMLUnmarshaller um;
    private String mzidFn;

    public SIIData(SpectrumIdentificationItem sii, MzIdentMLUnmarshaller umarsh) {
        //this.sii = sii;
        this.um = umarsh;
        this.pepRef = sii.getPeptideRef();
        this.id = sii.getId();
        this.mzCalculated = sii.getCalculatedMassToCharge().doubleValue();
        this.mzExperimental = sii.getExperimentalMassToCharge();
        this.charge = sii.getChargeState();
        this.rank = sii.getRank();
        this.passTh = sii.isPassThreshold();
        this.peptideEvidenceRef = sii.getPeptideEvidenceRef();
        this.peptideModString = this.createPeptideModString();
    }

//    public SpectrumIdentificationItem getSII() {
//        return sii;
//    }

    public MzIdentMLUnmarshaller getUnmarshaller() {
        return um;
    }

    public String getId() {
        return id;
    }

    public String getPeptideRef() {
        return pepRef;
    }

    public double getCalculatedMassToCharge() {
        return mzCalculated;
    }

    public double getExperimentalMassToCharge() {
        return mzExperimental;
    }

    public int getCharge() {
        return charge;
    }

    public int getRank() {
        return rank;
    }

    public boolean isPassThreshold() {
        return passTh;
    }

    public List getPeptideEvidenceRef() {
        return peptideEvidenceRef;
    }

    public String getPeptideModString() {
        return peptideModString;
    }
    
    public String getSequence(){
        return sequence;
    }

    public void setRetentionTime(double rt) {
        this.rt = rt;
    }

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

    /*
     * private method
     */
    private String createPeptideModString() {
        String modString = null;
        try {
            Peptide peptide = um.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.Peptide.class, pepRef);
            String pepSeq = peptide.getPeptideSequence();
            this.setSequence(pepSeq);
            List<Modification> mods = peptide.getModification();
            modString = "_";
            for (Modification mod : mods) {
                //modString = modString + mod.getLocation().toString() + "_";
                List<CvParam> cps = mod.getCvParam();
                for (CvParam cp : cps) {
                    modString = modString + cp.getAccession() + "_" + cp.getName() + "_";
                }
            }
            modString = pepSeq + modString;
            return modString;
        }
        catch (JAXBException ex) {
            Logger.getLogger(SIIData.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Couldn't get peptide object: " + pepRef + " -- " + ex.getMessage());
            return modString;
        }

    }

    /**
     * @return the mzidFn
     */
    public String getMzidFn() {
        return mzidFn;
    }

    /**
     * @param mzidFn the mzidFn to set
     */
    public void setMzidFn(String mzidFn) {
        this.mzidFn = mzidFn;
    }

    /**
     * @param peptideModString the peptideModString to set
     */
//    public void setPeptideModString(String peptideModString) {
//        this.peptideModString = peptideModString;
//    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

}
