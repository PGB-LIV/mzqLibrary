package uk.ac.liv.pgb.mzqlib.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;

import javafx.event.Event;
import javafx.event.EventHandler;

import javafx.fxml.FXML;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javax.xml.bind.JAXBException;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

import uk.ac.liv.pgb.mzqlib.MainApp;
import uk.ac.liv.pgb.mzqlib.constants.MzqDataConstants;
import uk.ac.liv.pgb.mzqlib.model.MzQuantMLSummary;
import uk.ac.liv.pgb.mzqlib.model.MzqDataMatrixRow;
import uk.ac.liv.pgb.mzqlib.model.MzqQuantLayer;

/**
 * FXML Controller class
 *
 * @author Da Qi
 */
public class MzqInfoController {
    private MainApp                            mainApp;
    @FXML
    private TableView<MzqQuantLayer>           quantLayerTable;
    @FXML
    private TableColumn<MzqQuantLayer, String> assayQLId;
    @FXML
    private TableColumn<MzqQuantLayer, String> parentListId;
    @FXML
    private TableColumn<MzqQuantLayer, String> dataType;
    @FXML
    private TableView<MzqDataMatrixRow>        dataMatrixTable;
    @FXML
    @SuppressWarnings("unused")
    private Label                              techniqueUsed;
    @FXML
    private Label                              software;
    @FXML
    private Label                              numberProteinGroupList;
    @FXML
    private Label                              numberProteinList;
    @FXML
    private Label                              numberPeptideList;
    @FXML
    private Label                              numberFeatureList;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        assayQLId.setCellValueFactory(cellData -> cellData.getValue().quantLayerId());
        parentListId.setCellValueFactory(cellData -> cellData.getValue().listId());
        dataType.setCellValueFactory(cellData -> cellData.getValue().dataType());

        // Clear person details.
        showAssayQLDetails(null);

        // Listen for selection changes and show the person details when changed.
        quantLayerTable.getSelectionModel()
                       .selectedItemProperty()
                       .addListener((observable, oldValue, newValue) -> showAssayQLDetails(newValue));
    }

    private void showAssayQLDetails(final MzqQuantLayer assayQL) {
        if (assayQL != null) {

            // if (assayQL.getListId())
            // Clear the table
            dataMatrixTable.getColumns().clear();
            dataMatrixTable.getItems().clear();

            ContextMenu popupMenu     = new ContextMenu();
            MenuItem    curveMenuItem = new MenuItem("Line plot");

            popupMenu.getItems().add(curveMenuItem);
            dataMatrixTable.setContextMenu(popupMenu);
            curveMenuItem.setOnAction(new EventHandler() {
                                          @Override
                                          public void handle(final Event event) {
                                              try {
                                                  mainApp.showCurve();
                                              } catch (JAXBException ex) {
                                                  Logger.getLogger(MzqInfoController.class.getName())
                                                        .log(Level.SEVERE, null, ex);
                                              }
                                          }
                                      });

            // Set selection mode
            if (assayQL.getListType().equals(MzqDataConstants.PROTEIN_GROUP_LIST_TYPE)
                    || assayQL.getListType().equals(MzqDataConstants.PROTEIN_LIST_TYPE)) {
                dataMatrixTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            } else {
                dataMatrixTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            }

            // The first column
            TableColumn<MzqDataMatrixRow, String> idCol = new TableColumn("Id");

            idCol.setCellValueFactory(cellData -> cellData.getValue().getObjectValue());
            dataMatrixTable.getColumns().add(idCol);

            int i = 0;

            for (StringProperty colName : assayQL.getColumnNames()) {
                final int                             j   = i;
                TableColumn<MzqDataMatrixRow, String> col = new TableColumn(colName.getValue());

                col.setCellValueFactory(cellData -> cellData.getValue().value(j));
                i++;
                dataMatrixTable.getColumns().add(col);
            }

            dataMatrixTable.getItems().addAll(assayQL.getDmRows());
        }

//      else {
//          //remove all the text.
//      }
        // Set table column resizable policy
        dataMatrixTable.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Utility method to show mzq summary.
     *
     * @param sum mzq summary.
     */
    public void showMzqSummary(final MzQuantMLSummary sum) {
        numberProteinGroupList.textProperty().bind(Bindings.format("%d", sum.proteinGroupListNumber()));
        numberProteinList.textProperty().bind(Bindings.format("%d", sum.proteinListNumber()));
        numberPeptideList.textProperty().bind(Bindings.format("%d", sum.peptideListNumber()));
        numberFeatureList.textProperty().bind(Bindings.format("%d", sum.featureListNumber()));
        software.textProperty().bind(sum.getSoftware());
    }

    /**
     * Get DataMatrix table.
     *
     * @return data matrix table view.
     */
    public TableView<MzqDataMatrixRow> getDataMatrixTable() {
        return dataMatrixTable;
    }

    /**
     * Set main app.
     *
     * @param mainApp main app.
     */
    public void setMainApp(final MainApp mainApp) {
        this.mainApp = mainApp;
        quantLayerTable.getItems().addAll(mainApp.getMzqAssayQuantLayerData());
        quantLayerTable.getItems().addAll(mainApp.getMzqFeatureQuantLayerData());
    }

    /**
     * Get QuantLayer table.
     *
     * @return quant layer table view.
     */
    public TableView<MzqQuantLayer> getQuantLayerTable() {
        return quantLayerTable;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
