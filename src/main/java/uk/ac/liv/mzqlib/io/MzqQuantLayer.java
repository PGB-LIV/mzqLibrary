/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.io;

import java.io.File;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 21-May-2014 15:44:20
 */
public class MzqQuantLayer {

    private File mzq;
    private MzQuantMLUnmarshaller um;

    public MzqQuantLayer(File mzqFile) {
        this.mzq = mzqFile;
        this.um = new MzQuantMLUnmarshaller(mzqFile);
    }

}
