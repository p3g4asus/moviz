package com.moviz.lib.hw;

import com.moviz.lib.comunication.DeviceStatus;
import com.moviz.lib.comunication.holder.BatteryLevelPrinter;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PWahooBlueSCHolder;
import com.moviz.lib.comunication.plus.message.DeviceChangeRequestMessage;
import com.moviz.lib.comunication.tcp.TCPMessageTypes;
import com.wahoofitness.connector.capabilities.Battery;
import com.wahoofitness.connector.capabilities.Capability.CapabilityType;
import com.wahoofitness.connector.capabilities.CrankRevs;
import com.wahoofitness.connector.capabilities.DeviceInfo;
import com.wahoofitness.connector.capabilities.FirmwareVersion;
import com.wahoofitness.connector.capabilities.WheelRevs;
import com.wahoofitness.connector.conn.connections.SensorConnection;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Fujitsu on 25/10/2016.
 */
public class WahooBlueSCDataProcessor extends WahooDataProcessor implements CrankRevs.Listener, WheelRevs.Listener, Battery.Listener, DeviceInfo.Listener, FirmwareVersion.Listener {

    protected long wheelDiam = 0;
    protected byte lastBattery = 0;
    protected long lastBatteryMS = 0;
    protected Battery lastBatteryCapability = null;
    protected int currentGear = 0;
    protected String gearFactor = "2.0, 1.8";
    protected double[] gearFactorP = new double[]{2.0,1.8};

    @Override
    public void onDeviceConnectionFailed(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceConnectionFailed(dev, devh);
        resetcls();
    }

    @Override
    public void onDeviceDisconnected(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceDisconnected(dev, devh);
        resetcls();
    }

    @Override
    public void onDeviceConnected(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceConnected(dev, devh);
        resetcls();
    }

    private void resetcls() {
        ((WahooBlueSCDeviceSimulator) mSim).setUser(mCurrentUser);
        ((WahooBlueSCDeviceSimulator) mSim).setWheelDiam(wheelDiam);
        ((WahooBlueSCDeviceSimulator) mSim).setGearFactor(gearFactorP = parseGearFactor(gearFactor));
        currentGear %= gearFactorP.length;
        ((WahooBlueSCDeviceSimulator) mSim).setCurrentGear(currentGear);
        postUser(mCurrentUser);
        setStatusVar(".wheeldiam", wheelDiam);
        setStatusVar(".gearfactor", gearFactor);
        setStatusVar(".currgearfactor", gearFactorP[currentGear]);
    }

    @Override
    public BaseMessage processCommand(BaseMessage hs) {
        byte type = hs.getType();
        if (type == TCPMessageTypes.UPDOWN_MESSAGE) {
            int val = ((com.moviz.lib.comunication.message.UpDownMessage) hs).getHow();
            currentGear+=val;
            while (currentGear<0)
                currentGear+=gearFactorP.length;
            currentGear%=gearFactorP.length;
            PDeviceHolder devh = mDeviceHolder.innerDevice();
            Map<String,String> adds = devh.deserializeAdditionalSettings();
            adds.put("currentgear", currentGear+"");
            devh.serializeAdditionalSettings(adds,false);
            ((WahooBlueSCDeviceSimulator)mSim).setCurrentGear(currentGear);
            setStatusVar(".currgearfactor", gearFactorP[currentGear]);
            return new DeviceChangeRequestMessage(devh, "currentgear", currentGear+"");
        }
        else
            return super.processCommand(hs);
    }

        private double[] parseGearFactor(String gearFactor) {
        JSONArray jsa = null;
        try {
            jsa = new JSONArray("["+gearFactor+"]");
            if (jsa.length()==0)
                throw new IllegalArgumentException("Array cannot be empty");
        } catch (Exception e) {
            try {
                jsa = new JSONArray("[2.0,1.8]");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        double[] out = new double[jsa.length()];
        for (int i = 0; i<out.length; i++) {
            try {
                out[i] = jsa.getDouble(i);
                if (out[i]<=0)
                    throw new IllegalArgumentException("Cannot be <=0");
            } catch (Exception e) {
                out[i] = 1.0;
            }
        }
        return out;
    }


    protected void updateVal(PWahooBlueSCHolder.SensorType upd, long val, double speed) {

        PWahooBlueSCHolder sh = new PWahooBlueSCHolder();
        sh.sensType = upd;
        sh.sensVal = val;
        sh.sensSpd = speed;
        int pause = mSim.step(sh);
        Timber.tag(TAG).d(mDeviceName+" P="+pause);
        DeviceStatus status = mDeviceState;
        long now = System.currentTimeMillis();
        if (now-lastBatteryMS>5*60000) {
            lastBatteryMS = now;
            if (lastBatteryCapability!=null)
                lastBatteryCapability.sendReadBatteryData();
        }
        if (pause==DeviceSimulator.PAUSE_DETECTED && status != DeviceStatus.DPAUSE) {
            setDeviceState(DeviceStatus.DPAUSE);
        } else if (pause==DeviceSimulator.DEVICE_ONLINE && status != DeviceStatus.RUNNING) {
            setDeviceState(DeviceStatus.RUNNING);
        }
        if (pause!=DeviceSimulator.DO_NOT_POST_DU)
            postDeviceUpdate(sh);
    }

    protected static final DeviceInfo.Type[] infoKeys = new DeviceInfo.Type[]{
            DeviceInfo.Type.MANUFACTURER_NAME,
            DeviceInfo.Type.DEVICE_NAME,
            DeviceInfo.Type.MODEL_NUMBER,
            DeviceInfo.Type.HARDWARE_REVISION,
            DeviceInfo.Type.FIRMWARE_REVISION,
            DeviceInfo.Type.SOFTWARE_REVISION,
            DeviceInfo.Type.SYSTEM_ID,
            DeviceInfo.Type.SERIAL_NUMBER
    };
    private LinkedHashMap<String, String> infoMap = new LinkedHashMap<>();

    public WahooBlueSCDataProcessor() {
        for (DeviceInfo.Type tp : infoKeys)
            infoMap.put(tp.toString(), "");
    }

    public void setWheelDiam(long diam) {
        wheelDiam = diam;
    }

    public long getWheelDiam() {
        return wheelDiam;
    }

    @Override
    public void pushSettingsChange() {
        Map<String,String> adds = mDeviceHolder.innerDevice().deserializeAdditionalSettings();
        try {
            wheelDiam = Long.parseLong(adds.get("wheeldiam"));
        } catch (Exception e) {
            e.printStackTrace();
            wheelDiam = 660;
        }

        try {
            gearFactor = adds.get("gearfactor");
        } catch (Exception e) {
            e.printStackTrace();
            gearFactor = "[2.0, 1.8]";
        }
        gearFactorP = parseGearFactor(gearFactor);

        try {
            currentGear = Integer.parseInt(adds.get("currentgear"));
            while (currentGear<0)
                currentGear+=gearFactorP.length;
            currentGear%=gearFactorP.length;
        } catch (Exception e) {
            e.printStackTrace();
            currentGear = 0;
        }
        ((WahooBlueSCDeviceSimulator) mSim).setCurrentGear(currentGear);
        setStatusVar(".currgearfactor", gearFactorP[currentGear]);
    }

    @Override
    public void onNewCapabilityDetected(SensorConnection sensorConnection, CapabilityType capabilityType) {
        Timber.tag(TAG).v("New Cap " + capabilityType);
        if (capabilityType == CapabilityType.CrankRevs) {
            CrankRevs crankRevs = (CrankRevs) sensorConnection.getCurrentCapability(CapabilityType.CrankRevs);
            crankRevs.addListener(this);
        } else if (capabilityType == CapabilityType.WheelRevs) {
            WheelRevs wheelRevs = (WheelRevs) sensorConnection.getCurrentCapability(CapabilityType.WheelRevs);
            wheelRevs.addListener(this);
        } else if (capabilityType == CapabilityType.Battery) {
            lastBatteryCapability = (Battery) sensorConnection.getCurrentCapability(CapabilityType.Battery);
            lastBatteryCapability.addListener(this);
        } else if (capabilityType == CapabilityType.DeviceInfo) {
            DeviceInfo deviceInfo = (DeviceInfo) sensorConnection.getCurrentCapability(CapabilityType.DeviceInfo);
            deviceInfo.addListener(this);
        } else if (capabilityType == CapabilityType.FirmwareVersion) {
            FirmwareVersion firmwareVersion = (FirmwareVersion) sensorConnection.getCurrentCapability(CapabilityType.FirmwareVersion);
            firmwareVersion.addListener(this);
        }
    }

    @Override
    public void initStatusVars(PHolderSetter statusVars) {
        super.initStatusVars(statusVars);
        statusVars.add(new PHolder((byte) 0, "status.batterylev", new BatteryLevelPrinter()));
        statusVars.add(new PHolder(PHolder.Type.SHORT, "status.wheeldiam"));
        statusVars.add(new PHolder(PHolder.Type.STRING, "status.gearfactor"));
        statusVars.add(new PHolder(PHolder.Type.DOUBLE, "status.currgearfactor"));
    }

    @Override
    public void onCrankRevsData(CrankRevs.Data data) {
        Timber.tag(TAG).v("New Cap onCrankRevsData " + data.getAccumulatedCrankRevs());
        updateVal(PWahooBlueSCHolder.SensorType.CRANK, data.getAccumulatedCrankRevs(), data.getCrankSpeed().asRevolutionsPerMinute());
    }

    @Override
    public void onWheelRevsData(WheelRevs.Data data) {
        Timber.tag(TAG).v("New Cap onWheelRevsData " + data.getAccumulatedWheelRevs());
        updateVal(PWahooBlueSCHolder.SensorType.WHEEL, data.getAccumulatedWheelRevs(), data.getWheelSpeed().asRevolutionsPerMinute());
    }

    @Override
    public void onBatteryData(Battery.Data data) {
        Timber.tag(TAG).v("New Cap onBatteryData " + data.getBatteryLevel());
        lastBattery = (byte) data.getBatteryLevel().ordinal();
        setStatusVar(".batterylev", lastBattery);
    }

    @Override
    public void onDeviceInfo(DeviceInfo.Type type, String s) {
        Timber.tag(TAG).v("New Cap onDeviceInfo " + type.toString() + " = " + s);
        infoMap.put(type.toString(), s);
        setDeviceDescription(infoMap, "Wahoo");
    }

    @Override
    public void onFirmwareVersion(String s) {
        Timber.tag(TAG).v("New Cap onFirmwareVersion " + s);
        infoMap.put(DeviceInfo.Type.FIRMWARE_REVISION.toString(), s);
    }

    @Override
    public void onFirmwareUpgradeRequired(String s, String s1) {
        Timber.tag(TAG).v("New Cap onFirmwareUpgradeRequired " + s + " " + s1);
    }

    public int getCurrentGear() {
        return currentGear;
    }

    public void setCurrentGear(int currentGear) {
        this.currentGear = currentGear;
    }
}
