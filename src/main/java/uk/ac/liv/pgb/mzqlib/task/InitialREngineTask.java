package uk.ac.liv.pgb.mzqlib.task;

import javafx.application.Platform;

import javafx.concurrent.Task;

import org.rosuda.JRI.Rengine;

import uk.ac.liv.pgb.mzqlib.r.RUtils;

/**
 *
 * @author Da Qi
 * @since 11-Sep-2014 15:13:21
 */
public class InitialREngineTask extends Task<Rengine> {
    private Rengine re;

    @Override
    protected final Rengine call() throws Exception {
        re = new Rengine(new String[]{" ", " "}, false, null);
        Platform.runLater(
            () -> {
                RUtils.installRequiredPackages(re);
            });

        return re;
    }

//  protected void installRequiredPackages() {
//
//      for (RequiredPackages rp : RequiredPackages.values()) {
//          installPackage(rp.getPackageName());
//      }
//
//  }
//
//  private void installPackage(String packageName) {
//      String condition = "!require(\""
//              + packageName
//              + "\")";
//      RBool uninstalled = this.re.eval(condition).asBool();
//      if (uninstalled.isTRUE()) {
//          Action response = Dialogs.create()
//                  .title("Install " + packageName + " package?")
//                  .message("mzqLibrary is going to install R package \"" + packageName + "\"")
//                  .showConfirm();
//
//          if (response == Dialog.Actions.YES) {
//              String installString = "install.packages(\""
//                      + packageName
//                      + "\", dependencies = TRUE)";
//
//              this.re.eval(installString);
//
//              //Sometimes, R will recommand user to install in user documentation folder.
//              //If user choose 'NO', the package will not be installed.
//              //The code below will check this.
//              uninstalled = this.re.eval(condition).asBool();
//              if (uninstalled.isTRUE()) {
//                  showUninstallWarningDialog(packageName);
//              }
//              else {
//                  showPackageInstalledDialog(packageName);
//              }
//
//          }
//          else {
//              showUninstallWarningDialog(packageName);
//          }
//      }
//  }
//
//  private void showPackageInstalledDialog(String packageName) {
//      Platform.runLater(() -> {
//          Dialogs.create()
//                  .title("Package installed")
//                  .message(packageName + " is installed.")
//                  .showInformation();
//      });
//  }
//
//  private void showUninstallWarningDialog(String packageName) {
//      Platform.runLater(() -> {
//          Dialogs.create()
//                  .title("Warning")
//                  .message(packageName + " package is not installed. \nSome mzqLibrary routines might not work properly.")
//                  .showWarning();
//      });
//  }
}


//~ Formatted by Jindent --- http://www.jindent.com
