package com.moviz.lib.comunication.plus.holder;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Matteo on 12/11/2016.
 */

public class PConfHolder implements Databasable {
    private long id = -1;
    private String name = "default";
    private String conf = "";

    public PConfHolder() {

    }

    @Override
    public boolean equals(Object o) {
        if (o==null || !(o instanceof PConfHolder))
            return false;
        else {
            PConfHolder cnf = (PConfHolder) o;
            return name.equals(cnf.name);
        }
    }

    public PConfHolder(PConfHolder c) {
        copyFrom(c);
    }

    public void copyFrom(PConfHolder c) {
        id = c.id;
        name = c.name;
        conf = c.conf;
    }

    public PConfHolder(long i,String nm, String val) {
        id = i;
        name = nm;
        conf = val;
    }

    public PConfHolder(String nm) {
        name = nm;
    }

    public PConfHolder(String nm, String val) {
        name = nm;
        conf = val;
    }

    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public ContentValues[] toDBValue() {
        ContentValues values = new ContentValues();
        if (id >= 0)
            values.put("_id", id);
        values.put("name", name);
        values.put("conf", conf);
        return new ContentValues[]{values};
    }

    @Override
    public void fromCursor(Cursor cursor, String[] p) {
        id = cursor.getLong(cursor.getColumnIndex(p[0] + "_id"));
        name = cursor.getString(cursor.getColumnIndex(p[0] + "name"));
        conf = cursor.getString(cursor.getColumnIndex(p[0] + "conf"));
    }

    @Override
    public String toDBTable() {
        String sql = "create table if not exists "+getTableName()+
                "(_id Integer primary key, " +
                "name text not NULL, " +
                "conf text not NULL, " +
                "UNIQUE(name));";
        return sql;
    }

    @Override
    public String getTableName() {
        return "deviceconf";
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long i) {
        id = i;
    }

    @Override
    public String selectCols(String p) {
        return p + "._id AS " + p + "_id, " +
                p + ".name AS " + p + "name, " +
                p + ".conf AS " + p + "conf";
    }

    @Override
    public int getConflictPolicy() {
        return SQLiteDatabase.CONFLICT_REPLACE;
    }

    public String getNameCondition(String startConf) {
        return "name="+ DatabaseUtils.sqlEscapeString(startConf);
    }
}
