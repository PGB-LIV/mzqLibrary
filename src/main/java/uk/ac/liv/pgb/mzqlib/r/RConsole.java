package uk.ac.liv.pgb.mzqlib.r;

import java.awt.FileDialog;
import java.awt.Frame;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author Da Qi
 * @since 30-Jul-2014 15:48:16
 */
public class RConsole implements RMainLoopCallbacks {

    /**
     * Override rBusy method.
     *
     * @param re    R engine.
     * @param which which.
     */
    @Override
    public final void rBusy(final Rengine re, final int which) {
        System.out.println("rBusy(" + which + ")");
    }

    /**
     * Override rChooseFile method.
     *
     * @param re      R engine.
     * @param newFile new file.
     *
     * @return result message.
     */
    @Override
    public final String rChooseFile(final Rengine re, final int newFile) {
        FileDialog fd = new FileDialog(new Frame(),
                                       (newFile == 0)
                                       ? "Select a file"
                                       : "Select a new file",
                                       (newFile == 0)
                                       ? FileDialog.LOAD
                                       : FileDialog.SAVE);

        fd.setVisible(true);

        String res = null;

        if (fd.getDirectory() != null) {
            res = fd.getDirectory();
        }

        if (fd.getFile() != null) {
            res = res == null
                  ? fd.getFile()
                  : res + fd.getFile();
        }

        return res;
    }

    /**
     * Override rFlushConsole method.
     *
     * @param re R engine.
     */
    @Override
    public void rFlushConsole(final Rengine re) {
    }

    /**
     * Override rLoadHistory method.
     *
     * @param re       R engine.
     * @param filename file name.
     */
    @Override
    public void rLoadHistory(final Rengine re, final String filename) {
    }

    /**
     * Override rReadConsole method.
     *
     * @param re           R engine.
     * @param prompt       prompt message.
     * @param addToHistory add to history value.
     *
     * @return result messages.
     */
    @Override
    public final String rReadConsole(final Rengine re, final String prompt, final int addToHistory) {
        System.out.print(prompt);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            String         s  = br.readLine();

            return s == null || s.length() == 0
                   ? s
                   : s + "\n";
        } catch (Exception ex) {
            Logger.getLogger(RConsole.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("jriReadConsole exception: " + ex.getMessage());
        }

        return null;
    }

    /**
     * Override rSaveHistory method.
     *
     * @param re       R engine.
     * @param filename file name.
     */
    @Override
    public void rSaveHistory(final Rengine re, final String filename) {
    }

    /**
     * Override rShowMessage method.
     *
     * @param re      R engine.
     * @param message message text.
     */
    @Override
    public final void rShowMessage(final Rengine re, final String message) {
        System.out.println("rShowMessage \"" + message + "\"");
    }

    /**
     * Override rWriteConsole method.
     *
     * @param re    R engine.
     * @param text  message text.
     * @param oType output type.
     */
    @Override
    public final void rWriteConsole(final Rengine re, final String text, final int oType) {
        System.out.print(text);
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
