package com.moviz.lib.comunication.holder;

import java.nio.ByteBuffer;

public class StatusHolder implements com.moviz.lib.comunication.EncDec, Holderable {
    public String lastAction = null;
    public com.moviz.lib.comunication.DeviceStatus lastStatus = null;
    public SessionHolder session = null;
    public com.moviz.lib.comunication.tcp.TCPStatus tcpStatus = null;
    public String tcpAddress = "";
    public int updateN = 0;
    protected HolderSetter holders = null;
    protected Class<? extends SessionHolder> sessionClass = SessionHolder.class;
    protected Class<? extends HolderSetter> holderClass = HolderSetter.class;

    public StatusHolder() {

    }

    public StatusHolder(StatusHolder s) {
        copyFrom(s);
    }

    public void copyFrom(StatusHolder s) {
        lastAction = s.lastAction;
        lastStatus = s.lastStatus;
        if (s.session != null) {
            if (session == null)
                session = newSession();
            session.copyFrom(s.session);
        } else
            session = null;
        tcpStatus = s.tcpStatus;
        tcpAddress = s.tcpAddress;
        updateN = s.updateN;
        if (s.holders != null) {
            if (holders == null)
                holders = newHolder();
            holders.addAll(s.holders);
        } else
            holders = null;
    }

    public HolderSetter getHolders() {
        if (holders == null)
            holders = newHolder();
        return holders;
    }

    public void newHolder(Holder h) {
        if (holders == null)
            holders = newHolder();
        holders.set(h);
    }

    public void newHolder(HolderSetter h) {
        if (holders == null)
            holders = newHolder();
        holders.set(h);
    }

    protected HolderSetter newHolder() {
        try {
            return holderClass.newInstance();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    protected SessionHolder newSession() {
        try {
            return sessionClass.newInstance();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void encode(com.moviz.lib.comunication.IEncoder e, ByteBuffer b) {
        e.encodeString(lastAction, b);
        e.encodeByte((byte) (lastStatus == null ? 120 : lastStatus.ordinal()), b);
        e.encodeByte((byte) (tcpStatus == null ? 120 : tcpStatus.ordinal()), b);
        e.encodeString(tcpAddress, b);
        e.encodeInt(updateN, b);
        SessionHolder ses = session == null ? newSession() : session;
        ses.encode(e, b);
        if (holders == null)
            holders = newHolder();
        e.encodeByte(holders.size(), b);
        for (Holder h : holders) {
            h.encode(e, b);
        }

    }

    @Override
    public int eSize(com.moviz.lib.comunication.IEncoder e) {
        int sz = e.getStringSize(lastAction) + //lastAction
                e.getByteSize() +//lastStatus
                e.getByteSize() + //tcpStatus
                e.getStringSize(tcpAddress) + //tcpAddress
                e.getIntSize() + //updateN
                (session = session == null ? newSession() : session).eSize(e);//session
        sz += e.getByteSize();
        if (holders == null)
            holders = newHolder();
        else {
            for (Holder h : holders) {
                sz += h.eSize(e);
            }
        }
        return sz;
    }

    @Override
    public com.moviz.lib.comunication.EncDec decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer b) {
        lastAction = dec.decodeString(b);
        int v = dec.decodeByte(b);
        lastStatus = v >= com.moviz.lib.comunication.DeviceStatus.statuses.length ? null : com.moviz.lib.comunication.DeviceStatus.statuses[v];
        v = dec.decodeByte(b);
        tcpStatus = v >= com.moviz.lib.comunication.tcp.TCPStatus.statuses.length ? null : com.moviz.lib.comunication.tcp.TCPStatus.statuses[v];
        tcpAddress = dec.decodeString(b);
        updateN = dec.decodeInt(b);
        if (session == null)
            session = newSession();
        session.decode(dec, b);
        int n = dec.decodeByte(b);
        if (holders == null)
            holders = newHolder();
        else
            holders.clear();
        for (int i = 0; i < n; i++) {
            Holder h = new Holder();
            h.decode(dec, b);
            holders.add(h);
        }
        return this;
    }

    @Override
    public HolderSetter toHolder(Class<? extends Holder> cl, Class<? extends HolderSetter> cllist, String pref) {
        HolderSetter rv = null;
        try {
            rv = cllist.newInstance();
            Holder hld = cl.newInstance();
            hld.setId(pref + "lastaction");
            hld.sO(lastAction);
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "laststatus");
            hld.sO((byte) (lastStatus == null ? 120 : lastStatus.ordinal()));
            hld.setPrint(new DeviceStatusPrinter());
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "tcpstatus");
            hld.sO((byte) (tcpStatus == null ? 120 : tcpStatus.ordinal()));
            hld.setPrint(new TCPStatusPrinter());
            rv.add(hld);

            hld = cl.newInstance();
            hld.setId(pref + "tcpaddress");
            hld.sO(tcpAddress);
            rv.add(hld);

            if (session == null)
                session = newSession();
            rv.addAll(session.toHolder(cl, cllist, pref + "ses."));

            hld = cl.newInstance();
            hld.setId(pref + "updaten");
            hld.sO(updateN);
            rv.add(hld);

            if (holders == null)
                holders = newHolder();
            rv.addAll(holders);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rv;
    }

    @Override
    public void fromHolder(HolderSetter hs, String pref) {
        byte tmp;
        lastAction = hs.get(pref + "lastaction").getString();
        lastStatus = (tmp = hs.get(pref + "laststatus").getByte()) > com.moviz.lib.comunication.DeviceStatus.statuses.length ? null : com.moviz.lib.comunication.DeviceStatus.statuses[tmp];
        tcpStatus = (tmp = hs.get(pref + "tcpstatus").getByte()) > com.moviz.lib.comunication.tcp.TCPStatus.statuses.length ? null : com.moviz.lib.comunication.tcp.TCPStatus.statuses[tmp];
        tcpAddress = hs.get(pref + "tcpaddress").getString();
        updateN = hs.get(pref + "updaten").getInt();
        if (session == null)
            session = newSession();
        session.fromHolder(hs, pref + "ses.");
        int idx = hs.indexOf(pref + "updaten");
        if (holders == null)
            holders = newHolder();
        else
            holders.clear();
        for (int i = idx + 1; i < hs.size(); i++)
            holders.add(hs.get(i));
    }

}
