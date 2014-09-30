package uk.ac.liv.mzqlib.view;

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
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import uk.ac.liv.mzqlib.MainApp;
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
    private TableColumn<MzqAssayQuantLayer, String> dataType;
    @FXML
    private TableView<MzqDataMatrixRow> dataMatrixTable;
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
        dataType.setCellValueFactory(cellData -> cellData.getValue().getDataType());

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
        software.textProperty().bind(sum.getSoftware());
    }

    private void showAssayQLDetails(MzqAssayQuantLayer assayQL) {
        if (assayQL != null) {
            // Clear the table
            dataMatrixTable.getColumns().clear();
            dataMatrixTable.getItems().clear();

            ContextMenu popupMenu = new ContextMenu();
            MenuItem curveMenuItem = new MenuItem("Line plot");

            popupMenu.getItems().add(curveMenuItem);
            dataMatrixTable.setContextMenu(popupMenu);

            curveMenuItem.setOnAction(new EventHandler() {

                @Override
                public void handle(Event event) {
                    mainApp.showCurve();
                }

            });

            // Set table column resizable policy
            dataMatrixTable.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

            // Set selection mode to  multiple
            dataMatrixTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // The first column
            TableColumn<MzqDataMatrixRow, String> idCol = new TableColumn("Id");
            idCol.setCellValueFactory(cellData -> cellData.getValue().ObjectId());

            dataMatrixTable.getColumns().add(idCol);

            int i = 0;
            for (StringProperty colName : assayQL.getColumnNames()) {
                final int j = i;
                TableColumn<MzqDataMatrixRow, String> col = new TableColumn(colName.getValue());
                col.setCellValueFactory(cellData -> cellData.getValue().Value(j));
                i++;
                dataMatrixTable.getColumns().add(col);
            }
            dataMatrixTable.getItems().addAll(assayQL.getDmRows());
        }
        else {
            //remove all the text.
        }
    }

    public TableView<MzqDataMatrixRow> getDataMatrixTable() {
        return dataMatrixTable;
    }

}
