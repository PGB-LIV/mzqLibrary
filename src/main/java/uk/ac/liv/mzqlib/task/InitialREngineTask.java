
package uk.ac.liv.mzqlib.task;

import javafx.concurrent.Task;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 11-Sep-2014 15:13:21
 */
public class InitialREngineTask extends Task<Rengine> {

    @Override
    protected Rengine call()
            throws Exception {
        return new Rengine(new String[]{" ", " "}, false, null);
    }

}
