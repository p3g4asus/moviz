package com.moviz.gui.preference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

public abstract class ValidatedEditTextPreference extends EditTextPreference {
    public ValidatedEditTextPreference(Context ctx, AttributeSet attrs,
                                       int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public ValidatedEditTextPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public ValidatedEditTextPreference(Context ctx) {
        super(ctx);
    }

    private class EditTextWatcher implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int before,
                                      int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            onEditTextChanged();
        }
    }

    EditTextWatcher m_watcher = new EditTextWatcher();

    protected abstract boolean onCheckValue(String value);

    protected abstract int getInputType();

    /**
     * Return true in order to enable positive button or false to disable it.
     */

    protected void onEditTextChanged() {
        boolean enable = onCheckValue(getEditText().getText().toString());
        Dialog dlg = getDialog();
        if (dlg instanceof AlertDialog) {
            AlertDialog alertDlg = (AlertDialog) dlg;
            Button btn = alertDlg.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setEnabled(enable);
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText et = getEditText();
        et.setInputType(getInputType());
        et.removeTextChangedListener(m_watcher);
        et.addTextChangedListener(m_watcher);
        onEditTextChanged();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            String value = getEditText().getText().toString();
            postPositiveClick(value);
        }
    }

    protected void postPositiveClick(String value) {

    }
}