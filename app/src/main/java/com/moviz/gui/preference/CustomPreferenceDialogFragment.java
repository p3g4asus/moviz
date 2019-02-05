package com.moviz.gui.preference;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;

public class CustomPreferenceDialogFragment extends PreferenceDialogFragmentCompat {
    public static CustomPreferenceDialogFragment newInstance(String key) {
        final CustomPreferenceDialogFragment fragment = new CustomPreferenceDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }
    private PreferenceDialogDisplay getCustomizablePreference() {
        return (PreferenceDialogDisplay) getPreference();
    }
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        getCustomizablePreference().setFragment(this);
        getCustomizablePreference().onPrepareDialogBuilder(builder, this);
    }
    @Override
    public void onDialogClosed(boolean positiveResult) {
        getCustomizablePreference().onDialogClosed(positiveResult);
    }
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        getCustomizablePreference().onBindDialogView(view);
    }
    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        getCustomizablePreference().onClick(dialog, which);
    }
}
