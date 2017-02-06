
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package uk.ac.man.mzqlib.normalisation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.liv.pgb.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Row;

/**
 *
 * @author man-mqbsshz2
 */
public final class Utils {
    private Utils() {
    }

    /**
     * calculate vector mean value
     *
     * @param m - a double type data vector
     *
     * @return a double value
     */
    public static double mean(final double[] m) {
        double sum = 0;

        for (int i = 0;i < m.length;i++) {
            sum += m[i];
        }

        return sum / m.length;
    }

    /**
     * get the median for a vector
     *
     * @param d - a double type data vector
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
     * resize an array
     *
     * @param oldArray - old array
     * @param newSize  - new size
     *
     * @return a new array
     */
    public static Object resizeArray(final Object oldArray, final int newSize) {
        int    oldSize        = java.lang.reflect.Array.getLength(oldArray);
        Class  elementType    = oldArray.getClass().getComponentType();
        Object newArray       = java.lang.reflect.Array.newInstance(elementType, newSize);
        int    preserveLength = Math.min(oldSize, newSize);

        if (preserveLength > 0) {
            System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        }

        return newArray;
    }

    /**
     * sort map entries with values
     *
     * @param map - a hashmap
     * @param dM  - data matrix
     *
     * @return sorted data matrix
     */
    public static DataMatrix sortedMap(final Map<String, List<String>> map, final DataMatrix dM) {
        Set s = map.entrySet();

        for (Iterator it = s.iterator();it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String    value = entry.getValue().toString();

            // remove the double quotation marks in front and rear
            value = value.substring(1, value.length() - 1).replaceAll("[\\,]", " ");

            Row row = new Row();

            row.setObjectRef(entry.getKey().toString());
            row.getValue().add(value);
            dM.getRow().add(row);
        }

        return dM;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
