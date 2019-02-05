package com.moviz.gui.preference;

import android.content.SharedPreferences;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.moviz.gui.R;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matteo on 03/11/2016.
 */

public abstract class BindSummaryToValueListener implements Preference.OnPreferenceChangeListener {
    public static final int SUMMARY = 1;
    public static final int LISTENER = 2;
    public static final int NOTIFY = 4;
    public static final int SUMMARY_NO_INIT = 8;
    public static final int SUMMARY_NOTIFY = SUMMARY|NOTIFY;
    public static final int SUMMARY_LISTENER = SUMMARY|LISTENER;
    public static final int SUMMARY_LISTENER_NOTIFY = SUMMARY|LISTENER|NOTIFY;
    protected final SharedPreferences sharedPref;

    public CallInfo getCallInfo(Preference pref) {
        return callMap.get(pref);
    }

    public static class CallInfo {
        public int callFlag = SUMMARY_LISTENER;
        public PDeviceHolder device = null;
        public CallInfo() {

        }
        public CallInfo(int callFl) {
            callFlag = callFl;
        }

        public CallInfo(PDeviceHolder devh) {
            device = devh;
        }

        public CallInfo(int callFl,PDeviceHolder devh) {
            callFlag = callFl;
            device = devh;
        }

        public boolean notifying() {
            return (callFlag&NOTIFY)!=0;
        }
    }
    private Map<Preference,CallInfo> callMap = new HashMap<>();

    public BindSummaryToValueListener(SharedPreferences prf) {
        sharedPref = prf;
    }


    public abstract void notifyChange(CallInfo ci, Preference p, String value);
    public void addPreference(Preference p, String val, CallInfo ci) {
        callMap.put(p,ci);
        if (p.getOnPreferenceChangeListener()!=this)
            p.setOnPreferenceChangeListener(this);
        if ((ci.callFlag& SUMMARY)!=0) {
            bindPreferenceSummaryToValue(p,val==null?sharedPref.getString(p.getKey(),""):val);
        }
    }

    public void bindPreferenceSummaryToValue(Preference preference, Object value) {
        String stringValue = value==null?"":value.toString();
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            if (index >= 0)
                listPreference.setValueIndex(index);

            // Set the summary to reflect the new value.
            preference
                    .setSummary(index >= 0 ? listPreference.getEntries()[index]
                            : null);

        }
        else if (preference instanceof CheckBoxPreference) {
            preference.setSummary(
                    stringValue.equals("true")? R.string.pref_checkbox_checked:R.string.pref_checkbox_unchecked
            );
        }
        else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
    }

    @Override
    public final boolean onPreferenceChange(Preference p, Object v) {
        CallInfo ci = callMap.get(p);
        int cf = ci.callFlag;
        boolean notify = true;
        if ((cf&(SUMMARY |SUMMARY_NO_INIT))!=0) {
            bindPreferenceSummaryToValue(p,v);
        }
        if ((cf& LISTENER)!=0) {
            notify = preferenceChange(ci,p,v);
        }
        if (notify && (cf& NOTIFY)!=0) {
            notifyChange(ci,p,v.toString());
        }
        return true;
    }

    public void reflectExternalChangeOnPreference(Preference p, Object v) {
        if (p==null)
            return;
        CallInfo ci = callMap.get(p);
        int origFlag = 0;
        if (ci!=null) {
            origFlag = ci.callFlag;
            ci.callFlag&=(~(LISTENER|NOTIFY));
        }
        if (p instanceof CheckBoxPreference) {
            ((CheckBoxPreference) p).setChecked(v!=null && v.toString().equals("true"));
        }
        else if (p instanceof ListPreference) {
            ((ListPreference) p).setValue(v==null?null:v.toString());
        }
        else if (p instanceof EditTextPreference) {
            ((EditTextPreference) p).setText(v==null?"":v.toString());
        }

        if (ci!=null) {
            ci.callFlag = origFlag;
            if ((origFlag&SUMMARY)!=0)
                bindPreferenceSummaryToValue(p,v);
        }

    }

    public abstract boolean preferenceChange(CallInfo ci, Preference p, Object v);
}
