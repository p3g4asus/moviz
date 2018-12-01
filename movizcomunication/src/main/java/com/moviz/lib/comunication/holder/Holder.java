package com.moviz.lib.comunication.holder;

import com.moviz.lib.comunication.EncDec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Holder implements EncDec {

    public static enum Type {
        INVALID, BYTE, SHORT, INT, LONG, DOUBLE, STRING, OBJECT, OBJECTNULL, LIST, LISTNULL;
        public static Type[] types = Type.values();
    }

    protected Type t = Type.INVALID;
    protected String id = "";
    protected HolderPrinter print = null;
    protected String valString = null;
    protected EncDec valObject = null;
    protected List<?> valList = null;
    protected long valLong = Long.MIN_VALUE;
    protected double valDouble = Double.MIN_VALUE;
    protected String resString = "";

    public String getFmtString() {
        if (print == null || !print.getClass().equals(ResPrinter.class))
            return "";
        else {
            int idx = resString.lastIndexOf("|||");
            if (idx < 0 || idx + 3 >= resString.length())
                return "";
            else
                return resString.substring(idx + 3);
        }
    }

    public String getResString() {
        if (print == null || !print.getClass().equals(ResPrinter.class))
            return resString;
        else {
            int idx = resString.lastIndexOf("|||");
            if (idx < 0 || idx + 3 >= resString.length())
                return resString;
            else
                return resString.substring(0, idx);
        }

    }

    public void setPrint(HolderPrinter p) {
        print = p;
    }

    public HolderPrinter getPrint() {
        return print;
    }

    public void setResString(String resString) {
        this.resString = resString;
    }

    public Holder() {

    }

    public Holder(Holder h) {
        copyFrom(h);
    }

    public void copyFrom(Holder h) {
        t = h.t;
        id = h.id;
        valString = h.valString;
        valLong = h.valLong;
        valDouble = h.valDouble;
        valObject = h.valObject;
        valList = h.valList;
        print = h.print;
        resString = h.resString;
    }

    public void sO(String s) {
        sO(s, id, print);
    }

    public void sO(byte s) {
        sO(s, id, print);
    }

    public void sO(short s) {
        sO(s, id, print);
    }

    public void sO(int s) {
        sO(s, id, print);
    }

    public void sO(long s) {
        sO(s, id, print);
    }

    public void sO(double s) {
        sO(s, id, print);
    }

    public void sO(EncDec s) {
        sO(s, id, print);
    }

    public void sO(List<?> s) {
        sO(s, id, print);
    }

    public Holder(Type tp, String ty) {
        id = ty;
        t = tp;
        type2print();
    }

    protected void type2print() {
        if (print == null) {
            if (t == Type.STRING)
                print = new StringPrinter();
            else if (t == Type.BYTE || t == Type.SHORT || t == Type.INT || t == Type.LONG)
                print = new IntegerPrinter();
            else if (t == Type.DOUBLE)
                print = new FloatPrinter();
            else if (t == Type.LIST) {
                print = new com.moviz.lib.comunication.holder.ListPrinter();
            } else if (t == Type.OBJECT) {
                print = new ObjectPrinter();
            }
        }
    }

    public Holder(List<?> s, String ty, HolderPrinter prn) {
        sO(s, ty, prn);
    }

    protected void sO(List<?> s, String ty, HolderPrinter prn) {
        valList = s;
        id = ty;
        t = s == null ? Type.LISTNULL : Type.LIST;
        print = prn;
        type2print();
    }

    public Holder(String s, String ty, HolderPrinter prn) {
        sO(s, ty, prn);
    }

    protected void sO(String s, String ty, HolderPrinter prn) {
        valString = s;
        id = ty;
        t = Type.STRING;
        print = prn;
        type2print();
    }

    public Holder(byte s, String ty, HolderPrinter prn) {
        sO(s, ty, prn);
    }

    protected void sO(byte s, String ty, HolderPrinter prn) {
        valLong = s;
        id = ty;
        t = Type.BYTE;
        print = prn;
        type2print();
    }

    public Holder(short s, String ty, HolderPrinter prn) {
        sO(s, ty, prn);
    }

    protected void sO(short s, String ty, HolderPrinter prn) {
        valLong = s;
        id = ty;
        t = Type.SHORT;
        print = prn;
        type2print();
    }

    public Holder(int s, String ty, HolderPrinter prn) {
        sO(s, ty, prn);
    }

    protected void sO(int s, String ty, HolderPrinter prn) {
        valLong = s;
        id = ty;
        t = Type.INT;
        print = prn;
        type2print();
    }

    public Holder(long s, String ty, HolderPrinter prn) {
        sO(s, ty, prn);
    }

    public boolean isAbout(String superid) {
        return id.startsWith(superid);
    }

    public String getChildId() {
        return getParentId(0);
    }

    public String getParentId(int n) {
        int idx;
        String s = id;
        for (int i = 0; i < n + 1; i++) {
            idx = s.lastIndexOf(".");
            if (idx < 0)
                return i == n ? s : "";
            else if (i == n)
                return idx < s.length() - 1 ? s.substring(idx + 1) : "";
            else if (idx > 0)
                s = s.substring(0, idx);
            else if (idx == 0)
                return "";
        }
        return "";
    }

    protected void sO(long s, String ty, HolderPrinter prn) {
        valLong = s;
        id = ty;
        t = Type.LONG;
        print = prn;
        type2print();
    }

    public Holder(double s, String ty, HolderPrinter prn) {
        sO(s, ty, prn);
    }

    protected void sO(double s, String ty, HolderPrinter prn) {
        valDouble = s;
        id = ty;
        t = Type.DOUBLE;
        print = prn;
        type2print();
    }

    public Holder(EncDec s, String ty, HolderPrinter prn) {
        sO(s, ty, prn);
    }

    protected void sO(EncDec s, String ty, HolderPrinter prn) {
        valObject = s;
        id = ty;
        t = s == null ? Type.OBJECTNULL : Type.OBJECT;
        print = prn;
        type2print();
    }

    public int getInt() {
        return (int) valLong;
    }

    public byte getByte() {
        return (byte) valLong;
    }

    public short getShort() {
        return (short) valLong;
    }

    public long getLong() {
        return valLong;
    }

    public double getDouble() {
        return valDouble;
    }

    public String getString() {
        return valString;
    }

    public EncDec getObject() {
        return valObject;
    }

    public List<?> getList() {
        return valList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int c(Holder h) {
        if (t == Type.INVALID || t == Type.OBJECT || t == Type.OBJECTNULL || t == Type.LIST || t == Type.LISTNULL ||
                h.t == Type.INVALID || h.t == Type.OBJECT || h.t == Type.OBJECTNULL || h.t == Type.LIST || h.t == Type.LISTNULL)
            throw new IllegalArgumentException();
        if (h.t == t ||
                ((h.t == Type.BYTE || h.t == Type.SHORT || h.t == Type.INT || h.t == Type.LONG || h.t == Type.DOUBLE) &&
                        (t == Type.BYTE || t == Type.SHORT || t == Type.INT || t == Type.LONG || t == Type.DOUBLE))) {
            if (t == Type.STRING)
                return valString.compareTo(h.valString);
            else if (t == Type.DOUBLE && h.t != Type.DOUBLE)
                return Double.compare(valDouble, h.valLong);
            else if (t != Type.DOUBLE && h.t == Type.DOUBLE)
                return Double.compare(valLong, h.valDouble);
            else if (t == Type.DOUBLE && h.t == Type.DOUBLE)
                return Double.compare(valDouble, h.valDouble);
            else if (valLong > h.valLong)
                return 1;
            else if (h.valLong > valLong)
                return -1;
            else
                return 0;
        } else
            throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return print.printVal(this);
    }

    public String toString(Object s) {
        return print.printVal(this, s);
    }

    protected void encodeList(List<?> l, com.moviz.lib.comunication.IEncoder enc, ByteBuffer bb) {
        enc.encodeInt(l.size(), bb);
        for (Object o : l) {
            if (o == null) {
                enc.encodeByte(Type.OBJECTNULL.ordinal(), bb);
            } else if (o instanceof Double) {
                enc.encodeByte(Type.DOUBLE.ordinal(), bb);
                enc.encodeDouble(((Double) o).doubleValue(), bb);
            } else if (o instanceof Long) {
                enc.encodeByte(Type.LONG.ordinal(), bb);
                enc.encodeInt(((Long) o).intValue(), bb);
            } else if (o instanceof Integer) {
                enc.encodeByte(Type.INT.ordinal(), bb);
                enc.encodeInt(((Integer) o).intValue(), bb);
            } else if (o instanceof Short) {
                enc.encodeByte(Type.SHORT.ordinal(), bb);
                enc.encodeShort(((Short) o).shortValue(), bb);
            } else if (o instanceof Byte) {
                enc.encodeByte(Type.BYTE.ordinal(), bb);
                enc.encodeByte(((Byte) o).byteValue(), bb);
            } else if (o instanceof String) {
                enc.encodeByte(Type.STRING.ordinal(), bb);
                enc.encodeString(((String) o), bb);
            } else if (o instanceof EncDec) {
                enc.encodeByte(Type.OBJECT.ordinal(), bb);
                enc.encodeString(o.getClass().getName(), bb);
                ((EncDec) o).encode(enc, bb);
            }
        }
    }

    private int eListSize(List<?> l, com.moviz.lib.comunication.IEncoder enc) {
        int sz = enc.getIntSize();
        for (Object o : l) {
            sz += enc.getByteSize();
            if (o instanceof Double) {
                sz += enc.getDoubleSize();
            } else if (o instanceof Long) {
                sz += enc.getIntSize();
            } else if (o instanceof Integer) {
                sz += enc.getIntSize();
            } else if (o instanceof Short) {
                sz += enc.getShortSize();
            } else if (o instanceof Byte) {
                sz += enc.getByteSize();
            } else if (o instanceof String) {
                sz += enc.getStringSize((String) o);
            } else if (o instanceof EncDec) {
                sz += ((EncDec) o).eSize(enc) + enc.getStringSize(o.getClass().getName());
            }
        }
        return sz;
    }

    protected List<Object> decodeList(com.moviz.lib.comunication.IDecoder dec, ByteBuffer bb) {
        List<Object> rv = new ArrayList<Object>();
        int n = dec.decodeInt(bb);
        Type t;
        for (int i = 0; i < n; i++) {
            t = Type.types[dec.decodeByte(bb)];
            if (t == Type.STRING)
                rv.add(dec.decodeString(bb));
            else if (t == Type.BYTE)
                rv.add(dec.decodeByte(bb));
            else if (t == Type.SHORT)
                rv.add(dec.decodeShort(bb));
            else if (t == Type.INT || t == Type.LONG)
                rv.add(dec.decodeInt(bb));
            else if (t == Type.DOUBLE)
                rv.add(dec.decodeDouble(bb));
            else if (t == Type.OBJECT) {
                String cn = dec.decodeString(bb);
                try {
                    EncDec ed = (EncDec) Class.forName(cn).newInstance();
                    rv.add(ed.decode(dec, bb));
                } catch (Exception e) {
                    e.printStackTrace();
                    rv.add(null);
                }
            }
        }
        return rv;
    }

    public void encode(com.moviz.lib.comunication.IEncoder enc, ByteBuffer bb) {
        enc.encodeString(id, bb);
        enc.encodeByte(t.ordinal(), bb);
        enc.encodeString(print.getClass().getName(), bb);
        if (t == Type.STRING)
            enc.encodeString(valString, bb);
        else if (t == Type.BYTE)
            enc.encodeByte((int) valLong, bb);
        else if (t == Type.SHORT)
            enc.encodeShort((int) valLong, bb);
        else if (t == Type.INT || t == Type.LONG)
            enc.encodeInt((int) valLong, bb);
        else if (t == Type.DOUBLE)
            enc.encodeDouble(valDouble, bb);
        else if (t == Type.LIST) {
            encodeList(valList, enc, bb);
        } else if (t == Type.OBJECT) {
            enc.encodeString(valObject.getClass().getName(), bb);
            valObject.encode(enc, bb);
        }
    }

    public int eSize(com.moviz.lib.comunication.IEncoder enc) {
        int sz = enc.getStringSize(id) + enc.getByteSize() + enc.getStringSize(print.getClass().getName());
        if (t == Type.STRING)
            return enc.getStringSize(valString) + sz;
        else if (t == Type.BYTE)
            return enc.getByteSize() + sz;
        else if (t == Type.SHORT)
            return enc.getShortSize() + sz;
        else if (t == Type.INT || t == Type.LONG)
            return enc.getIntSize() + sz;
        else if (t == Type.DOUBLE)
            return enc.getDoubleSize() + sz;
        else if (t == Type.LIST)
            return sz + eListSize(valList, enc);
        else if (t == Type.OBJECT)
            return valObject.eSize(enc) + sz + enc.getStringSize(valObject.getClass().getName());
        else
            return sz;
    }

    @Override
    public EncDec decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer bb) {
        String type = dec.decodeString(bb);
        Type t = Type.types[dec.decodeByte(bb)];
        String cn = dec.decodeString(bb);
        HolderPrinter prn = null;
        try {
            Object o = Class.forName(cn).newInstance();
            if (o instanceof HolderPrinter)
                prn = (HolderPrinter) o;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (t == Type.STRING)
            sO(dec.decodeString(bb), type, prn);
        else if (t == Type.BYTE)
            sO(dec.decodeByte(bb), type, prn);
        else if (t == Type.SHORT)
            sO(dec.decodeShort(bb), type, prn);
        else if (t == Type.INT || t == Type.LONG)
            sO(dec.decodeInt(bb), type, prn);
        else if (t == Type.DOUBLE)
            sO(dec.decodeDouble(bb), type, prn);
        else if (t == Type.LIST || t == Type.LISTNULL) {
            List<?> lst = t == Type.LISTNULL ? null : decodeList(dec, bb);
            sO(lst, type, prn);
        } else if (t == Type.OBJECT) {
            EncDec ed = null;
            try {
                Object o = Class.forName(dec.decodeString(bb)).newInstance();
                ed = (EncDec) o;
            } catch (Exception e) {
                ed = null;
            }
            if (ed == null)
                sO(ed, type, prn);
            else
                sO(ed.decode(dec, bb), type, prn);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof String)
            return id.equals(o);
        else if (!(o instanceof Holder))
            return false;
        else
            return id.equals(((Holder) o).id);
    }

    public void copyValueFrom(Holder h) {
        valString = h.valString;
        valLong = h.valLong;
        valDouble = h.valDouble;
        valObject = h.valObject;
        valList = h.valList;
    }

}
