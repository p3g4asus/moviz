package com.moviz.workers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v7.preference.PreferenceManager;

import com.moviz.gui.R;
import com.moviz.gui.app.CA;
import com.moviz.gui.fragments.SettingsFragment;
import com.moviz.gui.util.Messages;
import com.moviz.lib.comunication.DeviceStatus;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.message.ConnectMessage;
import com.moviz.lib.comunication.message.DisconnectMessage;
import com.moviz.lib.comunication.message.ExitMessage;
import com.moviz.lib.comunication.plus.holder.PConfHolder;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.comunication.plus.holder.UpdateDatabasable;
import com.moviz.lib.comunication.plus.message.BluetoothRefreshMesssage;
import com.moviz.lib.comunication.plus.message.ConfChangeMessage;
import com.moviz.lib.comunication.plus.message.DeviceChangeRequestMessage;
import com.moviz.lib.comunication.plus.message.DeviceChangedMessage;
import com.moviz.lib.comunication.plus.message.ProcessedOKMessage;
import com.moviz.lib.comunication.plus.message.TerminateMessage;
import com.moviz.lib.comunication.plus.message.UserSetMessage;
import com.moviz.lib.db.MySQLiteHelper;
import com.moviz.lib.hw.BluetoothState;
import com.moviz.lib.hw.DeviceListener;
import com.moviz.lib.hw.DeviceReadyListener;
import com.moviz.lib.hw.DeviceSearcher;
import com.moviz.lib.hw.GenericDevice;
import com.moviz.lib.utils.CommandManager;
import com.moviz.lib.utils.CommandProcessor;
import com.moviz.lib.utils.DeviceTypeMaps;
import com.moviz.lib.utils.ParcelableMessage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;


public class DeviceManagerService extends Service implements CommandProcessor {
    private static final int RELOAD_DEVICES = 1;
    private static final int RELOAD_OTHER_SETTINGS = 4;
    public static final String TAG = DeviceManagerService.class.getSimpleName();
    private static final int STATE_CONNECTED = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_REBINDING = 4;
    private static final int STATE_SEARCHING = 8;
    private static final int STATE_STOPPING = 16;
    private static final int STATE_EXITING = 32;
    public static final String ACTION_LOAD_CONFIGURATION = "DeviceManagerService.ACTION_LOAD_CONFIGURATION";
    public static final String EXTRA_CONFIGURATION_NAME = "DeviceManagerService.EXTRA_CONFIGURATION_NAME";
    public static final String EXTRA_DEBUG_FLAG = "DeviceManagerService.EXTRA_DEBUG_FLAG";
    private final static int NOTIFICATION_ID = 999998;

    private int mDebugFlag = 0;
    private String dbFold = null;
    private Context ctx = null;
    private int reloadOnDisc = RELOAD_DEVICES | RELOAD_OTHER_SETTINGS;
    private int needBluetoothEnable = -1;
    private SharedPreferences sharedPref = null;
    private Intent connectingStatus = null;
    private long connectingStatusT = 0;
    private int connRetryStatus, connRetryNum, connRetryDelay;
    private Map<PDeviceHolder, PSessionHolder> sessionMap = new ConcurrentHashMap<>();
    private Map<PDeviceHolder, GenericDevice> holder2deviceinstance = Collections.synchronizedMap(new LinkedHashMap<PDeviceHolder, GenericDevice>());
    private Vector<PDeviceHolder> holder2connectingDev = new Vector<>();
    private long mainSessionId = -1;
    private int setBluetoothAfterDisc = -1;
    private int managerState = 0;
    private int searchDeviceDone = 0;
    private DeviceSearcher currentDS = null;
    private PDeviceHolder currentDSDev = null;
    private StatusReceiver mStatusReceiver;
    private Vector<GenericDevice> searchDeviceList = new Vector<>();
    private TCPServer tcpServer;
    private DeviceManagerBinder mBinder = null;
    private static DeadState mDead = DeadState.NOTSTARTED;
    public enum DeadState {
        DEAD,
        NOTSTARTED,
        STARTED
    }
    private PDeviceHolder lastConnectingDevice = null;
    private HandlerThread mHandlerThread = new HandlerThread();

    public static DeadState dead() {
        return mDead;
    }

    private class HandlerThread extends Thread {
        private Handler mHandler;

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }

        public boolean runOnMe(Runnable r) {
            if (Thread.currentThread().equals(this)) {
                r.run();
                return true;
            }
            else {
                mHandler.post(r);
                return false;
            }
        }

        public void init() {
            if (mHandler==null) {
                start();
                while (true) {
                    if (mHandler == null) {
                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    } else
                        break;
                }
            }
        }

        public void waitFinish(Runnable runnable) {
            if (!runOnMe(runnable)) {
                mHandler.sendEmptyMessage(1500);
                while(mHandler.hasMessages(1500)) {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public BaseMessage processCommand(final BaseMessage hs2) {
        mHandlerThread.runOnMe(new Runnable() {
            @Override
            public void run() {
                GenericDevice dev;
                PDeviceHolder devh;
                Timber.tag(TAG).w("Processing " + hs2.getClass().getSimpleName());
                if (hs2 instanceof ConnectMessage && (managerState & (STATE_CONNECTING | STATE_STOPPING | STATE_REBINDING | STATE_SEARCHING)) == 0) {
                    connectTimerStop();
                    holder2connectingDev.clear();
                    connRetryStatus = 0;
                    reset();
                    Timber.tag(TAG).d("1 programStop false");
                    for (Map.Entry<PDeviceHolder, GenericDevice> entry : holder2deviceinstance.entrySet()) {
                        dev = entry.getValue();
                        devh = entry.getKey();
                        if (!dev.isConnected() && devh.isEnabled()) {
                            holder2connectingDev.add(devh);
                        }
                    }
                    if (!holder2connectingDev.isEmpty()) {
                        reset();
                        prepareConnection();
                    }
                } else if (hs2 instanceof BluetoothRefreshMesssage && ((BluetoothRefreshMesssage) hs2).getDevices() == null) {
                    currentDSDev = ((BluetoothRefreshMesssage) hs2).getSource();
                    if ((managerState & (STATE_STOPPING | STATE_CONNECTING | STATE_REBINDING | STATE_SEARCHING)) == 0) {
                        if (currentDSDev != null) {
                            currentDS = DeviceTypeMaps.type2search.get(currentDSDev.getType());
                            managerState |= STATE_SEARCHING;
                            if (!setBluetooth(true))
                                setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) new ParcelableMessage("exm_errs_adapter")));
                        }
                    } else
                        mBinder.postMessage(new BluetoothRefreshMesssage(currentDSDev, null),DeviceManagerService.this);
                } else if (hs2 instanceof DeviceChangedMessage) {
                    DeviceChangedMessage dcm = (DeviceChangedMessage) hs2;
                    devh = dcm.getDev();
                    DeviceChangedMessage.Reason why = dcm.getWhy();
                    if (devh == null)
                        reloadOnDisc |= RELOAD_OTHER_SETTINGS;
                    else if (!why.equals(DeviceChangedMessage.Reason.BECAUSE_DEVICE_CONF_CHANGED))
                        reloadOnDisc |= RELOAD_DEVICES;
            /*if ((managerState & (STATE_STOPPING | STATE_CONNECTING | STATE_REBINDING | STATE_SEARCHING)) == 0)
                reset();*/
                } else if (hs2 instanceof UserSetMessage) {
                    newUser = ((UserSetMessage) hs2).getUser();
                    reloadOnDisc |= RELOAD_OTHER_SETTINGS;
            /*if ((managerState & (STATE_STOPPING | STATE_CONNECTING | STATE_REBINDING | STATE_SEARCHING)) == 0)
                reset();*/
                } else if (hs2 instanceof ConfChangeMessage) {
                    ConfChangeMessage ccm = (ConfChangeMessage) hs2;
                    String ccmn = ccm.getConfName();
                    PConfHolder cnf;
                    if (ccmn!=null) {
                        cnf = new PConfHolder();
                        if (sqlite!=null && sqlite.getValue(cnf,cnf.getNameCondition(ccmn)))
                            newConfData(cnf);
                    }
                    else {
                        cnf = new PConfHolder(ccm.getConfId(), "", "");
                        if (sqlite != null && sqlite.getValue(cnf))
                            newConfData(cnf);
                    }
                } else if (hs2 instanceof DeviceChangeRequestMessage) {
                    devh = ((DeviceChangeRequestMessage) hs2).getDev();
                    String key, value;
                    if (devh == null) {
                        key = ((DeviceChangeRequestMessage) hs2).getKey();
                        reloadOnDisc |= RELOAD_OTHER_SETTINGS;
                    } else {
                        key = ((DeviceChangeRequestMessage) hs2).getFullKey();
                    }
                    value = ((DeviceChangeRequestMessage) hs2).getValue();
                    sharedPref.edit().putString(key,value).commit();
                    if (key.equals("pref_user"))
                        newUser = reloadUser();
                    else {
                        if (devh!=null && sqlite!=null && !key.startsWith("tmp"))
                            sqlite.newValue(devh);
                        mBinder.postMessage(new DeviceChangedMessage(DeviceChangedMessage.Reason.BECAUSE_DEVICE_CONF_CHANGED, devh,key,value),DeviceManagerService.this);
                    }
                } else if (hs2 instanceof ExitMessage) {
                    managerState |= STATE_EXITING;
                    Timber.tag(TAG).d("1 needtoexit true");
                    stopOperations();
                } else if (hs2 instanceof DisconnectMessage) {
                    //managerState&=(~STATE_EXITING);
                    Timber.tag(TAG).d("2 needtoexit false");
                    stopOperations();
                }
            }
        });

        return new ProcessedOKMessage();
    }

    public static class DeviceManagerBinder extends Binder implements CommandManager {
        private final StatusReceiver mStatusReceiver;
        private final Context mContext;
        private HashMap<Class<? extends BaseMessage>, Vector<CommandProcessor>> mCommandProcessors = new HashMap<>();
        private Handler mMainHabdler = null;
        public DeviceManagerBinder(Context ctx,StatusReceiver sr) {
            mStatusReceiver = sr;
            mContext = ctx;
            mMainHabdler = new Handler(ctx.getMainLooper());
        }
        public StatusReceiver getStatusReceiver() {
            return mStatusReceiver;
        }

        public void stop() {
            postMessage(new ExitMessage(),null);
        }

        protected void runOnMainThread(Runnable r) {
            if (mContext.getMainLooper().getThread().equals(Thread.currentThread()))
                r.run();
            else
                mMainHabdler.post(r);
        }

        @Override
        public void addCommandProcessor(final CommandProcessor cmdp, final Class<? extends BaseMessage>... messages) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Vector<CommandProcessor> cpv;
                    for (Class<? extends BaseMessage> c : messages) {
                        if (mCommandProcessors.containsKey(c))
                            cpv = mCommandProcessors.get(c);
                        else {
                            cpv = new Vector<>();
                            mCommandProcessors.put(c, cpv);
                        }
                        if (!cpv.contains(cmdp))
                            cpv.add(cmdp);
                    }
                }
            });
        }

        @Override
        public void removeCommandProcessor(final CommandProcessor cmdp, final Class<? extends BaseMessage>... messages) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Vector<CommandProcessor> cpv;
                    for (Class<? extends BaseMessage> c : messages) {
                        if (mCommandProcessors.containsKey(c)) {
                            cpv = mCommandProcessors.get(c);
                            cpv.remove(cmdp);
                            if (cpv.isEmpty())
                                mCommandProcessors.remove(c);
                        }
                    }
                }
            });

        }

        @Override
        public void postMessage(final BaseMessage bm, final CommandProcessor source) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Class<? extends BaseMessage> c = bm.getClass();
                    if (mCommandProcessors.containsKey(c)) {
                        BaseMessage rv;
                        Vector<CommandProcessor> cpv = mCommandProcessors.get(c);
                        int i = 0;
                        CommandProcessor cp;
                        while (true) {
                            synchronized (cpv) {
                                if (i<cpv.size())
                                    cp = cpv.get(i);
                                else
                                    break;
                            }
                            if (cp != source) {
                                rv = cp.processCommand(bm);
                                if (rv != null && !(rv instanceof ProcessedOKMessage))
                                    postMessage(rv, cp);
                            }
                            i++;
                        }
                    }
                }
            });
        }
    }


    private Intent setConnectingStatus(Intent newcs) {
        connectingStatus = newcs;
        connectingStatusT = System.currentTimeMillis();
        CA.lbm.sendBroadcast(newcs);
        return newcs;
    }


    private int nConnectedDevices() {
        GenericDevice dev;
        int n = 0;
        for (Map.Entry<PDeviceHolder, GenericDevice> entry : holder2deviceinstance.entrySet()) {
            dev = entry.getValue();
            if (dev.isConnected())
                n++;
        }
        return n;
    }

    /*private void deviceSettingsChanged(String key) {
        key = key.substring("pref_devicepriv_".length());
        int idx = key.indexOf('_');
        if (idx>=0) {
            int idx2 = key.indexOf('_', idx+1);
            if (idx2>0) {
                long v = Long.parseLong(key.substring(idx+1,idx2));
                for (PDeviceHolder devh:holder2deviceinstance.keySet()) {
                    if (v==devh.getId()) {
                        reloadMap.put(devh, (byte)2);
                        return;
                    }
                }
            }
        }
    }*/
    private GUIListener loglist = new GUIListener();
    private CommandReceiver commandReceiver = new CommandReceiver();
    private BluetoothStateReceiver bluetoothReceiver = new BluetoothStateReceiver();
    private MySQLiteHelper sqlite = null;
    private PUserHolder userObj = null, newUser = null;


    private void newConfData(PConfHolder cnf) {
        if (cnf.getConf().length() > 0) {
            List<PDeviceHolder> ldevh = (List<PDeviceHolder>) sqlite.getAllValues(new PDeviceHolder(), "orderd");
            SharedPreferences.Editor pEdit = sharedPref.edit();
            if (SettingsFragment.parseConfData(cnf, sharedPref, pEdit, ldevh, mBinder, null))
                pEdit.commit();
        }
    }

    private void closeDB() {
        if (sqlite != null) {
            try {
                sqlite.closeDB();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private MySQLiteHelper reloadDBConf() {
        Resources res = null;
        try {
            res = getResources();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        String newfold = sharedPref.getString("pref_dbfold", res == null ? "" : SettingsFragment.getDefaultDbFolder(this));
        if (dbFold==null || !newfold.equals(dbFold) || sqlite==null) {
            try {
                closeDB();
                dbFold = newfold;
                File fdb = new File(dbFold);
                if (!fdb.exists())
                    fdb.mkdirs();
                if (!fdb.exists() || !fdb.isDirectory() || !fdb.canRead() || !fdb.canWrite()) {
                    throw new IOException("Invalid forlder " + dbFold);
                } else {
                    sqlite = MySQLiteHelper.newInstance(ctx, dbFold);
                }
            } catch (Exception e) {
                closeDB();
            }
        }
        return sqlite;
    }

    private PUserHolder reloadUser() {
        PUserHolder user = null;
        int userN;
        try {
            userN = Integer.parseInt(sharedPref.getString("pref_user", "-1"));
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            userN = -1;
        }
        Timber.tag(TAG).d("User is "+userN);
        try {
            if (reloadDBConf() != null) {
                user = new PUserHolder();
                user.setId(userN);
                if (!sqlite.getValue(user))
                    user = null;
                else
                    mBinder.postMessage(new UserSetMessage(user),this);
            }
        } catch (Exception e) {
            user = null;
        }
        return user;
    }

    private void reset() {
        Timber.tag(TAG).d("Resetting "+reloadOnDisc);
        int connectedd = nConnectedDevices();
        connRetryNum = Integer.parseInt(sharedPref.getString("pref_connretrynum", "5"));
        connRetryDelay = Integer.parseInt(sharedPref.getString("pref_connretrydelay", "30"));
        if (connRetryDelay <= 0)
            connRetryDelay = 30;
        if ((reloadOnDisc & RELOAD_OTHER_SETTINGS) != 0 && connectedd == 0) {
            Timber.tag(TAG).d("Deciding user "+userObj+" "+newUser);
            if (newUser!= null && (userObj==null || !newUser.equals(userObj))) {
                userObj = newUser;
            }
            else if (newUser==null && userObj==null) {
                userObj = reloadUser();
            }
            newUser = null;
        }
        if ((reloadOnDisc & RELOAD_DEVICES) != 0 && sqlite != null && connectedd == 0) {
            List<PDeviceHolder> lst = (List<PDeviceHolder>) sqlite.getAllValues(new PDeviceHolder(), "orderd");
            GenericDevice dev;
            holder2connectingDev.clear();
            connRetryStatus = 0;
            for (Map.Entry<PDeviceHolder, GenericDevice> entry : holder2deviceinstance.entrySet()) {
                dev = entry.getValue();
                dev.stop();
                Timber.tag(TAG).d("Stopping "+entry.getKey());
            }
            holder2deviceinstance.clear();
            for (PDeviceHolder devh : lst) {
                try {
                    GenericDevice inst = DeviceTypeMaps.type2deviceclass.get(devh.getType()).newInstance();
                    if (inst.initOnce(devh, userObj, ctx, mBinder)) {
                        inst.setDebugFlag(mDebugFlag);
                        inst.loadTransientPref(sharedPref);
                        holder2deviceinstance.put(devh, inst);
                        if (devh.isEnabled())
                            holder2connectingDev.add(devh);
                        inst.setDeviceReadyListener(loglist);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (connectedd == 0 && sqlite != null && userObj != null)
            reloadOnDisc = 0;
    }

    private void stopOperations() {
        Timber.tag(TAG).d("2 programStop true");
        managerState |= STATE_STOPPING;
        if ((managerState & STATE_SEARCHING) != 0) {
            currentDS.stopSearch();
            return;
        } else if ((managerState & STATE_REBINDING) != 0) {
            currentDS.stopRebind();
            return;
        }
        GenericDevice dev;

        connectTimerStop();
        sessionMap.clear();
        holder2connectingDev.clear();
        searchDeviceDone = 0;
        searchDeviceList.clear();
        mainSessionId = -1;
        connRetryStatus = 0;
        currentDS = null;
        synchronized (this) {
            setBluetoothAfterDisc = 0;

            for (Map.Entry<PDeviceHolder, GenericDevice> entry : holder2deviceinstance.entrySet()) {
                dev = entry.getValue();
                if (dev.isConnected()) {
                    setBluetoothAfterDisc++;
                    dev.disconnect();
                } else if (dev.getBluetoothState() == BluetoothState.CONNECTING) {
                    setBluetoothAfterDisc++;
                }
            }
            Timber.tag(TAG).d("4 bluetoothafter " + setBluetoothAfterDisc);
            tcpServer.stopListening();
            if (setBluetoothAfterDisc == 0)
                setBluetooth(false);
        }
    }

    private class BluetoothStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Timber.tag(TAG).w("Pairing " + action);
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if ((managerState & STATE_EXITING) != 0)
                            doExit();
                        else {
                            managerState &= (~STATE_STOPPING);
                            setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE));
                            setupBluetoothIO(false, null);
                            setBluetoothAfterDisc = -1;
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE));
                        setupBluetoothIO(false, null);
                        if ((managerState & STATE_SEARCHING) != 0)
                            performDeviceSearch();
                        else
                            connectTimerInit(true);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }

    }

    private void performDeviceSearch() {
        if (currentDS != null && currentDSDev != null) {
            setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) new ParcelableMessage("exm_errp_searchingdev").put(getResources().getString(DeviceTypeMaps.type2res.get(currentDSDev.getType())))));
            currentDS.startSearch(ctx);
        }
    }

    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {
                    String msg = intent.getAction();
                    if (msg.equals(Messages.CMDGETCONSTATUS_MESSAGE)) {
                        long timeIb = intent.getLongExtra("t", 0);
                        if (connectingStatus != null && timeIb <= connectingStatusT)
                            CA.lbm.sendBroadcast(connectingStatus.putExtra("response", true));
                    } else if (msg.equals(DeviceSearcher.DEVICE_SEARCH_ERROR) && currentDS != null) {
                        currentDS = null;
                        managerState &= (~STATE_SEARCHING);
                        if ((managerState & STATE_STOPPING) != 0) {
                            setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE));
                            stopOperations();
                        } else
                            setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", intent.getParcelableExtra(DeviceSearcher.DEVICE_ERROR_CODE)));
                    } else if (msg.equals(DeviceSearcher.DEVICE_REBIND_ERROR) && currentDS != null) {
                        currentDS = null;
                        managerState &= (~STATE_REBINDING);
                        if ((managerState & STATE_STOPPING) != 0) {
                            setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE));
                            stopOperations();
                        } else {
                            int idx = intent.getIntExtra(DeviceSearcher.DEVICE_ERROR_IDX, -1);
                            ParcelableMessage pme = intent.getParcelableExtra(DeviceSearcher.DEVICE_ERROR_CODE);
                            if (idx >= 0 && idx < searchDeviceList.size())
                                pme.put(searchDeviceList.get(idx).innerDevice());
                            setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) pme));
                        }
                    } else if (msg.equals(DeviceSearcher.DEVICE_REBIND_OK) && currentDS != null) {
                        currentDS = null;
                        managerState &= (~STATE_REBINDING);
                        setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE));
                        if ((managerState & STATE_STOPPING) != 0)
                            stopOperations();
                        else
                            connectTimerInit(true);

                    } else if (msg.equals(DeviceSearcher.DEVICE_SEARCH_END) && currentDSDev != null) {
                        BluetoothRefreshMesssage brm = new BluetoothRefreshMesssage(currentDSDev, (PDeviceHolder[]) intent.getParcelableArrayExtra(DeviceSearcher.DEVICE_FOUND));
                        currentDS = null;
                        currentDSDev = null;
                        setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE));
                        mBinder.postMessage(brm, DeviceManagerService.this);
                        managerState &= (~STATE_SEARCHING);
                        if ((managerState & STATE_STOPPING) != 0)
                            stopOperations();
                        else if ((managerState & STATE_CONNECTED) == 0)
                            setBluetooth(false);
                    }
                }
            });
        }
    }

    private ConnectTimerTask connectTimerTask = new ConnectTimerTask();

    private class ConnectTimerTask implements Runnable {

        @Override
        public void run() {
            if ((managerState & STATE_STOPPING) == 0 && !holder2connectingDev.isEmpty()) {
                PDeviceHolder devh;
                GenericDevice dev;
                for (Iterator<PDeviceHolder> it = holder2connectingDev.iterator(); it.hasNext(); ) {
                    devh = it.next();
                    dev = holder2deviceinstance.get(devh);
                    if (dev.isConnected() || !devh.isEnabled() || (connRetryStatus >= connRetryNum && connRetryNum != 0)) {
                        it.remove();
                        connRetryStatus = 0;
                    }
                }
                connRetryStatus++;
                if (!holder2connectingDev.isEmpty()) {
                    for (Iterator<PDeviceHolder> it = holder2connectingDev.iterator(); it.hasNext(); ) {
                        devh = it.next();
                        dev = holder2deviceinstance.get(devh);
                        it.remove();
                        if (dev != null && devh.isEnabled()) {
                            lastConnectingDevice = devh;
                            Timber.tag(TAG).d("LastConnectingDevice is now "+devh);
                            dev.connect();
                            return;
                        }
                    }
                }
            }
            holder2connectingDev.clear();
            connectTimerStop();
        }

    }

    private void connectTimerStop() {
        mHandlerThread.mHandler.removeCallbacks(connectTimerTask);
        managerState &= (~STATE_CONNECTING);
        setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE));
        lastConnectingDevice = null;
        Timber.tag(TAG).d("LastConnectingDevice is now null");
        //connRetryStatus = 0;
    }

    private void doExit() {
        Timber.tag(TAG).d("DoExit DeviceManagerService");
        if (sqlite != null)
            sqlite.closeDB();
        GenericDevice dev;
        for (Map.Entry<PDeviceHolder, GenericDevice> entry : holder2deviceinstance.entrySet()) {
            dev = entry.getValue();
            dev.stop();
        }
        mDead = DeadState.DEAD;
        stopForeground(true);
        stopSelf();
        TerminateMessage tms = new TerminateMessage();
        mBinder.postMessage(tms, this);
    }

    private boolean devsReady() {
        GenericDevice dev;
        for (Map.Entry<PDeviceHolder, GenericDevice> entry : holder2deviceinstance.entrySet()) {
            dev = entry.getValue();
            if (!dev.isReady())
                return false;
        }
        return true;
    }

    private void prepareConnection() {
        if (devsReady()) {
            tcpServer.startListening();
            if (!setBluetooth(true)) {
                setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) new ParcelableMessage("exm_errs_adapter")));
            }
        }
    }

    private void connectTimerInit(boolean imme) {
        connectTimerStop();
        if ((managerState & STATE_STOPPING) != 0)
            return;
        PDeviceHolder devh;
        Intent feedback = null;
        int searchFlag = 0;
        if (userObj == null) {
            feedback = new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) new ParcelableMessage("exm_errs_user"));
        } else {
            int i = 0, needsRebind;
            DeviceSearcher devS;
            GenericDevice devI;
            currentDS = null;
            searchDeviceList.clear();

            for (Iterator<PDeviceHolder> it = holder2connectingDev.iterator(); it.hasNext(); ) {
                devh = it.next();
                if (devh.isEnabled()) {
                    try {
                        devI = holder2deviceinstance.get(devh);
                        devS = DeviceTypeMaps.type2search.get(devh.getType());
                        needsRebind = devS.needsRebind(devI);
                        if (needsRebind < 0 || (searchDeviceDone & (1 << i)) > 0)
                            throw new ParcelableMessage("exm_errs_notbound").put(devh);
                        else if (needsRebind != 0) {
                            if (currentDS == null || currentDS == devS) {
                                searchDeviceList.add(devI);
                                searchFlag |= (1 << i);
                                currentDS = devS;
                            }
                        }
                    } catch (ParcelableMessage e) {
                        it.remove();
                        if (feedback == null) {
                            feedback = new Intent(Messages.EXCEPTION_MESSAGE);
                        }
                        feedback.putExtra("except" + i, (Parcelable) e);
                    }
                    i++;
                }
            }
        }
        if (feedback != null)
            setConnectingStatus(feedback);
        else {
            if (searchFlag > 0 && !searchDeviceList.isEmpty()) {
                searchDeviceDone |= searchFlag;
                scanForDevices();
                return;
            } else {
                managerState |= STATE_CONNECTING;
                mHandlerThread.mHandler.postDelayed(connectTimerTask,imme ? 5000 : connRetryDelay * 1000);
            }
        }
        if (searchDeviceDone > 0) {
            searchDeviceDone = 0;
            searchDeviceList.clear();
        }

    }

    private void scanForDevices() {
        Intent msgintent = new Intent(Messages.EXCEPTION_MESSAGE);
        ParcelableMessage exc = new ParcelableMessage("exm_errp_rebindingdev").setType(ParcelableMessage.Type.WARINIG);
        PDeviceHolder devh;
        for (int i = 0; i < searchDeviceList.size(); i++) {
            devh = searchDeviceList.get(i).innerDevice();
            msgintent.putExtra("except" + (i + 1), (Parcelable) new ParcelableMessage("exm_devm").put(devh));
        }
        setConnectingStatus(msgintent.putExtra("except0", (Parcelable) exc));
        managerState |= STATE_REBINDING;
        currentDS.startRebind(ctx, searchDeviceList);
    }


    //testing only
    /*private void updateTimerInit() {
        Timer updateTimer = new Timer();
		updateTimer.schedule(new TimerTask() {
			public void run() {
				deviceManager.simUpdate();
			}
		}, 1000,1000);//sett
		
	}*/

    private class GUIListener implements DeviceListener, DeviceReadyListener {
        private void processStatusMessage(GenericDevice dev, PDeviceHolder devh, ParcelableMessage exc) {
            setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) exc));
        }

        private void resetConnectedState() {
            if ((managerState & STATE_CONNECTED) != 0) {
                GenericDevice d;
                managerState &= (~STATE_CONNECTED);
                for (Map.Entry<PDeviceHolder, GenericDevice> entry : holder2deviceinstance.entrySet()) {
                    d = entry.getValue();
                    if (d.isConnected()) {
                        managerState |= STATE_CONNECTED;
                        break;
                    }
                }
            }
        }

        @Override
        public void onDeviceConnected(final GenericDevice dev, final PDeviceHolder devh) {
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {
                    Timber.tag(TAG).i("Device connected " + devh.getName());
                    managerState |= STATE_CONNECTED;
                    if (setBluetoothAfterDisc >= 0) {
                        dev.disconnect();
                        connectTimerStop();
                    } else {
                        // showProgressDialog(false);
                        holder2connectingDev.remove(devh);
                        connRetryStatus = 0;
                        if (!holder2connectingDev.isEmpty())
                            connectTimerInit(true);
                        else
                            connectTimerStop();
                    }
                    mStatusReceiver.onDeviceConnected(dev, devh);
                    ParcelableMessage exc = new ParcelableMessage("exm_errr_connecteddev")
                            .put(devh).setType(ParcelableMessage.Type.OK);
                    processStatusMessage(dev, devh, exc);
                }
            });
        }

        private void processError(final GenericDevice dev, final PDeviceHolder devh, final ParcelableMessage e) {
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {
                    Timber.tag(TAG).i("Device " + e);
                    if (e != null && e.getId().indexOf("_errs_") >= 0) {
                        connRetryStatus = connRetryNum;
                    }
                    PSessionHolder currentSession = sessionMap.get(devh);
                    if (currentSession != null) {
                        Timber.tag(TAG).d("Resetting session device for "+devh);
                        currentSession.setDevice(new PDeviceHolder());
                    }
                    else
                        Timber.tag(TAG).d("NULL session for "+devh);

                    processStatusMessage(dev, devh, e);
                    Timber.tag(TAG).d("LastConnectingDevice "+lastConnectingDevice+"; disconnected dev is "+devh+". Equals?= "+(lastConnectingDevice!=null && lastConnectingDevice.equals(devh)));
                    holder2connectingDev.add((lastConnectingDevice!=null && lastConnectingDevice.equals(devh))?0:holder2connectingDev.size(), devh);
                    if ((managerState & (STATE_STOPPING|STATE_EXITING)) == 0 && (lastConnectingDevice==null || lastConnectingDevice.equals(devh))) {
                        reset();
                        connectTimerInit(connRetryStatus >= connRetryNum);
                    }
                }
            });
        }

        @Override
        public void onDeviceConnectionFailed(final GenericDevice dev, final PDeviceHolder devh) {
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {
                    Timber.tag(TAG).d("1 bluetoothafter " + setBluetoothAfterDisc);
                    resetConnectedState();
                    if (setBluetoothAfterDisc==0) {

                    }
                    else if (setBluetoothAfterDisc > 0) {
                        synchronized (DeviceManagerService.this) {
                            setBluetoothAfterDisc--;
                        }
                        if (setBluetoothAfterDisc == 0)
                            setBluetooth(false);
                    } else {
                        mStatusReceiver.onDeviceConnectionFailed(dev, devh);
                        processError(dev, devh,
                                new ParcelableMessage("exm_errr_connectionfailed").put(devh));
                    }
                }
            });
        }

        @Override
        public void onDeviceError(final GenericDevice dev, final PDeviceHolder devh, final ParcelableMessage e) {
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {
                    Timber.tag(TAG).d("2 bluetoothafter " + setBluetoothAfterDisc);
                    resetConnectedState();
                    if (setBluetoothAfterDisc==0) {

                    }
                    else if (setBluetoothAfterDisc > 0) {
                        synchronized (DeviceManagerService.this) {
                            setBluetoothAfterDisc--;
                        }
                        if (setBluetoothAfterDisc == 0)
                            setBluetooth(false);
                    } else {
                        mStatusReceiver.onDeviceConnectionFailed(dev, devh);
                        processError(dev, devh, e);
                    }
                }
            });
        }

        @Override
        public void onDeviceDisconnected(final GenericDevice dev, final PDeviceHolder devh) {
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {
                    Timber.tag(TAG).d("3 bluetoothafter " + setBluetoothAfterDisc);
                    resetConnectedState();
                    if (setBluetoothAfterDisc==0) {

                    }
                    else if (setBluetoothAfterDisc > 0) {
                        synchronized (DeviceManagerService.this) {
                            setBluetoothAfterDisc--;
                        }
                        if (setBluetoothAfterDisc == 0)
                            setBluetooth(false);
                    } else {
                        mStatusReceiver.onDeviceDisconnected(dev, devh);
                        processError(dev, devh,
                                new ParcelableMessage("exm_errr_connectionlost").put(devh));
                    }
                }
            });
        }

        @Override
        public void onDeviceUpdate(final GenericDevice dev, final PDeviceHolder devh, final DeviceUpdate f0) {
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {

                    DeviceStatus currentStatus = dev.getDeviceState();
                    Timber.tag(TAG).d("["+currentStatus+"] " + f0);
                    if (currentStatus != DeviceStatus.PAUSED &&
                            currentStatus != DeviceStatus.DPAUSE) {
                        int un = f0.getUpdateN();
                        PSessionHolder currentSession;
                        if (un == 1) {
                            long now = System.currentTimeMillis();
                            currentSession = new PSessionHolder(-1, mainSessionId, devh, now, userObj, dev.getSessionSettings());
                            sqlite.newValue(currentSession);
                            sessionMap.put(devh, currentSession);
                            if (mainSessionId < 0)
                                mainSessionId = currentSession.getId();
                            Timber.tag(TAG).d("Creating session for "+devh);
                            mStatusReceiver.onDeviceSession(dev, devh, currentSession);
                        }
                        if (un >= 1) {
                            currentSession = sessionMap.get(devh);
                            //Timber.d("Session for "+devh+" IS "+currentSession);
                            if (currentSession != null) {
                                if (!currentSession.getDevice().equals(devh)) {
                                    currentSession.setDevice(devh);
                                    Timber.tag(TAG).d("Resuming session for "+devh);
                                    mStatusReceiver.onDeviceSession(dev, devh, currentSession);
                                }
                                f0.setSessionId(currentSession.getId());
                                try {
                                    sqlite.newValue((UpdateDatabasable) f0);
                                } catch (Exception e) {
                                    CA.logException(e);
                                }
                            } else
                                return;
                        }
                        mStatusReceiver.onDeviceUpdate(dev, devh, f0);
                    }
                }
            });
        }

        @Override
        public void onUserSet(final GenericDevice dev, final PDeviceHolder devh, final PUserHolder u) {
            // TODO Auto-generated method stub
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {
                    mStatusReceiver.onUserSet(dev, devh, u);
                }
            });
        }

        @Override
        public void onDeviceDescription(final GenericDevice dev, final PDeviceHolder devh,
                                        final String desc) {
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {
                    sqlite.newValue(devh);
                    mStatusReceiver.onDeviceDescription(dev, devh, desc);
                }
            });
        }

        @Override
        public void onDeviceStatusChange(final GenericDevice dev, final PDeviceHolder devh,
                                         final PHolderSetter status) {
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {
                    mStatusReceiver.onDeviceStatusChange(dev, devh, status);
                }
            });
        }

        @Override
        public void onDeviceConnecting(final GenericDevice dev,
                                       final PDeviceHolder devh) {
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {
                    int ncon = nConnectedDevices();
                    mStatusReceiver.onDeviceConnecting(dev, devh);
                    ParcelableMessage devpe = new ParcelableMessage("exm_devm").put(devh);
                    ParcelableMessage connexc = new ParcelableMessage(ncon == 0 ? "exm_errp_connectingdev" : "exm_errr_connectingdev")
                            .put(ncon).put(connRetryStatus).setType(ParcelableMessage.Type.OK);
                    setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE)
                            .putExtra("except0", (Parcelable) connexc).putExtra("except1", (Parcelable) devpe));
                }
            });
        }

        @Override
        public void onDeviceReady(final GenericDevice inst, PDeviceHolder h) {
            mHandlerThread.runOnMe(new Runnable() {
                @Override
                public void run() {
                    inst.addDeviceListener(loglist);
            /*if (userObj != null)
                inst.pushUserChange(userObj);*/
                    prepareConnection();
                }
            });
        }
    }

	/*@Override
	public void onDetach() {
		super.onDetach();
		lbm = null;
	}*/

    @Override
    public void onDestroy() {
        Timber.tag(TAG).d("Destroying DeviceManagerService: dead="+mDead);
        mHandlerThread.waitFinish(new Runnable() {

            @Override
            public void run() {
                stopOperations();
                setupBluetoothIO(false, null);
                setupIO(false);
                if (sqlite!=null)
                    sqlite.closeDB();
            }
        });
        if (mDead!=DeadState.DEAD)
            mDead = DeadState.NOTSTARTED;
        super.onDestroy();
    }

    private void setupIO(boolean start) {
        if (start) {
            IntentFilter intentf = new IntentFilter();
            intentf.addAction(Messages.CMDGETCONSTATUS_MESSAGE);
            intentf.addAction(DeviceSearcher.DEVICE_SEARCH_ERROR);
            intentf.addAction(DeviceSearcher.DEVICE_REBIND_ERROR);
            intentf.addAction(DeviceSearcher.DEVICE_REBIND_OK);
            intentf.addAction(DeviceSearcher.DEVICE_SEARCH_END);
            //intentf.addAction(Messages.CMDPLTPROCESSOR_MESSAGE);
            CA.lbm.registerReceiver(commandReceiver, intentf);
        } else {
            try {
                CA.lbm.unregisterReceiver(commandReceiver);
            } catch (Exception e) {
            }
        }
    }

    private void setupBluetoothIO(boolean start, IntentFilter filt) {
        if (start) {
            ctx.registerReceiver(bluetoothReceiver, filt);
        } else {
            try {
                ctx.unregisterReceiver(bluetoothReceiver);
            } catch (Exception e) {
            }
        }
    }

    private boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean rv = bluetoothAdapter != null;
        if (rv) {
            ParcelableMessage exc = null;
            boolean isEnabled = bluetoothAdapter.isEnabled();

            if (enable && !isEnabled) {
                needBluetoothEnable = 1;
                setupBluetoothIO(true, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
                rv = bluetoothAdapter.enable();
                if (rv)
                    exc = new ParcelableMessage("exm_errp_activatingbluetooth");
            } else if (enable) {
                if (needBluetoothEnable == -1)
                    needBluetoothEnable = 0;
                if ((managerState & STATE_SEARCHING) != 0)
                    performDeviceSearch();
                else
                    connectTimerInit(true);
            } else if (!enable && isEnabled && needBluetoothEnable == 1) {
                setupBluetoothIO(true, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
                rv = bluetoothAdapter.disable();
                if (rv)
                    exc = new ParcelableMessage("exm_errp_disablingbluetooth");
            } else if (!enable) {
                if ((managerState & STATE_EXITING) != 0)
                    doExit();
                else {
                    managerState &= (~STATE_STOPPING);
                    setBluetoothAfterDisc = -1;
                }
            }
            if (exc != null)
                setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) exc.setType(ParcelableMessage.Type.OK)));
        }
        if (!rv) {
            setupBluetoothIO(false, null);
            if (needBluetoothEnable == -1)
                needBluetoothEnable = 0;
            if ((managerState & STATE_EXITING) != 0)
                doExit();
            else {
                managerState &= (~STATE_STOPPING);
                setBluetoothAfterDisc = -1;
            }
        }
        return rv;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.tag(TAG).d("DeviceManagerSerivice onCreate");
        Intent notificationIntent = new Intent(this, DeviceManagerService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_manager).setContentTitle(DeviceManagerService.class.getSimpleName())
                .setContentText(DeviceManagerService.class.getSimpleName())
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis()).build();
        this.startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        if (mDead==DeadState.NOTSTARTED) {
            Timber.tag(TAG).d("DeviceManagerSerivice created");
            mDead = DeadState.STARTED;
            String startConf = intent == null ? "" : intent.getStringExtra(EXTRA_CONFIGURATION_NAME);
            mDebugFlag = intent == null ? 0 : intent.getIntExtra(EXTRA_DEBUG_FLAG,0);
            ctx = getApplicationContext();
            mStatusReceiver = new StatusReceiver();
            mBinder = new DeviceManagerBinder(ctx, mStatusReceiver);
            tcpServer = new TCPServer(DeviceManagerService.this, mStatusReceiver, mBinder);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
            mBinder.addCommandProcessor(this,
                    ConnectMessage.class,
                    BluetoothRefreshMesssage.class,
                    DeviceChangedMessage.class,
                    UserSetMessage.class,
                    ConfChangeMessage.class,
                    DeviceChangeRequestMessage.class,
                    ExitMessage.class,
                    DisconnectMessage.class
            );
            init(startConf);
        }
        return START_STICKY;
    }

    private void init(final String startConf) {
        mHandlerThread = new HandlerThread();
        mHandlerThread.init();
        mHandlerThread.runOnMe(new Runnable() {
            @Override
            public void run() {
                if (startConf!=null && !startConf.isEmpty() && reloadDBConf()!=null) {
                    PConfHolder cnf = new PConfHolder();
                    if (sqlite.getValue(cnf,cnf.getNameCondition(startConf)))
                        newConfData(cnf);
                }

                //updateTimerInit();
                setupIO(true);
                reset();
            }
        });
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}
