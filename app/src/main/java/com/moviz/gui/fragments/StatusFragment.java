package com.moviz.gui.fragments;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.webkit.WebView;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PStatusHolder;
import com.moviz.lib.comunication.plus.message.DeviceChangedMessage;
import com.moviz.lib.comunication.plus.message.ProcessedOKMessage;
import com.moviz.lib.velocity.PVelocityContext;
import com.moviz.lib.velocity.VelocitySheet;

import java.util.Map;

public class StatusFragment extends DeviceFragment {

    public static class StatusVelocitySheet extends DeviceVelocitySheet {
        private  String dateFormat = "";

        public StatusVelocitySheet(DeviceFragment frag, Resources r, SharedPreferences shp, WebView wv) {
            super(frag, r, shp, wv);
        }


        @Override
        protected String getTemplateName() {
            return "status";
        }

        @Override
        protected void addToContext(PVelocityContext context) {
            super.addToContext(context);
            context.put("Log", Log.class);
            context.put("datef", dateFormat + " HH:mm:ss");
            context.put("TCPStatus", com.moviz.lib.comunication.tcp.TCPStatus.class);
            context.put("DeviceStatus", com.moviz.lib.comunication.DeviceStatus.class);
        }
        public void setDateFormat(String df) {
            dateFormat = df;
        }
    }

    protected Map<PDeviceHolder, PStatusHolder> lastMap = null;

    @Override
    protected Class<? extends DeviceVelocitySheet> getVelocitySheetClass() {
        return StatusVelocitySheet.class;
    }

    @Override
    public BaseMessage processCommand(BaseMessage hs2) {
        super.processCommand(hs2);
        if (hs2 instanceof DeviceChangedMessage) {
            String key = ((DeviceChangedMessage) hs2).getKey();
            if ((key==null || key.equals("pref_datef")) && velSheet!=null) {
                String v = ((DeviceChangedMessage) hs2).getValue();
                ((StatusVelocitySheet)velSheet).setDateFormat(v==null?sharedPref.getString("pref_datef", "dd/MM/yy"):v);
            }
        }
        return new ProcessedOKMessage();
    }

    @Override
    public void updateVelocity() {
        //Log.v(TAG, "Here to update velocity "+velSheet+" "+lastMap);
        if (velSheet!=null && lastMap!=null) {
            velSheet.updateValues(new VelocitySheet.SheetUpdate(lastMap,"status"));
            lastMap = null;
        }
    }

    @Override
    public void onDeviceUpdate(PDeviceHolder devh, DeviceUpdate upd, Map<PDeviceHolder, DeviceUpdate> uM) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeviceStatus(PDeviceHolder devh, PStatusHolder sta, Map<PDeviceHolder, PStatusHolder> uM) {
        //Log.v(TAG, "Request Velocity Update "+uM);
        lastMap = uM;
        updateCacher.notifyNeedsUpdate(this);
    }

}
