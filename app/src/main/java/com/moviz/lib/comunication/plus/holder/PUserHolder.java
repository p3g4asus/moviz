package com.moviz.lib.comunication.plus.holder;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.moviz.lib.comunication.holder.UserHolder;

import java.util.Date;

public class PUserHolder extends UserHolder implements Parcelable, Databasable {

    public PUserHolder(long idn, String nm, boolean male, double w, double h, byte a, boolean metric) {
        super(idn, nm, male, w, h, a, metric);
    }

    public PUserHolder(long idn, String nm, boolean male, double w, double h, long brd, boolean metric) {
        super(idn, nm, male, w, h, brd, metric);
    }


    public PUserHolder(Parcel p) {
        id = p.readLong();
        name = p.readString();
        age = p.readByte();
        height = p.readDouble();
        weight = p.readDouble();
        isMale = p.readByte() == 1;
    }

    public PUserHolder() {
        // TODO Auto-generated constructor stub
    }

    public PUserHolder(UserHolder u) {
        // TODO Auto-generated constructor stub
        super(u);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeByte(age);
        dest.writeDouble(height);
        dest.writeDouble(weight);
        dest.writeByte((byte) (isMale ? 1 : 0));
    }

    public static final Creator<PUserHolder> CREATOR = new Creator<PUserHolder>() {
        @Override
        public PUserHolder createFromParcel(Parcel parcel) {
            return new PUserHolder(parcel);
        }

        @Override
        public PUserHolder[] newArray(int i) {
            return new PUserHolder[i];
        }
    };

    @Override
    public ContentValues[] toDBValue() {
        ContentValues values = new ContentValues();
        if (id >= 0)
            values.put("_id", id);
        values.put("weight", (int) (getWeight() + 0.5));
        values.put("height", (int) (getHeight() + 0.5));
        values.put("birthday", getBirthDay());
        values.put("name", getName());
        values.put("male", isMale() ? 1 : 0);
        return new ContentValues[]{values};
    }

    @Override
    public void fromCursor(Cursor cursor, String[] p) {
        id = cursor.getLong(cursor.getColumnIndex(p[0] + "_id"));
        weight = cursor.getInt(cursor.getColumnIndex(p[0] + "weight"));
        height = cursor.getInt(cursor.getColumnIndex(p[0] + "height"));
        name = cursor.getString(cursor.getColumnIndex(p[0] + "name"));
        birthDay = cursor.getLong(cursor.getColumnIndex(p[0] + "birthday"));
        isMale = cursor.getInt(cursor.getColumnIndex(p[0] + "male")) == 1;
        age = (byte) getAge(new Date(birthDay));
    }

    @Override
    public String toDBTable() {
        return "create table if not exists " + getTableName() +
                "(_id integer primary key, " +
                "name text not null," +
                "weight Integer not null, " +
                "height Integer not null, " +
                "birthday Integer not null, " +
                "male Integer not null);";
    }

    @Override
    public String getTableName() {
        return "user";
    }

    @Override
    public String selectCols(String p) {
        return p + "._id AS " + p + "_id, " +
                p + ".name AS " + p + "name, " +
                p + ".weight AS " + p + "weight, " +
                p + ".height AS " + p + "height, " +
                p + ".birthday AS " + p + "birthday, " +
                p + ".male AS " + p + "male";
    }

    @Override
    public int getConflictPolicy() {
        return SQLiteDatabase.CONFLICT_NONE;
    }
}
