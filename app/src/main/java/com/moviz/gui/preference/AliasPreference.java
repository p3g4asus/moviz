package com.moviz.gui.preference;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

public class AliasPreference extends ValidatedEditTextPreference {
    public AliasPreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public AliasPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public AliasPreference(Context ctx) {
        super(ctx);
    }

    @Override
    protected boolean onCheckValue(String value) {
        return value != null && value.matches("[a-zA-Z][a-zA-Z_0-9]+");
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_TEXT;
    }
}
