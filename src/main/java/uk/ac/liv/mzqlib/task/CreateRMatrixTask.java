package uk.ac.liv.mzqlib.task;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.commons.lang3.math.NumberUtils;
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
        String x = "";
        String logX = "";

        List<String> rowNames = new ArrayList<>();
        hmParam.setRowNames(rowNames);
//        List<String> colNames = new ArrayList<>();
//        hmParam.setColNames(colNames);

        // The maximum value of the values
        double max = 0;
        // The minimum value of the values
        double min = 0;

        double logMax = 0;
        double logMin = 0;

        updateProgress(0, 1);

        int count = 1;

        for (MzqDataMatrixRow row : rowList) {

            updateMessage("Processing row: " + row.getObjectValue().get());

            // add to row names
            rowNames.add(row.getObjectValue().get());

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

                // Calclualate log value, if the value is 0 or not a number use 0.5 to calcluate the log
                String replaceV = String.valueOf(Math.log10(0.5));
                if (NumberUtils.isNumber(value.get())) {
                    double logV = Math.log10(Double.parseDouble(value.get()));
                    if (logV != Double.NEGATIVE_INFINITY && logV != Double.POSITIVE_INFINITY) {
                        logX = logX + String.valueOf(logV) + ",";
                        if (logV < logMin) {
                            logMin = logV;
                        }
                        if (logV > logMax) {
                            logMax = logV;
                        }
                    }
                    else {
                        logX = logX + replaceV + ",";
                    }
                }
                else {
                    logX = logX + replaceV + ",";
                }

                if (isCancelled()) {
                    updateMessage("Cancelled");
                    break;
                }
            }
            updateProgress(count, rowList.size());
            count++;
        }

        x = x.substring(0, x.lastIndexOf(","));
        logX = logX.substring(0, logX.lastIndexOf(","));
        hmParam.setMatrix(x);
        hmParam.setLogMatrix(logX);
        hmParam.setMax(max);
        hmParam.setMin(min);
        hmParam.setLogMax(logMax);
        hmParam.setLogMin(logMin);
        updateMessage("Done");
        updateProgress(1, 1);

        long stop = System.currentTimeMillis();
        System.out.println("Total time: " + (stop - start) / 1000);
        return hmParam;
    }

}
