package com.moviz.gui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.moviz.gui.R;
import com.moviz.gui.activities.ActivityMain;
import com.moviz.gui.dialogs.FolderDialogChange;
import com.moviz.gui.dialogs.MultipleSessionSelectDialog;
import com.moviz.gui.preference.BindSummaryToValueListener;
import com.moviz.gui.preference.ConfNamePreference;
import com.moviz.gui.preference.DBCleanPreference;
import com.moviz.gui.util.SessionExporterAction;
import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PConfHolder;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.comunication.plus.message.BluetoothRefreshMesssage;
import com.moviz.lib.comunication.plus.message.ConfChangeMessage;
import com.moviz.lib.comunication.plus.message.DeviceChangedMessage;
import com.moviz.lib.comunication.plus.message.ProcessedOKMessage;
import com.moviz.lib.comunication.plus.message.UserSetMessage;
import com.moviz.lib.db.MySQLiteHelper;
import com.moviz.lib.utils.CommandManager;
import com.moviz.lib.utils.CommandProcessor;
import com.moviz.lib.utils.DeviceTypeMaps;
import com.moviz.workers.DeviceManagerService;
import com.moviz.workers.DeviceManagerService.DeviceManagerBinder;

import java.io.File;
import java.io.FileFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.moviz.gui.preference.BindSummaryToValueListener.LISTENER;
import static com.moviz.gui.preference.BindSummaryToValueListener.SUMMARY;
import static com.moviz.gui.preference.BindSummaryToValueListener.SUMMARY_LISTENER;
import static com.moviz.gui.preference.BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY;
import static com.moviz.gui.preference.BindSummaryToValueListener.SUMMARY_NOTIFY;
import static com.moviz.lib.comunication.holder.DeviceHolder.CONF_DATA_TERMINATOR;

public class SettingsFragment extends PreferenceFragment implements CommandProcessor {
    private Context ctx = null;
    private List<PUserHolder> allUsers = null;
    private SharedPreferences sharedPref = null;
    private SharedPreferences.Editor prefEditor = null;
    private ListPreference pUser;
    private ListPreference pUserSel;
    private ListPreference pDateF;
    private ListPreference pStatusTemp;
    private ListPreference pWorkTemp;
    private Preference pDirTemp;
    private Preference pUserAdd;
    private Preference pUserEdt;
    private Preference pUserRem;
    private Preference pSessRem;
    private Preference pSessJoi;
    private Preference pSessExp;
    private Preference pDevAdd;
    private DBCleanPreference pSessCle;
    //private DBJoinPreference pSessJoi;
    private Preference pDbFold;
    private EditTextPreference pTcpPort;
    private EditTextPreference pSessionPoints;
    private EditTextPreference pConnRetryDelay;
    private EditTextPreference pConnRetryNum;
    private EditTextPreference pUpdateFreq;
    private CheckBoxPreference pScreenOn;
    private ConfNamePreference pConfSave;
    private ListPreference pConfSelect;
    private ListPreference pConfDelete;
    private ListPreference pConfSh;
    private Resources res = null;
    private List<DeviceSettings> deviceSettings = new ArrayList<DeviceSettings>();
    private SettingsServiceConnection mServiceConnection = null;
    private DeviceManagerBinder mBinder;
    private MyPListener mPCList;
    protected final static String TAG = SettingsFragment.class.getSimpleName();

    private String dateFormat;

    public static String getCommonConfData(SharedPreferences sharedPref) {
        Map<String, ?> keys = sharedPref.getAll();
        String[] vv = DeviceTypeMaps.type2confsave.get(null);
        String val = "";
        for (String v : vv) {
            Object o = keys.get(v);
            if (o != null) {
                val += v + "=" + o.toString() + "\n";
            }
        }
        val += CONF_DATA_TERMINATOR + "\n";
        return val;
    }

    public String getConfData() {
        String val = getCommonConfData(sharedPref);
        for (DeviceSettings ds : deviceSettings) {
            val += ds.getConfData();
        }
        return val;
    }

    public static boolean parseConfData(PConfHolder cnf, SharedPreferences sharedPref, SharedPreferences.Editor pEdit, List<PDeviceHolder> ldevh, CommandManager commandManager, CommandProcessor source) {
        boolean rv = false;
        String conf = cnf.getConf();
        String confname = cnf.getId()+"";
        String[] splits = conf.split("\n");
        boolean notifychange = !sharedPref.getString("pref_confselect", "").equals(confname);
        int sidx, nline = 0;
        if (notifychange) {
            pEdit.putString("pref_confselect", confname);
            rv = true;
        }
        for (String s : splits) {
            nline++;
            if (s.equals(CONF_DATA_TERMINATOR)) {
                if (notifychange) {
                    commandManager.postMessage(new DeviceChangedMessage(DeviceChangedMessage.Reason.BECAUSE_DEVICE_CHANGED, null, null, null),
                            source);
                }
                boolean dsrv = DeviceSettings.parseConfData(splits, nline, sharedPref, pEdit, ldevh, commandManager, source);
                if (!rv)
                    rv = dsrv;
                break;
            } else {
                sidx = s.indexOf('=');
                if (sidx > 0) {
                    String key = s.substring(0, sidx);
                    String value = sidx + 1 < s.length() ? s.substring(sidx + 1) : "";
                    pEdit.putString(key, value);
                    Log.v(TAG,"Putting "+key+" = "+value);
                    rv = true;
                    if (key.equals("pref_user"))
                        manageUserChange(value, commandManager, source);
                    else
                        notifychange = true;
                }
            }
        }
        return rv;
    }

    private static void manageUserChange(String value, CommandManager mBinder, CommandProcessor source) {
        if (!value.isEmpty()) {
            try {
                PUserHolder user = new PUserHolder();
                user.setId(Long.parseLong(value));
                MySQLiteHelper sqlite = MySQLiteHelper.newInstance(null, null);
                if (sqlite.getValue(user))
                    mBinder.postMessage(new UserSetMessage(user), source);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void manageEditSettings(Preference pref, String value) {
        prefEditor.putString(pref.getKey(), value).commit();
        BindSummaryToValueListener.CallInfo ci = mPCList.getCallInfo(pref);
        if (ci != null && ci.notifying())
            mPCList.notifyChange(ci, pref, value);

    }

    private class SettingsServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (DeviceManagerBinder) service;
            mBinder.addCommandProcessor(SettingsFragment.this, BluetoothRefreshMesssage.class, DeviceChangedMessage.class, UserSetMessage.class);
            for (DeviceSettings ds : deviceSettings) {
                ds.setCommandManager(mBinder);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            if (mBinder != null) {
                mBinder.removeCommandProcessor(SettingsFragment.this, BluetoothRefreshMesssage.class, DeviceChangedMessage.class);
                mBinder = null;
                for (DeviceSettings ds : deviceSettings) {
                    ds.setCommandManager(null);
                }
            }
        }

    }

    private class SessionRemoveDialog extends MultipleSessionSelectDialog {

        public SessionRemoveDialog(Date ifd, Date itd, String datef) {
            super(ifd, itd, datef);
        }

        @Override
        protected void onSessionSelect(
                LongSparseArray<List<PSessionHolder>> sessions, List<Long> keys) {
            // TODO Auto-generated method stub
            if (keys != null && !keys.isEmpty()) {
                MySQLiteHelper sqlite = MySQLiteHelper.newInstance(null, null);
                if (sqlite != null) {
                    String msid = "";
                    for (Long k : keys) {
                        msid += k + ",";
                    }
                    sqlite.deleteSessionByMainIds(msid.substring(0, msid.length() - 1));
                }
            }
        }
    }

    ;

    private class SessionJoinDialog extends MultipleSessionSelectDialog {

        public SessionJoinDialog(Date ifd, Date itd, String datef) {
            super(ifd, itd, datef);
        }

        @Override
        protected void onSessionSelect(
                LongSparseArray<List<PSessionHolder>> sessions, List<Long> keys) {
            // TODO Auto-generated method stub
            MySQLiteHelper sqlite;
            if (keys != null && !keys.isEmpty() && (sqlite = MySQLiteHelper.newInstance(null, null)) != null) {
                List<PSessionHolder> sold;
                PSessionHolder s1, s2;
                sold = sessions.get(keys.get(0));
                for (int j = 1; j < keys.size(); j++) {
                    sold.addAll(sessions.get(keys.get(j)));
                }
                Collections.sort(sold, new Comparator<PSessionHolder>() {

                    @Override
                    public int compare(PSessionHolder lhs, PSessionHolder rhs) {
                        long did1 = lhs.getDevice().getId();
                        long did2 = rhs.getDevice().getId();
                        if (did1 > did2) return 1;
                        else if (did1 < did2) return -1;
                        else if ((did1 = lhs.getDateStart()) > (did2 = rhs.getDateStart()))
                            return 1;
                        else if (did1 < did2) return -1;
                        else return 0;
                    }

                });
                for (int i = sold.size() - 1; i - 1 >= 0; i--) {
                    s1 = sold.get(i);
                    s2 = sold.get(i - 1);
                    if (s1.getDevice().equals(s2.getDevice())) {
                        sqlite.joinSessions(s2, s1);
                        sold.remove(i);
                    }
                }
            }
        }
    }

    ;


    public static String getDefaultAppDir(Resources res) {
        return Environment.getExternalStorageDirectory() + "/"
                + res.getString(R.string.app_name);
    }

    public static String getDefaultDbFolder(Resources res) {
        return getDefaultAppDir(res) + "/sessions";
    }

    public static String getDefaultTempFolder(Resources res) {
        return getDefaultAppDir(res) + "/templates";
    }

    private void setupTemplateList(final String tp, ListPreference lp) {
        FileFilter ff = new FileFilter() {
            public boolean accept(File file) {
                String nm;
                return file.isFile() && file.canRead() && (nm = file.getName()).startsWith(tp) && nm.endsWith(".vm");
            }
        };

        File[] files = new File(sharedPref.getString("pref_temp_dir", "")).listFiles(ff);
        //If this pathname does not denote a directory, then listFiles() returns null.
        if (files == null) {
            files = new File[0];
        }
        CharSequence entries[] = new String[files.length + 1];
        CharSequence entryValues[] = new String[files.length + 1];
        int i = 1;
        String nm;
        for (File u : files) {
            nm = u.getName();
            entries[i] = nm.substring(0, nm.length() - 3);
            entryValues[i] = u.getAbsolutePath();
            i++;
        }
        entries[0] = res.getString(R.string.pref_temp_raw);
        entryValues[0] = "r";
        lp.setEntries(entries);
        lp.setEntryValues(entryValues);
        if ((i = lp.findIndexOfValue(lp.getValue())) < 0) {
            lp.setValue("r");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity().getApplicationContext();
        // Load the preferences from an XML resource
        res = getResources();

        addPreferencesFromResource(R.xml.preferences);
        pUser = (ListPreference) findPreference("pref_user");
        pUserSel = (ListPreference) findPreference("pref_db_usel");
        pDateF = (ListPreference) findPreference("pref_datef");
        pStatusTemp = (ListPreference) findPreference("pref_temp_status");
        pWorkTemp = (ListPreference) findPreference("pref_temp_workout");
        pDirTemp = (Preference) findPreference("pref_temp_dir");
        pDevAdd = (Preference) findPreference("pref_device_add");
        pUserAdd = (Preference) findPreference("pref_db_uadd");
        pUserRem = (Preference) findPreference("pref_db_urem");
        pUserEdt = (Preference) findPreference("pref_db_uedt");
        pSessRem = (Preference) findPreference("pref_db_srem");
        pSessJoi = (Preference) findPreference("pref_db_sjoi");
        pSessExp = (Preference) findPreference("pref_db_sexp");
        pSessCle = (DBCleanPreference) findPreference("pref_db_scle");
        //pSessJoi = (DBJoinPreference)findPreference("pref_db_sjoi");
        pTcpPort = (EditTextPreference) findPreference("pref_tcpport");
        pSessionPoints = (EditTextPreference) findPreference("pref_sessionpoints");
        pConnRetryDelay = (EditTextPreference) findPreference("pref_connretrydelay");
        pConnRetryNum = (EditTextPreference) findPreference("pref_connretrynum");
        pUpdateFreq = (EditTextPreference) findPreference("pref_updatefreq");
        pConfSelect = (ListPreference) findPreference("pref_confselect");
        pConfDelete = (ListPreference) findPreference("pref_confdelete");
        pConfSh = (ListPreference) findPreference("pref_confsh");
        pConfSave = (ConfNamePreference) findPreference("pref_confsave");
        pScreenOn = (CheckBoxPreference) findPreference("pref_screenon");
        pDbFold = (Preference) findPreference("pref_dbfold");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        dateFormat = sharedPref.getString("pref_datef", "dd/MM/yy");
        prefEditor = sharedPref.edit();
        mPCList = new MyPListener(sharedPref);
        setupConfSelectList();
        setupFolderDbSearch();
        setupDateFormat();
        setupUserList();
        setupSessionList();
        setupTemplateList("status", pStatusTemp);
        setupTemplateList("workout", pWorkTemp);
        setupFolderTemplateSearch();
        DeviceSettings.restoreAll(this, getActivity(), mPCList, "pref_cat_device", deviceSettings, mBinder, this);
        mPCList.addPreference(pUser, null, new MyPListener.CallInfo(SUMMARY_LISTENER_NOTIFY));
        mPCList.addPreference(pUserSel, null, new MyPListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER));
        mPCList.addPreference(pDbFold, null, new MyPListener.CallInfo(SUMMARY_NOTIFY));
        mPCList.addPreference(pDateF, null, new MyPListener.CallInfo(BindSummaryToValueListener.SUMMARY_LISTENER_NOTIFY));
        mPCList.addPreference(pStatusTemp, null, new MyPListener.CallInfo(SUMMARY_LISTENER));
        mPCList.addPreference(pWorkTemp, null, new MyPListener.CallInfo(SUMMARY_LISTENER));
        mPCList.addPreference(pDirTemp, null, new MyPListener.CallInfo(SUMMARY));
        mPCList.addPreference(pTcpPort, null, new MyPListener.CallInfo(SUMMARY));
        mPCList.addPreference(pSessionPoints, null, new MyPListener.CallInfo(SUMMARY));
        mPCList.addPreference(pConnRetryDelay, null, new MyPListener.CallInfo(SUMMARY_NOTIFY));
        mPCList.addPreference(pConnRetryNum, null, new MyPListener.CallInfo(SUMMARY_NOTIFY));
        mPCList.addPreference(pUpdateFreq, null, new MyPListener.CallInfo(SUMMARY_NOTIFY));
        mPCList.addPreference(pConfSelect, null, new MyPListener.CallInfo(SUMMARY_NOTIFY));
        mPCList.addPreference(pConfDelete, null, new MyPListener.CallInfo(LISTENER));
        mPCList.addPreference(pConfSh, null, new MyPListener.CallInfo(LISTENER));
        mPCList.addPreference(pConfSave, null, new MyPListener.CallInfo(LISTENER));
        //bindPreferenceSummaryToValue(pScreenOn);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mServiceConnection == null) {
            mServiceConnection = new SettingsServiceConnection();
            ctx.bindService(new Intent(ctx, DeviceManagerService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        //setupDeviceSettingsIO(false);
        if (mServiceConnection != null) {
            ctx.unbindService(mServiceConnection);
            for (DeviceSettings ds : deviceSettings) {
                ds.setCommandManager(null);
            }
            mServiceConnection = null;
        }
        super.onPause();
    }

    private void setupDateFormat() {
        CharSequence[] values = pDateF.getEntryValues();
        CharSequence[] entries = new String[values.length];
        SimpleDateFormat sdf = new SimpleDateFormat();
        Date dt = new Date();
        for (int i = 0; i < values.length; i++) {
            sdf.applyPattern((String) values[i]);
            entries[i] = sdf.format(dt);
        }
        pDateF.setEntries(entries);
    }

    private void setupFolderDbSearch() {
        new FolderDialogChange(this, sharedPref, pDbFold, getDefaultDbFolder(res), res.getString(R.string.select)) {
            @Override
            public void afterchange(Preference pref, String value) {
                BindSummaryToValueListener.CallInfo ci = mPCList.getCallInfo(pref);
                if (ci != null && ci.notifying())
                    mPCList.notifyChange(ci, pref, value);
            }
        };
    }

    private void setupFolderTemplateSearch() {
        new FolderDialogChange(this, sharedPref, pDirTemp, getDefaultTempFolder(res), res.getString(R.string.select)) {
            @Override
            public void afterchange(Preference p, String newv) {
                setupTemplateList("status", pStatusTemp);
                setupTemplateList("workout", pWorkTemp);
            }
        };
    }

    private void saveConfData(String name) {
        String val = getConfData();
        MySQLiteHelper sqlite = MySQLiteHelper.newInstance(null, null);
        PConfHolder cnf;
        sqlite.newValue(cnf = new PConfHolder(name, val));
        setupConfSelectList();
        setConfNoNotification(cnf.getId()+"");
    }

    private void setConfNoNotification(String name) {
        prefEditor.putString(pConfSelect.getKey(),name);
        mPCList.reflectExternalChangeOnPreference(pConfSelect,name);
    }

    private class MyPListener extends BindSummaryToValueListener {

        public void notifyChange(CallInfo ci, Preference p, String value) {
            if (p == pUser)
                manageUserChange(value, mBinder, SettingsFragment.this);
            else if (p == pConfSelect) {
                if (mBinder != null && value != null)
                    mBinder.postMessage(new ConfChangeMessage(Long.parseLong(value)), SettingsFragment.this);
            } else if (mBinder != null) {
                String pkey = p.getKey();

                mBinder.postMessage(new DeviceChangedMessage(!pkey.startsWith("pref_devicepriv") ? DeviceChangedMessage.Reason.BECAUSE_DEVICE_CHANGED :
                        DeviceChangedMessage.Reason.BECAUSE_DEVICE_CONF_CHANGED, ci.device, pkey, value), SettingsFragment.this);
            }
        }

        public MyPListener(SharedPreferences prf) {
            super(prf);
        }

        @Override
        public boolean preferenceChange(CallInfo ci, Preference preference, Object v) {
            if (arrayContains(DeviceTypeMaps.type2confsave.get(null), preference.getKey()))
                setConfNoNotification(null);
            if (preference==pUserSel) {
                ListPreference lp = (ListPreference) preference;
                int index = lp.findIndexOfValue(v.toString());
                pUserEdt.setEnabled(index >= 0);
                pUserRem.setEnabled(index >= 0);
            }
            else if (preference==pDateF)
                dateFormat = v == null ? "dd/MM/yy" : v.toString();
            else if (preference==pConfSave) {
                if (v!=null) {
                    saveConfData(v.toString());
                }
            }
            else if (preference==pConfDelete) {
                if (v!=null) {
                    String selconf = pConfSelect.getValue();
                    if (selconf!=null && selconf.equals(v)) {
                        setConfNoNotification(null);
                    }
                    MySQLiteHelper sqlite = MySQLiteHelper.newInstance(null,null);
                    if (sqlite!=null) {
                        sqlite.deleteValue(new PConfHolder(Long.parseLong(v.toString()), "", ""));
                        setupConfSelectList();
                    }
                }
            }
            else if (preference==pConfSh) {
                if (v!=null) {
                    Intent addIntent = new Intent();
                    addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, getShortcutForConf(v.toString()));
                    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Moviz "+v.toString());
                    addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(res, R.drawable.ic_launcher_sh));

                    // Inform launcher to create shortcut
                    addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                    ctx.getApplicationContext().sendBroadcast(addIntent);
                }
            }
            else {
                String pkey = preference.getKey(), pkeystart;
                DeviceType devTp;
                PDeviceHolder d = ci.device;
                if (
                        d!=null && (
                        pkey.startsWith("pref_device_enabled") ||
                        (
                                pkey.startsWith(pkeystart = "pref_devicepriv_" + (devTp = d.getType()).name() + "_" + d.getId() + "_") &&
                                arrayContains(DeviceTypeMaps.type2confsave.get(devTp), pkey.substring(pkeystart.length()))
                        )
                    ))
                    setConfNoNotification(null);
                for (DeviceSettings devs : deviceSettings)
                    if (devs.onPreferenceChange(preference, v))
                        return true;
                return false;
            }
            return true;
        }
    }

    private Intent getShortcutForConf(String name) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                ctx.getApplicationContext().getPackageName(), ActivityMain.class.getName()));
        //Intent intent = new Intent(ctx, ActivityMain.class);
        intent.setAction(DeviceManagerService.ACTION_LOAD_CONFIGURATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(DeviceManagerService.EXTRA_CONFIGURATION_NAME,name);
        return intent;
    }

    private boolean arrayContains(String[] strings, String key) {
        for (String s:strings)
            if (s.equals(key))
                return true;
        return false;
    }

    private abstract class ValidatingTextWatcher implements TextWatcher {
        protected EditText myETX = null;
        protected Drawable originalDrawable = null, newDrawable = null;
        public final static int DEFAULT_COLOR = Color.RED;

        public ValidatingTextWatcher(EditText etx, int wrongColor) {
            myETX = etx;
            myETX.addTextChangedListener(this);
            originalDrawable = etx.getBackground();
            newDrawable = new ColorDrawable(wrongColor);
        }

        public ValidatingTextWatcher(EditText etx) {
            this(etx, DEFAULT_COLOR);
        }

        public abstract boolean validateText(Editable s);

        public boolean validateText() {
            return validateText(myETX.getText());
        }

        @SuppressLint("NewApi")
        public void afterTextChanged(Editable s) {
            if (s == null)
                s = myETX.getText();
            Drawable d = validateText(s) ? originalDrawable : newDrawable;
            int sdk = android.os.Build.VERSION.SDK_INT;
            int jellyBean = android.os.Build.VERSION_CODES.JELLY_BEAN;
            if (sdk < jellyBean) {
                myETX.setBackgroundDrawable(d);
            } else {
                myETX.setBackground(d);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }
    }

    private class IntGT0TextWatcher extends ValidatingTextWatcher {
        public IntGT0TextWatcher(EditText etx, int wrongColor) {
            super(etx, wrongColor);
        }

        public IntGT0TextWatcher(EditText etx) {
            super(etx);
        }

        public boolean validateText(Editable s) {
            int val = 0;
            try {
                val = Integer.parseInt(s.toString());
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                val = 0;
            }
            return val > 0;
        }
    }

    private class StringNotNullTextWatcher extends ValidatingTextWatcher {
        public StringNotNullTextWatcher(EditText etx, int wrongColor) {
            super(etx, wrongColor);
        }

        public StringNotNullTextWatcher(EditText etx) {
            super(etx);
        }

        public boolean validateText(Editable s) {
            String str;
            return s.length() > 0 && (str = s.toString()).indexOf(":") < 0 && str.indexOf("/") < 0;
        }
    }

    private class CorrectDateTextWatcher extends ValidatingTextWatcher {
        private SimpleDateFormat sdf = null;

        public CorrectDateTextWatcher(EditText etx, int wrongColor) {
            super(etx, wrongColor);
            sdf = new SimpleDateFormat(pDateF.getValue());
        }

        public CorrectDateTextWatcher(EditText etx) {
            this(etx, DEFAULT_COLOR);
        }

        public boolean validateText(Editable s) {
            Date d = null;
            int age = 0;
            try {
                d = sdf.parse(s.toString());
                age = PUserHolder.getAge(d);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                d = null;
                e.printStackTrace();
            }
            return d != null && age > 10 && age < 255;
        }
    }

    private void openUserDialog(final PUserHolder user) {
        Activity a = getActivity();
        if (a != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(a);
            // Get the layout inflater
            LayoutInflater inflater = a.getLayoutInflater();
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog
            // layout
            final View layout = inflater.inflate(R.layout.useralert, null);
            builder.setView(layout);
            final EditText nameETX = (EditText) layout
                    .findViewById(R.id.nameETX);
            final RadioButton maleRDB = (RadioButton) layout
                    .findViewById(R.id.maleRDB);
            final RadioButton femaleRDB = (RadioButton) layout
                    .findViewById(R.id.femaleRDB);
            final EditText heightETX = (EditText) layout
                    .findViewById(R.id.heightETX);
            final EditText weightETX = (EditText) layout
                    .findViewById(R.id.weightETX);
            final EditText bornETX = (EditText) layout
                    .findViewById(R.id.bornETX);
            final SimpleDateFormat sdf = new SimpleDateFormat(pDateF.getValue());
            if (user != null) {
                nameETX.setText(user.getName());
                if (user.isMale())
                    maleRDB.setChecked(true);
                else
                    femaleRDB.setChecked(true);

                weightETX.setText("" + ((int) (user.getWeight() + 0.5)));
                heightETX.setText("" + ((int) (user.getHeight() + 0.5)));
                bornETX.setText(sdf.format(new Date(user.getBirthDay())));
            } else {
                maleRDB.setChecked(true);
                bornETX.setText(sdf.format(new Date()));
            }
            final ValidatingTextWatcher[] tws = new ValidatingTextWatcher[]{
                    new StringNotNullTextWatcher(nameETX),
                    new IntGT0TextWatcher(heightETX),
                    new IntGT0TextWatcher(weightETX),
                    new CorrectDateTextWatcher(bornETX),};
            for (ValidatingTextWatcher vt : tws) {
                vt.afterTextChanged(null);
            }
            final AlertDialog ad = builder.create();
            ad.setTitle(res.getString(R.string.pref_useradd_title));
            ad.setButton(AlertDialog.BUTTON_NEGATIVE, res.getString(R.string.pref_useradd_ko),// sett
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ad.dismiss();
                        }
                    });
            ad.setButton(AlertDialog.BUTTON_POSITIVE, res.getString(R.string.pref_useradd_ok),
                    (DialogInterface.OnClickListener) null);

            ad.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {

                    Button b = ad.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            // TODO Do something
                            boolean dismiss = true;
                            for (ValidatingTextWatcher vt : tws) {
                                if (!vt.validateText())
                                    dismiss = false;
                            }
                            if (dismiss) {
                                try {
                                    String name = nameETX.getText().toString();
                                    int weight = Integer.parseInt(weightETX
                                            .getText().toString());
                                    int height = Integer.parseInt(heightETX
                                            .getText().toString());
                                    boolean male = maleRDB.isChecked();
                                    long birth = sdf.parse(
                                            bornETX.getText().toString())
                                            .getTime();
                                    PUserHolder u;
                                    if (user == null)
                                        u = new PUserHolder(-1,
                                                name, male, weight, height,
                                                birth, true);
                                    else {
                                        u = user;
                                        u.setName(name);
                                        u.setWeight(weight);
                                        u.setHeight(height);
                                        u.setMale(male);
                                        u.setBirthDay(birth);
                                    }
                                    MySQLiteHelper sqlite = MySQLiteHelper.newInstance(null, null);
                                    if (sqlite != null)
                                        sqlite.newValue(u);
                                    if (user == null) {
                                        manageEditSettings(pUserSel, u.getId() + "");
                                        fillUserList();
                                        mPCList.bindPreferenceSummaryToValue(pUserSel, u.getId() + "");
                                    }
                                    ad.dismiss();

                                } catch (NumberFormatException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                } catch (ParseException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
                }
            });
            ad.show();
        }
    }

    private void setupSessionList() {
        pSessExp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Activity a = getActivity();
                if (a != null)
                    new SessionExporterAction(null, null, pDateF.getValue(), sharedPref.getString("pref_dbfold", getDefaultDbFolder(res))).show(a);
                return true;
            }
        });

        pSessRem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Activity a = getActivity();
                if (a != null)
                    new SessionRemoveDialog(null, null, dateFormat).show(a);
                return false;
            }
        });

        pSessJoi.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Activity a = getActivity();
                if (a != null)
                    new SessionJoinDialog(null, null, dateFormat).show(a);
                return false;
            }
        });

        //pSessJoi.setOnPreferenceChangeListener(sessionCleanListener);
    }

    private void setupConfSelectList() {
        MySQLiteHelper sqlite = MySQLiteHelper.newInstance(null, null);
        List<PConfHolder> confs;
        if (sqlite != null)
            confs = (List<PConfHolder>) sqlite.getAllValues(new PConfHolder(),"name");
        else
            confs = new ArrayList<>();
        CharSequence entries[] = new String[confs.size()];
        CharSequence entryValues[] = new String[confs.size()];
        int i = 0;
        for (PConfHolder entry : confs) {
            entryValues[i] = entry.getId()+"";
            entries[i] = entry.getName();
            i++;
        }
        pConfSelect.setEntries(entries);
        pConfSelect.setEntryValues(entryValues);
        pConfDelete.setEntries(entries);
        pConfDelete.setEntryValues(entryValues);
        pConfSh.setEntries(entries);
        pConfSh.setEntryValues(entries);
    }

    private void fillUserList() {
        MySQLiteHelper sqlite = MySQLiteHelper.newInstance(null, null);
        if (sqlite != null)
            allUsers = (List<PUserHolder>) sqlite.getAllValues(new PUserHolder(), "name");
        else
            allUsers = new ArrayList<PUserHolder>();
        CharSequence entries[] = new String[allUsers.size()];
        CharSequence entryValues[] = new String[allUsers.size()];
        int i = 0;
        for (PUserHolder u : allUsers) {
            entries[i] = u.getName();
            entryValues[i] = u.getId() + "";
            i++;
        }
        pUser.setEntries(entries);
        pUser.setEntryValues(entryValues);
        pUserSel.setEntries(entries);
        pUserSel.setEntryValues(entryValues);
    }

    private void setupUserList() {
        fillUserList();
        pUserEdt.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                String v = pUserSel.getValue();
                int index;
                if (v != null && !v.isEmpty()
                        && (index = pUserSel.findIndexOfValue(v)) >= 0) {
                    openUserDialog(allUsers.get(index));
                }
                return true;
            }
        });

        pUserRem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            private void clearUser(Preference p) {
                manageEditSettings(p, "-1");
                mPCList.bindPreferenceSummaryToValue(p, "-1");
            }

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                String v = pUserSel.getValue();
                int index;
                if (v != null && !v.isEmpty()
                        && (index = pUserSel.findIndexOfValue(v)) >= 0) {
                    PUserHolder u = allUsers.get(index);
                    MySQLiteHelper sqlite = MySQLiteHelper.newInstance(null, null);
                    if (sqlite != null)
                        sqlite.deleteValue(u);
                    if (pUser.getValue().equals(u.getId() + ""))
                        clearUser(pUser);
                    clearUser(pUserSel);
                    fillUserList();
                }
                return true;
            }
        });
        pDevAdd.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                Activity a = getActivity();
                if (a != null)
                    deviceSettings.add(DeviceSettings.newDevice(SettingsFragment.this, a, mPCList, "pref_cat_device", mBinder, SettingsFragment.this));
                return true;
            }
        });
        pUserAdd.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                openUserDialog(null);
                return true;
            }
        });

    }

    @Override
    public BaseMessage processCommand(BaseMessage hs2) {
        if (hs2 instanceof BluetoothRefreshMesssage) {
            BluetoothRefreshMesssage brm = (BluetoothRefreshMesssage) hs2;
            for (DeviceSettings devs : deviceSettings)
                devs.bluetoothRefresh(brm.getSource(), brm.getDevices());
        } else if (hs2 instanceof DeviceChangedMessage) {
            DeviceChangedMessage dcm = (DeviceChangedMessage) hs2;
            PDeviceHolder devh = dcm.getDev();
            if (devh == null) {
                pStatusTemp.setValue(sharedPref.getString(pStatusTemp.getKey(), ""));
                pWorkTemp.setValue(sharedPref.getString(pWorkTemp.getKey(), ""));
                setupTemplateList("status", pStatusTemp);
                setupTemplateList("workout", pWorkTemp);
            } else {
                for (DeviceSettings ds : deviceSettings) {
                    if (ds.processExternalDeviceChange(devh))
                        break;
                }
            }
        } else if (hs2 instanceof UserSetMessage) {
            String newv = ((UserSetMessage) hs2).getUser().getId()+"";
            mPCList.addPreference(pUser,null,new BindSummaryToValueListener.CallInfo(SUMMARY));
            pUser.setValue(newv);
            mPCList.addPreference(pUser,null,new BindSummaryToValueListener.CallInfo(SUMMARY_NOTIFY));
        }
        else
            return null;
        return new ProcessedOKMessage();
    }

}