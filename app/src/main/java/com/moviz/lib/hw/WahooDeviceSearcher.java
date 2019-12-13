package com.moviz.lib.hw;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcelable;

import com.moviz.gui.app.CA;
import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.utils.ParcelableMessage;
import com.wahoofitness.connector.HardwareConnector;
import com.wahoofitness.connector.HardwareConnectorEnums;
import com.wahoofitness.connector.HardwareConnectorTypes;
import com.wahoofitness.connector.conn.connections.SensorConnection;
import com.wahoofitness.connector.conn.connections.params.BTLEConnectionParams;
import com.wahoofitness.connector.conn.connections.params.ConnectionParams;
import com.wahoofitness.connector.listeners.discovery.DiscoveryListener;
import com.wahoofitness.connector.listeners.discovery.DiscoveryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Fujitsu on 24/10/2016.
 */
public class WahooDeviceSearcher implements DeviceSearcher {
    private HardwareConnector mHardwareConnector;
    private Handler mHandler = new Handler();
    private ArrayList<PDeviceHolder> resultsList = new ArrayList<>();
    private HashMap<String, Boolean> addrMap = new HashMap<>();

    private Runnable endDiscovery = new Runnable() {
        @Override
        public void run() {
            mHardwareConnector.stopDiscovery();
            CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_END).putExtra(DEVICE_FOUND, resultsList.toArray(new PDeviceHolder[0])));
        }
    };

    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {

        @Override
        public void onDeviceDiscovered(ConnectionParams params) {
            if (params instanceof BTLEConnectionParams) {
                BTLEConnectionParams bcp = (BTLEConnectionParams) params;
                BluetoothDevice bd = bcp.getBluetoothDevice();
                String addr = bd.getAddress();
                if (!addrMap.containsKey(addr)) {
                    resultsList.add(new PDeviceHolder(-1, addr, "[" + params.getNetworkType().name() + "]"+
                            "[" + params.getSensorType().name() + "] " +
                            bd.getName(), "", DeviceType.hrdevice, "","", true));
                    addrMap.put(addr, true);
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

    public WahooDeviceSearcher() {

    }

    @Override
    public void startSearch(Context ct) {
        if (mHardwareConnector == null)
            mHardwareConnector = new HardwareConnector(ct, mHardwareConnectorCallback);
        scanLeDevice(true);
    }

    @Override
    public void stopSearch() {
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            resultsList.clear();
            addrMap.clear();

            if (mHardwareConnector != null) {
                mHandler.postDelayed(endDiscovery, 10000);
                DiscoveryResult res = mHardwareConnector.startDiscovery(mDiscoveryListener);
                DiscoveryResult.DiscoveryResultCode rc;
                if ((rc = res.getResult(HardwareConnectorTypes.NetworkType.BTLE)) != DiscoveryResult.DiscoveryResultCode.SUCCESS) {
                    CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_ERROR).putExtra(DEVICE_ERROR_CODE, (Parcelable) new ParcelableMessage("exm_errs_searcherror").put(rc.ordinal())));
                }
            } else
                CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_ERROR).putExtra(DEVICE_ERROR_CODE, (Parcelable) new ParcelableMessage("exm_errs_adapter")));
        } else {
            mHardwareConnector.stopDiscovery(mDiscoveryListener);
            mHandler.removeCallbacks(endDiscovery);
        }
    }

    @Override
    public int needsRebind(GenericDevice d) {
        return 0;
    }

    @Override
    public void startRebind(Context ct, List<GenericDevice> d) {

    }

    @Override
    public void stopRebind() {

    }
}
