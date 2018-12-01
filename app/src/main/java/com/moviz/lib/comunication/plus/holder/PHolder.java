package com.moviz.lib.comunication.plus.holder;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class PHolder extends com.moviz.lib.comunication.holder.Holder implements Parcelable {

    public PHolder(byte s, String ty, com.moviz.lib.comunication.holder.HolderPrinter prn) {
        super(s, ty, prn);
    }

    public PHolder(short s, String ty, com.moviz.lib.comunication.holder.HolderPrinter prn) {
        super(s, ty, prn);
    }

    public PHolder(int s, String ty, com.moviz.lib.comunication.holder.HolderPrinter prn) {
        super(s, ty, prn);
    }

    public PHolder(long s, String ty, com.moviz.lib.comunication.holder.HolderPrinter prn) {
        super(s, ty, prn);
    }

    public PHolder(double s, String ty, com.moviz.lib.comunication.holder.HolderPrinter prn) {
        super(s, ty, prn);
    }

    public PHolder(String s, String ty, com.moviz.lib.comunication.holder.HolderPrinter prn) {
        super(s, ty, prn);
    }

    public PHolder(List<?> s, String ty, com.moviz.lib.comunication.holder.HolderPrinter prn) {
        super(s, ty, prn);
    }

    public PHolder(com.moviz.lib.comunication.holder.Holder h) {
        super(h);
    }

    public PHolder() {
        super();
    }

    public String toDbVar() {
        String tp = "";
        if (t == Type.STRING)
            tp = " VARCHAR(255)";
        else if (t == Type.DOUBLE)
            tp = " real";
        else if (t == Type.BYTE || t == Type.SHORT || t == Type.INT || t == Type.LONG)
            tp = " integer";
        else
            return "";
        return id + tp + " not null";
    }

    public void toDBValue(ContentValues c) {
        if (t == Type.BYTE || t == Type.SHORT || t == Type.INT || t == Type.LONG)
            c.put(getChildId(), valLong);
        else if (t == Type.STRING)
            c.put(getChildId(), valString);
        else if (t == Type.DOUBLE)
            c.put(getChildId(), valDouble);
    }

    public void fromCursor(Cursor c, String p) {
        int idx = c.getColumnIndex(p + getChildId());
        if (idx >= 0) {
            if (t == Type.BYTE || t == Type.SHORT || t == Type.INT || t == Type.LONG)
                valLong = c.getLong(idx);
            else if (t == Type.STRING)
                valString = c.getString(idx);
            else if (t == Type.DOUBLE)
                valDouble = c.getDouble(idx);
        }
    }

    public PHolder(Parcel p) {
        String id = p.readString();
        Type t = Type.types[p.readByte()];
        String cn = p.readString();
        com.moviz.lib.comunication.holder.HolderPrinter prn = null;
        try {
            Object o = Class.forName(cn).newInstance();
            if (o instanceof com.moviz.lib.comunication.holder.HolderPrinter)
                prn = (com.moviz.lib.comunication.holder.HolderPrinter) o;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (t == Type.STRING)
            sO(p.readString(), id, prn);
        else if (t == Type.BYTE)
            sO(p.readByte(), id, prn);
        else if (t == Type.SHORT)
            sO(p.readInt(), id, prn);
        else if (t == Type.INT)
            sO(p.readInt(), id, prn);
        else if (t == Type.LONG)
            sO(p.readLong(), id, prn);
        else if (t == Type.DOUBLE)
            sO(p.readDouble(), id, prn);
        else if (t == Type.OBJECT) {
            cn = p.readString();
            com.moviz.lib.comunication.EncDec ed = null;
            try {
                ed = p.readParcelable(Class.forName(cn).getClassLoader());
            } catch (Exception e) {
                e.printStackTrace();
            }
            sO(ed, id, prn);
        } else if (t == Type.LIST) {
            List<?> l = readList(p);
            sO(l, id, prn);
        }
    }

    public PHolder(Type tp, String i) {
        super(tp, i);
    }

    private List<?> readList(Parcel p) {
        int n = p.readInt();
        List<Object> rv = new ArrayList<Object>();
        for (int i = 0; i < n; i++) {
            t = Type.types[p.readByte()];
            if (t == Type.STRING)
                rv.add(p.readString());
            else if (t == Type.BYTE)
                rv.add(p.readByte());
            else if (t == Type.SHORT)
                rv.add((short) p.readInt());
            else if (t == Type.INT)
                rv.add(p.readInt());
            else if (t == Type.LONG)
                rv.add(p.readLong());
            else if (t == Type.DOUBLE)
                rv.add(p.readDouble());
            else if (t == Type.OBJECT) {
                String cn = p.readString();
                try {
                    rv.add(p.readParcelable(Class.forName(cn).getClassLoader()));
                } catch (Exception e) {
                    e.printStackTrace();
                    rv.add(null);
                }
            }
        }
        return rv;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void writeList(List<?> l, Parcel dest, int flags) {
        dest.writeInt(l.size());
        for (Object o : l) {
            if (o == null) {
                dest.writeByte((byte) Type.OBJECTNULL.ordinal());
            } else if (o instanceof Double) {
                dest.writeByte((byte) Type.DOUBLE.ordinal());
                dest.writeDouble(((Double) o).doubleValue());
            } else if (o instanceof Long) {
                dest.writeByte((byte) Type.LONG.ordinal());
                dest.writeLong(((Long) o).longValue());
            } else if (o instanceof Integer) {
                dest.writeByte((byte) Type.INT.ordinal());
                dest.writeInt(((Integer) o).intValue());
            } else if (o instanceof Short) {
                dest.writeByte((byte) Type.SHORT.ordinal());
                dest.writeInt(((Short) o).shortValue());
            } else if (o instanceof Byte) {
                dest.writeByte((byte) Type.BYTE.ordinal());
                dest.writeByte(((Byte) o).byteValue());
            } else if (o instanceof String) {
                dest.writeByte((byte) Type.STRING.ordinal());
                dest.writeString(((String) o));
            } else if (o instanceof com.moviz.lib.comunication.EncDec) {
                dest.writeByte((byte) Type.OBJECT.ordinal());
                dest.writeString(o.getClass().getName());
                dest.writeParcelable(((Parcelable) o), flags);
            }
        }

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeByte((byte) t.ordinal());
        dest.writeString(print.getClass().getName());
        if (t == Type.STRING)
            dest.writeString(valString);
        else if (t == Type.BYTE)
            dest.writeByte((byte) valLong);
        else if (t == Type.SHORT)
            dest.writeInt((int) valLong);
        else if (t == Type.INT)
            dest.writeInt((int) valLong);
        else if (t == Type.LONG)
            dest.writeLong(valLong);
        else if (t == Type.DOUBLE)
            dest.writeDouble(valDouble);
        else if (t == Type.OBJECT) {
            dest.writeString(valObject.getClass().getName());
            dest.writeParcelable((Parcelable) valObject, flags);
        } else if (t == Type.LIST)
            writeList(valList, dest, flags);

    }

    public static final Creator<PHolder> CREATOR = new Creator<PHolder>() {
        @Override
        public PHolder createFromParcel(Parcel parcel) {
            return new PHolder(parcel);
        }

        @Override
        public PHolder[] newArray(int i) {
            return new PHolder[i];
        }
    };

}
