package com.moviz.lib.comunication.holder;

import com.moviz.lib.comunication.EncDec;

import java.nio.ByteBuffer;

public class PafersHolder implements EncDec, HeartUpdate {
    public double distance;
    public double speed;
    public short time;
    public short calorie;
    public short watt;
    public byte incline;
    public int pulse;
    public int rpm;
    public double distanceR;
    public short timeR;
    public double wattMn;
    public double speedMn;
    public double pulseMn;
    public double rpmMn;
    public int updateN;
    public long timeRms;
    public long timeRAbsms;
    public long id;
    public long sessionId;

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

    public PafersHolder() {
        this.distance = 0.0D;
        this.speed = 0.0D;
        this.time = 0;
        this.calorie = 0;
        this.watt = 0;
        this.incline = 0;
        this.pulse = 0;
        this.rpm = 0;
        this.distanceR = 0.0;
        this.timeR = 0;
        this.timeRAbsms = 0;
        this.timeRms = 0;
        this.wattMn = 0.0;
        this.speedMn = 0.0;
        this.pulseMn = 0.0;
        this.rpmMn = 0.0;
        this.updateN = 0;
        this.id = -1;
        this.sessionId = -1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof PafersHolder))
            return false;
        else {
            PafersHolder w = (PafersHolder) o;
            return distance == w.distance &&
                    speed == w.speed &&
                    time == w.time &&
                    calorie == w.calorie &&
                    watt == w.watt &&
                    incline == w.incline &&
                    pulse == w.pulse &&
                    rpm == w.rpm &&
                    distanceR == w.distanceR &&
                    timeR == w.timeR &&
                    timeRms == w.timeRms &&
                    timeRAbsms == w.timeRAbsms &&
                    wattMn == w.wattMn &&
                    speedMn == w.speedMn &&
                    pulseMn == w.pulseMn &&
                    rpmMn == w.rpmMn &&
                    updateN == w.updateN &&
                    id == w.id &&
                    sessionId == w.sessionId;
        }
    }

    public void copyFrom(com.moviz.lib.comunication.holder.DeviceUpdate u) {
        PafersHolder w = (PafersHolder) u;
        distance = w.distance;
        speed = w.speed;
        time = w.time;
        calorie = w.calorie;
        watt = w.watt;
        incline = w.incline;
        pulse = w.pulse;
        rpm = w.rpm;
        distanceR = w.distanceR;
        timeR = w.timeR;
        timeRms = w.timeRms;
        timeRAbsms = w.timeRAbsms;
        wattMn = w.wattMn;
        speedMn = w.speedMn;
        pulseMn = w.pulseMn;
        rpmMn = w.rpmMn;
        updateN = w.updateN;
        id = w.id;
        sessionId = w.sessionId;
    }

    public PafersHolder(PafersHolder w) {
        copyFrom(w);
    }

    @Override
    public String toString() {
        return String.format("Dist = %.2f Spd = %.2f tim = %d cal = %d, wat = %d, inc = %d, pul = %d, rpm = %d",
                this.distance,
                this.speed,
                this.time,
                this.calorie,
                this.watt,
                this.incline,
                this.pulse,
                this.rpm);
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
        enc.encodeByte(incline, b);
        enc.encodeByte((byte) pulse, b);
        enc.encodeByte((byte) rpm, b);
        enc.encodeShort(watt, b);
        enc.encodeShort(time, b);
        enc.encodeShort(timeR, b);
        enc.encodeShort(calorie, b);
        enc.encodeDouble(distance, b);
        enc.encodeDouble(distanceR, b);
        enc.encodeDouble(speed, b);
        enc.encodeDouble(pulseMn, b);
        enc.encodeDouble(rpmMn, b);
        enc.encodeDouble(wattMn, b);
        enc.encodeDouble(speedMn, b);
    }

    @Override
    public int eSize(com.moviz.lib.comunication.IEncoder enc) {
        // TODO Auto-generated method stub
        return enc.getIntSize() + //sessionId
                enc.getByteSize() +//incline
                enc.getByteSize() +//pulse
                enc.getByteSize() +//rpm
                enc.getShortSize() +//watt
                enc.getShortSize() +//time
                enc.getShortSize() +//timeR
                enc.getShortSize() +//calories
                enc.getDoubleSize() +//distance
                enc.getDoubleSize() +//distanceR
                enc.getDoubleSize() +//speed
                enc.getDoubleSize() +//pulseM
                enc.getDoubleSize() +//rpmM
                enc.getDoubleSize() +//wattM
                enc.getDoubleSize();//speedM
    }

    @Override
    public EncDec decode(com.moviz.lib.comunication.IDecoder d, ByteBuffer b) {
        sessionId = d.decodeInt(b);
        incline = (byte) d.decodeByte(b);
        pulse = d.decodeByte(b);
        rpm = d.decodeByte(b);
        watt = (short) d.decodeShort(b);
        time = (short) d.decodeShort(b);
        timeR = (short) d.decodeShort(b);
        calorie = (short) d.decodeShort(b);
        distance = d.decodeDouble(b);
        distanceR = d.decodeDouble(b);
        speed = d.decodeDouble(b);
        pulseMn = d.decodeDouble(b);
        rpmMn = d.decodeDouble(b);
        wattMn = d.decodeDouble(b);
        speedMn = d.decodeDouble(b);
        return this;
    }

    @Override
    public com.moviz.lib.comunication.holder.HolderSetter toHolder(Class<? extends com.moviz.lib.comunication.holder.Holder> cl, Class<? extends com.moviz.lib.comunication.holder.HolderSetter> cllist, String pref) {
        com.moviz.lib.comunication.holder.HolderSetter rv = null;
        try {
            rv = cllist.newInstance();
            com.moviz.lib.comunication.holder.Holder hld = cl.newInstance();
            hld.setId(pref + "sessionid");
            hld.sO(sessionId);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "incline");
            hld.sO(incline);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "pulse");
            hld.sO((short) pulse);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "rpm");
            hld.sO((short) rpm);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "watt");
            hld.sO((short) watt);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "time");
            hld.sO((short) time);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "timer");
            hld.sO((short) timeR);
            hld.setPrint(new STimePrinter());
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "calorie");
            hld.sO((short) calorie);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "distance");
            hld.sO(distance);
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
            hld.setId(pref + "pulsemn");
            hld.sO(pulseMn);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "rpmmn");
            hld.sO(rpmMn);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "wattmn");
            hld.sO(wattMn);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "speedmn");
            hld.sO(speedMn);
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
        incline = hs.get(pref + "incline").getByte();
        pulse = hs.get(pref + "pulse").getInt();
        rpm = hs.get(pref + "rpm").getInt();
        watt = hs.get(pref + "watt").getShort();
        time = hs.get(pref + "time").getShort();
        timeR = hs.get(pref + "timer").getShort();
        calorie = hs.get(pref + "calorie").getShort();
        distance = hs.get(pref + "distance").getDouble();
        distanceR = hs.get(pref + "distancer").getDouble();
        speed = hs.get(pref + "speed").getDouble();
        pulseMn = hs.get(pref + "pulsemn").getDouble();
        rpmMn = hs.get(pref + "rpmmn").getDouble();
        wattMn = hs.get(pref + "wattmn").getDouble();
        speedMn = hs.get(pref + "speedmn").getDouble();
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