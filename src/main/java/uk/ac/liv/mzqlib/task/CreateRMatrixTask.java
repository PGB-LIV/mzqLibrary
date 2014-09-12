/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.task;

import java.util.List;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import uk.ac.liv.mzqlib.model.HeatMapParam;
import uk.ac.liv.mzqlib.model.MzqDataMatrixRow;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 12-Sep-2014 11:10:08
 */
public class CreateRMatrixTask extends Task<HeatMapParam> {

    private final ObservableList<MzqDataMatrixRow> rowList = FXCollections.observableArrayList();

    public CreateRMatrixTask(ObservableList<MzqDataMatrixRow> list) {
        rowList.clear();
        rowList.addAll(list);
    }

    @Override
    protected HeatMapParam call()
            throws Exception {

        long start = System.currentTimeMillis();

        HeatMapParam hmParam = new HeatMapParam();

        final int rowNumber = rowList.size();

        hmParam.setRowNumber(rowNumber);
        //StringBuilder sb = new StringBuilder();
        String x = "";

        // The maximum value of the values
        double max = 0;
        // The minimum value of the values
        double min = 0;

        updateProgress(0, 1);

        int count = 1;
//        MutableInt mcount = new MutableInt(1);
//        MutableDouble mMax = new MutableDouble(0);
//        MutableDouble mMin = new MutableDouble(0);
//
//        rowList.stream().parallel()
//                //.filter((MzqDataMatrixRow row) -> row.getObjetId() != null)
//                .forEach((MzqDataMatrixRow row) -> {
//                    updateMessage("Processing row: " + row.getObjetId());
//                    List<StringProperty> values = row.Values();
//                    for (StringProperty value : values) {
//                        sb.append(value.get()).append(",");
//                        if (NumberUtils.isNumber(value.get())) {
//                            if (mMin.compareTo(new MutableDouble(value.get())) > 0) {
//                                mMin.setValue(Double.parseDouble(value.get()));
//                            }
//                            if (mMax.compareTo(new MutableDouble(value.get())) < 0) {
//                                mMax.setValue(Double.parseDouble(value.get()));
//                            }
//                        }
//                        if (isCancelled()) {
//                            updateMessage("Cancelled");
//                            break;
//                        }
//                    }
//                    updateProgress(mcount.intValue(), rowNumber + 5);
//                    mcount.add(1);
//                });
//
//        sb.substring(0, sb.lastIndexOf(","));
//        hmParam.setMatrix(sb.toString());
//        hmParam.setMin(mMin.doubleValue());
//        hmParam.setMax(mMax.doubleValue());

        for (MzqDataMatrixRow row : rowList) {

            updateMessage("Processing row: " + row.getObjetId());

            List<StringProperty> values = row.Values();
            for (StringProperty value : values) {
                x = x + value.get() + ",";
                if (NumberUtils.isNumber(value.get())) {
                    if (Double.parseDouble(value.get()) < min) {
                        min = Double.parseDouble(value.get());
                    }
                    if (Double.parseDouble(value.get()) > max) {
                        max = Double.parseDouble(value.get());
                    }
                }

                if (isCancelled()) {
                    updateMessage("Cancelled");
                    break;
                }
            }

            updateProgress(count, rowList.size() + 5);
            count++;
        }
//
//        System.out.println("x: " + x);
//        System.out.println("sb: " + sb.toString());
//        System.out.println("hmParam.getMax(): " + hmParam.getMax());
//        System.out.println("hmParam.getMin(): " + hmParam.getMin());
//        System.out.println("hmParam.getMatrix(): " + hmParam.getMatrix());
//        System.out.println("x equals sb.toString(): " + x.equals(sb.toString()));

        x = x.substring(0, x.lastIndexOf(","));
        hmParam.setMatrix(x);
        hmParam.setMax(max);
        hmParam.setMin(min);
        updateMessage("Done");
        updateProgress(1, 1);

        long stop = System.currentTimeMillis();
        System.out.println("Total time: " + (stop - start) / 1000);
        return hmParam;
    }

}
