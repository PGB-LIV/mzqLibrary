
package uk.ac.liv.pgb.mzqlib;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.dialog.ProgressDialog;

import org.rosuda.JRI.Rengine;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uk.ac.liv.pgb.jmzqml.MzQuantMLElement;
import uk.ac.liv.pgb.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.pgb.jmzqml.model.mzqml.IdOnly;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Protein;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroup;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinRef;
import uk.ac.liv.pgb.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Row;
import uk.ac.liv.pgb.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.pgb.mzqlib.constants.MzqDataConstants;
import uk.ac.liv.pgb.mzqlib.model.MzQuantMLData;
import uk.ac.liv.pgb.mzqlib.model.MzQuantMLSummary;
import uk.ac.liv.pgb.mzqlib.model.MzqAssayQuantLayer;
import uk.ac.liv.pgb.mzqlib.model.MzqDataMatrixRow;
import uk.ac.liv.pgb.mzqlib.model.MzqFeatureQuantLayer;
import uk.ac.liv.pgb.mzqlib.model.MzqQuantLayer;

import uk.ac.liv.pgb.mzqlib.r.RUtils;
import uk.ac.liv.pgb.mzqlib.r.RequiredPackages;
import uk.ac.liv.pgb.mzqlib.task.CreateRMatrixTask;
import uk.ac.liv.pgb.mzqlib.task.InitialREngineTask;
import uk.ac.liv.pgb.mzqlib.task.LoadMzQuantMLDataTask;
import uk.ac.liv.pgb.mzqlib.view.CommandLineGui;
import uk.ac.liv.pgb.mzqlib.view.HeatMapPdfController;
import uk.ac.liv.pgb.mzqlib.view.MzqInfoController;
import uk.ac.liv.pgb.mzqlib.view.RootLayoutController;

/**
 * MainApp class.
 *
 * @author Da Qi
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private Stage newStage; // for heat map pdf setting window
    private BorderPane rootLayout;
    private AnchorPane mzqInfo;
    private MzqInfoController mzqInfoController;
    private Rengine re;
    private RootLayoutController rootLayoutController;

    private static final String VERSION = "1.0-beta";
    private static final String WINDOW_TITLE = "mzqViewer - mzqLibrary "
            + VERSION;

    private MzQuantMLData mzqData = new MzQuantMLData();

    /**
     * Get observable list of MzqAssayQuantLayer.
     *
     * @return the list of MzqAssayQuantLayer.
     */
    public ObservableList<MzqAssayQuantLayer> getMzqAssayQuantLayerData() {
        return mzqData.getMzqAssayQuantLayerList();
    }

    /**
     * Get observable list of MzqFeatureQuantLayer.
     *
     * @return the list of MzqFeatureQuantLayer.
     */
    public ObservableList<MzqFeatureQuantLayer> getMzqFeatureQuantLayerData() {
        return mzqData.getMzqFeatureQuantLayerList();
    }

    /**
     * Get MzQuantMLSummary.
     *
     * @return MzQuantMLSummary.
     */
    public MzQuantMLSummary getMzQuantMLSummary() {
        return mzqData.getMzQuantMLSummary();
    }

    @Override
    public void start(final Stage primaryStage) {

        //setUserAgentStylesheet(STYLESHEET_CASPIAN);
        setUserAgentStylesheet(STYLESHEET_MODENA);
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(WINDOW_TITLE);
        initRootLayout();
        readPossibleStartupParameters();
    }

    private void readPossibleStartupParameters() {
        Parameters parameters = this.getParameters();
        for (Entry<String, String> entry : parameters.getNamed().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(
                    "dataForQualityCalculationLocation")) {
                // Get the location of our quality data files.
                String location = entry.getValue();

                Rengine engine = getRengine();

                if (engine != null) {
                    engine.eval(getResourceFileText(
                            "R/featureRTDelta_Simon_format.R"));
                    engine.eval(getResourceFileText(
                            "R/pepConsHist_Simon_format.R"));

                    String assayCountFrequenciesCommand = "pepConsHist(\""
                            + location + "\\\\"
                            + "assays_per_feature_format.csv\")";
                    String deltaStatsCommand = "featureRTDelta(\"" + location
                            + "\\\\" + "rt_format.csv\")";
                    // Create our display of the distribution of the frequency of features with 1-N assays.
                    engine.eval(assayCountFrequenciesCommand);
                    // Create our display of RT delta distribution for all features.
                    engine.eval(deltaStatsCommand);
                } else {
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE,
                                                                  "R engine not initialised! Aborting creation of quality plots.");
                    return;
                }
            }
        }
    }

    private String getResourceFileText(final String resourceRelativePath) {
        BufferedReader reader;
        try {
            InputStream stream = getClass().getClassLoader().
                    getResourceAsStream(resourceRelativePath);
            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            List<String> lines = readAllLines(reader);
            String joinedLines = String.join("\n", lines);
            return joinedLines;
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        return "";
    }

    private static List<String> readAllLines(final BufferedReader reader)
            throws IOException {
        List<String> lines = new LinkedList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();

        return lines;
    }

    /**
     * Initializes the root layout.
     */
    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getClassLoader().getResource(
                    "RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            rootLayoutController = loader.getController();
            rootLayoutController.setMainApp(this);
            rootLayoutController.disbbleMenus();

            primaryStage.show();
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Get the main stage.
     *
     * @return main stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Gets the new stage.
     *
     * @return new stage.
     */
    public Stage getNewStage() {
        return newStage;
    }

    /**
     * Load mzq file with detail.
     *
     * @param mzqFile input mzq file.
     */
    public void loadMzqFile(final File mzqFile) {

        closeMzqInfo();

        LoadMzQuantMLDataTask loadMzqDataTask = new LoadMzQuantMLDataTask(
                mzqFile);

        ProgressDialog pd = new ProgressDialog(loadMzqDataTask);
        pd.setTitle("Loading file");

        pd.show();

//        Dialogs.create()
//                .title("Loading file")
//                //.masthead("For file size over 100MB, unmarshalling process could take over 10 minutes.")
//                .showWorkerProgress(loadMzqDataTask);
        loadMzqDataTask.setOnSucceeded((WorkerStateEvent t) -> {
            mzqData = loadMzqDataTask.getValue();

            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(MainApp.class.getClassLoader().getResource(
                        "MzqInfo.fxml"));
                mzqInfo = (AnchorPane) loader.load();

                rootLayout.setCenter(mzqInfo);

                // Give the controller access to the main app
                mzqInfoController = loader.getController();
                mzqInfoController.showMzqSummary(mzqData.getMzQuantMLSummary());
                mzqInfoController.setMainApp(this);
                rootLayoutController.enableMenus();
            } catch (IOException ex) {
                Logger.getLogger(RootLayoutController.class.getName()).log(
                        Level.SEVERE, null, ex);
            }

            this.primaryStage.setTitle(WINDOW_TITLE + " - " + mzqFile.
                    getAbsolutePath());
        });

        loadMzqDataTask.setOnFailed((WorkerStateEvent t) -> {
            Platform.runLater(() -> {
//                Dialogs.create()
//                        .title("File Error")
//                        .message("The input file is not a valid mzQuantML file")
//                        .showException(loadMzqDataTask.getException());
                Alert alert = exceptionDialogCreate("File Error",
                                                    "The input file is not a valid mzQuantML file",
                                                    loadMzqDataTask.
                                                    getException());
                alert.showAndWait();
            });

        });

        Thread loadMzqDataTh = new Thread(loadMzqDataTask);
        loadMzqDataTh.setName("Loading MzQuantMLData");
        loadMzqDataTh.setDaemon(true);
        loadMzqDataTh.start();
    }

    /**
     * Get mzq unmarshaller.
     *
     * @return MzQuantMLUnmarshaller.
     */
    public MzQuantMLUnmarshaller getUnmarshaller() {
        return mzqData.getMzQuantMLUnmarshaller();
    }

    /**
     * Close mzq information window.
     */
    public void closeMzqInfo() {
        rootLayout.setCenter(null);
        rootLayoutController.disbbleMenus();
        primaryStage.setTitle(WINDOW_TITLE);
    }

    /**
     * Record last opened file path.
     *
     * @param file last opened file.
     */
    public void setLastFilePath(final File file) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (file != null) {
            prefs.put("lastFilePath", file.getParent());
        } else {
            prefs.put("lastFilePath", System.getProperty("user.home"));
        }
    }

    /**
     * Get parent path of last opened file.
     *
     * @return parent path.
     */
    public File getLastFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String filePath = prefs.get("lastFilePath", null);

        if (filePath != null) {
            File res = new File(filePath);
            if (res.exists()) {
                return new File(filePath);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Utility method to show PCA plot.
     */
    public void showPCAPlot() {

        //TODO: PCA plot does not need gplots. The initialREngine() method include install gplots as required package
        //TODO: Here the code only need to initial Rengine successfully (check R.dll and jri.dll)
        if (re == null) {
            initialREngine();
        }

        if (re != null) {
            TableView<MzqDataMatrixRow> dataMatrixTable = mzqInfoController.
                    getDataMatrixTable();
            ObservableList<MzqDataMatrixRow> rowList = dataMatrixTable.
                    getItems();
            CreateRMatrixTask rmTask = new CreateRMatrixTask(rowList);

            ProgressDialog pd = new ProgressDialog(rmTask);
            pd.setTitle("Generating PCA plot");

            pd.show();
//            Dialogs.create()
//                    .title("Generating PCA plot")
//                    .showWorkerProgress(rmTask);

            rmTask.setOnSucceeded((WorkerStateEvent t) -> {
                String setMatrix = "X = matrix(c(" + rmTask.getValue().
                        getLogMatrix() + "), nrow=" + rmTask.getValue().
                        getRowNumber() + ",byrow = TRUE)";
                re.eval(setMatrix);

                //build row names from rowNames list
                StringBuilder rowNames = new StringBuilder();
                for (String rn : rmTask.getValue().getRowNames()) {
                    rowNames.append("\"");
                    rowNames.append(rn);
                    rowNames.append("\", ");
                }

                // get column names
                StringBuilder colNames = new StringBuilder();
                ObservableList<TableColumn<MzqDataMatrixRow, ?>> colList
                        = dataMatrixTable.getColumns();
                Iterator<TableColumn<MzqDataMatrixRow, ?>> i = colList.
                        iterator();
                // skip the first column name --- "Id"
                i.next();

                while (i.hasNext()) {
                    colNames.append("\"");
                    colNames.append(i.next().getText());
                    colNames.append("\"");
                    if (i.hasNext()) {
                        colNames.append(",");
                    }
                }

                re.eval("rownames(X) <- c(" + rowNames.substring(0, rowNames.
                                                                 length() - 2)
                        + ")");
                re.eval("colnames(X) <- c(" + colNames.toString() + ")");

                re.eval("require(graphics)");
                re.eval("biplot(princomp(X))");
            });

            rmTask.setOnFailed((WorkerStateEvent t) -> {
                Platform.runLater(() -> {
                    Alert alert = exceptionDialogCreate("Error",
                                                        "There are exceptions during the loading data:",
                                                        rmTask.getException());
                    alert.showAndWait();
//                    Dialogs.create()
//                            .title("Error")
//                            .message("There are exceptions during the loading data:")
//                            .showException(rmTask.getException());
                });

            });

            Thread pcaTh = new Thread(rmTask);
            pcaTh.setDaemon(true);
            pcaTh.start();
        }
    }

    /**
     * Utility method to show heat map pdf creation window.
     */
    public void showHeatMapPdfWindow() {
        if (re == null) {
            initialREngine();
        }

        if (re != null) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(RootLayoutController.class.getClassLoader().
                        getResource("HeatMapPdf.fxml"));
                AnchorPane heatMapPdf = (AnchorPane) loader.load();

                HeatMapPdfController controller = loader.getController();
                controller.setMainApp(this);
                //controller.setCaller(rootLayoutController);

                Scene scene = new Scene(heatMapPdf);
                newStage = new Stage();
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.initOwner(primaryStage);
                newStage.setTitle("Specify PDF size (inch)");
                newStage.setScene(scene);
                newStage.show();

            } catch (IOException ex) {
                Logger.getLogger(RootLayoutController.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Utility method to save heat map plot to pdf file.
     *
     * @param pdfFile   pdf file.
     * @param pdfHValue pdf file height value.
     * @param pdfWValue pdf file width value.
     */
    public void saveHeatMapPdf(final File pdfFile, final double pdfHValue,
                               final double pdfWValue) {
        newStage.hide();

        if (re == null) {
            initialREngine();
        }

        if (re != null) {
            re.eval("if (!require(\"RColorBrewer\")) {\n"
                    + "install.packages(\"RColorBrewer\")\n"
                    + "}");

            // Require package gplots      
            re.eval("if (!require(\"gplots\")) {\n"
                    + "install.packages(\"gplots\", dependencies = TRUE)\n"
                    + "}");
            // Load heatmap.2 library
            re.eval("library(\"gplots\")");

            TableView<MzqDataMatrixRow> dataMatrixTable = mzqInfoController.
                    getDataMatrixTable();
            ObservableList<MzqDataMatrixRow> rowList = dataMatrixTable.
                    getItems();
            CreateRMatrixTask rmTask = new CreateRMatrixTask(rowList);

            ProgressDialog pd = new ProgressDialog(rmTask);
            pd.setTitle("Generating PDF");

            pd.show();
//            Dialogs.create()
//                    .title("Generating PDF")
//                    .showWorkerProgress(rmTask);

            rmTask.setOnSucceeded((WorkerStateEvent t) -> {
                String pdfCmd = "pdf(file='" + pdfFile.getAbsolutePath().
                        replace('\\', '/')
                        + "', height=" + pdfHValue + ", width=" + pdfWValue
                        + ", onefile=TRUE, family='Helvetica', pointsize=20)";
                System.out.println(pdfCmd);
                re.eval(pdfCmd);

                re.eval("if (!require(\"RColorBrewer\")) {\n"
                        + "install.packages(\"RColorBrewer\")\n"
                        + "}");

                // Require package gplots      
                re.eval("if (!require(\"gplots\")) {\n"
                        + "install.packages(\"gplots\", dependencies = TRUE)\n"
                        + "}");
                // Load heatmap.2 library
                re.eval("library(\"gplots\")");

                re.eval("breaks <- seq(from = "
                        + String.valueOf(rmTask.getValue().getLogMin() * 0.9)
                        + ", to = "
                        + String.valueOf(rmTask.getValue().getLogMax() * 1.1)
                        + ", length = 51)");

                // Set color palette
                //re.eval("color.palette  <- colorRampPalette(c(\"#000000\", \"#DC2121\", \"#E9A915\"))");
                // Set x matrix
                String setMatrix = "X = matrix(c(" + rmTask.getValue().
                        getLogMatrix() + "), nrow=" + rmTask.getValue().
                        getRowNumber() + ",byrow = TRUE)";
                re.eval(setMatrix);

                //build row names from rowNames list
                StringBuilder rowNames = new StringBuilder();
                for (String rn : rmTask.getValue().getRowNames()) {
                    rowNames.append("\"");
                    rowNames.append(rn);
                    rowNames.append("\", ");
                }

                // get column names
                StringBuilder colNames = new StringBuilder();
                ObservableList<TableColumn<MzqDataMatrixRow, ?>> colList
                        = dataMatrixTable.getColumns();
                Iterator<TableColumn<MzqDataMatrixRow, ?>> i = colList.
                        iterator();
                // skip the first column name --- "Id"
                i.next();

                while (i.hasNext()) {
                    colNames.append("\"");
                    colNames.append(i.next().getText());
                    colNames.append("\"");
                    if (i.hasNext()) {
                        colNames.append(",");
                    }
                }

                re.eval("rownames(X) <- c(" + rowNames.substring(0, rowNames.
                                                                 length() - 2)
                        + ")");
                re.eval("colnames(X) <- c(" + colNames.toString() + ")");

                // Set heatmap
                String setHeatmap = "heatmap.2(X,\n"
                        + "Rowv=TRUE,\n"
                        + "Colv=TRUE,\n"
                        + "na.rm=FALSE,\n"
                        + "distfun = dist,\n"
                        + "hclustfun = hclust,\n"
                        + "key=TRUE,\n"
                        + "keysize=1,\n"
                        + "trace=\"none\",\n"
                        + "scale=\"none\",\n"
                        + "density.info=c(\"none\"),\n"
                        + "#margins=c(18, 8),\n"
                        //                    //+ "col=color.palette,\n"
                        + "breaks = breaks,\n"
                        //+ "breaks = seq(from = 1, to = 10, length = 51), \n"
                        //                    + "lhei=c(0.4,4),\n"
                        + "main=\"" + mzqInfoController.getQuantLayerTable().
                        getSelectionModel().getSelectedItem().
                        getQuantLayerType()
                        + ": " + mzqInfoController.getQuantLayerTable().
                        getSelectionModel().getSelectedItem().getQuantLayerId()
                        + "\"\n"
                        + ")";
                re.eval(setHeatmap);
                re.eval("dev.off()");

                newStage.close();

                // open the saved pdf file after generation
                if (Desktop.isDesktopSupported()) {

                    try {
                        Desktop.getDesktop().open(pdfFile);

                        // no application registered for PDFs
                    } catch (IOException ex) {
                        Logger.getLogger(MainApp.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }

                }
            });

            rmTask.setOnFailed((WorkerStateEvent t) -> {
                Platform.runLater(() -> {
                    Alert alert = exceptionDialogCreate("Error",
                                                        "There are exceptions during the loading data:",
                                                        rmTask.getException());
                    alert.showAndWait();
//                    Dialogs.create()
//                            .title("Error")
//                            .message("There are exceptions during the loading data:")
//                            .showException(rmTask.getException());
                });

            });

            Thread saveHeatMapTh = new Thread(rmTask);
            saveHeatMapTh.setDaemon(true);
            saveHeatMapTh.start();
        }
    }

    /**
     * Utility method to show heat map in R window.
     */
    public void showHeatMapinR() {

        if (re == null) {
            initialREngine();
        }

        // Start R heatamp process
        if (re != null) {

            // install gplots
            if (!RUtils.installPackage(re, RequiredPackages.GPLOTS.
                                       getPackageName())) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("R package missing");
                alert.setHeaderText(null);
                alert.setContentText(
                        "Cannot perform heatmap plot as required R packages are missing!\nPlease install them first.");

                alert.showAndWait();

//                Dialogs.create()
//                        .title("R package missing")
//                        .message("Cannot perform heatmap plot as required R packages are missing!\nPlease install them first.")
//                        .showError();
            } else {
                // Load heatmap.2 library
                //re.eval("library(\"gplots\")");
                TableView<MzqDataMatrixRow> dataMatrixTable = mzqInfoController.
                        getDataMatrixTable();
                ObservableList<MzqDataMatrixRow> rowList = dataMatrixTable.
                        getItems();
                CreateRMatrixTask rmTask = new CreateRMatrixTask(rowList);

                ProgressDialog pd = new ProgressDialog(rmTask);
                pd.setTitle("Generating heat map by R");

                pd.show();

//                Dialogs.create()
//                        .title("Generating heat map by R")
//                        .showWorkerProgress(rmTask);
                rmTask.setOnSucceeded((WorkerStateEvent t) -> {
                    re.eval("breaks <- seq(from = "
                            + String.
                            valueOf(rmTask.getValue().getLogMin() * 0.9)
                            + ", to = "
                            + String.
                            valueOf(rmTask.getValue().getLogMax() * 1.1)
                            + ", length = 51)");

                    // Set color palette
                    //re.eval("color.palette  <- colorRampPalette(c(\"#000000\", \"#DC2121\", \"#E9A915\"))");
                    // Set x matrix
                    String setMatrix = "X = matrix(c(" + rmTask.getValue().
                            getLogMatrix() + "), nrow=" + rmTask.getValue().
                            getRowNumber() + ",byrow = TRUE)";
                    re.eval(setMatrix);

                    //build row names from rowNames list
                    StringBuilder rowNames = new StringBuilder();
                    for (String rn : rmTask.getValue().getRowNames()) {
                        rowNames.append("\"");
                        rowNames.append(rn);
                        rowNames.append("\", ");
                    }

                    // get column names
                    StringBuilder colNames = new StringBuilder();
                    ObservableList<TableColumn<MzqDataMatrixRow, ?>> colList
                            = dataMatrixTable.getColumns();
                    Iterator<TableColumn<MzqDataMatrixRow, ?>> i = colList.
                            iterator();
                    // skip the first column name --- "Id"
                    i.next();

                    while (i.hasNext()) {
                        colNames.append("\"");
                        colNames.append(i.next().getText());
                        colNames.append("\"");
                        if (i.hasNext()) {
                            colNames.append(",");
                        }
                    }

                    re.eval("rownames(X) <- c(" + rowNames.substring(0,
                                                                     rowNames.
                                                                     length()
                                                                     - 2) + ")");
                    re.eval("colnames(X) <- c(" + colNames.toString() + ")");

                    // Set heatmap
                    String setHeatmap = "heatmap.2(X,\n"
                            + "Rowv=TRUE,\n"
                            + "Colv=TRUE,\n"
                            + "na.rm=FALSE,\n"
                            + "distfun = dist,\n"
                            + "hclustfun = hclust,\n"
                            + "key=TRUE,\n"
                            + "keysize=1,\n"
                            + "trace=\"none\",\n"
                            + "scale=\"none\",\n"
                            + "density.info=c(\"none\"),\n"
                            + "#margins=c(18, 8),\n"
                            //+ "col=color.palette,\n"
                            + "breaks = breaks,\n"
                            //+ "breaks = seq(from = 1, to = 10, length = 51),\n"
                            //                    + "lhei=c(0.4,4),\n"
                            + "main=\""
                            + mzqInfoController.getQuantLayerTable().
                            getSelectionModel().getSelectedItem().
                            getQuantLayerType()
                            + ": " + mzqInfoController.getQuantLayerTable().
                            getSelectionModel().getSelectedItem().
                            getQuantLayerId() + "\"\n"
                            + ")";
                    re.eval(setHeatmap);
                });

                rmTask.setOnFailed((WorkerStateEvent t) -> {
                    Platform.runLater(() -> {
                        Alert alert = exceptionDialogCreate("Error",
                                                            "There are exceptions during the loading data:",
                                                            rmTask.
                                                            getException());
                        alert.showAndWait();
//                        Dialogs.create()
//                                .title("Error")
//                                .message("There are exceptions during the loading data:")
//                                .showException(rmTask.getException());
                    });

                });

                Thread showHeatMapTh = new Thread(rmTask);
                showHeatMapTh.setDaemon(true);
                showHeatMapTh.start();
            }
        }
    }

    /**
     * Utility method to show curve plot.
     *
     * @throws JAXBException exceptions.
     */
    public void showCurve()
            throws JAXBException {
        Stage curveStage = new Stage();
        curveStage.setTitle("Quantitative measurement across assays");

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Assay id");
        final LineChart<String, Number> lineChart
                = new LineChart<>(xAxis, yAxis);

        lineChart.setTitle("Quantitative measurement for across assays");

        TableView<MzqDataMatrixRow> dataMatrixTable = mzqInfoController.
                getDataMatrixTable();

        MzqQuantLayer selectedQL = mzqInfoController.getQuantLayerTable().
                getSelectionModel().getSelectedItem();
        ObservableList<TableColumn<MzqDataMatrixRow, ?>> columns
                = dataMatrixTable.getColumns();

        if (selectedQL.getListType().equals(
                MzqDataConstants.PROTEIN_GROUP_LIST_TYPE)) {

            MzqDataMatrixRow protGrpRow = dataMatrixTable.getSelectionModel().
                    getSelectedItem();
            if (protGrpRow == null) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("No data selected");
                alert.setHeaderText(null);
                alert.setContentText("Please select a data row");

                alert.showAndWait();

//                Dialogs.create()
//                        .title("No data selected")
//                        .message("Please select a data row")
//                        .showWarning();
            } else {
                if (protGrpRow.getObjectValue().get().equals("P60843")) {
                    System.out.println("stop");
                }

                //Plot protein group quant value line
                XYChart.Series series = new XYChart.Series<>();
                lineChart.getData().add(series);

                //Set protein group line color to RED
                series.getNode().setStyle("-fx-stroke: red;");

                series.setName(protGrpRow.getObjectValue().get());
                List<StringProperty> values = protGrpRow.values();
                int i = 1;
                for (StringProperty value : values) {
                    if (NumberUtils.isNumber(value.get())) {
                        series.getData().add(new XYChart.Data(columns.get(i).
                                getText(), Double.parseDouble(value.get())));
                    } else {
                        series.getData().add(new XYChart.Data(columns.get(i).
                                getText(), -1));
                    }
                    i++;
                }

                //showProteinGroupPeptideLinePlot(protGrpRow, selectedQL.getDataType(), columns, lineChart);
                //Get the first PeptideConsensusList and AssayQuantLayer regardless of the finalResult value
                Iterator<PeptideConsensusList> peptideConsensusListIter = this.
                        getUnmarshaller().unmarshalCollectionFromXpath(
                                MzQuantMLElement.PeptideConsensusList);
                DataMatrix peptideDM = new DataMatrix();
                if (peptideConsensusListIter != null) {
                    PeptideConsensusList peptideList = peptideConsensusListIter.
                            next();

                    //Get the peptide quant layer
                    List<QuantLayer<IdOnly>> assayQLs = peptideList.
                            getAssayQuantLayer();

                    for (QuantLayer assayQL : assayQLs) {
                        if (selectedQL.getDataType().toLowerCase(Locale.ENGLISH).
                                contains("normalised")
                                && assayQL.getDataType().getCvParam().getName().
                                toLowerCase(Locale.ENGLISH).contains(
                                "normalised")) {
                            peptideDM = assayQL.getDataMatrix();
                            break;
                        }
                        if (selectedQL.getDataType().toLowerCase(Locale.ENGLISH).
                                contains("raw")
                                && assayQL.getDataType().getCvParam().getName().
                                toLowerCase(Locale.ENGLISH).contains("raw")) {
                            peptideDM = assayQL.getDataMatrix();
                            break;
                        }
                    }
                }
                Map<String, List<StringProperty>> peptideDMMap
                        = convertDataMatrixToHashMap(peptideDM);

                ProteinGroup proteinGrp = this.getUnmarshaller().unmarshal(
                        uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroup.class,
                        protGrpRow.getObjectId());

                //Take the first protein as group leader in the protein group
                ProteinRef protRef = proteinGrp.getProteinRef().get(0);

                Protein protein = this.getUnmarshaller().unmarshal(
                        uk.ac.liv.pgb.jmzqml.model.mzqml.Protein.class, protRef.
                        getProteinRef());
                List<String> peptideRefs = protein.getPeptideConsensusRefs();
                for (String peptideRef : peptideRefs) {
                    PeptideConsensus peptide = this.getUnmarshaller().unmarshal(
                            uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensus.class,
                            peptideRef);
                    if (peptideDMMap != null) {
                        List<StringProperty> peptideValues = peptideDMMap.get(
                                peptideRef);
                        if (peptideValues != null) {
                            XYChart.Series peptideSeries
                                    = new XYChart.Series<>();
                            lineChart.getData().add(peptideSeries);

                            //Set peptide lines color to GRAY
                            peptideSeries.getNode().
                                    setStyle("-fx-stroke: gray;");

                            peptideSeries.setName(peptide.getPeptideSequence());
                            //if (peptideValues != null) {
                            int k = 1;
                            for (StringProperty value : peptideValues) {
                                if (NumberUtils.isNumber(value.get())) {
                                    peptideSeries.getData().add(
                                            new XYChart.Data(
                                                    columns.get(k).getText(),
                                                    Double.
                                                    parseDouble(value.get())));

                                } else {
                                    peptideSeries.getData().add(
                                            new XYChart.Data(
                                                    columns.get(k).getText(), -1));
                                }
                                k++;
                            }
                        }
                    }
                }
            }

        } else if (selectedQL.getListType().equals(
                MzqDataConstants.PROTEIN_LIST_TYPE)) {

            MzqDataMatrixRow protRow = dataMatrixTable.getSelectionModel().
                    getSelectedItem();

            if (protRow == null) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("No data selected");
                alert.setHeaderText(null);
                alert.setContentText("Please select a data row");

                alert.showAndWait();
//                Dialogs.create()
//                        .title("No data selected")
//                        .message("Please select a data row")
//                        .showWarning();
            } else {
                //Plot protein quant value line
                XYChart.Series series = new XYChart.Series<>();
                lineChart.getData().add(series);

                //Set protein line color to RED
                series.getNode().setStyle("-fx-stroke: red;");

                series.setName(protRow.getObjectValue().get());
                List<StringProperty> values = protRow.values();
                int i = 1;
                for (StringProperty value : values) {
                    if (NumberUtils.isNumber(value.get())) {
                        series.getData().add(new XYChart.Data(columns.get(i).
                                getText(), Double.parseDouble(value.get())));
                    } else {
                        series.getData().add(new XYChart.Data(columns.get(i).
                                getText(), -1));
                    }
                    i++;
                }

                showProteinPeptideLinePlot(protRow, selectedQL.getDataType(),
                                           columns, lineChart);
            }
        } else {
            ObservableList<MzqDataMatrixRow> rowList = dataMatrixTable.
                    getSelectionModel().getSelectedItems();

            if (rowList.isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("No data selected");
                alert.setHeaderText(null);
                alert.setContentText("Please select a data row");

                alert.showAndWait();

//                Dialogs.create()
//                        .title("No data selected")
//                        .message("Please select a data row")
//                        .showWarning();
            } else {
                for (MzqDataMatrixRow row : rowList) {
                    XYChart.Series series = new XYChart.Series();
                    lineChart.getData().add(series);
                    series.setName(row.getObjectValue().get());
                    List<StringProperty> values = row.values();
                    int i = 1;
                    for (StringProperty value : values) {
                        if (NumberUtils.isNumber(value.get())) {
                            series.getData().add(new XYChart.Data(
                                    dataMatrixTable.getColumns().get(i).
                                    getText(), Double.parseDouble(value.get())));
                        } else {
                            series.getData().add(new XYChart.Data(
                                    dataMatrixTable.getColumns().get(i).
                                    getText(), -1));
                        }
                        i++;
                    }
                }
            }
        }

        //show the chart only when data row is seleted.
        //without if condition, empty chart will show if no data row is selected
        if (dataMatrixTable.getSelectionModel().getSelectedItem() != null) {
            Scene scene = new Scene(lineChart, 800, 600);

            curveStage.setScene(scene);
            curveStage.show();
        }
    }

    /**
     * Utility method to show GUI.
     */
    public void showGui() {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.
                        getSystemLookAndFeelClassName());
                CommandLineGui gui = new CommandLineGui();
                gui.setTitle("Mzq library command line GUI");
                gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                gui.setVisible(true);
            } catch (ClassNotFoundException | InstantiationException 
                    | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
                Logger.getLogger(MainApp.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        });

    }

    /**
     * Show about window.
     */
    public void showAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("mzqViewer/mzqLibrary ver " + VERSION);
        alert.setContentText(
                "mzQuantML library is a toolset for post-processing mzQuantML file. For more information, please visit http://code.google.com/p/mzq-lib/.");

        alert.showAndWait();
//        Dialogs.create()
//                .title("About")
//                .masthead("mzqViewer/mzqLibrary ver " + VERSION)
//                .message("mzQuantML library is a toolset for post-processing mzQuantML file. For more information, please visit http://code.google.com/p/mzq-lib/.")
//                .showInformation();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        launch(args);
    }

    /**
     * Show the plot line of quantitative value from identified peptides for a
     * specific protein
     *
     * @param protRow   the selected protein row
     * @param dataType  the data type of the protein quant layer
     * @param lineChart the plot chart
     *
     * @throws JAXBException
     */
    private void showProteinPeptideLinePlot(final MzqDataMatrixRow protRow,
                                            final String dataType,
                                            final ObservableList<TableColumn<MzqDataMatrixRow, ?>> columns,
                                            final LineChart<String, Number> lineChart)
            throws JAXBException {

        //Get the first PeptideConsensusList and AssayQuantLayer regardless of the finalResult value
        Iterator<PeptideConsensusList> peptideConsensusListIter = this.
                getUnmarshaller().unmarshalCollectionFromXpath(
                        MzQuantMLElement.PeptideConsensusList);
        DataMatrix peptideDM = new DataMatrix();
        if (peptideConsensusListIter != null) {
            PeptideConsensusList peptideList = peptideConsensusListIter.next();

            //Get the peptide quant layer
            List<QuantLayer<IdOnly>> assayQLs = peptideList.getAssayQuantLayer();

            for (QuantLayer assayQL : assayQLs) {
                if (dataType.contains("normalised")
                        && assayQL.getDataType().getCvParam().getName().
                        contains("normalised")) {
                    peptideDM = assayQL.getDataMatrix();
                    break;
                }
                if (dataType.contains("raw")
                        && assayQL.getDataType().getCvParam().getName().
                        contains("raw")) {
                    peptideDM = assayQL.getDataMatrix();
                    break;
                }
            }
        }

        Map<String, List<StringProperty>> peptideDMMap
                = convertDataMatrixToHashMap(peptideDM);

        Protein protein = this.getUnmarshaller().unmarshal(
                uk.ac.liv.pgb.jmzqml.model.mzqml.Protein.class, protRow.
                getObjectId());
        List<String> peptideRefs = protein.getPeptideConsensusRefs();
        for (String peptideRef : peptideRefs) {
            PeptideConsensus peptide = this.getUnmarshaller().unmarshal(
                    uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensus.class,
                    peptideRef);
            if (peptideDMMap != null) {
                List<StringProperty> peptideValues = peptideDMMap.
                        get(peptideRef);
                XYChart.Series peptideSeries = new XYChart.Series<>();
                lineChart.getData().add(peptideSeries);
                peptideSeries.setName(peptide.getPeptideSequence());
                if (peptideValues != null) {
                    int k = 1;
                    for (StringProperty value : peptideValues) {
                        if (NumberUtils.isNumber(value.get())) {
                            peptideSeries.getData().add(new XYChart.Data(
                                    columns.
                                    get(k).getText(), Double.
                                    parseDouble(
                                            value.
                                            get())));
                        } else {
                            peptideSeries.getData().add(new XYChart.Data(
                                    columns.
                                    get(k).getText(), -1));
                        }
                        k++;
                    }
                }
            }
        }
    }

    /**
     *
     * @param protGrpRow
     * @param dataType
     * @param lineChart
     */
//    private void showProteinGroupPeptideLinePlot(MzqDataMatrixRow protGrpRow,
//                                                 String dataType,
//                                                 ObservableList<TableColumn<MzqDataMatrixRow, ?>> columns,
//                                                 LineChart<String, Number> lineChart)
//            throws JAXBException {
//
//        //Get the first PeptideConsensusList and AssayQuantLayer regardless of the finalResult value
//        Iterator<PeptideConsensusList> peptideConsensusListIter = this.getUnmarshaller().unmarshalCollectionFromXpath(MzQuantMLElement.PeptideConsensusList);
//        DataMatrix peptideDM = new DataMatrix();
//        if (peptideConsensusListIter != null) {
//            PeptideConsensusList peptideList = peptideConsensusListIter.next();
//
//            //Get the peptide quant layer
//            List<QuantLayer<IdOnly>> assayQLs = peptideList.getAssayQuantLayer();
//
//            for (QuantLayer assayQL : assayQLs) {
//                if (dataType.contains("normalised")
//                        && assayQL.getDataType().getCvParam().getName().contains("normalised")) {
//                    peptideDM = assayQL.getDataMatrix();
//                    break;
//                }
//                if (dataType.contains("raw")
//                        && assayQL.getDataType().getCvParam().getName().contains("raw")) {
//                    peptideDM = assayQL.getDataMatrix();
//                    break;
//                }
//            }
//        }
//        Map<String, List<StringProperty>> peptideDMMap = convertDataMatrixToHashMap(peptideDM);
//
//        ProteinGroup proteinGrp = this.getUnmarshaller().unmarshal(uk.ac.liv.pgb.jmzqml.model.mzqml.ProteinGroup.class, protGrpRow.getObjectId());
//
//        //Take the first protein as group leader in the protein group
//        ProteinRef protRef = proteinGrp.getProteinRef().get(0);
//
//        Protein protein = this.getUnmarshaller().unmarshal(uk.ac.liv.pgb.jmzqml.model.mzqml.Protein.class, protRef.getProteinRef());
//        List<String> peptideRefs = protein.getPeptideConsensusRefs();
//        for (String peptideRef : peptideRefs) {
//            PeptideConsensus peptide = this.getUnmarshaller().unmarshal(uk.ac.liv.pgb.jmzqml.model.mzqml.PeptideConsensus.class, peptideRef);
//            List<StringProperty> peptideValues = peptideDMMap.get(peptideRef);
//            XYChart.Series peptideSeries = new XYChart.Series<>();
//            lineChart.getData().add(peptideSeries);
//            peptideSeries.setName(peptide.getPeptideSequence());
//            if (peptideValues != null) {
//                int k = 1;
//                for (StringProperty value : peptideValues) {
//                    if (NumberUtils.isNumber(value.get())) {
//                        peptideSeries.getData().add(new XYChart.Data(columns.get(k).getText(), Double.parseDouble(value.get())));
//
//                    }
//                    else {
//                        peptideSeries.getData().add(new XYChart.Data(columns.get(k).getText(), -1));
//                    }
//                    k++;
//                }
//            }
//        }
//
//    }
    /**
     * Convert DataMatrix to HashMap
     *
     * @param peptideDM input DataMatrix object
     *
     * @return a HashMap contains the same information as in DataMatrix
     */
    private Map<String, List<StringProperty>> convertDataMatrixToHashMap(
            final DataMatrix peptideDM) {
        if (peptideDM == null) {
            return null;
        } else {
            Map<String, List<StringProperty>> retHashMap = new HashMap<>();

            List<Row> rows = peptideDM.getRow();

            if (rows.isEmpty()) {
                return retHashMap;
            } else {
                for (Row row : rows) {
                    retHashMap.put(row.getObjectRef(), listStringProperty(row.
                                   getValue()));
                }
                return retHashMap;
            }
        }
    }

    /**
     * Convert a list of String to a list of StringProperty
     *
     * @param values input list of String
     *
     * @return a list of StringProperty
     */
    private List<StringProperty> listStringProperty(final List<String> values) {
        if (values == null) {
            return null;
        } else {
            List<StringProperty> retList = new ArrayList<>();
            for (String s : values) {
                retList.add(new SimpleStringProperty(s));
            }
            return retList;
        }
    }

    /**
     * Utility method to install required R packages.
     */
    public void installRequiredPackages() {
        if (re == null) {
            initialREngine();
        } else if (RUtils.installRequiredPackages(re)) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("R packages");
                alert.setHeaderText(null);
                alert.setContentText("Required R packages has been installed.");

                alert.showAndWait();

//                Dialogs.create()
//                        .title("R packages")
//                        .message("Required R packages has been installed.")
//                        .showInformation();
            });
        }
    }

    private synchronized Rengine getRengine() {
        if (re != null) {
            return re;
        }
        try {
            System.loadLibrary("jri");
            re = new Rengine(new String[]{" ", " "}, false, null);
            Platform.runLater(() -> {
                RUtils.installRequiredPackages(re);
            });
        } catch (UnsatisfiedLinkError ex) {
            Logger.getLogger(MainApp.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        return null;
    }

    private void initialREngine() {

        // test if jri is installed correctly
        try {
            System.loadLibrary("jri");

            InitialREngineTask iniR = new InitialREngineTask();
            iniR.setOnSucceeded((WorkerStateEvent t) -> {
                re = iniR.getValue();
            });

            iniR.setOnFailed((WorkerStateEvent t) -> {

                Alert alert = exceptionDialogCreate("Error",
                                                    "There are exceptions during the R engine initialisation:",
                                                    iniR.getException());
                alert.showAndWait();
//                Dialogs.create()
//                        .title("Error")
//                        .message("There are exceptions during the R engine initalisation:")
//                        .showException(iniR.getException());

            });

            Thread iniRTh = new Thread(iniR);
            iniRTh.setDaemon(true);
            iniRTh.start();
        } catch (UnsatisfiedLinkError e) {
            Alert alert = exceptionDialogCreate("JRI package Error",
                                                "The R and JRI are not properly installed.\nAll R related functions will not work.\nPlease find out how to set up at http://code.google.com/p/mzq-lib/#How_to_run_mzqViewer_with_R",
                                                e);
            alert.showAndWait();
//            Dialogs.create()
//                    .title("JRI package Error")
//                    .message("The R and JRI are not properly installed.\nAll R related functions will not work.\nPlease find out how to set up at http://code.google.com/p/mzq-lib/#How_to_run_mzqViewer_with_R")
//                    .showException(e);

        }
    }

    /**
     * Create exception dialog.
     *
     * @param title     dialog title.
     * @param content   dialog content.
     * @param exception dialog exception.
     *
     * @return Alert dialog.
     */
    protected Alert exceptionDialogCreate(final String title,
                                          final String content,
                                          final Throwable exception) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
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

        //Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        return alert;
    }

}
