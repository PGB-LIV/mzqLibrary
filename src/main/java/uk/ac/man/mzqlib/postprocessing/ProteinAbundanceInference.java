package uk.ac.man.mzqlib.postprocessing;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import uk.ac.liv.pgb.jmzqml.MzQuantMLElement;
import uk.ac.liv.pgb.jmzqml.model.mzqml.MzQuantML;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroupList;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 * This code is created for calculating protein abundance inference
 *
 * @author man-mqbsshz2
 * @version 0.2
 */
public final class ProteinAbundanceInference {
    static final String    PATH               = "./src/main/resources/";
    static final String    PROTEIN_GROUP_LIST = "ProteinGroupList11";
    static final String    SEARCH_DATABASE    = "SD1";
    static final String    CV_PARAM_ID        = "PSI-MS";
    static final String    CV_REF             = "PSI-MS";
    private static boolean conflictPeptideExcluded;
    private String         inFile;

    /**
     * constructor
     *
     * @param inFile                       - input file
     * @param abundanceOperation            - calculation operator
     * @param inputDataTypeAccession        - input datatype accession
     * @param outputProteinGroupDTAccession - output protein group datatype
     *                                      accession
     * @param outputProteinGroupDTName      - output protein group datatype
     *                                      name
     *                                      datatype accession
     * @param quantLayerType                - quant layer type
     *
     * @throws FileNotFoundException file not found exceptions.
     */
    public ProteinAbundanceInference(final String inFile, final String abundanceOperation,
                                     final String inputDataTypeAccession, final String outputProteinGroupDTAccession,
                                     final String outputProteinGroupDTName, final String quantLayerType)
            throws FileNotFoundException {
        String cvAccessionPrefix          = "MS:";
        int    cvAccssionLength           = 10;
        int    cvAccessionLastSevenNumMax = 1002437;
        int    length1                    = inputDataTypeAccession.length();
        int    length2                    = outputProteinGroupDTAccession.length();
        String inputCvAccessionSuffix     = inputDataTypeAccession.substring(3, inputDataTypeAccession.length() - 3);
        String outputCvAccessionSuffix    =
            outputProteinGroupDTAccession.substring(3, outputProteinGroupDTAccession.length() - 3);
        String qlt1 = "AssayQuantLayer";
        String qlt2 = "RatioQuantLayer";
        String qlt3 = "StudyVariableQuantLayer";
        String co1  = "sum";
        String co2  = "mean";
        String co3  = "median";

        if (!(length1 == cvAccssionLength)) {
            throw new IllegalArgumentException("Invalid Input Peptide Datatype Parameter!!! " + inputDataTypeAccession);
        }

        if (!(inputDataTypeAccession.substring(0, 3).equals(cvAccessionPrefix))
                || !(Integer.parseInt(inputCvAccessionSuffix) >= 0)
                || !(Integer.parseInt(inputCvAccessionSuffix) <= cvAccessionLastSevenNumMax)) {
            throw new IllegalArgumentException("Wrong Input Peptide Datatype CV Accession!!! "
                                               + inputDataTypeAccession);
        }

        if (!(length2 == cvAccssionLength)) {
            throw new IllegalArgumentException("Invalid Output Protein Group CV Accession!!! "
                                               + outputProteinGroupDTAccession);
        }

        if (!outputProteinGroupDTAccession.substring(0, 3).equals(cvAccessionPrefix)
                && Integer.parseInt(outputCvAccessionSuffix) >= 0
                && Integer.parseInt(outputCvAccessionSuffix) <= cvAccessionLastSevenNumMax) {
            throw new IllegalArgumentException("Wrong Output Protein Group CV Accession!!! "
                                               + outputProteinGroupDTAccession);
        }

        if (!textHasContext(outputProteinGroupDTName)) {
            throw new IllegalArgumentException("Invalid Output Protein Group CV Name!!! " + outputProteinGroupDTName);
        }

//      if (!textHasContext(QuantLayerType)) {
//          throw new IllegalArgumentException("Invalid Quant Layer Type!!!");
//      }
        if (!(quantLayerType.equals(qlt1) || quantLayerType.equals(qlt2) || quantLayerType.equals(qlt3))) {
            throw new IllegalArgumentException("Invalid Quant Layer Type!!! " + quantLayerType);
        }

        if (!(abundanceOperation.equals(co1) || abundanceOperation.equals(co2) || abundanceOperation.equals(co3))) {
            throw new IllegalArgumentException("Method slected is not correct: " + abundanceOperation);
        }

        this.inFile = inFile;
    }

    /**
     * constructor with the option for skipping the conflicting peptides.
     *
     * @param inFile                       - input file
     * @param abundanceOperation            - calculation operator
     * @param inputDataTypeAccession        - input datatype accession
     * @param outputProteinGroupDTAccession - output protein group datatype
     *                                      accession
     * @param outputProteinGroupDTName      - output protein group datatype
     *                                      name
     *                                      datatype accession
     * @param quantLayerType                - quant layer type
     * @param conflictPeptideExcluded       - remove conflicting peptides
     *
     * @throws FileNotFoundException file not found exceptions.
     */
    public ProteinAbundanceInference(final String inFile, final String abundanceOperation,
                                     final String inputDataTypeAccession, final String outputProteinGroupDTAccession,
                                     final String outputProteinGroupDTName, final String quantLayerType,
                                     final boolean conflictPeptideExcluded)
            throws FileNotFoundException {
        String cvAccessionPrefix          = "MS:";
        int    cvAccssionLength           = 10;
        int    cvAccessionLastSevenNumMax = 1002437;
        int    length1                    = inputDataTypeAccession.length();
        int    length2                    = outputProteinGroupDTAccession.length();
        String inputCvAccessionSuffix     = inputDataTypeAccession.substring(3, inputDataTypeAccession.length() - 3);
        String outputCvAccessionSuffix    =
            outputProteinGroupDTAccession.substring(3, outputProteinGroupDTAccession.length() - 3);
        String qlt1 = "AssayQuantLayer";
        String qlt2 = "RatioQuantLayer";
        String qlt3 = "StudyVariableQuantLayer";
        String co1  = "sum";
        String co2  = "mean";
        String co3  = "median";

        if (!(length1 == cvAccssionLength)) {
            throw new IllegalArgumentException("Invalid Input Peptide Datatype Parameter!!! " + inputDataTypeAccession);
        }

        if (!(inputDataTypeAccession.substring(0, 3).equals(cvAccessionPrefix))
                || !(Integer.parseInt(inputCvAccessionSuffix) >= 0)
                || !(Integer.parseInt(inputCvAccessionSuffix) <= cvAccessionLastSevenNumMax)) {
            throw new IllegalArgumentException("Wrong Input Peptide Datatype CV Accession!!! "
                                               + inputDataTypeAccession);
        }

        if (!(length2 == cvAccssionLength)) {
            throw new IllegalArgumentException("Invalid Output Protein Group CV Accession!!! "
                                               + outputProteinGroupDTAccession);
        }

        if (!outputProteinGroupDTAccession.substring(0, 3).equals(cvAccessionPrefix)
                && Integer.parseInt(outputCvAccessionSuffix) >= 0
                && Integer.parseInt(outputCvAccessionSuffix) <= cvAccessionLastSevenNumMax) {
            throw new IllegalArgumentException("Wrong Output Protein Group CV Accession!!! "
                                               + outputProteinGroupDTAccession);
        }

        if (!textHasContext(outputProteinGroupDTName)) {
            throw new IllegalArgumentException("Invalid Output Protein Group CV Name!!! " + outputProteinGroupDTName);
        }

//      if (!textHasContext(QuantLayerType)) {
//          throw new IllegalArgumentException("Invalid Quant Layer Type!!!");
//      }
        if (!(quantLayerType.equals(qlt1) || quantLayerType.equals(qlt2) || quantLayerType.equals(qlt3))) {
            throw new IllegalArgumentException("Invalid Quant Layer Type!!! " + quantLayerType);
        }

        if (!(abundanceOperation.equals(co1) || abundanceOperation.equals(co2) || abundanceOperation.equals(co3))) {
            throw new IllegalArgumentException("Method slected is not correct: " + abundanceOperation);
        }

        this.inFile                                       = inFile;
        ProteinAbundanceInference.conflictPeptideExcluded = conflictPeptideExcluded;
    }

    /**
     * examine if there is a protein group list. If existing, remove it.
     *
     * @param um - unmarshalled mzq file
     */
    private void checkProteinGroupList(final MzQuantMLUnmarshaller um) {
        MzQuantML        mzq        = um.unmarshal(MzQuantMLElement.MzQuantML);
        ProteinGroupList protGrList = um.unmarshal(MzQuantMLElement.ProteinGroupList);

        if (protGrList != null) {
            mzq.setProteinGroupList(null);

            MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller(inFile);

            marshaller.marshall(mzq);
        }
    }

    /**
     * main method
     *
     * @param args - parameters for command line
     *
     * @throws JAXBException          jaxb exceptions.
     * @throws InstantiationException instantiation exceptions.
     * @throws IllegalAccessException illegal access exceptions.
     * @throws IllegalStateException  illegal state exceptions.
     * @throws FileNotFoundException  file not found exception.
     */
    public static void main(final String[] args)
            throws JAXBException, InstantiationException, IllegalAccessException, IllegalStateException,
                   FileNotFoundException {
        String quantLT = "AssayQuantLayer";

//      String refNo = "RefAssay2";
//      String filter = "qvalue001";
//      String infile = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\IdenOutcomes\\Filtered_AJ_" + filter
//              + "\\mzq\\consensusonly\\test\\unlabeled_result_FLUQT_mapped_" + filter
//              + "_peptideNormalization_medianSelectedRefAssay_25Oct.mzq";
//
//      String outfile = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\Idenoutcomes\\Filtered_AJ_" + filter
//              + "\\mzq\\consensusonly\\test\\unlabeled_result_FLUQT_mapped_" + filter
//              + "_peptideNormalized" + refNo + "_protein_inference_revDecoyTest_25Oct_samesetOrdered.mzq";
//      String infile = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\"
//              + "IdenOutcomes\\CPTAC_EvsB\\qvalue001\\mzq\\"
//              + "unlabeled_result_FLUQT_mapped_normalised_peptide_speciesNormalised.mzq";
//              + "unlabeled_result_FLUQT_mapped_normalised_peptide.mzq";
//              + "unlabeled_result_FLUQT_mapped_normalised_simon.mzq";
//              + "unlabeled_result_FLUQT_mapped_speciesNormalised_peptide_1_0.mzq";
        String infile = "C:\\Manchester\\result\\CPTAC_Study6_Progenesis_result\\study6\\CPTAC_study6\\"
                        + "data\\CPTAC_EvsB_unlabeled_result_FLUQT__mapped_v0.3.5_peptide_normalised.mzq";

//      String outfile = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\"
//              + "IdenOutcomes\\CPTAC_EvsB\\qvalue001\\mzq\\"
//              + "unlabeled_result_FLUQT_mapped_normalised_peptide_speciesNormalised_proteinInference.mzq";
//              + "unlabeled_result_FLUQT_mapped_normalised_peptide_proteinInference.mzq";
//              + "unlabeled_result_FLUQT_mapped_normalised_simon_proteinInference.mzq";
//              + "unlabeled_result_FLUQT_mapped_speciesNormalised_peptide_proteinInference_1_0.mzq";
        String  operator                      = "sum";    // "median", "mean"
        String  inputPeptideDTCA              = null;
        String  outputProteinGCA              = null;
        String  outputProteinGCN              = null;
        boolean signalConflictPeptideExcluded = true;     // conflicting peptides excluded

        inputPeptideDTCA = "MS:1001891";    // Progenesis:peptide normalised abundance
        outputProteinGCA = "MS:1001890";    // Progenesis:protein normalised abundance
        outputProteinGCN = "Progenesis: protein normalised abundance";

        // simon data
//      inputPeptideDTCA = "MS:1001850"; //Progenesis:peptide normalised abundance
//      inputRawPeptideDTCA = "MS:1001840";
//      outputProteinGCA = "MS:1002518"; //Progenesis:protein normalised abundance
//      outputProteinGCN = "Progenesis: protein normalised abundance";
//      outputRawProteinGCA = "MS:1002519";
//      outputRawProteinGCN = "Progenesis: protein raw abundance";
        if (args.length != 8 && args.length != 0) {
            System.out.println("Please input all eight parameters in order: input file, "
                               + "output file, quant layer type, input normalised peptide datatype CV accession,"
                               + "input raw peptide datatype CV accession, output protein group CV accession,"
                               + "output protein group CV name, output raw protein group CV accession, "
                               + "output raw protein group CV name" + "operator.");
        } else if (args.length == 8) {
            infile                        = args[0];
            operator                      = args[2];
            inputPeptideDTCA              = args[3];
            outputProteinGCA              = args[4];
            outputProteinGCN              = args[5];
            quantLT                       = args[6];
            signalConflictPeptideExcluded = Integer.parseInt(args[7]) != 0;
        }

        if (signalConflictPeptideExcluded) {
            ProteinAbundanceInference pai = new ProteinAbundanceInference(infile,
                                                                          operator,
                                                                          inputPeptideDTCA,
                                                                          outputProteinGCA,
                                                                          outputProteinGCN,
                                                                          quantLT,
                                                                          signalConflictPeptideExcluded);

            pai.proteinInference(conflictPeptideExcluded);
        } else {
            ProteinAbundanceInference pai = new ProteinAbundanceInference(infile,
                                                                          operator,
                                                                          inputPeptideDTCA,
                                                                          outputProteinGCA,
                                                                          outputProteinGCN,
                                                                          quantLT);

            pai.proteinInference();
        }
    }

    /**
     * unmarshal mzq to java object
     *
     * @param infile - mzq file
     *
     * @return - unmarshalled mzq Java object file
     *
     * @throws IllegalStateException illegal state exceptions.
     * @throws FileNotFoundException file not found exceptions.
     */
    public static MzQuantMLUnmarshaller mzqFileInput(final String infile)
            throws IllegalStateException, FileNotFoundException {
        File                  mzqFile  = new File(infile);
        MzQuantMLUnmarshaller infileUm = new MzQuantMLUnmarshaller(mzqFile);

        return infileUm;
    }

    /**
     * protein inference method
     *
     * @throws FileNotFoundException file not found exceptions.
     */
    public void proteinInference() throws FileNotFoundException {
        proteinInference(true);
    }

    /**
     * protein inference method with the parameter of conflicting signal
     *
     * @param signalConflict: with/without conflicting peptides
     *
     * @throws FileNotFoundException file not found exceptions.
     */
    public void proteinInference(final boolean signalConflict) throws FileNotFoundException {
        MzQuantMLUnmarshaller infileUm;

        try {
            infileUm = mzqFileInput(inFile);

            // remove the previous protein group list if existing
            checkProteinGroupList(infileUm);

//          pipeline_flag = pipeline_executor(infile_um, signalConflict);  // This function needs to be revisited as it doesn't do anything
        } catch (IllegalStateException ex) {
            System.out.println("****************************************************");
            System.out.println("The mzq file is not found!!! Please check the input.");
            System.err.println(ex);
            System.out.println("****************************************************");
        }

        System.out.println("****************************************************");
        System.out.println("******************** The pipeline does work successfully! *********************");

        if (signalConflict) {
            System.out.println("**** The protein abundance is calculated by removing conflicting pepConsensuses! ****");
        }
    }

    /**
     * String examination
     *
     * @param aText - a character string
     *
     * @return - true/false
     */
    private boolean textHasContext(final String aText) {
        String emptyString = "";

        return aText != null && !aText.trim().equals(emptyString);
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
