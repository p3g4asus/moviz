package com.moviz.lib.comunication.holder;

import com.moviz.lib.comunication.EncDec;

import java.nio.ByteBuffer;

public class HRDeviceHolder implements EncDec, HeartUpdate {

    public long timeRAbsms = 0;
    public long timeRms = 0;
    public short timeR = 0;
    public int updateN = 0;
    public long id = -1;
    public long sessionId = -1;
    public short pulse = 0;
    public int nBeatsR = 0;
    public short joule = -1;
    public long[] timeRs = new long[15];
    public float[] rrintervals = new float[15];
    public byte worn = -1;
    public byte nintervals = 0;
    public byte battery = -1;
    public double jouleMn = -1.0;
    public double pulseMn = 0.0;

    public HRDeviceHolder() {

    }

    public HRDeviceHolder(HRDeviceHolder w) {
        copyFrom(w);
    }

    @Override
    public String toString() {
        return String.format("HR = %d NBeats = %d Battery = %d", this.pulse, this.nBeatsR, this.battery);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof HRDeviceHolder))
            return false;
        else {
            HRDeviceHolder w = (HRDeviceHolder) o;
            return id == w.id && timeR == w.timeR && timeRms == w.timeRms
                    && timeRAbsms == w.timeRAbsms
                    && sessionId == w.sessionId && pulse == w.pulse
                    && updateN == w.updateN && nBeatsR == w.nBeatsR
                    && joule == w.joule
                    && worn == w.worn && nintervals == w.nintervals
                    && battery == w.battery;
        }
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
            hld.setId(pref + "joule");
            hld.sO((short) joule);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "worn");
            hld.sO(worn);
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
            hld.setId(pref + "joulemn");
            hld.sO(jouleMn);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "timer");
            hld.sO((short) timeR);
            hld.setPrint(new STimePrinter());
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "nintervals");
            hld.sO(nintervals);
            rv.add(hld);

            for (int i = 0; i < 15; i++) {
                hld = cl.newInstance();
                hld.setId(pref + "rrintervals" + i);
                hld.sO((int) (rrintervals[i] * 1024));
                rv.add(hld);
            }
            rv.add(hld);
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
        joule = hs.get(pref + "joule").getShort();
        worn = hs.get(pref + "worn").getByte();
        nBeatsR = hs.get(pref + "nbeatsr").getInt();
        pulseMn = hs.get(pref + "pulsemn").getDouble();
        jouleMn = hs.get(pref + "joulemn").getDouble();
        timeR = hs.get(pref + "timer").getShort();
        nintervals = hs.get(pref + "nintervals").getByte();
        for (int i = 0; i < 15; i++) {
            rrintervals[i] = hs.get(pref + "rrintervals" + i).getInt() / 1024F;
        }
    }

    @Override
    public void encode(com.moviz.lib.comunication.IEncoder enc, ByteBuffer b) {
        enc.encodeInt((int) sessionId, b);
        enc.encodeByte(pulse, b);
        enc.encodeShort(joule, b);
        enc.encodeByte(worn, b);
        enc.encodeInt(nBeatsR, b);
        enc.encodeDouble(pulseMn, b);
        enc.encodeDouble(jouleMn, b);
        enc.encodeShort(timeR, b);
    }

    @Override
    public int eSize(com.moviz.lib.comunication.IEncoder enc) {
        return enc.getIntSize() + //sessionId
                enc.getByteSize() +//pulse
                enc.getShortSize() +//joule
                enc.getByteSize() +//worn
                enc.getIntSize() +//nBeatsR
                enc.getDoubleSize() +//pulseMn
                enc.getDoubleSize() +//jouleMn
                enc.getShortSize();//timer
    }

    @Override
    public EncDec decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer b) {
        sessionId = dec.decodeInt(b);
        pulse = (short) dec.decodeByte(b);
        joule = (short) dec.decodeShort(b);
        worn = (byte) dec.decodeByte(b);
        nBeatsR = dec.decodeInt(b);
        pulseMn = dec.decodeDouble(b);
        jouleMn = dec.decodeDouble(b);
        timeR = (short) dec.decodeShort(b);
        return this;
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
    public void copyFrom(com.moviz.lib.comunication.holder.DeviceUpdate u) {
        HRDeviceHolder w = (HRDeviceHolder) u;
        id = w.id;
        timeR = w.timeR;
        timeRms = w.timeRms;
        timeRAbsms = w.timeRAbsms;
        sessionId = w.sessionId;
        pulse = w.pulse;
        joule = w.joule;
        worn = w.worn;
        nBeatsR = w.nBeatsR;
        pulseMn = w.pulseMn;
        jouleMn = w.jouleMn;
        nintervals = w.nintervals;
        battery = w.battery;
        for (int i = 0; i < 15; i++) {
            rrintervals[i] = w.rrintervals[i];
        }
        updateN = w.updateN;
    }

    @Override
    public long getSessionId() {
        // TODO Auto-generated method stub
        return sessionId;
    }

    @Override
    public void setSessionId(long i) {
        sessionId = i;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
