package com.moviz.lib.comunication.holder;

import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.EncDec;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceHolder implements EncDec, Holderable, Comparable<DeviceHolder> {
    protected String address = "";
    protected long id = -1;
    protected String description = "";
    protected String name = "";
    protected String alias = "device0";
    protected String additionalSettings = "";
    protected DeviceType type = DeviceType.pafers;
    protected boolean enabled = false;
    public static String CONF_DATA_TERMINATOR = "----";
    public static Pattern CONF_DATA_STARTER = Pattern.compile("^@([^ ]+) ([01])$");

    public String getAdditionalSettings() {
        return additionalSettings;
    }

    public void setAdditionalSettings(String additionalSettings) {
        this.additionalSettings = additionalSettings;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        return (int) id;
    }

    public DeviceHolder(long i, String addr, String nm, String al, DeviceType tp, String desc, String addit, boolean ena) {
        id = i;
        address = addr;
        description = desc;
        name = nm;
        alias = al;
        type = tp;
        enabled = ena;
        additionalSettings = addit;
    }

    public DeviceHolder() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DeviceHolder))
            return false;
        else {
            DeviceHolder u = (DeviceHolder) o;
            return u.id == id;
        }
    }

    @Override
    public String toString() {
        return id + " / " + address + " / " + name + " (" + alias + ") / " + description + " / " + additionalSettings + " [" + enabled + "]";
    }

    public void copyFrom(DeviceHolder w) {
        id = w.id;
        address = w.address;
        description = w.description;
        name = w.name;
        alias = w.alias;
        type = w.type;
        enabled = w.enabled;
        additionalSettings = w.additionalSettings;
    }

    public DeviceHolder(DeviceHolder w) {
        copyFrom(w);
    }


    @Override
    public com.moviz.lib.comunication.holder.HolderSetter toHolder(Class<? extends Holder> cl,
                                                                   Class<? extends com.moviz.lib.comunication.holder.HolderSetter> cllist, String pref) {
        com.moviz.lib.comunication.holder.HolderSetter rv = null;
        try {
            rv = cllist.newInstance();
            Holder hld = cl.newInstance();
            hld.setId(pref + "id");
            hld.sO(id);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "address");
            hld.sO(address);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "description");
            hld.sO(description);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "name");
            hld.sO(name);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "alias");
            hld.sO(alias);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "type");
            hld.sO((byte) (type == null ? DeviceType.types.length : type.ordinal()));
            hld.setPrint(new MSTimePrinter());
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "additionalsettings");
            hld.sO(additionalSettings);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "enabled");
            hld.sO((byte) (enabled ? 1 : 0));
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
        address = hs.get(pref + "address").getString();
        description = hs.get(pref + "description").getString();
        name = hs.get(pref + "name").getString();
        alias = hs.get(pref + "alias").getString();
        int v;
        type = (v = hs.get(pref + "type").getInt()) >= DeviceType.types.length ? null : DeviceType.types[v];
        additionalSettings = hs.get(pref + "additionalsettings").getString();
        enabled = hs.get(pref + "enabled").getInt() != 0;
    }

    @Override
    public void encode(com.moviz.lib.comunication.IEncoder enc, ByteBuffer bb) {
        enc.encodeInt((int) id, bb);
        enc.encodeString(address, bb);
        enc.encodeString(description, bb);
        enc.encodeString(name, bb);
        enc.encodeString(alias, bb);
        enc.encodeByte(type == null ? DeviceType.types.length : type.ordinal(), bb);
        enc.encodeString(additionalSettings, bb);
        enc.encodeByte(enabled ? 1 : 0, bb);
    }

    @Override
    public int eSize(com.moviz.lib.comunication.IEncoder enc) {
        return enc.getIntSize() +//id
                enc.getStringSize(address) +//address
                enc.getStringSize(description) +//description
                enc.getStringSize(name) +//name
                enc.getStringSize(alias) +//alias
                enc.getByteSize() +//type
                enc.getStringSize(additionalSettings) +//additionalSettings
                enc.getByteSize();//enabled
    }

    @Override
    public EncDec decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer b) {
        id = dec.decodeInt(b);
        address = dec.decodeString(b);
        description = dec.decodeString(b);
        name = dec.decodeString(b);
        alias = dec.decodeString(b);
        int v;
        type = (v = dec.decodeByte(b)) >= DeviceType.types.length ? null : DeviceType.types[v];
        additionalSettings = dec.decodeString(b);
        enabled = dec.decodeByte(b) != 0;
        return this;
    }

    @Override
    public int compareTo(DeviceHolder arg0) {
        return Long.compare(id, arg0.id);
    }

    public String getConfData(Map<DeviceType, String[]> type2confsave) {
        String rv = "@" + getAddress() + " " + (isEnabled() ? "1" : "0") + "\n";
        Map<String, String> adds = deserializeAdditionalSettings();
        String[] vv = type2confsave == null ? adds.keySet().toArray(new String[0]) : type2confsave.get(getType());
        if (vv != null) {
            for (String v : vv) {
                String confval = adds.get(v);
                if (confval != null) {
                    rv += v + "=" + confval + "\n";
                }
            }
        }
        rv += CONF_DATA_TERMINATOR + "\n";
        return rv;
    }

    public boolean parseConfData(String[] lines, int start) {
        Map<String, String> additionalS = null;
        Matcher devicematch;
        String s, key, value;
        boolean rv = false;
        DeviceHolder currentd = null;
        for (int i = start; i < lines.length; i++) {
            s = lines[i];
            devicematch = CONF_DATA_STARTER.matcher(s);
            if (devicematch.matches()) {
                address = devicematch.group(1);
                enabled = devicematch.group(2).charAt(0) == '1';
                additionalS = deserializeAdditionalSettings();
                currentd = this;
            } else if (currentd != null) {
                if (s.equals(CONF_DATA_TERMINATOR)) {
                    currentd.serializeAdditionalSettings(additionalS, false);

                } else {
                    int sidx = s.indexOf('=');
                    if (sidx > 0) {
                        key = s.substring(0, sidx);
                        value = sidx + 1 < s.length() ? s.substring(sidx + 1) : "";
                        s = additionalS.get(key);
                        if (s == null || !s.equals(value)) {
                            additionalS.put(key, value);
                            rv = true;
                        }
                    }
                }
            }
        }
        return rv;
    }

    public String serializeAdditionalSettings(Map<String, ?> all, boolean prefixAdded) {
        String ser = "";
        String startswith = prefixAdded ? "pref_devicepriv_" + type.name() + "_" + id + "_" : "";
        int lensw = startswith.length();
        for (Map.Entry<String, ?> sett : all.entrySet()) {
            String key = sett.getKey();
            if (!prefixAdded || (key.startsWith(startswith) && key.length() > lensw)) {
                ser += key.substring(lensw) + "=" + sett.getValue().toString() + "\n";
            }
        }
        return additionalSettings = ser;
    }

    public Map<String, String> deserializeAdditionalSettings() {
        Map<String, String> rv = new HashMap<>();
        String[] splitted = additionalSettings.split("\n");
        int idx;
        for (String s : splitted) {
            idx = s.indexOf('=');
            if (idx > 0) {
                rv.put(s.substring(0, idx), idx + 1 < s.length() ? s.substring(idx + 1) : "");
            }
        }
        return rv;
    }
}
