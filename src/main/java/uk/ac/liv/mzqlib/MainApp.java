package uk.ac.liv.mzqlib;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;
import org.rosuda.JRI.Rengine;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.mzqlib.model.*;
import uk.ac.liv.mzqlib.task.*;
import uk.ac.liv.mzqlib.view.*;

public class MainApp extends Application {

    private Stage primaryStage;
    private Stage newStage; // for heat map pdf setting window
    private BorderPane rootLayout;
    private AnchorPane mzqInfo;
    private MzqInfoController mzqInfoController;
    private static Rengine re;
    private RootLayoutController rootLayoutController;

    private static final String VERSION = "1.0";
    private static final String INCEPTION_YEAR = "2014";

    private MzQuantMLData mzqData = new MzQuantMLData();

    public ObservableList<MzqAssayQuantLayer> getMzqAssayQuantLayerData() {
        return mzqData.getMzqAssayQuantLayerList();
    }

    public MzQuantMLSummary getMzQuantMLSummary() {
        return mzqData.getMzQuantMLSummary();
    }

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("mzQuantML library");
        initRootLayout();

        InitialREngineTask iniR = new InitialREngineTask();
        iniR.setOnSucceeded((WorkerStateEvent t) -> {
            re = iniR.getValue();
        });

        Thread iniRTh = new Thread(iniR);
        iniRTh.setDaemon(true);
        iniRTh.start();
    }

    /**
     * Initializes the root layout.
     */
    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getClassLoader().getResource("RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            rootLayoutController = loader.getController();
            rootLayoutController.setMainApp(this);
            rootLayoutController.disbbleMenus();

            primaryStage.show();
        }
        catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Returns the main stage.
     *
     * @return main stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Stage getNewStage() {
        return newStage;
    }

    public void loadMzqFile(File mzqFile) {

        closeMzqInfo();

        LoadMzQuantMLDataTask loadMzqDataTask = new LoadMzQuantMLDataTask(mzqFile);

        Dialogs.create()
                .title("Loading file")
                .masthead("For file size over 100MB, unmarshalling process could take over 10 minutes.")
                .showWorkerProgress(loadMzqDataTask);

        loadMzqDataTask.setOnSucceeded((WorkerStateEvent t) -> {
            mzqData = loadMzqDataTask.getValue();

            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(MainApp.class.getClassLoader().getResource("MzqInfo.fxml"));
                mzqInfo = (AnchorPane) loader.load();

                rootLayout.setCenter(mzqInfo);

                // Give the controller access to the main app
                mzqInfoController = loader.getController();
                mzqInfoController.showMzqSummary(mzqData.getMzQuantMLSummary());
                mzqInfoController.setMainApp(this);
                rootLayoutController.enableMenus();
            }
            catch (IOException ex) {
                Logger.getLogger(RootLayoutController.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.primaryStage.setTitle("mzQuantML library - " + mzqFile.getAbsolutePath());
        });

        loadMzqDataTask.setOnFailed((WorkerStateEvent t) -> {
            Action response = Dialogs.create()
                    .title("File Error")
                    .message("The input file is not a valid mzQuantML file")
                    .showError();
        });

        Thread loadMzqDataTh = new Thread(loadMzqDataTask);
        loadMzqDataTh.setName("Loading MzQuantMLData");
        loadMzqDataTh.setDaemon(true);
        loadMzqDataTh.start();
    }

    public MzQuantMLUnmarshaller getUnmarshaller() {
        return mzqData.getMzQuantMLUnmarshaller();
    }

    public void closeMzqInfo() {
        rootLayout.setCenter(null);
        rootLayoutController.disbbleMenus();
        primaryStage.setTitle("mzQuantML library");
    }

    public void setLastFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (file != null) {
            prefs.put("lastFilePath", file.getParent());
        }
        else {
            prefs.put("lastFilePath", System.getProperty("user.home"));
        }
    }

    public File getLastFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String filePath = prefs.get("lastFilePath", null);
        if (filePath != null) {
            return new File(filePath);
        }
        else {
            return null;
        }
    }

    public void showHeatMapPdfWindow() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(RootLayoutController.class.getClassLoader().getResource("HeatMapPdf.fxml"));
            AnchorPane heatMapPdf = (AnchorPane) loader.load();

            HeatMapPdfController controller = loader.getController();
            controller.setMainApp(this);
            controller.setCaller(rootLayoutController);

            Scene scene = new Scene(heatMapPdf);
            newStage = new Stage();
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(primaryStage);
            newStage.setTitle("Specify PDF size (inch)");
            newStage.setScene(scene);
            newStage.show();

        }
        catch (IOException ex) {
            Logger.getLogger(RootLayoutController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showPCAPlot() {
        TableView<MzqDataMatrixRow> dataMatrixTable = mzqInfoController.getDataMatrixTable();
        ObservableList<MzqDataMatrixRow> rowList = dataMatrixTable.getItems();
        CreateRMatrixTask rmTask = new CreateRMatrixTask(rowList);

        Dialogs.create()
                .title("Generating PCA plot")
                .showWorkerProgress(rmTask);

        rmTask.setOnSucceeded((WorkerStateEvent t) -> {
            String setMatrix = "X = matrix(c(" + rmTask.getValue().getMatrix() + "), nrow=" + rmTask.getValue().getRowNumber() + ",byrow = TRUE)";
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
            ObservableList<TableColumn<MzqDataMatrixRow, ?>> colList = dataMatrixTable.getColumns();
            Iterator<TableColumn<MzqDataMatrixRow, ?>> i = colList.iterator();
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

            re.eval("rownames(X) <- c(" + rowNames.substring(0, rowNames.length() - 2) + ")");
            re.eval("colnames(X) <- c(" + colNames.toString() + ")");

            re.eval("require(graphics)");
            re.eval("biplot(princomp(X))");
        });

        Thread pcaTh = new Thread(rmTask);
        pcaTh.setDaemon(true);
        pcaTh.start();
    }

    public void saveHeatMapPdf(File pdfFile, double pdfHValue, double pdfWValue) {
        newStage.hide();

        re.eval("if (!require(\"RColorBrewer\")) {\n"
                + "install.packages(\"RColorBrewer\")\n"
                + "}");

        // Require package gplots      
        re.eval("if (!require(\"gplots\")) {\n"
                + "install.packages(\"gplots\", dependencies = TRUE)\n"
                + "}");
        // Load heatmap.2 library
        re.eval("library(\"gplots\")");

        TableView<MzqDataMatrixRow> dataMatrixTable = mzqInfoController.getDataMatrixTable();
        ObservableList<MzqDataMatrixRow> rowList = dataMatrixTable.getItems();
        CreateRMatrixTask rmTask = new CreateRMatrixTask(rowList);

        Dialogs.create()
                .title("Generating PDF")
                .showWorkerProgress(rmTask);

        rmTask.setOnSucceeded((WorkerStateEvent t) -> {
            String pdfCmd = "pdf(file='" + pdfFile.getAbsolutePath().replace('\\', '/')
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
                    + String.valueOf(rmTask.getValue().getMin())
                    + ", to = "
                    + String.valueOf(rmTask.getValue().getMax())
                    + ", length = 200)");

            // Set color palette
            //re.eval("color.palette  <- colorRampPalette(c(\"#000000\", \"#DC2121\", \"#E9A915\"))");
            // Set x matrix
            String setMatrix = "X = matrix(c(" + rmTask.getValue().getMatrix() + "), nrow=" + rmTask.getValue().getRowNumber() + ",byrow = TRUE)";
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
            ObservableList<TableColumn<MzqDataMatrixRow, ?>> colList = dataMatrixTable.getColumns();
            Iterator<TableColumn<MzqDataMatrixRow, ?>> i = colList.iterator();
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

            re.eval("rownames(X) <- c(" + rowNames.substring(0, rowNames.length() - 2) + ")");
            re.eval("colnames(X) <- c(" + colNames.toString() + ")");

            // Set heatmap
            String setHeatmap = "heatmap.2(X,\n"
                    //                    + "Rowv=TRUE,\n"
                    //                    + "Colv=TRUE,\n"
                    //                    + "na.rm=FALSE,\n"
                    //                    + "distfun = dist,\n"
                    //                    + "hclustfun = hclust,\n"
                    //                    + "key=TRUE,\n"
                    //                    + "keysize=1,\n"
                    //                    + "trace=\"none\",\n"
                    //                    + "scale=\"none\",\n"
                    //                    + "density.info=c(\"none\"),\n"
                    //                    + "#margins=c(18, 8),\n"
                    //                    //+ "col=color.palette,\n"
                    //                    + "breaks = breaks,\n"
                    //                    + "lhei=c(0.4,4),\n"
                    + "main=\"Heatmap of Progenesis: normalised abundance in ProteinList AssayQuantLayer\"\n"
                    + ")";
            re.eval(setHeatmap);
            re.eval("dev.off()");

            newStage.close();

            // open the saved pdf file after generation
            if (Desktop.isDesktopSupported()) {
                try {

                    Desktop.getDesktop().open(pdfFile);
                }
                catch (IOException ex) {
                    // no application registered for PDFs
                }
            }
        });

        Thread saveHeatMapTh = new Thread(rmTask);
        saveHeatMapTh.setDaemon(true);
        saveHeatMapTh.start();

    }

    public void showHeatMapinR() {
        //System.out.println("JLP = " + System.getProperty("java.library.path"));

        if (!Rengine.versionCheck()) {
            System.err.println("** Version mismatch - Java files don't match library version.");
            //System.exit(1);
        }

        // Start R heatamp process
        //re.eval("source(\"http://www.bioconductor.org/biocLite.R\")");
        //re.eval("biocLite(\"ALL\")");
        // Require package RColorBrewer
        re.eval("if (!require(\"RColorBrewer\")) {\n"
                + "install.packages(\"RColorBrewer\")\n"
                + "}");

        // Require package gplots      
        re.eval("if (!require(\"gplots\")) {\n"
                + "install.packages(\"gplots\", dependencies = TRUE)\n"
                + "}");
        // Load heatmap.2 library
        re.eval("library(\"gplots\")");

        TableView<MzqDataMatrixRow> dataMatrixTable = mzqInfoController.getDataMatrixTable();
        ObservableList<MzqDataMatrixRow> rowList = dataMatrixTable.getItems();
        CreateRMatrixTask rmTask = new CreateRMatrixTask(rowList);

        Dialogs.create()
                .title("Generating heat map by R")
                .showWorkerProgress(rmTask);

        rmTask.setOnSucceeded((WorkerStateEvent t) -> {
            re.eval("breaks <- seq(from = "
                    + String.valueOf(rmTask.getValue().getMin())
                    + ", to = "
                    + String.valueOf(rmTask.getValue().getMax())
                    + ", length = 200)");

            // Set color palette
            re.eval("color.palette  <- colorRampPalette(c(\"#000000\", \"#DC2121\", \"#E9A915\"))");

            // Set x matrix
            String setMatrix = "X = matrix(c(" + rmTask.getValue().getMatrix() + "), nrow=" + rmTask.getValue().getRowNumber() + ",byrow = TRUE)";
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
            ObservableList<TableColumn<MzqDataMatrixRow, ?>> colList = dataMatrixTable.getColumns();
            Iterator<TableColumn<MzqDataMatrixRow, ?>> i = colList.iterator();
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

            re.eval("rownames(X) <- c(" + rowNames.substring(0, rowNames.length() - 2) + ")");
            re.eval("colnames(X) <- c(" + colNames.toString() + ")");

            // Set heatmap
            String setHeatmap = "heatmap.2(X,"
                    //                    + "Rowv=TRUE,\n"
                    //                    + "Colv=TRUE,\n"
                    //                    + "na.rm=FALSE,\n"
                    //                    + "distfun = dist,\n"
                    //                    + "hclustfun = hclust,\n"
                    //                    + "key=TRUE,\n"
                    //                    + "keysize=1,\n"
                    //                    + "trace=\"none\",\n"
                    //                    + "scale=\"none\",\n"
                    //                    + "density.info=c(\"none\"),\n"
                    //                    + "#margins=c(18, 8),\n"
                    //                    + "col=color.palette,\n"
                    //                    + "breaks = breaks,\n"
                    //                    + "lhei=c(0.4,4),\n"
                    + "main=\"Heatmap of Progenesis: normalised abundance in ProteinList\""
                    + ")";
            re.eval(setHeatmap);
        });

        Thread showHeatMapTh = new Thread(rmTask);
        showHeatMapTh.setDaemon(true);
        showHeatMapTh.start();

    }

    public void showCurve() {
        Stage curveStage = new Stage();
        curveStage.setTitle("Quantitative measurement across assays");

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Assay id");
        final LineChart<String, Number> lineChart
                = new LineChart<>(xAxis, yAxis);

        lineChart.setTitle("Quantitative measurement for across assays");

        TableView<MzqDataMatrixRow> dataMatrixTable = mzqInfoController.getDataMatrixTable();
        ObservableList<MzqDataMatrixRow> rowList = dataMatrixTable.getSelectionModel().getSelectedItems();

        for (MzqDataMatrixRow row : rowList) {
            XYChart.Series series = new XYChart.Series();
            lineChart.getData().add(series);
            series.setName(row.getObjetId());
            List<StringProperty> values = row.Values();
            int i = 1;
            for (StringProperty value : values) {
                if (NumberUtils.isNumber(value.get())) {
                    series.getData().add(new XYChart.Data(dataMatrixTable.getColumns().get(i).getText(), Double.parseDouble(value.get())));
                }
                else {
                    series.getData().add(new XYChart.Data(dataMatrixTable.getColumns().get(i).getText(), -1));
                }
                i++;
            }

        }

        Scene scene = new Scene(lineChart, 800, 600);

        curveStage.setScene(scene);
        curveStage.show();
    }

    public void showGui() {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                CommandLineGui gui = new CommandLineGui();
                gui.setTitle("Mzq library command line GUI");
                gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                gui.setVisible(true);
            }
            catch (ClassNotFoundException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        });

    }

    public void showAbout() {
        Dialogs.create()
                .title("About")
                .masthead("Mzq Library ver " + VERSION)
                .message("Mzq library is a toolset for post-processing MzQuantML file. For more information, please visit http://code.google.com/p/mzq-lib/.")
                .showInformation();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        launch(args);
    }

}
