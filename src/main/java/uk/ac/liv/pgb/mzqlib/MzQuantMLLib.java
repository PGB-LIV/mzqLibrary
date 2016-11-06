
package uk.ac.liv.pgb.mzqlib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.pgb.mzqlib.openms.converter.ConsensusXMLProcessor;
import uk.ac.liv.pgb.mzqlib.openms.converter.ConsensusXMLProcessorFactory;
import uk.ac.liv.pgb.mzqlib.idmapper.MzqMzIdMapper;
import uk.ac.liv.pgb.mzqlib.idmapper.MzqMzIdMapperFactory;
import uk.ac.liv.pgb.mzqlib.maxquant.converter.MaxquantMzquantmlConverter;
import uk.ac.liv.pgb.mzqlib.progenesis.converter.ProgenMzquantmlConverter;
import uk.ac.liv.pgb.mzqlib.stats.MzqQLAnova;

import uk.ac.liv.pgb.mzqlib.utils.Utils;
import uk.ac.liv.pgb.mzqlib.utils.Gzipper;
import uk.ac.man.mzqlib.normalisation.PepProtAbundanceNormalisation;
import uk.ac.man.mzqlib.postprocessing.ProteinAbundanceInference;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;

/**
 * @author Da Qi, adapted from MzIdentMLLib.java
 * @author Fawaz Ghali, University of Liverpool, 2011
 */
public class MzQuantMLLib implements Serializable {

    private static final long serialVersionUID = 103L;

    //CSV converter
    /**
     * Constant.
     */
    public static final String csvConverterParams = "-compress true|false";

    /**
     * Constant.
     */
    public static final String csvConverterUsageExample = "-compress false";

    /**
     * Constant.
     */
    public static final String csvConverterToolDescription
            = "This tool converts an mzQuantML file to a CSV file.";

    /**
     * Constant.
     */
    public static final String csvConverterUsage
            = "CsvConverter input.mzq output.csv " + csvConverterParams
            + " \n\nDescription:\n" + csvConverterToolDescription;

    //XLS converter
    /**
     * Constant.
     */
    public static final String xlsConverterParams = "-compress true|false";

    /**
     * Constant.
     */
    public static final String xlsConverterUsageExample = "-compress false";

    /**
     * Constant.
     */
    public static final String xlsConverterToolDescription
            = "This tool converts an mzQuantML file to a XLS file.";

    /**
     * Constant.
     */
    public static final String xlsConverterUsage
            = "XlsConverter input.mzq output.csv " + xlsConverterParams
            + " \n\nDescription:\n" + xlsConverterToolDescription;

    //HTML converter
    /**
     * Constant.
     */
    public static final String htmlConverterParams = "-compress true|false";

    /**
     * Constant.
     */
    public static final String htmlConverterUsageExample = "-compress false";

    /**
     * Constant.
     */
    public static final String htmlConverterToolDescription
            = "This tool converts an mzQuantML file to a HTML file.";

    /**
     * Constant.
     */
    public static final String htmlConverterUsage
            = "HtmlConverter input.mzq output.html " + htmlConverterParams
            + " \n\nDescription:\n" + htmlConverterToolDescription;

    //mzTab converter
    /**
     * Constant.
     */
    public static final String mzTabConverterParams = "-compress true|false";

    /**
     * Constant.
     */
    public static final String mzTabConverterUsageExample = "-compress false";

    /**
     * Constant.
     */
    public static final String mzTabConverterToolDescription
            = "This tool converts an mzQuantML file to an mzTab file.";

    /**
     * Constant.
     */
    public static final String mzTabConverterUsage
            = "MzTabConverter input.mzq output.mztab " + mzTabConverterParams
            + " \n\nDescription:\n" + mzTabConverterToolDescription;

    //ConsensusXML converter
    /**
     * Constant.
     */
    public static final String consensusXMLConverterParams
            = "-compress true|false";

    /**
     * Constant.
     */
    public static final String consensusXMLConverterUsageExample
            = "-compress false";

    /**
     * Constant.
     */
    public static final String consensusXMLConverterToolDescription
            = "This tool converts a consensusXML file from openMS to an mzQuantML file.";

    /**
     * Constant.
     */
    public static final String consensusXMLConverterUsage
            = "ConsensusXMLConverter input.consensusXML output.mzq "
            + consensusXMLConverterParams
            + " \n\nDescription:\n" + consensusXMLConverterToolDescription;

    /**
     * Constant.
     */
    public static final String idMappingParams
            = "-rawToMzidMap rawToMzidMap [-compress true|false]";

    /**
     * Constant.
     */
    public static final String idMappingUsageExample
            = "-rawToMzidMap filename1.raw;C:\\Data\\Mzid\\filename1.mzid;filename2.raw;C:\\Data\\Mzid\\filename2.mzid -compress false";

    /**
     * Constant.
     */
    public static final String idMappingToolDescription
            = "This tool will reassign consensus peptide sequence by mapping the identifications across input mzid files. "
            + "The -rawToMzidMap argument requires pairs of a raw file name (file name only) and its mzid file (with absolute file path), using semicolon as seperator. "
            + "The raw file names must be same as those in the input mzq file.";

    /**
     * Constant.
     */
    public static final String idMappingUsage
            = "MzqMzIdMapping input.mzq output.mzq " + idMappingParams
            + " \n\nDescription:\n" + idMappingToolDescription;

    /**
     * Constant.
     */
    public static final String anovaParams
            = "-listType Protein|ProteinGroup -qlDTCA qlDataTypeCvAccession -assayIdsGroup assayIdsGroup [-compress true|false]";

    /**
     * Constant.
     */
    public static final String anovaUsageExample
            = "-listType ProteinGroup -qlDTCA MS:1002518 -assayIdsGroup ass_0,ass_1,ass_2,ass_3,ass_4;ass_5,ass_6,ass_7,ass_8,ass_9";

    /**
     * Constant.
     */
    public static final String anovaToolDescription
            = "This tool calculates one-way ANOVA p value of specified QuantLayer for either ProteinGroupList or ProteinList. "
            + "The QuantLayer is specified by passing Cv accession to qlDTCA option. The group of assays included in the ANOVA calculation is provided by the flat "
            + "string option of assayIdsGroup. The whole string is divided into groups which are separated by \";\" (semicolon) and in each group, "
            + "the member assay ids are separated by \",\" (comma).";

    /**
     * Constant.
     */
    public static final String anovaUsage = "AnovaPValue input.mzq output.mzq "
            + anovaParams
            + " \n\nDescription:\n" + anovaToolDescription;

    /**
     * Constant.
     */
    public static final String normalisationParams
            = "-normLvl Peptide|Feature -inDTCA inDTCA -outDTCA outDTCA -outDTCN outDTCN -tagDecoy tagDecoy [-compress true|false]";

    /**
     * Constant.
     */
    public static final String normalisationUsageExample
            = "-normLvl peptide -inDTCA MS:1001840 -outDTCA MS:1001891 -outDTCN Progenesis:peptide normalised abundance -tagDecoy XXX_";

    /**
     * Constant.
     */
    public static final String normalisationToolDescription
            = "This tool will calculate normalised value of specified AssayQuantLayer in one specified list (peptide or feature). "
            + "Then output the result as an mzq file. "
            + "The tool will automatically select the best reference assay.";

    /**
     * Constant.
     */
    public static final String normalisationUsage
            = "Normalisation input.mzq output.mzq " + normalisationParams
            + " \n\nDescription:\n" + normalisationToolDescription;

    /**
     * Constant.
     */
    public static final String proteinInferenceParams
            = "-op [sum|mean|median] -inPepNormCA inPeptideNormCvAccession -inPepRawCA inPeptideRawCvAccession "
            + "-outPGNormCA outProteinGroupNormCvAccession -outPGNormCN outProteinGroupNormCvName "
            + "-outPGRawCA outProteinGroupRawCvAccession -outPGRawCN outProteinGroupRawCvName";

    /**
     * Constant.
     */
    public static final String proteinInferenceUsageExample
            = "-op sum -inPepNormCA MS:1001891 -inPepRawCA MS:1001893 "
            + "-outPGNormCA MS:1002518 -outPGNormCN Progenesis:protein group normalised abundance "
            + "-outPGRawCA MS:1002519 -outPGRawCN Progenesis:protein group raw abundance";

    /**
     * Constant.
     */
    public static final String proteinInferenceToolDescription
            = "This tool will perform protein inference and calculate the abundance from specified quantlayer.";

    /**
     * Constant.
     */
    public static final String proteinInferenceUsage
            = "ProteinInference input.mzq output.mzq " + proteinInferenceParams
            + " \n\nDescription:\n" + proteinInferenceToolDescription;

    /**
     * Constant.
     */
    public static final String maxquantConverterParams
            = "-summary summaryFile -peptides peptidesFile -proteinGroups proteinGroupsFile "
            + "-template experimentalDesignTemplateFile";

    /**
     * Constant.
     */
    public static final String maxquantConverterUsageExample
            = "-summary summary.txt -peptides peptides.txt -proteinGroups proteinGroups.txt "
            + "-template ExperimentalDesignTemplate.txt";

    /**
     * Constant.
     */
    public static final String maxquantConverterToolDescription
            = "This tool will convert Maxquant result files to a single mzQuantML file. "
            + "User needs to select the \"evidence.txt\" in Maxquant output folder as the input file. Provide the other mandatory files via options. ";

    /**
     * Constant.
     */
    public static final String maxquantConverterUsage
            = "MaxquantConverter evidence.txt output.mzq "
            + maxquantConverterParams
            + " \n\nDescription:\n" + maxquantConverterToolDescription;

    /**
     * Constant.
     */
    public static final String progenesisConverterParams
            = "-pepList peptideListFile [-sep CsvSeparator -proteinGroupList [true|fasle] -rawPlusNorm [norm|raw]]";

    /**
     * Constant.
     */
    public static final String progenesisConverterUsageExample
            = "-pepList peptide_list.csv -proteinGroupList false -rawPlusNorm raw";

    /**
     * Constant.
     */
    public static final String progenesisConverterToolDescription
            = "This tool will convert Progenesis result files to a single mzQuantML file. "
            + "User needs to provide protein list CSV file as the input file and provide peptide list (or feature list) CSV file via option. ";

    /**
     * Constant.
     */
    public static final String progenesisConverterUsage
            = "ProgenesisConverter proteinList.csv output.mzq "
            + progenesisConverterParams
            + " \n\nDescription:\n" + progenesisConverterToolDescription;

    /**
     * Constant.
     */
    public String userFeedback = "java -jar jar-location/mzqlib-version.jar ";

    // Added by Fawaz Ghali to automatically update the MzidLib GUI 
    private final Map<String, String> allFunctions;

    //TODO: changed from 3 to 2 by Da 16/09/2014
    private static final int MINARGLEN = 3; //minimum argument length

    /**
     * Init all functions hashmap.
     */
    public MzQuantMLLib() {
        allFunctions = new HashMap<>();
        allFunctions.put("CsvConverter", csvConverterParams + ";@;"
                         + csvConverterUsage + ";@;" + csvConverterUsageExample
                         + ";@;" + csvConverterToolDescription);
        allFunctions.put("XlsConverter", xlsConverterParams + ";@;"
                         + xlsConverterUsage + ";@;" + xlsConverterUsageExample
                         + ";@;" + xlsConverterToolDescription);
        allFunctions.put("HtmlConverter", htmlConverterParams + ";@;"
                         + htmlConverterUsage + ";@;"
                         + htmlConverterUsageExample + ";@;"
                         + htmlConverterToolDescription);
        allFunctions.put("MzTabConverter", mzTabConverterParams + ";@;"
                         + mzTabConverterUsage + ";@;"
                         + mzTabConverterUsageExample + ";@;"
                         + mzTabConverterToolDescription);
        allFunctions.put("ConsensusXMLConverter", consensusXMLConverterParams
                         + ";@;" + consensusXMLConverterUsage + ";@;"
                         + consensusXMLConverterUsageExample + ";@;"
                         + consensusXMLConverterToolDescription);
        allFunctions.put("MzqMzIdMapping", idMappingParams + ";@;"
                         + idMappingUsage + ";@;" + idMappingUsageExample
                         + ";@;" + idMappingToolDescription);
        allFunctions.put("AnovaPValue", anovaParams + ";@;" + anovaUsage + ";@;"
                         + anovaUsageExample + ";@;" + anovaToolDescription);
        allFunctions.put("Normalisation", normalisationParams + ";@;"
                         + normalisationUsage + ";@;"
                         + normalisationUsageExample + ";@;"
                         + normalisationToolDescription);
        allFunctions.put("ProteinInference", proteinInferenceParams + ";@;"
                         + proteinInferenceUsage + ";@;"
                         + proteinInferenceUsageExample + ";@;"
                         + proteinInferenceToolDescription);
        allFunctions.put("MaxquantConverter", maxquantConverterParams + ";@;"
                         + maxquantConverterUsage + ";@;"
                         + maxquantConverterUsageExample + ";@;"
                         + maxquantConverterToolDescription);
        allFunctions.put("ProgenesisConverter", progenesisConverterParams
                         + ";@;" + progenesisConverterUsage + ";@;"
                         + progenesisConverterUsageExample + ";@;"
                         + progenesisConverterToolDescription);
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
     * Main class.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {

        MzQuantMLLib mzqLib = new MzQuantMLLib();
        try {

            mzqLib.init(args);

        } catch (Exception ex) {
            Logger.getLogger(MzQuantMLLib.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }

    /**
     * Initiates functions by specified arguments.
     *
     * @param args input arguments.
     *
     * @return messages.
     *
     * @throws IOException                    io exceptions.
     * @throws JAXBException                  jaxb exceptions.
     * @throws FileNotFoundException          file not found exceptions.
     * @throws DatatypeConfigurationException data type configuration
     *                                        exceptions.
     * @throws InterruptedException           interrupted exceptions.
     */
    public String init(String[] args)
            throws IOException, JAXBException, FileNotFoundException,
            DatatypeConfigurationException, InterruptedException {
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

        if (args != null && args.length > MINARGLEN) {

            inputFileName = args[1];
            outputFileName = args[2];
            // Added by FG uncompress
            if (inputFileName != null && outputFileName != null) {
                boolean testDir = new File(inputFileName).isDirectory();
                if (inputFileName.equals(outputFileName) && !testDir) {
                    guiFeedback = "Error: input and output file names are same";
                } else {
                    boolean uncompress = false;
                    File inputFile;
                    if (inputFileName.endsWith(".gz")) {
                        inputFile = Gzipper.extractFile(new File(inputFileName));
                        uncompress = true;
                    } else {
                        inputFile = new File(inputFileName);
                    }
                    // Added by FG compress file
                    String compress = Utils.getCmdParameter(args, "compress",
                                                            false);
                    //set default to "false" if compress parameter is not set:
                    if (compress == null) {
                        compress = "false";
                    }
                    if (Boolean.valueOf(compress) && outputFileName.endsWith(
                            ".gz")) {
                        outputFileName = outputFileName.substring(0,
                                                                  outputFileName.
                                                                  length()
                                                                  - 3);
                    }
                    // Added by FG check if path is folder

                    if (args[0].equals("CsvConverter")) {
                        new MzqLib("csv", inputFileName,
                                   outputFileName);
                    } else if (args[0].equals("XlsConverter")) {
                        new MzqLib("xls", inputFileName,
                                   outputFileName);
                    } else if (args[0].equals("HtmlConverter")) {
                        new MzqLib("html", inputFileName,
                                   outputFileName);
                    } else if (args[0].equals("ConsensusXMLConverter")) {
                        try {
                            ConsensusXMLProcessor conProc
                                    = ConsensusXMLProcessorFactory.getInstance().
                                    buildConsensusXMLProcessor(new File(
                                            inputFileName));
                            conProc.convert(outputFileName);
                        } catch (JAXBException | IOException ex) {
                            System.out.println(
                                    "Error running ConsensusXMLConverter: "
                                    + userFeedback + consensusXMLConverterUsage);
                            guiFeedback
                                    = "Error running ConsensusXMLConverter: "
                                    + consensusXMLConverterUsage + "\n"
                                    + ex.getMessage();
                            Logger.getLogger(MzQuantMLLib.class.getName()).log(
                                    Level.SEVERE, null, ex);
                        }
                    } else if (args[0].equals("MzTabConverter")) {
                        new MzqLib("mztab", inputFileName,
                                   outputFileName);
                    } else if (args[0].equals("MzqMzIdMapping")) {
                        String rawToMzidMapString = Utils.getCmdParameter(args,
                                                                          "rawToMzidMap",
                                                                          true);
                        MzQuantMLUnmarshaller mzqUm = new MzQuantMLUnmarshaller(
                                new File(inputFileName));
                        MzqMzIdMapper mapper = MzqMzIdMapperFactory.
                                getInstance().buildMzqMzIdMapper(mzqUm,
                                                                 rawToMzidMapString);
                        mapper.createMappedFile(new File(outputFileName));
                    } else if (args[0].equals("AnovaPValue")) {
                        String listType = Utils.
                                getCmdParameter(args, "listType", true);
                        String qlDTCA = Utils.getCmdParameter(args, "qlDTCA",
                                                              true);
                        String assayIdsGroupString = Utils.getCmdParameter(args,
                                                                           "assayIdsGroup",
                                                                           true);

                        MzqQLAnova mzqAnova = new MzqQLAnova(inputFileName,
                                                             listType,
                                                             assayIdsGroupString,
                                                             qlDTCA);
                        mzqAnova.writeMzQuantMLFile(outputFileName);
                    } else if (args[0].equals("Normalisation")) {
                        String normLevel = Utils.
                                getCmdParameter(args, "normLvl", true);
                        //String qlType = Utils.getCmdParameter(args, "qlType", true);
                        String inDTCA = Utils.getCmdParameter(args, "inDTCA",
                                                              true);
                        String outDTCA = Utils.getCmdParameter(args, "outDTCA",
                                                               true);
                        String outDTCN = Utils.getCmdParameter(args, "outDTCN",
                                                               true);
                        String tagDecoy = Utils.
                                getCmdParameter(args, "tagDecoy", true);

                        PepProtAbundanceNormalisation normalisation
                                = new PepProtAbundanceNormalisation(
                                        inputFileName, outputFileName, normLevel,
                                        "AssayQuantLayer", inDTCA, outDTCA,
                                        outDTCN, tagDecoy, null);
                        normalisation.multithreadingCalc();
                    } else if (args[0].equals("ProteinInference")) {
                        String op = Utils.getCmdParameter(args, "op", true);
                        String inPepNormCA = Utils.getCmdParameter(args,
                                                                   "inPepNormCA",
                                                                   true);
                        String inPepRawCA = Utils.getCmdParameter(args,
                                                                  "inPepRawCA",
                                                                  true);
                        String outPGNormCA = Utils.getCmdParameter(args,
                                                                   "outPGNormCA",
                                                                   true);
                        String outPGNormCN = Utils.getCmdParameter(args,
                                                                   "outPGNormCN",
                                                                   true);
                        String outPGRawCA = Utils.getCmdParameter(args,
                                                                  "outPGRawCA",
                                                                  true);
                        String outPGRawCN = Utils.getCmdParameter(args,
                                                                  "outPGRawCN",
                                                                  true);
                        //String qlType = Utils.getCmdParameter(args, "qlType", true);

                        ProteinAbundanceInference pai
                                = new ProteinAbundanceInference(inputFileName,
                                                                outputFileName,
                                                                op, inPepNormCA,
                                                                inPepRawCA,
                                                                outPGNormCA,
                                                                outPGNormCN,
                                                                outPGRawCA,
                                                                outPGRawCN,
                                                                "AssayQuantLayer");
                        pai.proteinInference();
                    } else if (args[0].equals("MaxquantConverter")) {
                        String summaryFn = Utils.
                                getCmdParameter(args, "summary", true);
                        String peptidesFn = Utils.getCmdParameter(args,
                                                                  "peptides",
                                                                  true);
                        String proteinGroupsFn = Utils.getCmdParameter(args,
                                                                       "proteinGroups",
                                                                       true);
                        String templateFn = Utils.getCmdParameter(args,
                                                                  "template",
                                                                  true);
                        MaxquantMzquantmlConverter maxCon
                                = new MaxquantMzquantmlConverter(inputFileName,
                                                                 peptidesFn,
                                                                 proteinGroupsFn,
                                                                 templateFn,
                                                                 summaryFn);
                        maxCon.convert(outputFileName);
                    } else if (args[0].equals("ProgenesisConverter")) {
                        String pepList = Utils.getCmdParameter(args, "pepList",
                                                               true);
                        String sep = Utils.getCmdParameter(args, "sep", false);
                        String pgl = Utils.getCmdParameter(args,
                                                           "proteinGroupList",
                                                           false);
                        String rawPlusNorm = Utils.getCmdParameter(args,
                                                                   "rawPlusNorm",
                                                                   false);

                        if (sep != null && sep.length() == 1) {
                            char[] sepCharArray = sep.toCharArray();
                            char seperator = sepCharArray[0];
                            ProgenMzquantmlConverter progenConv
                                    = new ProgenMzquantmlConverter(pepList,
                                                                   inputFileName,
                                                                   "", seperator);

                            if (pgl != null) {
                                boolean pglBoolean = Boolean.valueOf(pgl);
                                progenConv.convert(outputFileName, pglBoolean,
                                                   rawPlusNorm);
                            } else {
                                progenConv.convert(outputFileName, true,
                                                   rawPlusNorm);
                            }
                        } else if (sep != null && sep.length() != 1) {
                            throw new IllegalArgumentException(
                                    "The input sepearator must be a single character.");
                        } else {
                            ProgenMzquantmlConverter progenConv
                                    = new ProgenMzquantmlConverter(pepList,
                                                                   inputFileName,
                                                                   "", ',');

                            if (pgl != null) {
                                boolean pglBoolean = Boolean.valueOf(pgl);
                                progenConv.convert(outputFileName, pglBoolean,
                                                   rawPlusNorm);
                            } else {
                                progenConv.convert(outputFileName, true,
                                                   rawPlusNorm);
                            }
                        }
                    } else {
                        String tempFeedback
                                = "Program within mzqLibrary not recognized: "
                                + args[0]
                                + "\n\nPlease use one of the following command:\n\n"
                                + progenesisConverterUsage + "\n\n"
                                + maxquantConverterUsage + "\n\n"
                                + consensusXMLConverterUsage + "\n\n"
                                + csvConverterUsage + "\n\n" + xlsConverterUsage
                                + "\n\n" + htmlConverterUsage + "\n\n"
                                + mzTabConverterUsage + "\n\n"
                                + idMappingUsage + "\n\n" + normalisationUsage
                                + "\n\n" + proteinInferenceUsage + "\n\n"
                                + anovaUsage + "\n";

                        System.out.println(tempFeedback);
                        guiFeedback = "Error, usage: " + tempFeedback;
                        userFeedback = tempFeedback;
                    }
                    if (userFeedback.equals("")) {
                        userFeedback
                                = "Completed successfully, output written to "
                                + outputFileName;
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

            } else if (args[0].equals("CsvConverter")) {
                guiFeedback = "Error, usage: " + csvConverterUsage;
            } else if (args[0].equals("XlsConverter")) {
                guiFeedback = "Error, usage:  " + xlsConverterUsage;
            } else if (args[0].equals("HtmlConverter")) {
                guiFeedback = "Error, usage: " + htmlConverterUsage;
            } else if (args[0].equals("ConsensusXMLConverter")) {
                guiFeedback = "Error, usage:  " + consensusXMLConverterUsage;
            } else if (args[0].equals("MzTabConverter")) {
                guiFeedback = "Error, usage: " + mzTabConverterUsage;
            } else if (args[0].equals("MzqMzIdMapping")) {
                guiFeedback = "Error, usage:  " + idMappingUsage;
            } else if (args[0].equals("AnovaPValue")) {
                guiFeedback = "Error, usage:  " + anovaUsage;
            } else if (args[0].equals("Normalisation")) {
                guiFeedback = "Error, usage:  " + normalisationUsage;
            } else if (args[0].equals("ProteinInference")) {
                guiFeedback = "Error, usage:  " + proteinInferenceUsage;
            } else if (args[0].equals("MaxquantConverter")) {
                guiFeedback = "Error, usage:  " + maxquantConverterUsage;
            } else if (args[0].equals("ProgenesisConverter")) {
                guiFeedback = "Error, usage:  " + progenesisConverterUsage;
            } else {
                String tempFeedback
                        = "Program within mzqLibrary not recognized: " + args[0]
                        + "\n\nPlease use one of the following command:\n\n"
                        + progenesisConverterUsage + "\n\n"
                        + maxquantConverterUsage + "\n\n"
                        + consensusXMLConverterUsage + "\n\n"
                        + csvConverterUsage + "\n\n" + xlsConverterUsage
                        + "\n\n" + htmlConverterUsage + "\n\n"
                        + mzTabConverterUsage + "\n\n"
                        + idMappingUsage + "\n\n" + normalisationUsage + "\n\n"
                        + proteinInferenceUsage + "\n\n" + anovaUsage + "\n";

                System.out.println(tempFeedback);
                guiFeedback = "Error, usage: " + tempFeedback;
                userFeedback = tempFeedback;
            }
            if (userFeedback.equals("")) {
                userFeedback = "Completed successfully, output written to "
                        + outputFileName;
            }
        } else {
            String tempFeedback
                    = "Error insufficient arguments entered, options: "
                    + userFeedback + " toolname options\n"
                    + "\n\nTools:\n\n***************\n"
                    + csvConverterUsage
                    + "\n" + "***************\n" + xlsConverterUsage
                    + "\n" + "***************\n" + htmlConverterUsage
                    + "\n" + "***************\n" + mzTabConverterUsage
                    + "\n" + "***************\n" + progenesisConverterUsage
                    + "\n" + "***************\n" + maxquantConverterUsage
                    + "\n" + "***************\n" + consensusXMLConverterUsage
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
