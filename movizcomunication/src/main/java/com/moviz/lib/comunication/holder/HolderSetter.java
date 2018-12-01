package com.moviz.lib.comunication.holder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


public class HolderSetter extends ArrayList<Holder> {
    private static final long serialVersionUID = -7756846925142407644L;

    public boolean set(Holder h) {
        int idx;
        if ((idx = indexOf(h)) >= 0) {
            get(idx).copyValueFrom(h);
            return false;
        } else {
            add(h);
            return true;
        }
    }

    public Map<String, ? extends Holder> toMap(Id2Key v) {
        Map<String, Holder> mp = new LinkedHashMap<String, Holder>();
        for (Holder h : this) {
            mp.put(v == null ? h.getId() : v.convert(h.getId()), h);
        }
        return mp;
    }

    public void set(HolderSetter hs) {
        int idx;
        for (Holder h : hs) {
            if ((idx = indexOf(h)) >= 0) {
                get(idx).copyValueFrom(h);
            } else {
                add(h);
            }
        }
    }

    public Holder get(String h) {
        int idx;
        if ((idx = indexOf(new Holder("tm__", h, null))) >= 0) {
            return get(idx);
        } else {
            return null;
        }
    }

    public Holder getP(String parz) {
        for (Holder h : this) {
            if (h.getId().indexOf(parz) >= 0)
                return h;
        }
        return null;
    }

    public boolean unset(Holder h) {
        int idx;
        if ((idx = indexOf(h)) >= 0) {
            remove(idx);
            return true;
        } else
            return false;
    }


}
