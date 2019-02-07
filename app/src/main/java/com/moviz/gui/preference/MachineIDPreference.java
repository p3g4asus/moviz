package com.moviz.gui.preference;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

public class MachineIDPreference extends ValidatedEditTextPreference {
    public MachineIDPreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public MachineIDPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public MachineIDPreference(Context ctx) {
        super(ctx);
    }

    @Override
    protected boolean onCheckValue(String value) {
        int prt = -1;
        try {
            prt = Integer.parseInt(value);
        } catch (Exception e) {
            prt = -1;
        }
        return prt >= 0 && prt<=200;
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }
}
