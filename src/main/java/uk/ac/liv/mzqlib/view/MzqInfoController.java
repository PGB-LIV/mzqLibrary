/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.view;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import javax.xml.bind.JAXBException;
import uk.ac.liv.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
import uk.ac.liv.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.Row;
import uk.ac.liv.mzqlib.MainApp;
import uk.ac.liv.mzqlib.model.MzQuantMLData;
import uk.ac.liv.mzqlib.model.MzQuantMLSummary;
import uk.ac.liv.mzqlib.model.MzqAssayQuantLayer;
import uk.ac.liv.mzqlib.model.MzqDataMatrixRow;

/**
 * FXML Controller class
 *
 * @author Da Qi
 */
public class MzqInfoController {

    private MainApp mainApp;

    @FXML
    private TableView<MzqAssayQuantLayer> assayQuantLayerTable;
    @FXML
    private TableColumn<MzqAssayQuantLayer, String> assayQLId;
    @FXML
    private TableColumn<MzqAssayQuantLayer, String> parentListId;
    @FXML
    private TableView dataMatrixTable;
    @FXML
    private Label techniqueUsed;
    @FXML
    private Label software;
    @FXML
    private Label numberProteinGroupList;
    @FXML
    private Label numberProteinList;
    @FXML
    private Label numberPeptideList;
    @FXML
    private Label numberFeatureList;

    public MzqInfoController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        assayQLId.setCellValueFactory(cellData -> cellData.getValue().quantLayerId());
        parentListId.setCellValueFactory(cellData -> cellData.getValue().listId());

        // Clear person details.
        showAssayQLDetails(null);

// Listen for selection changes and show the person details when changed.
        assayQuantLayerTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showAssayQLDetails(newValue));
              
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        assayQuantLayerTable.setItems(mainApp.getMzqAssayQuantLayerData());
    }

    public void showMzqSummary(MzQuantMLSummary sum) {

        numberProteinGroupList.textProperty().bind(Bindings.format("%d", sum.proteinGroupListNumber()));
        numberProteinList.textProperty().bind(Bindings.format("%d", sum.proteinListNumber()));
        numberPeptideList.textProperty().bind(Bindings.format("%d", sum.peptideListNumber()));
        numberFeatureList.textProperty().bind(Bindings.format("%d", sum.featureListNumber()));

    }

    private void showAssayQLDetails(MzqAssayQuantLayer assayQL) {
        if (assayQL != null) {
            // Clear the table
            dataMatrixTable.getColumns().clear();
            dataMatrixTable.getItems().clear();

            ContextMenu addMenu = new ContextMenu();
            addMenu.getItems().add(new MenuItem("test"));
            dataMatrixTable.setContextMenu(addMenu);

            // Set table column resizable policy
            dataMatrixTable.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

            // Set selection mode to  multiple
            dataMatrixTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            QuantLayer ql = assayQL.getQuantLayer();
            List<String> columnNames = ql.getColumnIndex();

            // The first column
            TableColumn<MzqDataMatrixRow, String> idCol = new TableColumn("Id");
            idCol.setCellValueFactory(cellData -> cellData.getValue().ObjectId());

            dataMatrixTable.getColumns().add(idCol);

            int i = 0;
            for (String colName : columnNames) {
                final int j = i;
                TableColumn<MzqDataMatrixRow, String> col = new TableColumn(colName);
                col.setCellValueFactory(cellData -> cellData.getValue().Value(j));
                i++;
                dataMatrixTable.getColumns().add(col);
            }
            DataMatrix dm = ql.getDataMatrix();

            List<Row> rowList = dm.getRow();
            for (Row row : rowList) {
                try {
                    ObservableList<String> values = FXCollections.observableArrayList();
                    MzqDataMatrixRow qlRow = new MzqDataMatrixRow();

                    // get object reference
                    String objectId = row.getObjectRef();
                    String objectType = assayQL.getRowObjectType();
                    switch (objectType) {
                        case "Protein":
                            Protein prot = mainApp.getUnmarshaller().unmarshal(uk.ac.liv.jmzqml.model.mzqml.Protein.class, objectId);
                            qlRow.setObjectId(new SimpleStringProperty(prot.getAccession()));
                            break;
                        case "PeptideConsensus":
                            PeptideConsensus pep = mainApp.getUnmarshaller().unmarshal(uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus.class, objectId);
                            qlRow.setObjectId(new SimpleStringProperty(pep.getPeptideSequence()));
                            break;
                        default:
                            qlRow.setObjectId(new SimpleStringProperty(objectId));
                            break;
                    }

                    // get value of each row
                    values.addAll(row.getValue());
                    qlRow.setValues(values);
                    dataMatrixTable.getItems().add(qlRow);
                }
                catch (JAXBException ex) {
                    Logger.getLogger(MzqInfoController.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Unmarshall exception: " + ex.getMessage());
                }
            }

        }
        else {
            // Person is null, remove all the text.

        }
    }

    public TableView<MzqDataMatrixRow> getDataMatrixTable() {
        return dataMatrixTable;
    }

}
