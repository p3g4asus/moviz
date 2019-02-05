package com.moviz.gui.preference;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;

public interface PreferenceDialogDisplay {
    PreferenceDialogFragmentCompat onDisplayPreferenceDialog();
    void setFragment(PreferenceDialogFragmentCompat f);
    void onBindDialogView(View view);
    void onPrepareDialogBuilder(AlertDialog.Builder builder,DialogInterface.OnClickListener listener);
    void onDialogClosed(boolean positiveResult);
    void onClick(DialogInterface dialog, int which);
    Dialog getDialog();
    boolean isDialogOpen();
}
