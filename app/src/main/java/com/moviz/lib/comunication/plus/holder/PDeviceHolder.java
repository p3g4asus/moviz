package com.moviz.lib.comunication.plus.holder;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.holder.DeviceHolder;

public class PDeviceHolder extends DeviceHolder implements Parcelable, Databasable {
    private int orderd = 999;

    public int getOrderd() {
        return orderd;
    }

    public void setOrderd(int orderd) {
        this.orderd = orderd;
    }

    public PDeviceHolder(long i, String addr, String nm, String al, DeviceType pr, String desc, String addit, boolean ena) {
        super(i, addr, nm, al, pr, desc, addit, ena);
    }

    public PDeviceHolder(DeviceHolder w) {
        super(w);
    }

    public PDeviceHolder() {
    }

    public PDeviceHolder(Parcel w) {
        id = w.readLong();
        address = w.readString();
        name = w.readString();
        alias = w.readString();
        description = w.readString();
        type = DeviceType.types[w.readByte()];
        additionalSettings = w.readString();
        enabled = w.readByte() != 0;
        orderd = w.readInt();
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(address);
        dest.writeString(name);
        dest.writeString(alias);
        dest.writeString(description);
        dest.writeByte((byte) type.ordinal());
        dest.writeString(additionalSettings);
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeInt(orderd);

    }

    public static final Creator<PDeviceHolder> CREATOR = new Creator<PDeviceHolder>() {
        @Override
        public PDeviceHolder createFromParcel(Parcel parcel) {
            return new PDeviceHolder(parcel);
        }

        @Override
        public PDeviceHolder[] newArray(int i) {
            return new PDeviceHolder[i];
        }
    };

    @Override
    public ContentValues[] toDBValue() {
        ContentValues values = new ContentValues();
        if (id >= 0)
            values.put("_id", id);
        values.put("name", name);
        values.put("alias", alias);
        values.put("description", description);
        values.put("address", address);
        values.put("type", type.ordinal());
        values.put("additionalsettings", additionalSettings);
        values.put("enabled", enabled ? 1 : 0);
        values.put("orderd", orderd);
        return new ContentValues[]{values};
    }

    @Override
    public void fromCursor(Cursor cursor, String[] p) {
        id = cursor.getLong(cursor.getColumnIndex(p[0] + "_id"));
        name = cursor.getString(cursor.getColumnIndex(p[0] + "name"));
        alias = cursor.getString(cursor.getColumnIndex(p[0] + "alias"));
        description = cursor.getString(cursor.getColumnIndex(p[0] + "description"));
        address = cursor.getString(cursor.getColumnIndex(p[0] + "address"));
        type = DeviceType.types[cursor.getInt(cursor.getColumnIndex(p[0] + "type"))];
        try {
            additionalSettings = cursor.getString(cursor.getColumnIndex(p[0] + "additionalsettings"));
        } catch (Exception e) {
            additionalSettings = "";
        }
        enabled = cursor.getInt(cursor.getColumnIndex(p[0] + "enabled")) != 0;
        try {
            orderd = cursor.getInt(cursor.getColumnIndex(p[0] + "orderd"));
        } catch (Exception e) {
            orderd = 999;
        }
    }

    @Override
    public String toDBTable() {
        return "create table if not exists " + getTableName() +
                "(_id integer primary key autoincrement, " +
                "description text, " +
                "address VARCHAR(17), " +
                "name VARCHAR(30), " +
                "alias VARCHAR(30) not null, " +
                "type integer not null, " +
                "additionalsettings TEXT DEFAULT '', " +
                "enabled integer not null," +
                "orderd integer not null DEFAULT 999);";
    }

    @Override
    public String getTableName() {
        return "device";
    }

    @Override
    public String selectCols(String p) {
        return p + "._id AS " + p + "_id, " +
                p + ".description AS " + p + "description, " +
                p + ".name AS " + p + "name, " +
                p + ".alias AS " + p + "alias, " +
                p + ".address AS " + p + "address, " +
                p + ".type AS " + p + "type, " +
                "{"+p + ".[additionalsettings] AS " + p + "additionalsettings, }" +
                "{"+p + ".[orderd] AS " + p + "orderd, }" +
                p + ".enabled AS " + p + "enabled";
    }

    @Override
    public int getConflictPolicy() {
        return SQLiteDatabase.CONFLICT_NONE;
    }

}
