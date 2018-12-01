package com.moviz.lib.comunication.plus.holder;

import android.content.ContentValues;
import android.database.Cursor;

public interface Databasable {
    ContentValues[] toDBValue();

    void fromCursor(Cursor cursor, String[] prefix);

    String toDBTable();

    String getTableName();

    long getId();

    void setId(long id);

    String selectCols(String p);

    int getConflictPolicy();
}
