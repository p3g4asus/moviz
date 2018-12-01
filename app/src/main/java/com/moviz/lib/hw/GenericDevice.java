package com.moviz.lib.hw;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.moviz.lib.comunication.DeviceStatus;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.comunication.plus.message.DeviceChangedMessage;
import com.moviz.lib.comunication.plus.message.ProcessedOKMessage;
import com.moviz.lib.comunication.plus.message.UserSetMessage;
import com.moviz.lib.hw.DeviceService.BaseBinder;
import com.moviz.lib.utils.CommandManager;
import com.moviz.lib.utils.CommandProcessor;

public abstract class GenericDevice implements CommandProcessor {

    protected PDeviceHolder device = null;
    protected Context ctx;
    protected PUserHolder currentUser = null;
    protected DeviceBinder mBluetoothService;
    protected CommandManager commandManager;
    protected DeviceReadyListener mDeviceReadyListener = null;
    protected final String TAG = getClass().getSimpleName();

    public void sendMessage(BaseMessage bm) {
        commandManager.postMessage(bm,this);
    }

    public void addNotification() {
        Intent notificationIntent = new Intent(ctx, DeviceService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0,
                notificationIntent, 0);
        Notification notification = new Notification.Builder(ctx)
                .setSmallIcon(getIcon()).setContentTitle(getNotificationTitle())
                .setContentText(getNotificationText())
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis()).build();
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) device.getId(), notification);
    }

    public void removeNotification() {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel((int) device.getId());
    }

    private void pushUserChange(PUserHolder newus) {
        currentUser = newus;
        if (mBluetoothService != null)
            mBluetoothService.setUser(this, newus);
    }

    private void pushSettingsChange() {
        if (mBluetoothService != null)
            mBluetoothService.pushSettingsChange(this);
    }


    public void setDeviceReadyListener(DeviceReadyListener drl) {
        mDeviceReadyListener = drl;
    }

    protected abstract void prepareServiceConnection();

    protected abstract Class<? extends DeviceDataProcessor> getDataProcessorClass();
    protected abstract Class<? extends DeviceSimulator> getSimulatorClass();

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            BaseBinder bb = (BaseBinder) service;
            mBluetoothService = bb.getBinderForDevice(GenericDevice.this);
            mBluetoothService.setDataProcessorClass(getDataProcessorClass());
            mBluetoothService.setSimulatorClass(getSimulatorClass());
            if (mDeviceReadyListener != null) {
                mDeviceReadyListener.onDeviceReady(GenericDevice.this, device);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            /*if (mChars!=null) {
				for (BluetoothGattCharacteristic gattCharacteristic : mChars)
					mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, false);
			}*/
            //intDisconnect();
        }
    };


    public boolean initOnce(PDeviceHolder dev, PUserHolder usr, Context c, CommandManager cmdp) {
        device = dev;
        currentUser = usr;
        ctx = c;
        commandManager = cmdp;
        commandManager.addCommandProcessor(this, getAcceptedMessages());
        intStartService();
        return true;
    }

    protected Class<? extends BaseMessage>[] getAcceptedMessages() {
        return new Class[] {UserSetMessage.class, DeviceChangedMessage.class};
    }

    public GenericDevice() {
    }


    public void connect() {
        intConnect();
    }

    protected void intStartService() {
        Intent gattServiceIntent = new Intent(ctx, getServiceClass());
        ctx.startService(gattServiceIntent);
        ctx.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void intConnect() {
        prepareServiceConnection();
        mBluetoothService.connect(this, currentUser);
    }

    public void disconnect() {
        intDisconnect();
    }

    public String getAddress() {
        if (device != null)
            return device.getAddress();
        else
            return null;
    }

    protected void intDisconnect() {
        if (mBluetoothService != null)
            mBluetoothService.askDisconnect(this);
    }

    public void stop() {
        if (mBluetoothService != null) {
            mBluetoothService.stop();
            mBluetoothService = null;
            ctx.unbindService(mServiceConnection);
        }
        commandManager.removeCommandProcessor(this,getAcceptedMessages());
    }

    protected abstract int getIcon();

    protected abstract String getNotificationTitle();

    protected abstract String getNotificationText();


    protected Class<? extends DeviceService> getServiceClass() {
        return DeviceService.class;
    }

    @Override
    public BaseMessage processCommand(BaseMessage hs2) {
        if (hs2 instanceof UserSetMessage) {
            pushUserChange(((UserSetMessage) hs2).getUser());
            return new ProcessedOKMessage();
        }
        if (hs2 instanceof DeviceChangedMessage) {
            DeviceChangedMessage dcm = (DeviceChangedMessage) hs2;
            if (dcm.getWhy()== DeviceChangedMessage.Reason.BECAUSE_DEVICE_CONF_CHANGED) {
                PDeviceHolder devh = dcm.getDev();
                if (devh.equals(device)) {
                    device.setAdditionalSettings(devh.getAdditionalSettings());
                    pushSettingsChange();
                }
            }
            return null;
        }
        else if (mBluetoothService != null)
            return mBluetoothService.processCommand(this, hs2);
        else
            return null;
    }

    public PDeviceHolder innerDevice() {
        return device;
    }

    public void addDeviceListener(DeviceListener dcl) {
        if (mBluetoothService != null)
            mBluetoothService.addDeviceListener(dcl);
    }

    public BluetoothState getBluetoothState() {
        if (mBluetoothService != null)
            return mBluetoothService.getBluetoothState(this);
        else
            return null;
    }

    public boolean isConnected() {
        return getBluetoothState() == BluetoothState.CONNECTED;
    }

    public DeviceStatus getDeviceState() {
        if (mBluetoothService != null)
            return mBluetoothService.getDeviceState(this);
        else
            return null;
    }

    public String getSessionSettings() {
        if (mBluetoothService != null)
            return mBluetoothService.getSessionSettings(this);
        else
            return null;
    }

    public boolean isReady() {
        return mBluetoothService != null;
    }

    public void loadTransientPref(SharedPreferences sharedPreferences) {

    }
}
