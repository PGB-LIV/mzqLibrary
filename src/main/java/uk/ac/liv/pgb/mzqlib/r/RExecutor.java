package uk.ac.liv.pgb.mzqlib.r;

import java.util.Arrays;
import java.util.Iterator;

import org.rosuda.JRI.Rengine;

/**
 *
 * @author Da Qi
 * @since 30-Sep-2014 14:48:07
 */
public class RExecutor {

    /**
     * Constant.
     */
    public static final Rengine re = new Rengine(new String[]{"--vanilla"}, false, null);
    private final String        command;
    private final String[]      args;
    private StringBuilder       error;

    /**
     * Constructor of RExecutor class.
     *
     * @param command command string.
     * @param args    input argument.
     */
    public RExecutor(final String command, final String[] args) {
        this.command = command;
        this.args    = Arrays.copyOf(args, args.length);
    }

    /**
     * Run R scripts.
     */
    public void run() {
        StringBuilder runString = new StringBuilder();

        runString.append(command);
        runString.append("(");

        Iterator<String> i = Arrays.asList(args).iterator();

        while (i.hasNext()) {
            runString.append(i.next());

            if (i.hasNext()) {
                runString.append(", ");
            }
        }

        runString.append(")");
        re.eval(runString.toString());
    }

    /**
     * Get error messages.
     *
     * @return errors.
     */
    public String getError() {
        if (error == null) {
            error = new StringBuilder();
        }

        return error.toString();
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
