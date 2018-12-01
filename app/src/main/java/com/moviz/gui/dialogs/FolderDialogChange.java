package com.moviz.gui.dialogs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;

public class FolderDialogChange implements Preference.OnPreferenceClickListener {
    protected String defaultDir;
    protected Fragment root;
    protected SharedPreferences sharedPref;
    protected String positiveButtonText;

    public FolderDialogChange(Fragment f, SharedPreferences sh, Preference p, String defv, String positive) {
        defaultDir = defv;
        root = f;
        sharedPref = sh;
        positiveButtonText = positive;
        p.setDefaultValue(defv);
        p.setOnPreferenceClickListener(this);
    }

    public void afterchange(Preference p, String newv) {

    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        final Activity a = root.getActivity();
        if (a != null) {
            final String k = preference.getKey();
            File mPath = new File(sharedPref.getString(k,
                    defaultDir));
            final FileDialog fileDialog = new FileDialog(a, mPath);
            /*
			 * fileDialog.addFileListener(new
			 * FileDialog.FileSelectedListener() { public void
			 * fileSelected(File file) { Log.d(getClass().getName(),
			 * "selected file " + file.toString()); } });
			 */
            fileDialog
                    .addDirectoryListener(new FileDialog.DirectorySelectedListener() {
                        public void directorySelected(File directory) {
                            String d = directory.toString();
                            Log.d(getClass().getName(), "selected dir " + d);
                            Editor prefEditor = sharedPref.edit();
                            prefEditor.putString(k, d); // set your
                            // default
                            // value
                            // here
                            // (could be
                            // empty as
                            // well)
                            prefEditor.commit(); // finally save changes
                            afterchange(preference, d);
                            preference.setSummary(d);
                        }
                    });
            fileDialog.setSelectDirectoryOption(true);
            fileDialog.setPositiveButtonText(positiveButtonText);
            fileDialog.showDialog();
        }
        return false;
    }

}
