package com.moviz.lib.hw;

import com.moviz.lib.comunication.DeviceStatus;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PZephyrHxMHolder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Matteo on 13/11/2016.
 */

public class ZephyrHxMDataProcessor extends BluetoothChatDataProcessor<ZephyrHxMDevice.Message> {
    private String deviceDescription = null;


    @Override
    protected void initStatusVars(PHolderSetter statusVars) {
        super.initStatusVars(statusVars);
        statusVars.add(new PHolder(PHolder.Type.BYTE, "status.battery"));
    }

    @Override
    public void onDeviceConnected(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceConnected(dev,devh);
        deviceDescription = null;
    }

    @Override
    public byte getMessageStart() {
        return 0x02;
    }

    protected void initMessages() {
        messages = new byte[][]{
                {0x26, 0x37}
        };
        messageValues = ZephyrHxMDevice.Message.values();
    }

    public ZephyrHxMDataProcessor() {
    }

    private byte crc8PushByte(int crc, int ch) {
        byte i;
        crc = (crc ^ ch);
        for (i = 0; i < 8; i++) {
            if ((crc & 1) != 0) {
                crc = ((crc >> 1) ^ 0x8C);
            } else {
                crc = (crc >> 1);
            }
        }
        return (byte) (crc & 0xFF);
    }

    private byte crc8PushBlock(byte[] block, int start, int count) {
        byte crc = 0;
        int i = 0;
        for (; i < count; i++) {
            crc = crc8PushByte(crc & 0xFF, block[start + i] & 0xFF);
        }
        return crc;
    }

    @Override
    protected int processMessage(ZephyrHxMDevice.Message msg, byte[] buffer, int ret,
                                 int length) {
        int bptr = ret;
        if (msg == ZephyrHxMDevice.Message.MSG_NEWDATA) {
            if (length >= bptr + 60) {
                if (buffer[bptr + 59] == 3) {
                    if (crc8PushBlock(buffer, bptr + 3, 55) == buffer[bptr + 58]) {
                        setDeviceState(DeviceStatus.RUNNING);
                        ByteBuffer bb = ByteBuffer.wrap(buffer, bptr + 3, 55);
                        bb.order(ByteOrder.LITTLE_ENDIAN);
                        PZephyrHxMHolder w = new PZephyrHxMHolder();
                        w.firmwareID = bb.getShort();
                        w.firmwareVersion[0] = (char) (bb.get() & 0xFF);
                        w.firmwareVersion[1] = (char) (bb.get() & 0xFF);
                        w.hardwareID = bb.getShort();
                        w.hardwareVersion[0] = (char) (bb.get() & 0xFF);
                        w.hardwareVersion[1] = (char) (bb.get() & 0xFF);
                        w.battery = bb.get();
                        w.pulse = (short) (bb.get() & 0xFF);
                        w.heartBeat = (short) (bb.get() & 0xFF);
                        for (int i = 0; i < w.ts.length; i++) {
                            w.ts[i] = bb.getShort() & 0xFFFF;
                        }
                        bb.position(bb.position() + 6);
                        w.rawDistance = (short) (bb.getShort() & 0xFFFF);
                        w.distance = ((double) (w.rawDistance)) / 16.0;
                        w.speed = ((double) (bb.getShort() & 0xFFFF)) / 256.0 / 1000.0 * 3600.0;
                        w.strides = (short) (bb.get() & 0xFF);
                        if (deviceDescription == null) {
                            //notifyDeviceConnected();
                            setDeviceDescription(makeDeviceDescription(w));
                        }
                        if (!mSim.step(w)) {
                            setDeviceState(DeviceStatus.RUNNING);
                            setStatusVar(".battery", w.battery);
                            postDeviceUpdate(w);
                        } else
                            setDeviceState(DeviceStatus.PAUSED);
                        return (buffer[bptr + 2] & 0xFF) + 5;
                    } else
                        return 0;
                } else
                    return 0;
            } else
                return -1;
        }
        return 0;
    }

    private String makeDeviceDescription(PZephyrHxMHolder w) {
        deviceDescription = String.format("FW 9500.%04d.V%c%c - ", w.firmwareID, w.firmwareVersion[0], w.firmwareVersion[1]);
        deviceDescription += String.format("HW 9800.%04d.V%c%c", w.hardwareID, w.hardwareVersion[0], w.hardwareVersion[1]);
        return deviceDescription;
    }

}
