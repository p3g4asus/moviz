package com.moviz.lib.hw;

import android.bluetooth.le.ScanRecord;
import android.util.SparseArray;

import com.moviz.lib.comunication.DeviceStatus;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PKeiserM3iHolder;
import com.moviz.lib.comunication.plus.holder.PPafersHolder;
import com.wahoofitness.connector.capabilities.DeviceInfo;

import java.util.LinkedHashMap;

public class KeiserM3iDataProcessor extends NonConnectableDataProcessor {
    protected static final DeviceInfo.Type[] infoKeys = new DeviceInfo.Type[]{
            DeviceInfo.Type.DEVICE_NAME,
            DeviceInfo.Type.FIRMWARE_REVISION,
            DeviceInfo.Type.SOFTWARE_REVISION,
            DeviceInfo.Type.SYSTEM_ID
    };
    private LinkedHashMap<String, String> infoMap = new LinkedHashMap<>();
    public KeiserM3iDataProcessor() {
        super(15000,50);
        for (DeviceInfo.Type tp : infoKeys)
            infoMap.put(tp.toString(), "");
    }

    public static String getMachineId(ScanRecord rec) {
        SparseArray<byte[]> sp = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            sp = rec.getManufacturerSpecificData();
        }
        byte[] bt = null;
        if (sp != null && sp.size() > 0)
            bt = sp.get(sp.keyAt(0));
        if (bt!=null && bt.length>6) {
            int index = 0;

            // Moves index past prefix bits (some platforms remove prefix bits from data)
            if (bt[index] == 2 && bt[index + 1] == 1)
                index += 2;
            return ""+((int)bt[index+3]);
        }
        else
            return "";
    }

    @Override
    public boolean parseData(GenericDevice dev, PDeviceHolder devh, byte[] arr, int length) {

        // Checks that broadcast is not debug signal
        if (arr == null || arr.length < 4 || arr.length > 19)
            return false;

        // Sets parser index
        int index = 0;

        // Moves index past prefix bits (some platforms remove prefix bits from data)
        if (arr[index] == 2 && arr[index + 1] == 1)
            index += 2;
        int mayor = arr[index++];
        int minor = arr[index++];
        if (mayor==0x06 && arr.length> (index +13)) {
            if (infoMap.get(DeviceInfo.Type.FIRMWARE_REVISION.toString()).isEmpty()) {
                infoMap.put(DeviceInfo.Type.DEVICE_NAME.toString(), devh.getName());
                infoMap.put(DeviceInfo.Type.FIRMWARE_REVISION.toString(), String.format("0x%02X", mayor));
                infoMap.put(DeviceInfo.Type.SOFTWARE_REVISION.toString(), String.format("0x%02X", minor));
                infoMap.put(DeviceInfo.Type.SYSTEM_ID.toString(), arr[index + 1]+"");
                setDeviceDescription(infoMap,"KM3i");
            }
            PKeiserM3iHolder k3 = new PKeiserM3iHolder();
            k3.rpm = twoByteConcat(arr[index + 2], arr[index + 3]);// / 10;
            k3.pulse = twoByteConcat(arr[index + 4], arr[index + 5]);// / 10;
                // Power in Watts
            k3.watt = (short) twoByteConcat(arr[index + 6], arr[index + 7]);
                // Energy as KCal ("energy burned")
            k3.calorie = (short) twoByteConcat(arr[index + 8], arr[index + 9]);
            // Time in Seconds (broadcast as minutes and seconds)
            k3.time = (short) (arr[index + 10] * 60);
            k3.time += arr[index + 11];
            int dist = twoByteConcat(arr[index + 12], arr[index + 13]);
            if ((dist & 32768) != 0)
                k3.distance = (dist&0x7FFF)/10.0;
            else
                k3.distance = dist/10.0*1.60934;
            if (minor >= 0x21 && arr.length > (index + 14)) {
                // Raw Gear Value
                k3.incline = arr[index + 14];
            }
            performUpdate(k3);
            k3.pulse/=10;
            k3.pulseMn/=10.0;
            k3.rpm/=10;
            k3.rpmMn/=10.0;
            // Sets broadcast to valid
            return true;
        }

        return false;
    }

    public void performUpdate(PPafersHolder statistic) {
        boolean pause = mSim.step(statistic);
        DeviceStatus status = mDeviceState;
        if (pause && status != DeviceStatus.DPAUSE) {
            setDeviceState(DeviceStatus.DPAUSE);
        } else if (!pause && status != DeviceStatus.RUNNING) {
            setDeviceState(DeviceStatus.RUNNING);
        }
        postDeviceUpdate(statistic);
    }

    @Override
    public BaseMessage processCommand(BaseMessage hs2) {
        return null;
    }
}
