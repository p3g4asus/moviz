package com.moviz.gui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.moviz.gui.preference.BindSummaryToValueListener;
import com.moviz.lib.comunication.holder.DeviceHolder;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.db.MySQLiteHelper;
import com.moviz.lib.utils.CommandManager;
import com.moviz.lib.utils.CommandProcessor;

public abstract class DeviceSubSettings {
    protected PDeviceHolder dev;
    protected long myId;
    protected SharedPreferences sharedPref;
    protected Resources res;
    protected PreferenceFragmentCompat root;
    protected CommandManager mBinder = null;
    protected CommandProcessor mCommandProcessorSource;
    protected BindSummaryToValueListener listener;

    public void restore(PreferenceFragmentCompat pf, Context ctx, BindSummaryToValueListener listen, PreferenceScreen rootScreen, PDeviceHolder d, CommandManager bnd, CommandProcessor source) {
        dev = d;
        mBinder = bnd;
        listener = listen;
        mCommandProcessorSource = source;
        root = pf;
        myId = d.getId();
        res = pf.getResources();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        doRestore(ctx, rootScreen);
    }

    protected abstract void doRestore(Context ctx, PreferenceScreen rootScreen);

    public abstract boolean onPreferenceChange(Preference preference, Object value);

    public abstract void removePrefs(PreferenceScreen rootScreen, Editor edt);

    public abstract void processExternalDeviceChange();
    //PHolderSetter packSettings();

    protected boolean manageEdit(Preference pref, String val) {
        String keycomp = pref.getKey();
        String actualvalue = sharedPref.getString(keycomp, null);
        if ((actualvalue != null && val != null && !val.equals(actualvalue)) || (val != null && actualvalue == null)) {
            sharedPref.edit().putString(keycomp, val).commit();
            dev.serializeAdditionalSettings(sharedPref.getAll(), true);
            MySQLiteHelper sqlite = MySQLiteHelper.newInstance(null,null);
            if (sqlite!=null)
                sqlite.newValue(dev);
            return true;
            //listener.notifyChange(new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.NOTIFY,dev),pref,val);
        }
        else
            return false;
    }
}
