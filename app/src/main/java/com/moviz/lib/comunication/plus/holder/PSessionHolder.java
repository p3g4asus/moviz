package com.moviz.lib.comunication.plus.holder;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.moviz.lib.comunication.holder.DeviceHolder;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.holder.UserHolder;

public class PSessionHolder extends com.moviz.lib.comunication.holder.SessionHolder implements Parcelable, Joinable {
    public static final int NOT_EXPORTED = 65535;
    private int exported = NOT_EXPORTED;

    public int getExported() {
        return exported;
    }

    public void setExported(int exported) {
        this.exported = exported;
    }

    public PSessionHolder(long idn, long mId, DeviceHolder dev, long date, UserHolder usr, String sett) {
        super(idn, mId, dev, date, usr, sett);
        holders = new PHolderSetter();
        userClass = PUserHolder.class;
        deviceClass = PDeviceHolder.class;
        holderClass = PHolderSetter.class;
    }

    public PSessionHolder(com.moviz.lib.comunication.holder.SessionHolder s) {
        super(s);
        holders = new PHolderSetter();
        holders.addAll(s.getHolders());
        userClass = PUserHolder.class;
        deviceClass = PDeviceHolder.class;
        holderClass = PHolderSetter.class;
    }

    public PHolderSetter getPHolders() {
        if (holders == null)
            holders = newHolder();
        return (PHolderSetter) holders;
    }


    public PSessionHolder(Parcel p) {
        id = p.readLong();
        mainSessionId = p.readLong();
        device = p.readParcelable(PDeviceHolder.class.getClassLoader());
        dateStart = p.readLong();
        user = (UserHolder) p.readParcelable(PUserHolder.class.getClassLoader());
        settings = p.readString();
        exported = p.readInt();
        if (holders == null)
            holders = newHolder();
        int n = p.readByte();
        for (int i = 0; i < n; i++) {
            holders.set((Holder) p.readParcelable(PHolder.class.getClassLoader()));
        }
        userClass = PUserHolder.class;
        deviceClass = PDeviceHolder.class;
        holderClass = PHolderSetter.class;
    }

    public PSessionHolder() {
        userClass = PUserHolder.class;
        deviceClass = PDeviceHolder.class;
        holderClass = PHolderSetter.class;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(mainSessionId);
        dest.writeParcelable((Parcelable) device, flags);
        dest.writeLong(dateStart);
        dest.writeParcelable((Parcelable) user, flags);
        dest.writeString(settings);
        dest.writeInt(exported);
        dest.writeByte((byte) holders.size());
        if (holders == null)
            holders = newHolder();
        for (Holder h : holders) {
            if (!(h instanceof PHolder))
                dest.writeParcelable(new PHolder(h), flags);
            else
                dest.writeParcelable((PHolder) h, flags);
        }
        //dest.writeList(values);
    }

    public static final Creator<PSessionHolder> CREATOR = new Creator<PSessionHolder>() {
        @Override
        public PSessionHolder createFromParcel(Parcel parcel) {
            return new PSessionHolder(parcel);
        }

        @Override
        public PSessionHolder[] newArray(int i) {
            return new PSessionHolder[i];
        }
    };

    @Override
    public ContentValues[] toDBValue() {
        ContentValues cv = new ContentValues();
        if (id >= 0)
            cv.put("_id", id);
        cv.put("datestart", dateStart);
        cv.put("device", device.getId());
        cv.put("settings", settings);
        cv.put("user", user.getId());
        cv.put("exported", exported);
        if (mainSessionId >= 0)
            cv.put("mainid", mainSessionId);
        return new ContentValues[]{cv};
    }

    @Override
    public void fromCursor(Cursor cursor, String[] p) {
        id = cursor.getLong(cursor.getColumnIndex(p[0] + "_id"));
        int mainIdc = cursor.getColumnIndex(p[0] + "mainid");
        if (cursor.isNull(mainIdc))
            mainSessionId = id;
        else {
            mainSessionId = cursor.getLong(mainIdc);
            if (mainSessionId < 0)
                mainSessionId = id;
        }
        device = new PDeviceHolder();
        ((Databasable) device).fromCursor(cursor, new String[]{p[1]});
        user = new PUserHolder();
        ((Databasable) user).fromCursor(cursor, new String[]{p[2]});
        dateStart = cursor.getLong(cursor.getColumnIndex(p[0] + "datestart"));
        exported = cursor.getInt(cursor.getColumnIndex(p[0] + "exported"));
        settings = cursor.getString(cursor.getColumnIndex(p[0] + "settings"));
        getPHolders().fromCursor(cursor, p[3]);
    }

    @Override
    public String toDBTable() {
        return "create table if not exists " + getTableName() +
                "(_id integer primary key, " +
                "mainid integer, " +
                "device integer not null," +
                "datestart Integer not null, " +
                "exported Integer DEFAULT 65535, " +
                "settings text, " +
                "user Integer not null, " +
                "FOREIGN KEY(user) REFERENCES " + new PUserHolder().getTableName() + "(_id) ON DELETE CASCADE, " +
                "FOREIGN KEY(device) REFERENCES " + new PDeviceHolder().getTableName() + "(_id) ON DELETE CASCADE);";
    }

    @Override
    public String getTableName() {
        return "session";
    }

    @Override
    public String selectCols(String p) {
        return p + "._id AS " + p + "_id, " +
                p + ".mainid AS " + p + "mainid, " +
                p + ".device AS " + p + "device, " +
                p + ".datestart AS " + p + "datestart, " +
                p + ".settings AS " + p + "settings, " +
                p + ".exported AS " + p + "exported, " +
                p + ".user AS " + p + "user";
    }

    @Override
    public String[] join(Cursor c, long other, long difftime) {
        return new String[]{"DELETE FROM " + getTableName() + " WHERE _id=" + other};
    }

    @Override
    public String prepareForJoin(long other) {
        return null;
    }

    public ContentValues getExportedColumn() {
        ContentValues cv = new ContentValues();
        cv.put("exported", exported);
        return cv;
    }

    @Override
    public int getConflictPolicy() {
        return SQLiteDatabase.CONFLICT_NONE;
    }
}
