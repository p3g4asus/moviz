package com.moviz.lib.hw;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.utils.ParcelableMessage;
import com.wahoofitness.common.log.Logger;
import com.wahoofitness.connector.HardwareConnector;
import com.wahoofitness.connector.HardwareConnectorEnums;
import com.wahoofitness.connector.HardwareConnectorTypes;
import com.wahoofitness.connector.capabilities.Capability;
import com.wahoofitness.connector.conn.connections.SensorConnection;
import com.wahoofitness.connector.conn.connections.params.BTLEConnectionParams;
import com.wahoofitness.connector.conn.connections.params.ConnectionParams;
import com.wahoofitness.connector.listeners.discovery.DiscoveryListener;
import com.wahoofitness.connector.listeners.discovery.DiscoveryResult;

import timber.log.Timber;

/**
 * Created by Fujitsu on 24/10/2016.
 */
public class WahooBinder extends DeviceBinder {
    protected HardwareConnector mHardwareConnector;
    protected Handler mHandler = new Handler(Looper.myLooper());

    private EndDiscoveryRunnable endDiscovery = new EndDiscoveryRunnable();

    private class EndDiscoveryRunnable implements Runnable {
        public WahooDataProcessor getDataProcessor() {
            return dataProcessor;
        }

        private WahooDataProcessor dataProcessor = null;

        @Override
        public void run() {
            mHardwareConnector.stopDiscovery();
            if (dataProcessor!=null) {
                dataProcessor.postDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
                dataProcessor = null;
            }
        }

        public void setDataProcessor(WahooDataProcessor wd) {
            dataProcessor = wd;
        }
    };

    public interface OnNewCapabilityListener {
        void onNewCapabilityDetected(SensorConnection sensorConnection,
                                     Capability.CapabilityType capabilityType);
    }


    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {

        @Override
        public void onDeviceDiscovered(ConnectionParams params) {
            if (params instanceof BTLEConnectionParams) {
                BTLEConnectionParams bcp = (BTLEConnectionParams) params;
                BluetoothDevice bd = bcp.getBluetoothDevice();
                String addr = bd.getAddress();
                WahooDataProcessor dp = endDiscovery.getDataProcessor();
                if (dp!=null) {
                    if (addr.equals(dp.getDeviceAddress())) {
                        mHandler.removeCallbacks(endDiscovery);
                        mHardwareConnector.stopDiscovery();
                        mHardwareConnector.requestSensorConnection(params,mSensorConnectionListener);
                    }
                }
            }
        }

        @Override
        public void onDiscoveredDeviceLost(ConnectionParams params) {
            Timber.tag(TAG).v("mDiscoveryListener.onDiscoveredDeviceLost " + params);
        }

        @Override
        public void onDiscoveredDeviceRssiChanged(ConnectionParams params, int rssi) {
        }
    };

    private final HardwareConnector.Listener mHardwareConnectorCallback = new HardwareConnector.Listener() {

        /*@Override
        public void connectedSensor(SensorConnection sensorConnection) {
            Timber.tag(TAG).v("mHardwareConnectorCallback.connectedSensor " + sensorConnection);
        }
        @Override
        public void disconnectedSensor(SensorConnection sensorConnection) {
            Timber.tag(TAG).v("mHardwareConnectorCallback.disconnectedSensor " + sensorConnection);
        }*/

        @Override
        public void onHardwareConnectorStateChanged(HardwareConnectorTypes.NetworkType networkType,
                                          HardwareConnectorEnums.HardwareConnectorState hardwareState) {
            Timber.tag(TAG).v("mHardwareConnectorCallback.connectorStateChanged " + networkType + " " + hardwareState);
        }

        @Override
        public void onFirmwareUpdateRequired(SensorConnection sensorConnection,
                                             String currentVersionNumber, String recommendedVersion) {
            Timber.tag(TAG).v("mHardwareConnectorCallback.onFirmwareUpdateRequired " + sensorConnection + " " + currentVersionNumber + " " + recommendedVersion);
        }
    };

    private DeviceDataProcessor sensorConnection2DataProcessor(SensorConnection sensorConnection) {
        return mDevices.get(((BTLEConnectionParams) sensorConnection.getConnectionParams()).getBluetoothDevice().getAddress());
    }

    private final SensorConnection.Listener mSensorConnectionListener = new SensorConnection.Listener() {

        @Override
        public void onNewCapabilityDetected(SensorConnection sensorConnection,
                                            Capability.CapabilityType capabilityType) {
            Timber.tag(TAG).v("mSensorConnectionListener.onNewCapabilityDetected "+sensorConnection+" "+capabilityType);
            DeviceDataProcessor devb = sensorConnection2DataProcessor(sensorConnection);
            if (devb!=null)
                ((WahooDataProcessor) devb).onNewCapabilityDetected(sensorConnection, capabilityType);
        }

        @Override
        public void onSensorConnectionError(SensorConnection sensorConnection,
                                            HardwareConnectorEnums.SensorConnectionError error) {
            Timber.tag(TAG).v("mSensorConnectionListener.onSensorConnectionError "+sensorConnection+" "+error);
            if (error!=HardwareConnectorEnums.SensorConnectionError.BTLE_READ_CHARACTERISTICS_ERROR) {
                DeviceDataProcessor devb = sensorConnection2DataProcessor(sensorConnection);
                if (devb != null)
                    ((WahooDataProcessor) devb).setDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
                sensorConnection.disconnect();
            }
        }

        @Override
        public void onSensorConnectionStateChanged(SensorConnection sensorConnection,
                                                   HardwareConnectorEnums.SensorConnectionState state) {
            Timber.tag(TAG).v("mSensorConnectionListener.onSensorConnectionStateChanged "+sensorConnection+" "+state);
            DeviceDataProcessor devb = sensorConnection2DataProcessor(sensorConnection);
            if (devb!=null) {
                ParcelableMessage pm;
                if (state == HardwareConnectorEnums.SensorConnectionState.DISCONNECTED) {
                    devb.postDeviceError((pm = ((WahooDataProcessor)devb).resetDeviceError())!=null?pm:new ParcelableMessage("exm_errr_connectionlost"));
                } else if (state == HardwareConnectorEnums.SensorConnectionState.DISCONNECTING) {
                    devb.setBluetoothState(BluetoothState.DISCONNECTING);
                } else if (state == HardwareConnectorEnums.SensorConnectionState.CONNECTED) {
                    devb.setBluetoothState(BluetoothState.CONNECTED);
                } else if (state == HardwareConnectorEnums.SensorConnectionState.CONNECTING) {
                    devb.setBluetoothState(BluetoothState.CONNECTING);
                }
            }
        }
    };


    @Override
    public void setService(DeviceService.BaseBinder bb) {
        super.setService(bb);
    }

    @Override
    public void disconnect(final GenericDevice d) {
        final ConnectionParams params;
        if (mHardwareConnector!=null && (params = ((WahooDevice) d).cpFromDev())!=null)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    SensorConnection sensorConnection = mHardwareConnector.getSensorConnection(params);
                    if (sensorConnection != null) {
                        sensorConnection.disconnect();
                    }
                }
            });
    }

    @Override
    public boolean connect(GenericDevice device, PUserHolder us) {
        if (device == null) {
            Timber.tag(TAG).w("BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final DeviceDataProcessor bldevb = newDp(device);
        BluetoothState bst = bldevb.getBluetoothState();
        if (bst != BluetoothState.CONNECTING && bst != BluetoothState.CONNECTED) {
            bldevb.setUser(us);
            bldevb.setBluetoothState(BluetoothState.CONNECTING);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mHardwareConnector==null) {
                        mHardwareConnector = new HardwareConnector(mBase.getContext(), mHardwareConnectorCallback);
                        Logger.setLogLevel(Log.VERBOSE);
                    }
                    if (mHardwareConnector.startDiscovery(mDiscoveryListener).getResult(HardwareConnectorTypes.NetworkType.BTLE)!=DiscoveryResult.DiscoveryResultCode.SUCCESS)
                        bldevb.postDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
                    else {
                        endDiscovery.setDataProcessor((WahooDataProcessor) bldevb);
                        mHandler.postDelayed(endDiscovery, 10000);
                    }
                }
            });
            return true;
        }
        return false;
    }
}
