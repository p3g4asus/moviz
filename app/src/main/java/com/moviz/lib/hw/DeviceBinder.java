package com.moviz.lib.hw;

import android.os.Binder;

import com.moviz.lib.comunication.DeviceStatus;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.utils.ParcelableMessage;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

public abstract class DeviceBinder extends Binder implements DeviceListener {
    protected ConcurrentHashMap<String, DeviceDataProcessor> mDevices = null;
    protected ConcurrentHashMap<String, DeviceSimulator> mSimulators = null;
    protected DeviceService.BaseBinder mBase;
    protected boolean mNeedToStop = false;
    protected Vector<DeviceListener> mList = new Vector<DeviceListener>();
    protected final String TAG = getClass().getSimpleName();
    protected Class<? extends DeviceSimulator> mSimClass;
    protected Class<? extends DeviceDataProcessor> mDpClass;

    public ConcurrentHashMap<String, DeviceDataProcessor> getDeviceMap() {
        return mDevices;
    }

    public void setDeviceMaps(ConcurrentHashMap<String, DeviceDataProcessor> mp, ConcurrentHashMap<String, DeviceSimulator> mSims) {
        mDevices = mp;
        mSimulators = mSims;
    }

    public void setService(DeviceService.BaseBinder s) {
        mBase = s;
        mNeedToStop = false;
    }

    public void askDisconnect(GenericDevice d) {
        String addr = d.getAddress();
        if (mSimulators.containsKey(addr))
            mSimulators.remove(addr);
        if (mDevices.containsKey(addr))
            mDevices.get(addr).setBluetoothState(BluetoothState.DISCONNECTING);
        disconnect(d);
    }

    public abstract void disconnect(GenericDevice d);

    public abstract boolean connect(GenericDevice d, PUserHolder us);

    protected DeviceDataProcessor newDp(GenericDevice d) {
        DeviceDataProcessor dev = mDevices.get(d.getAddress());
        if (dev == null) {
            try {
                dev = mDpClass.newInstance();
                dev.setParams(d, mList, newSim(d), mBase.getContext());
                dev.setIsDebugging(d.getDebugFlag());
                mDevices.put(d.getAddress(), dev);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dev;
    }

    protected DeviceSimulator newSim(GenericDevice d) {
        DeviceSimulator sim = mSimulators.get(d.getAddress());
        Timber.tag(getClass().getName()).d("mSimulators "+mSimulators.size()+"; sim = "+sim);
        if (sim == null) {
            try {
                sim = mSimClass.newInstance();
                sim.reset();
                mSimulators.put(d.getAddress(), sim);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
            sim.setOffsets();
        return sim;
    }

    public DeviceBinder() {
        addDeviceListener(this);
    }

    public void setDataProcessorClass(Class<? extends DeviceDataProcessor> ddp) {
        mDpClass = ddp;
    }

    public void setSimulatorClass(Class<? extends DeviceSimulator> ddp) {
        mSimClass = ddp;
    }

    public PHolderSetter getStatusVars(GenericDevice d) {
        DeviceDataProcessor dch = mDevices.get(d.getAddress());
        if (dch != null)
            return dch.getStatusVars();
        else
            return null;
    }

    public DeviceUpdate getLastUpdate(GenericDevice d) {
        DeviceDataProcessor dch = mDevices.get(d.getAddress());
        if (dch != null)
            return dch.getLastUpdate();
        else
            return null;
    }

    public String getDeviceName(GenericDevice d) {
        DeviceDataProcessor dch = mDevices.get(d.getAddress());
        if (dch != null)
            return dch.mDeviceName;
        else
            return "";
    }

    protected void setDeviceName(GenericDevice d, String name) {
        DeviceDataProcessor dch = mDevices.get(d.getAddress());
        if (dch != null)
            dch.setDeviceName(name);
    }

    public void setUser(GenericDevice d, PUserHolder us) {
        DeviceDataProcessor dch = mDevices.get(d.getAddress());
        if (dch != null)
            dch.setUser(us);
    }

    public String getSessionSettings(GenericDevice d) {
        DeviceDataProcessor dch = mDevices.get(d.getAddress());
        if (dch != null)
            return dch.getSessionSettings();
        else
            return null;
    }

    public void stop() {
        if (!mNeedToStop) {
            mNeedToStop = true;
            Iterator<Map.Entry<String, DeviceDataProcessor>> it = mDevices.entrySet().iterator();
            DeviceDataProcessor vl;
            while (it.hasNext()) {
                Map.Entry<String, DeviceDataProcessor> pair = it.next();
                vl = pair.getValue();
                if (vl.isConnected())
                    disconnect(vl.mDeviceHolder);
                else
                    it.remove();
            }
            if (mDevices.isEmpty())
                mBase.stop();
        }
    }

    public void addDeviceListener(DeviceListener dcl) {
        if (!mList.contains(dcl))
            mList.add(dcl);
    }

    public void removeDeviceListener(DeviceListener dcl) {
        mList.remove(dcl);
    }

    public BaseMessage processCommand(GenericDevice d, BaseMessage hs2) {
        DeviceDataProcessor dch = mDevices.get(d.getAddress());
        if (dch != null)
            return dch.processCommand(hs2);
        else
            return null;
    }

    public BluetoothState getBluetoothState(GenericDevice d) {
        DeviceDataProcessor dch = mDevices.get(d.getAddress());
        if (dch != null)
            return dch.getBluetoothState();
        else
            return null;
    }

    public DeviceStatus getDeviceState(GenericDevice d) {
        DeviceDataProcessor dch = mDevices.get(d.getAddress());
        if (dch != null)
            return dch.getDeviceState();
        else
            return null;
    }

    public void pushSettingsChange(GenericDevice d) {
        DeviceDataProcessor dch = mDevices.get(d.getAddress());
        if (dch != null)
            dch.pushSettingsChange();
    }

    @Override
    public void onDeviceConnected(GenericDevice dev, PDeviceHolder devh) {
        dev.addNotification();
    }

    @Override
    public void onDeviceConnectionFailed(GenericDevice dev, PDeviceHolder devh) {
        mDevices.remove(dev.getAddress());
        dev.removeNotification();
        if (mNeedToStop && mDevices.isEmpty())
            mBase.stop();
    }

    @Override
    public void onDeviceDisconnected(GenericDevice dev, PDeviceHolder devh) {
        onDeviceConnectionFailed(dev, devh);
    }

    @Override
    public void onDeviceError(GenericDevice dev, PDeviceHolder devh, ParcelableMessage e) {
        onDeviceConnectionFailed(dev, devh);
    }

    @Override
    public void onDeviceUpdate(GenericDevice dev, PDeviceHolder devh,
                               DeviceUpdate paramFitnessHwApiDeviceFeedback) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUserSet(GenericDevice dev, PDeviceHolder devh, PUserHolder us) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeviceDescription(GenericDevice dev, PDeviceHolder devh,
                                    String desc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeviceStatusChange(GenericDevice dev, PDeviceHolder devh,
                                     PHolderSetter status) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeviceConnecting(GenericDevice mDeviceHolder,
                                   PDeviceHolder innerDevice) {
        // TODO Auto-generated method stub

    }
}
