package com.moviz.lib.comunication.plus.holder;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.googlefit.GoogleFitPointTransformer;
import com.moviz.lib.googlefit.HeartFitTransformer;
import com.moviz.lib.googlefit.HeartInstFitTransformer;

public class PZephyrHxMHolder extends com.moviz.lib.comunication.holder.ZephyrHxMHolder implements Parcelable, UpdateDatabasable, Joinable {
    private static HeartFitTransformer transformer = new HeartInstFitTransformer() {

        @Override
        public boolean validPoint(DeviceUpdate upd) {
            return ((PZephyrHxMHolder) upd).distance >= 0;
        }

    };

    public PZephyrHxMHolder() {
        super();
    }

    public PZephyrHxMHolder(com.moviz.lib.comunication.holder.ZephyrHxMHolder w) {
        super(w);
    }

    public PZephyrHxMHolder(Parcel p) {
        firmwareID = (short) p.readInt();
        p.readCharArray(firmwareVersion);
        hardwareID = (short) p.readInt();
        p.readCharArray(hardwareVersion);
        battery = p.readByte();
        pulse = (short) p.readInt();
        heartBeat = (short) p.readInt();
        okts = p.readByte();
        p.readIntArray(ts);
        p.readIntArray(tsR);
        distance = p.readDouble();
        distanceR = p.readDouble();
        rawDistance = (short) p.readInt();
        speed = p.readDouble();
        strides = (short) p.readInt();
        stridesR = p.readInt();
        nBeatsR = p.readInt();
        timeRms = p.readLong();
        timeRAbsms = p.readLong();
        timeR = (short) p.readInt();
        speedMn = p.readDouble();
        pulseMn = p.readDouble();
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(firmwareID);
        dest.writeCharArray(firmwareVersion);
        dest.writeInt(hardwareID);
        dest.writeCharArray(hardwareVersion);
        dest.writeByte(battery);
        dest.writeInt(pulse);
        dest.writeInt(heartBeat);
        dest.writeByte(okts);
        dest.writeIntArray(ts);
        dest.writeIntArray(tsR);
        dest.writeDouble(distance);
        dest.writeDouble(distanceR);
        dest.writeInt(rawDistance);
        dest.writeDouble(speed);
        dest.writeInt(strides);
        dest.writeInt(stridesR);
        dest.writeInt(nBeatsR);
        dest.writeLong(timeRms);
        dest.writeLong(timeRAbsms);
        dest.writeInt(timeR);
        dest.writeDouble(speedMn);
        dest.writeDouble(pulseMn);
    }

    public static final Creator<PZephyrHxMHolder> CREATOR = new Creator<PZephyrHxMHolder>() {
        @Override
        public PZephyrHxMHolder createFromParcel(Parcel parcel) {
            return new PZephyrHxMHolder(parcel);
        }

        @Override
        public PZephyrHxMHolder[] newArray(int i) {
            return new PZephyrHxMHolder[i];
        }
    };

    @Override
    public ContentValues[] toDBValue() {
        ContentValues[] vss = new ContentValues[1 + okts];

        vss[0] = new ContentValues();
        if (id >= 0)
            vss[0].put("_id", id);
        vss[0].put("ctimems", timeRms);
        vss[0].put("ctimeabsms", timeRAbsms);
        vss[0].put("odist", distance);
        vss[0].put("cdist", distanceR);
        vss[0].put("ospd", speed);
        vss[0].put("opul", pulse);
        vss[0].put("ostride", strides);
        vss[0].put("cstride", stridesR);
        vss[0].put("cbeats", nBeatsR);
        vss[0].put("obattery", battery);
        vss[0].put("session", sessionId);
        for (int i = okts - 1, j = 1; i >= 0; i--, j++) {
            vss[j] = new ContentValues();
            vss[j].put("ctimems", tsR[i]);
            vss[j].put("session", sessionId);
        }
        return vss;
    }

    @Override
    public void fromCursor(Cursor cursor, String[] p) {
        id = cursor.getLong(cursor.getColumnIndex(p[0] + "_id"));
        timeRms = cursor.getLong(cursor.getColumnIndex(p[0] + "ctimems"));
        timeRAbsms = cursor.getLong(cursor.getColumnIndex(p[0] + "ctimeabsms"));
        if (timeRAbsms == 0)
            timeRAbsms = timeRms;
        timeR = (short) (timeRms / 1000.0 + 0.5);
        int idxdist = cursor.getColumnIndex(p[0] + "odist");
        if (!cursor.isNull(idxdist)) {
            distance = cursor.getDouble(idxdist);
            distanceR = cursor.getDouble(cursor.getColumnIndex(p[0] + "cdist"));
            speed = cursor.getDouble(cursor.getColumnIndex(p[0] + "ospd"));
            pulse = cursor.getShort(cursor.getColumnIndex(p[0] + "opul"));
            strides = (short) cursor.getInt(cursor.getColumnIndex(p[0] + "ostride"));
            stridesR = cursor.getInt(cursor.getColumnIndex(p[0] + "cstride"));
            nBeatsR = cursor.getInt(cursor.getColumnIndex(p[0] + "cbeats"));
            battery = (byte) cursor.getShort(cursor.getColumnIndex(p[0] + "obattery"));
            okts = 0;
        } else {
            okts = 1;
            tsR[0] = (int) timeRms;
            distance = -1.0;
        }
    }

    @Override
    public String toDBTable() {
        // TODO Auto-generated method stub
        return "create table if not exists " + getTableName() +
                "(_id integer primary key, " +
                "ctimems Integer not null, " +
                "ctimeabsms Integer  DEFAULT 0, " +
                "odist real, " +
                "cdist real, " +
                "ospd real, " +
                "opul Integer, " +
                "ostride Integer, " +
                "cstride Integer, " +
                "cbeats Integer, " +
                "obattery Integer, " +
                "session Integer not null," +
                "FOREIGN KEY(session) REFERENCES session(_id) ON DELETE CASCADE);";
    }

    @Override
    public String getTableName() {
        return "zephyrhxmSV";
    }

    @Override
    public PHolderSetter getSessionAggregateVars() {
        PHolderSetter hs = new PHolderSetter();
        PHolder ph;
        hs.add(ph = new PHolder(PHolder.Type.LONG, "consvar.max.ctimems"));
        ph.setPrint(new com.moviz.lib.comunication.holder.MSTimePrinter());
        hs.add(ph = new PHolder(PHolder.Type.DOUBLE, "consvar.max.cdist"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        hs.add(ph = new PHolder(PHolder.Type.INT, "consvar.max.cstride"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        hs.add(ph = new PHolder(PHolder.Type.INT, "consvar.max.cbeats"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        hs.add(ph = new PHolder(PHolder.Type.DOUBLE, "consvar.avg.ospd"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        hs.add(ph = new PHolder(PHolder.Type.DOUBLE, "consvar.avg.opul"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        return hs;
    }

    @Override
    public String selectCols(String p) {
        return p + "._id AS " + p + "_id, " +
                p + ".ctimems AS " + p + "ctimems, " +
                p + ".ctimeabsms AS " + p + "ctimeabsms, " +
                p + ".odist AS " + p + "odist, " +
                p + ".cdist AS " + p + "cdist, " +
                p + ".ospd AS " + p + "ospd, " +
                p + ".opul AS " + p + "opul, " +
                p + ".ostride AS " + p + "ostride, " +
                p + ".cstride AS " + p + "cstride, " +
                p + ".cbeats AS " + p + "cbeats, " +
                p + ".obattery AS " + p + "obattery, " +
                p + ".session AS " + p + "session";
    }

    @Override
    public String prepareForJoin(long other) {
        return "SELECT " +
                "MAX(ctimems) AS mctimems, " +
                "MAX(cdist) AS mcdist, " +
                "MAX(cstride) AS mcstride, " +
                "MAX(cbeats) AS mcbeats " +
                "FROM " + getTableName() + " " +
                "WHERE session=" + sessionId + " " +
                "GROUP BY session";
    }

    @Override
    public String[] join(Cursor c, long other, long difftime) {
        long mctimems = c.getLong(0);
        double mcdist = c.getDouble(1);
        int mcstride = c.getInt(2);
        int mcbeats = c.getInt(3);
        String[] rv = new String[2];
        rv[0] = "INSERT INTO  " + getTableName() + " " +
                "(ctimems,ctimeabsms,odist,cdist,ospd,opul,ostride,cstride,cbeats,obattery,session) SELECT " +
                "ctimems+" + mctimems + ", " +
                "ctimeabsms+" + difftime + ", " +
                "odist, " +
                "cdist+" + mcdist + ", " +
                "ospd,opul,ostride, " +
                "cstride+" + mcstride + ", " +
                "cbeats+" + mcbeats + ", " +
                "obattery," + sessionId + " " +
                "FROM " + getTableName() + " " +
                "WHERE session=" + other;
        rv[1] = "DELETE FROM " + getTableName() + " " +
                "WHERE session=" + other;
        return rv;
    }

    @Override
    public GoogleFitPointTransformer getFitPointTransformer() {
        return transformer;
    }

    @Override
    public int getConflictPolicy() {
        return SQLiteDatabase.CONFLICT_NONE;
    }
}