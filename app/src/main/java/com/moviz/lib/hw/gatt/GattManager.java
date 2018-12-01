package com.moviz.lib.hw.gatt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.moviz.lib.hw.BluetoothState;
import com.moviz.lib.hw.gatt.operations.GattCharacteristicReadOperation;
import com.moviz.lib.hw.gatt.operations.GattDescriptorReadOperation;
import com.moviz.lib.hw.gatt.operations.GattDisconnectOperation;
import com.moviz.lib.hw.gatt.operations.GattOperation;
import com.moviz.lib.utils.ParcelableMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GattManager {

    private TimeoutTask mTimeoutTask = null;

    private class GattBundle {
        public BluetoothGatt mGatt;
        public boolean mConnected = false;

        public GattBundle(BluetoothGatt gatt) {
            mGatt = gatt;
        }

        public void con() {
            mConnected = true;
        }

        public void close() {
            try {
                mGatt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mConnected = false;
        }
    }

    //public static final String TRIGGER_CONNECTION_STATE_CHANGED = "GattManager.TRIGGER_CONNECTION_STATE_CHANGED";
    //public static final String EXTRA_DEVICE_ADDRESS = "GattManager.EXTRA_DEVICE_ADDRESS";
    //public static final String EXTRA_DEVICE_CONNSTATE = "GattManager.EXTRA_DEVICE_CONNSTATE";
    private static final String TAG = "GattManager";
    private ConcurrentLinkedQueue<GattOperation> mQueue;
    private ConcurrentHashMap<String, GattBundle> mGatts;
    private GattOperation mCurrentOperation;
    private HashMap<UUID, ArrayList<CharacteristicChangeListener>> mCharacteristicChangeListeners;
    private Handler mHandler = new Handler(Looper.myLooper());
    private ConnectionStateChangedListener mConnectionStateChangedListener = null;

    private Context context;
    private BluetoothManager mBluetoothManager = null;
    //private int mNumRep = 0;
    //private final static int MAX_NUM_REP = 2;

    public GattManager(Context ctx) {
        mQueue = new ConcurrentLinkedQueue<>();
        mGatts = new ConcurrentHashMap<>();
        mCurrentOperation = null;
        mCharacteristicChangeListeners = new HashMap<>();
        context = ctx;
    }

    public synchronized void cancelCurrentOperationBundle() {
        Log.v(TAG, "Cancelling current operation. Queue size before: " + mQueue.size());
        if (mCurrentOperation != null && mCurrentOperation.getBundle() != null) {
            for (GattOperation op : mCurrentOperation.getBundle().getOperations()) {
                mQueue.remove(op);
            }
        }
        Log.v(TAG, "Queue size after: " + mQueue.size());
        mCurrentOperation = null;
        drive();
    }

    private synchronized void removeOpOfDevice(GattOperation myop) {
        BluetoothDevice device = myop.getDevice();
        GattOperation op;
        for (Iterator<GattOperation> iterator = mQueue.iterator(); iterator.hasNext(); ) {
            op = iterator.next();
            if (op.getDevice().equals(device))
                iterator.remove();
        }
        mGatts.remove(device.getAddress());
    }

    public synchronized void cancelDeviceOperation() {
        Log.v(TAG, "Cancelling current operation. Queue size before: " + mQueue.size());
        if (mCurrentOperation != null) {
            removeOpOfDevice(mCurrentOperation);
            setCurrentOperation(null);
        }
        Log.v(TAG, "Queue size after: " + mQueue.size() + " (" + this + ")");
        drive();
    }

    public synchronized void queue(GattOperation gattOperation) {
        /*if (gattOperation instanceof GattDisconnectOperation) {
            if (mCurrentOperation!=null && mCurrentOperation.getDevice().equals(gattOperation.getDevice())) {
                setCurrentOperation(null);
                if(mCurrentOperationTimeout != null) {
                    mCurrentOperationTimeout.cancel(true);
                    mCurrentOperationTimeout = null;
                }
            }
            removeOpOfDevice(gattOperation);
        }*/
        mQueue.add(gattOperation);
        Log.v(TAG, "Queueing Gatt operation (" + gattOperation + "), size will now become: " + mQueue.size());
        drive();
    }

    private void handleDisconnection(BluetoothGatt deviceGatt, String devAddress, GattOperation operation) {
        if (deviceGatt != null) {
            try {
                deviceGatt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            cancelDeviceOperation();
        }
        setCurrentOperation(null);
        if (devAddress != null)
            mGatts.remove(devAddress);
        drive();
        if (mConnectionStateChangedListener != null)
            mConnectionStateChangedListener.error(devAddress,
                    new ParcelableMessage("exm_errr_connectionlost"),
                    operation);
    }

    private class TimeoutTask implements Runnable {

        private final String mDevAddress;
        private final GattOperation mOperation;

        public TimeoutTask(String addr, GattOperation op) {
            mDevAddress = addr;
            mOperation = op;
        }
        @Override
        public void run() {
            Log.w(TAG, "Timeout Detected");
            GattBundle deviceGatt = mGatts.get(mDevAddress);
            if (deviceGatt == null || !deviceGatt.mConnected) {
                if (mConnectionStateChangedListener != null)
                    mConnectionStateChangedListener.error(mDevAddress,
                            new ParcelableMessage("exm_errr_connectionfailed"),
                            mOperation);
                if (deviceGatt != null)
                    deviceGatt.close();
                cancelDeviceOperation();
            } else if (mOperation instanceof GattDisconnectOperation) {
                handleDisconnection(deviceGatt.mGatt, mDevAddress, mOperation);
            }
                /*else if (mNumRep<MAX_NUM_REP) {
                    mNumRep++;
                    Log.w(TAG,"Timeout Detected: repeating");
                    setCurrentOperation(null);
                    drive();
                }*/
            else {
                //mNumRep = 0;
                if (mConnectionStateChangedListener != null)
                    mConnectionStateChangedListener.error(mDevAddress,
                            new ParcelableMessage("exm_errr_gatt_operationtimeout").put(mOperation.toString()),
                            mOperation);
                Log.e(TAG, "Timeout ran to completion, time to cancel the entire operation bundle. Abort, abort!");
                cancelDeviceOperation();
            }
                /*else {
                    setCurrentOperation(null);
                    drive();
                }*/
        }
    }

    private synchronized void drive() {
        if (mCurrentOperation != null) {
            Log.v(TAG, "tried to drive, but currentOperation was not null, " + mCurrentOperation);
            return;
        }
        mHandler.removeCallbacks(mTimeoutTask);
        if (mQueue.size() == 0) {
            Log.v(TAG, "Queue empty, drive loop stopped.");
            mCurrentOperation = null;
            return;
        }
        if (mBluetoothManager == null)
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        final GattOperation operation = mQueue.poll();
        final BluetoothDevice device = operation.getDevice();
        final String devAddress = device.getAddress();
        Log.v(TAG, "Driving Gatt queue, size will now become: " + mQueue.size() + " (" + this + ")");
        setCurrentOperation(operation);

        mHandler.postDelayed(mTimeoutTask = new TimeoutTask(devAddress,operation),operation.getTimoutInMillis());

        GattBundle deviceGatt = mGatts.get(devAddress);
        //DBG
        //Log.d(TAG,"ECCO 1 "+deviceGatt+" "+(deviceGatt==null?"null2":deviceGatt.mConnected)+" "+mBluetoothManager.getConnectedDevices(BluetoothGatt.GATT).indexOf(device));
        if (deviceGatt != null && deviceGatt.mConnected && mBluetoothManager.getConnectedDevices(BluetoothGatt.GATT).indexOf(device) >= 0) {
            execute(deviceGatt.mGatt, operation);
        } else if (operation instanceof GattDisconnectOperation) {
            if (deviceGatt != null)
                handleDisconnection(deviceGatt.mGatt, devAddress, operation);
        } else if (deviceGatt == null) {
            Log.i(TAG, "Trying to connect to " + devAddress);
            BluetoothGatt gatt = device.connectGatt(context, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    if (mConnectionStateChangedListener != null)
                        mConnectionStateChangedListener.stateChanged(devAddress, BluetoothState.values()[newState], operation);

                    if (status == 133) {
                        Log.e(TAG, "Got the status 133 bug, closing gatt");
                        gatt.close();
                        mGatts.remove(devAddress);
                        return;
                    }

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "Gatt connected to device " + devAddress);
                        mGatts.get(devAddress).con();
                        gatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i(TAG, "Disconnected from gatt server " + devAddress + ", newState: " + newState);
                        handleDisconnection(gatt, devAddress, null);
                    }
                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorRead(gatt, descriptor, status);
                    if (status == BluetoothGatt.GATT_SUCCESS)
                        ((GattDescriptorReadOperation) mCurrentOperation).onRead(descriptor);
                    setCurrentOperation(null);
                    drive();
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                    setCurrentOperation(null);
                    drive();
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    if (status == BluetoothGatt.GATT_SUCCESS)
                        ((GattCharacteristicReadOperation) mCurrentOperation).onRead(characteristic);
                    setCurrentOperation(null);
                    drive();
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    Log.d(TAG, "services discovered, status: " + status);
                    execute(gatt, operation);
                }


                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    Log.d(TAG, "Characteristic " + characteristic.getUuid() + "written to on device " + device.getAddress());
                    setCurrentOperation(null);
                    drive();
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    //Log.e(TAG,"Characteristic " + characteristic.getUuid() + "was changed, device: " + device.getAddress());
                    if (mCharacteristicChangeListeners.containsKey(characteristic.getUuid())) {
                        for (CharacteristicChangeListener listener : mCharacteristicChangeListeners.get(characteristic.getUuid())) {
                            listener.onCharacteristicChanged(devAddress, characteristic);
                        }
                    }
                }
            });
            mGatts.put(devAddress, new GattBundle(gatt));
        } else {
            Log.w(TAG, "It seems I am disconnected but I was not notified");
            cancelDeviceOperation();
        }
    }

    private void execute(BluetoothGatt gatt, GattOperation operation) {
        if (operation != mCurrentOperation) {
            return;
        }
        try {
            Log.d(TAG, "Executing " + operation);
            operation.execute(gatt);
            if (!operation.hasAvailableCompletionCallback()) {
                setCurrentOperation(null);
                drive();
            }
        } catch (Exception e) {
            String addr;
            if (mConnectionStateChangedListener != null) {
                addr = operation.getDevice().getAddress();
                mConnectionStateChangedListener.error(
                        addr,
                        (gatt != null ?
                                new ParcelableMessage("exm_errr_gatt_operationfailed").put(operation.toString()) :
                                new ParcelableMessage("exm_errr_connectionlost")),
                        operation);
            }
            cancelDeviceOperation();
            e.printStackTrace();
        }
    }

    public synchronized void setCurrentOperation(GattOperation currentOperation) {
        mCurrentOperation = currentOperation;
    }

    public BluetoothGatt getGatt(BluetoothDevice device) {
        return mGatts.get(device).mGatt;
    }

    public BluetoothGatt getGatt(String deviceAddress) {
        return mGatts.get(deviceAddress).mGatt;
    }

    public void addCharacteristicChangeListener(UUID characteristicUuid, CharacteristicChangeListener characteristicChangeListener) {
        if (!mCharacteristicChangeListeners.containsKey(characteristicUuid)) {
            mCharacteristicChangeListeners.put(characteristicUuid, new ArrayList<CharacteristicChangeListener>());
        }
        mCharacteristicChangeListeners.get(characteristicUuid).add(characteristicChangeListener);
    }

    public void addNoDuplicateCharacteristicChangeListener(UUID characteristicUuid, CharacteristicChangeListener characteristicChangeListener) {
        ArrayList<CharacteristicChangeListener> chars;
        if (!mCharacteristicChangeListeners.containsKey(characteristicUuid)) {
            mCharacteristicChangeListeners.put(characteristicUuid, chars = new ArrayList<CharacteristicChangeListener>());
            chars.add(characteristicChangeListener);
        } else {
            chars = mCharacteristicChangeListeners.get(characteristicUuid);
            if (chars.indexOf(characteristicChangeListener) < 0)
                chars.add(characteristicChangeListener);
        }
    }

    public void queue(GattOperationBundle bundle) {
        for (GattOperation operation : bundle.getOperations()) {
            queue(operation);
        }
    }

    public void setConnectionStateChangedListener(
            ConnectionStateChangedListener mConnectionStateChangedListener) {
        this.mConnectionStateChangedListener = mConnectionStateChangedListener;
    }
}
