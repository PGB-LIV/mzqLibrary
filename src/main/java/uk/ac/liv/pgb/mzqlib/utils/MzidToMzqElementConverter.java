
package uk.ac.liv.pgb.mzqlib.utils;

import java.util.ArrayList;
import java.util.List;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Cv;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.pgb.jmzqml.model.mzqml.FileFormat;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Modification;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Param;
import uk.ac.liv.pgb.jmzqml.model.mzqml.SearchDatabase;
import uk.ac.liv.pgb.jmzqml.model.mzqml.UserParam;

/**
 *
 * @author Da Qi
 * @since 18-Mar-2014 13:57:49
 */
public class MzidToMzqElementConverter {

    /**
     * Utility method to convert mzIdentML modifications to mzQuantML
     * modifications.
     *
     * @param modifications list of mzIdentML modifications.
     *
     * @return list of mzQuantML modifications.
     */
    public static List<Modification> convertMzidModsToMzqMods(
            List<uk.ac.ebi.jmzidml.model.mzidml.Modification> modifications) {
        List<Modification> mzqMods = new ArrayList();
        for (uk.ac.ebi.jmzidml.model.mzidml.Modification mzidMod : modifications) {
            Modification mzqMod = new Modification();
            List<CvParam> mzqCps;
            List<uk.ac.ebi.jmzidml.model.mzidml.CvParam> mzidCps = mzidMod.
                    getCvParam();
            if (!mzidCps.isEmpty()) {
                mzqCps = new ArrayList();
                for (uk.ac.ebi.jmzidml.model.mzidml.CvParam mzidCp : mzidCps) {
                    CvParam mzqCp = convertMzidCvParamToMzqCvParam(mzidCp);
                    mzqCps.add(mzqCp);
                }
                mzqMod.getCvParam().addAll(mzqCps);
            }
            mzqMod.setAvgMassDelta(mzidMod.getAvgMassDelta());
            mzqMod.setLocation(mzidMod.getLocation());
            mzqMod.setMonoisotopicMassDelta(mzidMod.getMonoisotopicMassDelta());
        }
        return mzqMods;
    }

    /**
     * Utility method to convert mzIdentML CvParam to mzQuantML CvParam.
     *
     * @param mzidCp mzIdentML CvParam.
     *
     * @return mzQuantML CvParam.
     */
    public static CvParam convertMzidCvParamToMzqCvParam(
            uk.ac.ebi.jmzidml.model.mzidml.CvParam mzidCp) {
        CvParam mzqCp = new CvParam();

        if (mzidCp.getAccession() != null) {
            mzqCp.setAccession(mzidCp.getAccession());
        }

        if (mzidCp.getCv() != null) {
            Cv mzqCv = convertMzidCvToMzqCv(mzidCp.getCv());
            mzqCp.setCv(mzqCv);
        }

        if (mzidCp.getName() != null) {
            mzqCp.setName(mzidCp.getName());
        }

        if (mzidCp.getUnitCv() != null) {
            Cv mzqUnitCv = convertMzidCvToMzqCv(mzidCp.getUnitCv());
            mzqCp.setUnitCv(mzqUnitCv);
        }

        if (mzidCp.getUnitAccession() != null) {
            mzqCp.setUnitAccession(mzidCp.getUnitAccession());
        }

        if (mzidCp.getUnitName() != null) {
            mzqCp.setUnitName(mzidCp.getUnitName());
        }

        return mzqCp;
    }

    /**
     *
     * Utility method to convert mzIdentML UserParam to mzQuantML UserParam.
     *
     * @param mzidUp mzIdentML UserParam.
     *
     * @return mzQuantML UserParam.
     */
    public static UserParam convertMzidUserParamToMzqUserParam(
            uk.ac.ebi.jmzidml.model.mzidml.UserParam mzidUp) {
        UserParam mzqUp = new UserParam();

        if (mzidUp.getName() != null) {
            mzqUp.setName(mzidUp.getName());
        }

        if (mzidUp.getUnitCv() != null) {
            Cv mzqUnitCv = convertMzidCvToMzqCv(mzidUp.getUnitCv());
            mzqUp.setUnitCv(mzqUnitCv);
        }

        if (mzidUp.getUnitAccession() != null) {
            mzqUp.setUnitAccession(mzidUp.getUnitAccession());
        }

        if (mzidUp.getUnitName() != null) {
            mzqUp.setUnitName(mzidUp.getUnitName());
        }

        return mzqUp;
    }

    /**
     * Utility method to convert mzIdentML CV to mzQuantML CV.
     *
     * @param mzidCv mzIdentML CV.
     *
     * @return mzQuantML CV.
     */
    public static Cv convertMzidCvToMzqCv(
            uk.ac.ebi.jmzidml.model.mzidml.Cv mzidCv) {
        Cv mzqCv = new Cv();

        if (mzidCv.getFullName() != null) {
            mzqCv.setFullName(mzidCv.getFullName());
        }

        if (mzidCv.getId() != null) {
            mzqCv.setId(mzidCv.getId());
        }

        if (mzidCv.getUri() != null) {
            mzqCv.setUri(mzidCv.getUri());
        }

        if (mzidCv.getVersion() != null) {
            mzqCv.setVersion(mzidCv.getVersion());
        }

        return mzqCv;
    }

    /**
     * Utility method to convert mzIdentML FileFormat to mzQuantML FileFormat.
     *
     * @param mzidFF mzIdentMl FileFormat.
     *
     * @return mzQuantML FileFormat.
     */
    public static FileFormat convertMzidFileFormatToMzqFileFormat(
            uk.ac.ebi.jmzidml.model.mzidml.FileFormat mzidFF) {
        FileFormat mzqFF = new FileFormat();

        if (mzidFF.getCvParam() != null) {
            mzqFF.
                    setCvParam(convertMzidCvParamToMzqCvParam(mzidFF.
                            getCvParam()));
        }

        return mzqFF;
    }

    /**
     * Utility method to convert mzIdentML SearchDatabase to mzQuantML
     * SearchDatabase.
     *
     * @param searchDatabase mzIdentML SearchDatabase.
     *
     * @return mzQuantML SearchDatabase.
     */
    public static SearchDatabase convertMzidSDBToMzqSDB(
            uk.ac.ebi.jmzidml.model.mzidml.SearchDatabase searchDatabase) {
        SearchDatabase sDB = new SearchDatabase();
        if (searchDatabase.getId() != null) {
            sDB.setId(searchDatabase.getId());
        }

        if (searchDatabase.getName() != null) {
            sDB.setName(searchDatabase.getName());
        }

        if (searchDatabase.getNumDatabaseSequences() != null) {
            sDB.setNumDatabaseEntries(searchDatabase.getNumDatabaseSequences());
        }

        if (searchDatabase.getLocation() != null) {
            sDB.setLocation(searchDatabase.getLocation());
        }

        if (searchDatabase.getVersion() != null) {
            sDB.setVersion(searchDatabase.getVersion());
        }

        uk.ac.ebi.jmzidml.model.mzidml.Param mzidDBNameParam = searchDatabase.
                getDatabaseName();
        if (mzidDBNameParam != null) {
            Param mzqDBNameParam = new Param();
            if (mzidDBNameParam.getCvParam() != null) {
                mzqDBNameParam.setParam(convertMzidCvParamToMzqCvParam(
                        mzidDBNameParam.getCvParam()));
            }
            if (mzidDBNameParam.getUserParam() != null) {
                mzqDBNameParam.setParam(convertMzidUserParamToMzqUserParam(
                        mzidDBNameParam.getUserParam()));
            }
            sDB.setDatabaseName(mzqDBNameParam);
        }

        if (searchDatabase.getCvParam() != null && !searchDatabase.getCvParam().
                isEmpty()) {
            for (uk.ac.ebi.jmzidml.model.mzidml.CvParam cp : searchDatabase.
                    getCvParam()) {
                sDB.getCvParam().add(convertMzidCvParamToMzqCvParam(cp));
            }
        }

        // convert FileFormat
        uk.ac.ebi.jmzidml.model.mzidml.FileFormat mzidFF = searchDatabase.
                getFileFormat();

        if (mzidFF != null) {
            //FileFormat mzqFF = 
            MzidToMzqElementConverter.convertMzidFileFormatToMzqFileFormat(
                    mzidFF);
        }

        return sDB;
    }

}
