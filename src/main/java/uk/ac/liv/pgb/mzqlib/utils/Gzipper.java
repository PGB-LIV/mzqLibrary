
package uk.ac.liv.pgb.mzqlib.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Da Qi
 * @since 06-Nov-2013 14:55:14
 */
public class Gzipper {

    /**
     * Utility method to extract zip file.
     *
     * @param zipped_file zip file.
     *
     * @return unzipped file.
     *
     * @throws IOException io exceptions.
     */
    public static File extractFile(final File zipped_file)
            throws IOException {
        GZIPInputStream gin = null;
        File outFile = null;
        FileOutputStream fos = null;
        try (FileInputStream fis = new FileInputStream(zipped_file)) {
            gin = new GZIPInputStream(fis);
            outFile = File.createTempFile("tmp_" + new Random().toString(),
                                          ".mzq");
            //outFile = new File(zipped_file.getParent(), "tmp_" + zipped_file.getName().replaceAll("\\.gz$", ""));
            fos = new FileOutputStream(outFile);
            byte[] buf = new byte[100000];
            int len;
            while ((len = gin.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
        } catch (IOException ex) {
            Logger.getLogger(Gzipper.class.getName()).
                    log(Level.SEVERE, null, ex);
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (gin != null) {
                gin.close();
            }
        }
        return outFile;
    }

    /**
     * Utility method to delete a file.
     *
     * @param file file to be deleted.
     */
    public static void deleteFile(final File file) {
        boolean success = file.delete();
        if (!success) {
            System.out.println(file.getAbsolutePath() + " Deletion failed.");
            //System.exit(0);
        } else {
            System.out.println(file.getAbsolutePath() + " File deleted.");

        }

    }

    /**
     * Utility method to compress a file.
     *
     * @param file file to be compressed.
     */
    public static void compressFile(final File file) {
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
        } catch (IOException e) {
            System.out.println("Exception is" + e);
            Logger.getLogger(Gzipper.class.getName()).log(Level.SEVERE, null, e);

        }

    }

}
