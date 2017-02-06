package uk.ac.liv.pgb.mzqlib.progenesis.reader;

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

import au.com.bytecode.opencsv.CSVReader;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

/**
 *
 * @author Da Qi
 */
public class ProgenesisProteinListReader implements Closeable {

    // The title header rows  in feature list file.
    private static final int ROW1        = -2;    // 1st row
    private static final int ROW2        = -1;    // 2nd row
    private static final int ROW3        = 0;     // 3rd row
    private int              normAbStart = 0;
    private int              rawAbStart  = 0;
    private int              rawAbEnd    = 0;
    private int              posConf     = 0;
    private int              posAnova    = 0;
    private int              posMfc      = 0;

    //
    private int assayNum = 1;

    //
    private List<String>               stuVarList             = new ArrayList<>();
    private List<String>               assayList              = new ArrayList<>();
    private TIntObjectMap<String>      indexMap               = new TIntObjectHashMap<>();
    private TIntObjectMap<TDoubleList> normalizedAbundanceMap = new TIntObjectHashMap<>();
    private TIntObjectMap<TDoubleList> rawAbundanceMap        = new TIntObjectHashMap<>();
    private TIntDoubleMap              confidenceMap          = new TIntDoubleHashMap();
    private TIntDoubleMap              anovaMap               = new TIntDoubleHashMap();
    private TIntDoubleMap              maxFoldChangeMap       = new TIntDoubleHashMap();
    private Map<String, Set<String>>   studyGroupMap          = new HashMap<>();
    private TIntObjectMap<String[]>    completeMap            = new TIntObjectHashMap<>();
    private BufferedReader             br;

    /**
     * Constructor.
     *
     * @param rd reader.
     *
     * @throws IOException io exception.
     */
    public ProgenesisProteinListReader(final Reader rd) throws IOException {
        this(rd, ',');
    }

    ////////// ////////// //////////
    // Constructor

    /**
     * Constructor.
     *
     * @param rd        reader.
     * @param separator separator character.
     *
     * @throws IOException io exception.
     */
    public ProgenesisProteinListReader(final Reader rd, final char separator) throws IOException {
        br = new BufferedReader(rd);

        CSVReader reader = new CSVReader(br, separator);
        String    nextLine[];

        // Read the first line
        nextLine = reader.readNext();
        completeMap.put(ROW1, nextLine);

        for (int i = 0; i < nextLine.length; i++) {
            if (nextLine[i].toLowerCase(Locale.ENGLISH).equals(ProteinListHeaders.NORMALIZED_ABUNDANCE)) {
                normAbStart = i;
            } else if (nextLine[i].toLowerCase(Locale.ENGLISH).contains(ProteinListHeaders.RAW_ABUNDANCE)) {
                rawAbStart = i;
                rawAbEnd   = rawAbStart + (rawAbStart - normAbStart);

                break;
            }
        }

        // get the number of assay
        if (normAbStart != 0) {
            assayNum = getAssayNumber(normAbStart, nextLine);
        } else if (rawAbStart != 0) {
            assayNum = getAssayNumber(rawAbStart, nextLine);
        } else {
            throw new IllegalStateException("No abundance data was found in protein file! Please check!");
        }

        // Read the second line
        nextLine = reader.readNext();
        completeMap.put(ROW2, nextLine);

        int[] stuVarPosition = new int[300];
        int   m              = 0;

        if (normAbStart != 0) {
            for (int i = normAbStart; i < (normAbStart + assayNum); i++) {
                if (!nextLine[i].isEmpty()) {
                    stuVarList.add(nextLine[i]);
                    stuVarPosition[m] = i;
                    m++;
                }

                stuVarPosition[m] = i + 1;
            }
        } else if (rawAbStart != 0) {
            for (int i = rawAbStart; i < (rawAbStart + assayNum); i++) {
                if (!nextLine[i].isEmpty()) {
                    stuVarList.add(nextLine[i]);
                    stuVarPosition[m] = i;
                    m++;
                }

                stuVarPosition[m] = i + 1;
            }
        }

        // Read the third line
        nextLine = reader.readNext();
        completeMap.put(ROW3, nextLine);

        if (normAbStart != 0) {
            for (int i = normAbStart; i < (normAbStart + assayNum); i++) {
                assayList.add(nextLine[i]);
            }
        } else if (rawAbStart != 0) {
            for (int i = rawAbStart; i < (rawAbStart + assayNum); i++) {
                assayList.add(nextLine[i]);
            }
        }

        for (int i = 0; i < stuVarList.size(); i++) {
            String      key   = (String) stuVarList.get(i);
            Set<String> value = new HashSet<>();

            for (int k = stuVarPosition[i]; k < stuVarPosition[i + 1]; k++) {
                value.add(nextLine[k]);
            }

            studyGroupMap.put(key, value);
        }

        // get the position of confidence score, anova, max fold change
        for (int i = 0; i < nextLine.length; i++) {
            String lowerCaseHeader = nextLine[i].trim().toLowerCase(Locale.ENGLISH);

            if (lowerCaseHeader.equals(ProteinListHeaders.CONFIDENCE_SCORE)) {
                posConf = i;
            } else if (lowerCaseHeader.contains(ProteinListHeaders.ANOVA)) {
                posAnova = i;
            } else if (lowerCaseHeader.equals(ProteinListHeaders.MAX_FOLD_CHANGE)) {
                posMfc = i;
            }
        }

        TDoubleList currentV = new TDoubleArrayList();
        int         currentK = 1;

        while ((nextLine = reader.readNext()) != null) {

            // Build indexMap, stores proteinAccessions
            indexMap.put(currentK, nextLine[0]);

            double conf = getDoubleOrNaN(nextLine[posConf]);

            confidenceMap.put(currentK, conf);

            double anova = getDoubleOrNaN(nextLine[posAnova]);

            anovaMap.put(currentK, anova);

//          if (NumberUtils.isNumber(nextLine[pos_anova])) {
//              anovaMap.put(currentK, Double.parseDouble(nextLine[pos_anova]));
//          }
            double mfc = getDoubleOrNaN(nextLine[posMfc]);

            maxFoldChangeMap.put(currentK, mfc);

//          if (NumberUtils.isNumber(nextLine[pos_mfc])) {
//              maxFoldChangeMap.put(currentK, Double.parseDouble(nextLine[pos_mfc]));
//          }
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

                normalizedAbundanceMap.put(currentK, new TDoubleArrayList(currentV));
                currentV.clear();
            }

            // Build raw abundance hashmap
            if (rawAbStart != 0) {
                for (int i = rawAbStart; i < rawAbEnd; i++) {
                    if (!NumberUtils.isNumber(nextLine[i])) {
                        currentV.add(0.0);
                    } else {
                        currentV.add(Double.parseDouble(nextLine[i]));
                    }
                }

                rawAbundanceMap.put(currentK, new TDoubleArrayList(currentV));
                currentV.clear();
            }

            currentK++;
        }
    }

    /**
     * Override close method.
     *
     * @throws IOException io exception.
     */
    @Override
    public final void close() throws IOException {
        try {
            br.close();
        } catch (IOException ex) {
            throw new IOException("Fail to close protein list file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Get ANOVA value map.
     *
     * @return ANOVA value map.
     */
    public final TIntDoubleMap getAnovaMap() {
        return anovaMap;
    }

    private List<String> getArrayPart(final String[] array, final int start, final int end) {
        List<String> retList = new ArrayList<>();

        for (int i = start; i < end + 1; i++) {
            retList.add(array[i]);
        }

        return retList;
    }

    /**
     * Get assay list.
     *
     * @return assay list.
     */
    public final List<String> getAssayList() {
        return assayList;
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

    /**
     * Get confidence value map.
     *
     * @return confidence value map.
     */
    public final TIntDoubleMap getConfidenceMap() {
        return confidenceMap;
    }

    private static double getDoubleOrNaN(final String string) {
        double value;

        try {
            value = Double.parseDouble(string);
        } catch (NumberFormatException ex) {
            value = Double.NaN;
        }

        return value;
    }

    // private function
    // isFirstThreeRow determine whether an entry is from the title row
    private boolean isFirstThreeRow(final Integer i) {
        boolean b = false;

        if (i == ROW1 || i == ROW2 || i == ROW3) {
            b = true;
        }

        return b;
    }

    /**
     * Get index map.
     *
     * @return index map.
     */
    public final TIntObjectMap<String> getInexMap() {
        return indexMap;
    }

    /**
     * Get MaxFoldChange value map.
     *
     * @return max fold change value map.
     */
    public final TIntDoubleMap getMaxFoldChangeMap() {
        return maxFoldChangeMap;
    }

    /**
     * Get multiple column value map from start to end column.
     *
     * @param startCol start column position.
     * @param endCol   end column position.
     *
     * @return multiple column value map.
     */
    public final TIntObjectMap<List<String>> getMultipleColumn(final int startCol, final int endCol) {
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
     * Get normalized abundance value map.
     *
     * @return norm abundance value map.
     */
    public final TIntObjectMap<TDoubleList> getNormalizedAbundanceMap() {
        return normalizedAbundanceMap;
    }

    /**
     * Get raw abundance value map.
     *
     * @return raw abundance value map.
     */
    public final TIntObjectMap<TDoubleList> getRawAbundanceMap() {
        return rawAbundanceMap;
    }

    /**
     * Get single column value map.
     *
     * @param col column index.
     *
     * @return single column value map.
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

    /**
     * Get study variable list.
     *
     * @return study variable list.
     */
    public final List<String> getStuVarList() {
        return stuVarList;
    }

    /**
     * Get study grouping map.
     *
     * @return study grouping map.
     */
    public final Map<String, Set<String>> getStudyGroupMap() {
        return studyGroupMap;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
