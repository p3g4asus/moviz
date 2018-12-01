package com.moviz.gui.preference;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class DynamicListPreference extends ListPreference {
    public interface DynamicListPreferenceOnClickListener {
        public void onClick(DynamicListPreference preference);
    }

    private DynamicListPreferenceOnClickListener mOnClickListner;

    public DynamicListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicListPreference(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        if (mOnClickListner != null)
            mOnClickListner.onClick(this);
        else
            super.onClick();
    }

    public void openList() {
        super.onClick();
    }

    public void setOnClickListner(DynamicListPreferenceOnClickListener l) {
        mOnClickListner = l;
    }

}