package com.moviz.gui.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.moviz.gui.R;
import com.moviz.lib.comunication.message.ConnectMessage;
import com.moviz.lib.comunication.message.DisconnectMessage;
import com.moviz.lib.comunication.message.ExitMessage;
import com.moviz.lib.comunication.message.PauseMessage;
import com.moviz.lib.comunication.message.StartMessage;
import com.moviz.lib.comunication.message.UpDownMessage;
import com.moviz.workers.DeviceManagerService;
import com.moviz.workers.DeviceManagerService.DeviceManagerBinder;

public class ButtonFragment extends Fragment {
    //private TextView actionTV = null;
    //private TextView statusTV = null;
    private Button upbutton;
    private Button downbutton;
    private Button playbutton;
    private Button stopbutton;
    private Button pausebutton;
    private Button exitbutton;
    private Button connectbutton;
    protected DeviceManagerBinder mBinder;
    protected ButtonServiceConnection mServiceConnection = null;
    protected Context ctx;

    private class ButtonServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (DeviceManagerBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub

        }

    }

    @Override
    public void onPause() {
        if (mServiceConnection != null) {
            try {
                ctx.unbindService(mServiceConnection);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mServiceConnection = null;
            mBinder = null;
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mServiceConnection == null) {
            mServiceConnection = new ButtonServiceConnection();
            ctx.bindService(new Intent(ctx, DeviceManagerService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

	/*protected StatusReceiver connectionUpdates = new StatusReceiver() {

		@Override
		protected void processMessage(String msg, int cm) {
			if ((cm&MODIFIED_STATUS)!=0)
				asynchUpdateStatus();
			else if ((cm&MODIFIED_ACTION)!=0)
				asynchUpdateAction();
		}
	};*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.button, container, false);
        return view;
    }
	/*protected void asynchUpdateAction() {
		if (isVisible())
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					updateAction(connectionUpdates.getLastAction());
				}
			});
	}
	
	protected void asynchUpdateStatus() {
		if (isVisible())
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					updateStatus(connectionUpdates.getLastStatus());
				}
			});
	}
	
	private void updateAction(String act) {
		if (isVisible())
			actionTV.setText(act==null?"none":act);
	}
	
	private void updateStatus(DeviceStatus lastStatus2) {
		if (isVisible())
			statusTV.setText(lastStatus2==null?"none":lastStatus2.toString());
	}*/
	
	/*private void openConfirm(final Intent b) {
		Activity a = getActivity();
		if (a != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(a);
			// Get the layout inflater
			LayoutInflater inflater = a.getLayoutInflater();
			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog
			// layout
			final View layout = inflater.inflate(R.layout.switchalert, null);
			builder.setView(layout);
			final Switch confirmSWC = (Switch) layout
					.findViewById(R.id.confirmSWC);
			final AlertDialog ad = builder.create();
			confirmSWC.setChecked(false);
			confirmSWC.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						CA.lbm.sendBroadcast(b);
						ad.dismiss();
					}
				}
			});
			ad.setTitle(getString(R.string.wv_ca_title));
			ad.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.wv_ca_close),// sett
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							ad.dismiss();
						}
					});
			ad.setOnShowListener(new OnShowListener() {
				
				@Override
				public void onShow(DialogInterface dialog) {
					// TODO Auto-generated method stub
					confirmSWC.setSwitchMinWidth((int) (layout.getWidth()));
				}
			});
			ad.show();
			
		}
	}
	
	private CharSequence getString(String string) {
		// TODO Auto-generated method stub
		return null;
	}*/

    private void processUpDown(int val) {
        if (mBinder != null)
            mBinder.postMessage(new UpDownMessage(val),null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // get the url to open
        View v = getView();
        //statusTV = (TextView) v.findViewById(R.id.statusTV);
        //actionTV = (TextView) v.findViewById(R.id.actionTV);
        upbutton = (Button) v.findViewById(R.id.upBTN);
        downbutton = (Button) v.findViewById(R.id.downBTN);
        playbutton = (Button) v.findViewById(R.id.playBTN);
        stopbutton = (Button) v.findViewById(R.id.stopBTN);
        connectbutton = (Button) v.findViewById(R.id.connectBTN);
        pausebutton = (Button) v.findViewById(R.id.pauseBTN);
        exitbutton = (Button) v.findViewById(R.id.exitBTN);
        upbutton.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                processUpDown(3);
                return true;
            }
        });
        downbutton.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                processUpDown(-3);
                return true;
            }
        });
        upbutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                processUpDown(1);
            }
        });
        downbutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                processUpDown(-1);
            }
        });
        playbutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mBinder != null)
                    mBinder.postMessage(new StartMessage(),null);
            }
        });
        pausebutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mBinder != null)
                    mBinder.postMessage(new PauseMessage(),null);
            }
        });
        connectbutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mBinder != null)
                    mBinder.postMessage(new ConnectMessage(),null);
            }
        });
        stopbutton.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (mBinder != null)
                    mBinder.postMessage(new DisconnectMessage(),null);
                return true;
            }
        });
        exitbutton.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (mBinder != null)
                    mBinder.postMessage(new ExitMessage(),null);
                return true;
            }
        });

        //updateAction(connectionUpdates.getLastAction());
        //updateStatus(connectionUpdates.getLastStatus());
    }
}
