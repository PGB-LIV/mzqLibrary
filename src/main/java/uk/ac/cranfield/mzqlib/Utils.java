
package uk.ac.cranfield.mzqlib;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.ac.ebi.pride.jmztab.model.Param;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParam;

/**
 *
 * @author Jun Fan@qmul
 */
public class Utils {

    private static final String MZQ_XSD = "mzQuantML_1_0_0.xsd";

    /**
     * Convert mzq CvParam to mzTab CvParam.
     *
     * @param qParam CvParam from mzq file.
     *
     * @return CvParam of mzTab.
     */
    public static Param convertMztabParam(final CvParam qParam) {
        return new uk.ac.ebi.pride.jmztab.model.CVParam(qParam.getCvRef(),
                                                        qParam.getAccession(),
                                                        qParam.getName(),
                                                        qParam.getValue());
    }

    /**
     * Validate mzq file.
     *
     * @param mzqFile mzq file name.
     *
     * @return true if the mzq file is valid.
     */
    public static boolean validateMzqFile(final String mzqFile) {
        return validate(mzqFile, MZQ_XSD);
    }

    private static boolean validate(final String xmlFile, final String xsdFile) {
        try {
            System.out.
                    println(Utils.class.getClassLoader().getResource(xsdFile));
            Source schemaFile = new StreamSource(Utils.class.getClassLoader().
                    getResourceAsStream(xsdFile));
            SchemaFactory factory = SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
//            String location = Utils.locateFile(xmlFile, xTracker.folders);
//            Document doc = db.parse(location);
            Document doc = db.parse(xmlFile);
            doc.getDocumentElement().normalize();
            validator.validate(new DOMSource(doc));
        } catch (SAXException ex) {
            if (ex.getMessage().contains("schema_reference")) {
                throw new IllegalStateException(
                        "ERROR: Can not find the specified xsd file "
                        + xsdFile + " to validate the XML file", ex);
            } else {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Exception while validating \n" + e,
                                            e);
        }
        return true;
    }

    /**
     * Calculate the mean of list of double.
     *
     * @param list list of double.
     *
     * @return the mean of the list of double.
     */
    public static double mean(final List<Double> list) {
        if (list.isEmpty()) {
            return Double.NaN;
        }
        return sum(list) / list.size();
    }

    /**
     * Calculate the median of list of double.
     *
     * @param list list of double.
     *
     * @return the median of the list of double.
     */
    public static double median(final List<Double> list) {
        Collections.sort(list);
        int len = list.size();
        if (len == 0) {
            return 0;
//            return Double.NaN;
        }
        int middle = len / 2;
        if (len % 2 == 1) {
            return list.get(middle);
        } else {
            return (list.get(middle - 1) + list.get(middle)) / 2.0;
        }
    }

    /**
     * Calculate the sum of list of double.
     *
     * @param list list of double.
     *
     * @return the sum of the list of double.
     */
    public static double sum(final List<Double> list) {
        double ret = 0;
        for (double d : list) {
            ret += d;
        }
        return ret;
    }

    /**
     * Get the extension of a file.
     *
     * @param f The file to get the extension from
     *
     * @return the extension of the given file
     */
    public static String getExtension(final File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase(Locale.ENGLISH);
        }
        return ext;
    }

    /**
     * a static method to return
     *
     * @return a file chooser starting from
     *
     * @param path which would require a confirmation when overwriting an
     *             existing file
     */
    public static FileChooserWithGenericFileFilter createOverwriteFileChooser(
            final String path) {
        return new FileChooserWithGenericFileFilter(path) {

            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(
                            getTopLevelAncestor(),
                            "The selected file already exists. "
                            + "Do you want to overwrite it?",
                            "The file already exists",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                    }
                }
                super.approveSelection();
            }

        };
    }

}

class GenericFileFilter extends javax.swing.filechooser.FileFilter {

    private String[] fileExts;
    private String description;

    public GenericFileFilter(final String[] filesExtsIn,
                             final String description) {
        this.fileExts = filesExtsIn;
        this.description = description;
    }

    /**
     * Whether the given file is accepted by this filter.
     */
    @Override
    public boolean accept(final File f) {
        if (f != null) {
            //By accepting all directories, this filter allows the user to navigate around the file system
            //otherwise limited to the directory with which the chooser is initialized
            if (f.isDirectory()) {
                return true;
            }
            String extension = Utils.getExtension(f);
            if (extension != null) {
                for (String ext : fileExts) {
                    if (extension.equalsIgnoreCase(ext)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * The description of this filter. For example: "JPG and GIF Images"
     */
    @Override
    public String getDescription() {
        return description;
    }

    public String[] getFileExts() {
        return fileExts;
    }

}

class FileChooserWithGenericFileFilter extends JFileChooser {

    FileChooserWithGenericFileFilter() {
        super();
    }

    FileChooserWithGenericFileFilter(final File file) {
        super(file);
    }

    FileChooserWithGenericFileFilter(final String dir) {
        super(dir);
    }

    File getFullSaveSelectedFile() {
        String filename = getSelectedFile().toString();
        String[] fileExts = ((GenericFileFilter) getFileFilter()).getFileExts();
        String ext = fileExts[0];
        if (!filename.endsWith(ext)) {
            filename += ".";
            filename += ext;
        }
        return new File(filename);
    }

}
