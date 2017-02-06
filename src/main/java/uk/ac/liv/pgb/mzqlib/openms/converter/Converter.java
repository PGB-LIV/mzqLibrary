package uk.ac.liv.pgb.mzqlib.openms.converter;

import java.io.File;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

/**
 * Converting OpenMS consensusXML file to mzQuantML file
 *
 */
public final class Converter {
    private Converter() {
    }

    /**
     * Main class.
     *
     * @param args input arguments.
     */
    public static void main(final String[] args) {
        File   file   = new File("CPTAC_study6_2400_3600_FLUQT.consensusXML");
        String output = "CPTAC_study6_2400_3600_FLUQT.consensusXML.mzq";

        try {
            ConsensusXMLProcessor conProc = ConsensusXMLProcessorFactory.getInstance().buildConsensusXMLProcessor(file);

            conProc.convert(output);
        } catch (IOException | JAXBException ex) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
