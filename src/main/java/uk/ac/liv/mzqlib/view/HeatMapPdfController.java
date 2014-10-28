package uk.ac.liv.mzqlib.view;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.apache.commons.lang.math.NumberUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;
import uk.ac.liv.mzqlib.MainApp;

/**
 * FXML Controller class
 *
 * @author Da Qi
 */
public class HeatMapPdfController {

    //Reference to the main application
    private MainApp mainApp;
    private RootLayoutController rootController;

    @FXML
    TextField pdfH;

    @FXML
    TextField pdfW;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setCaller(RootLayoutController c) {
        this.rootController = c;
    }

    @FXML
    private void handleHeatMapinR() {

        if (NumberUtils.isNumber(pdfH.getText()) && NumberUtils.isNumber(pdfW.getText())
                && !pdfH.getText().trim().equals("0") && !pdfW.getText().trim().equals("0")) {
            //mainApp.showHeatMapinR(Double.parseDouble(pdfH.getText()),
            //                       Double.parseDouble(pdfW.getText()));

            // show file chooser
            FileChooser fileChooser = new FileChooser();

            if (mainApp.getLastFilePath() != null) {
                fileChooser.setInitialDirectory(mainApp.getLastFilePath());
            }
            else {
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            }
            // Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);

            // Set the title for file dialog
            fileChooser.setTitle("Save to pdf file");

            // Show open file dialog
            File pdfFile = fileChooser.showSaveDialog(mainApp.getNewStage());

            if (pdfFile != null) {
                double pdfHValue = Double.parseDouble(pdfH.getText().trim());
                double pdfWValue = Double.parseDouble(pdfW.getText().trim());
                mainApp.saveHeatMapPdf(pdfFile, pdfHValue, pdfWValue);
            }
        }
        else {
            Action response = Dialogs.create()
                    .title("Something wrong with the input demension!")
                    .message("The input Height or Width is invalid or empty. They must be numbers.")
                    .showError();
        }

        //mainApp.getNewStage().close();
    }

}