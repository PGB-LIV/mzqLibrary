package uk.ac.liv.pgb.mzqlib.view;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.net.URL;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import javax.xml.datatype.DatatypeConfigurationException;

import org.controlsfx.dialog.ProgressDialog;

import uk.ac.liv.pgb.mzqlib.progenesis.converter.ProgenMzquantmlConverter;

/**
 * FXML Controller class
 *
 * @author Da Qi
 */
public class ProgenesisConverterController implements Initializable {
    private static File currentDirectory;
    @FXML
    ComboBox            separatorBox;
    @FXML
    TextField           flFileTextField;
    @FXML
    TextField           plFileTextField;
    @FXML
    TextField           idFileTextField;

    @FXML
    @SuppressWarnings("unused")
    private void convert() {

        // Both peptide list and protein list files are missing
        if (this.flFileTextField.getText().isEmpty() && this.plFileTextField.getText().isEmpty()) {
            Alert alert = new Alert(AlertType.ERROR);

            alert.setTitle("File missing");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one list csv file!");
            alert.showAndWait();

//          Dialogs.create()
//                  .title("File missing")
//                  .message("Please select at least one list csv file!")
//                  .showError();
        } else if ((!this.flFileTextField.getText().isEmpty() &&!this.flFileTextField.getText().endsWith(".csv"))
                   || (!this.plFileTextField.getText().isEmpty() &&!this.plFileTextField.getText().endsWith(".csv"))) {    // Either of the file names is not end with ".csv"
            Alert alert = new Alert(AlertType.ERROR);

            alert.setTitle("Wrong file format");
            alert.setHeaderText(null);
            alert.setContentText("The selected list file(s) is/are not in csv format! Please select the right format.");
            alert.showAndWait();

//          Dialogs.create()
//                  .title("Wrong file format")
//                  .message("The selected list file(s) is/are not in csv format! Please select the right format.")
//                  .showError();
        } else {
            FileChooser fileChooser = new FileChooser();

            fileChooser.setTitle("Save the MzQuantML file");
            fileChooser.setInitialDirectory(currentDirectory);

            // ... Applying file extension filters ...//
            ExtensionFilter filter = new ExtensionFilter("MzQuantML (*.mzq)", "*.mzq");

            fileChooser.getExtensionFilters().add(filter);

            File outputFile = fileChooser.showSaveDialog(null);

            if (outputFile != null) {
                final String outputFn = outputFile.getAbsolutePath().endsWith("mzq")
                                        ? outputFile.getAbsolutePath()
                                        : outputFile.getAbsolutePath() + ".mzq";

                // get the separator
                Character separatorObject = ',';    // Default is comma

                if (separatorBox.getSelectionModel().getSelectedItem().equals(",")) {
                    separatorObject = ',';
                } else if (separatorBox.getSelectionModel().getSelectedItem().equals(";")) {
                    separatorObject = ';';
                } else if (separatorBox.getSelectionModel().getSelectedItem().equals("Tab")) {
                    separatorObject = '\t';
                }

                final char   separator   = separatorObject;
                final String flFn        = flFileTextField.getText();
                final String plFn        = plFileTextField.getText();
                final String idFn        = idFileTextField.getText();
                Task<String> convertTask = new Task<String>() {
                    @Override
                    protected String call() throws IOException, DatatypeConfigurationException {
                        updateMessage("Start converting");

                        ProgenMzquantmlConverter progenConv = new ProgenMzquantmlConverter(flFn, plFn, idFn, separator);

                        progenConv.convert(outputFn, true, null);
                        updateMessage("Converting finished");
                        updateProgress(1, 1);

                        return outputFn;
                    }
                };

                convertTask.setOnSucceeded(
                    (WorkerStateEvent t) -> {
                        Platform.runLater(
                            () -> {
                                if (convertTask.isDone()) {
                                    Alert alert = new Alert(AlertType.INFORMATION);

                                    alert.setTitle("Converting succeeded");
                                    alert.setHeaderText(null);
                                    alert.setContentText("The mzQuantML file is stored in " + convertTask.getValue());
                                    alert.showAndWait();

                                    // Dialogs.create()
                                    // .title("Converting succeeded")
                                    // .message("The MzQuantML file is stored in " + convertTask.getValue())
                                    // .showInformation();
                                }
                            });
                    });
                convertTask.setOnFailed(
                    (WorkerStateEvent t) -> {
                        Platform.runLater(
                            () -> {
                                Alert alert = new Alert(AlertType.ERROR);

                                alert.setTitle("Convert failed");
                                alert.setHeaderText(null);
                                alert.setContentText("There are exceptions during the conversion process:");

                                StringWriter sw = new StringWriter();
                                PrintWriter  pw = new PrintWriter(sw);

                                convertTask.getException().printStackTrace(pw);

                                String   exceptionText = sw.toString();
                                Label    label         = new Label("The exception stacktrace was:");
                                TextArea textArea      = new TextArea(exceptionText);

                                textArea.setEditable(false);
                                textArea.setWrapText(true);
                                textArea.setMaxWidth(Double.MAX_VALUE);
                                textArea.setMaxHeight(Double.MAX_VALUE);
                                GridPane.setVgrow(textArea, Priority.ALWAYS);
                                GridPane.setHgrow(textArea, Priority.ALWAYS);

                                GridPane expContent = new GridPane();

                                expContent.setMaxWidth(Double.MAX_VALUE);
                                expContent.add(label, 0, 0);
                                expContent.add(textArea, 0, 1);

                                // Set expandable Exception into the dialog pane.
                                alert.getDialogPane().setExpandableContent(expContent);
                                alert.showAndWait();

                                // Dialogs.create()
                                // .title("Convert failed")
                                // .message("There are exceptions during the conversion process:")
                                // .showException(convertTask.getException());
                            });
                    });

                ProgressDialog pd = new ProgressDialog(convertTask);

                pd.setTitle("Converting progress");
                pd.show();

//              Dialogs.create()
//                      .title("Converting progress")
//                      .showWorkerProgress(convertTask);
                Thread convertTh = new Thread(convertTask);

                convertTh.setDaemon(true);
                convertTh.start();
            }
        }
    }

    /**
     * Initializes the controller class.
     *
     * @param url url
     * @param rb  resource bundle
     */
    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        ObservableList<String> options = FXCollections.observableArrayList(",", ";", "Tab");

        separatorBox.setItems(options);
        separatorBox.setValue(",");
    }

    @FXML
    @SuppressWarnings("unused")
    private void selectFeatureFile() {
        FileChooser fileChooser = new FileChooser();

        if (currentDirectory == null) {
            try {
                currentDirectory = new File(new File(".").getCanonicalPath());
            } catch (IOException ex) {
                Logger.getLogger(ProgenesisConverterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        fileChooser.setTitle("Select a feature list file");
        fileChooser.setInitialDirectory(currentDirectory);

        // ... Applying file extension filters ...//
        ExtensionFilter filter = new ExtensionFilter("CSV(*.csv)", "*.csv");

        fileChooser.getExtensionFilters().add(filter);

        File flFile = fileChooser.showOpenDialog(null);

        if (flFile != null) {
            flFileTextField.setText(flFile.getAbsolutePath());
            currentDirectory = flFile.getParentFile();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void selectIdentFile() {
        FileChooser fileChooser = new FileChooser();

        if (currentDirectory == null) {
            try {
                currentDirectory = new File(new File(".").getCanonicalPath());
            } catch (IOException ex) {
                Logger.getLogger(ProgenesisConverterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        fileChooser.setTitle("Select an identification file");
        fileChooser.setInitialDirectory(currentDirectory);

        // ... Applying file extension filters ...//
        ExtensionFilter filter = new ExtensionFilter("CSV(*.csv)", "*.csv");

        fileChooser.getExtensionFilters().add(filter);

        File idFile = fileChooser.showOpenDialog(null);

        if (idFile != null) {
            idFileTextField.setText(idFile.getAbsolutePath());
            currentDirectory = idFile.getParentFile();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void selectProteinFile() {
        FileChooser fileChooser = new FileChooser();

        if (currentDirectory == null) {
            try {
                currentDirectory = new File(new File(".").getCanonicalPath());
            } catch (IOException ex) {
                Logger.getLogger(ProgenesisConverterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        fileChooser.setTitle("Select a protein list file");
        fileChooser.setInitialDirectory(currentDirectory);

        // ... Applying file extension filters ...//
        ExtensionFilter filter = new ExtensionFilter("CSV(*.csv)", "*.csv");

        fileChooser.getExtensionFilters().add(filter);

        File plFile = fileChooser.showOpenDialog(null);

        if (plFile != null) {
            plFileTextField.setText(plFile.getAbsolutePath());
            currentDirectory = plFile.getParentFile();
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
