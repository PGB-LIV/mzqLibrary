
package uk.ac.cranfield.mzqlib;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import uk.ac.cranfield.mzqlib.converter.CsvConverter;
import uk.ac.cranfield.mzqlib.converter.GenericConverter;
import uk.ac.cranfield.mzqlib.converter.HtmlConverter;
import uk.ac.cranfield.mzqlib.converter.MztabConverter;
import uk.ac.cranfield.mzqlib.converter.XlsConverter;
import uk.ac.cranfield.mzqlib.data.MzqData;
import uk.ac.liv.pgb.jmzqml.MzQuantMLElement;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AnalysisSummary;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AssayList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.InputFiles;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroupList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RatioList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SoftwareList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.StudyVariableList;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 * MzqLib class.
 * @author Jun Fan
 */
public class MzqLib {

    private final static int CSV = 1;
    private final static int MZTAB = 2;
    private final static int HTML = 3;
    private final static int XLS = 4;

    /**
     * Static MzqData instance.
     */
    public static final MzqData DATA = new MzqData();
    private HashMap<String, Integer> converterTypeMap = new HashMap<>();

    //call the GUI

    /**
     * Constructor.
     */
    public MzqLib() {

    }

    /**
     * Call the converter.
     * @param typeStr File format of the output file from converter. 
     * @param mzqFile Input mzq file name.
     * @param outputFile Output file name.
     */
    public MzqLib(String typeStr, String mzqFile, String outputFile) {
        initialize();
        int type = getType(typeStr);
        if (type == 0) {
            System.out.println(
                    "Unrecognized converter type. Please use one value in the collection of "
                    + converterTypeMap.keySet().toString());
            printUsage();
        }
        parseMzq(mzqFile);
        GenericConverter converter = null;
        switch (type) {
            case CSV:
                converter = new CsvConverter(mzqFile, outputFile);
                DATA.setNeedAutoAssignment(false);
                break;
            case MZTAB:
                converter = new MztabConverter(mzqFile, outputFile);
                DATA.setNeedAutoAssignment(true);//feature_ref is needed to get m/z and rt for peptide from features
                break;
            case HTML:
                converter = new HtmlConverter(mzqFile, outputFile);
                DATA.setNeedAutoAssignment(true);
                break;
            case XLS:
                converter = new XlsConverter(mzqFile, outputFile);
                DATA.setNeedAutoAssignment(false);
                break;
            default:
        }
        DATA.autoAssign();
        if (converter != null) {
            converter.convert();
        }
    }

    private void parseMzq(String mzqFile) {
        File file = new File(mzqFile);
        if (!file.exists()) {
            throw new IllegalStateException("Can not find the specified file:"
                    + file.getAbsolutePath());
        }
        //boolean validFlag = Utils.validateMzqFile(mzqFile);
//        if(!validFlag){
//            System.out.println("The mzQuantML validation went wrong, program terminated");
//            System.exit(1);
//        }
//        System.out.println("Validation successful for the file "+mzqFile);

        MzQuantMLUnmarshaller unmarshaller = new MzQuantMLUnmarshaller(file);

//	<xsd:element name="AssayList" type="AssayListType" minOccurs="1" maxOccurs="1"/>
        DATA.addAssays(unmarshaller.unmarshal(AssayList.class));
//	<xsd:element name="StudyVariableList" type="StudyVariableListType" minOccurs="0" maxOccurs="1"/>
        DATA.addStudyVariables(unmarshaller.unmarshal(StudyVariableList.class));
        DATA.addRatios(unmarshaller.unmarshal(RatioList.class));

        List<FeatureList> features = new ArrayList<>();
        Iterator<FeatureList> featureIterator = unmarshaller.
                unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);
        while (featureIterator.hasNext()) {
            features.add(featureIterator.next());
        }
        DATA.addFeatures(features);

        Iterator<PeptideConsensusList> peptideConsensusLists = unmarshaller.
                unmarshalCollectionFromXpath(
                        MzQuantMLElement.PeptideConsensusList);
        while (peptideConsensusLists.hasNext()) {
            PeptideConsensusList pcList = peptideConsensusLists.next();
            if (pcList.isFinalResult()) {
                DATA.addPeptides(pcList);
                break;
            }
        }

        ProteinList proteinList = unmarshaller.unmarshal(
                MzQuantMLElement.ProteinList);
        DATA.addProteins(proteinList);
        ProteinGroupList pgList = unmarshaller.unmarshal(
                MzQuantMLElement.ProteinGroupList);
        DATA.addProteinGroups(pgList);

//	<xsd:element name="SoftwareList" type="SoftwareListType" minOccurs="1" maxOccurs="1"/>
        DATA.setSoftwareList(unmarshaller.unmarshal(SoftwareList.class));
//	<xsd:element name="AnalysisSummary" type="ParamListType" minOccurs="1" maxOccurs="1">
        DATA.setAnalysisSummary(unmarshaller.unmarshal(AnalysisSummary.class));
//	<xsd:element name="InputFiles" type="InputFilesType" minOccurs="1" maxOccurs="1"/>
        DATA.setInputFiles(unmarshaller.unmarshal(InputFiles.class));
//	<xsd:element name="CvList" type="CvListType" minOccurs="1" maxOccurs="1"/>
        DATA.setCvList(unmarshaller.unmarshal(CvList.class));
        DATA.setMzqID(unmarshaller.getMzQuantMLId());
        DATA.setMzqName(unmarshaller.getMzQuantMLName());
    }

//    private static void batch() {
//        File dir = new File(".");
//        for (File file : dir.listFiles()) {
//            if (file.getAbsolutePath().endsWith(".mzq")) {
//                System.out.println(file.getAbsolutePath());
//                MzqLib lib = new MzqLib("xls", file.getAbsolutePath(), "");
//                MzqLib lib2 = new MzqLib("csv", file.getAbsolutePath(), "");
//                MzqLib lib3 = new MzqLib("html", file.getAbsolutePath(), "");
//                MzqLib lib4 = new MzqLib("mztab", file.getAbsolutePath(), "");
//            }
//        }
//        //System.exit(0);
//    }

    /**
     * Main class.
     * @param args input argument array.
     */
    public static void main(String[] args) {
//        new MzqLib("csv","maxquant-silac.mzq","");
//        new MzqLib("mztab","iTraq3standards.mzq","");
//        new MzqLib("mztab","CPTAC-Progenesis-small-example.mzq","");
//        new MzqLib("mztab", "AllAgesPeptideNormalised_proteins_pvalues.mzq", "");
//        new MzqLib("csv","AllAgesPeptideNormalised_proteins_pvalues.mzq","");
//        new MzqLib("xls","AllAgesPeptideNormalised_proteins_pvalues.mzq","");
        //System.exit(0);
        //batch();
        int argsLen = args.length;
        //MzqLib lib;
        switch (argsLen) {
//            case 0://GUI
//                lib = new MzqLib();
//                break;
            case 2://command line converter
                //lib = 
                new MzqLib(args[0].toLowerCase(Locale.ENGLISH), args[1], "");
                break;
            case 3://command line converter
                //lib = 
                new MzqLib(args[0].toLowerCase(Locale.ENGLISH), args[1], args[2]);
                break;
            default:
                printUsage();
        }
    }

    private static void printUsage() {
        System.out.println(
                "Usage: java -jar mzqlib.jar <converter type> <mzQuantML file> [output file]");
//        System.out.println("There are two ways of using mzqlib jar file:");
//        System.out.println("1. without options: it will present a GUI");
//        System.out.println("2. with options: the first option must be the type of the output file type, now supports csv, mztab,html and xls\n\tthe second option is the mzquantml file which needs to be converted\n\tThe third option is optional, which defines the name of the output file");
        System.out.println(
                "The first parameter specifies the type of the output file type, now supports csv, mztab,html and xls.\nThe second parameter is the mzquantml file which needs to be converted\nThe third parameter is optional, which defines the name of the output file");
        System.out.println("For example java -jar mzqlib.jar html example.mzq");
        //System.exit(0);
    }

    private void initialize() {
        converterTypeMap.put("html", HTML);
        converterTypeMap.put("mztab", MZTAB);
        converterTypeMap.put("csv", CSV);
        converterTypeMap.put("xls", XLS);
        //data = new MzqData();
    }

    private int getType(String type) {
        if (converterTypeMap.containsKey(type)) {
            return converterTypeMap.get(type);
        }
        return 0;
    }

}
