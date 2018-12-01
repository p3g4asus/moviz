package com.moviz.gui.preference;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

import com.moviz.lib.db.MySQLiteHelper;

public class DBCleanPreference extends ValidatedEditTextPreference {
    private MySQLiteHelper sqlite = null;

    private void init() {
        sqlite = MySQLiteHelper.newInstance(null, null);
    }

    public DBCleanPreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        init();
    }

    public DBCleanPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    @Override
    protected boolean onCheckValue(String value) {
        int prt = -1;
        try {
            prt = Integer.parseInt(value);
        } catch (Exception e) {
            prt = -1;
        }
        return prt > 0;
    }

    @Override
    protected void postPositiveClick(String value) {
        super.postPositiveClick(value);
        if (sqlite == null)
            init();
        if (sqlite != null) {
            StringBuilder sb = new StringBuilder();
            long[] sesid = sqlite.getShorterSessions(Integer.parseInt(value), sb);
            if (sesid.length > 0)
                sqlite.deleteSessionByMainIds(sb.toString());
        }
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }

}
