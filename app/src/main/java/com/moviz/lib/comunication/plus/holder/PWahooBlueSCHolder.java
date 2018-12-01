package com.moviz.lib.comunication.plus.holder;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.moviz.lib.comunication.holder.WahooBlueSCHolder;
import com.moviz.lib.googlefit.GoogleFitPointTransformer;
import com.moviz.lib.googlefit.WahooBlueSCFitTrasformer;

/**
 * Created by Matteo on 29/10/2016.
 */

public class PWahooBlueSCHolder extends WahooBlueSCHolder implements Parcelable, UpdateDatabasable, Joinable {
    private static GoogleFitPointTransformer transformer = new WahooBlueSCFitTrasformer();


    public PWahooBlueSCHolder() {
        super();
    }

    public PWahooBlueSCHolder(com.moviz.lib.comunication.holder.WahooBlueSCHolder w) {
        super(w);
    }

    public PWahooBlueSCHolder(Parcel p) {
        sensType = (SensorType) p.readSerializable();
        id = p.readLong();
        calorie = (short) p.readInt();
        distance = p.readDouble();
        sessionId = p.readLong();
        timeRAbsms = p.readLong();
        timeRms = p.readLong();
        timeR = (short) p.readInt();
        updateN = p.readInt();
        sensVal = p.readLong();
        sensSpd = p.readDouble();
        sensSpdMn = p.readDouble();
        sensSpdMnR = p.readDouble();
        speedKmH = p.readDouble();
        speedKmHmn = p.readDouble();
        gear = p.readByte();
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(sensType);
        dest.writeLong(id);
        dest.writeInt(calorie);
        dest.writeDouble(distance);
        dest.writeLong(sessionId);
        dest.writeLong(timeRAbsms);
        dest.writeLong(timeRms);
        dest.writeInt(timeR);
        dest.writeInt(updateN);
        dest.writeLong(sensVal);
        dest.writeDouble(sensSpd);
        dest.writeDouble(sensSpdMn);
        dest.writeDouble(sensSpdMnR);
        dest.writeDouble(speedKmH);
        dest.writeDouble(speedKmHmn);
        dest.writeByte((byte) gear);
    }

    @Override
    public ContentValues[] toDBValue() {
        ContentValues values = new ContentValues();
        if (id >= 0)
            values.put("_id", id);
        values.put("stype", sensType.ordinal());
        values.put("ocal", calorie);
        values.put("cdist", distance);
        values.put("session", getSessionId());
        values.put("ctime", timeR);
        values.put("ctimems", timeRms);
        values.put("ctimeabsms", timeRAbsms);
        values.put("sval", sensVal);
        values.put("sspd", sensSpd);
        values.put("sspdmn", sensSpdMn);
        values.put("sspdmnr", sensSpdMnR);
        values.put("spdkmh", speedKmH);
        values.put("spdkmhmn", speedKmHmn);
        values.put("gear", gear);
        return new ContentValues[]{values};
    }

    @Override
    public void fromCursor(Cursor cursor, String[] p) {
        sensType = SensorType.values()[cursor.getShort(cursor.getColumnIndex(p[0] + "stype"))];
        id = cursor.getLong(cursor.getColumnIndex(p[0] + "_id"));
        calorie = cursor.getShort(cursor.getColumnIndex(p[0] + "ocal"));
        distance = cursor.getDouble(cursor.getColumnIndex(p[0] + "cdist"));
        sessionId = cursor.getLong(cursor.getColumnIndex(p[0] + "session"));
        timeR = cursor.getShort(cursor.getColumnIndex(p[0] + "ctime"));
        timeRms = cursor.getLong(cursor.getColumnIndex(p[0] + "ctimems"));
        timeRAbsms = cursor.getLong(cursor.getColumnIndex(p[0] + "ctimeabsms"));
        if (timeRAbsms == 0)
            timeRAbsms = timeRms;
        sensVal = cursor.getLong(cursor.getColumnIndex(p[0] + "sval"));
        sensSpd = cursor.getDouble(cursor.getColumnIndex(p[0] + "sspd"));
        sensSpdMn = cursor.getDouble(cursor.getColumnIndex(p[0] + "sspdmn"));
        sensSpdMnR = cursor.getDouble(cursor.getColumnIndex(p[0] + "sspdmnr"));
        speedKmH = cursor.getDouble(cursor.getColumnIndex(p[0] + "spdkmh"));
        speedKmHmn = cursor.getDouble(cursor.getColumnIndex(p[0] + "spdkmhmn"));
        gear = cursor.getInt(cursor.getColumnIndex(p[0] + "gear"));

    }

    @Override
    public String toDBTable() {
        return "create table if not exists " + getTableName() +
                "(_id Integer primary key, " +
                "stype Integer not null, " +
                "ocal Integer not null, " +
                "cdist real not null, " +
                "session Integer not null," +
                "ctime Integer not null, " +
                "ctimems Integer not null, " +
                "ctimeabsms Integer DEFAULT 0, " +
                "sval real not null, " +
                "sspd real not null, " +
                "sspdmn real not null, " +
                "sspdmnr real not null, " +
                "spdkmh real not null, " +
                "spdkmhmn real not null, " +
                "gear Integer not null, " +
                "FOREIGN KEY(session) REFERENCES session(_id) ON DELETE CASCADE);";
    }

    public static final Creator<PPafersHolder> CREATOR = new Creator<PPafersHolder>() {
        @Override
        public PPafersHolder createFromParcel(Parcel parcel) {
            return new PPafersHolder(parcel);
        }

        @Override
        public PPafersHolder[] newArray(int i) {
            return new PPafersHolder[i];
        }
    };

    @Override
    public String getTableName() {
        return "wahoobluescSV";
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
        return hs;
    }

    @Override
    public String selectCols(String p) {
        return p + "._id AS " + p + "_id, " +
                p + ".stype AS " + p + "stype, " +
                p + ".ocal AS " + p + "ocal, " +
                p + ".cdist AS " + p + "cdist, " +
                p + ".session AS " + p + "session, " +
                p + ".ctime AS " + p + "ctime, " +
                p + ".ctimems AS " + p + "ctimems, " +
                p + ".ctimeabsms AS " + p + "ctimeabsms, " +
                p + ".sval AS " + p + "sval, " +
                p + ".sspd AS " + p + "sspd, " +
                p + ".sspdmn AS " + p + "sspdmn, " +
                p + ".sspdmnr AS " + p + "sspdmnr" +
                p + ".spdkmh AS " + p + "spdkmh" +
                p + ".spdkmhmn AS " + p + "spdkmhmn" +
                p + ".gear AS " + p + "gear";
    }

    @Override
    public String[] join(Cursor c, long othersession, long difftime) {
        double mcdist = c.getDouble(0);
        int mocal = c.getInt(1);
        int mctime = c.getInt(2);
        long mctimems = c.getLong(3);
        long msval = c.getLong(4);
        String[] rv = new String[2];
        rv[0] = "INSERT INTO  " + getTableName() + " " +
                "(ocal,cdist,stype,session,ctime,ctimems,ctimeabsms,sval,sspd,sspdmn,sspdmnr,spdkmh,spdkmhmn,gear) SELECT " +
                "ocal+" + mocal + ", " +
                "cdist+" + mcdist + ", " +
                sensType.ordinal() + ", " +
                sessionId + ", " +
                "ctime+" + mctime + ", " +
                "ctimems+" + mctimems + ", " +
                "ctimeabsms+" + difftime + ", " +
                "sval+" + msval + ", " +
                "sspd, " +
                "sspdmn, " +
                "sspdmnr " +
                "spdkmh " +
                "spdkmhmn " +
                "gear " +
                "FROM " + getTableName() + " " +
                "WHERE session=" + othersession;
        rv[1] = "DELETE FROM " + getTableName() + " " +
                "WHERE session=" + othersession;
        return rv;
    }

    @Override
    public String prepareForJoin(long othersessionid) {
        return "SELECT " +
                "MAX(ocal) AS mocal " +
                "MAX(cdist) AS mcdist, " +
                "MAX(ctime) AS mctime, " +
                "MAX(ctimems) AS mctimems, " +
                "MAX(sval) AS msval, " +
                "FROM " + getTableName() + " " +
                "WHERE session=" + getSessionId() + " " +
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
