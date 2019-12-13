package com.moviz.gui.app;

import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.stetho.Stetho;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.log.timber.nRFLoggerTree;
import timber.log.Timber;

public class CA extends MultiDexApplication {
    public static LocalBroadcastManager lbm = null;
    public static String PACKAGE_NAME = "";
    public static LogSession mLogSession;
    public static Thread.UncaughtExceptionHandler mDefaultEA;

    public CA() {
        // this method fires only once per application start. 
        // getApplicationContext returns null here
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup handler for uncaught exceptions.
        mDefaultEA = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });

        // this method fires once as well as constructor 
        // but also application has context here
        PACKAGE_NAME = getApplicationContext().getPackageName();
        Stetho.initializeWithDefaults(this);
        mLogSession = Logger.newSession(getApplicationContext(), new SimpleDateFormat("dd_MM_yy_HH_mm_ss").format(new Date()), "Moviz");
        Timber.plant(new nRFLoggerTree(mLogSession));
        Timber.plant(new Timber.DebugTree());
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        //e.printStackTrace(); // not all Android versions will print the stack trace automatically
        logException(e);
        if (mDefaultEA==null)
            System.exit(1);
        else
            mDefaultEA.uncaughtException(thread, e);
    }

    public static void logException(Throwable e) {
        e.printStackTrace();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        Logger.e(mLogSession,sw.toString());
    }
}
