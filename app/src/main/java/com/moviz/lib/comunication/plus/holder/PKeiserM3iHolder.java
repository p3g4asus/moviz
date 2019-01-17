package com.moviz.lib.comunication.plus.holder;

import android.os.Parcel;

public class PKeiserM3iHolder extends PPafersHolder {

    @Override
    public String getTableName() {
        return "keiserSV";
    }

    public PKeiserM3iHolder(Parcel p) {
        super(p);
    }
    public PKeiserM3iHolder() {
        super();
    }
    public PKeiserM3iHolder(PKeiserM3iHolder p) {
        super(p);
    }
    public static final Creator<PKeiserM3iHolder> CREATOR = new Creator<PKeiserM3iHolder>() {
        @Override
        public PKeiserM3iHolder createFromParcel(Parcel parcel) {
            return new PKeiserM3iHolder(parcel);
        }

        @Override
        public PKeiserM3iHolder[] newArray(int i) {
            return new PKeiserM3iHolder[i];
        }
    };
}
