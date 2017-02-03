package uk.ac.liv.pgb.mzqlib.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.liv.pgb.jmzqml.model.mzqml.InputFiles;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Param;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SearchDatabase;
import uk.ac.liv.pgb.jmzqml.model.mzqml.UserParam;

/**
 * General utilities class
 *
 * @author lukas007
 *
 */
public class Utils {

    /**
     * Add a SearchDatabase to InputFiles.
     *
     * @param inFiles input files.
     * @param db      search database.
     *
     */
    public static void addSearchDBToInputFiles(final InputFiles inFiles, final SearchDatabase db) {
        List<SearchDatabase> searchDBs = inFiles.getSearchDatabase();

        searchDBs.add(db);
    }

    /**
     * Round a double value and keeping (at max) the given number of decimal
     * places.
     *
     * @param value                 value to be rounded.
     * @param numberOfDecimalPlaces number of decimal places.
     *
     * @return rounded double.
     */
    public static double round(final double value, final int numberOfDecimalPlaces) {
        double multipicationFactor = Math.pow(10, numberOfDecimalPlaces);

        return Math.round(value * multipicationFactor) / multipicationFactor;
    }

    /**
     * Returns the value of a command-line parameter
     *
     * @param args     : command-line arguments (assuming couples in the form
     *                 "-argname", "argvalue" )
     * @param name     : the parameter 'name'
     * @param required required flag.
     *
     * @return returns null if the parameter is not found (and is not required).
     *         If the parameter is not
     *         found but is required, it throws an error.
     */
    public static String getCmdParameter(final String[] args, final String name, final boolean required) {
        for (int i = 0; i < args.length; i++) {
            String argName = args[i];

            if (argName.equals("-" + name)) {
                String argValue = "";

                if (i + 1 < args.length) {
                    argValue = args[i + 1];
                }

                if (required && (argValue.trim().length() == 0 || argValue.startsWith("-"))) {
                    System.err.println("Parameter value expected for " + argName);

                    throw new IllegalArgumentException("Expected parameter value not found: " + argName);
                } else if (argValue.trim().length() == 0 || argValue.startsWith("-")) {
                    return "";
                } else {
                    return argValue;
                }
            }
        }

        // Nothing found, if required, throw error, else return "";
        if (required) {
            System.err.println("Parameter -" + name + " expected ");

            throw new IllegalArgumentException("Expected parameter not found: " + name);
        }

        return null;
    }

    /**
     * Initialized the CV map based on the /resources/CV_psi-ms.obo.txt CV file.
     *
     * @return CV map.
     *
     * @throws IOException io exceptions.
     */
    public static Map<String, String> getInitializedCVMap() throws IOException {

        // Read resource file and build up map:
        BufferedReader      in        = null;
        Map<String, String> resultMap = new HashMap<>();

        try {

            // Use the getResourceAsStream trick to read the file packaged in
            // the .jar .  This simplifies usage of the solution as no extra
            // classpath or path configurations are needed:
            InputStream resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream("CV_psi-ms.obo.txt");
            Reader      reader           = new InputStreamReader(resourceAsStream, "UTF-8");

            in = new BufferedReader(reader);

            String inputLine;
            String key   = "";
            String value = "";

            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("id:")) {
                    key = inputLine.split("id:")[1].trim();
                }

                if (inputLine.startsWith("name:")) {

                    // validate:
                    if (key.equals("")) {
                        throw new IllegalStateException("Unexpected name: preceding id: entry in CV file");
                    }

                    value = inputLine.split("name:")[1].trim();
                    resultMap.put(key, value);

                    // reset:
                    key   = "";
                    value = "";
                }
            }

            return resultMap;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Create SearchDatabase.
     *
     * @param id   id of the search database.
     * @param loc  location of the search database.
     * @param name name of the search database.
     *
     * @return SearchDatabase
     */
    public static SearchDatabase setSearchDB(final String id, final String loc, final String name) {
        SearchDatabase db = new SearchDatabase();

        db.setId(id);
        db.setLocation(loc);

        Param dbName = new Param();

        db.setDatabaseName(dbName);

        UserParam dbNameParam = new UserParam();

        dbNameParam.setName(name);
        dbName.setParam(dbNameParam);

        return db;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
