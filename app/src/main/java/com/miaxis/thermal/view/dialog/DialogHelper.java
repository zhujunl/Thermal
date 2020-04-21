package com.miaxis.thermal.view.dialog;

import android.app.AlertDialog;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;

public class DialogHelper {

    public static void fullScreenMaterialDialog(MaterialDialog dialog) {
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static MaterialDialog fullScreenMaterialDialogLink(MaterialDialog dialog) {
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        return dialog;
    }

    public static AlertDialog fullScreenAlertDialogLink(AlertDialog dialog) {
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        return dialog;
    }

}
