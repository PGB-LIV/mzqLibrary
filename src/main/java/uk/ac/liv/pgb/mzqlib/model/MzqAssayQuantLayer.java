package uk.ac.liv.pgb.mzqlib.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;

import javax.xml.bind.JAXBException;

import uk.ac.liv.pgb.jmzqml.model.mzqml.Assay;
import uk.ac.liv.pgb.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * @author Da Qi
 * @since 23-Jul-2014 15:16:04
 */
public class MzqAssayQuantLayer extends MzqQuantLayer {

    /**
     *
     * @param um         the unmarshaller of the mzq file
     * @param listId     the id of the list element (e.g. ProteinGroupList,
     *                   ProteinList, PeptideConsensusList, etc.
     * @param quantLayer the QuantLayer object
     * @param listType   the list type (e.g. Protein, ProteinGroup, etc.)
     * @param dataType   the data type of the quant layer
     *
     * @throws JAXBException jaxb exception
     */
    public MzqAssayQuantLayer(final MzQuantMLUnmarshaller um, final String listId, final QuantLayer quantLayer,
                              final String listType, final String dataType)
            throws JAXBException {
        setQuantLayerType(new SimpleStringProperty("AssayQuantLayer"));
        setMzqUm(um);
        setListId(new SimpleStringProperty(listId));
        setQuantLayerId(new SimpleStringProperty(quantLayer.getId()));
        setListType(new SimpleStringProperty(listType));
        setDataType(new SimpleStringProperty(dataType));

        // set column names
        setColumnNames(new ArrayList<>());

        List<String> columnIndex = quantLayer.getColumnIndex();

        for (String colId : columnIndex) {
            Assay assay = this.getMzqUm().unmarshal(Assay.class, colId);

            getColumnNames().add(new SimpleStringProperty(assay.getName()));
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
