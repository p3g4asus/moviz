package com.moviz.lib.comunication.holder;

import com.moviz.lib.comunication.EncDec;
import com.moviz.lib.comunication.IDecoder;
import com.moviz.lib.comunication.IEncoder;

import java.nio.ByteBuffer;

/**
 * Created by Matteo on 29/10/2016.
 */

public class WahooBlueSCHolder implements EncDec, DeviceUpdate {
    public short calorie = 0;
    public double distance = 0.0;
    public long id = -1;
    public long timeRAbsms;
    public long timeRms;
    public short timeR;
    public int updateN;
    public long sessionId;
    public long sensVal;
    public double sensSpd, sensSpdMn, sensSpdMnR;
    public SensorType sensType = SensorType.CRANK;
    public double speedKmHmn;
    public int gear;
    public double speedKmH;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public enum SensorType {
        CRANK, WHEEL
    }

    @Override
    public String toString() {
        return String.format("Time = %d [%s] Val = %d Spd = %f SpdM = %f, SpdMR = %f",
                this.timeR,
                this.sensType.toString(),
                this.sensVal,
                this.sensSpd,
                this.sensSpdMn,
                this.sensSpdMnR);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof WahooBlueSCHolder))
            return false;
        else {
            WahooBlueSCHolder w = (WahooBlueSCHolder) o;
            return sensSpdMn == w.sensSpdMn &&
                    sensSpdMnR == w.sensSpdMnR &&
                    sensSpd == w.sensSpd &&
                    sensVal == w.sensVal &&
                    timeR == w.timeR &&
                    timeRms == w.timeRms &&
                    timeRAbsms == w.timeRAbsms &&
                    updateN == w.updateN &&
                    id == w.id &&
                    calorie == w.calorie &&
                    distance == w.distance &&
                    sensType == w.sensType &&
                    sessionId == w.sessionId;
        }
    }

    public WahooBlueSCHolder() {
        this.sensType = SensorType.CRANK;
        this.id = -1;
        this.calorie = 0;
        this.distance = 0.0;
        this.timeRAbsms = 0;
        this.timeRms = 0;
        this.updateN = 0;
        this.sensVal = 0;
        this.sensSpd = -1.0;
        this.sensSpdMn = 0.0;
        this.sensSpdMnR = 0.0;
        this.sessionId = -1;
        this.timeR = 0;
        this.gear = 0;
        this.speedKmH = 0.0;
        this.speedKmHmn = 0.0;
    }


    @Override
    public void encode(IEncoder enc, ByteBuffer bb) {
        enc.encodeByte(sensType.ordinal(), bb);
        enc.encodeInt((int) sensVal, bb);
        enc.encodeDouble(sensSpd, bb);
        enc.encodeDouble(sensSpdMn, bb);
        enc.encodeDouble(sensSpdMnR, bb);
        enc.encodeShort(calorie, bb);
        enc.encodeShort(timeR, bb);
        enc.encodeInt((int) sessionId, bb);
        enc.encodeInt((int) id, bb);
        enc.encodeDouble(distance, bb);
        enc.encodeDouble(speedKmH, bb);
        enc.encodeDouble(speedKmHmn, bb);
        enc.encodeByte(gear, bb);
    }

    @Override
    public int eSize(IEncoder enc) {
        return enc.getByteSize() +//sensType
                enc.getIntSize() +//sensVal
                enc.getDoubleSize() +//sensSpd
                enc.getDoubleSize() +//sensSpdMn
                enc.getDoubleSize() +//sensSpdMnR
                enc.getShortSize() + //calorie
                enc.getShortSize() + //timeR
                enc.getIntSize() + //sessionId
                enc.getIntSize() + //id
                enc.getDoubleSize() + //distance
                enc.getDoubleSize() + //speedKmH
                enc.getDoubleSize() + //speedKmHmn
                enc.getByteSize(); //gear
    }

    @Override
    public EncDec decode(IDecoder dec, ByteBuffer bb) {
        sensType = SensorType.values()[dec.decodeByte(bb)];
        sensVal = dec.decodeInt(bb);
        sensSpd = dec.decodeDouble(bb);
        sensSpdMn = dec.decodeDouble(bb);
        sensSpdMnR = dec.decodeDouble(bb);
        calorie = (short) dec.decodeShort(bb);
        timeR = (short) dec.decodeShort(bb);
        sessionId = dec.decodeInt(bb);
        id = dec.decodeInt(bb);
        distance = dec.decodeDouble(bb);
        speedKmH = dec.decodeDouble(bb);
        speedKmHmn = dec.decodeDouble(bb);
        gear = dec.decodeByte(bb);
        return this;
    }

    @Override
    public void adjustAbsTs(long d) {
        timeRAbsms += d;
    }

    public WahooBlueSCHolder(WahooBlueSCHolder w) {
        copyFrom(w);
    }

    @Override
    public void copyFrom(DeviceUpdate u) {
        WahooBlueSCHolder w = (WahooBlueSCHolder) u;
        sensType = w.sensType;
        timeRAbsms = w.timeRAbsms;
        timeRms = w.timeRms;
        updateN = w.updateN;
        sensVal = w.sensVal;
        sensSpd = w.sensSpd;
        sensSpdMn = w.sensSpdMn;
        sensSpdMnR = w.sensSpdMnR;
        sessionId = w.sessionId;
        timeR = w.timeR;
        calorie = w.calorie;
        distance = w.distance;
        id = w.id;
        speedKmH = w.speedKmH;
        speedKmHmn = w.speedKmHmn;
        gear = w.gear;
    }

    @Override
    public long getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public HolderSetter toHolder(Class<? extends Holder> cl, Class<? extends HolderSetter> cllist, String pref) {
        HolderSetter rv = null;
        try {

            rv = cllist.newInstance();
            Holder hld = cl.newInstance();
            hld.setId(pref + "senstype");
            hld.sO((byte) sensType.ordinal());
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + (sensType==SensorType.CRANK?"crankn":"wheeln"));
            hld.sO(sensVal);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + (sensType==SensorType.CRANK?"crankspeed":"wheelspeed"));
            hld.sO(sensSpd);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + (sensType==SensorType.CRANK?"crankspeedmno":"wheelspeedmno"));
            hld.sO(sensSpdMn);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + (sensType==SensorType.CRANK?"crankspeedmn":"wheelspeedmn"));
            hld.sO(sensSpdMnR);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + (sensType==SensorType.CRANK?"crankcalorie":"wheelcalorie"));
            hld.sO(calorie);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "timer");
            hld.sO(timeR);
            hld.setPrint(new STimePrinter());
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "sessionid");
            hld.sO(sessionId);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "id");
            hld.sO(id);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + (sensType==SensorType.CRANK?"crankdistance":"wheeldistance"));
            hld.sO(distance);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + (sensType==SensorType.CRANK?"crankspeedkmh":"wheelspeedkmh"));
            hld.sO(speedKmH);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + (sensType==SensorType.CRANK?"crankspeedkmhmn":"wheelspeedkmhmn"));
            hld.sO(speedKmHmn);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "gear");
            hld.sO((byte) gear);
            rv.add(hld);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rv;
    }

    @Override
    public void fromHolder(HolderSetter hs, String pref) {
        sensType = SensorType.values()[hs.get(pref + "senstype").getByte()];
        sensVal = hs.get(pref + (sensType==SensorType.CRANK?"crankn":"wheeln")).getInt();
        sensSpd = hs.get(pref + (sensType==SensorType.CRANK?"crankspeed":"wheelspeed")).getDouble();
        sensSpdMn = hs.get(pref + (sensType==SensorType.CRANK?"crankspeedmno":"wheelspeedmno")).getDouble();
        sensSpdMnR = hs.get(pref + (sensType==SensorType.CRANK?"crankspeedmn":"wheelspeedmn")).getDouble();
        calorie = hs.get(pref + (sensType==SensorType.CRANK?"crankcalorie":"wheelcalorie")).getShort();
        timeR = hs.get(pref + "timer").getShort();
        sessionId = hs.get(pref + "sessionid").getLong();
        id = hs.get(pref + "id").getLong();
        distance = hs.get(pref + (sensType==SensorType.CRANK?"ckankdistance":"wheeldistance")).getDouble();
        speedKmH = hs.get(pref + (sensType==SensorType.CRANK?"crankspeedkmh":"wheelspeedkmh")).getDouble();
        speedKmHmn = hs.get(pref + (sensType==SensorType.CRANK?"crankspeedkmhmn":"wheelspeedkmhmn")).getDouble();
        gear = hs.get(pref + "gear").getByte();
    }
}
