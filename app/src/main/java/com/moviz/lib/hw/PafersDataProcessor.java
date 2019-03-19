package com.moviz.lib.hw;

import android.util.Log;

import com.moviz.lib.comunication.DeviceStatus;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.message.ProgramChangeMessage;
import com.moviz.lib.comunication.message.ProgramListMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PPafersHolder;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.comunication.plus.message.DeviceChangeRequestMessage;
import com.moviz.lib.comunication.plus.message.ProcessedOKMessage;
import com.moviz.lib.comunication.tcp.TCPMessageTypes;
import com.moviz.lib.program.ProgramParser;
import com.moviz.lib.utils.ParcelableMessage;
import com.moviz.lib.utils.UnitUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Fujitsu on 02/11/2016.
 */

public class PafersDataProcessor extends BluetoothChatDataProcessor<PafersDevice.Message> {
    private String mDeviceBrand = "";
    private String mDeviceManufacturer = "";
    private Timer keepAliveTimer = null;

    private Random randomV = new Random();
    private byte mDeviceType = DEVICE_TYPE_TREDMIL;
    private byte mDeviceUnit;
    private byte currentActionMode = ACTION_MODE_INVALID;
    private int currentIncline = 1;
    private int currentTargetWatt = 0;
    private int currentTargetPulse = 0;
    private boolean startNotified = false;

    private String programFile = "", programFold = "";
    private long programDelay = 0;


    public static final byte DEVICE_TYPE_BIKE = 0;
    public static final byte DEVICE_TYPE_ELLIPTICAL = 1;
    public static final byte DEVICE_TYPE_TREDMIL = 2;

    public static final byte DEVICE_UNIT_METRIC = 0;

    private static final byte ACTION_MODE_MANUAL_INCLINE = 0;
    private static final byte ACTION_MODE_HRC_INCLINE = 1;
    private static final byte ACTION_MODE_WATT_INCLINE = 2;
    private static final byte ACTION_MODE_INVALID = 10;

    public static final int START_NOTIFICATION_TH = 2;

    private ProgramParser program;

    public PafersDataProcessor() {
        this.program = new ProgramParser(null, this, null);
    }

    @Override
    public void initStatusVars(PHolderSetter statusVars) {
        super.initStatusVars(statusVars);
        statusVars.add(new PHolder(PHolder.Type.STRING, "status.program"));
    }


    protected void initMessages() {
        messages = new byte[][]{
                {0x0D},
                {0x0C, 0x01},
                {(byte) 0xBB, 0x00},
                {(byte) 0xBB, 0x01},
                {0x1F, 0x04},
                {0x09, 0x01, 0x00},
                {0x09, 0x01, 0x01},
                {0x09, 0x01, 0x02},
                {0x0B, 0x01, 0x01},
                {(byte) 0xB5, 0x08},
                {(byte) 0xB9, 0x14}
        };
        messageValues = PafersDevice.Message.values();
    }

    @Override
    public BaseMessage processCommand(BaseMessage hs) {
        byte type = hs.getType();
        if (type == TCPMessageTypes.UPDOWN_MESSAGE) {
            if (isConnected()) {
                int val = ((com.moviz.lib.comunication.message.UpDownMessage) hs).getHow();
                if (val > 0) {
                    up(val);
                } else if (val < 0) {
                    down(val);
                }
            }
        } else if (type == TCPMessageTypes.START_MESSAGE) {
            if (isConnected())
                start();
        } else if (type == TCPMessageTypes.PAUSE_MESSAGE) {
            if (isConnected())
                pause();
        } else if (type == TCPMessageTypes.PROGRAMCHANGE_MESSAGE) {
            String prg = ((ProgramChangeMessage) hs).getProgram();
            File f = new File(programFold + "/" + prg + ProgramParser.PROGRAMFILE_EXTENSION);
            PDeviceHolder devh = mDeviceHolder.innerDevice();
            Map<String,String> adds = devh.deserializeAdditionalSettings();
            adds.put("pfile", f.getPath());
            devh.serializeAdditionalSettings(adds,false);
            if (f.isFile() && f.exists() && f.canRead()) {
                return new DeviceChangeRequestMessage(devh, "pfile", f.getPath());
            }
        } else if (type == TCPMessageTypes.PROGRAMLISTREQUEST_MESSAGE) {
            String[] list = ProgramParser.list(programFold);
            String rv = "";
            for (String l : list) {
                if (!rv.isEmpty())
                    rv += com.moviz.lib.comunication.ComunicationConstants.LIST_SEPARATOR;
                rv += l.substring(0, l.length() - ProgramParser.PROGRAMFILE_EXTENSION.length());
            }
            return new ProgramListMessage(rv);
        } else
            return null;
        return new ProcessedOKMessage();
    }

    @Override
    protected int processMessage(PafersDevice.Message msg, byte[] buffer, int ret,
                                 int length) {
        int bptr = ret;
        DeviceStatus mDeviceState = getDeviceState();
        // int length = buffer.length;
        if (msg == PafersDevice.Message.MSG_NEWDATA) {
            if (isBike()) {
                if (length >= bptr + 13 + 3) {
                    ByteBuffer bb = ByteBuffer.wrap(buffer, bptr + 3, 13);

                    PPafersHolder statistic = new PPafersHolder();

                    statistic.time = bb.getShort();
                    byte distanceX = bb.get();
                    byte distanceY = bb.get();
                    statistic.calorie = bb.getShort();
                    byte speedX = bb.get();
                    byte speedY = bb.get();
                    statistic.incline = bb.get();
                    statistic.pulse = (bb.get() & 0xFF);
                    statistic.rpm = (bb.get() & 0xFF);
                    statistic.watt = bb.getShort();

                    statistic.distance = ((distanceX & 0xFF) + 1.0D * distanceY / 100.0D);
                    statistic.speed = ((speedX & 0xFF) + 1.0D * speedY / 100.0D);

                    currentTargetWatt = statistic.watt;
                    currentTargetPulse = statistic.pulse;
                    currentIncline = statistic.incline;
                    performUpdate(statistic);
                    return (buffer[bptr + 2] & 0xFF) + 3;
                }
            } else if (length >= bptr + 10 + 3) {
                ByteBuffer bb = ByteBuffer.wrap(buffer, bptr + 3, 10);

                PPafersHolder statistic = new PPafersHolder();

                statistic.time = bb.getShort();
                byte distanceX = bb.get();
                byte distanceY = bb.get();
                statistic.calorie = bb.getShort();
                byte speedX = bb.get();
                byte speedY = bb.get();
                statistic.incline = bb.get();
                statistic.pulse = (bb.get() & 0xFF);

                currentTargetPulse = statistic.pulse;
                currentIncline = statistic.incline;

                statistic.distance = ((distanceX & 0xFF) + 1.0D * distanceY / 100.0D);
                statistic.speed = ((speedX & 0xFF) + 1.0D * speedY / 100.0D);

                performUpdate(statistic);
                return (buffer[bptr + 2] & 0xFF) + 3;
            }
            return -1;
        } else if (msg == PafersDevice.Message.MSG_UNIT) {
            if (length >= bptr + 1 + 3) {
                ByteBuffer bb = ByteBuffer.wrap(buffer, bptr + 3, 1);
                mDeviceUnit = bb.get();
                return (buffer[bptr + 2] & 0xFF) + 3;
            } else
                return -1;
        } else if (msg == PafersDevice.Message.MSG_INVALIDMACHINETYPE) {
            queryMachineType();
            return (buffer[bptr + 2] & 0xFF) + 3;
        } else if (msg == PafersDevice.Message.MSG_TYPE) {
            if (length >= bptr + 1 + 3) {
                ByteBuffer bb = ByteBuffer.wrap(buffer, bptr + 3, 1);
                mDeviceType = bb.get();

                setDeviceState(DeviceStatus.STANDBY);

                //onConnectionSuccess();
                return (buffer[bptr + 2] & 0xFF) + 3;
            } else
                return -1;
        } else if (msg == PafersDevice.Message.MSG_SPEEDLIM) {
            if (length >= bptr + 4 + 3) {
                ByteBuffer bb = ByteBuffer.wrap(buffer, bptr + 3, 4);

                byte[] speedLimit = new byte[4];
                bb.get(speedLimit, 0, 4);

                double minSpeed = 1.0D * speedLimit[2] + 1.0D
                        * speedLimit[3] / 100.0D;
                double maxSpeed = 1.0D * speedLimit[0] + 1.0D
                        * speedLimit[1] / 100.0D;
                if (!isMetric()) {
                    minSpeed = UnitUtil.mile2km(minSpeed);
                    maxSpeed = UnitUtil.mile2km(maxSpeed);
                }
                return (buffer[bptr + 2] & 0xFF) + 3;
            } else
                return -1;
        } else if (msg == PafersDevice.Message.MSG_CHANGESTATE1) {

            if (mDeviceState != DeviceStatus.RUNNING) {
                setDeviceState(DeviceStatus.RUNNING);
            }
            return (buffer[bptr + 2] & 0xFF) + 3;
        } else if (msg == PafersDevice.Message.MSG_CHANGESTATE2) {
            if (mDeviceState != DeviceStatus.STANDBY) {
                //keyuse = false
                setDeviceState(DeviceStatus.STANDBY);
            }
            return (buffer[bptr + 2] & 0xFF) + 3;
        } else if (msg == PafersDevice.Message.MSG_KEYSTOP) {
            if (mDeviceState != DeviceStatus.STANDBY) {
                setDeviceState(DeviceStatus.STANDBY);
            }
            return (buffer[bptr + 2] & 0xFF) + 3;
        } else if (msg == PafersDevice.Message.MSG_CHANGESTATE0) {
            DeviceStatus status = mDeviceState;
            if (status != DeviceStatus.PAUSED
                    && status != DeviceStatus.DPAUSE) {
                setDeviceState(DeviceStatus.PAUSED);
            }
            return (buffer[bptr + 2] & 0xFF) + 3;
        } else if (msg == PafersDevice.Message.MSG_MANUFACTURER) {
            if (length >= 8 + 3 + bptr) {
                String res = new String(buffer, bptr + 3, 8)
                        .replaceAll("[^\\x20-\\x7f]", "")
                        .toUpperCase(Locale.US).trim();
                boolean notify = false;
                if (!res.equals(mDeviceManufacturer) && !res.isEmpty() && !mDeviceBrand.isEmpty())
                    notify = true;
                mDeviceManufacturer = res;
                if (notify)
                    postDeviceDescription(makeDeviceDescription());
                return (buffer[bptr + 2] & 0xFF) + 3;
            } else
                return -1;
        } else if (msg == PafersDevice.Message.MSG_BRAND) {
            if (length >= 20 + 3 + bptr) {
                String res = new String(buffer, bptr + 3, 20)
                        .replaceAll("[^\\x20-\\x7f]", "")
                        .toUpperCase(Locale.US).trim();
                boolean notify = false;
                if (!res.equals(mDeviceBrand) && !res.isEmpty() && !mDeviceManufacturer.isEmpty())
                    notify = true;
                if (notify)
                    postDeviceDescription(makeDeviceDescription());
                mDeviceBrand = res;
                return (buffer[bptr + 2] & 0xFF) + 3;
            } else
                return -1;
        } else
            return 0;
    }


    @Override
    public byte getMessageStart() {
        return 0x55;
    }

    private String makeDeviceDescription() {
        return mDeviceBrand + " - " + mDeviceManufacturer;
    }

    public void performUpdate(PPafersHolder statistic) {
        if (startNotified) {
            int pause = mSim.step(statistic);
            DeviceStatus status = mDeviceState;
            if (pause==DeviceSimulator.PAUSE_DETECTED && status != DeviceStatus.DPAUSE) {
                setDeviceState(DeviceStatus.DPAUSE);
            } else if (pause==DeviceSimulator.DO_NOT_POST_DU && status != DeviceStatus.RUNNING) {
                setDeviceState(DeviceStatus.RUNNING);
            }
            if (pause!=DeviceSimulator.DO_NOT_POST_DU) {
                postDeviceUpdate(statistic);
                setStatusVar(".program", program.toString());
            }
        }
    }

    public void simUpdate() {
        PPafersHolder statistic = new PPafersHolder();
        statistic.time = (short) (Math.abs(randomV.nextInt()) % 32768);
        statistic.calorie = (short) (Math.abs(randomV.nextInt()) % 32768);
        statistic.incline = (byte) (Math.abs(randomV.nextInt()) % 128);
        statistic.pulse = Math.abs(randomV.nextInt()) % 256;
        statistic.rpm = Math.abs(randomV.nextInt()) % 256;
        statistic.watt = (short) (Math.abs(randomV.nextInt()) % 32768);

        statistic.distance = Math.abs(randomV.nextDouble()) * 150.0;
        statistic.speed = Math.abs(randomV.nextDouble()) * 60.0;
        int pause = mSim.step(statistic);
        if (pause==DeviceSimulator.PAUSE_DETECTED && mDeviceState != DeviceStatus.DPAUSE) {
            setDeviceState(DeviceStatus.DPAUSE);
        }
        if (pause!=DeviceSimulator.DO_NOT_POST_DU)
            postDeviceUpdate(statistic);
        setStatusVar(".program", program.toString());
    }

    public boolean isRunning() {
        return mDeviceState == DeviceStatus.RUNNING;
    }

    public boolean isPaused() {
        return mDeviceState == DeviceStatus.PAUSED;
    }

    @Override
    public String getSessionSettings() {
        return programFile;
    }

    @Override
    public void connectTh() {
        resetcls();
        try {
            program.parse();
            super.connectTh();
        } catch (Exception e) {
            PDeviceHolder devh = mDeviceHolder.innerDevice();
            postDeviceError(
                    new ParcelableMessage("exm_errr_pafers_program")
                            .put(devh)
                            .put(programFile));
        }
    }

    private boolean sendCommand(byte[] command) {
        if (mBluetoothState != BluetoothState.CONNECTED) {
            return false;
        }
        if (command.length == 0) {
            return false;
        }
        write(command);
        return true;
    }

    private void initRemoteDevice() {
        byte[][] command = {{85, 12, 1, -1}, {85, -69, 1, -1},
                {85, 36, 1, -1}, {85, 37, 1, -1}, {85, 38, 1, -1},
                {85, 39, 1, -1}, {85, 2, 1, -1}, {85, 3, 1, -1},
                {85, 4, 1, -1}, {85, 6, 1, -1}, {85, 31, 1, -1},
                {85, -96, 1, -1}, {85, -80, 1, -1}, {85, -78, 1, -1},
                {85, -77, 1, -1}, {85, -76, 1, -1}, {85, -75, 1, -1},
                {85, -74, 1, -1}, {85, -73, 1, -1}, {85, -72, 1, -1},
                {85, -71, 1, -1}, {85, -70, 1, -1}, {85, 11, 1, -1},
                {85, 24, 1, -1}, {85, 25, 1, -1}, {85, 26, 1, -1},
                {85, 27, 1, -1}};
        for (int i = 0; i < 27; i++) {
            sendCommand(command[i]);
        }
        waitMsg(-1, 2000L);
    }

    public void setProgramParams(String pfold, String pfile, long del) {
        Log.w(TAG, "set3 " + pfile);
        programFold = pfold;
        programFile = pfile;
        programDelay = del;
        program.setPath(pfile);
        program.setStartDelay(del);
    }

    public void resetcls() {
        currentTargetWatt = 0;
        currentTargetPulse = 0;
        currentIncline = 1;
        currentActionMode = ACTION_MODE_INVALID;
        startNotified = false;
        program.setPath(programFile);
        program.setStartDelay(programDelay);
        program.setUser(mCurrentUser);
    }

    private void setActionMode(byte mode) {
        byte[] command = {85, 21, 1, mode};
        currentActionMode = mode;
        sendCommand(command);
    }

    public void queryManufacturer() {
        byte[] command = {85, -75, 1, -1};
        sendCommand(command);
    }

    @Override
    public void setDeviceState(DeviceStatus d) {
        super.setDeviceState(d);
        if (d == DeviceStatus.STANDBY)
            resetcls();
    }

    public void queryBrand() {
        byte[] command = {85, -71, 1, -1};
        sendCommand(command);
    }

    public void queryMachineType() {
        byte[] command = {85, -69, 1, -1};
        sendCommand(command);
    }

    public void up(int n) {
        updown(Math.abs(n));
    }

    public void down(int n) {
        updown(-Math.abs(n));
    }

    private void updown(int val) {
        if (val != 0) {
            if (currentActionMode == ACTION_MODE_WATT_INCLINE) {
                setTargetWatt(currentTargetWatt + val);
            } else if (currentActionMode == ACTION_MODE_HRC_INCLINE) {
                setTargetPulse(currentTargetPulse + val);
            } else if (currentActionMode == ACTION_MODE_MANUAL_INCLINE) {
                setIncline(currentIncline + val);
            }
        }
    }

    public void reset() {
        byte[] command = {85, 10, 1, 2};
        sendCommand(command);

        setDeviceState(DeviceStatus.STANDBY);
    }

    public void start() {
        startcmd();
        //onDeviceStarted();
        startNotified = true;
    }

    private void startcmd() {
        byte[] command = {85, 10, 1, 1};
        sendCommand(command);

        setDeviceState(DeviceStatus.RUNNING);
    }

    public void pause() {
        byte[] command = {85, 10, 1, 0};
        sendCommand(command);

        setDeviceState(DeviceStatus.PAUSED);

        enableStartKey();

    }

    public void stop() {
        byte[] command = {85, 10, 1, 2};
        sendCommand(command);

        setDeviceState(DeviceStatus.STANDBY);
    }

    public boolean isBike() {
        return this.mDeviceType == 0;
    }

    public boolean isElliptical() {
        return this.mDeviceType == 1;
    }

    public boolean isTreadmill() {
        return this.mDeviceType == 2;
    }

    public boolean isBikeOrElliptical() {
        return (isBike()) || (isElliptical());
    }

    public boolean isMetric() {
        return this.mDeviceUnit == 0;
    }

    public boolean isImperial() {
        return this.mDeviceUnit != 0;
    }

    public void setTargetSpeed(double speed) {
        Double speedToCode = Double.valueOf(speed);
        if (!isMetric()) {
            speedToCode = Double.valueOf(UnitUtil.km2mile(speedToCode
                    .doubleValue()));
        }
        byte speedByte1 = (byte) speedToCode.intValue();
        speedToCode = Double.valueOf((speedToCode.doubleValue() - speedToCode
                .intValue()) * 100.0D);
        byte speedByte2 = (byte) speedToCode.intValue();

        byte[] command = {85, 15, 2, speedByte1, speedByte2};
        sendCommand(command);
    }

    public void setTargetTimeDistanceAndCalorie(int time, double distance,
                                                int calorie) {
        byte timeByte = (byte) time;

        Double distanceToCode = Double.valueOf(distance);
        if (!isMetric()) {
            distanceToCode = Double.valueOf(UnitUtil.km2mile(distanceToCode
                    .doubleValue()));
        }
        byte distanceByte1 = (byte) distanceToCode.intValue();

        distanceToCode = Double
                .valueOf((distanceToCode.doubleValue() - distanceToCode
                        .intValue()) * 100.0D);
        byte distanceByte2 = (byte) distanceToCode.intValue();

        byte calorieByte1 = (byte) (calorie / 256);
        byte calorieByte2 = (byte) (calorie % 256);

        byte[] command = {85, 14, 5, timeByte, distanceByte1, distanceByte2,
                calorieByte1, calorieByte2};

        sendCommand(command);
    }

    public void setTargetPulse(int pulse) {
        if (pulse > 50 && pulse <= 255) {
            byte[] command = {85, 22, 1, (byte) pulse};
            sendCommand(command);
            currentTargetPulse = pulse;
        }
    }

    public void setTargetWatt(int watt) {
        if (watt > 0 && watt < 500) {
            byte wattByte1 = (byte) (watt / 256);
            byte wattByte2 = (byte) (watt % 256);
            byte[] command = {85, 35, 2, wattByte1, wattByte2};
            sendCommand(command);
            currentTargetWatt = watt;
        }
    }

    public void setIncline(int i) {
        int checkRangeIncline = i;
        if (isBikeOrElliptical()) {
            if (checkRangeIncline < 1) {
                i = 1;
            }
            if (checkRangeIncline > 64) {
                i = 1;
            }
        } else {
            if (checkRangeIncline < 0) {
                i = 0;
            }
            if (checkRangeIncline > 64) {
                i = 0;
            }
        }
        this.currentIncline = i;

        byte[] command = {85, 17, 1, (byte) i};
        sendCommand(command);
    }

    public void setUserData(PUserHolder user) {
        if (user == null)
            user = new PUserHolder(-1, "bobo", true, 75, 180, 30, true);
        byte gender = (byte) (user.isMale() ? 0 : 1);
        double weightToSend = user.getWeight();
        double heightToSend = user.getHeight();
        byte weightInteger = (byte) (int) Math.floor(weightToSend);
        byte weightFraction = (byte) (int) Math
                .round(100.0D * (weightToSend - weightInteger));

        byte heightInteger = (byte) (int) Math.floor(heightToSend);
        byte heightFraction = (byte) (int) Math
                .round(100.0D * (heightToSend - heightInteger));

        byte[] command = {85, 1, 6, user.getAge(), gender, weightInteger,
                weightFraction, heightInteger, heightFraction};
        sendCommand(command);
        postUser(user);
    }

    public void enableStartKey() {
        byte[] command = {85, 8, 1, 1};
        sendCommand(command);
    }

    private void sendKeepAlive(boolean keepAlive) {
        byte bytey = 0;
        if (keepAlive) {
            bytey = 1;
        }
        byte[] command = {85, 23, 1, bytey};
        sendCommand(command);
    }

    private void queryPulseType() {
        byte[] command = {85, 7, 1, -1};
        sendCommand(command);
    }

    public void startQuick(PUserHolder user) {
        startQuick(1.0D, (byte) 0, user);
    }

    /**
     * @deprecated
     */
    public void startQuick(final double speed, final int incline,
                           final PUserHolder user) {
        new Thread("TquickStart") {
            public void run() {
                if (isBikeOrElliptical()) {
                    reset();
                    waitMsg(-1, 200L);

                    setUserData(user);
                    waitMsg(-1, 200L);

                    setActionMode(ACTION_MODE_MANUAL_INCLINE);
                    waitMsg(-1, 200L);

                    setIncline(1);
                    waitMsg(-1, 200L);

                    startcmd();
                    waitMsg(-1, 200L);
                    queryPulseType();
                    waitMsg(-1, 200L);
                    //onDeviceStarted();
                } else {
                    reset();
                    waitMsg(-1, 200L);

                    setUserData(user);
                    waitMsg(-1, 200L);

                    setActionMode(ACTION_MODE_MANUAL_INCLINE);
                    waitMsg(-1, 200L);

                    setTargetSpeed(speed);
                    waitMsg(-1, 200L);

                    setIncline(incline);
                    waitMsg(-1, 200L);

                    enableStartKey();
                    waitMsg(PafersDevice.Message.MSG_CHANGESTATE1.ordinal(), 0L);
                    waitMsg(-1, 3000L);

                    startcmd();
                    waitMsg(-1, 200L);
                    queryPulseType();
                    waitMsg(-1, 200L);
                }
            }
        }.start();
    }

    public void startCustom(int time, double distance, int calorie,
                            PUserHolder user) {
        startCustom(1.0D, (byte) 0, time, distance, calorie, user);
    }

    /**
     * @deprecated
     */
    public void startCustom(final double speed, final int incline,
                            final int time, final double distance, final int calorie,
                            final PUserHolder user) {
        new Thread("customMode") {
            public void run() {
                if (isBikeOrElliptical()) {
                    reset();
                    waitMsg(-1, 200L);

                    setUserData(user);
                    waitMsg(-1, 200L);

                    setActionMode((byte) 0);
                    waitMsg(-1, 200L);

                    setIncline(1);
                    waitMsg(-1, 200L);

                    setTargetTimeDistanceAndCalorie(
                            time, distance, calorie);
                    waitMsg(-1, 200L);

                    startcmd();
                    waitMsg(-1, 200L);

                    queryPulseType();
                    waitMsg(-1, 200L);
                    //onDeviceStarted();
                } else {
                    reset();
                    waitMsg(-1, 200L);

                    setUserData(user);
                    waitMsg(-1, 200L);

                    setActionMode((byte) 0);
                    waitMsg(-1, 200L);

                    setTargetSpeed(incline);
                    waitMsg(-1, 200L);

                    setIncline(incline);
                    waitMsg(-1, 200L);

                    setTargetTimeDistanceAndCalorie(
                            time, distance, calorie);
                    waitMsg(-1, 200L);

                    enableStartKey();
                    waitMsg(PafersDevice.Message.MSG_CHANGESTATE1.ordinal(), 0L);

                    waitMsg(-1, 3000L);
                    startcmd();
                    waitMsg(-1, 200L);

                    queryPulseType();
                    waitMsg(-1, 200L);
                }
            }
        }.start();
    }

    public void startHrc(final double speed, final int incline,
                         final int pulse, final PUserHolder user) {
        new Thread("HRCMode") {
            public void run() {
                if (isBikeOrElliptical()) {
                    reset();
                    waitMsg(-1, 200L);

                    setUserData(user);
                    waitMsg(-1, 200L);

                    setActionMode(ACTION_MODE_HRC_INCLINE);
                    waitMsg(-1, 200L);

                    setTargetPulse(pulse);
                    waitMsg(-1, 200L);

                    startcmd();
                    waitMsg(-1, 200L);
                    queryPulseType();
                    waitMsg(-1, 200L);
                    //onDeviceStarted();
                } else {
                    reset();
                    waitMsg(-1, 200L);

                    setUserData(user);
                    waitMsg(-1, 200L);

                    setActionMode(ACTION_MODE_HRC_INCLINE);
                    waitMsg(-1, 200L);

                    setTargetSpeed(speed);
                    waitMsg(-1, 200L);

                    setIncline(incline);
                    waitMsg(-1, 200L);

                    setTargetPulse(pulse);
                    waitMsg(-1, 200L);

                    enableStartKey();

                    waitMsg(PafersDevice.Message.MSG_CHANGESTATE1.ordinal(), 0L);
                    waitMsg(-1, 3000L);

                    startcmd();
                    waitMsg(-1, 200L);
                    queryPulseType();
                    waitMsg(-1, 200L);
                }
            }
        }.start();
    }

    public void startWatt(final int watt, final PUserHolder user) {
        new Thread("wattMode") {
            public void run() {
                if (isBikeOrElliptical()) {
                    reset();
                    waitMsg(-1, 200L);

                    setUserData(user);
                    waitMsg(-1, 200L);

                    setActionMode(ACTION_MODE_WATT_INCLINE);
                    waitMsg(-1, 200L);

                    setTargetWatt(watt);
                    waitMsg(-1, 200L);

                    startcmd();
                    waitMsg(-1, 200L);

                    queryPulseType();
                    waitMsg(-1, 200L);
                    //onDeviceStarted();
                } else {
                    Log.w(TAG,
                            "Watt mode not support for Treadmill");
                }
            }
        }.start();
    }

    private void startKeepAlive() {
        keepAliveTimer = new Timer();
        keepAliveTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                sendKeepAlive(true);
            }
        }, 1000, 1000);
    }

    private void stopKeepAlive() {
        if (keepAliveTimer != null) {
            keepAliveTimer.cancel();
            keepAliveTimer = null;
        }
    }

    @Override
    public void onDeviceConnected(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceConnected(dev,devh);
        initRemoteDevice();
        startKeepAlive();
        program.start();
    }

    @Override
    public void onDeviceConnectionFailed(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceConnectionFailed(dev, devh);
        program.stop();
        resetcls();
    }

    @Override
    public void onDeviceDisconnected(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceDisconnected(dev,devh);
        stopKeepAlive();
        program.stop();
        resetcls();
    }

    @Override
    public void onDeviceStarted(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceStarted(dev, devh);
        if (startNotified)
            program.schedule();
    }

    @Override
    public void onDeviceResumed(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceResumed(dev, devh);
        if (startNotified)
            program.schedule();
    }

    @Override
    public void onDevicePaused(GenericDevice dev, PDeviceHolder devh) {
        super.onDevicePaused(dev,devh);
        program.pause();
    }

    @Override
    public void onDeviceStopped(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceStopped(dev, devh);
        program.stop();
    }

    public String getProgramFile() {
        return programFile;
    }

    public String getProgramFold() {
        return programFold;
    }

    public long getProgramDelay() {
        return programDelay;
    }

    @Override
    public void pushSettingsChange() {
        Map<String,String> adds = mDeviceHolder.innerDevice().deserializeAdditionalSettings();
        try {
            programDelay = Long.parseLong(adds.get("startdelay"));
        } catch (Exception e) {
            e.printStackTrace();
            programDelay = 2000;
        }
        programFold = adds.get("pfold");
        if (programFold==null)
            programFold = "";
        programFile = adds.get("pfile");
        if (programFile==null)
            programFile = "";
        Log.i(TAG,"New conf "+programFold+" "+programFile+" "+programDelay);

    }
}