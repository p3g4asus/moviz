package com.moviz.gui.fragments;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.webkit.WebView;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.WahooBlueSCHolder;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PStatusHolder;
import com.moviz.lib.velocity.PVelocityContext;
import com.moviz.lib.velocity.VelocitySheet;

import java.util.Map;

public class WorkoutFragment extends DeviceFragment {

    protected Map<PDeviceHolder, DeviceUpdate> lastMap = null;

    public static class WorkoutVelocitySheet extends DeviceVelocitySheet {


        public WorkoutVelocitySheet(DeviceFragment frag, Resources r, SharedPreferences shp, WebView wv) {
            super(frag, r, shp, wv);
        }

        @Override
        protected void addToContext(PVelocityContext context) {
            super.addToContext(context);
            context.put("WSensorType", WahooBlueSCHolder.SensorType.class);
        }

        @Override
        protected String getTemplateName() {
            return "workout";
        }

    }

    @Override
    public void onDeviceUpdate(PDeviceHolder devh, DeviceUpdate upd, Map<PDeviceHolder, DeviceUpdate> uM) {
        //Timber.tag(TAG).v("Request Velocity Update "+uM);
        lastMap = uM;
        updateCacher.notifyNeedsUpdate(this);
    }

    @Override
    public void onDeviceStatus(PDeviceHolder devh, PStatusHolder sta, Map<PDeviceHolder, PStatusHolder> uM) {
        // TODO Auto-generated method stub

    }

    @Override
    protected Class<? extends DeviceVelocitySheet> getVelocitySheetClass() {
        return WorkoutVelocitySheet.class;
    }

    @Override
    public void updateVelocity() {
        //Timber.tag(TAG).v("Here to update velocity "+velSheet+" "+lastMap);
        if (velSheet!=null && lastMap!=null) {
            velSheet.updateValues(new VelocitySheet.SheetUpdate(lastMap,"upd"));
            lastMap = null;
        }
    }
}
