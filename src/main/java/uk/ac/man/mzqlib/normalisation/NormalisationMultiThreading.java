/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.man.mzqlib.normalisation;

import java.io.FileNotFoundException;

//import static uk.ac.man.mzqlib.normalisation.PepProtAbundanceNormalisation;
/**
 *
 * @author man-mqbsshz2
 */
public class NormalisationMultiThreading {

    final static String path = "C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\Idenoutcomes\\Filtered_AJ_";
    final static String filter = "qvalue001";
//    final static String quantLT = "AssayQuantLayer";
    final static String fileName = "unlabeled_result_FLUQT_mapped_" + filter;

    public static void main(String[] args) throws FileNotFoundException {

        String inputFile = path + filter + "\\mzq\\" + fileName + ".mzq";;
        String outputFile = path + filter + "\\mzq\\consensusonly\\test\\" + fileName
                + "_peptideNormalization_medianSelectedRefAssay_25Oct.mzq";

        String processingLevel = "peptide"; //"feature"
        String quantLayerType = null;
        String inDataTypeAccession = null;
        String outDataTypeAccession = null;
        String decoyTag = null;

        if (processingLevel.equalsIgnoreCase("peptide")) {
            quantLayerType = "AssayQuantLayer";
            inDataTypeAccession = "MS:1001840"; //LC-MS feature intensity
            outDataTypeAccession = "MS:1001891"; //Progenesis:peptide normalised abundance
            decoyTag = "XXX_";

        } else if (processingLevel.equalsIgnoreCase("feature")) {
            quantLayerType = "AssayQuantLayer";
            inDataTypeAccession = "MS:1001840"; //LC-MS feature intensity
            outDataTypeAccession = "MS:1001891"; //Progenesis:peptide normalised abundance
            decoyTag = "XXX_";
        }

        if (args.length != 7 && args.length != 0) {
            System.out.println("Please input all seven parameters in order: "
                    + "input file, output file, quant layer type, input datatype CV accession,"
                    + "output datatype CV accession, processing level, prefix of decoy regex.");
        } else if (args.length == 7) {
            inputFile = args[0];
            outputFile = args[1];
            processingLevel = args[2];
            quantLayerType = args[3];
            inDataTypeAccession = args[4];
            outDataTypeAccession = args[5];
            decoyTag = args[6];
        }


            PepProtAbundanceNormalisation workTask = new PepProtAbundanceNormalisation(inputFile,
                    outputFile, processingLevel, quantLayerType, inDataTypeAccession, outDataTypeAccession, decoyTag);
            workTask.multithreadingCalc();
    }
}
