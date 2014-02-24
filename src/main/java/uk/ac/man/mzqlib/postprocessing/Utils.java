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
import uk.ac.liv.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.jmzqml.model.mzqml.Row;

/**
 *
 * @author man-mqbsshz2
 */
public class Utils {
    
    /**
     * Make the records of DataMatrix in order
     * @param map
     * @param dM
     * @return 
     */
    public static DataMatrix SortedMap(Map<String, List<String>> map, DataMatrix dM) {
        Set s = map.entrySet();

        for (Iterator it = s.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
//            String key = (String) entry.getKey();
            String value = entry.getValue().toString();
            //remove the double quotation marks in front and rear
            value = value.substring(1, value.length() - 2).replaceAll("[\\,]", " ");
            //remove the last 5 characters of groupId
            value = value.substring(0, value.length() - 5);

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
     * calculate the column sum in a two-dimensional array
     * @param arr
     * @return 
     */
     public static double[] ColumnSum(double[][] arr) {
        int index = 0;
        double[] temp = new double[arr[index].length];
        for (int i = 0; i < arr[0].length; i++) {
            double sum = 0;
            for (double[] arr1 : arr) {
                sum += arr1[i];
            }

//            for (int j = 0; j < arr.length; j++) {
//                sum += arr[j][i];
//            }
            temp[index] = sum;
//      System.out.println("Index is: " + index + " Sum is: " + sum);
            index++;
        }
        return temp;
    }

     /**
      * get the median for a vector
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
    
}
