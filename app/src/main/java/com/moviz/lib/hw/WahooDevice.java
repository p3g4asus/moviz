package com.moviz.lib.hw;

import com.moviz.gui.R;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolder;
import com.wahoofitness.connector.conn.connections.params.ConnectionParams;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Fujitsu on 24/10/2016.
 */
public abstract class WahooDevice extends GenericDevice {

    @Override
    protected void prepareServiceConnection() {

    }

    @Override
    protected int getIcon() {
        return R.drawable.ic_stat_wahoodevice;
    }

    @Override
    protected String getNotificationTitle() {
        return "Wahoo Device";
    }

    @Override
    protected String getNotificationText() {
        return "Wahoo " + innerDevice().getAlias() + " active";
    }

    private static String serialCPFromDev(PDeviceHolder dev) {
        try {
            Pattern p = Pattern.compile("^\\[([^\\]]+)\\]\\[([^\\]]+)\\](.*)$");
            Matcher m = p.matcher(dev.getName());
            if (m.find()) {
                JSONObject var1 = new JSONObject();
                var1.put("networkType", m.group(1));
                var1.put("sensorType", m.group(2));
                var1.put("deviceName", m.group(3));
                var1.put("bluetoothDevice", dev.getAddress());
                return var1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public ConnectionParams cpFromDev() {
        if (device != null) {
            String s = serialCPFromDev(device);
            if (!s.isEmpty())
                return ConnectionParams.deserialize(s);
        }
        return null;
    }
}
