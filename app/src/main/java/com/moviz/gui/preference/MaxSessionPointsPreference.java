package com.moviz.gui.preference;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

public class MaxSessionPointsPreference extends ValidatedEditTextPreference {
    public MaxSessionPointsPreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public MaxSessionPointsPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public MaxSessionPointsPreference(Context ctx) {
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
        return prt >= 0;
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }

}
