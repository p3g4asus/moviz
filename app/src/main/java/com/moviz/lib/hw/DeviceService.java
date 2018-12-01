package com.moviz.lib.hw;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.moviz.gui.R;
import com.moviz.gui.app.CA;
import com.moviz.lib.comunication.message.ProtocolMessage;
import com.moviz.lib.utils.DeviceTypeMaps;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import no.nordicsemi.android.log.Logger;

public class DeviceService extends Service {
    private int lastIcon = R.drawable.ic_stat_manager;
    protected BaseBinder mBinder = null;
    private String notificationText = getClass().getSimpleName() + " active";
    private String notificationTitle = getClass().getSimpleName() + " logging";

    private final static int NOTIFICATION_ID = 999999;

    public final static String ACTION_BLUETOOTH_STATE_CHANGED = "DeviceService.ACTION_STATE_CHANGED";
    public final static String ACTION_DATA_AVAILABLE = "DeviceService.ACTION_DATA_AVAILABLE";
    public static final String ACTION_DEVICE_STATE_CHANGED = "DeviceService.ACTION_DEVICE_STATE_CHANGED";
    public static final String ACTION_USER_SET = "DeviceService.ACTION_USER_SET";
    public static final String ACTION_STATUS_VAR_CHANGED = "DeviceService.ACTION_STATUS_VAR_CHANGED";
    public static final String ACTION_DESCRIPTION_CHANGED = "DeviceService.ACTION_DESCRIPTION_CHANGED";
    public static final String ACTION_DEVICE_ERROR = "DeviceService.ACTION_DEVICE_ERROR";


    public final static String EXTRA_NEWSTATE = "DeviceService.EXTRA_NEWSTATE";
    public final static String EXTRA_SERVICE_ICON = "DeviceService.EXTRA_SERVICE_ICON";
    public static final String EXTRA_NOTIFICATION_TITLE = "DeviceService.EXTRA_NOTIFICATION_TITLE";
    public static final String EXTRA_NOTIFICATION_TEXT = "DeviceService.EXTRA_NOTIFICATION_TEXT";
    public static final String EXTRA_DEVICE_UPDATE = "DeviceService.EXTRA_DEVICE_UPDATE";
    public static final String EXTRA_NEWUSER = "DeviceService.EXTRA_NEWUSER";
    public final static String EXTRA_DEVICE = "DeviceService.EXTRA_DEVICE";
    public static final String EXTRA_DESCRIPTION = "DeviceService.EXTRA_DESCRIPTION";
    public static final String EXTRA_DEVICE_ERROR = "DeviceService.EXTRA_DEVICE_ERROR";

    protected ConcurrentHashMap<String, DeviceDataProcessor> mDevices = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, DeviceSimulator> mSimulators = new ConcurrentHashMap<>();

    public class BaseBinder extends Binder {
        public BaseBinder() {
        }

        public DeviceBinder getBinderForDevice(GenericDevice dev) {
            DeviceBinder bnd = DeviceTypeMaps.type2binder.get(dev.innerDevice().getType());
            bnd.setDeviceMaps(mDevices, mSimulators);
            bnd.setService(this);
            return bnd;
        }

        public void stop() {
            Logger.d(CA.mLogSession,"DeviceService Stop");
            try {
                removeNotification();
                stopSelf();
            } catch (Exception e) {

            }
        }

        public Context getContext() {
            return getApplicationContext();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) {
            mBinder = new BaseBinder();
        }
        return mBinder;
    }

    public void processCommand(ProtocolMessage c) {
        if (!(c instanceof com.moviz.lib.comunication.message.ConnectMessage)) {
            for (Map.Entry<String, DeviceDataProcessor> entry : mDevices.entrySet()) {
                entry.getValue().processCommand(c);
            }
        }
    }

    @Override
    public void onDestroy() {
        Logger.d(CA.mLogSession,"DeviceService Destroyed");
        if (mBinder != null) {
            for (Map.Entry<String, DeviceDataProcessor> entry : mDevices.entrySet()) {
                mBinder.getBinderForDevice(entry.getValue().mDeviceHolder).stop();
            }
            mDevices.clear();
            mBinder.stop();
        }
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(CA.mLogSession,"DeviceService Created");
        Intent notificationIntent = new Intent(this, DeviceService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(lastIcon).setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis()).build();
        this.startForeground(NOTIFICATION_ID, notification);
    }

    protected void removeNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

}
