
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

    private final int FILENAME_SUF_LEN = 3;
    //CSV converter
    /**
     * Constant.
     */
    private static final String CSV_CONVERTER_PARAMS = "-compress true|false";

    /**
     * Constant.
     */
    private static final String CSV_CONVERTER_USAGE_EXAMPLE = "-compress false";

    /**
     * Constant.
     */
    private static final String CSV_CONVERTER_TOOL_DES
            = "This tool converts an mzQuantML file to a CSV file.";

    /**
     * Constant.
     */
    private static final String CSV_CONVERTER_USAGE
            = "CsvConverter input.mzq output.csv " + CSV_CONVERTER_PARAMS
            + " \n\nDescription:\n" + CSV_CONVERTER_TOOL_DES;

    //XLS converter
    /**
     * Constant.
     */
    private static final String XLS_CONVERTER_PARAMS = "-compress true|false";

    /**
     * Constant.
     */
    private static final String XLS_CONVERTER_USAGE_EXAMPLE = "-compress false";

    /**
     * Constant.
     */
    private static final String XLS_CONVERTER_TOOL_DES
            = "This tool converts an mzQuantML file to a XLS file.";

    /**
     * Constant.
     */
    private static final String XLS_CONVERTER_USAGE
            = "XlsConverter input.mzq output.csv " + XLS_CONVERTER_PARAMS
            + " \n\nDescription:\n" + XLS_CONVERTER_TOOL_DES;

    //HTML converter
    /**
     * Constant.
     */
    private static final String HTML_CONVERTER_PARAMS = "-compress true|false";

    /**
     * Constant.
     */
    private static final String HTML_CONVERTER_USAGE_EXAMPLE = "-compress false";

    /**
     * Constant.
     */
    private static final String HTML_CONVERTER_TOOL_DES
            = "This tool converts an mzQuantML file to a HTML file.";

    /**
     * Constant.
     */
    private static final String HTML_CONVERTER_USAGE
            = "HtmlConverter input.mzq output.html " + HTML_CONVERTER_PARAMS
            + " \n\nDescription:\n" + HTML_CONVERTER_TOOL_DES;

    //mzTab converter
    /**
     * Constant.
     */
    private static final String MZTAB_CONVERTER_PARAMS = "-compress true|false";

    /**
     * Constant.
     */
    private static final String MZTAB_CONVERTER_USAGE_EXAMPLE = "-compress false";

    /**
     * Constant.
     */
    private static final String MZTAB_CONVERTER_TOOL_DES
            = "This tool converts an mzQuantML file to an mzTab file.";

    /**
     * Constant.
     */
    private static final String MZTAB_CONVERTER_USAGE
            = "MzTabConverter input.mzq output.mztab " + MZTAB_CONVERTER_PARAMS
            + " \n\nDescription:\n" + MZTAB_CONVERTER_TOOL_DES;

    //ConsensusXML converter
    /**
     * Constant.
     */
    private static final String CONSENSUSXML_CONVERTER_PARAMS
            = "-compress true|false";

    /**
     * Constant.
     */
    private static final String CONSENSUSXML_CONVERTER_USAGE_EXAMPLE
            = "-compress false";

    /**
     * Constant.
     */
    private static final String CONSENSUSXML_CONVERTER_TOOL_DES
            = "This tool converts a consensusXML file from openMS to an mzQuantML file.";

    /**
     * Constant.
     */
    private static final String CONSENSUSXML_CONVERTER_USAGE
            = "ConsensusXMLConverter input.consensusXML output.mzq "
            + CONSENSUSXML_CONVERTER_PARAMS
            + " \n\nDescription:\n" + CONSENSUSXML_CONVERTER_TOOL_DES;

    /**
     * Constant.
     */
    private static final String ID_MAPPING_PARAMS
            = "-rawToMzidMap rawToMzidMap [-compress true|false]";

    /**
     * Constant.
     */
    private static final String ID_MAPPING_USAGE_EXAMPLE
            = "-rawToMzidMap filename1.raw;C:\\Data\\Mzid\\filename1.mzid;filename2.raw;C:\\Data\\Mzid\\filename2.mzid -compress false";

    /**
     * Constant.
     */
    private static final String ID_MAPPING_TOOL_DES
            = "This tool will reassign consensus peptide sequence by mapping the identifications across input mzid files. "
            + "The -rawToMzidMap argument requires pairs of a raw file name (file name only) and its mzid file (with absolute file path), using semicolon as seperator. "
            + "The raw file names must be same as those in the input mzq file.";

    /**
     * Constant.
     */
    private static final String ID_MAPPING_USAGE
            = "MzqMzIdMapping input.mzq output.mzq " + ID_MAPPING_PARAMS
            + " \n\nDescription:\n" + ID_MAPPING_TOOL_DES;

    /**
     * Constant.
     */
    private static final String ANOVA_PARAMS
            = "-listType Protein|ProteinGroup -qlDTCA qlDataTypeCvAccession -assayIdsGroup assayIdsGroup [-compress true|false]";

    /**
     * Constant.
     */
    private static final String ANOVA_USAGE_EXAMPLE
            = "-listType ProteinGroup -qlDTCA MS:1002518 -assayIdsGroup ass_0,ass_1,ass_2,ass_3,ass_4;ass_5,ass_6,ass_7,ass_8,ass_9";

    /**
     * Constant.
     */
    private static final String ANOVA_TOOL_DES
            = "This tool calculates one-way ANOVA p value of specified QuantLayer for either ProteinGroupList or ProteinList. "
            + "The QuantLayer is specified by passing Cv accession to qlDTCA option. The group of assays included in the ANOVA calculation is provided by the flat "
            + "string option of assayIdsGroup. The whole string is divided into groups which are separated by \";\" (semicolon) and in each group, "
            + "the member assay ids are separated by \",\" (comma).";

    /**
     * Constant.
     */
    private static final String ANOVA_USAGE = "AnovaPValue input.mzq output.mzq "
            + ANOVA_PARAMS
            + " \n\nDescription:\n" + ANOVA_TOOL_DES;

    /**
     * Constant.
     */
    private static final String NORMALISATION_PARAMS
            = "-normLvl Peptide|Feature -inDTCA inDTCA -outDTCA outDTCA -outDTCN outDTCN -tagDecoy tagDecoy [-compress true|false]";

    /**
     * Constant.
     */
    private static final String NORMALISATION_USAGE_EXAMPLE
            = "-normLvl peptide -inDTCA MS:1001840 -outDTCA MS:1001891 -outDTCN Progenesis:peptide normalised abundance -tagDecoy XXX_";

    /**
     * Constant.
     */
    private static final String NORMALISATION_TOOL_DES
            = "This tool will calculate normalised value of specified AssayQuantLayer in one specified list (peptide or feature). "
            + "Then output the result as an mzq file. "
            + "The tool will automatically select the best reference assay.";

    /**
     * Constant.
     */
    private static final String NORMALISATION_USAGE
            = "Normalisation input.mzq output.mzq " + NORMALISATION_PARAMS
            + " \n\nDescription:\n" + NORMALISATION_TOOL_DES;

    /**
     * Constant.
     */
    private static final String PROTEIN_INFERENCE_PARAMS
            = "-op [sum|mean|median] -inPepNormCA inPeptideNormCvAccession "
            + "-outPGNormCA outProteinGroupNormCvAccession -outPGNormCN outProteinGroupNormCvName";

    /**
     * Constant.
     */
    private static final String PROTEIN_INFERENCE_USAGE_EXAMPLE
            = "-op sum -inPepNormCA MS:1001891 "
            + "-outPGNormCA MS:1002518 -outPGNormCN Progenesis:protein group normalised abundance";

    /**
     * Constant.
     */
    private static final String PROTEIN_INFERENCE_TOOL_DES
            = "This tool will perform protein inference and calculate the abundance from specified quantlayer.";

    /**
     * Constant.
     */
    private static final String PROTEIN_INFERENCE_USAGE
            = "ProteinInference input.mzq output.mzq " + PROTEIN_INFERENCE_PARAMS
            + " \n\nDescription:\n" + PROTEIN_INFERENCE_TOOL_DES;

    /**
     * Constant.
     */
    private static final String MAXQUANT_CONVERTER_PARAMS
            = "-summary summaryFile -peptides peptidesFile -proteinGroups proteinGroupsFile "
            + "-template experimentalDesignTemplateFile";

    /**
     * Constant.
     */
    private static final String MAXQUANT_CONVERTER_USAGE_EXAMPLE
            = "-summary summary.txt -peptides peptides.txt -proteinGroups proteinGroups.txt "
            + "-template ExperimentalDesignTemplate.txt";

    /**
     * Constant.
     */
    private static final String MAXQUANT_CONVERTER_TOOL_DES
            = "This tool will convert Maxquant result files to a single mzQuantML file. "
            + "User needs to select the \"evidence.txt\" in Maxquant output folder as the input file. Provide the other mandatory files via options. ";

    /**
     * Constant.
     */
    private static final String MAXQUANT_CONVERTER_USAGE
            = "MaxquantConverter evidence.txt output.mzq "
            + MAXQUANT_CONVERTER_PARAMS
            + " \n\nDescription:\n" + MAXQUANT_CONVERTER_TOOL_DES;

    /**
     * Constant.
     */
    private static final String PROGENESIS_CONVERTER_PARAMS
            = "-pepList peptideListFile [-sep CsvSeparator -proteinGroupList [true|fasle] -rawPlusNorm [norm|raw]]";

    /**
     * Constant.
     */
    private static final String PROGENESIS_CONVERTER_USAGE_EXAMPLE
            = "-pepList peptide_list.csv -proteinGroupList false -rawPlusNorm raw";

    /**
     * Constant.
     */
    private static final String PROGENESIS_CONVERTER_TOOL_DES
            = "This tool will convert Progenesis result files to a single mzQuantML file. "
            + "User needs to provide protein list CSV file as the input file and provide peptide list (or feature list) CSV file via option. ";

    /**
     * Constant.
     */
    private static final String PROGENESIS_CONVERTER_USAGE
            = "ProgenesisConverter proteinList.csv output.mzq "
            + PROGENESIS_CONVERTER_PARAMS
            + " \n\nDescription:\n" + PROGENESIS_CONVERTER_TOOL_DES;

    /**
     * Constant.
     */
    private String userFeedback = "java -jar jar-location/mzqlib-version.jar ";

    // Added by Fawaz Ghali to automatically update the MzidLib GUI
    private final Map<String, String> allFunctions;

    //TODO: changed from 3 to 2 by Da 16/09/2014
    private static final int MINARGLEN = 3; //minimum argument length

    /**
     * Init all functions hashmap.
     */
    public MzQuantMLLib() {
        allFunctions = new HashMap<>();
        allFunctions.put("CsvConverter", CSV_CONVERTER_PARAMS + ";@;"
                         + CSV_CONVERTER_USAGE + ";@;" + CSV_CONVERTER_USAGE_EXAMPLE
                         + ";@;" + CSV_CONVERTER_TOOL_DES);
        allFunctions.put("XlsConverter", XLS_CONVERTER_PARAMS + ";@;"
                         + XLS_CONVERTER_USAGE + ";@;" + XLS_CONVERTER_USAGE_EXAMPLE
                         + ";@;" + XLS_CONVERTER_TOOL_DES);
        allFunctions.put("HtmlConverter", HTML_CONVERTER_PARAMS + ";@;"
                         + HTML_CONVERTER_USAGE + ";@;"
                         + HTML_CONVERTER_USAGE_EXAMPLE + ";@;"
                         + HTML_CONVERTER_TOOL_DES);
        allFunctions.put("MzTabConverter", MZTAB_CONVERTER_PARAMS + ";@;"
                         + MZTAB_CONVERTER_USAGE + ";@;"
                         + MZTAB_CONVERTER_USAGE_EXAMPLE + ";@;"
                         + MZTAB_CONVERTER_TOOL_DES);
        allFunctions.put("ConsensusXMLConverter", CONSENSUSXML_CONVERTER_PARAMS
                         + ";@;" + CONSENSUSXML_CONVERTER_USAGE + ";@;"
                         + CONSENSUSXML_CONVERTER_USAGE_EXAMPLE + ";@;"
                         + CONSENSUSXML_CONVERTER_TOOL_DES);
        allFunctions.put("MzqMzIdMapping", ID_MAPPING_PARAMS + ";@;"
                         + ID_MAPPING_USAGE + ";@;" + ID_MAPPING_USAGE_EXAMPLE
                         + ";@;" + ID_MAPPING_TOOL_DES);
        allFunctions.put("AnovaPValue", ANOVA_PARAMS + ";@;" + ANOVA_USAGE + ";@;"
                         + ANOVA_USAGE_EXAMPLE + ";@;" + ANOVA_TOOL_DES);
        allFunctions.put("Normalisation", NORMALISATION_PARAMS + ";@;"
                         + NORMALISATION_USAGE + ";@;"
                         + NORMALISATION_USAGE_EXAMPLE + ";@;"
                         + NORMALISATION_TOOL_DES);
        allFunctions.put("ProteinInference", PROTEIN_INFERENCE_PARAMS + ";@;"
                         + PROTEIN_INFERENCE_USAGE + ";@;"
                         + PROTEIN_INFERENCE_USAGE_EXAMPLE + ";@;"
                         + PROTEIN_INFERENCE_TOOL_DES);
        allFunctions.put("MaxquantConverter", MAXQUANT_CONVERTER_PARAMS + ";@;"
                         + MAXQUANT_CONVERTER_USAGE + ";@;"
                         + MAXQUANT_CONVERTER_USAGE_EXAMPLE + ";@;"
                         + MAXQUANT_CONVERTER_TOOL_DES);
        allFunctions.put("ProgenesisConverter", PROGENESIS_CONVERTER_PARAMS
                         + ";@;" + PROGENESIS_CONVERTER_USAGE + ";@;"
                         + PROGENESIS_CONVERTER_USAGE_EXAMPLE + ";@;"
                         + PROGENESIS_CONVERTER_TOOL_DES);
    }

    /**
     * Getter for all functions to be used in MzqLib GUI
     *
     * @return all functions as hashmap
     */
    public final Map<String, String> getAllFunctions() {
        return allFunctions;
    }

    /**
     * Main class.
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {

        MzQuantMLLib mzqLib = new MzQuantMLLib();
        try {

            mzqLib.init(args);

        } catch (IOException | JAXBException | DatatypeConfigurationException
                | InterruptedException ex) {
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
    public final String init(final String[] args)
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
                                                                  - FILENAME_SUF_LEN);
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
                                    + userFeedback + CONSENSUSXML_CONVERTER_USAGE);
                            guiFeedback
                                    = "Error running ConsensusXMLConverter: "
                                    + CONSENSUSXML_CONVERTER_USAGE + "\n"
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
                        String outPGNormCA = Utils.getCmdParameter(args,
                                                                   "outPGNormCA",
                                                                   true);
                        String outPGNormCN = Utils.getCmdParameter(args,
                                                                   "outPGNormCN",
                                                                   true);
                        ProteinAbundanceInference pai
                                = new ProteinAbundanceInference(inputFileName,
                                                                op, inPepNormCA,
                                                                outPGNormCA,
                                                                outPGNormCN,
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
                                + PROGENESIS_CONVERTER_USAGE + "\n\n"
                                + MAXQUANT_CONVERTER_USAGE + "\n\n"
                                + CONSENSUSXML_CONVERTER_USAGE + "\n\n"
                                + CSV_CONVERTER_USAGE + "\n\n" + XLS_CONVERTER_USAGE
                                + "\n\n" + HTML_CONVERTER_USAGE + "\n\n"
                                + MZTAB_CONVERTER_USAGE + "\n\n"
                                + ID_MAPPING_USAGE + "\n\n" + NORMALISATION_USAGE
                                + "\n\n" + PROTEIN_INFERENCE_USAGE + "\n\n"
                                + ANOVA_USAGE + "\n";

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
                guiFeedback = "Error, usage: " + CSV_CONVERTER_USAGE;
            } else if (args[0].equals("XlsConverter")) {
                guiFeedback = "Error, usage:  " + XLS_CONVERTER_USAGE;
            } else if (args[0].equals("HtmlConverter")) {
                guiFeedback = "Error, usage: " + HTML_CONVERTER_USAGE;
            } else if (args[0].equals("ConsensusXMLConverter")) {
                guiFeedback = "Error, usage:  " + CONSENSUSXML_CONVERTER_USAGE;
            } else if (args[0].equals("MzTabConverter")) {
                guiFeedback = "Error, usage: " + MZTAB_CONVERTER_USAGE;
            } else if (args[0].equals("MzqMzIdMapping")) {
                guiFeedback = "Error, usage:  " + ID_MAPPING_USAGE;
            } else if (args[0].equals("AnovaPValue")) {
                guiFeedback = "Error, usage:  " + ANOVA_USAGE;
            } else if (args[0].equals("Normalisation")) {
                guiFeedback = "Error, usage:  " + NORMALISATION_USAGE;
            } else if (args[0].equals("ProteinInference")) {
                guiFeedback = "Error, usage:  " + PROTEIN_INFERENCE_USAGE;
            } else if (args[0].equals("MaxquantConverter")) {
                guiFeedback = "Error, usage:  " + MAXQUANT_CONVERTER_USAGE;
            } else if (args[0].equals("ProgenesisConverter")) {
                guiFeedback = "Error, usage:  " + PROGENESIS_CONVERTER_USAGE;
            } else {
                String tempFeedback
                        = "Program within mzqLibrary not recognized: " + args[0]
                        + "\n\nPlease use one of the following command:\n\n"
                        + PROGENESIS_CONVERTER_USAGE + "\n\n"
                        + MAXQUANT_CONVERTER_USAGE + "\n\n"
                        + CONSENSUSXML_CONVERTER_USAGE + "\n\n"
                        + CSV_CONVERTER_USAGE + "\n\n" + XLS_CONVERTER_USAGE
                        + "\n\n" + HTML_CONVERTER_USAGE + "\n\n"
                        + MZTAB_CONVERTER_USAGE + "\n\n"
                        + ID_MAPPING_USAGE + "\n\n" + NORMALISATION_USAGE + "\n\n"
                        + PROTEIN_INFERENCE_USAGE + "\n\n" + ANOVA_USAGE + "\n";

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
                    + CSV_CONVERTER_USAGE
                    + "\n" + "***************\n" + XLS_CONVERTER_USAGE
                    + "\n" + "***************\n" + HTML_CONVERTER_USAGE
                    + "\n" + "***************\n" + MZTAB_CONVERTER_USAGE
                    + "\n" + "***************\n" + PROGENESIS_CONVERTER_USAGE
                    + "\n" + "***************\n" + MAXQUANT_CONVERTER_USAGE
                    + "\n" + "***************\n" + CONSENSUSXML_CONVERTER_USAGE
                    + "\n" + "***************\n" + ID_MAPPING_USAGE
                    + "\n" + "***************\n" + NORMALISATION_USAGE
                    + "\n" + "***************\n" + PROTEIN_INFERENCE_USAGE
                    + "\n" + "***************\n" + ANOVA_USAGE + "\n";

            System.out.println(tempFeedback);
            guiFeedback = tempFeedback;
        }

        return guiFeedback;

    }

}
