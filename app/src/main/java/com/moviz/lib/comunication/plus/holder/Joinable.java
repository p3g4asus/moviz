package com.moviz.lib.comunication.plus.holder;

import android.database.Cursor;

public interface Joinable extends Databasable {
    public String prepareForJoin(long other);

    public String[] join(Cursor c, long other, long difftime);
}
