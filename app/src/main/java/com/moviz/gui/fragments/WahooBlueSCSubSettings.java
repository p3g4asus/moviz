package com.moviz.gui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.moviz.gui.R;
import com.moviz.gui.preference.BindSummaryToValueListener;
import com.moviz.gui.preference.GearFactorPreference;
import com.moviz.gui.preference.MaxSessionPointsPreference;
import com.moviz.gui.preference.WheelDiameterPreference;

import java.util.Map;

/**
 * Created by Matteo on 30/10/2016.
 */

public class WahooBlueSCSubSettings extends DeviceSubSettings {
    private WheelDiameterPreference pWheelDiam;
    private GearFactorPreference pGearFactor;
    private MaxSessionPointsPreference pCurrentGear;

    @Override
    public void doRestore(Context ctx, PreferenceScreen rootScreen) {
        Map<String, String> setMap = dev.deserializeAdditionalSettings();
        String currentFF;
        pWheelDiam = new WheelDiameterPreference(ctx);
        pWheelDiam.setKey("pref_devicepriv_" + dev.getType().name() + "_" + myId + "_wheeldiam");
        pWheelDiam.setTitle(R.string.pref_device_wahoobluesc_wheeldiam_title);
        pWheelDiam.setOnPreferenceChangeListener(listener);
        currentFF = setMap.get("wheeldiam");
        BindSummaryToValueListener.CallInfo ci = new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY,dev);
        listener.addPreference(pWheelDiam,null,ci);
        if (currentFF == null)
            listener.onPreferenceChange(pWheelDiam, "711");

        rootScreen.addPreference(pWheelDiam);

        pGearFactor = new GearFactorPreference(ctx);
        pGearFactor.setKey("pref_devicepriv_" + dev.getType().name() + "_" + myId + "_gearfactor");
        pGearFactor.setTitle(R.string.pref_device_wahoobluesc_gearfactor_title);
        pGearFactor.setOnPreferenceChangeListener(listener);
        currentFF = setMap.get("gearfactor");
        ci = new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY,dev);
        listener.addPreference(pGearFactor,null,ci);
        if (currentFF == null)
            listener.onPreferenceChange(pGearFactor, "2.0, 1.8");

        rootScreen.addPreference(pGearFactor);

        pCurrentGear = new MaxSessionPointsPreference(ctx);
        pCurrentGear.setKey("pref_devicepriv_" + dev.getType().name() + "_" + myId + "_currentgear");
        pCurrentGear.setTitle(R.string.pref_device_wahoobluesc_currentgear_title);
        pCurrentGear.setOnPreferenceChangeListener(listener);
        currentFF = setMap.get("currentgear");
        ci = new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY,dev);
        listener.addPreference(pCurrentGear,null,ci);
        if (currentFF == null)
            listener.onPreferenceChange(pCurrentGear, "0");

        rootScreen.addPreference(pCurrentGear);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference == pWheelDiam)
            return manageEdit(pWheelDiam, value.toString());
        else if (preference == pCurrentGear)
            return manageEdit(pCurrentGear, value.toString());
        else if (preference == pGearFactor)
            return manageEdit(pGearFactor, value.toString());
        else
            return false;
    }

    @Override
    public void removePrefs(PreferenceScreen rootScreen, SharedPreferences.Editor pEdit) {
        try {
            rootScreen.removePreference(pWheelDiam);
        } catch (Exception e) {

        }
        try {
            rootScreen.removePreference(pGearFactor);
        } catch (Exception e) {
        }
        try {
            rootScreen.removePreference(pCurrentGear);
        } catch (Exception e) {
        }
        pEdit.remove("pref_devicepriv_" + dev.getType().name() + "_" + myId + "_gearfactor");
        pEdit.remove("pref_devicepriv_" + dev.getType().name() + "_" + myId + "_currentgear");
        pEdit.remove("pref_devicepriv_" + dev.getType().name() + "_" + myId + "_wheeldiam");
        myId = -1;
        dev = null;
    }

    @Override
    public void processExternalDeviceChange() {
        Map<String, String> sett = dev.deserializeAdditionalSettings();
        listener.reflectExternalChangeOnPreference(pWheelDiam,sett.get("wheeldiam"));
        listener.reflectExternalChangeOnPreference(pGearFactor,sett.get("gearfactor"));
        listener.reflectExternalChangeOnPreference(pCurrentGear,sett.get("currentgear"));
    }
}
