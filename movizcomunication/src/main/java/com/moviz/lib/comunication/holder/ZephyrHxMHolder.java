package com.moviz.lib.comunication.holder;

import com.moviz.lib.comunication.EncDec;

import java.nio.ByteBuffer;

public class ZephyrHxMHolder implements EncDec, HeartUpdate {
    public short firmwareID = 0;
    public char[] firmwareVersion = new char[]{'0', '0'};
    public short hardwareID = 0;
    public char[] hardwareVersion = new char[]{'0', '0'};
    public byte battery = 0;
    public short pulse = 0;
    public short heartBeat = 0;
    public byte okts = 0;
    public int[] ts = new int[15];
    public int[] tsR = new int[15];
    public double distance = 0.0;
    public double distanceR = 0.0;
    public short rawDistance = 0;
    public double speed = 0.0;
    public short strides = 0;
    public int stridesR = 0;
    public int nBeatsR = 0;
    public long timeRms = 0;
    public long timeRAbsms = 0;
    public short timeR = 0;
    public int updateN = 0;
    public double speedMn = 0.0;
    public double pulseMn = 0.0;
    public long id = -1;
    public long sessionId = -1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public ZephyrHxMHolder() {

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ZephyrHxMHolder))
            return false;
        else {
            ZephyrHxMHolder w = (ZephyrHxMHolder) o;
            return firmwareID == w.firmwareID &&
                    hardwareID == w.hardwareID &&
                    battery == w.battery &&
                    pulse == w.pulse &&
                    heartBeat == w.heartBeat &&
                    okts == w.okts &&
                    distance == w.distance &&
                    distanceR == w.distanceR &&
                    rawDistance == w.rawDistance &&
                    speed == w.speed &&
                    strides == w.strides &&
                    stridesR == w.stridesR &&
                    nBeatsR == w.nBeatsR &&
                    timeRms == w.timeRms &&
                    timeRAbsms == w.timeRAbsms &&
                    timeR == w.timeR &&
                    speedMn == w.speedMn &&
                    pulseMn == w.pulseMn &&
                    id == w.id &&
                    sessionId == w.sessionId &&
                    updateN == w.updateN;
        }
    }

    public void copyFrom(com.moviz.lib.comunication.holder.DeviceUpdate u) {
        ZephyrHxMHolder w = (ZephyrHxMHolder) u;
        firmwareID = w.firmwareID;
        System.arraycopy(w.firmwareVersion, 0, firmwareVersion, 0, hardwareVersion.length);
        hardwareID = w.hardwareID;
        System.arraycopy(w.hardwareVersion, 0, hardwareVersion, 0, hardwareVersion.length);
        battery = w.battery;
        pulse = w.pulse;
        heartBeat = w.heartBeat;
        okts = w.okts;
        System.arraycopy(w.ts, 0, ts, 0, ts.length);
        System.arraycopy(w.tsR, 0, tsR, 0, tsR.length);
        distance = w.distance;
        distanceR = w.distanceR;
        rawDistance = w.rawDistance;
        speed = w.speed;
        strides = w.strides;
        stridesR = w.stridesR;
        nBeatsR = w.nBeatsR;
        timeRms = w.timeRms;
        timeRAbsms = w.timeRAbsms;
        updateN = w.updateN;
        timeR = w.timeR;
        speedMn = w.speedMn;
        pulseMn = w.pulseMn;
        id = w.id;
        sessionId = w.sessionId;
    }

    public ZephyrHxMHolder(ZephyrHxMHolder w) {
        copyFrom(w);
    }

    @Override
    public String toString() {
        return String.format("HR = %d Dist = %.2f Spd = %.2f Str = %d HB = %d Bat = %d%% Fir=%04d.%c%c Har=%d.%c%c",
                this.pulse,
                this.distance,
                this.speed,
                this.strides,
                this.heartBeat,
                this.battery,
                this.firmwareID, this.firmwareVersion[0], this.firmwareVersion[1],
                this.hardwareID, this.hardwareVersion[0], this.hardwareVersion[1]);
    }

    @Override
    public long getAbsTs() {
        return timeRAbsms;
    }

    @Override
    public long getTs() {
        return timeRms;
    }

    @Override
    public int getUpdateN() {
        return updateN;
    }

    @Override
    public void encode(com.moviz.lib.comunication.IEncoder enc, ByteBuffer b) {
        enc.encodeInt((int) sessionId, b);
        enc.encodeByte(pulse, b);
        enc.encodeDouble(distanceR, b);
        enc.encodeDouble(speed, b);
        enc.encodeInt(stridesR, b);
        enc.encodeInt(nBeatsR, b);
        enc.encodeDouble(pulseMn, b);
        enc.encodeDouble(speedMn, b);
        enc.encodeShort(timeR, b);
        enc.encodeByte(okts, b);
        for (int i = 0; i < 15; i++) {
            enc.encodeInt(tsR[i], b);
        }
    }

    //nello status
    //enc.getShortSize()+//firmwareID
    //enc.getByteSize()+//firmwareVersion[0]
    //enc.getByteSize()+//firmwareVersion[1]
    //enc.getByteSize()+//battery
    //enc.getIntSize()+//updateN

    @Override
    public int eSize(com.moviz.lib.comunication.IEncoder enc) {

        return enc.getIntSize() + //sessionId
                enc.getByteSize() +//pulse
                enc.getDoubleSize() +//distanceR
                enc.getDoubleSize() +//speed
                enc.getIntSize() +//stridesR
                enc.getIntSize() +//nBeatsR
                enc.getDoubleSize() +//pulseM
                enc.getDoubleSize() +//speedM;
                enc.getShortSize() +//timeR
                enc.getByteSize() +//okts
                enc.getIntSize() * 15;//tsR[]
    }

    @Override
    public EncDec decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer b) {
        sessionId = dec.decodeInt(b);
        pulse = (short) dec.decodeByte(b);
        distanceR = dec.decodeDouble(b);
        speed = dec.decodeDouble(b);
        stridesR = dec.decodeInt(b);
        nBeatsR = dec.decodeInt(b);
        pulseMn = dec.decodeDouble(b);
        speedMn = dec.decodeDouble(b);
        timeR = (short) dec.decodeShort(b);
        okts = (byte) dec.decodeByte(b);
        for (int i = 0; i < 15; i++) {
            tsR[i] = dec.decodeInt(b);
        }
        return this;
    }

    @Override
    public com.moviz.lib.comunication.holder.HolderSetter toHolder(Class<? extends com.moviz.lib.comunication.holder.Holder> cl,
                                                                   Class<? extends com.moviz.lib.comunication.holder.HolderSetter> cllist, String pref) {
        com.moviz.lib.comunication.holder.HolderSetter rv = null;
        try {
            rv = cllist.newInstance();
            com.moviz.lib.comunication.holder.Holder hld = cl.newInstance();
            hld.setId(pref + "sessionid");
            hld.sO(sessionId);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "pulse");
            hld.sO((short) pulse);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "distancer");
            hld.sO(distanceR);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "speed");
            hld.sO(speed);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "stridesr");
            hld.sO(stridesR);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "nbeatsr");
            hld.sO(nBeatsR);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "pulsemn");
            hld.sO(pulseMn);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "speedmn");
            hld.sO(speedMn);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "timer");
            hld.sO((short) timeR);
            hld.setPrint(new STimePrinter());
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "okts");
            hld.sO((byte) okts);
            rv.add(hld);

            for (int i = 0; i < 15; i++) {
                hld = cl.newInstance();
                hld.setId(pref + "tsr" + i);
                hld.sO(tsR[i]);
                rv.add(hld);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rv;
    }

    @Override
    public void fromHolder(com.moviz.lib.comunication.holder.HolderSetter hs, String pref) {
        sessionId = hs.get(pref + "sessionid").getLong();
        pulse = hs.get(pref + "pulse").getShort();
        distanceR = hs.get(pref + "distancer").getDouble();
        speed = hs.get(pref + "speed").getDouble();
        stridesR = hs.get(pref + "stridesr").getInt();
        nBeatsR = hs.get(pref + "nbeatsr").getInt();
        pulseMn = hs.get(pref + "pulsemn").getDouble();
        speedMn = hs.get(pref + "speedmn").getDouble();
        timeR = hs.get(pref + "timer").getShort();
        okts = hs.get(pref + "okts").getByte();

        for (int i = 0; i < 15; i++) {
            tsR[i] = hs.get(pref + "tsr" + i).getInt();
        }
    }

    @Override
    public int getPulse() {
        return pulse;
    }

    @Override
    public void adjustAbsTs(long d) {
        timeRAbsms += d;
    }
}
