/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.man.mzqlib.normalisation;

import java.io.FileNotFoundException;

//import static uk.ac.man.mzqlib.normalisation.PepProtAbundanceNormalisation;
/**
 * Normalisation at the peptide or feature level. Users can choose a reference
 * assay, otherwise the code will automatically select a robust reference based
 * on a median criterion. The scaling factor is calculated using the identified
 * features, i.e. features with the identified sequences.
 *
 * @author man-mqbsshz2
 */
public class NormalisationMultiThreading {

    final static String path = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\Idenoutcomes\\Filtered_AJ_";
    final static String filter = "qvalue001";
    final static String fileName = "unlabeled_result_FLUQT_mapped_" + filter;

    public static void main(String[] args) throws FileNotFoundException {

        String inputFile = path + filter + "\\mzq\\" + fileName + ".mzq";;
        String outputFile = path + filter + "\\mzq\\consensusonly\\test\\" + fileName
                + "_peptideNormalization_medianSelectedRefAssay_25Oct_feature1.mzq";
//        String inputFile = "C:\\Manchester\\work\\ProteoSuite\\AndyPaper\\AllAges.mzq\\" + "AllAges.mzq";
//        String outputFile = "C:\\Manchester\\work\\ProteoSuite\\AndyPaper\\" + "AllAges_featureNormalised116.mzq";

        //iTraq for exception test
//        String inputFile = "C:\\Manchester\\work\\ProteoSuite\\mzq-lib\\example_files\\test_ksl_itraq1.mzq";
//        String outputFile = "C:\\Manchester\\work\\ProteoSuite\\mzq-lib\\example_files\\test_ksl_itraq_normalised.mzq";
        String processingLevel = "feature";//"peptide"; //
        String quantLayerType = null;
        String inDataTypeAccession = null;
        String outDataTypeAccession = null;
        String outDataTypeName = null;
        String decoyTag = null;
        String refNo = null;

        //the assay range user prefers to select
//        int assayMin = 1;
//        int assayMax = 1;
        if (processingLevel.equalsIgnoreCase("peptide")) {
            quantLayerType = "AssayQuantLayer";
//            puf3 data
//            inDataTypeAccession = "MS:1001840"; //LC-MS feature intensity
//            outDataTypeAccession = "MS:1001891"; //Progenesis:peptide normalised abundance
//            decoyTag = "XXX_";
            
//andy data in paper work
            inDataTypeAccession = "MS:1001893"; //Progenesis:peptide raw abundance
            outDataTypeAccession = "MS:1001891"; //Progenesis:peptide normalised abundance
            outDataTypeName = "normalised " + processingLevel + " abundance";
            decoyTag = "XXX_";
//            refNo = "1";

        } else if (processingLevel.equalsIgnoreCase("feature")) {
            quantLayerType = "AssayQuantLayer";
            inDataTypeAccession = "MS:1001840"; //LC-MS feature intensity
            outDataTypeAccession = "MS:1001891"; //Progenesis:peptide normalised abundance
            outDataTypeName = "normalised " + processingLevel + " abundance";
            decoyTag = "XXX_";
//            refNo = "1";
        }

        if ((args.length != 8 || args.length != 9) && args.length != 0) {
            System.out.println("Please input all seven parameters in order: "
                    + "input file, output file, processing level, quant layer type, "
                    + "input datatype CV accession, output datatype CV accession, "
                    + "output datatype CV name, prefix of decoy regex, reference number (optional).");
        } else if (args.length == 8) {
            inputFile = args[0];
            outputFile = args[1];
            processingLevel = args[2];
            quantLayerType = args[3];
            inDataTypeAccession = args[4];
            outDataTypeAccession = args[5];
            outDataTypeName = args[6];
            decoyTag = args[7];
        } else if (args.length == 9) {
            inputFile = args[0];
            outputFile = args[1];
            processingLevel = args[2];
            quantLayerType = args[3];
            inDataTypeAccession = args[4];
            outDataTypeAccession = args[5];
            outDataTypeName = args[6];
            decoyTag = args[7];
            refNo = args[8];
//            assayMin = args[7];
//            assayMax = args[8];
        }

//        PepProtAbundanceNormalisation workTask = new PepProtAbundanceNormalisation(inputFile,
//                outputFile, processingLevel, quantLayerType, inDataTypeAccession, outDataTypeAccession, 
//                decoyTag, assayMin, assayMax);
        PepProtAbundanceNormalisation workTask = new PepProtAbundanceNormalisation(inputFile,
                outputFile, processingLevel, quantLayerType, inDataTypeAccession, outDataTypeAccession,
                outDataTypeName, decoyTag, refNo);
        workTask.multithreadingCalc();
    }
}
