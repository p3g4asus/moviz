package com.moviz.gui.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.moviz.gui.R;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.message.DeviceChangedMessage;
import com.moviz.lib.comunication.plus.message.ProcessedOKMessage;
import com.moviz.lib.utils.CommandProcessor;
import com.moviz.lib.velocity.AndroidVelocitySheet;
import com.moviz.lib.velocity.PVelocityContext;
import com.moviz.workers.AdvancedListener;
import com.moviz.workers.DeviceManagerService;
import com.moviz.workers.StatusReceiver;

import java.lang.reflect.Constructor;

public abstract class DeviceFragment extends Fragment implements AdvancedListener,CommandProcessor {
    protected abstract Class<? extends DeviceVelocitySheet> getVelocitySheetClass();
    protected final String TAG = getClass().getSimpleName();

    public static abstract class DeviceVelocitySheet extends AndroidVelocitySheet {
        protected DeviceFragment mFragment;
        public DeviceVelocitySheet(DeviceFragment frag, Resources r, SharedPreferences shp, WebView wv) {
            super(r, shp, wv);
            mFragment = frag;
        }
        @Override
        protected void addToContext(PVelocityContext context) {
            super.addToContext(context);
            context.put("bgColor", "#ffffff");
        }

        @Override
        protected void putStringToSheet(final String val) {
            Activity a;
            if (mFragment.isVisible()) {
                a = mFragment.getActivity();
                if (a != null) {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DeviceVelocitySheet.super.putStringToSheet(val);
                        }
                    });
                }
            }
        }
    }


    private WebView webview = null;
    private Resources res;

    protected DeviceVelocitySheet velSheet;

    protected SharedPreferences sharedPref;

    protected StatusReceiver statusRec;
    protected UpdateCacher updateCacher;
    protected DeviceManagerService.DeviceManagerBinder mBinder;
    protected VelocityServiceConnection mServiceConnection = null;
    protected Context ctx;

    public class VelocityServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (DeviceManagerService.DeviceManagerBinder) service;
            statusRec = mBinder.getStatusReceiver();
            statusRec.addAdvancedListener(DeviceFragment.this);
            mBinder.addCommandProcessor(DeviceFragment.this,DeviceChangedMessage.class);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

    }

    @Override
    public BaseMessage processCommand(BaseMessage hs2) {
        if (hs2 instanceof DeviceChangedMessage) {
            String key = ((DeviceChangedMessage) hs2).getKey();
            if (key==null || key.equals("pref_updatefreq")) {
                String v = ((DeviceChangedMessage) hs2).getValue();
                long updatePer = 2000;
                try {
                    v = v==null?sharedPref.getString("pref_updatefreq","2000"):v;
                    updatePer = Long.parseLong(v);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateCacher = UpdateCacher.newInstance(updatePer);
            }
        }
        return new ProcessedOKMessage();
    }





	/*@Override
	public void onViewStateRestored (Bundle s) {
		super.onViewStateRestored(s);
		connectionUpdates.fromBundle(s);
	}
	
	@Override
	public void onSaveInstanceState(Bundle s) {
        super.onSaveInstanceState(s);
        connectionUpdates.toBundle(s);
    }*/


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getResources();
        ctx = getActivity().getApplicationContext();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        //processCommand(new DeviceChangedMessage(DeviceChangedMessage.Reason.BECAUSE_DEVICE_CHANGED,null,null,null));
        //header = loadHF("header_"+getTemplateName());
        //footer = loadHF("footer_"+getTemplateName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.webview, container, false);
        return view;
    }
	
	/*@Override
	public void onDetach() {
		super.onDetach();
		CA.lbm = null;
	}*/

    public abstract void updateVelocity();


    @Override
    public void onResume() {
        super.onResume();
        processCommand(new DeviceChangedMessage(DeviceChangedMessage.Reason.BECAUSE_DEVICE_CHANGED, null, null, null));
        updateCacher.registerSource(this);
        updateCacher.notifyNeedsUpdate(this);
        if (mServiceConnection == null) {
            mServiceConnection = new VelocityServiceConnection();
            ctx.bindService(new Intent(ctx, DeviceManagerService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        updateCacher.unregisterSource(this);
        if (mServiceConnection != null) {
            if (statusRec != null) {
                statusRec.removeAdvancedListener(this);
                statusRec = null;
            }
            if (mBinder!=null) {
                mBinder.removeCommandProcessor(DeviceFragment.this,DeviceChangedMessage.class);
                mBinder = null;
            }

            try {
                ctx.unbindService(mServiceConnection);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mServiceConnection = null;
        }
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // get the url to open
        try {
            View v = getView();
            webview = (WebView) v.findViewById(R.id.princWV);
            Class<? extends DeviceVelocitySheet> cla = getVelocitySheetClass();
            Constructor<? extends DeviceVelocitySheet> constr = cla.getConstructor(DeviceFragment.class,Resources.class,SharedPreferences.class,WebView.class);
            DeviceVelocitySheet vels = constr.newInstance(DeviceFragment.this,res,sharedPref,webview);
            processCommand(new DeviceChangedMessage(DeviceChangedMessage.Reason.BECAUSE_DEVICE_CHANGED, null, null, null));
            vels.init();
            velSheet = vels;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
