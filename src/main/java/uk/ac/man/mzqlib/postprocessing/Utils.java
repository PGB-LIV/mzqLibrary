
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package uk.ac.man.mzqlib.postprocessing;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.liv.pgb.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Row;

/**
 * Utilities used
 *
 * @author man-mqbsshz2
 */
public final class Utils {
    private Utils() {
    }

    /**
     * calculate the column sum in a two-dimensional array
     *
     * @param arr - a 2-d array
     *
     * @return a 1-d array
     */
    public static double[] columnSum(final double[][] arr) {
        int      index = 0;
        double[] temp  = new double[arr[index].length];

        for (int i = 0; i < arr[0].length; i++) {
            double sum = 0;

            for (double[] arr1 : arr) {
                sum += arr1[i];
            }

            temp[index] = sum;
            index++;
        }

        return temp;
    }

    /**
     * get the median for a vector
     *
     * @param d - a 1-d array
     *
     * @return a double value
     */
    public static double median(final double[] d) {
        Arrays.sort(d);

        int middle = d.length / 2;

        if (d.length % 2 == 0) {
            double left  = d[middle - 1];
            double right = d[middle];

            return (left + right) / 2;
        } else {
            return d[middle];
        }
    }

    /**
     * Make the records of DataMatrix in order
     *
     * @param map - a hashmap
     * @param dM  - data matrix
     *
     * @return a data matrix
     */
    public static DataMatrix sortedMap(final Map<String, List<String>> map, final DataMatrix dM) {
        Set s = map.entrySet();

        for (Iterator it = s.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();

//          String key = (String) entry.getKey();
            String value = entry.getValue().toString();

            // remove the double quotation marks in front and rear
            value = value.substring(1, value.length() - 2).replaceAll("[\\,]", " ");

            // remove the last 5 characters of groupId
            value = value.substring(0, value.length() - 5);

            Row row = new Row();

            row.setObjectRef(entry.getKey().toString());
            row.getValue().add(value);
            dM.getRow().add(row);
        }

        return dM;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
