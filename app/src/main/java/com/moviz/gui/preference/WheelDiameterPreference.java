package com.moviz.gui.preference;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

/**
 * Created by Matteo on 30/10/2016.
 */

public class WheelDiameterPreference extends ValidatedEditTextPreference {
    public WheelDiameterPreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public WheelDiameterPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public WheelDiameterPreference(Context ctx) {
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
        return prt >= 200 && prt <= 1016;
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }
}
