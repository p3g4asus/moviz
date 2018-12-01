package com.moviz.workers;

import com.moviz.gui.util.Messages;
import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PPafersHolder;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;
import com.moviz.lib.comunication.plus.holder.PStatusHolder;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.hw.DeviceListenerPlus;
import com.moviz.lib.hw.GenericDevice;
import com.moviz.lib.utils.DeviceTypeMaps;
import com.moviz.lib.utils.ParcelableMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class StatusReceiver implements DeviceListenerPlus {
    public static final int COMPLETESTATUS_REQ = 1;
    public static final int COMPLETESTATUS_RESP = 2;
    private Vector<AdvancedListener> mAdvancedListeners = new Vector<AdvancedListener>();
    protected PStatusHolder privStatus = new PStatusHolder();
    protected Map<PDeviceHolder, PStatusHolder> statusMap = new HashMap<PDeviceHolder, PStatusHolder>();
    protected Map<PDeviceHolder, PStatusHolder> fakeStatusMap = new HashMap<PDeviceHolder, PStatusHolder>();
    protected Map<PDeviceHolder, DeviceUpdate> updateMap = new HashMap<PDeviceHolder, DeviceUpdate>();
    protected Map<PDeviceHolder, DeviceUpdate> fakeUpdateMap = new HashMap<PDeviceHolder, DeviceUpdate>();
    public final static int MODIFIED_UPDATE = 1;
    public final static int MODIFIED_ACTION = 2;
    public final static int MODIFIED_HOLDERS = 4;
    public final static int MODIFIED_PRIVHOLDER = 8;
    public final static int MODIFIED_STATUS = 16;
    public final static int MODIFIED_USER = 32;
    public final static int MODIFIED_DEVICE = 64;
    //public final static int MODIFIED_PGRSTATUS = 128;
    public final static int MODIFIED_TCPSTATUS = 256;
    public final static int MODIFIED_UPDATEN = 512;
    public final static int MODIFIED_ALL = 1023;


    public void removeAdvancedListener(AdvancedListener al) {
        mAdvancedListeners.remove(al);
    }

    public void addAdvancedListener(AdvancedListener al) {
        mAdvancedListeners.add(al);
    }

    protected void postDeviceUpdate(PDeviceHolder devh, DeviceUpdate upd) {
        Map<PDeviceHolder, DeviceUpdate> uM = new HashMap<>();
        uM.putAll(updateMap);
        for (AdvancedListener adv : mAdvancedListeners)
            adv.onDeviceUpdate(devh, upd, uM);
    }

    protected void postDeviceStatus(PDeviceHolder devh, PStatusHolder sta) {
        Map<PDeviceHolder, PStatusHolder> uM = new HashMap<>();
        uM.putAll(statusMap);
        for (AdvancedListener adv : mAdvancedListeners)
            adv.onDeviceStatus(devh, sta, uM);
    }

    public StatusReceiver() {
        privStatus.session = new PSessionHolder();
        PDeviceHolder devh = new PDeviceHolder();
        devh.setAlias("F");
        devh.setId(-1);
        devh.setType(DeviceType.pafers);
        privStatus.session.setDevice(devh);
        fakeStatusMap.put(devh, privStatus);
        fakeUpdateMap.put(devh, new PPafersHolder());
    }


    private void newMapEntry(PDeviceHolder devh) {
        PStatusHolder status = new PStatusHolder();
        statusMap.put(devh, status);
        DeviceUpdate lastUpdate = (DeviceUpdate) DeviceTypeMaps.type2update.get(devh.getType());
        try {
            lastUpdate = lastUpdate.getClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateMap.put(devh, lastUpdate);
        status.copyFrom(privStatus);
        PDeviceHolder dev2 = new PDeviceHolder();
        dev2.copyFrom(devh);
        status.session.setDevice(dev2);
    }

    private void manageFlags(int flag) {
        PStatusHolder status;

        for (Map.Entry<PDeviceHolder, PStatusHolder> entry : statusMap.entrySet()) {
            status = entry.getValue();
            if ((flag & MODIFIED_TCPSTATUS) != 0) {
                //Log.d("TCPStatus",privStatus.tcpStatus+" TCP");
                status.tcpStatus = privStatus.tcpStatus;
                status.tcpAddress = privStatus.tcpAddress;
            }
            if ((flag & MODIFIED_ACTION) != 0) {
                status.lastAction = privStatus.lastAction;
            }
        }

    }

    @Override
    public void onDeviceUpdate(GenericDevice dev, PDeviceHolder devh, DeviceUpdate upd) {
        //Log.d("DevUpdateSTA", "" + upd+" updN "+upd.getUpdateN());
        int cm = 0;
        PStatusHolder status = statusMap.get(devh);
        DeviceUpdate lastUpdate = updateMap.get(devh);
        if (lastUpdate == null || !lastUpdate.equals(upd)) {
            if (lastUpdate == null || lastUpdate.getUpdateN() != upd.getUpdateN())
                cm |= MODIFIED_UPDATEN;
            if (upd != null) {
                if (lastUpdate == null)
                    try {
                        lastUpdate = upd.getClass().newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                lastUpdate.copyFrom(upd);
            }
            status.updateN = upd.getUpdateN();
            cm |= MODIFIED_UPDATE;
        }
        if ((cm & MODIFIED_UPDATE) != 0)
            postDeviceUpdate(devh, lastUpdate);
        if ((cm & MODIFIED_UPDATEN) != 0)
            postDeviceStatus(devh, status);
        //Log.d("DevUpdateSTA", "" + upd+" updN "+upd.getUpdateN()+" ccm = "+cm);
    }

    @Override
    public void onDeviceConnecting(GenericDevice dev,
                                   PDeviceHolder devh) {
        if (!statusMap.containsKey(devh))
            newMapEntry(devh);
        else
            statusMap.get(devh).lastStatus = com.moviz.lib.comunication.DeviceStatus.CONNECTING;
        privStatus.lastAction = Messages.CONNECTING_MESSAGE;
        manageFlags(MODIFIED_ACTION);
        postDeviceUpdate(devh, updateMap.get(devh));
        postDeviceStatus(devh, statusMap.get(devh));
    }


    @Override
    public void onDeviceConnected(GenericDevice dev, PDeviceHolder devh) {
        privStatus.lastAction = Messages.CONNECTED_MESSAGE;
        manageFlags(MODIFIED_ACTION);
        postDeviceStatus(devh, statusMap.get(devh));
    }

    @Override
    public void onDeviceConnectionFailed(GenericDevice dev, PDeviceHolder devh) {
        onDeviceDisconnected(dev, devh, Messages.CONNECTIONERROR_MESSAGE);

    }

    @Override
    public void onDeviceDisconnected(GenericDevice dev, PDeviceHolder devh) {
        onDeviceDisconnected(dev, devh, Messages.DISCONNECTED_MESSAGE);
    }

    @Override
    public void onDeviceError(GenericDevice dev, PDeviceHolder devh, ParcelableMessage e) {
        onDeviceDisconnected(dev, devh, Messages.CONNECTIONERROR_MESSAGE);
    }

    private void onDeviceDisconnected(GenericDevice dev, PDeviceHolder devh, String m) {
        privStatus.lastAction = m;
        updateMap.remove(devh);
        statusMap.remove(devh);
        manageFlags(MODIFIED_ACTION);
    }

    @Override
    public void onUserSet(GenericDevice dev, PDeviceHolder devh, PUserHolder us) {
        PStatusHolder status = statusMap.get(devh);
        if (status.session != null) {
            status.session.setUser(us);
        }
        privStatus.lastAction = Messages.USER_MESSAGE;
        manageFlags(MODIFIED_ACTION);
        postDeviceStatus(devh, status);

    }

    @Override
    public void onDeviceDescription(GenericDevice dev, PDeviceHolder devh,
                                    String desc) {
        PStatusHolder status = statusMap.get(devh);
        String tmp;
        if (status.session != null) {
            PDeviceHolder devh2 = (PDeviceHolder) status.session.getDevice();
            tmp = devh2.getDescription();
            if (!tmp.equals(devh.getDescription())) {
                devh.setDescription(tmp);
            }
        }
        privStatus.lastAction = Messages.DEVICEDESCRIPTIONCHANGED_MESSAGE;
        manageFlags(MODIFIED_ACTION);
        postDeviceStatus(devh, status);
    }

    @Override
    public void onDeviceStatusChange(GenericDevice dev, PDeviceHolder devh,
                                     PHolderSetter hs) {
        PStatusHolder status = statusMap.get(devh);
        //Log.i("StatusReceiver","STAT1 "+msg+" - "+devh+" - "+stat);
        status.newHolder(hs);
        status.lastStatus = com.moviz.lib.comunication.DeviceStatus.valueOf(hs.get(0).getString());
        privStatus.lastAction = Messages.STATUSCHANGE_MESSAGE;
        manageFlags(MODIFIED_ACTION);
        postDeviceStatus(devh, status);
    }

    @Override
    public void onDeviceSession(GenericDevice dev, PDeviceHolder devh,
                                PSessionHolder ses) {
        PStatusHolder status = statusMap.get(devh);
        status.session = ses;
        postDeviceStatus(devh, status);
    }

    @Override
    public void onTcpStatus(com.moviz.lib.comunication.tcp.TCPStatus tcps, String addr) {
        if (privStatus.tcpStatus == null || !tcps.equals(privStatus.tcpStatus)) {
            privStatus.lastAction = Messages.TCPSTATUS_MESSAGE;
            privStatus.tcpStatus = tcps;
            privStatus.tcpAddress = addr;
            manageFlags(MODIFIED_ACTION | MODIFIED_TCPSTATUS);
            for (Map.Entry<PDeviceHolder, PStatusHolder> entry : statusMap.entrySet()) {
                postDeviceStatus(entry.getKey(), entry.getValue());
            }
        }

    }

    public PStatusHolder getPrivStatus() {
        return privStatus;
    }

    public Map<PDeviceHolder, DeviceUpdate> getUpdates() {
        if (updateMap.isEmpty())
            return fakeUpdateMap;
        else
            return updateMap;
    }

    public Map<PDeviceHolder, PStatusHolder> getStatuses() {
        if (statusMap.isEmpty())
            return fakeStatusMap;
        else
            return statusMap;
    }

    public Set<PDeviceHolder> getActiveDevices() {
        if (statusMap.isEmpty())
            return fakeStatusMap.keySet();
        else
            return statusMap.keySet();
    }

    public DeviceUpdate getLastUpdate(PDeviceHolder d) {
        return updateMap.get(d);
    }


    public PStatusHolder getStatus(PDeviceHolder d) {
        return statusMap.get(d);
    }
}
