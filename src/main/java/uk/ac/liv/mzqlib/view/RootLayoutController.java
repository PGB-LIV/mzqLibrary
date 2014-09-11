/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.liv.mzqlib.view;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import uk.ac.liv.mzqlib.MainApp;

/**
 * FXML Controller class
 *
 * @author Da Qi
 */
public class RootLayoutController {

    // Reference to the main application
    private MainApp mainApp;

    private Stage newStage;
    private File file; //mzq file

    @FXML
    private Menu heatmap;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public Stage getStage() {
        return newStage;
    }

    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();

        if (mainApp.getLastFilePath() != null) {
            fileChooser.setInitialDirectory(mainApp.getLastFilePath());
        }
        else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        // Set extension filter
        ExtensionFilter extFilter = new ExtensionFilter("mzQuantML file (*.mzq)", "*.mzq");
        fileChooser.getExtensionFilters().add(extFilter);

        // Set the title for file dialog
        fileChooser.setTitle("Select an mzQuantML file");

        // Show open file dialog
        file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (file != null) {

            mainApp.loadMzqFile(file);
            mainApp.setLastFilePath(file);
        }
    }

    @FXML
    private void handleClose() {
        mainApp.closeMzqInfo();
    }

    @FXML
    private void handleHeatMapinR() {
        mainApp.showHeatMapinR();
    }

    @FXML
    private void handleHeatMapPdf() {
        mainApp.showHeatMapPdfWindow();
    }

    @FXML
    private void handlePCA() {
        mainApp.showPCAPlot();
    }

    @FXML
    private void handleCurve() {
        mainApp.showCurve();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    public void enableHeatMap() {
        heatmap.setDisable(false);
    }

    public void disbbleHeatMap() {
        heatmap.setDisable(true);
    }

}
