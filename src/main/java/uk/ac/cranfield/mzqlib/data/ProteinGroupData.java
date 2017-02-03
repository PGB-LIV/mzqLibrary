
package uk.ac.cranfield.mzqlib.data;

import java.util.List;
import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroup;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinRef;

/**
 * ProteinGroupData class.
 *
 * @author Jun
 */
public class ProteinGroupData extends QuantitationLevel {

    private ProteinGroup pg;
    private  static final String SEPARATOR = ";";

    /**
     * Constructor of ProteinGroupData.
     *
     * @param proteinGroup ProteinGroup.
     */
    public ProteinGroupData(final ProteinGroup proteinGroup) {
        pg = proteinGroup;
    }

    /**
     * Get ProteinGroup id.
     *
     * @return id.
     */
    public String getId() {
        return pg.getId();
    }

    /**
     * Get Anchor Protein String.
     *
     * @return anchor protein string.
     */
    public String getAnchorProteinStr() {
        ProteinRef lead = pg.getProteinRef().get(0); //ProteinRef 1:n
        return lead.getProteinRef();
    }

    /**
     * Get ambiguity protein members in one String.
     *
     * @return ambiguity proteins.
     */
    public String getAmbiguityMemberStr() {
        List<ProteinRef> proteinRefs = pg.getProteinRef();
        if (proteinRefs.size() == 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < proteinRefs.size(); i++) {
            ProteinRef ref = proteinRefs.get(i);
            ProteinData protein = MzqLib.DATA.getProtein(ref.getProteinRef());
            sb.append(protein.getAccession());
            sb.append(SEPARATOR);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
