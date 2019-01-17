package com.moviz.lib.comunication.plus.holder;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.moviz.lib.googlefit.GoogleFitPointTransformer;
import com.moviz.lib.googlefit.PafersFitTransformer;

import java.lang.reflect.Constructor;

public class PPafersHolder extends com.moviz.lib.comunication.holder.PafersHolder implements Parcelable, UpdateDatabasable, Joinable {
    private static PafersFitTransformer transformer = new PafersFitTransformer();

    public PPafersHolder() {
        super();
    }

    public PPafersHolder(com.moviz.lib.comunication.holder.PafersHolder w) {
        super(w);
    }

    public PPafersHolder(Parcel p) {
        distance = p.readDouble();
        speed = p.readDouble();
        time = (short) p.readInt();
        calorie = (short) p.readInt();
        watt = (short) p.readInt();
        incline = (byte) p.readInt();
        pulse = p.readInt();
        rpm = p.readInt();
        distanceR = p.readDouble();
        timeR = (short) p.readInt();
        timeRms = p.readLong();
        timeRAbsms = p.readLong();
        wattMn = p.readDouble();
        speedMn = p.readDouble();
        pulseMn = p.readDouble();
        rpmMn = p.readDouble();
        updateN = p.readInt();
        id = p.readLong();
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeString(getClass().getName());
        dest.writeDouble(distance);
        dest.writeDouble(speed);
        dest.writeInt(time);
        dest.writeInt(calorie);
        dest.writeInt(watt);
        dest.writeInt(incline);
        dest.writeInt(pulse);
        dest.writeInt(rpm);
        dest.writeDouble(distanceR);
        dest.writeInt(timeR);
        dest.writeLong(timeRms);
        dest.writeLong(timeRAbsms);
        dest.writeDouble(wattMn);
        dest.writeDouble(speedMn);
        dest.writeDouble(pulseMn);
        dest.writeDouble(rpmMn);
        dest.writeInt(updateN);
        dest.writeLong(id);
    }

    @Override
    public ContentValues[] toDBValue() {
        ContentValues values = new ContentValues();
        if (id >= 0)
            values.put("_id", id);
        values.put("otime", time);
        values.put("ctime", timeR);
        values.put("ctimems", timeRms);
        values.put("ctimeabsms", timeRAbsms);
        values.put("odist", distance);
        values.put("cdist", distanceR);
        values.put("ocal", calorie);
        values.put("ospd", speed);
        values.put("opul", pulse);
        values.put("orpm", rpm);
        values.put("owatt", watt);
        values.put("oinc", incline);
        values.put("session", sessionId);
        return new ContentValues[]{values};
    }

    @Override
    public void fromCursor(Cursor cursor, String[] p) {
        id = cursor.getLong(cursor.getColumnIndex(p[0] + "_id"));
        time = cursor.getShort(cursor.getColumnIndex(p[0] + "otime"));
        timeR = cursor.getShort(cursor.getColumnIndex(p[0] + "ctime"));
        timeRms = cursor.getLong(cursor.getColumnIndex(p[0] + "ctimems"));
        timeRAbsms = cursor.getLong(cursor.getColumnIndex(p[0] + "ctimeabsms"));
        if (timeRAbsms == 0)
            timeRAbsms = timeRms;
        distance = cursor.getDouble(cursor.getColumnIndex(p[0] + "odist"));
        distanceR = cursor.getDouble(cursor.getColumnIndex(p[0] + "cdist"));
        calorie = cursor.getShort(cursor.getColumnIndex(p[0] + "ocal"));
        speed = cursor.getDouble(cursor.getColumnIndex(p[0] + "ospd"));
        pulse = cursor.getInt(cursor.getColumnIndex(p[0] + "opul"));
        rpm = cursor.getInt(cursor.getColumnIndex(p[0] + "orpm"));
        watt = cursor.getShort(cursor.getColumnIndex(p[0] + "owatt"));
        incline = (byte) cursor.getShort(cursor.getColumnIndex(p[0] + "oinc"));
    }

    @Override
    public String toDBTable() {
        return "create table if not exists " + getTableName() +
                "(_id Integer primary key, " +
                "otime Integer not null, " +
                "ctime Integer not null, " +
                "ctimems Integer not null, " +
                "ctimeabsms Integer DEFAULT 0, " +
                "odist real not null, " +
                "cdist real not null, " +
                "ocal Integer not null, " +
                "ospd real not null, " +
                "opul Integer not null, " +
                "orpm Integer not null, " +
                "owatt Integer not null, " +
                "oinc Integer not null, " +
                "session Integer not null," +
                "FOREIGN KEY(session) REFERENCES session(_id) ON DELETE CASCADE);";
    }

    public static final Creator<PPafersHolder> CREATOR = new Creator<PPafersHolder>() {
        @Override
        public PPafersHolder createFromParcel(Parcel parcel) {
            String cln = parcel.readString();
            try {
                Class<? extends PPafersHolder> c = (Class<? extends PPafersHolder>) Class.forName(cln);
                Constructor co = c.getConstructor(Parcel.class);
                return (PPafersHolder) co.newInstance(parcel);
            }
            catch (Exception e) {
            }
            return null;
        }

        @Override
        public PPafersHolder[] newArray(int i) {
            return new PPafersHolder[i];
        }
    };

    @Override
    public String getTableName() {
        return "pafersSV";
    }

    @Override
    public PHolderSetter getSessionAggregateVars() {
        PHolderSetter hs = new PHolderSetter();
        PHolder ph;
        hs.add(ph = new PHolder(PHolder.Type.LONG, "consvar.max.ctimems"));
        ph.setPrint(new com.moviz.lib.comunication.holder.MSTimePrinter());
        hs.add(ph = new PHolder(PHolder.Type.DOUBLE, "consvar.max.cdist"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        hs.add(ph = new PHolder(PHolder.Type.INT, "consvar.max.ocal"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        hs.add(ph = new PHolder(PHolder.Type.DOUBLE, "consvar.avg.ospd"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        hs.add(ph = new PHolder(PHolder.Type.DOUBLE, "consvar.avg.opul"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        hs.add(ph = new PHolder(PHolder.Type.DOUBLE, "consvar.avg.orpm"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        hs.add(ph = new PHolder(PHolder.Type.DOUBLE, "consvar.avg.owatt"));
        ph.setPrint(new com.moviz.lib.comunication.holder.ResPrinter());
        return hs;
    }

    @Override
    public String selectCols(String p) {
        return p + "._id AS " + p + "_id, " +
                p + ".otime AS " + p + "otime, " +
                p + ".ctime AS " + p + "ctime, " +
                p + ".ctimems AS " + p + "ctimems, " +
                p + ".ctimeabsms AS " + p + "ctimeabsms, " +
                p + ".odist AS " + p + "odist, " +
                p + ".cdist AS " + p + "cdist, " +
                p + ".ocal AS " + p + "ocal, " +
                p + ".ospd AS " + p + "ospd, " +
                p + ".opul AS " + p + "opul, " +
                p + ".orpm AS " + p + "orpm, " +
                p + ".owatt AS " + p + "owatt, " +
                p + ".oinc AS " + p + "oinc, " +
                p + ".session AS " + p + "session";
    }

    @Override
    public String[] join(Cursor c, long othersession, long difftime) {
        int motime = c.getInt(0);
        int mctime = c.getInt(1);
        long mctimems = c.getLong(2);
        double mcdist = c.getDouble(3);
        double modist = c.getDouble(4);
        int mocal = c.getInt(5);
        String[] rv = new String[2];
        rv[0] = "INSERT INTO  " + getTableName() + " " +
                "(otime,ctime,ctimems,odist,cdist,ocal,ctimeabsmsospd,opul,orpm,owatt,oinc,session) SELECT " +
                "otime+" + motime + ", " +
                "ctime+" + mctime + ", " +
                "ctimems+" + mctimems + ", " +
                "odist+" + modist + ", " +
                "cdist+" + mcdist + ", " +
                "ocal+" + mocal + ", " +
                "ctimeabsms+" + difftime + ", " +
                "ospd,opul,orpm,owatt,oinc," + sessionId + " " +
                "FROM " + getTableName() + " " +
                "WHERE session=" + othersession;
        rv[1] = "DELETE FROM " + getTableName() + " " +
                "WHERE session=" + othersession;
        return rv;
    }

    @Override
    public String prepareForJoin(long othersessionid) {
        return "SELECT " +
                "MAX(otime) AS motime, " +
                "MAX(ctime) AS mctime, " +
                "MAX(ctimems) AS mctimems, " +
                "MAX(cdist) AS mcdist, " +
                "MAX(odist) AS modist, " +
                "MAX(ocal) AS mocal " +
                "FROM " + getTableName() + " " +
                "WHERE session=" + sessionId + " " +
                "GROUP BY session";
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

/*
 * Location: C:\Users\Fujitsu\Downloads\libPFHWApi-for-android-ver-20140122.jar
 * 
 * Qualified Name: com.pafers.fitnesshwapi.lib.device.FitnessHwApiDeviceFeedback
 * 
 * JD-Core Version: 0.7.0.1
 */