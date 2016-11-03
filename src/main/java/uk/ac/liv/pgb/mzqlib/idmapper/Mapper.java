
package uk.ac.liv.pgb.mzqlib.idmapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 07-Mar-2014 10:27:50
 */
public class Mapper {

    private static File mzqFile;
    private static Map<String, String> rawToMzidMap;
    private static MzQuantMLUnmarshaller umarsh;

    /**
     * Main class.
     *
     * @param args input arguments.
     *
     * @throws IOException I/O exceptions.
     */
    public static void main(String args[])
            throws IOException {

        //String outMzFn = "CPTAC-Progenesis-after-mapper.mzq";                
        //mzqFile = new File("CPTAC-Progenesis.mzq");
        mzqFile = new File("CPTAC-Progenesis-small-example.mzq");
        File outMzFile = new File("CPTAC-Progenesis-after-mapper-small.mzq");
        rawToMzidMap = new HashMap();
        rawToMzidMap.put("mam_042408o_CPTAC_study6_6B011.raw",
                         "mam_042408o_CPTAC_study6_6B011_rt.mzid");
        rawToMzidMap.put("mam_050108o_CPTAC_study6_6B011.raw",
                         "mam_050108o_CPTAC_study6_6B011_rt.mzid");
        rawToMzidMap.put("mam_050108o_CPTAC_study6_6B011_080504231912.raw",
                         "mam_050108o_CPTAC_study6_6B011_080504231912_rt.mzid");
        rawToMzidMap.put("mam_042408o_CPTAC_study6_6C008.raw",
                         "mam_042408o_CPTAC_study6_6C008_rt.mzid");
        rawToMzidMap.put("mam_050108o_CPTAC_study6_6C008.raw",
                         "mam_050108o_CPTAC_study6_6C008_rt.mzid");
        rawToMzidMap.put("mam_050108o_CPTAC_study6_6C008_080505040419.raw",
                         "mam_050108o_CPTAC_study6_6C008_080505040419_rt.mzid");
        rawToMzidMap.put("mam_042408o_CPTAC_study6_6D004.raw",
                         "mam_042408o_CPTAC_study6_6D004_rt.mzid");
        rawToMzidMap.put("mam_050108o_CPTAC_study6_6D004.raw",
                         "mam_050108o_CPTAC_study6_6D004_rt.mzid");
        rawToMzidMap.put("mam_050108o_CPTAC_study6_6D004_080505084927.raw",
                         "mam_050108o_CPTAC_study6_6D004_080505084927_rt.mzid");
        rawToMzidMap.put("mam_042408o_CPTAC_study6_6E004.raw",
                         "mam_042408o_CPTAC_study6_6E004_rt.mzid");
        rawToMzidMap.put("mam_050108o_CPTAC_study6_6E004.raw",
                         "mam_050108o_CPTAC_study6_6E004_rt.mzid");
        rawToMzidMap.put("mam_050108o_CPTAC_study6_6E004_080505133441.raw",
                         "mam_050108o_CPTAC_study6_6E004_080505133441_rt.mzid");
        try {
            umarsh = new MzQuantMLUnmarshaller(mzqFile);

            MzqMzIdMapper mapper = MzqMzIdMapperFactory.getInstance().
                    buildMzqMzIdMapper(umarsh, rawToMzidMap);
            mapper.createMappedFile(outMzFile);
        } catch (JAXBException ex) {
            Logger.getLogger(Mapper.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
    }

}
