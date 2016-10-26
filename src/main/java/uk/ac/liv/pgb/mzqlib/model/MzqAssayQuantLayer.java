
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
 * @institute University of Liverpool
 * @time 23-Jul-2014 15:16:04
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
    public MzqAssayQuantLayer(MzQuantMLUnmarshaller um, String listId,
                              QuantLayer quantLayer,
                              String listType,
                              String dataType)
            throws JAXBException {
        this.quantLayerType = new SimpleStringProperty("AssayQuantLayer");
        this.mzqUm = um;
        this.listId = new SimpleStringProperty(listId);
        this.quantLayerId = new SimpleStringProperty(quantLayer.getId());
        this.listType = new SimpleStringProperty(listType);
        this.dataType = new SimpleStringProperty(dataType);

        //set column names 
        this.columnNames = new ArrayList<>();
        List<String> columnIndex = quantLayer.getColumnIndex();
        for (String colId : columnIndex) {
            Assay assay = mzqUm.unmarshal(
                    Assay.class, colId);
            this.columnNames.add(new SimpleStringProperty(assay.getName()));
        }
    }

}
