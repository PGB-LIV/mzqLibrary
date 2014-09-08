
package uk.ac.liv.mzqlib.utils;

import java.io.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 06-Nov-2013 14:55:14
 */
public class Gzipper {

    public static File extractFile(File zipped_file) {
        GZIPInputStream gin = null;
        File outFile = null;
        try {

            FileOutputStream fos = null;
            gin = new GZIPInputStream(new FileInputStream(zipped_file));
            outFile = File.createTempFile("tmp_" + new Random().toString(), ".mzq");
            //outFile = new File(zipped_file.getParent(), "tmp_" + zipped_file.getName().replaceAll("\\.gz$", ""));
            fos = new FileOutputStream(outFile);
            byte[] buf = new byte[100000];
            int len;
            while ((len = gin.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fos.close();

        }
        catch (IOException ex) {
            Logger.getLogger(Gzipper.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            try {
                gin.close();
            }
            catch (IOException ex) {
                Logger.getLogger(Gzipper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return outFile;

    }

    public static void deleteFile(File file) {
        boolean success = file.delete();
        if (!success) {
            System.out.println(file.getAbsolutePath() + " Deletion failed.");
            //System.exit(0);
        }
        else {
            System.out.println(file.getAbsolutePath() + " File deleted.");

        }

    }

    public static void compressFile(File file) {
        try {

            FileOutputStream fos = new FileOutputStream(file + ".gz");
            try (GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
                FileInputStream fin = new FileInputStream(file);
                try (BufferedInputStream in = new BufferedInputStream(fin)) {
                    byte[] buffer = new byte[1024];
                    int i;
                    while ((i = in.read(buffer)) >= 0) {
                        gzos.write(buffer, 0, i);
                    }
                    System.out.println("the file is in now gzip format");
                }
            }
        }
        catch (IOException e) {
            System.out.println("Exception is" + e);
            Logger.getLogger(Gzipper.class.getName()).log(Level.SEVERE, null, e);

        }

    }

}
