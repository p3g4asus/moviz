package com.moviz.gui.preference;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

public class IntPreference extends ValidatedEditTextPreference {
    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    protected int min = 0,max = Integer.MAX_VALUE;
    public IntPreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        min = attrs.getAttributeIntValue(null,"min",0);
        max = attrs.getAttributeIntValue(null,"max",Integer.MAX_VALUE);
    }

    public IntPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        min = attrs.getAttributeIntValue(null,"min",0);
        max = attrs.getAttributeIntValue(null,"max",Integer.MAX_VALUE);
    }

    public IntPreference(Context ctx) {
        super(ctx);
    }

    @Override
    protected boolean onCheckValue(String value) {
        int prt;
        try {
            prt = Integer.parseInt(value);
        } catch (Exception e) {
            return false;
        }
        return prt >= min && prt<=max;
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }

}
