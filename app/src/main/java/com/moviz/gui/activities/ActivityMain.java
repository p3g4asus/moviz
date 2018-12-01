package com.moviz.gui.activities;


import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.moviz.gui.R;
import com.moviz.gui.app.CA;
import com.moviz.gui.fragments.DrawerFragment;
import com.moviz.gui.fragments.PlotFragment;
import com.moviz.gui.fragments.SettingsFragment;
import com.moviz.gui.fragments.StatusFragment;
import com.moviz.gui.fragments.WorkoutFragment;
import com.moviz.gui.util.Messages;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.message.ConnectMessage;
import com.moviz.lib.comunication.message.DisconnectMessage;
import com.moviz.lib.comunication.message.ExitMessage;
import com.moviz.lib.comunication.message.PauseMessage;
import com.moviz.lib.comunication.message.StartMessage;
import com.moviz.lib.comunication.message.UpDownMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.message.ConfChangeMessage;
import com.moviz.lib.comunication.plus.message.DeviceChangedMessage;
import com.moviz.lib.comunication.plus.message.ProcessedOKMessage;
import com.moviz.lib.comunication.plus.message.TerminateMessage;
import com.moviz.lib.utils.CommandProcessor;
import com.moviz.lib.utils.ParcelableMessage;
import com.moviz.workers.DeviceManagerService;
import com.moviz.workers.DeviceManagerService.DeviceManagerBinder;
import com.moviz.workers.GoogleFitService;

import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;
import no.nordicsemi.android.log.Logger;


public class ActivityMain extends ActionBarActivity implements MaterialTabListener, View.OnClickListener, View.OnLongClickListener, CommandProcessor {

    public final static int TAB_WORKOUT_FRAGMENT = 0;
    public final static int TAB_STATUS_FRAGMENT = 1;
    public static final int TAB_SETTINGS_FRAGMENT = 3;
    public static final int TAB_PLOT_FRAGMENT = 2;

    private Resources res = null;
    private boolean mAuthInProgress = false;
    private long lastIbernationTime = System.currentTimeMillis();
    private DeviceManagerBinder mBinder;

    private ProgressDialog progress;
    private DeviceManagerConnection mServiceConnection = null;

    private static final String AUTH_PENDING = "MainActivity.AUTH_PENDING";
    //int corresponding to the number of tabs in our Activity
    public static final int TAB_COUNT = 4;
    private Toolbar mToolbar;
    //a layout grouping the toolbar and the tabs together
    private ViewGroup mContainerToolbar;
    private MaterialTabHost mTabHost;
    private ViewPager mPager;
    private ViewPagerAdapter mAdapter;
    //private FloatingActionButton mFAB;
    //private FloatingActionMenu mFABMenu;
    private DrawerFragment mDrawerFragment;

    private static String TAG_CONNECT = "tag_connect";
    private static String TAG_PLAY = "tag_play";
    private static String TAG_DISCONNECT = "tag_disconnect";
    private static String TAG_PAUSE = "tag_pause";
    private static String TAG_EXIT = "tag_exit";
    private static String TAG_PLUS = "tag_plus";
    private static String TAG_MINUS = "tag_minus";
    private FloatingActionMenu mFABMenu;
    private String startConf = null;

    public static final String TAG = "ActivityMain";


    @Override
    public BaseMessage processCommand(BaseMessage hs2) {
        if ((hs2 instanceof ExitMessage) || (hs2 instanceof DisconnectMessage)) {
            showProgressDialog(false, "");
        } else if (hs2 instanceof TerminateMessage) {
            finish();
            System.exit(0);
        } else if (hs2 instanceof DeviceChangedMessage) {
            if (((DeviceChangedMessage) hs2).getDev() == null)
                adjustScreenOn();
        } else
            return null;
        return new ProcessedOKMessage();
    }

    private void startGoogleFitService() {
        if (GoogleFitService.getStatus() == GoogleFitService.MyStatus.IDLE && !mAuthInProgress) {
            Intent gattServiceIntent = new Intent(this, GoogleFitService.class);
            gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_DB_PATH, sharedPref.getString("pref_dbfold", res == null ? "" : SettingsFragment.getDefaultDbFolder(res)) + "/old/00_pafersmain.db");
            gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_SESSION_NUMBER, 0);
            gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_SESSION_OFFSET, 0);
            gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_REPEAT_AFTER_SYNCH, -1);
            gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_SESSION_KEY, (long)101);
            //gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_SESSION_KEYS, new long[] {27,120,152,248,266,275,299,396,426,434,440,455,459,483,513,515,594,601,608,613,615});
            //gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_SESSION_KEYS, new long[] {6,8,13,19,29,33,56,72,80,115,127,133,141,153,203,233});
            //gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_SESSION_DATESTA, (new GregorianCalendar(2015, 4, 26)).getTimeInMillis());
            //gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_SESSION_DATESTO, (new GregorianCalendar(2015, 4, 27)).getTimeInMillis());
            gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_SESSION_DATESTA, (new GregorianCalendar(2014, 10, 10)).getTimeInMillis());
            gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_SESSION_DATESTO, (new GregorianCalendar(2016, 10, 5,23,0,0)).getTimeInMillis());
            gattServiceIntent.putExtra(GoogleFitService.FIT_EXTRA_SESSION_OPERATION, GoogleFitService.FIT_EXTRA_SESSION_OPERATION_READ);
            startService(gattServiceIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            mAuthInProgress = false;
            if (resultCode == RESULT_OK) {
                startGoogleFitService();
            }
        }
    }

    private void intStartService() {
        Intent gattServiceIntent = new Intent(this, DeviceManagerService.class);
        gattServiceIntent.setAction(DeviceManagerService.ACTION_LOAD_CONFIGURATION);
        gattServiceIntent.putExtra(DeviceManagerService.EXTRA_CONFIGURATION_NAME, startConf);
        startService(gattServiceIntent);
    }

    private void adjustScreenOn() {
        if (sharedPref.getBoolean("pref_screenon", true))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private class DeviceManagerConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBinder = (DeviceManagerBinder) service;
            mBinder.addCommandProcessor(ActivityMain.this, ExitMessage.class, DisconnectMessage.class, TerminateMessage.class, DeviceChangedMessage.class);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            /*if (mChars!=null) {
				for (BluetoothGattCharacteristic gattCharacteristic : mChars)
					mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, false);
			}*/
            //intDisconnect();
        }

    }

    @Override
    protected void onNewIntent(Intent startingIntent) {
        if (startingIntent != null && startingIntent.getAction().equals(DeviceManagerService.ACTION_LOAD_CONFIGURATION)) {
            startConf = startingIntent.getStringExtra(DeviceManagerService.EXTRA_CONFIGURATION_NAME);
            if (mBinder!=null) {
                mBinder.postMessage(new ConfChangeMessage(startConf),this);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeviceManagerService.DeadState deadState = DeviceManagerService.dead();
        if (deadState == DeviceManagerService.DeadState.DEAD) {
            finish();
            System.exit(0);
        }
        setContentView(R.layout.activity_main);
        Intent startingIntent = getIntent();
        if (startingIntent != null && startingIntent.getAction().equals(DeviceManagerService.ACTION_LOAD_CONFIGURATION)) {
            startConf = startingIntent.getStringExtra(DeviceManagerService.EXTRA_CONFIGURATION_NAME);
        }
        Logger.d(CA.mLogSession,"Starting activity "+startConf);
        setupFAB2();
        setupTabs();
        setupDrawer();
        //animate the Toolbar when it comes into the picture
        //AnimationUtils.animateToolbarDroppingDown(mContainerToolbar);

        if (savedInstanceState != null) {
            mAuthInProgress = savedInstanceState.getBoolean(AUTH_PENDING, false);
        }
        res = getResources();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        intStartService();
        setupProgressDialog();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // updateTimerInit();
        if (deadState== DeviceManagerService.DeadState.NOTSTARTED)
            startGoogleFitService();
        adjustScreenOn();

    }

    private void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setNeutralButton(res.getString(R.string.change_settings), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        //startActivity(intent);
                        mPager.setCurrentItem(mAdapter.getCount() - 1);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {

        protected String getExceptionMessage(Intent m) {
            String rv = "";
            int i = 0;
            while (true) {
                if (m.hasExtra("except" + i)) {
                    if (!rv.isEmpty())
                        rv += "\n";

                    rv += ((ParcelableMessage) m.getParcelableExtra("except" + i)).getMessage(null, res);
                    i++;
                } else
                    break;
            }
            return rv;
        }

        protected String getDeviceMessage(Intent m) {
            String rv = "";
            int i = 0;
            PDeviceHolder devh;
            while (true) {
                if (m.hasExtra("dev" + i)) {
                    devh = m.getParcelableExtra("dev" + i);
                    rv += "\n" + (i + 1) + ") " + devh.getAlias() + " (" + devh.getName() + " [" + devh.getAddress() + "])";
                    i++;
                } else
                    break;
            }
            return rv;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getAction();
            if (msg.equals(GoogleFitService.FIT_NOTIFY_INTENT)) {
                if (intent.hasExtra(GoogleFitService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE) &&
                        intent.hasExtra(GoogleFitService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE)) {
                    //Recreate the connection result
                    int statusCode = intent.getIntExtra(GoogleFitService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE, 0);
                    PendingIntent pendingIntent = intent.getParcelableExtra(GoogleFitService.FIT_EXTRA_NOTIFY_FAILED_INTENT);
                    ConnectionResult result = new ConnectionResult(statusCode, pendingIntent);
                    Log.i("MainActivity", "Connection failed. Cause: " + result.toString());
                    if (!result.hasResolution()) {
                        // Show the localized error dialog
                        GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                ActivityMain.this, 0).show();
                        return;
                    }
                    try {
                        Log.i("MainActivity", "Attempting to resolve failed connection");
                        if (!mAuthInProgress) {
                            mAuthInProgress = true;
                            result.startResolutionForResult(ActivityMain.this,
                                    1);
                        }
                    } catch (IntentSender.SendIntentException e) {
                        Log.e("MainActivity",
                                "Exception while starting resolution activity", e);
                    }
                } else if (intent.hasExtra(GoogleFitService.FIT_EXTRA_NOTIFY_SUSPENDED_REASON)) {
                    msg = "Suspended fit R=" + intent.getIntExtra(GoogleFitService.FIT_EXTRA_NOTIFY_SUSPENDED_REASON, 0)
                            + " K=" + intent.getLongExtra(GoogleFitService.FIT_EXTRA_NOTIFY_SUSPENDED_SESSION, -1);
                    Log.e("MainActivity", msg);
                    Toast.makeText(ActivityMain.this, msg, Toast.LENGTH_LONG).show();
                }
            } else if (msg.equals(Messages.EXCEPTION_MESSAGE)) {
                ParcelableMessage exc = (ParcelableMessage) intent.getParcelableExtra("except0");
                if (exc == null)
                    showProgressDialog(false, "");
                else {
                    String idexc = exc.getId();
                    //int headerid = intent.getIntExtra("EXCEPTION_MESSAGE_HEADER", -1);
                    //String header = headerid==-1?"":res.getString(headerid)+"\n";
                    String msgexc = getExceptionMessage(intent);
                    if (idexc.indexOf("_errp_") >= 0) {
                        showProgressDialog(true, msgexc);
                    } else if (idexc.indexOf("_errr_") >= 0) {
                        showProgressDialog(false, "");
                        makeToast(msgexc, exc.getMsgType());
                        //Toast.makeText(ActivityMain.this, msgexc, Toast.LENGTH_LONG).show();
                    } else if (idexc.indexOf("_errs_") >= 0) {
                        showProgressDialog(false, "");
                        Log.v(TAG,"Ril Error "+idexc);
                        showError(res.getString(R.string.exm_errs_exception) + "\n" + msgexc);
                    }
                }
            }
        }

        private void makeToast(String msgexc, ParcelableMessage.Type msgType) {
            LayoutInflater inflater = LayoutInflater.from(ActivityMain.this);
            View layout = inflater.inflate(R.layout.toast_layout, null);

            ImageView image = (ImageView) layout.findViewById(R.id.toast_image);
            if (msgType == ParcelableMessage.Type.OK)
                image.setImageResource(R.drawable.ic_action_shield_ok);
            else if (msgType == ParcelableMessage.Type.ERROR)
                image.setImageResource(R.drawable.ic_action_shield_error);
            else
                image.setImageResource(R.drawable.ic_action_shield_warning);

            TextView textV = (TextView) layout.findViewById(R.id.toast_text);
            textV.setText(msgexc);

            Toast toast = new Toast(ActivityMain.this);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
    };
    private SharedPreferences sharedPref;

    private void setupProgressDialog() {
        progress = new ProgressDialog(this);
        progress.setMessage(res.getString(R.string.exm_errp_connecting));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
    }

    private void showProgressDialog(final boolean show, final String string) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (show) {
                    progress.cancel();
                    progress.setMessage(string);
                    progress.show();
                } else
                    progress.cancel();
            }
        });
    }

    @Override
    public void onBackPressed() {
        int ci = mPager.getCurrentItem();
        mPager.setCurrentItem(ci > 0 ? ci - 1 : mAdapter.getCount() - 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DeviceManagerService.dead()== DeviceManagerService.DeadState.DEAD) {
            finish();
            System.exit(0);
            return;
        } else if (mServiceConnection == null) {
            mServiceConnection = new DeviceManagerConnection();
            bindService(new Intent(this, DeviceManagerService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
        adjustScreenOn();
        setupIO(true);
    }

    @Override
    protected void onPause() {
        setupIO(false);
        lastIbernationTime = System.currentTimeMillis();
        if (mServiceConnection != null) {
            if (mBinder != null) {
                mBinder.removeCommandProcessor(this, ExitMessage.class, DisconnectMessage.class, TerminateMessage.class, DeviceChangedMessage.class);
                mBinder = null;
            }
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mServiceConnection != null) {
            if (mBinder != null) {
                mBinder.removeCommandProcessor(this, ExitMessage.class, DisconnectMessage.class, TerminateMessage.class, DeviceChangedMessage.class);
                mBinder = null;
            }
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
        outState.putBoolean(AUTH_PENDING, mAuthInProgress);
    }

    private void setupIO(boolean start) {
        if (start) {
            try {
                CA.lbm.unregisterReceiver(messageReceiver);
            } catch (Exception e) {
            }
            IntentFilter intentf = new IntentFilter();
            intentf.addAction(Messages.EXCEPTION_MESSAGE);
            intentf.addAction(GoogleFitService.FIT_NOTIFY_INTENT);
            CA.lbm.registerReceiver(messageReceiver, intentf);
            CA.lbm.sendBroadcast(new Intent(Messages.CMDGETCONSTATUS_MESSAGE).putExtra("t", lastIbernationTime));
        } else {
            try {
                CA.lbm.unregisterReceiver(messageReceiver);
            } catch (Exception e) {
            }
            CA.lbm.registerReceiver(messageReceiver, new IntentFilter(GoogleFitService.FIT_NOTIFY_INTENT));
        }
    }

    private void setupDrawer() {
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        mContainerToolbar = (ViewGroup) findViewById(R.id.container_app_bar);
        //set the Toolbar as ActionBar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //setup the Na
        // vigationDrawer
        mDrawerFragment = (DrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        mDrawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
    }

    @Override
    public void onDestroy() {
        Logger.d(CA.mLogSession,"Activity Destroy");
        super.onDestroy();
    }

    public void onDrawerItemClicked(int index) {
        mPager.setCurrentItem(index);
    }

    public View getContainerToolbar() {
        return mContainerToolbar;
    }

    private void setupTabs() {
        mTabHost = (MaterialTabHost) findViewById(R.id.materialTabHost);
        mPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        //when the page changes in the ViewPager, step the Tabs accordingly
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mTabHost.setSelectedNavigationItem(position);

            }
        });
        //Add all the Tabs to the TabHost
        for (int i = 0; i < mAdapter.getCount(); i++) {
            mTabHost.addTab(
                    mTabHost.newTab()
                            .setIcon(mAdapter.getIcon(i))
                            .setTabListener(this));
        }
    }

    private void setupFAB2() {
        mFABMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.fab_b_close);
        fb.setOnLongClickListener(this);
        fb = (FloatingActionButton) findViewById(R.id.fab_b_connect);
        fb.setOnClickListener(this);
        fb = (FloatingActionButton) findViewById(R.id.fab_b_disconnect);
        fb.setOnLongClickListener(this);
        fb = (FloatingActionButton) findViewById(R.id.fab_b_minus);
        fb.setOnClickListener(this);
        fb.setOnLongClickListener(this);
        fb = (FloatingActionButton) findViewById(R.id.fab_b_pause);
        fb.setOnClickListener(this);
        fb = (FloatingActionButton) findViewById(R.id.fab_b_play);
        fb.setOnClickListener(this);
        fb = (FloatingActionButton) findViewById(R.id.fab_b_plus);
        fb.setOnClickListener(this);
        fb.setOnLongClickListener(this);
    }

    /*private void setupFAB() {
        //define the icon for the main floating action button
        ImageView iconFAB = new ImageView(this);
        iconFAB.setImageResource(R.drawable.ic_action_new);

        //set the appropriate background for the main floating action button along with its icon
        mFAB = new FloatingActionButton.Builder(this)
                .setContentView(iconFAB)
                .setBackgroundDrawable(R.drawable.selector_button_red)
                .build();

        //define the icons for the sub action buttons
        ImageView iconExit = new ImageView(this);
        iconExit.setImageResource(R.drawable.ic_action_close);
        ImageView iconPlay = new ImageView(this);
        iconPlay.setImageResource(R.drawable.ic_action_play);
        ImageView iconDisconnect = new ImageView(this);
        iconDisconnect.setImageResource(R.drawable.ic_action_disconnect);
        ImageView iconPause = new ImageView(this);
        iconPause.setImageResource(R.drawable.ic_action_pause);
        ImageView iconConnect = new ImageView(this);
        iconConnect.setImageResource(R.drawable.ic_action_connect);
        ImageView iconPlus = new ImageView(this);
        iconPlus.setImageResource(R.drawable.ic_action_plus);
        ImageView iconMinus = new ImageView(this);
        iconMinus.setImageResource(R.drawable.ic_action_minus);

        //set the background for all the sub buttons
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        itemBuilder.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_sub_button_gray));


        //build the sub buttons
        SubActionButton buttonExit = itemBuilder.setContentView(iconExit).build();
        SubActionButton buttonPlay = itemBuilder.setContentView(iconPlay).build();
        SubActionButton buttonConnect = itemBuilder.setContentView(iconConnect).build();
        SubActionButton buttonDisconnect = itemBuilder.setContentView(iconDisconnect).build();
        SubActionButton buttonPause = itemBuilder.setContentView(iconPause).build();
        SubActionButton buttonPlus = itemBuilder.setContentView(iconPlus).build();
        SubActionButton buttonMinus = itemBuilder.setContentView(iconMinus).build();

        //to determine which button was clicked, set Tags on each button
        buttonExit.setTag(TAG_EXIT);
        buttonPlay.setTag(TAG_PLAY);
        buttonConnect.setTag(TAG_CONNECT);
        buttonDisconnect.setTag(TAG_DISCONNECT);
        buttonPause.setTag(TAG_PAUSE);
        buttonPlus.setTag(TAG_PLUS);
        buttonMinus.setTag(TAG_MINUS);

        buttonExit.setOnLongClickListener(this);
        buttonPlay.setOnClickListener(this);
        buttonConnect.setOnClickListener(this);
        buttonDisconnect.setOnLongClickListener(this);
        buttonPause.setOnClickListener(this);
        buttonPlus.setOnClickListener(this);
        buttonMinus.setOnClickListener(this);
        buttonPlus.setOnLongClickListener(this);
        buttonMinus.setOnLongClickListener(this);

        //add the sub buttons to the main floating action button
        mFABMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(buttonExit)
                .addSubActionView(buttonDisconnect)
                .addSubActionView(buttonConnect)
                .addSubActionView(buttonPause)
                .addSubActionView(buttonPlay)
                .addSubActionView(buttonMinus)
                .addSubActionView(buttonPlus)
                .attachTo(mFAB)
                .build();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present. 
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_workout:
                mPager.setCurrentItem(TAB_WORKOUT_FRAGMENT);
                return true;
            case R.id.action_status:
                mPager.setCurrentItem(TAB_STATUS_FRAGMENT);
                return true;
            case R.id.action_plot:
                mPager.setCurrentItem(TAB_PLOT_FRAGMENT);
                return true;
            case R.id.action_settings:
                mPager.setCurrentItem(TAB_SETTINGS_FRAGMENT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onTabSelected(MaterialTab materialTab) {
        //when a Tab is selected, step the ViewPager to reflect the changes
        mPager.setCurrentItem(materialTab.getPosition());
    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {
    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {
    }

    private void processUpDown(int val) {
        if (mBinder != null)
            mBinder.postMessage(new UpDownMessage(val), this);
    }

    @Override
    public void onClick(View v) {
        int tg = v.getId();
        if (tg == R.id.fab_b_connect) {
            if (mBinder != null)
                mBinder.postMessage(new ConnectMessage(), this);
        } else if (tg == R.id.fab_b_plus) {
            processUpDown(1);
        } else if (tg == R.id.fab_b_minus) {
            processUpDown(-1);
        } else if (tg == R.id.fab_b_pause) {
            if (mBinder != null)
                mBinder.postMessage(new PauseMessage(), this);
        } else if (tg == R.id.fab_b_play) {
            if (mBinder != null)
                mBinder.postMessage(new StartMessage(), this);
        }
    }

    private void vibrate() {
        TimerTask vibTask = new TimerTask() {

            @Override
            public void run() {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(150);
            }
        };
        Timer vibTimer = new Timer();
        vibTimer.schedule(vibTask, 100);
    }

    @Override
    public boolean onLongClick(View v) {
        int tg = v.getId();

        if (tg == R.id.fab_b_close) {
            vibrate();
            if (mBinder != null)
                mBinder.postMessage(new ExitMessage(), this);
            return true;
        } else if (tg == R.id.fab_b_disconnect) {
            vibrate();
            if (mBinder != null)
                mBinder.postMessage(new DisconnectMessage(), this);
            return true;
        } else if (tg == R.id.fab_b_plus) {
            vibrate();
            processUpDown(+3);
            return true;
        } else if (tg == R.id.fab_b_minus) {
            vibrate();
            processUpDown(-3);
            return true;
        } else
            return false;

    }


    private void toggleTranslateFAB(float slideOffset) {
        if (mFABMenu != null) {
            if (mFABMenu.isOpened()) {
                mFABMenu.close(true);
            }
            mFABMenu.setTranslationX(slideOffset * 200);
        }
    }

    public void onDrawerSlide(float slideOffset) {
        toggleTranslateFAB(slideOffset);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        int icons[] = {R.drawable.ic_action_workout,
                R.drawable.ic_action_status,
                R.drawable.ic_action_plot,
                R.drawable.ic_action_settings};

        FragmentManager fragmentManager;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentManager = fm;
        }

        public Fragment getItem(int num) {
            Fragment fragment = null;
//            L.m("getItem called for " + num);
            switch (num) {
                case TAB_WORKOUT_FRAGMENT:
                    fragment = new WorkoutFragment();
                    break;
                case TAB_STATUS_FRAGMENT:
                    fragment = new StatusFragment();
                    break;
                case TAB_SETTINGS_FRAGMENT:
                    fragment = new SettingsFragment();
                    break;
                case TAB_PLOT_FRAGMENT:
                    fragment = new PlotFragment();
                    break;
            }
            return fragment;

        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.tabs)[position];
        }

        private Drawable getIcon(int position) {
            return getResources().getDrawable(icons[position]);
        }
    }
} 