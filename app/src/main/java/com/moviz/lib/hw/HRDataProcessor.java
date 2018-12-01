package com.moviz.lib.hw;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.movisens.smartgattlib.Characteristic;
import com.movisens.smartgattlib.characteristics.BatteryLevel;
import com.movisens.smartgattlib.characteristics.HeartRateMeasurement;
import com.movisens.smartgattlib.characteristics.ManufacturerNameString;
import com.moviz.lib.comunication.DeviceStatus;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHRDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Created by Fujitsu on 02/11/2016.
 */
public class HRDataProcessor extends BLEDataProcessor {
    private byte lastBattery = -1;
    private LinkedHashMap<String, String> infoMap = new LinkedHashMap<String, String>();


    public HRDataProcessor() {
        infoMap.put(INFOMAP_MANUFACTURERDATA, "");
        infoMap.put(INFOMAP_MODELDATA, "");
        infoMap.put(INFOMAP_SERIALNUMBERDATA, "");
        infoMap.put(INFOMAP_HARDWAREREVDATA, "");
        infoMap.put(INFOMAP_FIRMWAREREVDATA, "");
        infoMap.put(INFOMAP_SOFTWAREREVDATA, "");
    }

    @Override
    public boolean onReadData(GenericDevice dev, PDeviceHolder devh, BluetoothGattCharacteristic characteristic) {
        UUID cuid = characteristic.getUuid();
        boolean rv = false;
        if (Characteristic.HEART_RATE_MEASUREMENT.equals(cuid)) {
            PHRDeviceHolder w = new PHRDeviceHolder();
            byte[] value = characteristic.getValue();
            HeartRateMeasurement hrm = new HeartRateMeasurement(value); // Interpret Characteristic

            int eeval = hrm.getEe();
            if (eeval >= 0) {
                w.joule = (short) eeval;
            }
            w.pulse = (short) hrm.getHr();
            w.battery = lastBattery;
            ArrayList<Float> tm = hrm.getRrInterval();
            if (tm != null) {
                int ln = Math.min(tm.size(), w.rrintervals.length);
                for (int i = 0; i < ln; i++)
                    w.rrintervals[i] = tm.get(i);
                w.nintervals = (byte) ln;
            }
            HeartRateMeasurement.SensorWorn val = hrm.getSensorWorn();
            w.worn = (byte) (val == HeartRateMeasurement.SensorWorn.WORN ? 1 : val == HeartRateMeasurement.SensorWorn.NOT_WORN? 0:2);
            if (!mSim.step(w)) {
                setDeviceState(DeviceStatus.RUNNING);
                postDeviceUpdate(w);
            } else
                setDeviceState(DeviceStatus.PAUSED);
            rv = true;
        } else if (Characteristic.MODEL_NUMBER_STRING.equals(cuid) ||
                Characteristic.FIRMWARE_REVISION_STRING.equals(cuid) ||
                Characteristic.HARDWARE_REVISION_STRING.equals(cuid) ||
                Characteristic.SOFTWARE_REVISION_STRING.equals(cuid) ||
                Characteristic.MANUFACTURER_NAME_STRING.equals(cuid) ||
                Characteristic.SERIAL_NUMBER_STRING.equals(cuid)) {
            byte[] value = characteristic.getValue();
            ManufacturerNameString hrm = new ManufacturerNameString(value); // Interpret Characteristic
            infoMap.put(cuid.toString(), hrm.getManufacturerNameString());
            setDeviceDescription(infoMap, "BLE-HR");
            rv = true;
        } else if (Characteristic.BATTERY_LEVEL.equals(cuid)) {
            byte[] value = characteristic.getValue();
            BatteryLevel hrm = new BatteryLevel(value); // Interpret Characteristic
            lastBattery = (byte) hrm.getBatteryLevel();
            setStatusVar(".battery", lastBattery);
            Log.d(TAG, "Battery = " + lastBattery + "%");
            rv = true;
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(
                        data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ",
                            byteChar));
                Log.d(TAG, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        return rv;
    }

    @Override
    public void initStatusVars(PHolderSetter statusVars) {
        super.initStatusVars(statusVars);
        statusVars.add(new PHolder(PHolder.Type.BYTE, "status.battery"));
    }

}