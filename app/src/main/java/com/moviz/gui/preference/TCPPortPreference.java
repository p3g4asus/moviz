package com.moviz.gui.preference;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

public class TCPPortPreference extends ValidatedEditTextPreference {
    public TCPPortPreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public TCPPortPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    @Override
    protected boolean onCheckValue(String value) {
        int prt = -1;
        try {
            prt = Integer.parseInt(value);
        } catch (Exception e) {
            prt = -1;
        }
        return prt >= 0 && prt <= 65535;
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }

}
