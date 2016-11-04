
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

    private Map<Integer, MzqDataControlElement> pgLevel = new HashMap<>();
    private Map<Integer, MzqDataControlElement> proteinLevel
            = new HashMap<>();
    private Map<Integer, MzqDataControlElement> peptideLevel
            = new HashMap<>();
    private Map<Integer, MzqDataControlElement> featureLevel
            = new HashMap<>();

    /**
     * Add control element.
     *
     * @param level   the level to add element.
     * @param type    the type of the element.
     * @param element the element to be added.
     */
    public void addElement(int level, int type, String element) {
        getControlElement(level, type).addElement(element);
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
    public boolean isRequired(int level, int type, String quantityName) {
        return getControlElement(level, type).isRequired(quantityName);
    }

    /**
     * Get set of elements.
     *
     * @param level the level in consideration.
     * @param type  the type of elements.
     *
     * @return set of elements.
     */
    public Set<String> getElements(int level, int type) {
        return getControlElement(level, type).getElements();
    }

    private MzqDataControlElement getControlElement(int level, int type) {
        Map<Integer, MzqDataControlElement> map = null;
        switch (level) {
            case MzqData.PROTEIN_GROUP:
                map = pgLevel;
                break;
            case MzqData.PROTEIN:
                map = proteinLevel;
                break;
            case MzqData.PEPTIDE:
                map = peptideLevel;
                break;
            case MzqData.FEATURE:
                map = featureLevel;
                break;
            default:
                break;
        }
        if (map == null) {
            throw new IllegalStateException(
                    "Unrecognized quantitation level, program exits in MzqDataControl.java");
        }

        if (!map.containsKey(type)) {
            MzqDataControlElement controlElement = new MzqDataControlElement();
            map.put(type, controlElement);
        }
        return map.get(type);
    }

}

class MzqDataControlElement {

    private Set<String> elements = new HashSet<>();

    boolean isRequired(String quantityName) {
        if (elements.isEmpty()) {
            return false;
        }
        if (elements.contains(quantityName)) {
            return true;
        }
        return false;
    }

    Set<String> getElements() {
        return elements;
    }

    void addElement(String element) {
        elements.add(element);
    }

}
