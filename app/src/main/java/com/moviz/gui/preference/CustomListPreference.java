/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.moviz.gui.preference;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
public class CustomListPreference extends ListPreference implements PreferenceDialogDisplay {
    private PreferenceDialogFragmentCompat mFragment;
    public CustomListPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                  int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public CustomListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CustomListPreference(Context context) {
        super(context);
    }
    @Override
    public boolean isDialogOpen() {
        return getDialog() != null && getDialog().isShowing();
    }
    @Override
    public Dialog getDialog() {
        return mFragment != null ? mFragment.getDialog() : null;
    }
    @Override
    public void onPrepareDialogBuilder(AlertDialog.Builder builder,
                                       DialogInterface.OnClickListener listener) {
    }
    @Override
    public void onDialogClosed(boolean positiveResult) {
    }
    @Override
    public void onClick(DialogInterface dialog, int which) {
    }
    @Override
    public void onBindDialogView(View view) {
    }
    @Override
    public void setFragment(PreferenceDialogFragmentCompat fragment) {
        mFragment = fragment;
    }

    @Override
    public PreferenceDialogFragmentCompat onDisplayPreferenceDialog() {
        CustomPreferenceDialogFragment  f = CustomPreferenceDialogFragment.newInstance(getKey());
        setFragment(f);
        return f;
    }
}
