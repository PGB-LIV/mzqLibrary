
package uk.ac.cranfield.mzqlib.data;

import java.util.List;
import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroup;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinRef;

/**
 *
 * @author Jun
 */
public class ProteinGroupData extends QuantitationLevel {

    private ProteinGroup pg;
    private final static String SEPARATOR = ";";

    public ProteinGroupData(ProteinGroup proteinGroup) {
        pg = proteinGroup;
    }

//    public ProteinGroup getPg() {
//        return pg;
//    }
    public String getId() {
        return pg.getId();
    }

    public String getAnchorProteinStr() {
        ProteinRef lead = pg.getProteinRef().get(0);//ProteinRef 1:n
        return lead.getProteinRef();
    }

    public String getAmbiguityMemberStr() {
        List<ProteinRef> proteinRefs = pg.getProteinRef();
        if (proteinRefs.size() == 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < proteinRefs.size(); i++) {
            ProteinRef ref = proteinRefs.get(i);
            ProteinData protein = MzqLib.data.getProtein(ref.getProteinRef());
            sb.append(protein.getAccession());
            sb.append(SEPARATOR);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
