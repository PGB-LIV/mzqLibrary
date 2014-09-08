
package uk.ac.liv.mzqlib;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.rosuda.JRI.Rengine;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.jmzqml.model.mzqml.IdOnly;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList;
import uk.ac.liv.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.SmallMoleculeList;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.liv.mzqlib.model.MzqAssayQuantLayer;
import uk.ac.liv.mzqlib.model.MzqDataMatrixRow;
import uk.ac.liv.mzqlib.view.HeatMapPdfController;
import uk.ac.liv.mzqlib.view.MzqInfoController;
import uk.ac.liv.mzqlib.view.RootLayoutController;

public class MainApp extends Application {

    private Stage primaryStage;
    private Stage newStage; // for heat map pdf setting window
    private BorderPane rootLayout;
    private AnchorPane mzqInfo;
    private MzQuantMLUnmarshaller mzqUm;
    private MzqInfoController mzqInfoController;
    private static Rengine re;
    private RootLayoutController rootLayoutController;
    private int rowNumber; // for heat map
    private double min; // for heat map
    private double max; // for heat map

    private final ObservableList<MzqAssayQuantLayer> assayQuantLayerData = FXCollections.observableArrayList();

//    static {
//        try {
//            System.load("jri.dll");
//        }
//        catch (UnsatisfiedLinkError e) {
//            System.err.println("Native code library failed to load.\n" + e);
//            System.exit(1);
//        }
//    }
    public ObservableList<MzqAssayQuantLayer> getMzqAssayQuantLayerData() {
        return assayQuantLayerData;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("mzQuantML library");
        initRootLayout();
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
            rootLayoutController.disbbleHeatMap();
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
        mzqUm = new MzQuantMLUnmarshaller(mzqFile);
        this.primaryStage.setTitle("mzQuantML library - " + mzqFile.getAbsolutePath());

        // Build assayQuantLayerData
        assayQuantLayerData.clear();
        assayQuantLayerData.addAll(getAssayQuantLayerList(mzqUm));
    }

    public MzQuantMLUnmarshaller getUnmarshaller() {
        return mzqUm;
    }

    public void showMzqInfo() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getClassLoader().getResource("MzqInfo.fxml"));
            mzqInfo = (AnchorPane) loader.load();

            rootLayout.setCenter(mzqInfo);

            // Give the controller access to the main app
            mzqInfoController = loader.getController();
            mzqInfoController.showMzqInfo(mzqUm);
            mzqInfoController.setMainApp(this);
            rootLayoutController.enableHeatMap();
        }
        catch (IOException ex) {
            Logger.getLogger(RootLayoutController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeMzqInfo() {
        rootLayout.setCenter(null);
        rootLayoutController.disbbleHeatMap();
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

    private String createMatrixForR() {
        TableView<MzqDataMatrixRow> dataMatrixTable = mzqInfoController.getDataMatrixTable();
        ObservableList<MzqDataMatrixRow> rowList = dataMatrixTable.getItems();
        rowNumber = rowList.size();
        String x = "";

        // The minimum value of the values
        min = 0;

        // The maximum value of the values
        max = 0;

        for (MzqDataMatrixRow row : rowList) {
            List<StringProperty> values = row.Values();
            for (StringProperty value : values) {
                x = x + value.get() + ",";
                if (Double.parseDouble(value.get()) < min) {
                    min = Double.parseDouble(value.get());
                }
                if (Double.parseDouble(value.get()) > max) {
                    max = Double.parseDouble(value.get());
                }
            }
        }
        x = x.substring(0, x.lastIndexOf(","));

        return x;
    }

    public void showPCAPlot() {
        String x = createMatrixForR();

        // Set x matrix
        String setMatrix = "X = matrix(c(" + x + "), nrow=" + rowNumber + ",byrow = TRUE)";
        re.eval(setMatrix);
        re.eval("require(graphics)");
        re.eval("biplot(princomp(X))");
    }

    public void saveHeatMapPdf(File pdfFile, double pdfHValue, double pdfWValue) {
        re.eval("if (!require(\"RColorBrewer\")) {\n"
                + "install.packages(\"RColorBrewer\")\n"
                + "}");

        // Require package gplots      
        re.eval("if (!require(\"gplots\")) {\n"
                + "install.packages(\"gplots\", dependencies = TRUE)\n"
                + "}");
        // Load heatmap.2 library
        re.eval("library(\"gplots\")");

        String x = createMatrixForR();

        String pdfCmd = "pdf(file='" + pdfFile.getAbsolutePath().replace('\\', '/')
                + "', height=" + pdfHValue + ", width=" + pdfWValue
                + ", onefile=TRUE, family='Helvetica', pointsize=20)";
        System.out.println(pdfCmd);
        re.eval(pdfCmd);
        //re.eval("pdf(file='D:/test.pdf', height=50, width=20, onefile=TRUE, family='Helvetica', pointsize=20)");
//        pdf(file='D:\\HLA Epitopes\\r_epi_workspace\\derek_hms\\AB_epitopes_euro_heatmap.pdf', 
//        height=50, width=20, onefile=TRUE, family='Helvetica', pointsize=20)
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
                + String.valueOf(min)
                + ", to = "
                + String.valueOf(max)
                + ", length = 200)");

        // Set color palette
        re.eval("color.palette  <- colorRampPalette(c(\"#000000\", \"#DC2121\", \"#E9A915\"))");

        // Set x matrix
        String setMatrix = "X = matrix(c(" + x + "), nrow=" + rowNumber + ",byrow = TRUE)";
        re.eval(setMatrix);

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
                + "col=color.palette,\n"
                + "breaks = breaks,\n"
                + "lhei=c(0.4,4),\n"
                + "main=\"Heatmap of\"\n"
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
    }

    public void showHeatMapinR() {
        //System.out.println("JLP = " + System.getProperty("java.library.path"));

        if (!Rengine.versionCheck()) {
            System.err.println("** Version mismatch - Java files don't match library version.");
            //System.exit(1);
        }

        String x = createMatrixForR();

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

        re.eval("breaks <- seq(from = "
                + String.valueOf(min)
                + ", to = "
                + String.valueOf(max)
                + ", length = 200)");

        // Set color palette
        re.eval("color.palette  <- colorRampPalette(c(\"#000000\", \"#DC2121\", \"#E9A915\"))");

        // Set x matrix
        String setMatrix = "X = matrix(c(" + x + "), nrow=" + rowNumber + ",byrow = TRUE)";
        re.eval(setMatrix);

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
                + "col=color.palette,\n"
                + "breaks = breaks,\n"
                + "lhei=c(0.4,4),\n"
                + "main=\"Heatmap of\"\n"
                + ")";
        re.eval(setHeatmap);

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
        re = new Rengine(args, false, null);
        launch(args);
    }

    /**
     * Get all the existing AssayQuantLayer(s) from the given
     * MzQuantMLUnmarshaller
     *
     * @param mzqUm the MzQuantMLUnmarshaller
     *
     * @return a list of MzqAssayQuantLayer objects
     */
    private List<MzqAssayQuantLayer> getAssayQuantLayerList(
            MzQuantMLUnmarshaller mzqUm) {
        List<MzqAssayQuantLayer> assayQuantLayers = new ArrayList<>();

        // Process protein group list
        ProteinGroupList protGrpList = mzqUm.unmarshal(MzQuantMLElement.ProteinGroupList);
        if (protGrpList != null) {
            List<QuantLayer<IdOnly>> protGrpAssQLs = protGrpList.getAssayQuantLayer();
            for (QuantLayer assQL : protGrpAssQLs) {
                assayQuantLayers.add(new MzqAssayQuantLayer(mzqUm, protGrpList.getId(), assQL, "ProteinGroup"));
            }
        }

        // Process protein list
        ProteinList protList = mzqUm.unmarshal(MzQuantMLElement.ProteinList);
        if (protList != null) {
            List<QuantLayer<IdOnly>> protAssQLs = protList.getAssayQuantLayer();
            for (QuantLayer assQL : protAssQLs) {
                assayQuantLayers.add(new MzqAssayQuantLayer(mzqUm, protList.getId(), assQL, "Protein"));
            }
        }

        // Process peptide list
        Iterator<PeptideConsensusList> peptideListIter = mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.PeptideConsensusList);
        if (peptideListIter != null) {
            while (peptideListIter.hasNext()) {
                PeptideConsensusList peptideList = peptideListIter.next();
                List<QuantLayer<IdOnly>> pepAssQLs = peptideList.getAssayQuantLayer();
                for (QuantLayer assQL : pepAssQLs) {
                    assayQuantLayers.add(new MzqAssayQuantLayer(mzqUm, peptideList.getId(), assQL, "PeptideConsensus"));
                }
            }
        }

        // Process feature list
        Iterator<FeatureList> featureListIter = mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);
        if (featureListIter != null) {
            while (featureListIter.hasNext()) {
                FeatureList featureList = featureListIter.next();
                List<QuantLayer<IdOnly>> ftAssQLs = featureList.getMS2AssayQuantLayer();
                for (QuantLayer assQL : ftAssQLs) {
                    assayQuantLayers.add(new MzqAssayQuantLayer(mzqUm, featureList.getId(), assQL, "Feature"));
                }
            }
        }

        // Process small molecule list
        SmallMoleculeList smallMolList = mzqUm.unmarshal(MzQuantMLElement.SmallMoleculeList);
        if (smallMolList != null) {
            List<QuantLayer<IdOnly>> smallMolAssQLs = smallMolList.getAssayQuantLayer();
            for (QuantLayer assQL : smallMolAssQLs) {
                assayQuantLayers.add(new MzqAssayQuantLayer(mzqUm, smallMolList.getId(), assQL, "SmallMolecule"));
            }
        }

        return assayQuantLayers;
    }

    public void showCurve() {
        Stage curveStage = new Stage();
        curveStage.setTitle("Quantitative measurement accross assays");

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Assay id");
        final LineChart<String, Number> lineChart
                = new LineChart<>(xAxis, yAxis);

        lineChart.setTitle("Quantitative measurement for accross assays");

        TableView<MzqDataMatrixRow> dataMatrixTable = mzqInfoController.getDataMatrixTable();
        ObservableList<MzqDataMatrixRow> rowList = dataMatrixTable.getSelectionModel().getSelectedItems();
        rowNumber = rowList.size();

        for (MzqDataMatrixRow row : rowList) {
            XYChart.Series series = new XYChart.Series();
            lineChart.getData().add(series);
            series.setName(row.getObjetId());
            List<StringProperty> values = row.Values();
            int i = 1;
            for (StringProperty value : values) {
                series.getData().add(new XYChart.Data(dataMatrixTable.getColumns().get(i).getText(), Double.parseDouble(value.get())));
                i++;
            }

        }

//        XYChart.Series series1 = new XYChart.Series();
//        series1.setName("Portfolio 1");
//
//        series1.getData().add(new XYChart.Data("Jan", 23));
//        series1.getData().add(new XYChart.Data("Feb", 14));
//        series1.getData().add(new XYChart.Data("Mar", 15));
//        series1.getData().add(new XYChart.Data("Apr", 24));
//        series1.getData().add(new XYChart.Data("May", 34));
//        series1.getData().add(new XYChart.Data("Jun", 36));
//        series1.getData().add(new XYChart.Data("Jul", 22));
//        series1.getData().add(new XYChart.Data("Aug", 45));
//        series1.getData().add(new XYChart.Data("Sep", 43));
//        series1.getData().add(new XYChart.Data("Oct", 17));
//        series1.getData().add(new XYChart.Data("Nov", 29));
//        series1.getData().add(new XYChart.Data("Dec", 25));
//
//        XYChart.Series series2 = new XYChart.Series();
//        series2.setName("Portfolio 2");
//        series2.getData().add(new XYChart.Data("Jan", 33));
//        series2.getData().add(new XYChart.Data("Feb", 34));
//        series2.getData().add(new XYChart.Data("Mar", 25));
//        series2.getData().add(new XYChart.Data("Apr", 44));
//        series2.getData().add(new XYChart.Data("May", 39));
//        series2.getData().add(new XYChart.Data("Jun", 16));
//        series2.getData().add(new XYChart.Data("Jul", 55));
//        series2.getData().add(new XYChart.Data("Aug", 54));
//        series2.getData().add(new XYChart.Data("Sep", 48));
//        series2.getData().add(new XYChart.Data("Oct", 27));
//        series2.getData().add(new XYChart.Data("Nov", 37));
//        series2.getData().add(new XYChart.Data("Dec", 29));
//
//        XYChart.Series series3 = new XYChart.Series();
//        series3.setName("Portfolio 3");
//        series3.getData().add(new XYChart.Data("Jan", 44));
//        series3.getData().add(new XYChart.Data("Feb", 35));
//        series3.getData().add(new XYChart.Data("Mar", 36));
//        series3.getData().add(new XYChart.Data("Apr", 33));
//        series3.getData().add(new XYChart.Data("May", 31));
//        series3.getData().add(new XYChart.Data("Jun", 26));
//        series3.getData().add(new XYChart.Data("Jul", 22));
//        series3.getData().add(new XYChart.Data("Aug", 25));
//        series3.getData().add(new XYChart.Data("Sep", 43));
//        series3.getData().add(new XYChart.Data("Oct", 44));
//        series3.getData().add(new XYChart.Data("Nov", 45));
//        series3.getData().add(new XYChart.Data("Dec", 44));
        Scene scene = new Scene(lineChart, 800, 600);

        curveStage.setScene(scene);
        curveStage.show();
    }

}
