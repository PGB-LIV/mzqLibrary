
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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *         &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="unique_id" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="label" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="size" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
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
    propOrder = {"userParam"}
)
@XmlRootElement(name = "map")
public class Map {

    /**
     *
     */
    protected List<UserParam> userParam;

    /**
     *
     */
    @XmlAttribute(
        name     = "name",
        required = true
    )
    protected String name;

    /**
     *
     */
    @XmlAttribute(name = "unique_id")
    protected String uniqueId;

    /**
     *
     */
    @XmlAttribute(
        name     = "id",
        required = true
    )
    @XmlSchemaType(name = "unsignedInt")
    protected long id;

    /**
     *
     */
    @XmlAttribute(name = "label")
    protected String label;

    /**
     *
     */
    @XmlAttribute(name = "size")
    @XmlSchemaType(name = "unsignedInt")
    protected Long size;

    /**
     * Gets the value of the id property.
     *
     * @return value of id.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value value of id.
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the label property.
     *
     * @return
     *         possible object is
     *         {@link String }
     *
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     *
     * @param value
     *              allowed object is
     *              {@link String }
     *
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return
     *         possible object is
     *         {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *              allowed object is
     *              {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the size property.
     *
     * @return
     *         possible object is
     *         {@link Long }
     *
     */
    public Long getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     *
     * @param value
     *              allowed object is
     *              {@link Long }
     *
     */
    public void setSize(Long value) {
        this.size = value;
    }

    /**
     * Gets the value of the uniqueId property.
     *
     * @return
     *         possible object is
     *         {@link String }
     *
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Sets the value of the uniqueId property.
     *
     * @param value
     *              allowed object is
     *              {@link String }
     *
     */
    public void setUniqueId(String value) {
        this.uniqueId = value;
    }

    /**
     * Gets the value of the userParam property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userParam
     * property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserParam().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UserParam }
     *
     *
     * @return UserParam list.
     */
    public List<UserParam> getUserParam() {
        if (userParam == null) {
            userParam = new ArrayList<>();
        }

        return this.userParam;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
