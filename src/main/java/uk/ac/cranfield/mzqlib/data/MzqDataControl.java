package uk.ac.cranfield.mzqlib.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * MzqData Control Class.
 *
 * @author Jun Fan@cranfield
 */
public class MzqDataControl {
    private final Map<Integer, MzqDataControlElement> pgLevel      = new HashMap<>();
    private final Map<Integer, MzqDataControlElement> proteinLevel = new HashMap<>();
    private final Map<Integer, MzqDataControlElement> peptideLevel = new HashMap<>();
    private final Map<Integer, MzqDataControlElement> featureLevel = new HashMap<>();

    /**
     * Add control element.
     *
     * @param level   the level to add element.
     * @param type    the type of the element.
     * @param element the element to be added.
     */
    public final void addElement(final int level, final int type, final String element) {
        getControlElement(level, type).addElement(element);
    }

    private MzqDataControlElement getControlElement(final int level, final int type) {
        Map<Integer, MzqDataControlElement> map = null;

        switch (level) {
        case MzqData.PROTEIN_GROUP :
            map = pgLevel;

            break;

        case MzqData.PROTEIN :
            map = proteinLevel;

            break;

        case MzqData.PEPTIDE :
            map = peptideLevel;

            break;

        case MzqData.FEATURE :
            map = featureLevel;

            break;

        default :
            break;
        }

        if (map == null) {
            throw new IllegalStateException("Unrecognized quantitation level, program exits in MzqDataControl.java");
        }

        if (!map.containsKey(type)) {
            MzqDataControlElement controlElement = new MzqDataControlElement();

            map.put(type, controlElement);
        }

        return map.get(type);
    }

    /**
     * Get set of elements.
     *
     * @param level the level in consideration.
     * @param type  the type of elements.
     *
     * @return set of elements.
     */
    public final Set<String> getElements(final int level, final int type) {
        return getControlElement(level, type).getElements();
    }

    /**
     * Judge if an element is required.
     *
     * @param level        the level in consideration.
     * @param type         the type of the element.
     * @param quantityName the quantity name.
     *
     * @return ture if specified element is required.
     */
    public final boolean isRequired(final int level, final int type, final String quantityName) {
        return getControlElement(level, type).isRequired(quantityName);
    }
}


class MzqDataControlElement {
    private final Set<String> elements = new HashSet<>();

    void addElement(final String element) {
        elements.add(element);
    }

    Set<String> getElements() {
        return elements;
    }

    boolean isRequired(final String quantityName) {
        if (elements.isEmpty()) {
            return false;
        }

        if (elements.contains(quantityName)) {
            return true;
        }

        return false;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
