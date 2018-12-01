package com.moviz.lib.comunication.plus.holder;

import android.content.ContentValues;
import android.database.Cursor;

import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.holder.HolderSetter;

public class PHolderSetter extends HolderSetter {

    /**
     *
     */
    private static final long serialVersionUID = -5793712136920680302L;

    public String toDbVar() {
        String rv = "";
        int i = 0;
        for (Holder h : this) {
            if (i != 0)
                rv += ",";
            if (h instanceof PHolder)
                rv += ((PHolder) h).toDbVar();
            else
                rv += new PHolder(h).toDbVar();
        }
        return rv;
    }

    public void fromCursor(Cursor c, String p) {
        for (Holder h : this) {
            if (h instanceof PHolder)
                ((PHolder) h).fromCursor(c, p);
            else {
                PHolder h2 = new PHolder(h);
                h2.fromCursor(c, p);
                h.copyFrom(h2);
            }
        }
    }

    public void toDBValue(ContentValues c) {
        for (Holder h : this) {
            if (h instanceof PHolder)
                ((PHolder) h).toDBValue(c);
            else
                new PHolder(h).toDBValue(c);
        }
    }
}
