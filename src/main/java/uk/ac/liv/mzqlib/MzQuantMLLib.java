package uk.ac.liv.mzqlib;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;
import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.mzqlib.consensusxml.convertor.ConsensusXMLProcessor;
import uk.ac.liv.mzqlib.consensusxml.convertor.ConsensusXMLProcessorFactory;
import uk.ac.liv.mzqlib.idmapper.MzqMzIdMapper;
import uk.ac.liv.mzqlib.idmapper.MzqMzIdMapperFactory;
import uk.ac.liv.mzqlib.stats.MzqQLAnova;

import uk.ac.liv.mzqlib.util.Utils;
import uk.ac.liv.mzqlib.utils.Gzipper;
import uk.ac.man.mzqlib.normalisation.PepProtAbundanceNormalisation;

/**
 * @author Da Qi, adapted from MzIdentMLLib.java
 * @author Fawaz Ghali, University of Liverpool, 2011
 */
public class MzQuantMLLib {

    public static String csvConvertorParams = "-compress true|false";
    public static String csvConvertorUsageExample = "-compress false";
    public static String csvConvertorToolDescription = "";
    public static String csvConvertorUsage = "CsvConvertor input.mzq output.csv " + csvConvertorParams + " \n\nDescription:\n" + csvConvertorToolDescription;

    public static String htmlConvertorParams = "-compress true|false";
    public static String htmlConvertorUsageExample = "-compress false";
    public static String htmlConvertorToolDescription = "";
    public static String htmlConvertorUsage = "HtmlConvertor input.mzq output.html " + htmlConvertorParams + " \n\nDescription:\n" + htmlConvertorToolDescription;

    public static String consensusXMLConvertorParams = "-compress true|false";
    public static String consensusXMLConvertorUsageExample = "-compress false";
    public static String consensusXMLConvertorToolDescription = "";
    public static String consensusXMLConvertorUsage = "ConsensusXMLConvertor input.consensusXML output.mzq " + consensusXMLConvertorParams
            + " \n\nDescription:\n" + consensusXMLConvertorToolDescription;

    public static String mzTabConvertorParams = "-compress true|false";
    public static String mzTabConvertorUsageExample = "-compress false";
    public static String mzTabConvertorToolDescription = "";
    public static String mzTabConvertorUsage = "MzTabConvertor input.mzq output.mztab " + mzTabConvertorParams
            + " \n\nDescription:\n" + mzTabConvertorToolDescription;

    public static String idMappingParams = "-rawToMzidMap rawToMzidMap [-compress true|false]";
    //example: mam_042408o_CPTAC_study6_6B011.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_042408o_CPTAC_study6_6B011_rt.mzid;mam_050108o_CPTAC_study6_6B011.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_050108o_CPTAC_study6_6B011_rt.mzid;mam_050108o_CPTAC_study6_6B011_080504231912.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_050108o_CPTAC_study6_6B011_080504231912_rt.mzid;mam_042408o_CPTAC_study6_6C008.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_042408o_CPTAC_study6_6C008_rt.mzid;mam_050108o_CPTAC_study6_6C008.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_050108o_CPTAC_study6_6C008_rt.mzid;mam_050108o_CPTAC_study6_6C008_080505040419.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_050108o_CPTAC_study6_6C008_080505040419_rt.mzid;mam_042408o_CPTAC_study6_6D004.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_042408o_CPTAC_study6_6D004_rt.mzid;mam_050108o_CPTAC_study6_6D004.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_050108o_CPTAC_study6_6D004_rt.mzid;mam_050108o_CPTAC_study6_6D004_080505084927.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_050108o_CPTAC_study6_6D004_080505084927_rt.mzid;mam_042408o_CPTAC_study6_6E004.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_042408o_CPTAC_study6_6E004_rt.mzid;mam_050108o_CPTAC_study6_6E004.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_050108o_CPTAC_study6_6E004_rt.mzid;mam_050108o_CPTAC_study6_6E004_080505133441.raw;D:\Users\ddq\Documents\NetBeansProjects\mzq-lib\mam_050108o_CPTAC_study6_6E004_080505133441_rt.mzid
    public static String idMappingUsageExample = "-rawToMzidMap filename1.raw;C:\\Data\\Mzid\\filename1.mzid;filename2.raw;C:\\Data\\Mzid\\filename2.mzid -compress false";
    public static String idMappingToolDescription = "This tool will reassign consensus peptide sequence by mapping the identifications across input mzid files. "
            + "The -rawToMzidMap argument requires pairs of a raw file name (file name only) and its mzid file (with absolute file path), using semicolon as seperator. "
            + "The raw file names must be same as those in the input mzq file.";
    public static String idMappingUsage = "MzqMzIdMapping input.mzq output.mzq " + idMappingParams
            + " \n\nDescription:\n" + idMappingToolDescription;

    public static String anovaParams = "-listType [Protein|ProteinGroup] -qlDataType qlDataType -assayIdsGroup assayIdsGroup [-compress true|false]";
    public static String anovaUsageExample = "-listType ProteinGroup -qlDataType MS:1002518 -assayIdsGroup ass_0,ass_1,ass_2,ass_3,ass_4;ass_5,ass_6,ass_7,ass_8,ass_9";
    public static String anovaToolDescription = "This tool will calculate one-way ANOVA p value of speificied QuantLayer for either ProteinGroupList or ProteinList."
            + "The QuantLayer is specified by passing CV accession to qlDataType option. The group of assays included in the ANOVA calculation is proivded by the flat string option of assayIdsGroup."
            + "The whole string is divided into groups which are separated by \";\" (semicolon) and in each group, the member assay ids are separated by \",\" (comma).";
    public static String anovaUsage = "AnovaPValue input.mzq output.mzq " + anovaParams
            + " \n\nDescription:\n" + anovaToolDescription;

    public static String normalisationParams = "-normLvl [Peptide|Feature] -qlType qlType -inDTCA inDTCA -outDTCA outDTCA -tagDecoy tagDecoy [-compress true|false]";
    public static String normalisationUsageExample = "-normLvl peptide -qlType AssayQuantLayer -inDTCA MS:1001840 -outDTCA MS:1001891 -tagDecoy XXX_";
    public static String normalisationToolDescription = "This tool will calculate normalised value of specified AssayQuantLayer in one specified list (peptide or feature). "
            + "Then output the result as an mzq file. "
            + "The tool will automatically select the best reference assay.";
    public static String normalisationUsage = "Normalisation input.mzq output.mzq " + normalisationParams
            + " \n\nDescription:\n" + normalisationToolDescription;

    public static String proteinInferenceParams = "";
    public static String proteinInferenceUsageExample = "-normLvl peptide -qlType AssayQuantLayer -inDTCA MS:1001840 -outDTCA MS:1001891 -tagDecoy XXX_";
    public static String proteinInferenceToolDescription = "This tool will calculate normalised value of specified AssayQuantLayer in one specified list (peptide or feature). "
            + "Then output the result as an mzq file. "
            + "The tool will automatically select the best reference assay.";
    public static String proteinInferenceUsage = "Normalisation input.mzq output.mzq " + proteinInferenceParams
            + " \n\nDescription:\n" + proteinInferenceToolDescription;

    public static String userFeedback = "java -jar jar-location/mzqlib-version.jar ";

// Added by Fawaz Ghali to automatically update the MzidLib GUI 
    private Map<String, String> allFunctions;

    /**
     * Init all functions hashmap
     */
    public MzQuantMLLib() {
        allFunctions = new HashMap<>();
        allFunctions.put("CsvConvertor", csvConvertorParams + ";@;" + csvConvertorUsage + ";@;" + csvConvertorUsageExample);
        allFunctions.put("HtmlConvertor", htmlConvertorParams + ";@;" + htmlConvertorUsage + ";@;" + htmlConvertorUsageExample);
        allFunctions.put("ConsensusXMLConvertor", consensusXMLConvertorParams + ";@;" + consensusXMLConvertorUsage + ";@;" + consensusXMLConvertorUsageExample);
        allFunctions.put("MzTabConvertor", mzTabConvertorParams + ";@;" + mzTabConvertorUsage + ";@;" + mzTabConvertorUsageExample);
        allFunctions.put("MzqMzIdMapping", idMappingParams + ";@;" + idMappingUsage + ";@;" + idMappingUsageExample);
        allFunctions.put("MzqAnovaPValue", anovaParams + ";@;" + anovaUsage + ";@;" + anovaUsageExample);
        allFunctions.put("Normalisation", normalisationParams + ";@;" + normalisationUsage + ";@;" + normalisationUsageExample);
    }

    /**
     * Getter for all functions to be used in MzidLib GUI
     *
     * @return all functions as hashmap
     */
    public Map<String, String> getAllFunctions() {
        return allFunctions;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        MzQuantMLLib mzqLib = new MzQuantMLLib();
        try {

            mzqLib.init(args);

        }
        catch (Exception ex) {
        }
    }

    public String init(String[] args)
            throws Exception {
        String inputFileName = "";
        String outputFileName = "";

        String guiFeedback = "";     //0 for successful execution

        if (args != null && args.length > 1) {
            System.out.println("");
            System.out.println("About to run " + args[0]);
            System.out.println("Using the following parameters:");
            for (int i = 1; i < args.length; i++) {
                String string = args[i];
                System.out.print(string + " ");
            }
            System.out.println();
        }

        //TODO: changed from 3 to 2 by Da 16/09/2014
        if (args.length > 3) {

            inputFileName = args[1];
            outputFileName = args[2];
            // Added by FG uncompress
            boolean testDir = new File(inputFileName).isDirectory();
            if (inputFileName.equals(outputFileName) && !testDir) {
                guiFeedback = "Error: input and output file names are same";
            }
            else {
                boolean uncompress = false;
                File inputFile;
                if (inputFileName.endsWith(".gz")) {
                    inputFile = Gzipper.extractFile(new File(inputFileName));
                    uncompress = true;

                }
                else {
                    inputFile = new File(inputFileName);
                }
                // Added by FG compress file
                String compress = Utils.getCmdParameter(args, "compress", false);
                //set default to "false" if compress parameter is not set:
                if (compress == null) {
                    compress = "false";
                }
                if (Boolean.valueOf(compress)) {
                    if (outputFileName.endsWith(".gz")) {
                        outputFileName = outputFileName.substring(0, outputFileName.length() - 3);
                    }

                }
                // Added by FG check if path is folder

                if (args[0].equals("CsvConvertor")) {
                    if (inputFileName != null && outputFileName != null) {
                        MzqLib mzqlib = new MzqLib("csv", inputFileName, outputFileName);
                    }
                    else {
                        guiFeedback = "Error, usage: " + csvConvertorUsage;
                    }
                }
                else if (args[0].equals("HtmlConvertor")) {
                    if (inputFileName != null && outputFileName != null) {
                        MzqLib mzqlib = new MzqLib("html", inputFileName, outputFileName);
                    }
                    else {
                        guiFeedback = "Error, usage: " + htmlConvertorUsage;
                    }
                }
                else if (args[0].equals("ConsensusXMLConvertor")) {

                    try {
                        ConsensusXMLProcessor conProc = ConsensusXMLProcessorFactory.getInstance().buildConsensusXMLProcessor(new File(inputFileName));
                        conProc.convert(outputFileName);
                    }
                    catch (JAXBException | IOException ex) {
                        System.out.println("Error running ConsensusXMLConvertor: " + userFeedback + consensusXMLConvertorUsage);
                        guiFeedback = "Error running ConsensusXMLConvertor: " + consensusXMLConvertorUsage + "\n"
                                + ex.getMessage();

                    }
                }
                else if (args[0].equals("MzTabConvertor")) {
                    if (inputFileName != null && outputFileName != null) {
                        MzqLib mzqlib = new MzqLib("mztab", inputFileName, outputFileName);
                    }
                    else {
                        guiFeedback = "Error, usage: " + mzTabConvertorUsage;
                    }
                }
                else if (args[0].equals("MzqMzIdMapping")) {
                    if (inputFileName != null && outputFileName != null) {
                        String rawToMzidMapString = Utils.getCmdParameter(args, "rawToMzidMap", true);

                        MzQuantMLUnmarshaller mzqUm = new MzQuantMLUnmarshaller(new File(inputFileName));
                        MzqMzIdMapper mapper = MzqMzIdMapperFactory.getInstance().buildMzqMzIdMapper(mzqUm, rawToMzidMapString);
                        mapper.createMappedFile(outputFileName);
                    }
//                    }
                }
                else if (args[0].equals("MzqAnovaPValue")) {
                    if (inputFileName != null && outputFileName != null) {
                        String listType = Utils.getCmdParameter(args, "listType", true);
                        String qlDataType = Utils.getCmdParameter(args, "qlDataType", true);
                        String assayIdsGroupString = Utils.getCmdParameter(args, "assayIdsGroup", true);

                        MzqQLAnova mzqAnova = new MzqQLAnova(inputFileName, listType, assayIdsGroupString, qlDataType);
                        mzqAnova.writeMzQuantMLFile(outputFileName);
                    }
                }
                else if (args[0].equals("Normalisation")) {
                    if (inputFileName != null && outputFileName != null) {
                        String normLevel = Utils.getCmdParameter(args, "normLvl", true);
                        String qlType = Utils.getCmdParameter(args, "qlType", true);
                        String inDTCA = Utils.getCmdParameter(args, "inDTCA", true);
                        String outDTCA = Utils.getCmdParameter(args, "outDTCA", true);
                        String tagDecoy = Utils.getCmdParameter(args, "tagDecoy", true);
                        //String refAssay = Utils.getCmdParameter(args, "refAssay", false);                       

                        PepProtAbundanceNormalisation normalisation = new PepProtAbundanceNormalisation(inputFileName, outputFileName, normLevel, qlType, inDTCA, outDTCA, tagDecoy);
                        normalisation.multithreadingCalc();
                    }
                }
                else {
                    String tempFeedback = "Program within MzidLib not recognized: " + args[0] + csvConvertorUsage + "\n" + htmlConvertorUsage + "\n"
                            + consensusXMLConvertorUsage + "\n" + mzTabConvertorUsage + "\n" + idMappingUsage + "\n" + anovaUsage;

                    System.out.println(tempFeedback);
                    guiFeedback = "Error, usage: " + tempFeedback;
                    userFeedback = tempFeedback;
                }
                if (userFeedback.equals("")) {
                    userFeedback = "Completed successfully, output written to " + outputFileName;
                }
                System.out.println(userFeedback);
                // Added by FG delete tmp file
                if (uncompress) {
//                    Gzipper.deleteFile(inputFile);
                    inputFile.deleteOnExit();
                }

                if (Boolean.valueOf(compress)) {
                    Gzipper.compressFile(new File(outputFileName));
                }
            }

        }
        else {
            String tempFeedback = "Error insufficient arguments entered, options: " + userFeedback + " toolname options\n"
                    + "\n\nTools:\n\n***************\n"
                    + csvConvertorUsage
                    + "\n" + "***************\n" + htmlConvertorUsage
                    + "\n" + "***************\n" + consensusXMLConvertorUsage
                    + "\n" + "***************\n" + mzTabConvertorUsage
                    + "\n" + "***************\n" + idMappingUsage
                    + "\n" + "***************\n" + anovaUsage + "\n";

            System.out.println(tempFeedback);
            guiFeedback = tempFeedback;
        }

        return guiFeedback;

    }

}
