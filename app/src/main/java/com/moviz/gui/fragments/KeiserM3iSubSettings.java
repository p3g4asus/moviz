package com.moviz.gui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.moviz.gui.R;
import com.moviz.gui.preference.BindSummaryToValueListener;
import com.moviz.gui.preference.IntPreference;
import com.moviz.gui.preference.MachineIDPreference;
import com.moviz.gui.preference.WheelDiameterPreference;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

import java.util.Map;

public class KeiserM3iSubSettings extends DeviceSubSettings {
    private MachineIDPreference pMachineId;
    private IntPreference pSpeedBufferSize;
    @Override
    protected void doRestore(Context ctx, PreferenceScreen rootScreen) {
        pMachineId = new MachineIDPreference(ctx);
        pMachineId.setKey(PDeviceHolder.getSubSettingKey(dev,"machineid"));
        pMachineId.setTitle(R.string.pref_device_m3i_machineid_title);
        manageDefault(pMachineId,"1");

        pSpeedBufferSize = new IntPreference(ctx);
        pSpeedBufferSize.setKey(PDeviceHolder.getSubSettingKey(dev,"buffsize"));
        pSpeedBufferSize.setTitle(R.string.pref_device_m3i_buffsize_title);
        pSpeedBufferSize.setMin(1);
        pSpeedBufferSize.setMax(500);
        manageDefault(pSpeedBufferSize,"165");

        rootScreen.addPreference(pMachineId);
        rootScreen.addPreference(pSpeedBufferSize);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference == pMachineId)
            return manageEdit(pMachineId, value.toString());
        else if (preference == pSpeedBufferSize)
            return manageEdit(pSpeedBufferSize, value.toString());
        else
            return false;
    }

    @Override
    public void removePrefs(PreferenceScreen rootScreen, SharedPreferences.Editor pEdit) {
        try {
            rootScreen.removePreference(pMachineId);
        } catch (Exception e) {

        }
        try {
            rootScreen.removePreference(pSpeedBufferSize);
        } catch (Exception e) {

        }
        pEdit.remove(PDeviceHolder.getSubSettingKey(dev,"machineid"));
        pEdit.remove(PDeviceHolder.getSubSettingKey(dev,"buffsize"));
        myId = -1;
        dev = null;
    }

    @Override
    public void processExternalDeviceChange() {
        Map<String, String> sett = dev.deserializeAdditionalSettings();
        listener.reflectExternalChangeOnPreference(pMachineId,sett.get("machineid"));
        listener.reflectExternalChangeOnPreference(pSpeedBufferSize,sett.get("buffsize"));
    }
}
