
//
//This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
//See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
//Any modifications to this file will be lost upon recompilation of the source schema. 
//Generated on: 2014.03.18 at 02:59:01 PM GMT 
//
package uk.ac.liv.pgb.mzqlib.openms.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{}consensusElement" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name      = "",
    propOrder = {"consensusElement"}
)
@XmlRootElement(name = "consensusElementList")
public class ConsensusElementList {

    /**
     *
     */
    @XmlElement(required = true)
    protected List<ConsensusElement> consensusElement;

    /**
     * The consensus element combines corresponding elements of the maps. Gets
     * the value of the consensusElement property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the
     * consensusElement property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConsensusElement().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConsensusElement }
     *
     *
     * @return ConsensusElement list.
     */
    public List<ConsensusElement> getConsensusElement() {
        if (consensusElement == null) {
            consensusElement = new ArrayList<>();
        }

        return this.consensusElement;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
