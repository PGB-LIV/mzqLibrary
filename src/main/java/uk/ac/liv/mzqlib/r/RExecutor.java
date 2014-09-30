package uk.ac.liv.mzqlib.r;

import java.util.Arrays;
import java.util.Iterator;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 30-Sep-2014 14:48:07
 */
public class RExecutor {

    public static Rengine re = new Rengine(new String[]{"--vanilla"}, false, null);
    private final String command;
    private final String[] args;
    private StringBuilder error;

    public RExecutor(String command, String[] args) {
        this.command = command;
        this.args = args;
    }

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

    public String getError() {
        if (error == null) {
            error = new StringBuilder();
        }
        return error.toString();
    }

}
