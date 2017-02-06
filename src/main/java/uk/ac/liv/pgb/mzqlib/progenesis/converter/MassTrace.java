package uk.ac.liv.pgb.mzqlib.progenesis.converter;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ddq
 */
public class MassTrace {
    private double rtStart;
    private double rtEnd;
    private double mzStart;
    private double mzEnd;

    /**
     * Constructor of MassTrace.
     */
    public MassTrace() {
        rtStart = (double) 0;
        rtEnd   = (double) 0;
        mzStart = (double) 0;
        mzEnd   = (double) 0;
    }

    /**
     * Constructor with initial parameters (double).
     *
     * @param mz     mass to charge value
     * @param charge charge value
     * @param rt     retention time value
     * @param rtWin  retention time window value
     */
    public MassTrace(final double mz, final double charge, final double rt, final double rtWin) {
        rtStart = rt - rtWin / 2;
        rtEnd   = rtStart + rtWin;
        mzStart = mz;
        mzEnd   = mzStart + 3 / charge;
    }

    /**
     * Constructor with initial parameters (String).
     *
     * @param mz     mass to charge value
     * @param charge charge value
     * @param rt     retention time value
     * @param rtWin  retention time window value
     */
    public MassTrace(final String mz, final String charge, final String rt, final String rtWin) {
        this(Double.parseDouble(mz), Double.parseDouble(charge), Double.parseDouble(rt), Double.parseDouble(rtWin));
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
    public MassTrace(final double mz, final double charge, final double rt, final double rtWin,
                     final DecimalFormat df) {
        this(mz, charge, rt, rtWin);
        this.rtStart = Double.valueOf(df.format(rtStart));
        this.rtEnd   = Double.valueOf(df.format(rtEnd));
        this.mzStart = Double.valueOf(df.format(mzStart));
        this.mzEnd   = Double.valueOf(df.format(mzEnd));
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
    public MassTrace(final String mz, final String charge, final String rt, final String rtWin,
                     final DecimalFormat df) {
        this(Double.parseDouble(mz), Double.parseDouble(charge), Double.parseDouble(rt), Double.parseDouble(rtWin), df);
    }

    /**
     * Get list of mass trace value.
     *
     * @return List&lt;Double&gt;
     */
    public final List<Double> getMassTraceDoubleList() {
        List<Double> mtDList = new ArrayList<>();

        mtDList.add(rtStart);
        mtDList.add(mzStart);
        mtDList.add(rtEnd);
        mtDList.add(mzEnd);

        return mtDList;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
