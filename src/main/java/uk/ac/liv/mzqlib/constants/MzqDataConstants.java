package uk.ac.liv.mzqlib.constants;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 30-Oct-2014 13:49:46
 */
public class MzqDataConstants {

    public final static String PROTEIN_GROUP_LIST_TYPE = "ProteinGroupList";
    public final static String PROTEIN_LIST_TYPE = "ProteinList";
    public final static String PEPTIDE_LIST_TYPE = "PeptideConsensusList";
    public final static String FEATURE_LIST_TYPE = "FeatureList";
    public final static String SMALL_MOLECULE_LIST_TYPE = "SmallMoleculeList";

    public static final String CvIDPSIMS = "PSI-MS";
    public static final String CvNamePSIMS = "Proteomics Standards Initiative Mass Spectrometry Vocabularies";
    public static final String CvUriPSIMS = "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/"
            + "mzML/controlledVocabulary/psi-ms.obo";
    public static final String CvVerPSIMS = "3.61.0";
    
    //CV terms for different techniques
    public static final String LABEL_FREE = "LC-MS label-free quantitation analysis";
    public static final String LABEL_FREE_ACCESSION = "MS:1001834";
    public static final String MS1 = "MS1 label-based analysis";
    public static final String MS1_ACCESSION = "MS:1002018";
    public static final String MS2 = "MS2 tag-based analysis";
    public static final String MS2_ACCESSION = "MS:1002023";
    public static final String SPECTRAL_COUNTING = "spectral counting quantitation analysis";
    public static final String SPECTRAL_COUNTING_ACCESSION = "MS:1001836";
    public static final String SRM = "SRM quantitation analysis";
    public static final String SRM_ACCESSION = "MS:1001838";
}
