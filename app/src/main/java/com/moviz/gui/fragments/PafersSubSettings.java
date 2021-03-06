package com.moviz.gui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.moviz.gui.R;
import com.moviz.gui.dialogs.FileDialog;
import com.moviz.gui.dialogs.FolderDialogChange;
import com.moviz.gui.preference.BindSummaryToValueListener;
import com.moviz.gui.preference.MaxSessionPointsPreference;
import com.moviz.lib.program.ProgramParser;

import java.io.File;
import java.util.Map;

public class PafersSubSettings extends DeviceSubSettings {
    private Preference pProgramFold;
    private Preference pProgramFile;
    private MaxSessionPointsPreference pStartDelay;

    private class ProgramFoldDialogChange extends FolderDialogChange {

        public ProgramFoldDialogChange(Fragment f, SharedPreferences sh, Preference p, String defv, String pos) {
            super(f, sh, p, defv, pos);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void afterchange(Preference p, String newv) {
            listener.onPreferenceChange(pProgramFold,newv);
            listener.onPreferenceChange(pProgramFile,"");
            pProgramFile.setSummary(null);
        }

    }

    private void setupFolderProgramSearch() {
        new ProgramFoldDialogChange(root, sharedPref, pProgramFold, getDefaultProgramFolder(res), res.getString(R.string.select));
    }

    private void setupFileSearch() {

        pProgramFile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                final Activity a = root.getActivity();
                if (a != null) {
                    File mPath = new File(sharedPref.getString("pref_devicepriv_pafers_" + myId + "_pfold",
                            getDefaultProgramFolder(res)));
                    final FileDialog fileDialog = new FileDialog(a, mPath);
                    /*
					 * fileDialog.addFileListener(new
					 * FileDialog.FileSelectedListener() { public void
					 * fileSelected(File file) { Log.d(getClass().getName(),
					 * "selected file " + file.toString()); } });
					 */
                    fileDialog
                            .addFileListener(new FileDialog.FileSelectedListener() {

                                @Override
                                public void fileSelected(File file) {
                                    String d = file.getPath();
                                    Log.d(getClass().getName(), "selected file " + d);
                                    listener.onPreferenceChange(pProgramFile,d);
                                    pProgramFile.setSummary(ProgramParser.extractName(file));
                                }
                            });
                    fileDialog.setFileEndsWith(ProgramParser.PROGRAMFILE_EXTENSION);
                    fileDialog.setShowExtension(false);
                    fileDialog.setSelectFileOption(true);
                    fileDialog.showDialog();
                }
                return false;
            }
        });
    }

    public PafersSubSettings() {

    }

    @Override
    public void doRestore(Context ctx, PreferenceScreen rootScreen) {
        Map<String, String> setMap = dev.deserializeAdditionalSettings();
        String currentVF, currentFF;
        pProgramFold = new Preference(ctx);
        pProgramFold.setKey("pref_devicepriv_pafers_" + myId + "_pfold");
        pProgramFold.setTitle(R.string.pref_device_pafers_pfold_title);
        pProgramFold.setPersistent(false);
        currentVF = setMap.get("pfold");
        BindSummaryToValueListener.CallInfo ci = new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY,dev);
        listener.addPreference(pProgramFold,null,ci);
        if (currentVF == null)
            listener.onPreferenceChange(pProgramFold, currentVF = getDefaultProgramFolder(res));
        setupFolderProgramSearch();

        pProgramFile = new Preference(ctx);
        pProgramFile.setKey("pref_devicepriv_pafers_" + myId + "_pfile");
        pProgramFile.setTitle(R.string.pref_device_pafers_pfile_title);
        pProgramFile.setPersistent(false);

        currentFF = setMap.get("pfile");
        ci = new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.LISTENER|BindSummaryToValueListener.NOTIFY,dev);
        listener.addPreference(pProgramFile,null,ci);
        if (currentFF == null)
            listener.onPreferenceChange(pProgramFile, currentFF = currentVF + "/man1" + ProgramParser.PROGRAMFILE_EXTENSION);
        pProgramFile.setSummary(ProgramParser.extractName(currentFF));

        setupFileSearch();

        ci = new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY,dev);
        pStartDelay = new MaxSessionPointsPreference(ctx);
        pStartDelay.setKey("pref_devicepriv_pafers_" + myId + "_startdelay");
        pStartDelay.setTitle(R.string.pref_device_pafers_startdelay_title);
        listener.addPreference(pStartDelay,null,ci);
        currentFF = setMap.get("startdelay");
        if (currentFF == null)
            listener.onPreferenceChange(pStartDelay, "2000");


        rootScreen.addPreference(pProgramFold);
        rootScreen.addPreference(pProgramFile);
        rootScreen.addPreference(pStartDelay);

    }

    public static String getDefaultProgramFolder(Resources res) {
        return SettingsFragment.getDefaultAppDir(res) + "/programs";
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference == pStartDelay || preference == pProgramFile || preference == pProgramFold)
            return manageEdit(preference, value == null ? null : value.toString());
        else
            return false;
    }

    @Override
    public void removePrefs(PreferenceScreen rootScreen, Editor pEdit) {
        try {
            rootScreen.removePreference(pProgramFold);
            rootScreen.removePreference(pProgramFile);
            rootScreen.removePreference(pStartDelay);
        } catch (Exception e) {

        }
        pEdit.remove("pref_devicepriv_pafers_" + myId + "_pfold");
        pEdit.remove("pref_devicepriv_pafers_" + myId + "_pfile");
        pEdit.remove("pref_devicepriv_pafers_" + myId + "_startdelay");
        myId = -1;
        dev = null;
    }

    @Override
    public void processExternalDeviceChange() {
        Map<String, String> sett = dev.deserializeAdditionalSettings();
        listener.reflectExternalChangeOnPreference(pStartDelay,sett.get("startdelay"));
        listener.bindPreferenceSummaryToValue(pProgramFold, sett.get("pfold"));
        pProgramFile.setSummary(ProgramParser.extractName(sett.get("pfile")));
    }

	/*@Override
	public PHolderSetter packSettings() {
		PHolderSetter phs = new PHolderSetter();
		phs.add(new PHolder(sharedPref.getString("pref_devicepriv_pafers_"+myId+"_pfile", ""),"pafers.settings.programfile",null));
		return phs;
	}*/

}
