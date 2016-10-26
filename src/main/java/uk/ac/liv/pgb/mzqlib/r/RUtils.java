
package uk.ac.liv.pgb.mzqlib.r;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import org.rosuda.JRI.RBool;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 21-Nov-2014 11:13:05
 */
public class RUtils {

    public static boolean installRequiredPackages(Rengine re) {

        boolean allInstalled = true;
        List<Boolean> installedList = new ArrayList<>();

        // Make sure every package will be installed regardless the return value;
        for (RequiredPackages rp : RequiredPackages.values()) {
            boolean installed = installPackage(re, rp.getPackageName());
            installedList.add(installed);
        }

        for (Boolean bool : installedList) {
            allInstalled = allInstalled && bool;
        }
        return allInstalled;
    }

    public static boolean installPackage(Rengine re, String packageName) {

        boolean installed = false;

        String condition = "!require(\""
                + packageName
                + "\")";
        RBool uninstalled = re.eval(condition).asBool();
        if (uninstalled.isTRUE()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Install " + packageName + " package?");
            alert.setHeaderText(null);
            alert.setContentText("mzqLibrary is going to install R package \""
                    + packageName + "\"");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
//            Action response = Dialogs.create()
//                    .title("Install " + packageName + " package?")
//                    .message("mzqLibrary is going to install R package \"" + packageName + "\"")
//                    .showConfirm();

//            if (response == Dialog.Actions.YES) {
                String installString = "install.packages(\""
                        + packageName
                        + "\", dependencies = TRUE)";

                re.eval(installString);

                //Sometimes, R will recommand user to install in user documentation folder. 
                //If user choose 'NO', the package will not be installed.
                //The code below will check this.
                uninstalled = re.eval(condition).asBool();
                if (uninstalled.isTRUE()) {
                    installed = false;
                    //showUninstallWarningDialog(packageName);
                } else {
                    installed = true;
                    showPackageInstalledDialog(packageName);
                }

            }
//            else {
//                installed = false;
//                showUninstallWarningDialog(packageName);
//            }
        } else {
            installed = true;
        }
        return installed;
    }

    private static void showPackageInstalledDialog(String packageName) {
        //Platform.runLater(() -> {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Package installed");
        alert.setHeaderText(null);
        alert.setContentText(packageName + " is installed.");

        alert.showAndWait();

//        Dialogs.create()
//                .title("Package installed")
//                .message(packageName + " is installed.")
//                .showInformation();
        //});
    }

//    private static void showUninstallWarningDialog(String packageName) {
//        Platform.runLater(() -> {
//            Dialogs.create()
//                    .title("Warning")
//                    .message(packageName + " package is not installed. \nSome mzqLibrary routines might not work properly.\n"
//                            + "Use R packages menu to reinstall!")
//                    .showWarning();
//        });
//    }
}
