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
import uk.ac.liv.mzqlib.maxquant.converter.MaxquantMzquantmlConvertor;
import uk.ac.liv.mzqlib.progenesis.converter.ProgenMzquantmlConvertor;
import uk.ac.liv.mzqlib.stats.MzqQLAnova;

import uk.ac.liv.mzqlib.util.Utils;
import uk.ac.liv.mzqlib.utils.Gzipper;
import uk.ac.man.mzqlib.normalisation.PepProtAbundanceNormalisation;
import uk.ac.man.mzqlib.postprocessing.ProteinAbundanceInference;

/**
 * @author Da Qi, adapted from MzIdentMLLib.java
 * @author Fawaz Ghali, University of Liverpool, 2011
 */
public class MzQuantMLLib {

    //CSV converter
    public static String csvConvertorParams = "-compress true|false";
    public static String csvConvertorUsageExample = "-compress false";
    public static String csvConvertorToolDescription = "This tool converts an mzQuantML file to a CSV file.";
    public static String csvConvertorUsage = "CsvConvertor input.mzq output.csv " + csvConvertorParams + " \n\nDescription:\n" + csvConvertorToolDescription;

    //XLS converter
    public static String xlsConvertorParams = "-compress true|false";
    public static String xlsConvertorUsageExample = "-compress false";
    public static String xlsConvertorToolDescription = "This tool converts an mzQuantML file to a XLS file.";
    public static String xlsConvertorUsage = "XlsConvertor input.mzq output.csv " + xlsConvertorParams + " \n\nDescription:\n" + xlsConvertorToolDescription;

    //HTML converter
    public static String htmlConvertorParams = "-compress true|false";
    public static String htmlConvertorUsageExample = "-compress false";
    public static String htmlConvertorToolDescription = "This tool converts an mzQuantML file to a HTML file.";
    public static String htmlConvertorUsage = "HtmlConvertor input.mzq output.html " + htmlConvertorParams + " \n\nDescription:\n" + htmlConvertorToolDescription;

    //mzTab converter
    public static String mzTabConvertorParams = "-compress true|false";
    public static String mzTabConvertorUsageExample = "-compress false";
    public static String mzTabConvertorToolDescription = "This tool converts an mzQuantML file to an mzTab file.";
    public static String mzTabConvertorUsage = "MzTabConvertor input.mzq output.mztab " + mzTabConvertorParams
            + " \n\nDescription:\n" + mzTabConvertorToolDescription;

    //ConsensusXML converter
    public static String consensusXMLConvertorParams = "-compress true|false";
    public static String consensusXMLConvertorUsageExample = "-compress false";
    public static String consensusXMLConvertorToolDescription = "This tool converts a consensusXML file from openMS to an mzQuantML file.";
    public static String consensusXMLConvertorUsage = "ConsensusXMLConvertor input.consensusXML output.mzq " + consensusXMLConvertorParams
            + " \n\nDescription:\n" + consensusXMLConvertorToolDescription;

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

    public static String normalisationParams = "-normLvl [Peptide|Feature] -qlType qlType -inDTCA inDTCA -outDTCA outDTCA -outDTCN outDTCN -tagDecoy tagDecoy [-compress true|false]";
    public static String normalisationUsageExample = "-normLvl peptide -qlType AssayQuantLayer -inDTCA MS:1001840 -outDTCA MS:1001891 -outDTCN Progenesis:peptide normalised abundance -tagDecoy XXX_";
    public static String normalisationToolDescription = "This tool will calculate normalised value of specified AssayQuantLayer in one specified list (peptide or feature). "
            + "Then output the result as an mzq file. "
            + "The tool will automatically select the best reference assay.";
    public static String normalisationUsage = "Normalisation input.mzq output.mzq " + normalisationParams
            + " \n\nDescription:\n" + normalisationToolDescription;

    public static String proteinInferenceParams = "-op [sum|mean|median] -inPepNormCA inPeptideNormCvAccession -inPepRawCA inPeptideRawCvAccession "
            + "-outPGNormCA outProteinGroupNormCvAccession -outPGNormCN outProteinGroupNormCvName "
            + "-outPGRawCA outProteinGroupRawCvAccession -outPGRawCN outProteinGroupRawCvName -qlType QuantLayerType";
    public static String proteinInferenceUsageExample = "-op sum -inPepNormCA MS:1001891 -inPepRawCA MS:1001893 "
            + "-outPGNormCA MS:1002518 -outPGNormCN Progenesis:protein group normalised abundance "
            + "-outPGRawCA MS:1002519 -outPGRawCN Progenesis:protein group raw abundance -qlType AssayQuantLayer";
    public static String proteinInferenceToolDescription = "This tool will perform protein inference and calculate the abundance from specified quantlayer.";
    public static String proteinInferenceUsage = "ProteinInference input.mzq output.mzq " + proteinInferenceParams
            + " \n\nDescription:\n" + proteinInferenceToolDescription;

    public static String maxquantConvertorParams = "-summary summaryFile -peptides peptidesFile -proteinGroups proteinGroupsFile "
            + "-template experimentalDesignTemplateFile";
    public static String maxquantConvertorUsageExample = "-summary summary.txt -peptides peptides.txt -proteinGroups proteinGroups.txt "
            + "-template ExperimentalDesignTemplate.txt";
    public static String maxquantConvertorToolDescription = "This tool will convert Maxquant result files to a single mzQuantML file. "
            + "User needs to select the \"evidence.txt\" in Maxquant output folder as the input file. Provide the other mandatory files via options. ";
    public static String maxquantConvertorUsage = "MaxquantConvertor evidence.txt output.mzq " + maxquantConvertorParams
            + " \n\nDescription:\n" + maxquantConvertorToolDescription;

    public static String progenesisConvertorParams = "-pepList peptideListFile [-sep CsvSeparator -proteinGroupList [true|fasle] -rawPlusNorm [norm|raw]]";
    public static String progenesisConvertorUsageExample = "-pepList peptide_list.csv -proteinGroupList false -rawPlusNorm raw";
    public static String progenesisConvertorToolDescription = "This tool will convert Progenesis result files to a single mzQuantML file. "
            + "User needs to provide protein list CSV file as the input file and provide peptide list (or feature list) CSV file via option. ";
    public static String progenesisConvertorUsage = "ProgenesisConvertor proteinList.csv output.mzq " + progenesisConvertorParams
            + " \n\nDescription:\n" + progenesisConvertorToolDescription;

    public static String userFeedback = "java -jar jar-location/mzqlib-version.jar ";

// Added by Fawaz Ghali to automatically update the MzidLib GUI 
    private Map<String, String> allFunctions;

    /**
     * Init all functions hashmap
     */
    public MzQuantMLLib() {
        allFunctions = new HashMap<>();
        allFunctions.put("CsvConvertor", csvConvertorParams + ";@;" + csvConvertorUsage + ";@;" + csvConvertorUsageExample + ";@;" + csvConvertorToolDescription);
        allFunctions.put("XlsConvertor", xlsConvertorParams + ";@;" + xlsConvertorUsage + ";@;" + xlsConvertorUsageExample + ";@;" + xlsConvertorToolDescription);
        allFunctions.put("HtmlConvertor", htmlConvertorParams + ";@;" + htmlConvertorUsage + ";@;" + htmlConvertorUsageExample + ";@;" + htmlConvertorToolDescription);
        allFunctions.put("MzTabConvertor", mzTabConvertorParams + ";@;" + mzTabConvertorUsage + ";@;" + mzTabConvertorUsageExample + ";@;" + mzTabConvertorToolDescription);
        allFunctions.put("ConsensusXMLConvertor", consensusXMLConvertorParams + ";@;" + consensusXMLConvertorUsage + ";@;" + consensusXMLConvertorUsageExample + ";@;" + consensusXMLConvertorToolDescription);
        allFunctions.put("MzqMzIdMapping", idMappingParams + ";@;" + idMappingUsage + ";@;" + idMappingUsageExample + ";@;" + idMappingToolDescription);
        allFunctions.put("AnovaPValue", anovaParams + ";@;" + anovaUsage + ";@;" + anovaUsageExample + ";@;" + anovaToolDescription);
        allFunctions.put("Normalisation", normalisationParams + ";@;" + normalisationUsage + ";@;" + normalisationUsageExample + ";@;" + normalisationToolDescription);
        allFunctions.put("ProteinInference", proteinInferenceParams + ";@;" + proteinInferenceUsage + ";@;" + proteinInferenceUsageExample + ";@;" + proteinInferenceToolDescription);
        allFunctions.put("MaxquantConvertor", maxquantConvertorParams + ";@;" + maxquantConvertorUsage + ";@;" + maxquantConvertorUsageExample + ";@;" + maxquantConvertorToolDescription);
        allFunctions.put("ProgenesisConvertor", progenesisConvertorParams + ";@;" + progenesisConvertorUsage + ";@;" + progenesisConvertorUsageExample + ";@;" + progenesisConvertorToolDescription);
    }

    /**
     * Getter for all functions to be used in MzqLib GUI
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
                else if (args[0].equals("XlsConvertor")) {
                    if (inputFileName != null && outputFileName != null) {
                        MzqLib mzqlib = new MzqLib("xls", inputFileName, outputFileName);
                    }
                    else {
                        guiFeedback = "Error, usage:  " + xlsConvertorUsage;
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
                else if (args[0].equals("AnovaPValue")) {
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
                        String outDTCN = Utils.getCmdParameter(args, "outDTCN", true);
                        String tagDecoy = Utils.getCmdParameter(args, "tagDecoy", true);

                        PepProtAbundanceNormalisation normalisation = new PepProtAbundanceNormalisation(inputFileName, outputFileName, normLevel, qlType, inDTCA, outDTCA, outDTCN, tagDecoy, null);
                        normalisation.multithreadingCalc();
                    }
                }
                else if (args[0].equals("ProteinInference")) {
                    if (inputFileName != null && outputFileName != null) {
                        String op = Utils.getCmdParameter(args, "op", true);
                        String inPepNormCA = Utils.getCmdParameter(args, "inPepNormCA", true);
                        String inPepRawCA = Utils.getCmdParameter(args, "inPepRawCA", true);
                        String outPGNormCA = Utils.getCmdParameter(args, "outPGNormCA", true);
                        String outPGNormCN = Utils.getCmdParameter(args, "outPGNormCN", true);
                        String outPGRawCA = Utils.getCmdParameter(args, "outPGRawCA", true);
                        String outPGRawCN = Utils.getCmdParameter(args, "outPGRawCN", true);
                        String qlType = Utils.getCmdParameter(args, "qlType", true);

                        ProteinAbundanceInference pai = new ProteinAbundanceInference(inputFileName, outputFileName, op, inPepNormCA, inPepRawCA,
                                                                                      outPGNormCA, outPGNormCN, outPGRawCA, outPGRawCN, qlType);
                        pai.proteinInference();
                    }
                }
                else if (args[0].equals("MaxquantConvertor")) {
                    if (inputFileName != null && outputFileName != null) {
                        String summaryFn = Utils.getCmdParameter(args, "summary", true);
                        String peptidesFn = Utils.getCmdParameter(args, "peptides", true);
                        String proteinGroupsFn = Utils.getCmdParameter(args, "proteinGroups", true);
                        String templateFn = Utils.getCmdParameter(args, "template", true);
                        MaxquantMzquantmlConvertor maxCon = new MaxquantMzquantmlConvertor(inputFileName, peptidesFn, proteinGroupsFn, templateFn, summaryFn);
                        maxCon.convert(outputFileName);
                    }
                }
                else if (args[0].equals("ProgenesisConvertor")) {
                    if (inputFileName != null && outputFileName != null) {
                        String pepList = Utils.getCmdParameter(args, "pepList", true);
                        String sep = Utils.getCmdParameter(args, "sep", false);
                        String pgl = Utils.getCmdParameter(args, "proteinGroupList", false);
                        String rawPlusNorm = Utils.getCmdParameter(args, "rawPlusNorm", false);

                        if (sep != null && sep.length() == 1) {
                            char[] sepCharArray = sep.toCharArray();
                            char seperator = sepCharArray[0];
                            ProgenMzquantmlConvertor progenConv = new ProgenMzquantmlConvertor(pepList, inputFileName, "", seperator);

                            if (pgl != null) {
                                boolean pglBoolean = Boolean.valueOf(pgl);
                                progenConv.convert(outputFileName, pglBoolean, rawPlusNorm);
                            }
                            else {
                                progenConv.convert(outputFileName, true, rawPlusNorm);
                            }
                        }
                        else if (sep != null && sep.length() != 1) {
                            throw new RuntimeException("The input sepearator must be a single character.");
                        }
                        else {
                            ProgenMzquantmlConvertor progenConv = new ProgenMzquantmlConvertor(pepList, inputFileName, "", ',');

                            if (pgl != null) {
                                boolean pglBoolean = Boolean.valueOf(pgl);
                                progenConv.convert(outputFileName, pglBoolean, rawPlusNorm);
                            }
                            else {
                                progenConv.convert(outputFileName, true, rawPlusNorm);
                            }
                        }
                    }
                }
                else {
                    String tempFeedback = "Program within mzqLibrary not recognized: " + args[0] + "\n\nPlease use one of the following command:\n\n"
                            + progenesisConvertorUsage + "\n\n" + maxquantConvertorUsage + "\n\n" + consensusXMLConvertorUsage + "\n\n"
                            + csvConvertorUsage + "\n\n" + xlsConvertorUsage + "\n\n" + htmlConvertorUsage + "\n\n" + mzTabConvertorUsage + "\n\n"
                            + idMappingUsage + "\n\n" + normalisationUsage + "\n\n" + proteinInferenceUsage + "\n\n" + anovaUsage + "\n";

                    System.out.println(tempFeedback);
                    guiFeedback = "Error, usage: " + tempFeedback;
                    userFeedback = tempFeedback;
                }
                if (userFeedback.equals("")) {
                    userFeedback = "Completed successfully, output written to " + outputFileName;
                }
                //System.out.println(userFeedback);
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
                    + "\n" + "***************\n" + xlsConvertorUsage
                    + "\n" + "***************\n" + htmlConvertorUsage
                    + "\n" + "***************\n" + mzTabConvertorUsage
                    + "\n" + "***************\n" + progenesisConvertorUsage
                    + "\n" + "***************\n" + maxquantConvertorUsage
                    + "\n" + "***************\n" + consensusXMLConvertorUsage                    
                    + "\n" + "***************\n" + idMappingUsage
                    + "\n" + "***************\n" + normalisationUsage
                    + "\n" + "***************\n" + proteinInferenceUsage
                    + "\n" + "***************\n" + anovaUsage + "\n";

            System.out.println(tempFeedback);
            guiFeedback = tempFeedback;
        }

        return guiFeedback;

    }

}
