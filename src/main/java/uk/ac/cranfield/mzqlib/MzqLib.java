package uk.ac.cranfield.mzqlib;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import uk.ac.cranfield.mzqlib.converter.CsvConverter;
import uk.ac.cranfield.mzqlib.converter.GenericConverter;
import uk.ac.cranfield.mzqlib.converter.HtmlConverter;
import uk.ac.cranfield.mzqlib.converter.MztabConverter;
import uk.ac.cranfield.mzqlib.data.MzqData;
import uk.ac.liv.jmzqml.model.mzqml.MzQuantML;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

public class MzqLib {
    private final int CSV = 1;
    private final int MZTAB = 2;
    private final int HTML = 3;
    public static MzqData data = new MzqData();
    private HashMap<String,Integer> converterTypeMap = new HashMap<String, Integer>();
    //call the GUI
    public MzqLib(){
        
    }
    //call the converter
    public MzqLib(String typeStr,String mzqFile){
        initialize();
        int type = getType(typeStr);
        if (type==0) {
            System.out.println("Unrecognized converter type. Please use one value in the collection of " + converterTypeMap.keySet().toString());
            printUsage();
        }
        parseMzq(mzqFile);
        GenericConverter converter = null;
        switch(type){
            case CSV:
                converter = new CsvConverter(mzqFile);
                break;
            case MZTAB:
                converter = new MztabConverter(mzqFile);
                break;
            case HTML:
                converter = new HtmlConverter(mzqFile);
                break;
            default:
        }
        converter.convert();
    }
    
    private void parseMzq(String mzqFile){
        File file = new File(mzqFile);
        if(!file.exists()){
            System.out.println("Can not find the specified file:"+file.getAbsolutePath());
            System.exit(1);
        }
        boolean validFlag = Utils.validateMzqFile(mzqFile);
        if(!validFlag){
            System.out.println("The mzQuantML validation went wrong, program terminated");
            System.exit(1);
        }
        System.out.println("Validation successful for the file "+mzqFile);
        MzQuantMLUnmarshaller unmarshaller = new MzQuantMLUnmarshaller(mzqFile);
        MzQuantML mzq = unmarshaller.unmarshall();
        
        data.addAssays(mzq.getAssayList());
        data.addStudyVariables(mzq.getStudyVariableList());
        data.addRatios(mzq.getRatioList());
        data.addProteins(mzq.getProteinList());
        
        final PeptideConsensusList finalResultPcList = getFinalResult(mzq.getPeptideConsensusList());
        if(finalResultPcList!=null) data.addPeptides(finalResultPcList);
//        data.addProteins(mzq.getProteinList());
    }

    private PeptideConsensusList getFinalResult(List<PeptideConsensusList> peptideConsensusLists) {
        for (PeptideConsensusList pcList:peptideConsensusLists){
            if (pcList.isFinalResult()) return pcList;
        }
        return null;
    }
    
    private static void batch(){
        File dir = new File(".");
        for(File file:dir.listFiles()){
            if(file.getAbsolutePath().endsWith(".mzq")){
                System.out.println(file.getAbsolutePath());
                MzqLib lib = new MzqLib("csv",file.getAbsolutePath());
            }
        }
        System.exit(0);
    }

    public static void main( String[] args ) {
        batch();
        int argsLen = args.length;
        MzqLib lib;
        switch(argsLen){
            case 0://GUI
                lib = new MzqLib();
                break;
            case 2://command line converter
                lib = new MzqLib(args[0].toLowerCase(),args[1]);
                break;
            default:
                printUsage();
        }
    }
    
    private static void printUsage(){
        System.out.println("Usage: java -jar mzqlib.jar [converter type] [mzQuantML file]");
        System.out.println("There are two ways of using mzqlib jar file:");
        System.out.println("1. without options: it will present a GUI");
        System.out.println("2. with options: the first option must be the type of the output file type, now supports csv,mztab and html, the second option is the mzquantml file which needs to be converted");
        System.out.println("For example java -jar mzqlib.jar html example.mzq");
        System.exit(0);
    }
    
    private void initialize(){
        converterTypeMap.put("html", HTML);
        converterTypeMap.put("mztab", MZTAB);
        converterTypeMap.put("csv", CSV);
        data = new MzqData();
    }
    
    private int getType(String type){
        if(converterTypeMap.containsKey(type)) return converterTypeMap.get(type);
        return 0;
    }
}
