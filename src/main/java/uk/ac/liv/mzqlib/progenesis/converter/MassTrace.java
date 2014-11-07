
package uk.ac.liv.mzqlib.progenesis.converter;

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

    /*
     * constructor
     */
    public MassTrace() {
        rt_start = (double) 0;
        rt_end = (double) 0;
        mz_start = (double) 0;
        mz_end = (double) 0;
    }

    /*
     * @param m/z, charge, retention time(center), retention time window
     */
    public MassTrace(double mz, double charge, double rt, double rtWin) {
        rt_start = rt - rtWin / 2;
        rt_end = rt_start + rtWin;
        mz_start = mz;
        mz_end = mz_start + 3 / charge;
    }

    public MassTrace(String mz, String charge, String rt, String rtWin) {
        this(Double.parseDouble(mz), Double.parseDouble(charge), Double.parseDouble(rt), Double.parseDouble(rtWin));
    }

    public MassTrace(double mz, double charge, double rt, double rtWin, DecimalFormat df) {
        this(mz, charge, rt, rtWin);
        this.rt_start = Double.valueOf(df.format(rt_start)); 
        this.rt_end = Double.valueOf(df.format(rt_end));
        this.mz_start = Double.valueOf(df.format(mz_start));
        this.mz_end = Double.valueOf(df.format(mz_end));
    }

    public MassTrace(String mz, String charge, String rt, String rtWin, DecimalFormat df) {
        this(Double.parseDouble(mz), Double.parseDouble(charge), Double.parseDouble(rt), Double.parseDouble(rtWin), df);
    }

    /*
     * public methods
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
