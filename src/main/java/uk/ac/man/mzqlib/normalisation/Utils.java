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
import uk.ac.liv.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.jmzqml.model.mzqml.Row;

/**
 *
 * @author man-mqbsshz2
 */
public class Utils {

    public static DataMatrix SortedMap(Map<String, List<String>> map, DataMatrix dM) {
        Set s = map.entrySet();

        for (Iterator it = s.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
//            String key = (String) entry.getKey();
            String value = entry.getValue().toString();
            
            //remove the double quotation marks in front and rear
            value = value.substring(1, value.length() - 1).replaceAll("[\\,]", " ");
            
            //remove the last 5 characters of groupId
//            value = value.substring(0, value.length() - 5);

//            System.out.println(key + " => " + value);
            Row row = new Row();

            row.setObjectRef(entry.getKey().toString());
//            System.out.println("sKey: " + sKey);
//            String sKey = entry.getKey().toString();
//            row.setObjectRef(gIo.get(sKey));

            row.getValue().add(value);
            dM.getRow().add(row);
        }
        return dM;
    }

    /**
     * get the median for a vector
     *
     * @param d
     * @return
     */
    public static double Median(double[] d) {
        Arrays.sort(d);
        int middle = d.length / 2;
        if (d.length % 2 == 0) {
            double left = d[middle - 1];
            double right = d[middle];
            return (left + right) / 2;
        } else {
            return d[middle];
        }
    }

    public static double mean(double[] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }

    public static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(
                elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) {
            System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        }
        return newArray;
    }
}
