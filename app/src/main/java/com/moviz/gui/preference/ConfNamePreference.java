package com.moviz.gui.preference;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

/**
 * Created by Matteo on 31/10/2016.
 */

public class ConfNamePreference extends ValidatedEditTextPreference {
    public ConfNamePreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public ConfNamePreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public ConfNamePreference(Context ctx) {
        super(ctx);
    }

    @Override
    protected boolean onCheckValue(String value) {
        return value != null && value.length() > 0 && value.matches("^[a-zA-Z_0-9\\-]+$");
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_TEXT;
    }
}
