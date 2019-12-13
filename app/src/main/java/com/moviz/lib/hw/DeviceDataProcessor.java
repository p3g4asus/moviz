package com.moviz.lib.hw;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.moviz.gui.fragments.SettingsFragment;
import com.moviz.lib.comunication.DeviceStatus;
import com.moviz.lib.comunication.EncDec;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.utils.CommandProcessor;
import com.moviz.lib.utils.ParcelableMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import timber.log.Timber;

public abstract class DeviceDataProcessor implements DeviceConnectionListener, CommandProcessor {
    protected PHolderSetter statusVars = new PHolderSetter();
    protected Context ctx;
    protected BluetoothDevice mBluetoothDevice;
    protected GenericDevice mDeviceHolder;
    protected String mAddress;
    protected String mDeviceName;
    protected BluetoothState mBluetoothState = BluetoothState.IDLE;
    protected DeviceStatus mDeviceState = DeviceStatus.OFFLINE;
    protected PUserHolder mCurrentUser = null;
    protected DeviceSimulator mSim = null;
    private String mDeviceDescription = getClass().getName();
    private DeviceUpdate lastUpdate = null;
    protected Vector<DeviceListener> mList;
    protected static final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    protected final String TAG = getClass().getSimpleName();
    protected int mDebugFlag = 0;
    protected FileOutputStream mDebugFos = null;
    public static int DF_ACTIVE = 1;

    //public abstract DeviceDataProcessor newInstance();

    public void pushSettingsChange() {

    }

    protected String makeDeviceDescription(Map<String, String> infoMap, String prefix) {
        String rv = "";
        Set<String> keys = infoMap.keySet();
        for (String key : keys) {
            String tmp = infoMap.get(key);
            if (!tmp.isEmpty()) {
                if (!rv.isEmpty())
                    rv += "/";
                rv += tmp;
            }
        }

        return rv.isEmpty() ? prefix : rv + " " + prefix;
    }

    public void setParams(GenericDevice device, Vector<DeviceListener> lst, DeviceSimulator sim, Context c) {
        mBluetoothDevice = mAdapter
                .getRemoteDevice(device.getAddress());
        mList = lst;
        ctx = c;
        mDeviceHolder = device;
        mSim = sim;
        mDeviceName = device.device.getName();
        mAddress = device.getAddress();
        pushSettingsChange();

        initStatusVars(statusVars);
    }

    public String getDeviceAddress() {
        return mAddress;
    }

    protected void initStatusVars(PHolderSetter statusVar) {
        statusVars.add(new PHolder(DeviceStatus.OFFLINE.toString(), "status.devicestatus",null));
    }

    public DeviceDataProcessor() {

    }

    public void setDeviceDescription(Map<String, String> infoMap, String prefix) {
        setDeviceDescription(makeDeviceDescription(infoMap, prefix));
    }

    public PHolderSetter getStatusVars() {
        return statusVars;
    }

    public void setDeviceName(String name) {
        this.mDeviceName = name;
    }

    public void setUser(PUserHolder u) {
        mCurrentUser = u;
    }

    public String getDeviceDescription() {
        return mDeviceDescription;
    }

    public void setDeviceDescription(String d) {
        if (!d.equals(mDeviceDescription)) {
            mDeviceDescription = d;
            postDeviceDescription(mDeviceDescription);
        }
    }

    public boolean isConnected() {
        return mBluetoothState == BluetoothState.CONNECTED;
    }

    public synchronized void setBluetoothState(BluetoothState newState) {
        if (newState != mBluetoothState) {
            Timber.tag(TAG).i("New state for device "+mBluetoothDevice.getName()+": "+newState);
            this.mBluetoothState = newState;
            if (newState == BluetoothState.CONNECTED) {
                onDeviceConnected(mDeviceHolder, mDeviceHolder.innerDevice());
                for (DeviceListener dl : mList)
                    dl.onDeviceConnected(mDeviceHolder, mDeviceHolder.innerDevice());
            } else if (newState == BluetoothState.CONNECTING) {
                onDeviceConnecting(mDeviceHolder, mDeviceHolder.innerDevice());
                for (DeviceListener dl : mList)
                    dl.onDeviceConnecting(mDeviceHolder, mDeviceHolder.innerDevice());
            }
            ctx.sendBroadcast(new Intent(DeviceService.ACTION_BLUETOOTH_STATE_CHANGED).putExtra(DeviceService.EXTRA_NEWSTATE, newState).putExtra(DeviceService.EXTRA_DEVICE, mDeviceHolder.innerDevice()));
        }
    }

    public DeviceStatus getDeviceState() {
        return mDeviceState;
    }

    public synchronized void setDeviceState(DeviceStatus newState) {
        if (newState != mDeviceState) {
            DeviceStatus oldState = mDeviceState;
            this.mDeviceState = newState;
            setStatusVar(".devicestatus", newState.name());
            //ctx.sendBroadcast(new Intent(DeviceService.ACTION_DEVICE_STATE_CHANGED).putExtra(DeviceService.EXTRA_NEWSTATE, newState).putExtra(DeviceService.EXTRA_DEVICE, mDeviceHolder));
            //statusVars.get(0).sO(mDeviceState.name());
            if (newState == DeviceStatus.RUNNING && oldState != newState) {
                if (oldState == DeviceStatus.STANDBY)
                    onDeviceStarted(mDeviceHolder, mDeviceHolder.innerDevice());
                else
                    onDeviceResumed(mDeviceHolder, mDeviceHolder.innerDevice());
            } else if ((newState == DeviceStatus.DPAUSE || newState == DeviceStatus.PAUSED) && newState != oldState)
                onDevicePaused(mDeviceHolder, mDeviceHolder.innerDevice());
            else if (newState == DeviceStatus.STANDBY && oldState != DeviceStatus.OFFLINE)
                onDeviceStopped(mDeviceHolder, mDeviceHolder.innerDevice());
        }

    }

    public void postDeviceUpdate(DeviceUpdate d) {
        lastUpdate = d;
        ctx.sendBroadcast(new Intent(DeviceService.ACTION_DATA_AVAILABLE).putExtra(DeviceService.EXTRA_DEVICE_UPDATE, (Parcelable) d));
        onDeviceUpdate(mDeviceHolder, mDeviceHolder.innerDevice(), d);
        for (DeviceListener dl : mList)
            dl.onDeviceUpdate(mDeviceHolder, mDeviceHolder.innerDevice(), d);
    }

    public void postUser(PUserHolder u) {
        ctx.sendBroadcast(new Intent(DeviceService.ACTION_USER_SET).putExtra(DeviceService.EXTRA_NEWUSER, u).putExtra(DeviceService.EXTRA_DEVICE, mDeviceHolder.innerDevice()));
        onUserSet(mDeviceHolder, mDeviceHolder.innerDevice(), u);
        for (DeviceListener dl : mList)
            dl.onUserSet(mDeviceHolder, mDeviceHolder.innerDevice(), u);
    }

    public void postStatusVarChange(PHolderSetter s) {
        ctx.sendBroadcast(new Intent(DeviceService.ACTION_STATUS_VAR_CHANGED).putExtra(DeviceService.EXTRA_NEWSTATE, s).putExtra(DeviceService.EXTRA_DEVICE, mDeviceHolder.innerDevice()));
        onDeviceStatusChange(mDeviceHolder, mDeviceHolder.innerDevice(), statusVars);
        for (DeviceListener dl : mList)
            dl.onDeviceStatusChange(mDeviceHolder, mDeviceHolder.innerDevice(), statusVars);
    }

    public void postDeviceDescription(String desc) {
        ctx.sendBroadcast(new Intent(DeviceService.ACTION_DESCRIPTION_CHANGED).putExtra(DeviceService.EXTRA_DESCRIPTION, desc).putExtra(DeviceService.EXTRA_DEVICE, mDeviceHolder.innerDevice()));
        onDeviceDescription(mDeviceHolder, mDeviceHolder.innerDevice(), desc);
        for (DeviceListener dl : mList)
            dl.onDeviceDescription(mDeviceHolder, mDeviceHolder.innerDevice(), desc);
    }

    public void postDeviceError(ParcelableMessage e) {
        ctx.sendBroadcast(new Intent(DeviceService.ACTION_DEVICE_ERROR).putExtra(DeviceService.EXTRA_DEVICE_ERROR, (Parcelable) e).putExtra(DeviceService.EXTRA_DEVICE, mDeviceHolder.innerDevice()));
        String id = e.getId();
        Timber.tag("DeviceDataProcessor").e("Error " + id);
        if (id.equals("exm_errr_connectionfailed")) {
            Timber.tag("DeviceDataProcessor").e(mBluetoothDevice + " disconnected");
            onDeviceConnectionFailed(mDeviceHolder, mDeviceHolder.innerDevice());
            for (DeviceListener dl : mList)
                dl.onDeviceConnectionFailed(mDeviceHolder, mDeviceHolder.innerDevice());
        } else if (id.equals("exm_errr_connectionlost")) {
            onDeviceDisconnected(mDeviceHolder, mDeviceHolder.innerDevice());
            for (DeviceListener dl : mList)
                dl.onDeviceDisconnected(mDeviceHolder, mDeviceHolder.innerDevice());
            // mButtonStop.setVisibility(View.VISIBLE);
        } else {
            if (mBluetoothState != BluetoothState.CONNECTED)
                onDeviceConnectionFailed(mDeviceHolder, mDeviceHolder.innerDevice());
            else
                onDeviceDisconnected(mDeviceHolder, mDeviceHolder.innerDevice());
            onDeviceError(mDeviceHolder, mDeviceHolder.innerDevice(), e);
            for (DeviceListener dl : mList)
                dl.onDeviceError(mDeviceHolder, mDeviceHolder.innerDevice(), e);
        }
    }

    public void setIsDebugging(int debugFlag) {
        mDebugFlag = debugFlag;
    }

    public int isDebugging() {
        return mDebugFlag;
    }

    protected void debugFileClose() {
        if (mDebugFos!=null) {
            try {
                mDebugFos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mDebugFos = null;
        }
    }

    protected FileOutputStream debugFileOpen(PDeviceHolder devh) {
        debugFileClose();
        if ((mDebugFlag&DF_ACTIVE)!=0) {
            String fname = SettingsFragment.getDefaultAppDir(ctx) + "/debug";
            File f = new File(fname);
            if (!f.exists())
                f.mkdirs();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
            fname += "/" + sdf.format(new Date()) + "_" + devh.getAlias() + "_" + devh.getId() + ".bin";
            try {
                mDebugFos = new FileOutputStream(fname);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mDebugFos;
    }

    protected byte[] debugFileElementHeader(byte[] pld, int pldlen) {
        return null;
    }

    protected byte[] debugFileElementFooter(byte[] header,byte[] pld, int pldlen) {
        return null;
    }

    protected void debugFileAppendElement(byte[] arr, int len) {
        if (mDebugFos!=null) {
            try {
                byte[] arr2 = debugFileElementHeader(arr,len);
                if (arr2!=null)
                    mDebugFos.write(arr2);
                mDebugFos.write(arr,0,len);
                byte[] arr3 = debugFileElementFooter(arr2,arr,len);
                if (arr3!=null)
                    mDebugFos.write(arr3);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void postReadData(BluetoothGattCharacteristic characteristic) {
        onReadData(mDeviceHolder, mDeviceHolder.innerDevice(), characteristic);
    }

    public void postReadData(byte[] buffer, int size) {
        onReadData(mDeviceHolder, mDeviceHolder.innerDevice(), buffer, size);
    }

    @Override
    public void onDeviceConnectionFailed(GenericDevice dev, PDeviceHolder devh) {
        setBluetoothState(BluetoothState.IDLE);
        setDeviceState(DeviceStatus.OFFLINE);
    }

    @Override
    public void onDeviceDisconnected(GenericDevice dev, PDeviceHolder devh) {
        setBluetoothState(BluetoothState.IDLE);
        setDeviceState(DeviceStatus.OFFLINE);
        debugFileClose();
    }

    @Override
    public void onDeviceConnected(GenericDevice dev, PDeviceHolder devh) {
        setDeviceName(mBluetoothDevice.getName());
        setDeviceState(DeviceStatus.STANDBY);
        debugFileOpen(devh);
    }

    @Override
    public void onDeviceStopped(GenericDevice dev, PDeviceHolder devh) {

    }

    @Override
    public void onDevicePaused(GenericDevice dev, PDeviceHolder devh) {
    }

    @Override
    public void onDeviceResumed(GenericDevice dev, PDeviceHolder devh) {
    }

    @Override
    public void onDeviceStarted(GenericDevice dev, PDeviceHolder devh) {
    }

    @Override
    public void onDeviceError(GenericDevice dev, PDeviceHolder devh,ParcelableMessage e) {

    }

    @Override
    public void onDeviceUpdate(GenericDevice dev, PDeviceHolder devh,
                               DeviceUpdate paramFitnessHwApiDeviceFeedback) {
    }

    @Override
    public void onUserSet(GenericDevice dev, PDeviceHolder devh, PUserHolder us) {
    }

    @Override
    public void onDeviceDescription(GenericDevice dev, PDeviceHolder devh,
                                    String desc) {
        devh.setDescription(desc);
    }

    @Override
    public void onDeviceStatusChange(GenericDevice dev, PDeviceHolder devh,
                                     PHolderSetter status){
    }

    @Override
    public void onDeviceConnecting(GenericDevice mDeviceHolder,
                                   PDeviceHolder innerDevice) {
    }

    @Override
    public boolean onReadData(GenericDevice dev, PDeviceHolder devh, byte[] arr, int length) {
        debugFileAppendElement(arr,length);
        return false;
    }

    @Override
    public boolean onReadData(GenericDevice dev, PDeviceHolder devh, BluetoothGattCharacteristic bcc) {
        return false;
    }

    @Override
    public void onDataWrite(GenericDevice dev, PDeviceHolder devh, byte[] arr, int length) {
    }

    public void setStatusVar(String id, String s) {
        Holder h = statusVars.getP(id);
        if (h!=null) {
            h.sO(s);
            postStatusVarChange(statusVars);
        }
    }

    public void setStatusVar(String id, byte s) {
        Holder h = statusVars.getP(id);
        if (h!=null) {
            h.sO(s);
            postStatusVarChange(statusVars);
        }
    }

    public void setStatusVar(String id, short s) {
        Holder h = statusVars.getP(id);
        if (h!=null) {
            h.sO(s);
            postStatusVarChange(statusVars);
        }
    }

    public void setStatusVar(String id, int s) {
        Holder h = statusVars.getP(id);
        if (h!=null) {
            h.sO(s);
            postStatusVarChange(statusVars);
        }
    }

    public void setStatusVar(String id, long s) {
        Holder h = statusVars.getP(id);
        if (h!=null) {
            h.sO(s);
            postStatusVarChange(statusVars);
        }
    }

    public void setStatusVar(String id, double s) {
        Holder h = statusVars.getP(id);
        if (h!=null) {
            h.sO(s);
            postStatusVarChange(statusVars);
        }
    }

    public void setStatusVar(String id, EncDec s) {
        Holder h = statusVars.getP(id);
        if (h!=null) {
            h.sO(s);
            postStatusVarChange(statusVars);
        }
    }

    public void setStatusVar(String id, List<?> s) {
        Holder h = statusVars.getP(id);
        if (h!=null) {
            h.sO(s);
            postStatusVarChange(statusVars);
        }
    }

    public DeviceUpdate getLastUpdate() {
        return lastUpdate;
    }

    public String getSessionSettings() {
        return null;
    }

    /*public void addDeviceListener(DeviceListener dcl) {
        if (!mList.contains(dcl))
            mList.add(dcl);
    }
    public void removeDeviceListener(DeviceListener dcl) {
        mList.remove(dcl);
    }*/
    public BluetoothState getBluetoothState() {
        return mBluetoothState;
    }

    public GenericDevice getDevice() {
        return mDeviceHolder;
    }
}
