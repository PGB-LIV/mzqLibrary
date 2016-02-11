
package uk.ac.liv.mzqlib.maxquant.converter;

import au.com.bytecode.opencsv.*;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Read MaxQuant output files and parse them for converting to mzQuantML format.
 *
 * @author Da Qi
 */
public class MaxquantFilesReader {

    private static final String EVIDENCE = "evidence.txt";
    private static final String PEPTIDES = "peptides.txt";
    private static final String PROTEINGROUPS = "proteingroups.txt";
    private static final String DESIGN = "experimentaldesigntemplate.txt";
    private static final String SUMMARY = "summary.txt";
    private final File evidenceFile;
    private final File peptidesFile;
    private final File proteinGroupsFile;
    private final File experimentalDesignTemplateFile;
    private final File summaryFile;
    private char separator;
    private BufferedReader br;
    private FileInputStream fis;
    private CSVReader csvReader;
    private List<String> assays;
    private List<String> experiments;
    private List<String> studyVars;
    private List<String> primeStudyVars;
    private List<String> peptides;
    private List<String> rawFiles;
    private Map<String, String> rawFileExpMap;
    private Map<String, String> assayRawFileMap;
    private TIntObjectMap<List<String>> evidenceMap;
    private Map<String, List<String>> primeStudyGroupMap;
    private Map<String, List<String>> studyGroupMap;
    private Map<String, List<String>> peptideToIntMap;
    private Map<String, List<String>> peptideToEvdIdsMap;
    private Map<String, List<String>> peptideToRatioMap;
    private Map<String, List<String>> peptideToProtMap;
    private Map<String, List<String>> proteinToPepMap;
    private TIntObjectMap<TDoubleList> proteinIntMap;
    private TIntObjectMap<String> majorityProteinIDMap; // take the first protein id from IDs
    private TIntObjectMap<TIntList> proteinUniqPepMap;
    private Boolean hasProteinGroupsFile = false;
    private Boolean isLabelFree = false;
    private int multiplicity;
    private int groupNum = 0;

    /**
     * Constructor of MaxquantFilesReader
     * This constructor takes five files and one char as parameter.
     * Five files refer to evidence.txt, peptides.txt, proteinGroups.txt, experimentalDesignTemplate.txt, summary.txt (the file names can be different).
     *
     * @param evidenceF                   evidence.txt file
     * @param peptidesF                   peptides.txt file
     * @param proteinGroupsF              proteinGrouops.txt file
     * @param experimentalDesignTemplateF experimentalDesignTemplate.txt file
     * @param summaryF                    summary.txt file
     * @param sep                         the separator used by input files
     *
     * @throws IOException io exception
     */
    public MaxquantFilesReader(File evidenceF,
                               File peptidesF,
                               File proteinGroupsF,
                               File experimentalDesignTemplateF,
                               File summaryF,
                               char sep)
            throws IOException {
        this.evidenceFile = evidenceF;
        this.peptidesFile = peptidesF;
        this.proteinGroupsFile = proteinGroupsF;
        this.experimentalDesignTemplateFile = experimentalDesignTemplateF;
        this.summaryFile = summaryF;
        this.separator = sep;

        readSummaryFile();

        readExperimentalDesignTemplateFile();

        readEvidenceFile();

        readProteinGroupsFile();

        readPeptidesFile();

        proteinToPepMap = mapTranspose(peptideToProtMap);
    }

    /**
     * Constructor of MaxquantFilesReader
     * This constructor takes five files as parameters.
     * Five files refer to evidence.txt, peptides.txt, proteinGroups.txt, experimentalDesignTemplate.txt, summary.txt (the file names can be different).
     * The files are tab-delimited by default.
     *
     * @param evidenceF                   evidence.txt file
     * @param peptidesF                   peptides.txt file
     * @param proteinGroupsF              proteinGrouops.txt file
     * @param experimentalDesignTemplateF experimentalDesignTemplate.txt file
     * @param summaryF                    summary.txt file
     *
     * @throws IOException io exception
     */
    public MaxquantFilesReader(File evidenceF,
                               File peptidesF,
                               File proteinGroupsF,
                               File experimentalDesignTemplateF,
                               File summaryF)
            throws IOException {
        this(evidenceF, peptidesF, proteinGroupsF, experimentalDesignTemplateF, summaryF, '\t');
    }

    /**
     * Constructor of MaxquantFilesReader
     * This constructor takes a folder and a char as parameter. The folder must contains five required files otherwise exception will be thrown.
     * Five files refer to evidence.txt, peptides.txt, proteinGroups.txt, experimentalDesignTemplate.txt, summary.txt (the file names MUST be the same case-insensitive).
     *
     * @param folderName the folder with input files
     * @param sep        the separator used by input files
     *
     * @throws IOException io exception
     */
    public MaxquantFilesReader(String folderName, char sep)
            throws IOException {
        this.separator = sep;

        File path = new File(folderName);
        String[] fileList = path.list();
        String[] requireList = {EVIDENCE, PEPTIDES, PROTEINGROUPS, DESIGN, SUMMARY};

        String parentPath = path.getPath();

        /**
         * check if the file folder contains all the required files
         */
        if (!containsAll(fileList, requireList)) {
            this.evidenceFile = null;
            this.proteinGroupsFile = null;
            this.summaryFile = null;
            this.peptidesFile = null;
            this.experimentalDesignTemplateFile = null;
            System.out.println("The selected folder doesn't contain one of the following files requried:");
            System.out.println("(" + EVIDENCE + ", " + PEPTIDES + ", " + PROTEINGROUPS + ", " + DESIGN + ", " + SUMMARY + ").");
        }
        else {

            this.evidenceFile = new File(parentPath + "//" + EVIDENCE);
            this.proteinGroupsFile = new File(parentPath + "//" + PROTEINGROUPS);
            this.summaryFile = new File(parentPath + "//" + SUMMARY);
            this.peptidesFile = new File(parentPath + "//" + PEPTIDES);
            this.experimentalDesignTemplateFile = new File(parentPath + "//" + DESIGN);

            /**
             * check if proteinGroups.txt exists in the path
             */
            List<String> fList = Arrays.asList(fileList);
            if (fList.contains(PROTEINGROUPS)) {
                hasProteinGroupsFile = true;
            }

            readSummaryFile();

            readExperimentalDesignTemplateFile();

            readEvidenceFile();

            readProteinGroupsFile();

            readPeptidesFile();

//            /*
//             * build a protein to peptide HashMap,
//             * with protein accession as key,
//             * and peptide sequence list as value
//             */
            proteinToPepMap = mapTranspose(peptideToProtMap);
        }

    }

    /**
     * Constructor of MaxquantFilesReader
     * This constructor takes a folder as parameter. The folder must contains five required files otherwise exception will be thrown.
     * Five files refer to evidence.txt, peptides.txt, proteinGroups.txt, experimentalDesignTemplate.txt, summary.txt (the file names MUST be the same case-insensitive).
     * The files are tab-delimited by default.
     *
     * @param folderName the folder with input files
     *
     * @throws IOException io exception
     */
    public MaxquantFilesReader(String folderName)
            throws IOException {
        this(folderName, '\t');
    }

    /**
     *
     * Read from summary.txt to find out the type of the experiment (e.g label-free or SILAC).
     * Set the value for multiplicity, isLabelFree flag. For label-free, multiplicity must be set to 0.
     */
    private void readSummaryFile()
            throws IOException {
        if (this.summaryFile == null) {
            //TODO
        }
        else {
            try {
                String nextLine[];
                multiplicity = 0;

                fis = new FileInputStream(this.summaryFile);
                br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                csvReader = new CSVReader(br, separator);

                nextLine = csvReader.readNext();
                if (nextLine != null) {
                    for (String nextLine1 : nextLine) {
                        if (nextLine1.contains("Labels")) {
                            multiplicity++;
                        }
                    }

                    // adjust multiplicity: 0 for label free; set isLabelFree flag
                    if (multiplicity == 1) {
                        multiplicity = 0;
                        isLabelFree = true;
                    }
                    else {
                        isLabelFree = false;
                    }
                }
                else {
                    throw new IllegalStateException("The file is empty.\n");
                }
            }
            catch (FileNotFoundException ex) {
                System.out.println("The " + this.summaryFile.getAbsolutePath() + " can not be found.");
                throw new RuntimeException("The " + this.summaryFile.getAbsolutePath() + " can not be found: " + ex.getMessage() + ".\n");
            }
        }

    }

    /**
     * Read from experimentalDesignTemplate.txt.
     * The methods will build list of assays, experiments, rawFiles, and studyVars;
     * map of rawFileExpMap, assayRawFileMap, primeStudyGroupMap, and studyGroupMap
     * In label free case, the primeStudyGroupMap and sutdyGroupMap are the same. They both contain entries of study variable to list of assay names.
     * In label based case (e.g. SILAC), the studyGroupMap contains more than primeStudyGroupMap.
     * The studyGroupMap contains entries of study variable to list of assay names not only from experimentalDesignTemplate.txt,
     * but also extra study variables based on label types.
     * Set groupNum to 1 if it is label free.
     */
    private void readExperimentalDesignTemplateFile()
            throws IOException {
        if (this.experimentalDesignTemplateFile == null) {
            // TODO
        }
        else {
            try {
                String[] nextLine;
                fis = new FileInputStream(this.experimentalDesignTemplateFile);
                br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                csvReader = new CSVReader(br, separator);

                /**
                 * Build the assay list and raw file list
                 */
                csvReader.readNext(); // Skip the title row
                assays = new ArrayList(); // List of AssayName
                experiments = new ArrayList();
                rawFiles = new ArrayList(); // List of rawFileName
                rawFileExpMap = new HashMap<>();
                assayRawFileMap = new HashMap<>();
                String assayName;
                String rawFileName;
                int expId = 0;
                while ((nextLine = csvReader.readNext()) != null) {
                    // build different assay lists for label-free or SILAC method
                    if (isLabelFree) {
                        assayName = nextLine[0]; // The first column is used as AssayName
                        rawFileName = nextLine[0] + ".raw"; // AssayName plus '.raw' suffix is used as rawFileName
                        assays.add(assayName);
                        assayRawFileMap.put(assayName, rawFileName);
                    }
                    else if (multiplicity == 2) {
                        // Each raw file contains two assays
                        rawFileName = nextLine[0] + ".raw";
                        assayName = nextLine[0] + "_Light";
                        assays.add(assayName);
                        assayRawFileMap.put(assayName, rawFileName);
                        assayName = nextLine[0] + "_Heavy";
                        assays.add(assayName);
                        assayRawFileMap.put(assayName, rawFileName);
                    }
                    else if (multiplicity == 3) {
                        // Each raw file contains three assays
                        rawFileName = nextLine[0] + ".raw";
                        assayName = nextLine[0] + "_Light";
                        assays.add(assayName);
                        assayRawFileMap.put(assayName, rawFileName);
                        rawFileName = nextLine[0] + ".raw";
                        assayName = nextLine[0] + "_Medium";
                        assays.add(assayName);
                        assayRawFileMap.put(assayName, rawFileName);
                        assayName = nextLine[0] + "_Heavy";
                        assays.add(assayName);
                        assayRawFileMap.put(assayName, rawFileName);
                    }
                    // This is very rare case when more than 2 labels are used.
                    else {
                        for (int i = 0; i < multiplicity; i++) {
                            rawFileName = nextLine[0] + ".raw";
                            assayName = nextLine[0] + "_" + i;
                            assays.add(assayName);
                            assayRawFileMap.put(assayName, rawFileName);
                        }
                    }
                    // end of build different assay lists for .....
                    if (nextLine[2].isEmpty()) {
                        // In the case when the third column has no value, a number for experiment is given for the raw file and its assay(s)
                        // This will be treat as label free experiment
                        experiments.add(String.valueOf(expId++));
                    }
                    else {
                        experiments.add(nextLine[2]);
                    }
                    rawFiles.add(nextLine[0] + ".raw");

                    //TODO: check if rawFileExpMap has been used anywhere else!
                    rawFileExpMap.put(nextLine[0], nextLine[2]);
                }
                csvReader.close();

                /**
                 * build a study variable group list and map if available
                 */
                primeStudyGroupMap = new HashMap<>(); // Same as the studyGroupMap for label free expriment.
                List<String> tempAssays;
                primeStudyVars = new ArrayList<>();
                for (int i = 0; i < experiments.size(); i++) {
                    String studyV;
                    int prefixLen = 0;
                    tempAssays = new ArrayList();
                    studyV = "";
                    for (int j = 0; j < experiments.size(); j++) {
                        if (!(i == j)) {
                            int tempLen = lengthOfPrefix((String) experiments.get(i), (String) experiments.get(j));
                            if ((tempLen > 0) && (prefixLen < tempLen)) {
                                prefixLen = tempLen; // find the longest prefix. e.g. 'test1', 'test2', 'test12' TODO: bugs to be fixed
                                studyV = ((String) experiments.get(i)).substring(0, prefixLen);
                                tempAssays.clear(); // clear previous assays if new longer prefix is found

                                // TODO: can this if/else if/else block be shortened?
                                if (isLabelFree) {
                                    tempAssays.add(assays.get(j));
                                }
                                else if (multiplicity == 2) {
                                    tempAssays.add(assays.get(j * multiplicity));
                                    tempAssays.add(assays.get(j * multiplicity + 1));
                                }
                                else {
                                    for (int n = 0; n < multiplicity; n++) {
                                        tempAssays.add(assays.get(j * multiplicity + n));
                                    }
                                }
                            }
                            else if ((prefixLen > 0) && (prefixLen == tempLen)) {
                                // TODO: can this if/else if/else block be shortened?
                                if (isLabelFree) {
                                    tempAssays.add(assays.get(j));
                                }
                                else if (multiplicity == 2) {
                                    tempAssays.add(assays.get(j * multiplicity));
                                    tempAssays.add(assays.get(j * multiplicity + 1));
                                }
                                else {
                                    for (int n = 0; n < multiplicity; n++) {
                                        tempAssays.add(assays.get(j * multiplicity + n));
                                    }
                                }
                            }
                        }
                    }
                    if (prefixLen == 0) {
                        studyV = (String) experiments.get(i);
                    }
                    if (isLabelFree) {
                        tempAssays.add(assays.get(i));
                    }
                    else if (multiplicity == 2) {
                        tempAssays.add(assays.get(i * multiplicity));
                        tempAssays.add(assays.get(i * multiplicity + 1));
                    }
                    else {
                        for (int n = 0; n < multiplicity; n++) {
                            tempAssays.add(assays.get(i * multiplicity + n));
                        }
                    }
                    if (primeStudyGroupMap.get(studyV) == null) {
                        primeStudyGroupMap.put(studyV, new ArrayList(tempAssays));
                    }
                    if (!primeStudyVars.contains(studyV)) {
                        primeStudyVars.add(studyV);
                    }
                }

                studyGroupMap = new HashMap(primeStudyGroupMap);
                //HashMap cloneStudyGroupMap = new HashMap();
                Map cloneStudyGroupMap = new HashMap(primeStudyGroupMap);
                if (!isLabelFree && multiplicity == 2) {
                    Iterator iStudy = cloneStudyGroupMap.keySet().iterator();
                    while (iStudy.hasNext()) {
                        String studyV = (String) iStudy.next();
                        String studyV_L = studyV + "_L";
                        String studyV_H = studyV + "_H";
                        List assListLight = new ArrayList();
                        List assListHeavy = new ArrayList();
                        List value = studyGroupMap.get(studyV);
                        Iterator iV = value.iterator();
                        while (iV.hasNext()) {
                            String assName = (String) iV.next();
                            if (assName.toLowerCase(Locale.ENGLISH).contains("light")) {
                                assListLight.add(assName);
                            }
                            else if (assName.toLowerCase(Locale.ENGLISH).contains("heavy")) {
                                assListHeavy.add(assName);
                            }
                        }
                        studyGroupMap.put(studyV_L, new ArrayList(assListLight));
                        studyGroupMap.put(studyV_H, new ArrayList(assListHeavy));
                    }
                }
                else {
                    // TODO: more than 2 label cases
                }
                studyVars = new ArrayList(studyGroupMap.keySet());

                /*
                 * multiplicity indicates the number of labels (e.g. heavy vs.
                 * light)
                 * number of condition groups is number of assay divided by
                 * multiplicity
                 * the intensity value of each assay location is show below
                 * (e.g. multiplicity=2, assay number=4):
                 * intensity of total assay + intensity of each label
                 * + intensity of total from group 1 + intensity of each labeled assay from group 1
                 * + intensity of total from group 2 + intensity of each labeled assay from group 2
                 * + and so on.
                 */
                if (multiplicity != 0) {
                    groupNum = primeStudyGroupMap.size();
                }
                else {
                    groupNum = 1;
                }
            }
            catch (FileNotFoundException ex) {
                System.out.println("The " + this.experimentalDesignTemplateFile.getAbsolutePath() + " can not be found.");
                throw new RuntimeException("The " + this.experimentalDesignTemplateFile.getAbsolutePath() + " can not be found: " + ex.getMessage() + ".\n");
            }
        }

    }

    /**
     * Read from evidence.txt.
     */
    private void readEvidenceFile()
            throws IOException {
        if (this.evidenceFile == null) {
            // TODO
        }
        else {
            try {
                fis = new FileInputStream(this.evidenceFile);
                br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                csvReader = new CSVReader(br, '\t');
                /*
                 * read the column title and acquire column number
                 */
                String[] nextLine;
                int posId = 0, posSeq = 0, posMod = 0, posProt = 0, posRaw = 0, posExp = 0, posChr = 0, posMz = 0, posRet = 0, posInt = 0;
                nextLine = csvReader.readNext();
                for (int i = 0; i < nextLine.length; i++) {
                    if (nextLine[i].equalsIgnoreCase("id")) {
                        posId = i;
                    }
                    else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("sequence")) {
                        posSeq = i;
                    }
                    else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("modifications")) {
                        posMod = i;
                    }
                    else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("leading proteins")) {
                        posProt = i;
                    }
                    else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("raw file")) {
                        posRaw = i;
                    }
                    else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("experiment")) {
                        posExp = i;
                    }
                    else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("charge")) {
                        posChr = i;
                    }
                    else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("m/z")) {
                        posMz = i;
                    }
                    else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("retention time")) {
                        posRet = i;
                    }
                    else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("intensity")) {
                        posInt = i;
                    }
                }

                /*
                 * read specific column in to an intermediate HashMap with "id" as key, an array list as value
                 */
                evidenceMap = new TIntObjectHashMap<>();

                while ((nextLine = csvReader.readNext()) != null) {
                    int key = Integer.parseInt(nextLine[posId]);
                    /*
                     * The positions for features are: (0)Sequence,
                     * (1)Modifications, (2)Leading Proteins,
                     * (3)Raw File, (4)Experiment, (5)Charge, (6)m/z, (7)Retention
                     * Time, (8)Intensity
                     * (9)Intensity L, (10)Intensity H (for 2 labels case)
                     */
                    List<String> valueList;
                    if (isLabelFree) {
                        String[] values = {nextLine[posSeq], nextLine[posMod], nextLine[posProt], nextLine[posRaw], nextLine[posExp],
                            nextLine[posChr], nextLine[posMz], nextLine[posRet], nextLine[posInt]};
                        valueList = Arrays.asList(values);
                    }
                    else {
                        String[] values = {nextLine[posSeq], nextLine[posMod], nextLine[posProt], nextLine[posRaw], nextLine[posExp],
                            nextLine[posChr], nextLine[posMz], nextLine[posRet], nextLine[posInt]};// nextLine[posInt + 1], nextLine[posInt + 2]};
                        valueList = Arrays.asList(values);
                        // Add intensity bunch
                        for (int i = 0; i < multiplicity; i++) {
                            valueList.add(nextLine[posInt + i + 1]);
                        }
                    }
                    evidenceMap.put(key, valueList);
                }
                csvReader.close();
            }
            catch (FileNotFoundException ex) {
                System.out.println("The " + this.evidenceFile.getAbsolutePath() + " can not be found. ");
                throw new RuntimeException("The " + this.evidenceFile.getAbsolutePath() + " can not be found: " + ex.getMessage() + ".\n");
            }
        }
    }

    /**
     * Read proteinGroups.txt.
     */
    private void readProteinGroupsFile()
            throws IOException {
        if (this.proteinGroupsFile == null) {
            // TODO
        }
        else {
            try {
                fis = new FileInputStream(this.proteinGroupsFile);
                br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                csvReader = new CSVReader(br, separator);
                String[] nextLine;
                int posMajProtIds = 0, posProtInt = 0, posUniqPep = 0;
                nextLine = csvReader.readNext();
                for (int i = 0; i < nextLine.length; i++) {
                    if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("majority protein ids")) {
                        posMajProtIds = i;
                    }
                    else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("intensity")) {
                        posProtInt = i;
                    }
                    else if (nextLine[i].toLowerCase(Locale.ENGLISH).contains("unique peptides")) {  // this is the position of the LAST column title containing "unique peptides";
                        posUniqPep = i;
                    }
                }
                // adjust posUniqPep to point one position AHEAD OF the first title containing "unique peptides"; 
                // this trick only works for Label free example as SILAC doesn't need to report "unique peptides" (not true, see Ian's data)
                if (multiplicity == 0) {
                    posUniqPep = posUniqPep - assays.size();
                }
                else {
                    posUniqPep = posUniqPep - groupNum;
                }

                majorityProteinIDMap = new TIntObjectHashMap<>();
                proteinIntMap = new TIntObjectHashMap<>();
                proteinUniqPepMap = new TIntObjectHashMap<>();
                int key = 0;
                while ((nextLine = csvReader.readNext()) != null) {
                    String majProtIds = nextLine[posMajProtIds];
                    String protId = majProtIds;
                    // Only take the first ID in Majority protein IDs as the protein id
                    if (majProtIds.contains(";")) {
                        int endIndex = majProtIds.indexOf(';');
                        protId = majProtIds.substring(0, endIndex);
                    }

                    majorityProteinIDMap.put(key, protId);

                    /*
                     * build a intensity list of each assay on each protein
                     * assay number comes from assays.size()
                     * first id of major protein ids as the key, intensity list as value
                     * The total intensity of each group or the whole assay is not recorded
                     */
                    TDoubleList intList = new TDoubleArrayList();

                    if (isLabelFree) {
                        for (int i = 0; i < assays.size(); i++) {
                            if (NumberUtils.isNumber(nextLine[posProtInt + i + 1])) {
                                intList.add(Double.parseDouble(nextLine[posProtInt + i + 1]));
                            }
                            else {
                                throw new RuntimeException("There is non number cell in one of the intensity columns.");
                            }
                        }
                    }
                    else {
                        int i = 0; // i is the count of assays
                        for (int j = 0; j < groupNum; j++) {  // j is the count of groups
                            int k = 0; // k is the count of labels in each group
                            while (k < multiplicity) {
                                if (groupNum == 1) {
                                    if (NumberUtils.isNumber(nextLine[posProtInt + multiplicity + (i + 1) + j])) {
                                        intList.add(Double.parseDouble(nextLine[posProtInt + multiplicity + (i + 1) + j]));
                                    }
                                    else {
                                        throw new RuntimeException("There is non number cell in one of the intensity columns.");
                                    }
                                }
                                else if (NumberUtils.isNumber(nextLine[posProtInt + multiplicity + (i + 1) + (j + 1)])) {
                                    intList.add(Double.parseDouble(nextLine[posProtInt + multiplicity + (i + 1) + (j + 1)]));
                                }
                                else {
                                    throw new RuntimeException("There is non number cell in one of the intensity columns.");
                                }
                                i++;
                                k++;
                            }
                        }
                    }
                    proteinIntMap.put(key, intList);

                    /*
                     * build a uniqe peptides list of each assay on each protein
                     * this is ONLY for label-free examples, not included in
                     * SILAC examples
                     * first id of major protein ids as the key, unique peptide
                     * list as value
                     */
                    if (isLabelFree) {
                        TIntList uniqPepList = new TIntArrayList();
                        for (int j = 0; j < assays.size(); j++) {
                            if (NumberUtils.isNumber(nextLine[posUniqPep + j + 1])) {
                                uniqPepList.add(Integer.parseInt(nextLine[posUniqPep + j + 1]));
                            }
                            else {
                                throw new RuntimeException("There is non number cell in one of the unique peptides columns.");
                            }
                        }
                        proteinUniqPepMap.put(key, uniqPepList);
                    }
                    key++;
                }
            }
            catch (FileNotFoundException ex) {
                System.out.println("The " + this.proteinGroupsFile.getAbsolutePath() + " can not be found.");
                throw new RuntimeException("The " + this.proteinGroupsFile.getAbsolutePath() + " can not be found: " + ex.getMessage() + ".\n");
            }
        }
    }

    /**
     * Read from peptides.txt.
     */
    private void readPeptidesFile()
            throws IOException {
        try {
            fis = new FileInputStream(this.peptidesFile);
            br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            csvReader = new CSVReader(br, separator);
            String[] nextLine;

            /*
             * get the column number for required fields
             */
            nextLine = csvReader.readNext();
            int posEvdId = 0;
            int posPepSeq = 0;
            int posPepInt = 0;
            int posProt = 0;

            //Maxquant only reports H/L ratio
            int[] posPepRatio = new int[primeStudyVars.size()];

            //titles of ratio are stored in String[] titlePepRatio
            String[] titlePepRatio = new String[primeStudyVars.size()];

            /*
             * Initialise peptideToRatioMap because
             * the first entry is used to record ratio title,
             * such as "Ratio H/L config_1", set key as "0"
             */
            peptideToRatioMap = new HashMap<>();

            for (int i = 0; i < nextLine.length; i++) {
                if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("evidence ids")) {
                    posEvdId = i;
                }
                else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("sequence")) {
                    posPepSeq = i;
                }
                else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("proteins")) {
                    posProt = i;
                }
                else if (nextLine[i].toLowerCase(Locale.ENGLISH).contains("h/l")) {
                    for (int k = 0; k < primeStudyVars.size(); k++) {
                        if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("ratio h/l " + (String) primeStudyVars.get(k))) {
                            posPepRatio[k] = i;
                            titlePepRatio[k] = nextLine[i];
                        }
                    }
                }
                else if (nextLine[i].toLowerCase(Locale.ENGLISH).equals("intensity")) {
                    posPepInt = i;
                    //break;
                }
            }

            peptideToRatioMap.put("0", Arrays.asList(titlePepRatio));

            /*
             * A peptide sequence to feature ids HashMap<String, ArrayList>
             * Using peptide sequence as key. It is not checked yet for every peptides.txt if it has unique peptide sequence in each row.
             */
            peptideToEvdIdsMap = new HashMap<>();
            peptideToIntMap = new HashMap<>();
            peptideToProtMap = new HashMap<>();
            peptides = new ArrayList();
            while ((nextLine = csvReader.readNext()) != null) {

                /*
                 * build a peptide to evidence ids map, with peptide sequence as
                 * key, evidence id list as value Map<String, List<String>>
                 */
                String key = nextLine[posPepSeq];
                //if (!nextLine[posEvdId].isEmpty()) {
                String[] evidenceIds = nextLine[posEvdId].split(";");
                List<String> evidenceIdList = Arrays.asList(evidenceIds);
                peptideToEvdIdsMap.put(key, new ArrayList(evidenceIdList));
                //}
                /*
                 * build a intensity list of each assay on each peptide
                 * assay number comes from assays.size()
                 * peptide sequence as key, intensity list as value
                 */
                List intList = new ArrayList();

                if (isLabelFree) {
                    for (int i = 0; i < assays.size(); i++) {
                        intList.add(nextLine[posPepInt + i + 1]);
                    }
                }
                else {
                    int i = 0; // i is the count of assays
                    for (int j = 0; j < groupNum; j++) {  // j is the count of groups
                        int k = 0; // k is the count of labels in each group
                        while (k < multiplicity) {
                            if (groupNum == 1) {
                                intList.add(nextLine[posPepInt + multiplicity + (i + 1) + j]);
                            }
                            else {
                                intList.add(nextLine[posPepInt + multiplicity + (i + 1) + (j + 1)]);
                            }
                            i++;
                            k++;
                        }
                    }
                }

                peptideToIntMap.put(key, new ArrayList(intList));

                /*
                 * build a peptide sequence to ratio list HashMap
                 * peptide sequence as key, ratio list as value
                 * The first entry used to record ratio title,
                 * such as "Ratio H/L config_1", set key as "0"
                 */
                if (!isLabelFree) {
                    List<String> ratioList = peptideToRatioMap.get(key);
                    if (ratioList == null) {
                        ratioList = new ArrayList<>();
                        for (int i = 0; i < posPepRatio.length; i++) {
                            // TODO: treat empty cell as ZERO, correct?
                            String ratioValue = (nextLine[posPepRatio[i]].isEmpty() ? "0" : nextLine[posPepRatio[i]]);
                            ratioList.add(ratioValue);
                        }
                    }
                    peptideToRatioMap.put(key, ratioList);
                }

                /*
                 * build a peptide sequence to protein accessions HashMap
                 * peptide sequence as key, protein accession list as value
                 */
                String[] proteins = nextLine[posProt].split(";");
                List<String> proteinList = Arrays.asList(proteins);
                peptideToProtMap.put(key, new ArrayList(proteinList));

                /*
                 * build a peptide list
                 */
                peptides.add(key);
            }
            csvReader.close();
        }
        catch (FileNotFoundException ex) {
            System.out.println("The " + this.peptidesFile.getAbsolutePath() + " can not be found.");
            throw new RuntimeException("The " + this.peptidesFile.getAbsolutePath() + " can not be found: " + ex.getMessage() + ".\n");
        }

    }

    /**
     * Get assay list.
     *
     * @return assay list
     */
    public List<String> getAssayList() {
        return this.assays;
    }

    /**
     * Get map of raw file name to experiment name.
     *
     * @return Map
     */
    public Map getRawFileExpMap() {
        return this.rawFileExpMap;
    }

    /**
     * Get prime study variable list.
     *
     * @return list
     */
    public List<String> getPrimeStudyVariableList() {
        return this.primeStudyVars;
    }

    /**
     * Get study variable list.
     *
     * @return list
     */
    public List<String> getStudyVariableList() {
        return this.studyVars;
    }

    /**
     * Get map of prime study group name to study variable list.
     *
     * @return Map
     */
    public Map getPrimeStudyGroupMap() {
        return this.primeStudyGroupMap;
    }

    /**
     * Get map of study group name to assay list.
     *
     * @return Map
     */
    public Map getStudyGroupMap() {
        return this.studyGroupMap;
    }

    /**
     * Get peptide list.
     *
     * @return peptide list
     */
    public List getPeptideList() {
        return this.peptides;
    }

    /**
     * Get raw file name list.
     *
     * @return raw file name list
     */
    public List getRawFileList() {
        return this.rawFiles;
    }

    /**
     * Get map of assay name to raw file name.
     *
     * @return Map
     */
    public Map getAssayRawFileMap() {
        return this.assayRawFileMap;
    }

    /**
     * Get map of index to a list of various values from evidence.txt.
     *
     * @return TIntObjectMap<List<String>>
     */
    public TIntObjectMap<List<String>> getEvidenceMap() {
        return this.evidenceMap;
    }

    /**
     * Get map of peptide sequence to evidence index.
     *
     * @return Map
     */
    public Map getPeptideEvidenceIdsMap() {
        return this.peptideToEvdIdsMap;
    }

    /**
     * Get map of peptide sequence to intensity.
     *
     * @return Map
     */
    public Map getPeptideIntensityMap() {
        return this.peptideToIntMap;
    }

    /**
     * Get map of peptide sequence to ratio.
     *
     * @return Map
     */
    public Map getPeptideRatioMap() {
        return this.peptideToRatioMap;
    }

    /**
     * Get map of index to list of protein intensity value.
     *
     * @return TIntObjectMap<TDoubleList>
     */
    public TIntObjectMap<TDoubleList> getProteinIntensityMap() {
        return this.proteinIntMap;
    }

    /**
     * Get map of protein id to list of unique peptide ids.
     *
     * @return TIntObjectMap<TIntList>
     */
    public TIntObjectMap<TIntList> getProteinUniquePeptiedsMap() {
        return this.proteinUniqPepMap;
    }

    /**
     * Get map of protein to list of peptides.
     *
     * @return Map
     */
    public Map getProteinPeptidesMap() {
        return this.proteinToPepMap;
    }

    /**
     * Checks if the input folder contains the proteingroup.txt file.
     *
     * @return boolean value
     */
    public Boolean hasProteinGroupsFile() {
        return this.hasProteinGroupsFile;
    }

    /**
     * Checks if the input files are from label-free experiment or not.
     *
     * @return boolean value
     */
    public Boolean isLabelFree() {
        return this.isLabelFree;
    }

    /**
     * Get number of label (for label free, the number is 0).
     *
     * @return the number of label
     */
    public int getLabelNumber() {
        return this.multiplicity;
    }

    private boolean containsAll(String[] list, String[] require) {
        boolean contain = true;
        List<String> aList = Arrays.asList(list);
        List<String> aListLowerCase = new ArrayList();
        for (String a : aList) {
            aListLowerCase.add(a.toLowerCase(Locale.ENGLISH));
        }
        for (int i = 0; i < require.length; i++) {

            if (!aListLowerCase.contains(require[i])) {
                contain = false;
                break;
            }
        }
        return contain;
    }

    /**
     * Get the length of same prefix string between two strings.
     * For example: comparing two string 'fool' and 'foods', the return will be 3. 'fool' and 'wools' will return 0.
     *
     * @param a first input string
     * @param b second input string
     *
     * @return the length of same prefix between two strings.
     */
    private int lengthOfPrefix(String a, String b) {
        // Compare length is based on the shortest length of two strings.
        int length = (a.length() < b.length()) ? a.length() : b.length();
        int len = 1;
        if ((length == 1) && (a.charAt(0) == b.charAt(0))) {
            len = 1;
        }
        else if ((a.charAt(0) != b.charAt(0))) {
            len = 0;
        }
        else {
            len = len + lengthOfPrefix(a.substring(1), b.substring(1));
        }
        return len;
    }

    /*
     * transpose a HashMap<String, String> to a HashMap<String,
     * ArrayList<String>>
     */
    private Map<String, List<String>> mapTranspose(
            Map<String, List<String>> m) {
        Map<String, List<String>> mTr = new HashMap<>();
        Iterator iM = m.entrySet().iterator();
        while (iM.hasNext()) {
            Map.Entry<String, ArrayList<String>> entry = (Map.Entry<String, ArrayList<String>>) iM.next();
            String key = entry.getKey();
            ArrayList val = entry.getValue();
            Iterator iVal = val.iterator();
            while (iVal.hasNext()) {
                String s = (String) iVal.next();
                List<String> aL = mTr.get(s);
                if (aL == null) {
                    aL = new ArrayList<>();
                    mTr.put(s, aL);
                }
                aL.add(key);
            }

        }
        return mTr;
    }

    /**
     * Get map of majorirty protein id.
     *
     * @return TIntObjectMap<String>
     */
    public TIntObjectMap<String> getMajorityProteinIDMap() {
        return majorityProteinIDMap;
    }

}
