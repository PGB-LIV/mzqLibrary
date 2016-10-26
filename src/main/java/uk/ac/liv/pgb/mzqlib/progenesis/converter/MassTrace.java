
package uk.ac.liv.pgb.mzqlib.progenesis.converter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ddq
 */
public class MassTrace {

    private double rt_start;
    private double rt_end;
    private double mz_start;
    private double mz_end;

    /**
     * Constructor of MassTrace.
     */
    public MassTrace() {
        rt_start = (double) 0;
        rt_end = (double) 0;
        mz_start = (double) 0;
        mz_end = (double) 0;
    }

    /**
     * Constructor with initial parameters (double).
     *
     * @param mz     mass to charge value
     * @param charge charge value
     * @param rt     retention time value
     * @param rtWin  retention time window value
     */
    public MassTrace(double mz, double charge, double rt, double rtWin) {
        rt_start = rt - rtWin / 2;
        rt_end = rt_start + rtWin;
        mz_start = mz;
        mz_end = mz_start + 3 / charge;
    }

    /**
     * Constructor with initial parameters (String).
     *
     * @param mz     mass to charge value
     * @param charge charge value
     * @param rt     retention time value
     * @param rtWin  retention time window value
     */
    public MassTrace(String mz, String charge, String rt, String rtWin) {
        this(Double.parseDouble(mz), Double.parseDouble(charge), Double.
             parseDouble(rt), Double.parseDouble(rtWin));
    }

    /**
     * Constructor with initial parameters (double).
     *
     * @param mz     mass to charge value
     * @param charge charge value
     * @param rt     retention time value
     * @param rtWin  retention time window value
     * @param df     DecimalFormat setting
     */
    public MassTrace(double mz, double charge, double rt, double rtWin,
                     DecimalFormat df) {
        this(mz, charge, rt, rtWin);
        this.rt_start = Double.valueOf(df.format(rt_start));
        this.rt_end = Double.valueOf(df.format(rt_end));
        this.mz_start = Double.valueOf(df.format(mz_start));
        this.mz_end = Double.valueOf(df.format(mz_end));
    }

    /**
     * Constructor with initial parameters (String).
     *
     * @param mz     mass to charge value
     * @param charge charge value
     * @param rt     retention time value
     * @param rtWin  retention time window value
     * @param df     DecimalFormat setting
     */
    public MassTrace(String mz, String charge, String rt, String rtWin,
                     DecimalFormat df) {
        this(Double.parseDouble(mz), Double.parseDouble(charge), Double.
             parseDouble(rt), Double.parseDouble(rtWin), df);
    }

    /**
     * Get list of mass trace value.
     *
     * @return List<Double>
     */
    public List<Double> getMassTraceDoubleList() {
        List<Double> mtDList = new ArrayList<>();
        mtDList.add(rt_start);
        mtDList.add(mz_start);
        mtDList.add(rt_end);
        mtDList.add(mz_end);
        return mtDList;
    }

}
