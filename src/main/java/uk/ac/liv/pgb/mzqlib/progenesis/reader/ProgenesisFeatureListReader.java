
package uk.ac.liv.pgb.mzqlib.progenesis.reader;

import au.com.bytecode.opencsv.CSVReader;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TDoubleObjectMap;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntDoubleProcedure;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.TDoubleSet;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

/**
 *
 * @author Da Qi
 */
public class ProgenesisFeatureListReader implements Closeable {

    private BufferedReader br;
    //
    // The title header rows  in feature list file.
    private static final int ROW1 = -2; // 1st row
    private static final int ROW2 = -1; // 2nd row
    private static final int ROW3 = 0;  // 3rd row
    //
    private int normAbStart = 0;
    private int rawAbStart = 0;
    private int intensityStart = 0;
    private int retTimeStart = 0;
    private int proteinStart = 0;
    private int peptideStart = 0;
    private int chargePos = 0;
    private int mzPos = 0;
    private int masterRTPos = 0;
    private int useInQuantPos = 0;
    private int rtWinPos = 0;
    private int scorePos = 0;
    private int modPos = 0;
    private int assayNum = 1;
    private List<String> stuVarList = new ArrayList<>();
    private List<String> assayList = new ArrayList<>();

    private TIntObjectMap<String> indexMap = new TIntObjectHashMap<>();
    private TIntObjectMap<TDoubleList> normalizedAbundanceMap
            = new TIntObjectHashMap<>();
    private TIntObjectMap<TDoubleList> rawAbundanceMap
            = new TIntObjectHashMap<>();
    private TIntObjectMap<TDoubleList> intensityMap = new TIntObjectHashMap<>();
    private TIntObjectMap<TDoubleList> retentionTimeMap
            = new TIntObjectHashMap<>();
    private TIntDoubleMap mzMap = new TIntDoubleHashMap();
    private TIntIntMap chargeMap = new TIntIntHashMap();
    private Map<String, Set<String>> studyGroupMap = new HashMap<>();
    private TDoubleObjectMap<TIntSet> masterRetenTimeMap
            = new TDoubleObjectHashMap<>();
    private TIntDoubleMap masterRetenTimeMapWithDuplicate
            = new TIntDoubleHashMap();
    private Map<String, TIntSet> proteinMap = new HashMap<>();
    private TIntObjectMap<String> proteinMapWithDuplicate
            = new TIntObjectHashMap<>();
    private Map<String, TIntSet> peptideMap = new HashMap<>();
    private TIntObjectMap<String> peptideMapWithDuplicate
            = new TIntObjectHashMap<>();
    private TIntObjectMap<String[]> completeMap = new TIntObjectHashMap<>();
    private TIntObjectMap<Boolean> useInQuantMap = new TIntObjectHashMap<>();
    private TIntDoubleMap rtWinMap = new TIntDoubleHashMap();
    private TIntDoubleMap scoreMap = new TIntDoubleHashMap();
    private TIntObjectMap<String> modificationMap = new TIntObjectHashMap<>();
    //
    private String headingRow2 = "";
    private String headingRow3 = "";

    /**
     * Constructor of ProgenesisFeatureListReader.
     *
     * @param rd        input Reader
     * @param separator separator of input file
     *
     * @throws IOException io exceptions.
     */
    public ProgenesisFeatureListReader(final Reader rd, final char separator)
            throws IOException {
        br = new BufferedReader(rd);

        try (CSVReader reader = new CSVReader(br, separator)) {
            String nextLine[];

            // Read the first line of the csv file
            nextLine = reader.readNext();
            completeMap.put(ROW1, nextLine);

            for (int i = 0; i < nextLine.length; i++) {
                if (nextLine[i].toLowerCase(Locale.ENGLISH).equals(
                        FeatureListHeaders.NORMALIZED_ABUNDANCE)) {
                    normAbStart = i;
                } else if (nextLine[i].toLowerCase(Locale.ENGLISH).contains(
                        FeatureListHeaders.RAW_ABUNDANCE)) {
                    rawAbStart = i;
                } else if (nextLine[i].toLowerCase(Locale.ENGLISH).contains(
                        FeatureListHeaders.INTENSITY)) {
                    intensityStart = i;
                } else if (nextLine[i].toLowerCase(Locale.ENGLISH).contains(
                        FeatureListHeaders.SAMPLE_RETENTION_TIME)) {    // Column of 'Sample retention time (min)'
                    retTimeStart = i;
                    break;
                }
            }

            // get the number of assay
            if (normAbStart != 0) {
                assayNum = getAssayNumber(normAbStart, nextLine);
            } else if (rawAbStart != 0) {
                assayNum = getAssayNumber(rawAbStart, nextLine);
            } else {
                throw new IllegalStateException(
                        "No abundance data was found in peptide or feature file! Please check!");
            }

            // Read the second line to get study variable list
            nextLine = reader.readNext();
            completeMap.put(ROW2, nextLine);

            //TODO: investigate int[200]
            int[] stuVarPosition = new int[300];
            int m = 0;
            if (normAbStart != 0) {
                for (int i = normAbStart; i < (normAbStart + assayNum); i++) {
                    if (!nextLine[i].isEmpty()) {
                        stuVarList.add(nextLine[i].trim().replace(" ", "_"));
                        stuVarPosition[m] = i;
                        m++;
                    }
                    headingRow2 = headingRow2 + nextLine[i] + ",";
                    stuVarPosition[m] = i + 1;
                }
            } else if (rawAbStart != 0) {
                for (int i = rawAbStart; i < (rawAbStart + assayNum); i++) {
                    if (!nextLine[i].isEmpty()) {
                        stuVarList.add(nextLine[i].trim().replace(" ", "_"));
                        stuVarPosition[m] = i;
                        m++;
                    }
                    headingRow2 = headingRow2 + nextLine[i] + ",";
                    stuVarPosition[m] = i + 1;
                }
            }

            // Read the third line to get assay list
            nextLine = reader.readNext();
            completeMap.put(ROW3, nextLine);

            if (normAbStart != 0) {
                storeHeadingRow3(normAbStart, nextLine);
            } else if (rawAbStart != 0) {
                storeHeadingRow3(rawAbStart, nextLine);
            }

            for (int i = 0; i < nextLine.length; i++) {
                String lowerCaseHeader = nextLine[i].trim().toLowerCase(
                        Locale.ENGLISH);

                if (lowerCaseHeader.equals(FeatureListHeaders.RETENTION_TIME)) {
                    masterRTPos = i;
                } else if (lowerCaseHeader.equals(FeatureListHeaders.CHARGE)) {
                    chargePos = i;
                } else if (lowerCaseHeader.equals(FeatureListHeaders.M_Z)) {
                    mzPos = i;
                } else if (lowerCaseHeader.equals(
                        FeatureListHeaders.USE_IN_QUANT)) {
                    useInQuantPos = i;
                } else if (lowerCaseHeader.equals(
                        FeatureListHeaders.RETENTION_TIME_WINDOW)) {
                    rtWinPos = i;
                } else if (lowerCaseHeader.equals(FeatureListHeaders.SCORE)) {
                    scorePos = i;
                } else if (lowerCaseHeader.contains(
                        FeatureListHeaders.MODIFICATIONS)) {
                    modPos = i;
                } else if (lowerCaseHeader.equals(FeatureListHeaders.PROTEIN)
                        || lowerCaseHeader.equals(FeatureListHeaders.ACCESSION)) {
                    proteinStart = i;
                } else if (lowerCaseHeader.equals(FeatureListHeaders.SEQUENCE)) {
                    peptideStart = i;
                }
            }

            if (masterRTPos == 0 || chargePos == 0 || mzPos == 0
                    || scorePos == 0 || modPos == 0
                    || proteinStart == 0 || peptideStart == 0) {
                throw new IllegalStateException(
                        "The third row of progenesis feature file should contain the header not the data! Please check!");
            }

            for (int i = 0; i < stuVarList.size(); i++) {
                String key = (String) stuVarList.get(i);
                Set<String> value = new HashSet<>();
                for (int k = stuVarPosition[i]; k < stuVarPosition[i + 1]; k++) {
                    value.add(nextLine[k]);
                }
                studyGroupMap.put(key, value);
            }

            TDoubleList currentV = new TDoubleArrayList();
            int currentK = 1;
            while ((nextLine = reader.readNext()) != null) {

                // Build indexMap
                indexMap.put(currentK, nextLine[0]);

                // Build completeMap
                completeMap.put(currentK, nextLine);

                // Build normalized abundance hashmap
                if (normAbStart != 0) {
                    for (int i = normAbStart; i < (normAbStart + assayNum); i++) {
                        if (!NumberUtils.isNumber(nextLine[i])) {
                            currentV.add(0.0);
                        } else {
                            currentV.add(Double.parseDouble(nextLine[i]));
                        }
                    }
                    normalizedAbundanceMap.put(currentK, new TDoubleArrayList(
                                               currentV));
                    currentV.clear();
                }

                // Build raw abundance hashmap
                if (rawAbStart != 0) {
                    for (int i = rawAbStart; i < (rawAbStart + assayNum); i++) {
                        if (!NumberUtils.isNumber(nextLine[i])) {
                            currentV.add(0.0);
                        } else {
                            currentV.add(Double.parseDouble(nextLine[i]));
                        }
                    }
                    rawAbundanceMap.
                            put(currentK, new TDoubleArrayList(currentV));
                    currentV.clear();
                }

                // Build intensity hashmap
                if (intensityStart != 0) {
                    for (int i = intensityStart; i < (intensityStart + assayNum);
                            i++) {
                        if (!NumberUtils.isNumber(nextLine[i])) {
                            currentV.add(0.0);
                        } else {
                            currentV.add(Double.parseDouble(nextLine[i]));
                        }
                    }
                    intensityMap.put(currentK, new TDoubleArrayList(currentV));
                    currentV.clear();
                }

                // Build Sample retention time hashmap
                if (retTimeStart != 0) {
                    for (int i = retTimeStart; i < (retTimeStart + assayNum);
                            i++) {
                        if (!NumberUtils.isNumber(nextLine[i])) {
                            currentV.add(0.0);
                        } else {
                            currentV.add(Double.parseDouble(nextLine[i]));
                        }
                    }
                    retentionTimeMap.put(currentK,
                                         new TDoubleArrayList(currentV));
                    currentV.clear();
                }

                // Build mass over charge hashmap
                if (NumberUtils.isNumber(nextLine[mzPos])) {
                    mzMap.put(currentK, Double.parseDouble(nextLine[mzPos]));
                }

                // Build charge hashmap
                if (NumberUtils.isNumber(nextLine[chargePos])) {
                    chargeMap.put(currentK, Integer.
                                  parseInt(nextLine[chargePos]));
                }

                /*
                 * Build mater retention time hashmap with duplicated values,
                 ** key is matched feature and value is the retention time.
                 ** This will be used in getMasterRetenTimeList() to return a
                 * list of
                 ** non duplicated retention time values; will also be used in
                 ** getMasterRetenTimeMap() to return a HashMap<String,
                 * ArrayList<String>>.
                 **
                 */
                if (NumberUtils.isNumber(nextLine[masterRTPos])) {
                    masterRetenTimeMapWithDuplicate.put(currentK, Double.
                                                        parseDouble(
                                                                nextLine[masterRTPos]));
                }

                //filter out the non-protein row
                if (!nextLine[proteinStart].isEmpty()) {
                    proteinMapWithDuplicate.
                            put(currentK, nextLine[proteinStart]);
                }
                //filter out the non-peptide row
                if (!nextLine[peptideStart].isEmpty()) {
                    peptideMapWithDuplicate.
                            put(currentK, nextLine[peptideStart]);
                }

                // Build use in quantitation hashmap
                if (useInQuantPos != 0) {
                    useInQuantMap.put(currentK, Boolean.valueOf(
                                      nextLine[useInQuantPos]));
                }

                // build retention time window hashmap
                if (NumberUtils.isNumber(nextLine[rtWinPos])) {
                    rtWinMap.put(currentK, Double.
                                 parseDouble(nextLine[rtWinPos]));
                }

                // build mascot ion score hashmap
                if (NumberUtils.isNumber(nextLine[scorePos])) {
                    scoreMap.put(currentK, Double.
                                 parseDouble(nextLine[scorePos]));
                }

                // build modification hashmap
                if (!nextLine[modPos].isEmpty()) {
                    modificationMap.put(currentK, nextLine[modPos]);
                }

                currentK++;
            }
        }
    }

    /**
     * Constructor of ProgenesisFeatuerListReader.
     *
     * @param rd input Reader
     *
     * @throws IOException io exceptions.
     */
    public ProgenesisFeatureListReader(final Reader rd)
            throws IOException {
        this(rd, ',');
    }

    /**
     * Get IndexMap.
     *
     * @return index map.
     */
    public final TIntObjectMap<String> getIndexMap() {
        return indexMap;
    }

    /**
     * Get normalized abundance map.
     *
     * @return normalized abundance map.
     */
    public final TIntObjectMap<TDoubleList> getNormalizedAbundanceMap() {
        return normalizedAbundanceMap;
    }

    /**
     * Get raw abundance map.
     *
     * @return raw abundance map.
     */
    public final TIntObjectMap<TDoubleList> getRawAbundanceMap() {
        return rawAbundanceMap;
    }

    /**
     * Get intensity map.
     *
     * @return intensity map.
     */
    public final TIntObjectMap<TDoubleList> getIntensityMap() {
        return intensityMap;
    }

    /**
     * Get retention time map.
     *
     * @return retention time map.
     */
    public final TIntObjectMap<TDoubleList> getRetentionTimeMap() {
        return retentionTimeMap;
    }

    /**
     * Get retention time window map.
     *
     * @return retention time window map.
     */
    public final TIntDoubleMap getRtWindowMap() {
        return rtWinMap;
    }

    /**
     * Get score map.
     *
     * @return score map.
     */
    public final TIntDoubleMap getScoreMap() {
        return scoreMap;
    }

    /**
     * Get modification map.
     *
     * @return modification map.
     */
    public final TIntObjectMap<String> getModificationMap() {
        return modificationMap;
    }

    /**
     * Get mz map.
     *
     * @return mz map.
     */
    public final TIntDoubleMap getMassOverChargeMap() {
        return mzMap;
    }

    /**
     * Get charge map.
     *
     * @return charge map.
     */
    public final TIntIntMap getChargeMap() {
        return chargeMap;
    }

    /**
     * Get study variable list.
     *
     * @return study variable list.
     */
    public final List<String> getStudyVariableList() {
        return stuVarList;
    }

    /**
     * Get assay list.
     *
     * @return assay list.
     */
    public final List<String> getAssayList() {
        return assayList;
    }

    /**
     * Get study group map.
     *
     * @return study group map.
     */
    public final Map<String, Set<String>> getStudyGroupMap() {
        return studyGroupMap;
    }

    /**
     * Get master retention time map.
     *
     * @return master retention time map.
     */
    public final TDoubleObjectMap<TIntSet> getMasterRetenTimeMap() {

        masterRetenTimeMapWithDuplicate.forEachEntry(new TIntDoubleProcedure() {

            @Override
            public boolean execute(final int key, final double value) {
                TIntSet mf = masterRetenTimeMap.get(value);
                if (mf == null) {
                    mf = new TIntHashSet();
                    masterRetenTimeMap.put(value, mf);
                }
                mf.add(key);
                return true;
            }

        });
        return masterRetenTimeMap;
    }

    /**
     * Get set of master retention time.
     *
     * @return set of mater retention time.
     */
    public final TDoubleSet getMasterRetenTimeList() {
        return masterRetenTimeMap.keySet();
    }

    /**
     * Get protein map.
     *
     * @return protein map.
     */
    public final Map<String, TIntSet> getProteinMap() {

        proteinMapWithDuplicate.forEachEntry(new TIntObjectProcedure<String>() {

            @Override
            public boolean execute(final int key, final String accession) {

                TIntSet idSet = proteinMap.get(accession);
                if (idSet == null) {
                    idSet = new TIntHashSet();
                    proteinMap.put(accession, idSet);
                }
                idSet.add(key);
                return true;
            }

        });
        return proteinMap;
    }

    /**
     * Get protein list.
     *
     * @return protein list.
     */
    public final Set getProteinList() {
        return proteinMap.keySet();
    }

    /**
     * Get protein to peptides map.
     *
     * @return protein to peptides map.
     */
    public final Map<String, Set<String>> getProteinPeptidesMap() {
        final Map<String, Set<String>> proteinPeptidesMap = new HashMap<>();

        proteinMapWithDuplicate.forEachEntry(new TIntObjectProcedure<String>() {

            @Override
            public boolean execute(final int id, final String accession) {

                Set<String> pepSeqSet = proteinPeptidesMap.get(accession);
                if (pepSeqSet == null) {
                    pepSeqSet = new HashSet<>();
                    proteinPeptidesMap.put(accession, pepSeqSet);
                }
                String pepSeq = peptideMapWithDuplicate.get(id);
                if (!pepSeq.isEmpty()) {
                    pepSeqSet.add(pepSeq);
                }
                return true;
            }

        });
        return proteinPeptidesMap;
    }

    /**
     * Get peptide to proteins map.
     *
     * @return peptide to proteins map.
     */
    public final Map<String, Set<String>> getPeptideProteinsMap() {
        final Map<String, Set<String>> peptideProteinsMap = new HashMap<>();

        peptideMapWithDuplicate.forEachEntry(new TIntObjectProcedure<String>() {

            @Override
            public boolean execute(final int id, final String pepSeq) {
                Set<String> protAccSet = peptideProteinsMap.get(pepSeq);
                if (protAccSet == null) {
                    protAccSet = new HashSet<>();
                    peptideProteinsMap.put(pepSeq, protAccSet);
                }
                String protAcc = proteinMapWithDuplicate.get(id);
                if (!protAcc.isEmpty()) {
                    protAccSet.add(protAcc);
                }
                return true;
            }

        });
        return peptideProteinsMap;
    }

    /**
     * Get peptide map.
     *
     * @return peptide map.
     */
    public final Map<String, TIntSet> getPeptideMap() {

        peptideMapWithDuplicate.forEachEntry(new TIntObjectProcedure<String>() {

            @Override
            public boolean execute(final int id, final String pepSeq) {
                TIntSet idSet = peptideMap.get(pepSeq);
                if (idSet == null) {
                    idSet = new TIntHashSet();
                    peptideMap.put(pepSeq, idSet);
                }
                idSet.add(id);
                return true;
            }

        });
        return peptideMap;
    }

    /**
     * Get peptide list.
     *
     * @return peptide list.
     */
    public final Set<String> getPeptideList() {
        return peptideMap.keySet();
    }

    /**
     * Get peptide duplicate map.
     *
     * @return peptide duplicate map.
     */
    public final TIntObjectMap<String> getPeptideDuplicateMap() {
        return peptideMapWithDuplicate;
    }

    /**
     * Get protein duplicate map.
     *
     * @return protein duplicate map.
     */
    public final TIntObjectMap<String> getProteinDuplicateMap() {
        return proteinMapWithDuplicate;
    }

    /**
     * Get master retention time duplicate map.
     *
     * @return master retention time duplicate map.
     */
    public final TIntDoubleMap getMasterRTDuplicateMap() {
        return masterRetenTimeMapWithDuplicate;
    }

    /**
     * Get use in quant map.
     *
     * @return use in quant map.
     */
    public final TIntObjectMap<Boolean> getUseInQuantMap() {
        return useInQuantMap;
    }

    /**
     * Get complete map.
     * The map contain all the records from feature list file.
     *
     * @return complete map.
     */
    public final TIntObjectMap<String[]> getCompleteMap() {
        return completeMap;
    }

    /**
     * Get heading row 2.
     *
     * @return heading row 2.
     */
    public final String getHeadingRow2() {
        return headingRow2;
    }

    /**
     * Get heading row 3.
     *
     * @return heading row 3.
     */
    public final String getHeadingRow3() {
        return headingRow3;
    }

    /**
     * Get assay number.
     *
     * @return assay number.
     */
    public final int getAssayNumber() {
        return assayNum;
    }

    /**
     * Get multiple column map.
     *
     * @param startCol start column position.
     * @param endCol   end column position.
     *
     * @return multiple column map.
     */
    public final TIntObjectMap<List<String>> getMultipleColumn(
            final int startCol,
            final int endCol) {
        final TIntObjectMap<List<String>> retMap = new TIntObjectHashMap<>();

        completeMap.forEachEntry(new TIntObjectProcedure<String[]>() {

            @Override
            public boolean execute(final int id, final String[] values) {
                if (!isFirstThreeRow(id)) {
                    retMap.put(id, getArrayPart(values, startCol, endCol));
                }
                return true;
            }

        });
        return retMap;
    }

    /**
     * Get single column map.
     *
     * @param col column index.
     *
     * @return single column map.
     */
    public final TIntObjectMap<String> getSingleColumn(final int col) {

        final TIntObjectMap<String> retMap = new TIntObjectHashMap<>();

        completeMap.forEachEntry(new TIntObjectProcedure<String[]>() {

            @Override
            public boolean execute(final int id, final String[] values) {
                if (!isFirstThreeRow(id)) {
                    retMap.put(id, values[col]);
                }
                return true;
            }

        });
        return retMap;
    }

    //private function
    // isFirstThreeRow determine whether an entry is from the title row
    private boolean isFirstThreeRow(final int i) {
        boolean b = false;
        if (i == ROW1 || i == ROW2 || i == ROW3) {
            b = true;
        }
        return b;
    }

    private List<String> getArrayPart(final String[] array, final int start,
                                      final int end) {
        List<String> retList = new ArrayList<>();
        for (int i = start; i < end + 1; i++) {
            retList.add(array[i]);
        }
        return retList;
    }

    private int getAssayNumber(final int start, final String[] line) {
        int ret = 1;

        for (int i = start + 1; i < line.length; i++) {
            if (line[i].toLowerCase(Locale.ENGLISH).equals("")) {
                ret++;
            } else {
                break;
            }
        }
        return ret;
    }

    private void storeHeadingRow3(final int start, final String[] line) {
        for (int i = start; i < (start + assayNum); i++) {
            assayList.add(line[i]);
            headingRow3 = headingRow3 + line[i] + ",";
        }
    }

    /**
     * Close reader.
     *
     * @throws IOException io exception
     */
    @Override
    public final void close()
            throws IOException {
        try {
            br.close();
        } catch (IOException ex) {
            throw new IOException("Fail to close feature list file: " + ex.
                    getMessage(), ex);
        }
    }

}
