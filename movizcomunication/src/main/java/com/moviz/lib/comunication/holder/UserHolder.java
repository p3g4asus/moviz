package com.moviz.lib.comunication.holder;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class UserHolder implements Holderable, com.moviz.lib.comunication.EncDec {
    protected double height = 0.0;
    protected double weight = 0.0;
    protected byte age = (byte) 0;
    protected boolean isMale = true;
    protected long birthDay = Long.MAX_VALUE;
    protected String name = "";
    protected long id = -1;

    public long getId() {
        return id;
    }

    public void setId(long l) {
        this.id = l;
    }

    public static int getAge(Date dt) {

        GregorianCalendar cal = new GregorianCalendar();
        int y, m, d, a;

        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH);
        d = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(dt);
        a = y - cal.get(Calendar.YEAR);
        if ((m < cal.get(Calendar.MONTH))
                || ((m == cal.get(Calendar.MONTH)) && (d < cal
                .get(Calendar.DAY_OF_MONTH)))) {
            --a;
        }
        if (a < 0)
            throw new IllegalArgumentException("Age < 0");
        return a;
    }

    private void setAll(long idn, String n, boolean male, double w, double h, byte a, boolean metric) {
        if (metric) {
            height = h;
            weight = w;
        } else {
            height = com.moviz.lib.utils.UnitUtil.cm2inch(h);
            weight = com.moviz.lib.utils.UnitUtil.kg2pound(w);
        }
        name = n;
        isMale = male;
        age = a;
        id = idn;
    }

    public UserHolder(long idn, String nm, boolean male, double w, double h, byte a, boolean metric) {
        setAll(idn, nm, male, w, h, a, metric);
    }

    public UserHolder(long idn, String nm, boolean male, double w, double h, long brd, boolean metric) {
        setAll(idn, nm, male, w, h, (byte) getAge(new Date(brd)), metric);
        birthDay = brd;
    }

    public UserHolder() {
    }

    public UserHolder(UserHolder u) {
        copyFrom(u);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof UserHolder))
            return false;
        else {
            UserHolder u = (UserHolder) o;
            return u.id == id;
        }
    }

    @Override
    public String toString() {
        return String.format("User(\"%s\",'%c',%d,%.0f,%.0f)", name, isMale ? 'M' : 'F', age, height, weight);
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public byte getAge() {
        return age;
    }

    public void setAge(byte age) {
        this.age = age;
    }

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean isMale) {
        this.isMale = isMale;
    }

    public long getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(long birthDay) {
        this.birthDay = birthDay;
        this.age = (byte) getAge(new Date(birthDay));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void copyFrom(UserHolder u) {
        height = u.height;
        weight = u.weight;
        name = u.name;
        isMale = u.isMale;
        age = u.age;
        id = u.id;
        birthDay = u.birthDay;
    }

    @Override
    public void encode(com.moviz.lib.comunication.IEncoder enc, ByteBuffer bb) {
        enc.encodeInt((int) id, bb);
        enc.encodeByte(age, bb);
        enc.encodeByte(isMale ? 1 : 0, bb);
        enc.encodeString(name, bb);
        enc.encodeDouble(height, bb);
        enc.encodeDouble(weight, bb);
    }

    @Override
    public int eSize(com.moviz.lib.comunication.IEncoder enc) {
        return enc.getIntSize() +//id
                enc.getByteSize() +//age
                enc.getByteSize() +//isMale
                enc.getStringSize(name) +//name
                enc.getDoubleSize() +//height
                enc.getDoubleSize();//weight
    }

    @Override
    public com.moviz.lib.comunication.EncDec decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer b) {
        id = dec.decodeInt(b);
        age = (byte) dec.decodeByte(b);
        isMale = dec.decodeByte(b) != 0;
        name = dec.decodeString(b);
        height = dec.decodeDouble(b);
        weight = dec.decodeDouble(b);
        return this;
    }

    @Override
    public HolderSetter toHolder(Class<? extends Holder> cl,
                                 Class<? extends HolderSetter> cllist, String pref) {
        HolderSetter rv = null;
        try {
            rv = cllist.newInstance();
            Holder hld = cl.newInstance();
            hld.setId(pref + "id");
            hld.sO(id);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "name");
            hld.sO(name);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "age");
            hld.sO(age);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "ismale");
            hld.sO((byte) (isMale ? 1 : 0));
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "birthday");
            hld.sO(birthDay);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "height");
            hld.sO(height);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "weight");
            hld.sO(weight);
            rv.add(hld);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rv;
    }

    @Override
    public void fromHolder(HolderSetter hs, String pref) {
        id = hs.get(pref + "id").getLong();
        birthDay = hs.get(pref + "birthday").getLong();
        age = hs.get(pref + "age").getByte();
        name = hs.get(pref + "name").getString();
        isMale = hs.get(pref + "ismale").getByte() != 0;
        height = hs.get(pref + "height").getDouble();
        weight = hs.get(pref + "weight").getDouble();
    }
}
