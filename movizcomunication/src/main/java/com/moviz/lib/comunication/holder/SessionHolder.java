package com.moviz.lib.comunication.holder;

import com.moviz.lib.comunication.EncDec;
import com.moviz.lib.comunication.IDecoder;
import com.moviz.lib.comunication.IEncoder;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SessionHolder implements EncDec, Holderable {
    protected long id = -1;
    protected long mainSessionId = -1;
    protected com.moviz.lib.comunication.holder.DeviceHolder device = null;
    protected long dateStart = 0;
    protected String settings;
    protected com.moviz.lib.comunication.holder.UserHolder user;
    protected List<DeviceUpdate> values = new ArrayList<DeviceUpdate>();
    protected com.moviz.lib.comunication.holder.HolderSetter holders = null;
    protected Class<? extends com.moviz.lib.comunication.holder.UserHolder> userClass = com.moviz.lib.comunication.holder.UserHolder.class;
    protected Class<? extends com.moviz.lib.comunication.holder.DeviceHolder> deviceClass = com.moviz.lib.comunication.holder.DeviceHolder.class;
    protected Class<? extends com.moviz.lib.comunication.holder.HolderSetter> holderClass = com.moviz.lib.comunication.holder.HolderSetter.class;

    public long getMainSessionId() {
        return mainSessionId;
    }

    public void setMainSessionId(long mainSessionId) {
        this.mainSessionId = mainSessionId;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public com.moviz.lib.comunication.holder.DeviceHolder getDevice() {
        return device;
    }

    public void setDevice(com.moviz.lib.comunication.holder.DeviceHolder device) {
        this.device = device;
    }

    public long getDateStart() {
        return dateStart;
    }

    public void setDateStart(long dateStart) {
        this.dateStart = dateStart;
    }

    public com.moviz.lib.comunication.holder.UserHolder getUser() {
        return user;
    }

    public void setUser(com.moviz.lib.comunication.holder.UserHolder user) {
        this.user = user;
    }

    public void setValues(List<DeviceUpdate> values) {
        this.values = values;
    }

    public com.moviz.lib.comunication.holder.HolderSetter getHolders() {
        if (holders == null)
            holders = newHolder();
        return holders;
    }

    public SessionHolder(long idn, long mId, com.moviz.lib.comunication.holder.DeviceHolder dev, long date, com.moviz.lib.comunication.holder.UserHolder usr, String sett) {
        id = idn;
        mainSessionId = mId;
        device = dev;
        dateStart = date;
        user = usr;
        settings = sett;
    }

    public void newHolder(com.moviz.lib.comunication.holder.Holder h) {
        if (holders == null)
            holders = newHolder();
        holders.set(h);
    }

    public void newHolder(com.moviz.lib.comunication.holder.HolderSetter h) {
        if (holders == null)
            holders = newHolder();
        holders.set(h);
    }

    public SessionHolder(SessionHolder s) {
        copyFrom(s);
    }

    public void copyFrom(SessionHolder s) {
        id = s.id;
        mainSessionId = s.mainSessionId;
        if (s.device != null) {
            if (device == null)
                device = newDevice();
            device.copyFrom(s.device);
        } else
            device = null;
        dateStart = s.dateStart;
        if (s.user != null) {
            if (user == null)
                user = newUser();
            user.copyFrom(s.user);
        } else
            user = null;
        settings = s.settings;
        values.addAll(s.values);
        if (s.holders != null) {
            if (holders == null)
                holders = newHolder();
            holders.addAll(s.holders);
        } else
            holders = null;
    }

    public SessionHolder() {
    }

    public void addValue(DeviceUpdate v) {
        values.add(v);
    }

    public void addValues(ArrayList<DeviceUpdate> v) {
        values.addAll(v);
    }

    public List<DeviceUpdate> getValues() {
        return values;
    }

    public void prepareToValues(int num) {
        values.clear();
        ((ArrayList<DeviceUpdate>) values).ensureCapacity(num);
    }

    protected com.moviz.lib.comunication.holder.HolderSetter newHolder() {
        try {
            return holderClass.newInstance();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    protected com.moviz.lib.comunication.holder.DeviceHolder newDevice() {
        try {
            return deviceClass.newInstance();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    protected com.moviz.lib.comunication.holder.UserHolder newUser() {
        try {
            return userClass.newInstance();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int hashCode() {
        return (int) id;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SessionHolder))
            return false;
        else {
            SessionHolder u = (SessionHolder) o;
            return u.id == id;
        }
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        String s = String.format("%s - %s - %s", sdf.format(new Date(dateStart)), user.getName(), device.getAlias());
        if (holders == null)
            holders = newHolder();
        for (com.moviz.lib.comunication.holder.Holder h : holders) {
            s += " - " + h;
        }
        return s;
    }

    public String toString(String datef) {
        SimpleDateFormat sdf = new SimpleDateFormat(datef + " HH:mm:ss");
        String s = String.format("%s - %s - %s", sdf.format(new Date(dateStart)), user.getName(), device.getAlias());
        if (holders == null)
            holders = newHolder();
        for (com.moviz.lib.comunication.holder.Holder h : holders) {
            s += " - " + h;
        }
        return s;
    }

    public String toString2(String datef) {
        SimpleDateFormat sdf = new SimpleDateFormat(datef + " HH:mm:ss");
        return String.format("%s - %s - ", sdf.format(new Date(dateStart)), user.getName());
    }


    @Override
    public com.moviz.lib.comunication.holder.HolderSetter toHolder(Class<? extends com.moviz.lib.comunication.holder.Holder> cl,
                                                                   Class<? extends com.moviz.lib.comunication.holder.HolderSetter> cllist, String pref) {
        com.moviz.lib.comunication.holder.HolderSetter rv = null;
        try {

            rv = cllist.newInstance();
            com.moviz.lib.comunication.holder.Holder hld = cl.newInstance();
            hld.setId(pref + "id");
            hld.sO(id);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "mainsessionid");
            hld.sO(mainSessionId);
            rv.add(hld);

            if (device == null)
                device = newDevice();
            if (user == null)
                user = newUser();
            rv.addAll(device.toHolder(cl, cllist, pref + "device."));
            rv.addAll(user.toHolder(cl, cllist, pref + "user."));

            hld = cl.newInstance();
            hld.setId(pref + "settings");
            hld.sO(settings);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "datestart");
            hld.sO(dateStart);
            hld.setPrint(new com.moviz.lib.comunication.holder.DatePrinter());
            rv.add(hld);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rv;
    }

    @Override
    public void fromHolder(com.moviz.lib.comunication.holder.HolderSetter hs, String pref) {
        id = hs.get(pref + "id").getLong();
        mainSessionId = hs.get(pref + "mainsessionid").getLong();
        if (device == null)
            device = newDevice();
        if (user == null)
            user = newUser();
        device.fromHolder(hs, pref + "device.");
        user.fromHolder(hs, pref + "user.");
        dateStart = hs.get(pref + "datestart").getLong();
        settings = hs.get(pref + "settings").getString();
    }

    @Override
    public void encode(IEncoder enc, ByteBuffer bb) {
        enc.encodeInt((int) id, bb);
        enc.encodeInt((int) mainSessionId, bb);
        enc.encodeInt((int) (dateStart / 1000), bb);
        enc.encodeString(settings, bb);
        device.encode(enc, bb);
        user.encode(enc, bb);
    }

    @Override
    public int eSize(IEncoder enc) {
        return enc.getIntSize() +///id;
                enc.getIntSize() +///mainSessionId;
                enc.getIntSize() +///dateStart;
                enc.getStringSize(settings) +///settings;
                (device = device == null ? newDevice() : device).eSize(enc) +//device
                (user = user == null ? newUser() : user).eSize(enc);///user
    }

    @Override
    public EncDec decode(IDecoder dec, ByteBuffer b) {
        id = dec.decodeInt(b);
        mainSessionId = dec.decodeInt(b);
        dateStart = ((long) dec.decodeInt(b)) * 1000l;
        settings = dec.decodeString(b);
        if (device == null)
            device = newDevice();
        device.decode(dec, b);
        if (user == null)
            user = newUser();
        user.decode(dec, b);
        return this;
    }

}
