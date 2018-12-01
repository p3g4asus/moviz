package com.moviz.gui.preference;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.widget.EditText;

import org.json.JSONArray;

/**
 * Created by Matteo on 13/10/2017.
 */

public class GearFactorPreference extends ValidatedEditTextPreference {
    public GearFactorPreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public GearFactorPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public GearFactorPreference(Context ctx) {
        super(ctx);
    }

    @Override
    protected boolean onCheckValue(String value) {
        try {
            value = "["+value+"]";
            JSONArray jsa = new JSONArray(value);
            for (int i = 0; i<jsa.length(); i++) {
                if (jsa.getDouble(i)<=0)
                    throw new IllegalArgumentException("Cannot be <=0");
            }
            return jsa.length()>0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_TEXT_VARIATION_PHONETIC;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText et = getEditText();
        et.setKeyListener(DigitsKeyListener.getInstance("0123456789.,"));
    }
}
