package com.moviz.gui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.moviz.gui.R;
import com.moviz.gui.preference.BindSummaryToValueListener;
import com.moviz.gui.preference.MachineIDPreference;
import com.moviz.gui.preference.WheelDiameterPreference;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

import java.util.Map;

public class KeiserM3iSubSettings extends DeviceSubSettings {
    private MachineIDPreference pMachineId;
    @Override
    protected void doRestore(Context ctx, PreferenceScreen rootScreen) {
        Map<String, String> setMap = dev.deserializeAdditionalSettings();
        String currentFF;
        pMachineId = new MachineIDPreference(ctx);
        pMachineId.setKey(PDeviceHolder.getSubSettingKey(dev,"machineid"));
        pMachineId.setTitle(R.string.pref_device_m3i_machineid_title);
        pMachineId.setOnPreferenceChangeListener(listener);
        currentFF = setMap.get("machineid");
        BindSummaryToValueListener.CallInfo ci = new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY,dev);
        listener.addPreference(pMachineId,null,ci);
        if (currentFF == null)
            listener.onPreferenceChange(pMachineId, "1");

        rootScreen.addPreference(pMachineId);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference == pMachineId)
            return manageEdit(pMachineId, value.toString());
        else
            return false;
    }

    @Override
    public void removePrefs(PreferenceScreen rootScreen, SharedPreferences.Editor pEdit) {
        try {
            rootScreen.removePreference(pMachineId);
        } catch (Exception e) {

        }
        pEdit.remove(PDeviceHolder.getSubSettingKey(dev,"machineid"));
        myId = -1;
        dev = null;
    }

    @Override
    public void processExternalDeviceChange() {
        Map<String, String> sett = dev.deserializeAdditionalSettings();
        listener.reflectExternalChangeOnPreference(pMachineId,sett.get("machineid"));
    }
}
