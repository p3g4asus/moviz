package com.moviz.gui.fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;

import com.moviz.gui.R;
import com.moviz.gui.preference.AliasPreference;
import com.moviz.gui.preference.BindSummaryToValueListener;
import com.moviz.gui.preference.DynamicListPreference;
import com.moviz.gui.preference.IntPreference;
import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.holder.DeviceHolder;
import com.moviz.lib.comunication.plus.holder.Databasable;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.message.BluetoothRefreshMesssage;
import com.moviz.lib.comunication.plus.message.DeviceChangedMessage;
import com.moviz.lib.db.MySQLiteHelper;
import com.moviz.lib.utils.CommandManager;
import com.moviz.lib.utils.CommandProcessor;
import com.moviz.lib.utils.DeviceTypeMaps;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class DeviceSettings {
    public static String ENABLED_KEY = "pref_device_enabled";
    public static String SCREEN_KEY = "pref_scr_device";
    protected SharedPreferences sharedPref;
    protected PreferenceCategory root;
    protected PreferenceFragmentCompat host;
    DynamicListPreference pBluetooth;
    protected ListPreference pType;
    protected CheckBoxPreference pEnabled;
    protected PreferenceScreen rootScreen;
    protected PreferenceCategory deviceCat;
    protected Preference pRemove;
    protected AliasPreference pAlias;
    protected IntPreference pOrderd;
    protected BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    protected Resources res = null;
    protected long myId = -1;
    protected PDeviceHolder dev = null;
    protected DeviceSubSettings subSettings;
    protected static MySQLiteHelper sqlite;
    protected PreferenceFragmentCompat pf;
    protected Context ctx;
    protected BindSummaryToValueListener listener;
    protected CommandManager mBinder;
    protected CommandProcessor mCommandProcessorSource;
    protected final static String TAG = DeviceSettings.class.getSimpleName();

    public String getConfData() {
        return dev.getConfData(DeviceTypeMaps.type2confsave);
    }

    public static boolean parseConfData(String[] lines, int start, SharedPreferences sharedPref, SharedPreferences.Editor pEdit, List<PDeviceHolder> ldevh, CommandManager commandManager, CommandProcessor source) {
        PDeviceHolder currentd = null;
        Map<String, String> additionalS = null;
        Pattern devicep = DeviceHolder.CONF_DATA_STARTER;
        Matcher devicematch;
        DeviceChangedMessage.Reason reason = null;
        String s,key,value;
        boolean rv = false;
        for (int i = start; i < lines.length; i++) {
            s = lines[i];
            devicematch = devicep.matcher(s);
            if (currentd == null && devicematch.matches()) {
                String addr = devicematch.group(1);
                if (ldevh != null) {
                    for (PDeviceHolder devh : ldevh) {
                        if (addr.equalsIgnoreCase(devh.getAddress())) {
                            boolean newenabled = devicematch.group(2).charAt(0) == '1';
                            currentd = devh;
                            if (newenabled != devh.isEnabled()) {
                                currentd.setEnabled(newenabled);
                                pEdit.putBoolean(key = ENABLED_KEY + devh.getId(),newenabled);
                                Timber.tag(TAG).v("Putting "+key+" "+newenabled);
                                rv = true;
                                reason = DeviceChangedMessage.Reason.BECAUSE_DEVICE_CHANGED;
                            }
                            additionalS = devh.deserializeAdditionalSettings();
                            break;
                        }
                    }
                }
            } else if (currentd != null) {
                if (s.equals(PDeviceHolder.CONF_DATA_TERMINATOR)) {
                    if (reason != null) {
                        currentd.serializeAdditionalSettings(additionalS, false);
                        MySQLiteHelper.newInstance(null,null).newValue(currentd);
                        commandManager.postMessage(new DeviceChangedMessage(reason, currentd,null,null), source);
                        reason = null;
                    }
                    currentd = null;
                } else {
                    int sidx = s.indexOf('=');
                    if (sidx > 0) {
                        key = s.substring(0, sidx);
                        value = sidx + 1 < s.length() ? s.substring(sidx + 1) : "";
                        s = additionalS.get(key);
                        if (s==null || !s.equals(value)) {
                            if (reason == null)
                                reason = DeviceChangedMessage.Reason.BECAUSE_DEVICE_CONF_CHANGED;
                            additionalS.put(key,value);
                            Timber.tag(TAG).v("Putting "+key+" "+value);
                            pEdit.putString(
                                    PDeviceHolder.getSubSettingKey(currentd,key),
                                    value);
                            rv = true;
                        }
                    }
                }
            }
        }
        return rv;
    }

    private DynamicListPreference.DynamicListPreferenceOnClickListener pBluetoothList = new DynamicListPreference.DynamicListPreferenceOnClickListener() {
        @Override
        public void onClick(DynamicListPreference preference) {
            if (mBinder != null && dev != null)
                mBinder.postMessage(new BluetoothRefreshMesssage(dev, null), mCommandProcessorSource);
        }
    };

    public void setCommandManager(CommandManager bnd) {
        mBinder = bnd;
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference == pBluetooth) {
            if (mBluetoothAdapter == null) {
                preference.setSummary(res.getString(R.string.pref_device_bluetooth_noadapter));
                return true;
            } else {
                CharSequence[] entr = pBluetooth.getEntries();
                if (entr == null || entr.length == 0) {
                    preference.setSummary(res.getString(R.string.pref_device_bluetooth_nobound));
                    return true;
                }
            }
        }
        if (preference == pBluetooth || preference == pType || preference == pEnabled || preference == pAlias || preference == pOrderd) {
            refreshDB(preference, value, false);
            setRootTitle();
            if (preference == pType) {
                initSubSettings(DeviceType.valueOf(pType.getValue()));
            }
            return true;
        } else if (subSettings != null) {
            return subSettings.onPreferenceChange(preference, value);
        }
        else
            return false;
    }

    protected void setRootTitle() {
        String titl = getScreenTitle();
        //Dialog dia = rootScreen.getDialog();
        //if (dia != null)
         //   dia.setTitle(titl);
        rootScreen.setTitle(titl);
        //((BaseAdapter) pf.getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
    }

    protected void setupBluetoothList(PDeviceHolder devh, PDeviceHolder[] deviceFound) {
        int num;
        String addr = "";
        String devaddr = devh.getAddress();
        String seladdr = "";
        boolean mineFirst;
        if (deviceFound == null || deviceFound.length == 0) {
            num = devaddr.isEmpty() ? 0 : 1;
            mineFirst = true;
        } else if (devaddr.isEmpty()) {
            num = deviceFound.length;
            mineFirst = false;
        } else {
            num = deviceFound.length + 1;
            mineFirst = true;
            for (PDeviceHolder bt : deviceFound) {
                addr = bt.getAddress();
                if (addr.equals(devaddr)) {
                    num--;
                    seladdr = devaddr;
                    mineFirst = false;
                    break;
                }
            }
        }
        if (num > 0) {
            CharSequence entries[] = new String[num];
            CharSequence entryValues[] = new String[num];
            int i = 0;
            if (mineFirst) {
                i = 1;
                seladdr = devaddr;
                entries[0] = devh.getName() + "[" + devaddr + "]";
                entryValues[0] = devaddr;
            }
            if (deviceFound != null)
                for (PDeviceHolder bt : deviceFound) {
                    if (seladdr.isEmpty())
                        seladdr = addr;
                    addr = bt.getAddress();
                    entries[i] = bt.getName() + "[" + addr + "]";
                    entryValues[i] = addr;
                    i++;
                }
            pBluetooth.setEntries(entries);
            pBluetooth.setEntryValues(entryValues);
            pBluetooth.setValue(seladdr);
        } else {
            CharSequence entries[] = new String[num];
            CharSequence entryValues[] = new String[num];
            pBluetooth.setEntries(entries);
            pBluetooth.setEntryValues(entryValues);
        }
    }

    protected void refreshDB(Preference p, Object value, boolean addedDb) {
        byte added = -1;
        if (addedDb)
            added = 1;
        else if (p == null)
            return;
        if (p == pBluetooth || p == null) {
            String val = p == null ? pBluetooth.getValue() : value.toString();
            if (val == null || val.isEmpty()) {
                dev.setAddress("");
                dev.setName("");
            } else {
                if (!val.equals(dev.getAddress()))
                    dev.setAddress(val);
                else if (p != null)
                    added = 0;
                int idx = pBluetooth.findIndexOfValue(val);
                CharSequence[] boundeddevs = pBluetooth.getEntries();
                String nm = res.getString(R.string.pref_device_noname);
                if (idx >= 0 && idx < boundeddevs.length) {
                    nm = boundeddevs[idx].toString();
                    try {
                        dev.setName(nm.substring(0, nm.lastIndexOf("[")));
                    } catch (Exception e) {
                        dev.setName(res.getString(R.string.pref_device_noname));
                    }
                }
                /*else if (dev.isEnabled()) {
					dev.setName(res.getString(R.string.pref_device_nobound));
				}*/
            }
        }
        if (p == pType || p == null) {
            DeviceType newTp = DeviceType.valueOf(p == null ? pType.getValue() : value.toString());
            if (newTp != dev.getType()) {
                dev.setType(newTp);
            } else if (p != null)
                added = 0;
        }

        if (p == pOrderd || p == null) {
            int txt = -1;
            try {
                txt = Integer.parseInt(p == null ? pOrderd.getText() : value.toString());
            } catch (NumberFormatException nfe) {
                txt = -2;
            }
            if (txt >= 0 && txt != dev.getOrderd())
                dev.setOrderd(txt);
            else if (p != null)
                added = 0;
        }

        if (p == pAlias || p == null) {
            String txt = p == null ? pAlias.getText() : value.toString();
            if (txt != null && !txt.isEmpty() && !txt.equals(dev.getAlias()))
                dev.setAlias(txt);
            else if (p != null)
                added = 0;
        }
        if (p == pEnabled || p == null)
            dev.setEnabled(p == null ? pEnabled.isChecked() : (Boolean) value);
        if (added != 0) {
            sqlite.newValue(dev);
            if (mBinder != null)
                mBinder.postMessage(new DeviceChangedMessage(
                        added == 1 ? DeviceChangedMessage.Reason.BECAUSE_DEVICE_ADDED : DeviceChangedMessage.Reason.BECAUSE_DEVICE_CHANGED,
                        dev,
                        added==1?pEnabled.getKey():(p==null?null:p.getKey()),value==null?null:value.toString()),
                        mCommandProcessorSource);
        }
    }

    protected void removeFromDB() {
        if (sqlite!=null)
            sqlite.deleteValue(dev);
        if (mBinder != null)
            mBinder.postMessage(new DeviceChangedMessage(DeviceChangedMessage.Reason.BECAUSE_DEVICE_REMOVED, dev,null,null), mCommandProcessorSource);
        dev = null;
        myId = -1;
    }

    protected void setupTypesList(PDeviceHolder devh) {
        int sz = DeviceTypeMaps.type2res.size();
        CharSequence entries[] = new String[sz];
        CharSequence entryValues[] = new String[sz];
        int i = 0;
        for (Map.Entry<DeviceType, Integer> entry : DeviceTypeMaps.type2res.entrySet()) {
            entries[i] = res.getString(entry.getValue());
            entryValues[i++] = entry.getKey().name();
        }
        pType.setEntries(entries);
        pType.setEntryValues(entryValues);
        DeviceType tp = devh.getType();
        if (tp != null)
            pType.setValue(tp.name());
        else
            pType.setValue(entryValues[0].toString());
    }

    protected void bluetoothRefresh(PDeviceHolder source, PDeviceHolder[] deviceFound) {
        if (pBluetooth!=null && dev != null && (source.equals(dev) || (dev.getAddress().isEmpty() && dev.getType().equals(source.getType())))) {
            setupBluetoothList(dev, deviceFound);
            onPreferenceChange(pBluetooth, pBluetooth.getValue());
            pBluetooth.openList();
        }
    }

    protected void setupRemovePreference() {
        pRemove.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                //rootScreen.getDialog().dismiss();
                Editor pEdit = sharedPref.edit();
                if (subSettings != null)
                    subSettings.removePrefs(rootScreen, pEdit);
                pEdit.commit();
                if (deviceCat!=null)
                    deviceCat.removePreference(rootScreen);

                removeFromDB();
                return true;
            }
        });
    }

    protected String getScreenTitle() {
        return res.getString(DeviceTypeMaps.type2res.get(dev.getType())) + " - " + dev.getAlias() +
                " [" + (dev.isEnabled() ? res.getString(R.string.pref_device_enabled_enabled) :
                res.getString(R.string.pref_device_enabled_disabled)) + "]";
    }

    private void initSubSettings(DeviceType tp) {
        if (subSettings != null) {
            Editor edt = sharedPref.edit();
            subSettings.removePrefs(rootScreen, edt);
            edt.commit();
            subSettings = null;
        }
        Class<? extends DeviceSubSettings> cl = DeviceTypeMaps.type2settingsclass.get(tp);
        if (cl != null) {
            try {
                subSettings = cl.newInstance();
                subSettings.restore(pf, ctx, listener, rootScreen, dev, mBinder, mCommandProcessorSource);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public DeviceSettings restore(PreferenceFragmentCompat p, Context ct, BindSummaryToValueListener listen, PDeviceHolder d, String rootkey, CommandManager bnd, CommandProcessor source) {
        dev = d;
        myId = dev.getId();
        mBinder = bnd;
        mCommandProcessorSource = source;
        if (myId < 0)
            myId = sqlite.getNextId(dev);
        pf = p;
        ctx = ct;
        listener = listen;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        res = pf.getResources();
        if (rootkey!=null)
            deviceCat = (PreferenceCategory) pf.findPreference(rootkey);
        rootScreen = pf.getPreferenceManager().createPreferenceScreen(ctx);
        rootScreen.setOrderingAsAdded(true);
        rootScreen.setKey(SCREEN_KEY + myId);
        rootScreen.setPersistent(false);
        //if (deviceCat==null) {
        pBluetooth = new DynamicListPreference(ctx);
        pBluetooth.setKey("pref_device_bluetooth" + myId);
        pBluetooth.setPersistent(false);
        pBluetooth.setDialogTitle(R.string.pref_device_bluetooth_dtitle);
        pBluetooth.setTitle(R.string.pref_device_bluetooth_title);
        pBluetooth.setOnClickListner(pBluetoothList);
        setupBluetoothList(dev, null);
        listener.addPreference(pBluetooth, pBluetooth.getValue(), new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY, dev));

        pAlias = new AliasPreference(ctx);
        pAlias.setKey("pref_device_alias" + myId);
        pAlias.setPersistent(false);
        pAlias.setDialogTitle(R.string.pref_device_alias_dtitle);
        pAlias.setTitle(R.string.pref_device_alias_title);
        pAlias.setText(dev.getAlias());
        listener.addPreference(pAlias, pAlias.getText(), new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER, dev));

        pOrderd = new IntPreference(ctx);
        pOrderd.setKey("pref_device_orderd" + myId);
        pOrderd.setPersistent(false);
        pOrderd.setDialogTitle(R.string.pref_device_orderd_dtitle);
        pOrderd.setTitle(R.string.pref_device_orderd_title);
        pOrderd.setText(dev.getOrderd() + "");
        listener.addPreference(pOrderd, pOrderd.getText(), new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY, dev));

        pEnabled = new CheckBoxPreference(ctx);
        pEnabled.setKey(ENABLED_KEY + myId);
        pEnabled.setPersistent(false);
        pEnabled.setTitle(R.string.pref_device_enabled_title);
        //pEnabled.setDialogTitle(R.string.pref_device_enabled_dtitle);
        //pEnabled.setEntries(R.array.pref_device_enabled_entries);
        //pEnabled.setEntryValues(R.array.pref_device_enabled_values);
        pEnabled.setChecked(dev.isEnabled());
        listener.addPreference(pEnabled, pEnabled.isChecked() + "", new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY, dev));

        pType = new ListPreference(ctx);
        pType.setKey("pref_device_type" + myId);
        pType.setPersistent(false);
        pType.setDialogTitle(R.string.pref_device_type_dtitle);
        pType.setTitle(R.string.pref_device_type_title);
        setupTypesList(dev);
        listener.addPreference(pType, pType.getValue(), new BindSummaryToValueListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY, dev));

        pRemove = new Preference(ctx);
        pRemove.setKey("pref_device_remove" + myId);
        pRemove.setPersistent(false);
        pRemove.setTitle(R.string.pref_device_remove_title);
        setupRemovePreference();
        rootScreen.addPreference(pBluetooth);
        rootScreen.addPreference(pAlias);
        rootScreen.addPreference(pOrderd);
        rootScreen.addPreference(pEnabled);
        rootScreen.addPreference(pType);
        rootScreen.addPreference(pRemove);
        initSubSettings(dev.getType());
        //PreferenceCategory category = new PreferenceCategory(ctx);
        //category.setTitle(R.string.pref_device_cat_remove);
        //category.setKey("pref_device_cat_remove"+myId);
        //category.addPreference(pRemove);
        //rootScreen.addPreference(category);
        /*}
        else*/
        if (deviceCat!=null)
            deviceCat.addPreference(rootScreen);
        setRootTitle();

        return this;
    }

    public DeviceSettings newDevice(PreferenceFragmentCompat pf, Context ctx, BindSummaryToValueListener listener, String rootkey, CommandManager bnd, CommandProcessor source) {
        PDeviceHolder d = new PDeviceHolder();
        sqlite.newValue(d);
        restore(pf, ctx, listener, d, rootkey, bnd, source);
        refreshDB(null, null,true);
        //ds.rootScreen.getDialog().show();
        //pf.getPreferenceScreen().onItemClick(null, null, ds.rootScreen.getOrder(), 0);
        return this;
    }
	
	/*public void processSettingsRequest() {
		if (subSettings!=null)
			CA.lbm.sendBroadcast(new Intent(Messages.DEVICESETTINGSCHANGED_MESSAGE).putExtra("dev", dev).putParcelableArrayListExtra("settings", (ArrayList<? extends Parcelable>) subSettings.packSettings()));
	}*/

    public boolean processExternalDeviceChange(PDeviceHolder devh) {
        if (devh.getId() == dev.getId()) {
            dev.copyFrom(devh);
            setRootTitle();
            listener.reflectExternalChangeOnPreference(pEnabled,dev.isEnabled());
            if (subSettings != null) {
                subSettings.processExternalDeviceChange();
            }
            return true;
        } else
            return false;
    }

    public static void restoreAll(PreferenceFragmentCompat pf, Context ctx, BindSummaryToValueListener listener, String rootkey, List<DeviceSettings> lst, CommandManager bnd, CommandProcessor source) {
        lst.clear();
        sqlite = MySQLiteHelper.newInstance(null, null);
        pf.getPreferenceScreen().setOrderingAsAdded(true);
        if (sqlite != null) {
            List<PDeviceHolder> devices = (List<PDeviceHolder>) sqlite.getAllValues(new PDeviceHolder(), "orderd");
            for (Databasable dd : devices) {
                PDeviceHolder d = (PDeviceHolder) dd;
                DeviceType dt = d.getType();
                if (dt != null) {
                    try {
                        lst.add(new DeviceSettings().restore(pf, ctx, listener, d, rootkey, bnd, source));
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    public DeviceSettings() {
    }

    public PreferenceScreen getPrefereceScreen() {
        return rootScreen;
    }

    public PDeviceHolder getDevice() {
        return dev;
    }
}
