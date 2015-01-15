
package uk.ac.liv.mzqlib.r;

import java.util.ArrayList;
import java.util.List;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
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
            Action response = Dialogs.create()
                    .title("Install " + packageName + " package?")
                    .message("mzqLibrary is going to install R package \"" + packageName + "\"")
                    .showConfirm();

            if (response == Dialog.Actions.YES) {
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
                }
                else {
                    installed = true;
                    showPackageInstalledDialog(packageName);
                }

            }
//            else {
//                installed = false;
//                showUninstallWarningDialog(packageName);
//            }
        }
        else {
            installed = true;
        }
        return installed;
    }

    private static void showPackageInstalledDialog(String packageName) {
        //Platform.runLater(() -> {
            Dialogs.create()
                    .title("Package installed")
                    .message(packageName + " is installed.")
                    .showInformation();
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
