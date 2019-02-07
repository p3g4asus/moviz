package com.moviz.lib.utils;

import android.bluetooth.le.ScanSettings;
import android.content.res.Resources;
import android.os.Build;

import com.moviz.gui.R;
import com.moviz.gui.fragments.DeviceSubSettings;
import com.moviz.gui.fragments.PafersSubSettings;
import com.moviz.gui.fragments.WahooBlueSCSubSettings;
import com.moviz.lib.comunication.plus.holder.PKeiserM3iHolder;
import com.moviz.lib.hw.BLDeviceSearcher;
import com.moviz.lib.hw.BLEBinder;
import com.moviz.lib.hw.BLEDeviceSearcher;
import com.moviz.lib.hw.BluetoothChatBinder;
import com.moviz.lib.hw.DeviceBinder;
import com.moviz.lib.hw.DeviceSearcher;
import com.moviz.lib.hw.KeiserM3iDevice;
import com.moviz.lib.hw.NonConnectableBinder;
import com.moviz.lib.hw.PafersBinder;
import com.moviz.lib.hw.WahooBlueSCBinder;
import com.moviz.lib.hw.WahooDeviceSearcher;
import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.message.UpdateCommandMessage;
import com.moviz.lib.comunication.message.WahooBlueSCWorkoutMessage;
import com.moviz.lib.comunication.message.ZephyrHxMWorkoutMessage;
import com.moviz.lib.comunication.plus.holder.PHRDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PPafersHolder;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;
import com.moviz.lib.comunication.plus.holder.PWahooBlueSCHolder;
import com.moviz.lib.comunication.plus.holder.PZephyrHxMHolder;
import com.moviz.lib.comunication.plus.holder.UpdateDatabasable;
import com.moviz.lib.hw.GenericDevice;
import com.moviz.lib.hw.HRDevice;
import com.moviz.lib.hw.PafersDevice;
import com.moviz.lib.hw.WahooBlueSCDevice;
import com.moviz.lib.hw.ZephyrHxMDevice;
import com.moviz.lib.plot.HRDevicePlotProcessor;
import com.moviz.lib.plot.PafersPlotProcessor;
import com.moviz.lib.plot.PlotProcessor;
import com.moviz.lib.plot.WahooBlueSCPlotProcessor;
import com.moviz.lib.plot.ZephyrHxMPlotProcessor;
import com.moviz.lib.sessionexport.HRDeviceSessionExporter;
import com.moviz.lib.sessionexport.PafersSessionExporter;
import com.moviz.lib.sessionexport.SessionExporter;
import com.moviz.lib.sessionexport.WahooBlueSCSessionExporter;
import com.moviz.lib.sessionexport.ZephyrHxMSessionExporter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DeviceTypeMaps {
    public static final Map<DeviceType, UpdateDatabasable> type2update;

    static {
        Map<DeviceType, UpdateDatabasable> aMap = new HashMap<DeviceType, UpdateDatabasable>();
        aMap.put(DeviceType.pafers, new PPafersHolder());
        aMap.put(DeviceType.keiserm3i, new PKeiserM3iHolder());
        aMap.put(DeviceType.zephyrhxm, new PZephyrHxMHolder());
        aMap.put(DeviceType.hrdevice, new PHRDeviceHolder());
        aMap.put(DeviceType.wahoobluesc, new PWahooBlueSCHolder());
        type2update = Collections.unmodifiableMap(aMap);
    }

    public static final Map<DeviceType, Integer> type2res;

    static {
        Map<DeviceType, Integer> aMap = new HashMap<DeviceType, Integer>();
        aMap.put(DeviceType.pafers, R.string.pref_device_type_pafers);
        aMap.put(DeviceType.keiserm3i, R.string.pref_device_type_keiserm3i);
        aMap.put(DeviceType.zephyrhxm, R.string.pref_device_type_zephyrhxm);
        aMap.put(DeviceType.hrdevice, R.string.pref_device_type_hrdevice);
        aMap.put(DeviceType.wahoobluesc, R.string.pref_device_type_wahoobluesc);
        type2res = Collections.unmodifiableMap(aMap);
    }

    public static final Map<DeviceType, DeviceSearcher> type2search;

    static {
        Map<DeviceType, DeviceSearcher> aMap = new HashMap<>();
        BLDeviceSearcher bds = new BLDeviceSearcher();
        aMap.put(DeviceType.pafers, bds);
        aMap.put(DeviceType.zephyrhxm, bds);
        BLEDeviceSearcher bleds = new BLEDeviceSearcher();
        aMap.put(DeviceType.hrdevice, bleds);
        aMap.put(DeviceType.wahoobluesc, new WahooDeviceSearcher());
        ScanSettings.Builder sst = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0);
        if (Build.VERSION.SDK_INT >= 23) {
            sst.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).setReportDelay(0);
        }
        bleds = new BLEDeviceSearcher(null,10000,sst);
        aMap.put(DeviceType.keiserm3i, bleds);
        type2search = Collections.unmodifiableMap(aMap);
    }

    public static final Map<DeviceType, String[]> type2confsave;

    static {
        Map<DeviceType, String[]> aMap = new HashMap<>();
        aMap.put(null, new String[]{"pref_temp_status", "pref_temp_workout", "pref_user"});
        aMap.put(DeviceType.pafers, new String[]{"pfold", "pfile"});
        aMap.put(DeviceType.zephyrhxm, new String[0]);
        aMap.put(DeviceType.keiserm3i, new String[0]);
        aMap.put(DeviceType.hrdevice, new String[0]);
        aMap.put(DeviceType.wahoobluesc, new String[]{"wheeldiam","gearfactor","currentgear"});
        type2confsave = Collections.unmodifiableMap(aMap);
    }

    public static final Map<DeviceType, SessionExporter> type2sessionexporter;

    static {
        Map<DeviceType, SessionExporter> aMap = new HashMap<DeviceType, SessionExporter>();
        aMap.put(DeviceType.pafers, new PafersSessionExporter());
        aMap.put(DeviceType.keiserm3i, new PafersSessionExporter());
        aMap.put(DeviceType.zephyrhxm, new ZephyrHxMSessionExporter());
        aMap.put(DeviceType.hrdevice, new HRDeviceSessionExporter());
        aMap.put(DeviceType.wahoobluesc, new WahooBlueSCSessionExporter());
        type2sessionexporter = Collections.unmodifiableMap(aMap);
    }

    public static final Map<DeviceType, Class<? extends PlotProcessor>> type2pltprocclass;

    static {
        Map<DeviceType, Class<? extends PlotProcessor>> aMap = new HashMap<DeviceType, Class<? extends PlotProcessor>>();
        aMap.put(DeviceType.pafers, PafersPlotProcessor.class);
        aMap.put(DeviceType.keiserm3i, PafersPlotProcessor.class);
        aMap.put(DeviceType.zephyrhxm, ZephyrHxMPlotProcessor.class);
        aMap.put(DeviceType.hrdevice, HRDevicePlotProcessor.class);
        aMap.put(DeviceType.wahoobluesc, WahooBlueSCPlotProcessor.class);
        type2pltprocclass = Collections.unmodifiableMap(aMap);
    }

    public static final Map<DeviceType, Class<? extends DeviceSubSettings>> type2settingsclass;

    static {
        Map<DeviceType, Class<? extends DeviceSubSettings>> aMap = new HashMap<DeviceType, Class<? extends DeviceSubSettings>>();
        aMap.put(DeviceType.pafers, PafersSubSettings.class);
        aMap.put(DeviceType.wahoobluesc, WahooBlueSCSubSettings.class);
        aMap.put(DeviceType.zephyrhxm, null);
        aMap.put(DeviceType.hrdevice, null);
        aMap.put(DeviceType.keiserm3i, null);
        type2settingsclass = Collections.unmodifiableMap(aMap);
    }

    public static final Map<DeviceType, Class<? extends GenericDevice>> type2deviceclass;

    static {
        Map<DeviceType, Class<? extends GenericDevice>> aMap = new HashMap<DeviceType, Class<? extends GenericDevice>>();
        aMap.put(DeviceType.pafers, PafersDevice.class);
        aMap.put(DeviceType.zephyrhxm, ZephyrHxMDevice.class);
        aMap.put(DeviceType.hrdevice, HRDevice.class);
        aMap.put(DeviceType.wahoobluesc, WahooBlueSCDevice.class);
        aMap.put(DeviceType.keiserm3i, KeiserM3iDevice.class);
        type2deviceclass = Collections.unmodifiableMap(aMap);
    }

    public static final Map<DeviceType, Class<? extends UpdateCommandMessage>> type2updateclass;

    static {
        Map<DeviceType, Class<? extends UpdateCommandMessage>> aMap = new HashMap<DeviceType, Class<? extends UpdateCommandMessage>>();
        aMap.put(DeviceType.pafers, com.moviz.lib.comunication.message.PafersWorkoutMessage.class);
        aMap.put(DeviceType.keiserm3i, com.moviz.lib.comunication.message.PafersWorkoutMessage.class);
        aMap.put(DeviceType.zephyrhxm, ZephyrHxMWorkoutMessage.class);
        aMap.put(DeviceType.hrdevice, com.moviz.lib.comunication.message.HRDeviceWorkoutMessage.class);
        aMap.put(DeviceType.wahoobluesc, WahooBlueSCWorkoutMessage.class);
        type2updateclass = Collections.unmodifiableMap(aMap);
    }

    public static final Map<DeviceType, DeviceBinder> type2binder;

    static {
        Map<DeviceType, DeviceBinder> aMap = new HashMap<DeviceType, DeviceBinder>();
        aMap.put(DeviceType.pafers, new PafersBinder());
        aMap.put(DeviceType.keiserm3i, new NonConnectableBinder());
        aMap.put(DeviceType.zephyrhxm, new BluetoothChatBinder());
        aMap.put(DeviceType.hrdevice, new BLEBinder());
        aMap.put(DeviceType.wahoobluesc, new WahooBlueSCBinder());
        type2binder = Collections.unmodifiableMap(aMap);
    }

    public static final Map<String, Integer> holderId2resource;

    static {
        Map<String, Integer> mp = new HashMap<String, Integer>();
        mp.put("consvar.max.ctimems", R.string.device_length);
        mp.put("consvar.max.cdist", R.string.device_distance);
        mp.put("consvar.max.cstride", R.string.device_strides);
        mp.put("consvar.max.cbeats", R.string.device_beats);
        mp.put("consvar.max.ocal", R.string.device_calorie);
        mp.put("consvar.avg.ospd", R.string.device_mspeed);
        mp.put("consvar.avg.opul", R.string.device_mpulse);
        mp.put("consvar.avg.orpm", R.string.device_mrpm);
        mp.put("consvar.avg.owatt", R.string.device_mwatt);
        mp.put("plot.x.minutes", R.string.device_minutes);
        mp.put("plot.x.crankminutes", R.string.device_minutes);
        mp.put("plot.x.wheelminutes", R.string.device_minutes);
        mp.put("plot.x.seconds", R.string.device_seconds);
        mp.put("plot.y.pulse", R.string.device_pulse);
        mp.put("plot.y.speed", R.string.device_speed);
        mp.put("plot.y.cardio", R.string.device_cardio);
        mp.put("plot.y.rpm", R.string.device_rpm);
        mp.put("plot.y.crankrev", R.string.device_crankrev);
        mp.put("plot.y.watt", R.string.device_watt);
        mp.put("plot.y.incline", R.string.device_incline);
        mp.put("status.program", R.string.device_status_program);
        mp.put("status.battery", R.string.device_status_battery);
        mp.put("status.batterylev", R.string.device_status_batterylev);
        mp.put("status.device", R.string.device_status_device);
        mp.put("upd.pulse", R.string.device_upd_pulse);
        mp.put("upd.distancer", R.string.device_upd_distancer);
        mp.put("upd.speed", R.string.device_upd_speed);
        mp.put("upd.stridesr", R.string.device_upd_stridesr);
        mp.put("upd.nbeatsr", R.string.device_upd_nbeatsr);
        mp.put("upd.pulsemn", R.string.device_upd_pulsemn);
        mp.put("upd.speedmn", R.string.device_upd_speedmn);
        mp.put("upd.timer", R.string.device_upd_timer);
        mp.put("upd.okts", R.string.device_upd_okts);
        mp.put("upd.tsr", R.string.device_upd_tsr);
        mp.put("status.lastaction", R.string.device_status_lastaction);
        mp.put("status.devicestatus", R.string.device_status_devicestatus);
        mp.put("status.laststatus", R.string.device_status_laststatus);
        mp.put("status.tcpstatus", R.string.device_status_tcpstatus);
        mp.put("status.tcpaddress", R.string.device_status_tcpaddress);
        mp.put("status.updaten", R.string.device_status_updaten);
        mp.put("status.starttime", R.string.device_status_starttime);
        mp.put("status.uname", R.string.device_status_uname);
        mp.put("status.uage", R.string.device_status_uage);
        mp.put("status.uismale", R.string.device_status_uismale);
        mp.put("status.uheight", R.string.device_status_uheight);
        mp.put("status.uweight", R.string.device_status_uweight);
        mp.put("upd.incline", R.string.device_upd_incline);
        mp.put("upd.pulse", R.string.device_upd_pulse);
        mp.put("upd.rpm", R.string.device_upd_rpm);
        mp.put("upd.watt", R.string.device_upd_watt);
        mp.put("upd.time", R.string.device_upd_time);
        mp.put("upd.timer", R.string.device_upd_timer);
        mp.put("upd.calorie", R.string.device_upd_calorie);
        mp.put("upd.distance", R.string.device_upd_distance);
        mp.put("upd.distancer", R.string.device_upd_distancer);
        mp.put("upd.speed", R.string.device_upd_speed);
        mp.put("upd.pulsemn", R.string.device_upd_pulsemn);
        mp.put("upd.rpmmn", R.string.device_upd_rpmmn);
        mp.put("upd.wattmn", R.string.device_upd_wattmn);
        mp.put("upd.speedmn", R.string.device_upd_speedmn);
        holderId2resource = Collections.unmodifiableMap(mp);
    }

    public static String setHolderResource(Holder h, Resources res) {
        if (h.getResString().isEmpty()) {
            Integer ires = holderId2resource.get(h.getId());
            if (ires != null)
                h.setResString(res.getString(ires));
        }
        return h.getResString();
    }

    public static PSessionHolder setSessionResources(PSessionHolder s, Resources res) {
        PHolderSetter holders = s.getPHolders();
        for (Holder h : holders) {
            DeviceTypeMaps.setHolderResource(h, res);
        }
        return s;
    }

}
