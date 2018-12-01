package com.moviz.lib.comunication.plus.holder;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.HRDeviceHolder;
import com.moviz.lib.googlefit.GoogleFitPointTransformer;
import com.moviz.lib.googlefit.HeartFitTransformer;
import com.moviz.lib.googlefit.HeartInstFitTransformer;

public class PHRDeviceHolder extends HRDeviceHolder implements Parcelable, UpdateDatabasable, Joinable {

    private static HeartFitTransformer transformer = new HeartInstFitTransformer() {

        @Override
        public boolean validPoint(DeviceUpdate upd) {
            return ((PHRDeviceHolder) upd).pulse >= 0;
        }

    };

    public PHRDeviceHolder() {
        super();
    }

    public PHRDeviceHolder(HRDeviceHolder w) {
        super(w);
    }

    public PHRDeviceHolder(Parcel p) {
        pulse = (short) p.readInt();
        joule = (short) p.readInt();
        worn = p.readByte();

        nBeatsR = p.readInt();
        pulseMn = p.readDouble();
        jouleMn = p.readDouble();
        timeRms = p.readLong();
        timeRAbsms = p.readLong();
        timeR = (short) p.readInt();
        nintervals = p.readByte();
        p.readFloatArray(rrintervals);
    }

    @Override
    public ContentValues[] toDBValue() {
        ContentValues[] vss = new ContentValues[1 + nintervals];

        vss[0] = new ContentValues();
        if (id >= 0)
            vss[0].put("_id", id);
        vss[0].put("ctimems", timeRms);
        vss[0].put("ctimeabsms", timeRAbsms);
        vss[0].put("opul", pulse);
        vss[0].put("ojoule", joule);
        vss[0].put("oworn", worn);
        vss[0].put("cbeats", nBeatsR);
        vss[0].put("session", sessionId);
        for (int i = 0, j = 1; i < nintervals; i++, j++) {
            long tot = (int) (rrintervals[i] * 1024);
            vss[j] = new ContentValues();
            vss[j].put("ctimems", tot);
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
        int idxdist = cursor.getColumnIndex(p[0] + "opul");
        if (!cursor.isNull(idxdist)) {
            pulse = cursor.getShort(cursor.getColumnIndex(p[0] + "opul"));
            joule = cursor.getShort(cursor.getColumnIndex(p[0] + "ojoule"));
            worn = (byte) cursor.getShort(cursor.getColumnIndex(p[0] + "oworn"));

            nBeatsR = cursor.getInt(cursor.getColumnIndex(p[0] + "cbeats"));
        } else {
            pulse = -1;
            nintervals = 1;
            rrintervals[0] = timeRms / 1024F;
        }

    }

    @Override
    public String toDBTable() {
        return "create table if not exists " + getTableName() +
                "(_id integer primary key, " +
                "ctimems Integer not null, " +
                "ctimeabsms Integer DEFAULT 0, " +
                "opul Integer, " +
                "ojoule Integer, " +
                "oworn Integer, " +
                "cbeats Integer, " +
                "session Integer not null," +
                "FOREIGN KEY(session) REFERENCES session(_id) ON DELETE CASCADE);";
    }

    @Override
    public String getTableName() {
        return "hrdeviceSV";
    }

    @Override
    public String selectCols(String p) {
        return p + "._id AS " + p + "_id, " +
                p + ".ctimems AS " + p + "ctimems, " +
                p + ".ctimeabsms AS " + p + "ctimeabsms, " +
                p + ".opul AS " + p + "opul, " +
                p + ".ojoule AS " + p + "ojoule, " +
                p + ".oworn AS " + p + "oworn, " +
                p + ".cbeats AS " + p + "cbeats, " +
                p + ".session AS " + p + "session";
    }

    @Override
    public String prepareForJoin(long other) {
        return "SELECT " +
                "MAX(ctimems) AS mctimems, " +
                "MAX(cbeats) AS mcbeats " +
                "FROM " + getTableName() + " " +
                "WHERE session=" + sessionId + " " +
                "GROUP BY session";
    }

    @Override
    public String[] join(Cursor c, long other, long difftime) {
        long mctimems = c.getLong(0);
        int mcbeats = c.getInt(1);
        String[] rv = new String[2];
        rv[0] = "INSERT INTO  " + getTableName() + " " +
                "(ctimems,ctimeabsms,opul,cbeats,session) SELECT " +
                "ctimems+" + mctimems + ", " +
                "ctimeabsms+" + difftime + ", " +
                "opul, " +
                "ojoule, " +
                "oworn, " +
                "cbeats+" + mcbeats + ", " +
                sessionId + " " +
                "FROM " + getTableName() + " " +
                "WHERE session=" + other;
        rv[1] = "DELETE FROM " + getTableName() + " " +
                "WHERE session=" + other;
        return rv;
    }

    @Override
    public PHolderSetter getSessionAggregateVars() {
        PHolderSetter hs = new PHolderSetter();
        PHolder ph;
        hs.add(ph = new PHolder(PHolder.Type.LONG, "consvar.max.ctimems"));
        ph.setPrint(new com.moviz.lib.comunication.holder.MSTimePrinter());
        hs.add(ph = new PHolder(PHolder.Type.DOUBLE, "consvar.avg.opul"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        hs.add(ph = new PHolder(PHolder.Type.INT, "consvar.max.cbeats"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        return hs;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pulse);
        dest.writeInt(joule);
        dest.writeByte(worn);
        dest.writeInt(nBeatsR);
        dest.writeDouble(pulseMn);
        dest.writeDouble(jouleMn);
        dest.writeLong(timeRms);
        dest.writeLong(timeRAbsms);
        dest.writeInt(timeR);
        dest.writeByte(nintervals);
        dest.writeFloatArray(rrintervals);
    }

    public static final Creator<PHRDeviceHolder> CREATOR = new Creator<PHRDeviceHolder>() {
        @Override
        public PHRDeviceHolder createFromParcel(Parcel parcel) {
            return new PHRDeviceHolder(parcel);
        }

        @Override
        public PHRDeviceHolder[] newArray(int i) {
            return new PHRDeviceHolder[i];
        }
    };

    @Override
    public GoogleFitPointTransformer getFitPointTransformer() {
        return transformer;
    }

    @Override
    public int getConflictPolicy() {
        return SQLiteDatabase.CONFLICT_NONE;
    }

}
