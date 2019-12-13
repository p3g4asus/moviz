package com.moviz.workers;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.LongSparseArray;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.moviz.gui.R;
import com.moviz.gui.app.CA;
import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.holder.DeviceHolder;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;
import com.moviz.lib.comunication.plus.holder.UpdateDatabasable;
import com.moviz.lib.db.MySQLiteHelper;
import com.moviz.lib.googlefit.ActivitySegments;
import com.moviz.lib.googlefit.GoogleFitPoint;
import com.moviz.lib.googlefit.GoogleFitPointTransformer;
import com.moviz.lib.utils.DeviceTypeMaps;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;


public class GoogleFitService extends Service implements MySyncStatusObserver.Callback {


    public static enum MyStatus {
        IDLE,
        STARTING,
        RUNNING,
        STOPPING
    }

    public static final String FIT_EXTRA_SESSION_NUMBER = "GoogleFitService.FIT_EXTRA_SESSION_NUMBER";
    protected static final String TAG = GoogleFitService.class.getSimpleName();
    public static final String FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE = "GoogleFitService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE";
    public static final String FIT_EXTRA_NOTIFY_FAILED_INTENT = "GoogleFitService.FIT_EXTRA_NOTIFY_FAILED_INTENT";
    public static final String FIT_NOTIFY_INTENT = "GoogleFitService.FIT_NOTIFY_INTENT";
    public static final String FIT_EXTRA_NOTIFY_SUSPENDED_REASON = "GoogleFitService.FIT_EXTRA_NOTIFY_SUSPENDED_INTENT";
    public static final String FIT_EXTRA_NOTIFY_SUSPENDED_SESSION = "GoogleFitService.FIT_EXTRA_NOTIFY_SUSPENDED_SESSION";
    public static final String FIT_EXTRA_DB_PATH = "GoogleFitService.FIT_EXTRA_DB_PATH";
    public static final int FIT_EXTRA_SESSION_OPERATION_INSERT = 1;
    public static final int FIT_EXTRA_SESSION_OPERATION_DELETE = 2;
    public static final int FIT_EXTRA_SESSION_OPERATION_READ = 3;
    public static final int FIT_EXTRA_SESSION_OPERATION_FORCEINSERT = 4;
    public static final int FIT_EXTRA_SESSION_OPERATION_DELETEALL = 5;
    public static final int FIT_EXTRA_SESSION_OPERATION_DELETEALL_BY_SESSION = 7;
    public static final int FIT_EXTRA_SESSION_OPERATION_INSCHECK = 6;
    public static final String FIT_EXTRA_SESSION_OPERATION = "GoogleFitService.FIT_EXTRA_SESSION_OPERATION";
    public static final String FIT_EXTRA_SESSION_OFFSET = "GoogleFitService.FIT_EXTRA_SESSION_OFFSET";
    public static final String FIT_EXTRA_NO_UNKNOWN_ACT = "GoogleFitService.FIT_EXTRA_NO_UNKNOWN_ACT";
    public static final String FIT_EXTRA_SESSION_KEY = "GoogleFitService.FIT_EXTRA_SESSION_KEY";
    public static final String FIT_EXTRA_SESSION_KEYS = "GoogleFitService.FIT_EXTRA_SESSION_KEYS";
    public static final String FIT_EXTRA_SESSION_DATESTA = "GoogleFitService.FIT_EXTRA_SESSION_DATESTA";
    public static final String FIT_EXTRA_SESSION_DATESTO = "GoogleFitService.FIT_EXTRA_SESSION_DATESTO";
    public static final String FIT_EXTRA_REPEAT_AFTER_SYNCH = "GoogleFitService.FIT_EXTRA_REPEAT_AFTER_SYNCH";
    public static final String FIT_AUTHORITY = "com.google.android.gms.fitness";
    private final IBinder mBinder = new LocalBinder();
    private int mSessionNumber = -1;
    private int mSessionOffset = 0;
    private GoogleApiClient mClient = null;
    private LongSparseArray<List<PSessionHolder>> mSessions = null;
    private static MyStatus mIsRunning = MyStatus.IDLE;
    private MySQLiteHelper mSQL = null;
    private String mDbPath = ".";
    private int mSessionOperation = FIT_EXTRA_SESSION_OPERATION_READ;
    private boolean mNoUnknown = true;
    private long mSessionKey = -1;
    private long[] mSessionKeys = null;
    private long mSessionDateSta = -1;
    private long mSessionDateSto = -1;
    private int mRepeatAfterSynch = -1;
    private Account[] accountList;

    public class LocalBinder extends Binder {
        public GoogleFitService getService() {
            return GoogleFitService.this;
        }
    }

    public void stop() {
        mIsRunning = MyStatus.IDLE;
        if (mClient != null) {
            mClient.disconnect();
            mClient = null;
        }
        if (mSQL != null) {
            mSQL.closeDB();
            mSQL = null;
        }
        removeNotification();
        Timber.tag(TAG).i("Service stopped");
        stopSelf();
    }

    private void removeNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(R.drawable.ic_stat_googlefit);
    }

    private void dumpAccounts() {
        AccountManager acm
                = AccountManager.get(getApplicationContext());
        Account[] acct = null;

        SyncAdapterType[] types = ContentResolver.getSyncAdapterTypes();
        for (SyncAdapterType type : types) {
            Timber.tag(TAG).d("--------------------");
            Timber.tag(TAG).d(type.authority + "--" + type.accountType);
            acct = acm.getAccountsByType(type.accountType);
            for (int i = 0; i < acct.length; i++) {
                int p = ContentResolver.getIsSyncable(acct[i], type.authority);
                Timber.tag(TAG).i("account name: " + acct[i].name);
                Timber.tag(TAG).i("syncable: " + String.valueOf(p));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIsRunning == MyStatus.RUNNING)
            mIsRunning = MyStatus.STOPPING;
        else {
            stop();
        }
    }

    private void showNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, GoogleFitService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_googlefit).setContentTitle("GoogleFit Move")
                .setContentText("GoogleFit Move")
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis()).build();
        nm.notify(R.drawable.ic_stat_googlefit, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotification();
        mIsRunning = MyStatus.STARTING;
        if (intent != null) {
            if (intent.hasExtra(FIT_EXTRA_SESSION_NUMBER))
                mSessionNumber = intent.getIntExtra(FIT_EXTRA_SESSION_NUMBER, mSessionNumber);
            if (intent.hasExtra(FIT_EXTRA_SESSION_OFFSET))
                mSessionOffset = intent.getIntExtra(FIT_EXTRA_SESSION_OFFSET, mSessionOffset);
            if (intent.hasExtra(FIT_EXTRA_SESSION_KEY))
                mSessionKey = intent.getLongExtra(FIT_EXTRA_SESSION_KEY, mSessionKey);
            if (intent.hasExtra(FIT_EXTRA_SESSION_KEYS))
                mSessionKeys = intent.getLongArrayExtra(FIT_EXTRA_SESSION_KEYS);
            if (intent.hasExtra(FIT_EXTRA_SESSION_DATESTA))
                mSessionDateSta = intent.getLongExtra(FIT_EXTRA_SESSION_DATESTA, mSessionDateSta);
            if (intent.hasExtra(FIT_EXTRA_SESSION_DATESTO))
                mSessionDateSto = intent.getLongExtra(FIT_EXTRA_SESSION_DATESTO, mSessionDateSto);
            if (intent.hasExtra(FIT_EXTRA_SESSION_OPERATION))
                mSessionOperation = intent.getIntExtra(FIT_EXTRA_SESSION_OPERATION, mSessionOperation);
            if (intent.hasExtra(FIT_EXTRA_DB_PATH))
                mDbPath = intent.getStringExtra(FIT_EXTRA_DB_PATH);
            if (intent.hasExtra(FIT_EXTRA_NO_UNKNOWN_ACT))
                mNoUnknown = intent.getBooleanExtra(FIT_EXTRA_NO_UNKNOWN_ACT, mNoUnknown);
            if (intent.hasExtra(FIT_EXTRA_REPEAT_AFTER_SYNCH))
                mRepeatAfterSynch = intent.getIntExtra(FIT_EXTRA_REPEAT_AFTER_SYNCH,-1);
        }
        buildFitnessClient();
        mClient.connect();
        return Service.START_NOT_STICKY;
    }

    private void notifyUiFailedConnection(ConnectionResult result) {
        Intent intent = new Intent(FIT_NOTIFY_INTENT);
        intent.putExtra(FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE, result.getErrorCode());
        intent.putExtra(FIT_EXTRA_NOTIFY_FAILED_INTENT, result.getResolution());
        CA.lbm.sendBroadcast(intent);
        stop();
    }

//MAIN
//    Timber.tag(TAG).i("Connection failed. Cause: " + result.toString());
//    if (!result.hasResolution()) {
//        // Show the localized error dialog
//        GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
//                MainActivity.this, 0).show();
//        return;
//    }
//    // The failure has a resolution. Resolve it.
//    // Called typically when the app is not yet authorized, and an
//    // authorization dialog is displayed to the user.
//    if (!authInProgress) {
//        try {
//            Timber.tag(TAG).i("Attempting to resolve failed connection");
//            authInProgress = true;
//            result.startResolutionForResult(MainActivity.this,
//                    REQUEST_OAUTH);
//        } catch (IntentSender.SendIntentException e) {
//            Timber.tag(TAG).e(//                    "Exception while starting resolution activity", e);
//        }
//    }

//	private BroadcastReceiver mFitStatusReceiver = new BroadcastReceiver() {
//	    @Override
//	    public void onReceive(Context context, Intent intent) {
//	        // Get extra data included in the Intent
//	        if (intent.hasExtra(GoogleFitService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE) &&
//	                intent.hasExtra(GoogleFitService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE)) {
//	            //Recreate the connection result
//	            int statusCode = intent.getIntExtra(GoogleFitService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE, 0);
//	            PendingIntent pendingIntent = intent.getParcelableExtra(GoogleFitService.FIT_EXTRA_NOTIFY_FAILED_INTENT);
//	            ConnectionResult result = new ConnectionResult(statusCode, pendingIntent);
//	            fitHandleFailedConnection(result);
//	        }
//	    }
//	};

    private static class SessionInfo {
        public List<DeviceUpdate> ups;
        public long ts;

        public SessionInfo(long t, List<DeviceUpdate> u) {
            ups = u;
            ts = t;
        }

        public void addAll(List<DeviceUpdate> us, long afterSesStart) {
            for (DeviceUpdate u : us) {
                u.adjustAbsTs(afterSesStart - ts);
            }
            ups.addAll(us);
        }
    }

    private static class PrepareResult {
        public DeviceHolder mainDevice = null;
        public Status status = null;
        public String mainActivity = FitnessActivities.UNKNOWN;
        public TreeMap<DeviceHolder, SessionInfo> updatesMap = null;
        public List<DataSource> dsources = null;
        public long sesStartTime = -1, sesStopTime = -1, mainId = -1;

        public PrepareResult(Status s) {
            status = s;
        }

        public PrepareResult(Status s, DeviceHolder d,
                             String act, long mainI, long sta, long sto,
                             List<DataSource> ds,
                             TreeMap<DeviceHolder, SessionInfo> u) {
            status = s;
            mainDevice = d;
            mainId = mainI;
            updatesMap = u;
            sesStartTime = sta;
            sesStopTime = sto;
            dsources = ds;
            mainActivity = act;
        }
    }

    private static class DeviceComparator implements Comparator<DeviceHolder> {
        private DeviceHolder first = null;

        public void setFirst(DeviceHolder d) {
            first = d;
        }

        @Override
        public int compare(DeviceHolder lhs, DeviceHolder rhs) {
            if (first == null)
                return lhs.compareTo(rhs);
            else if (lhs.equals(first))
                return -1;
            else if (rhs.equals(first))
                return 1;
            else
                return lhs.compareTo(rhs);
        }

    }

    private Status deleteSession(PrepareResult w) {
        /*DataDeleteRequest ddr = new DataDeleteRequest.Builder()
        .deleteAllSessions()
		.setTimeInterval(w.sesStartTime, w.sesStopTime, TimeUnit.MILLISECONDS)
		.build();
		Timber.tag(TAG).i("Deleting session");
		Status dstat = Fitness.HistoryApi.deleteData(mClient,ddr).await(5, TimeUnit.MINUTES);
		if (dstat.isSuccess()) {*/
        Status dstat = new Status(CommonStatusCodes.SUCCESS);
        DataDeleteRequest ddr;
        for (int jj = 0; jj < w.dsources.size(); jj++) {
            DataSource ds = w.dsources.get(jj);
            Timber.tag(TAG).i("Deleting data " + ds.getDataType());
            ddr = new DataDeleteRequest.Builder()
                    .deleteAllSessions()
                    .setTimeInterval(w.sesStartTime, w.sesStopTime, TimeUnit.MILLISECONDS)
                    .addDataType(ds.getDataType())
                    .build();
            dstat = Fitness.HistoryApi.deleteData(mClient, ddr).await(5, TimeUnit.MINUTES);
            if (!dstat.isSuccess())
                break;
        }
        //}
        return dstat;
    }

    private Status deleteSessions(long sta, long sto) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss");
        Timber.tag(TAG).i("Deleting all data from " + sdf.format(sta) + " to " + sdf.format(sto));
        DataDeleteRequest ddr = new DataDeleteRequest.Builder()
                .deleteAllSessions()
                .setTimeInterval(sta, sto, TimeUnit.MILLISECONDS)
                .deleteAllData()
                .build();
        return Fitness.HistoryApi.deleteData(mClient, ddr).await(30, TimeUnit.MINUTES);
    }

    private Status readSessionCheck(PrepareInsertResult pir) {
        PrepareResult w = pir.w;
        Status rv = new Status(CommonStatusCodes.SUCCESS);
        DataPoint fp,lp;
        long stime, etime;//, currstime = 0,curretime = 0;
        List<DataPoint> dpoints;
        List<DataPoint> dtp2;
        List<DataSet> dataSets;
        //SessionReadResult secstat = null;
        for (DataSet d: pir.datasetsToPlace) {
            dpoints = d.getDataPoints();
            if (dpoints.size()>=2) {
                fp = dpoints.get(0);
                lp = dpoints.get(dpoints.size() - 1);
                stime = fp.getStartTime(TimeUnit.MILLISECONDS);
                if (stime==0) {
                    stime = fp.getTimestamp(TimeUnit.MILLISECONDS);
                    etime = lp.getTimestamp(TimeUnit.MILLISECONDS);
                }
                else
                    etime = lp.getEndTime(TimeUnit.MILLISECONDS);
                /*if (secstat==null || stime<currstime || etime<currstime || etime>curretime || stime>curretime) {
                    SessionReadRequest.Builder readRequestB = new SessionReadRequest.Builder()
                            .setTimeInterval(stime, etime, TimeUnit.MILLISECONDS)
                            .setSessionId(getSessionIdentifier(w));
                    readRequestB.enableServerQueries();
                    secstat = Fitness.SessionsApi.readSession(mClient, readRequestB.build())
                            .await(5, TimeUnit.MINUTES);
                    currstime = stime;
                    curretime = etime;
                    rv = secstat.getStatus();
                    if (!rv.isSuccess())
                        return rv;
                }*/
                //Timber.tag(TAG).v(fp.getDataType()+" ST="+stime+",EN="+etime);
                DataReadRequest drr = new DataReadRequest.Builder()
                        .enableServerQueries()
                        .setTimeRange(stime, etime, TimeUnit.MILLISECONDS)
                        .read(d.getDataSource())
                        .build();
                DataReadResult darr = Fitness.HistoryApi.readData(mClient, drr).await(1, TimeUnit.MINUTES);
                if ((rv = darr.getStatus()).isSuccess()) {
                    dataSets = darr.getDataSets();
                    boolean okcheck = false;
                    int diff;
                    for (DataSet dataSet : dataSets) {
                        //dumpDataSetAndPoints(dataSet);
                        dtp2 = dataSet.getDataPoints();

                        if (Math.abs(diff = dtp2.size()-dpoints.size())<5)
                            okcheck = true;
                        else
                            Timber.tag(TAG).e("Diff for "+d.getDataType()+" = "+diff);
                        break;
                    }
                    if (!okcheck)
                        return new Status(CommonStatusCodes.SERVICE_VERSION_UPDATE_REQUIRED);
                }
                else
                    return rv;

            }
        }
        return rv;
    }

    private Status readSession(PrepareResult w) {
        Status rv = new Status(CommonStatusCodes.SUCCESS);
        long tintStart = w.sesStartTime, tintStop;
        ArrayList<ArrayList<DataPoint>> points = new ArrayList<ArrayList<DataPoint>>();
        ArrayList<DataPoint> dtp1;
        DataSource ds;
        List<DataPoint> dtp2;
        List<DataSet> dataSets;
        DataPoint lp, fp;
        for (int jj = 0; jj < w.dsources.size(); jj++) {
            ds = w.dsources.get(jj);
            if (points.size() <= jj)
                points.add(dtp1 = new ArrayList<DataPoint>());
            else
                dtp1 = points.get(jj);
            if (!dtp1.isEmpty())
                lp = dtp1.get(dtp1.size() - 1);
            else
                lp = null;
            if (w.sesStopTime - tintStart > 4800 * 500) {
                tintStop = tintStart + 500 * 4800;
                jj--;
            } else
                tintStop = w.sesStopTime;
            SessionReadRequest.Builder readRequestB = new SessionReadRequest.Builder()
                    .setTimeInterval(tintStart, tintStop, TimeUnit.MILLISECONDS)
                    .setSessionId(getSessionIdentifier(w));
            readRequestB.enableServerQueries();
            SessionReadResult secstat = Fitness.SessionsApi.readSession(mClient, readRequestB.build())
                    .await(5, TimeUnit.MINUTES);
            rv = secstat.getStatus();
            if (rv.isSuccess()) {
                for (Session sest : secstat.getSessions()) {
                    // Process the session
                    dumpSession(sest);

                    // Process the data sets for this session

                }
                DataReadRequest drr = new DataReadRequest.Builder()
                        .enableServerQueries()
                        .setTimeRange(tintStart, tintStop, TimeUnit.MILLISECONDS)
                        .read(ds)
                        .build();
                DataReadResult darr = Fitness.HistoryApi.readData(mClient, drr).await(1, TimeUnit.MINUTES);
                if ((rv = darr.getStatus()).isSuccess()) {
                    dataSets = darr.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        //dumpDataSetAndPoints(dataSet);
                        dumpDataSet(dataSet);
                        dtp2 = dataSet.getDataPoints();
                        if (!dtp2.isEmpty())
                            fp = dtp2.get(0);
                        else
                            fp = null;
                        if (fp != null && lp != null &&
                                fp.getEndTime(TimeUnit.MILLISECONDS) == lp.getEndTime(TimeUnit.MILLISECONDS) &&
                                fp.getStartTime(TimeUnit.MILLISECONDS) == lp.getStartTime(TimeUnit.MILLISECONDS)) {
                            dtp1.remove(dtp1.size() - 1);
                        }
                        dtp1.addAll(dtp2);

                    }
                } else
                    return rv;

            } else
                return rv;
            tintStart = tintStop == w.sesStopTime ? w.sesStartTime : tintStop + 1;
	        /*List<DataType> datas = DataType.getAggregatesForInput(ds.getDataType());
			if (datas==null && !datas.isEmpty()) {
				Timber.tag(TAG).w("Start Buckets");
				DataReadRequest drr = new DataReadRequest.Builder()
						.enableServerQueries()
						.setTimeRange(w.sesStartTime, w.sesStopTime, TimeUnit.MILLISECONDS)
						.aggregate(ds, datas.get(0)).bucketByTime((int)(w.sesStopTime-w.sesStartTime), TimeUnit.MILLISECONDS)
						.build();
				DataReadResult darr = Fitness.HistoryApi.readData(mClient, drr).await(1, TimeUnit.MINUTES);
				if (darr.getStatus().isSuccess()) {
					for (Bucket bb:darr.getBuckets()) {
						for (DataSet dataSet : bb.getDataSets()) {
							dumpDataSetAndPoints(dataSet);
						}
					}
				}
				Timber.tag(TAG).w("End Buckets");
			}*/
        }
        for (ArrayList<DataPoint> dataSet : points) {
            dumpPoints(dataSet);
        }
        return rv;
    }

    private String getSessionIdentifier(PrepareResult w) {
        String mainAlias = w.mainDevice.getAlias();
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss");
        return mainAlias + "_" + sdf.format(w.sesStartTime);
    }

    private static class PrepareInsertResult {
        public String sesDescription, sesName;
        public ArrayList<DataSet> datasetsToPlace;
        public PrepareResult w;
        public long numVals;
        public ActivitySegments actSegments;
    }

    private String getSessionDebugString(PrepareInsertResult pir) {
        PrepareResult w = pir.w;
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss");
        return "K=" + w.mainId + " Id Of Session = " + w.mainDevice.getAlias() + "_"
                + sdf.format(w.sesStartTime) + " Finish = "
                + sdf.format(w.sesStopTime) + " NV = " + pir.numVals;
    }

    private Status insertSession(PrepareInsertResult pir) {
        Status tmpStat;
        PrepareResult w = pir.w;
        if (pir.datasetsToPlace != null) {
            SessionInsertRequest.Builder reqB = new SessionInsertRequest.Builder();

            Timber.tag(TAG).i("INSERTING "+getSessionDebugString(pir));
            Session session = new Session.Builder()
                    .setName(pir.sesName)
                    .setIdentifier(getSessionIdentifier(w))
                    .setDescription(pir.sesDescription)
                    .setStartTime(w.sesStartTime, TimeUnit.MILLISECONDS)
                    .setEndTime(w.sesStopTime, TimeUnit.MILLISECONDS)
                    .setActivity(w.mainActivity).build();
            //tmpStat = deleteSession(w);
            SessionInsertRequest reqIns = reqB.setSession(session).addDataSet(pir.actSegments.createDataset(w.mainActivity)).build();
            tmpStat = Fitness.SessionsApi.insertSession(mClient, reqIns).await(
                    5, TimeUnit.MINUTES);// .await(Math.max(numVals*1000,30000),
            // TimeUnit.MILLISECONDS);
            /*int dbgrm = 0;
			List<DataPoint> dbglst;
			DataPoint dbgf,dbgl;*/
            if (tmpStat.isSuccess()) {
                for (DataSet d : pir.datasetsToPlace) {
					/*dbglst = d.getDataPoints();
					dbgf = dbglst.get(0);
					dbgl = dbglst.get(dbglst.size()-1);
					Timber.tag(TAG).i("Adding DS");
					dumpDataSet(d);
					Timber.tag(TAG).i("DP FIRST");
					dumpDataPoint(dbgf);
					Timber.tag(TAG).i("DP LAST");
					dumpDataPoint(dbgl);*/
                    Timber.tag(TAG).v("PUT " + d.toString());
                    tmpStat = Fitness.HistoryApi.insertData(mClient, d).await(
                            5, TimeUnit.MINUTES);
                    if (!tmpStat.isSuccess()/* || dbgrm++==1*/) {
                        if (tmpStat.getStatusCode()== 5021) {
                            for (DataSet dd : pir.datasetsToPlace) {
                                if (dd.getDataType().equals(d.getDataType())) {
                                    if (dd==d) {
                                        Timber.tag(TAG).e("-ERROR HERE-");
                                    }
                                    dumpDataSetAndPoints(dd);
                                }
                            }

                        }
                        break;
                    }
                }
            }
        } else
            tmpStat = new Status(CommonStatusCodes.SERVICE_MISSING);
        Timber.tag(TAG).log(tmpStat.isSuccess() ? Log.INFO : Log.ERROR,
                "K=" + w.mainId + " ST=" + tmpStat.getStatusCode() + " ["
                        + tmpStat.getStatusMessage() + "]");
        return tmpStat;
    }

    private PrepareInsertResult prepareInsertSession(PrepareResult w) {
        PrepareInsertResult pir = new PrepareInsertResult();
        pir.w = w;
        if (!w.mainActivity.equals(FitnessActivities.UNKNOWN) || !mNoUnknown) {
            DeviceHolder dev;
            List<DeviceUpdate> sesvals;
            long lastTs, newTs;
            String sesDescription = "";
            ActivitySegments actSegments = new ActivitySegments();
            long numVals = 0, baseTs;
            List<DataSet> fullDs;
            GoogleFitPointTransformer gfp;
            ArrayList<DataSet> datasetsToPlace = new ArrayList<DataSet>();
            if (w.mainActivity.equals(FitnessActivities.UNKNOWN))
                actSegments.add(w.sesStartTime, w.sesStopTime);
            for (Entry<DeviceHolder, SessionInfo> entry : w.updatesMap
                    .entrySet()) {
                dev = entry.getKey();
                if (!sesDescription.isEmpty())
                    sesDescription += " - ";
                sesDescription += dev.getAlias();
                sesvals = entry.getValue().ups;
                Timber.tag(TAG).i("Size is " + sesvals.size() + " for device "
                                + dev.getAlias());
                if (!sesvals.isEmpty()) {
                    baseTs = entry.getValue().ts;
                    lastTs = 0;
                    newTs = -1;
                    gfp = ((GoogleFitPoint) sesvals.get(0))
                            .getFitPointTransformer();
                    gfp.setMaxPoints(500);
                    for (DeviceUpdate devUpd : sesvals) {
                        if (gfp.validPoint(devUpd)) {
                            newTs = devUpd.getAbsTs();
                            if (newTs >= lastTs) {
                                if (w.mainDevice.equals(dev)
                                        && !w.mainActivity
                                        .equals(FitnessActivities.UNKNOWN)) {
                                    if (actSegments.isEmpty())
                                        actSegments.add(baseTs + newTs);
                                    else if (lastTs - newTs >= gfp
                                            .fitPauseDetectTh()) {
                                        actSegments.add(lastTs + newTs);
                                        actSegments.add(baseTs + newTs);
                                    }
                                }
                                if ((fullDs = gfp.insertDataPoint(devUpd, baseTs,
                                        4800, actSegments)) != null) {
                                    numVals++;
                                    if (!fullDs.isEmpty())
                                        datasetsToPlace.addAll(fullDs);
                                }

                                lastTs = newTs;
                            }
                        }
                    }
                    if (w.mainDevice.equals(dev)
                            && !w.mainActivity
                            .equals(FitnessActivities.UNKNOWN)) {
                        if (!actSegments.closed()) {
                            actSegments.add(newTs + baseTs);
                        }
                    }
                    if ((fullDs = gfp.insertDataPoint(null, baseTs, 4800,
                            actSegments)) != null) {
                        numVals++;
                        if (!fullDs.isEmpty())
                            datasetsToPlace.addAll(fullDs);
                    }
                    datasetsToPlace.addAll(gfp.pointsToSend());
                }
            }

            String mainAlias = w.mainDevice.getAlias();
            pir.sesDescription = sesDescription;
            pir.sesName = mainAlias + " " + w.mainActivity + " session";
            pir.actSegments = actSegments;
            pir.datasetsToPlace = datasetsToPlace;
            pir.numVals = numVals;
        }

        return pir;
    }

    private PrepareResult prepareSession(long mainId, List<PSessionHolder> ses) {
        DeviceHolder dev;
        PSessionHolder pauseSes = null;
        DeviceComparator dComp = new DeviceComparator();
        TreeMap<DeviceHolder, SessionInfo> updatesMap = new TreeMap<DeviceHolder, SessionInfo>(dComp);
        DeviceHolder mainDevice = null;
        UpdateDatabasable tmpUpd;
        DeviceType devType;
        GoogleFitPointTransformer gfp;
        List<DeviceUpdate> sesvals;
        String mainActivity = FitnessActivities.UNKNOWN;
        Status rv = new Status(CommonStatusCodes.SUCCESS);
        List<DataSource> dsources = new ArrayList<DataSource>();
        dsources.add(ActivitySegments.DATASOURCE);
        long baseTs, sesStartTime = Long.MAX_VALUE, sesStopTime = Long.MIN_VALUE, currentTs;
        for (PSessionHolder sh : ses) {
            mSQL.loadSessionValues(sh, null);
            dev = sh.getDevice();
            sesvals = sh.getValues();
            baseTs = sh.getDateStart();
            if (!sesvals.isEmpty()) {
                devType = dev.getType();
                tmpUpd = DeviceTypeMaps.type2update.get(devType);
                gfp = tmpUpd.getFitPointTransformer();
                if (pauseSes == null && !(mainActivity = gfp.fitActivity()).equals(FitnessActivities.UNKNOWN)) {
                    pauseSes = sh;
                    dComp.setFirst(dev);
                    mainDevice = dev;
                } else if (mainDevice == null) {
                    mainDevice = dev;
                }
                if (updatesMap.containsKey(dev))
                    updatesMap.get(dev).addAll(sesvals, sh.getDateStart());
                else {
                    currentTs = sesvals.get(0).getAbsTs() + baseTs;
                    if (sesStartTime > currentTs)
                        sesStartTime = currentTs;
                    currentTs = sesvals.get(sesvals.size() - 1).getAbsTs() + baseTs;
                    if (sesStopTime < currentTs)
                        sesStopTime = currentTs;
                    updatesMap.put(dev, new SessionInfo(baseTs, sesvals));
                    rv = gfp.createDataSources(mClient, dev);
                    if (!rv.isSuccess())
                        return new PrepareResult(rv);
                    else
                        dsources.addAll(gfp.getDataSources());
                }

            }
        }
        return new PrepareResult(rv, mainDevice, mainActivity, mainId, sesStartTime, sesStopTime, dsources, updatesMap);
    }

    private Status insertSession(long mainId, List<PSessionHolder> ses) {
        PrepareResult pr = prepareSession(mainId, ses);
        if (pr.status.isSuccess())
            return insertSession(prepareInsertSession(pr));
        else
            return pr.status;
    }

    private Status deleteAllBySession(long mainId, List<PSessionHolder> ses) {
        PrepareResult pr = prepareSession(mainId, ses);
        if (pr.status.isSuccess())
            return deleteSessions(pr.sesStartTime-10000,pr.sesStopTime+10000);
        else
            return pr.status;
    }

    private Status deleteSession(long mainId, List<PSessionHolder> ses) {
        PrepareResult pr = prepareSession(mainId, ses);
        if (pr.status.isSuccess())
            return deleteSession(pr);
        else
            return pr.status;
    }

    private Status insertSessionCheck(long mainId, List<PSessionHolder> ses) {
        PrepareResult pr = prepareSession(mainId, ses);
        if (pr.status.isSuccess()) {
            PrepareInsertResult pir = prepareInsertSession(pr);
            if (pir.datasetsToPlace!=null) {
                Status sta = readSessionCheck(pir);
                if (sta.isSuccess()) {
                    Timber.tag(TAG).v("NOTHING " + getSessionDebugString(pir));
                    return sta;
                } else {
                    //Status sta;
                    Timber.tag(TAG).e("Should insert " + getSessionDebugString(pir));
                    return sta;
                    /*if ((sta = insertSession(pir)).isSuccess()) {
                        return new Status(CommonStatusCodes.AUTH_API_INVALID_CREDENTIALS);
                    }
                    else
                        return sta;*/
                }
            }
            else
                return new Status(CommonStatusCodes.SERVICE_DISABLED);
        }
        else
            return pr.status;
    }


    private Status readSession(long mainId, List<PSessionHolder> ses) {
        PrepareResult pr = prepareSession(mainId, ses);
        if (pr.status.isSuccess())
            return readSession(pr);
        else
            return pr.status;
    }

//	private int insertSession(List<PSessionHolder> ses) {
//		DeviceHolder dev;
//		PSessionHolder pauseSes = null;
//		DeviceComparator dComp = new DeviceComparator();
//		TreeMap<DeviceHolder,SessionInfo> updatesMap = new TreeMap<DeviceHolder,SessionInfo>(dComp); 
//		String mainActivity = FitnessActivities.UNKNOWN, mainDevice = null;
//		UpdateDatabasable tmpUpd;
//		DeviceType devType;
//		String aliasV;
//		GoogleFitPointTransformer gfp;
//		List<DeviceUpdate> sesvals;
//		int tmpresult = 0;
//		long baseTs,sesStartTime = Long.MAX_VALUE,sesStopTime = Long.MIN_VALUE,currentTs;
//		for (PSessionHolder sh: ses) {
//			mSQL.loadSessionValues(sh, null);
//			dev = sh.getDevice();
//			aliasV = dev.getAlias();
//			sesvals = sh.getValues();
//			baseTs = sh.getDateStart();
//			if (!sesvals.isEmpty()) {
//				devType = dev.getType();
//				tmpUpd = DeviceTypeMaps.type2update.get(devType);
//				gfp = tmpUpd.getFitPointTransformer();
//				if (pauseSes==null && !(mainActivity = gfp.fitActivity()).equals(FitnessActivities.UNKNOWN)) {
//					pauseSes = sh;
//					dComp.setFirst(dev);
//					mainDevice = aliasV;
//				}
//				else if (mainDevice==null) {					
//					mainDevice = aliasV;
//					dComp.setFirst(dev);
//				}
//				if (updatesMap.containsKey(dev))
//					updatesMap.get(dev).addAll(sesvals,sh.getDateStart());
//				else {
//					currentTs = sesvals.get(0).getAbsTs()+baseTs;
//					if (sesStartTime>currentTs)
//						sesStartTime = currentTs;
//					currentTs = sesvals.get(sesvals.size()-1).getAbsTs()+baseTs;
//					if (sesStopTime<currentTs)
//						sesStopTime = currentTs;
//					updatesMap.put(dev, new SessionInfo(baseTs,sesvals));
//					tmpresult = gfp.createDataSources(mClient, dev);
//					if (tmpresult!=0)
//						return tmpresult;
//				}
//				
//			}
//		}
//		SessionInsertRequest.Builder reqB = new SessionInsertRequest.Builder();
//		long lastTs,newTs;
//		String sesDescription = "";
//		ActivitySegments actSegments = new ActivitySegments();
//	    long numVals = 0;
//	    List<DataSet> fullDs;
//	    ArrayList<DataSet> datasetsToPlace = new ArrayList<DataSet>();
//	    if (pauseSes==null)
//	    	actSegments.add(sesStartTime,sesStopTime);
//	    for (Entry<DeviceHolder, SessionInfo> entry : updatesMap.entrySet()) {
//	    	dev = entry.getKey();
//			if (!sesDescription.isEmpty())
//				sesDescription+=" - ";
//			sesDescription+=dev.getAlias();
//			sesvals = entry.getValue().ups;
//			Timber.tag(TAG).i("Size is "+sesvals.size()+" for device "+dev.getAlias());
//			if (!sesvals.isEmpty()) {
//				baseTs = entry.getValue().ts;
//				lastTs = 0; 
//				newTs = -1;
//				gfp = ((GoogleFitPoint) sesvals.get(0)).getFitPointTransformer();
//				gfp.setMaxPoints(500);
//				for (DeviceUpdate devUpd:sesvals) {					
//					if (gfp.validPoint(devUpd)) {
//						newTs = devUpd.getAbsTs();
//						if (mainDevice.equals(dev.getAlias()) && pauseSes!=null) {
//							if (actSegments.isEmpty())
//								actSegments.add(baseTs+newTs);
//							else if (lastTs-newTs>=gfp.fitPauseDetectTh()) {
//								actSegments.add(lastTs+newTs);
//								actSegments.add(baseTs+newTs);
//							}
//						}
//						if ((fullDs = gfp.insertDataPoint(devUpd,baseTs,4800,actSegments))!=null) {
//							numVals++;
//							if (!fullDs.isEmpty())
//								datasetsToPlace.addAll(fullDs);
//						}
//						
//						
//						lastTs = newTs;
//					}
//				}
//				if (mainDevice.equals(dev.getAlias()) && pauseSes!=null) {
//					if (!actSegments.closed()) {
//						actSegments.add(newTs+baseTs);
//					}
//				}
//				if ((fullDs = gfp.insertDataPoint(null,baseTs,4800,actSegments))!=null) {
//					numVals++;
//					if (!fullDs.isEmpty())
//						datasetsToPlace.addAll(fullDs);
//				}
//				datasetsToPlace.addAll(gfp.pointsToSend());
//				gfp.reset();
//			}
//		}
//		reqB.addDataSet(actSegments.createDataset());
//		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss");
//		Timber.tag(TAG).i("Id Of Session = "+mainDevice+"_"+sdf.format(sesStartTime)+" Finish = "+sdf.format(sesStopTime));
//		Session session = new Session.Builder()
//	    .setName(mainDevice+" "+mainActivity+" session")
//         .setIdentifier(mainDevice+"_"+sdf.format(sesStartTime))
//         .setDescription(sesDescription)
//         .setStartTime(sesStartTime, TimeUnit.MILLISECONDS)
//         .setEndTime(sesStopTime, TimeUnit.MILLISECONDS)
//         .setActivity(mainActivity)
//         .build();
//		DataDeleteRequest ddr = new DataDeleteRequest.Builder()
//			.deleteAllSessions()
//			.setTimeInterval(sesStartTime, sesStopTime, TimeUnit.MILLISECONDS)
//			.deleteAllData()
//			.build();
//		Status deleteStat = Fitness.HistoryApi.deleteData(mClient,ddr).await(numVals*1000, TimeUnit.MILLISECONDS);
//		SessionInsertRequest reqIns = reqB.setSession(session).build();
//		Status tmpStat = Fitness.SessionsApi.insertSession(mClient, reqIns).await(1, TimeUnit.MINUTES);//.await(Math.max(numVals*1000,30000), TimeUnit.MILLISECONDS);
//		if (tmpStat.isSuccess()) {
//			for (DataSet d:datasetsToPlace) {
//				tmpStat = Fitness.HistoryApi.insertData(mClient, d).await(1, TimeUnit.MINUTES);
//				if (!tmpStat.isSuccess())
//					break;
//			}
//		}
//		if (tmpStat.isSuccess()) {
//	        SessionReadRequest readRequest = new SessionReadRequest.Builder()
//            .setTimeInterval(sesStartTime, sesStopTime, TimeUnit.MILLISECONDS)
//            .read(DataType.TYPE_HEART_RATE_BPM)
//            .setSessionId(mainDevice+"_"+sdf.format(sesStartTime))
//            .build();
//	        SessionReadResult secstat = Fitness.SessionsApi.readSession(mClient, readRequest)
//                    .await(numVals*1000, TimeUnit.MILLISECONDS);
//	        if (secstat.getStatus().isSuccess()) {
//	        	for (Session sest : secstat.getSessions()) {
//	                // Process the session
//	                dumpSession(sest);
//
//	                // Process the data sets for this session
//	                List<DataSet> dataSets = secstat.getDataSet(sest);
//	                for (DataSet dataSet : dataSets) {
//	                    dumpDataSet(dataSet);
//	                }
//	            }
//	        }
//		}
//		return tmpStat.getStatusCode();
//	}

    private void dumpDataSet(DataSet dataSet) {
        Timber.tag(TAG).i("DST = " + dataSet.getDataType().getName() + " (" + dataSet.getDataPoints().size() + ")" +
                " DSN = " + dataSet.getDataSource().getStreamName() +
                " DSI=" + dataSet.getDataSource().getStreamIdentifier());
    }

    private void dumpDataPoint(DataPoint dp) {
        SimpleDateFormat df = new SimpleDateFormat("yyMMdd_HHmmss");
        String t;
        t = "DPTP = " + dp.getDataType().getName() +
                " [" + df.format(dp.getStartTime(TimeUnit.MILLISECONDS)) +
                "-" + df.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + "] {";
        for (Field field : dp.getDataType().getFields()) {
            t += "F_" + field.getName() + "=" + dp.getValue(field) + " / ";
        }
        t += "}";
        Timber.tag(TAG).i(t);
    }

    private void dumpDataSetAndPoints(DataSet dataSet) {
        Timber.tag(TAG).i("----------------------------------");
        dumpDataSet(dataSet);
        for (DataPoint dp : dataSet.getDataPoints()) {
            dumpDataPoint(dp);
        }
        Timber.tag(TAG).i("----------------------------------");
    }

    private void dumpPoints(ArrayList<DataPoint> adp) {
        for (DataPoint dp : adp) {
            dumpDataPoint(dp);
        }
    }

    private void dumpSession(Session session) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss");
        Timber.tag(TAG).i("Data returned for Session: " + session.getName() + " [" + session.getIdentifier() + "]"
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + session.getStartTime(TimeUnit.MILLISECONDS)
                + "\n\tEnd: " + session.getEndTime(TimeUnit.MILLISECONDS));
    }

    private void requestSynch() {

        // Register for sync status changes
        final MySyncStatusObserver observer = new MySyncStatusObserver(accountList, "com.google.android.gms.fitness", this);
        final Object providerHandle = ContentResolver.addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE |
                        ContentResolver.SYNC_OBSERVER_TYPE_PENDING, observer);
        // Pass in the handle so the observer can unregister itself from events when finished.
        // You could optionally save this handle at the Activity level but I prefer to
        // encapsulate everything in the observer and let it handle everything
        observer.setProviderHandle(providerHandle);

        setAutoSynch(true);
    }

    @Override
    public void onSyncsStarted() {
        Timber.tag(TAG).i("Synch started");
    }

    @Override
    public void onSyncsFinished() {
        Timber.tag(TAG).i("Synch finished");

        setAutoSynch(false);
        if (mRepeatAfterSynch>=0)
            new WorkerThread().start();
        else
            stop();
    }

    private void setAutoSynch(boolean ena) {
        final Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
        for (final Account account : accountList) {
            // Request the sync
            Timber.tag(TAG).i("Processing "+account.name);
            ContentResolver.setSyncAutomatically(account, FIT_AUTHORITY, ena);
        }

    }

    private class WorkerThread extends Thread {
        public WorkerThread() {
            super();
        }
        public void run() {
            if (mIsRunning == MyStatus.STARTING) {
                mIsRunning = MyStatus.RUNNING;
                boolean repeatAborted = false;
                int n = -1;
                if (mSQL == null) {
                    mSQL = new MySQLiteHelper(GoogleFitService.this.getApplicationContext(), mDbPath);
                    mSQL.openDB();
                }
                if (mSessions == null) {
                    try {
                        mSessions = mSQL.getAllSessions(
                                mSessionDateSta >= 0 ? new Date(mSessionDateSta) : null,
                                mSessionDateSto >= 0 ? new Date(mSessionDateSto) : null,
                                mSessionOperation == FIT_EXTRA_SESSION_OPERATION_INSERT && mSessionKey<0 && mSessionKeys==null?
                                        Boolean.valueOf(false) : null);
                    } catch (Exception e) {
                        Timber.tag(TAG).e("Cannot get sessions");
                        e.printStackTrace();
                        CA.lbm.sendBroadcast(new Intent(FIT_NOTIFY_INTENT).putExtra(FIT_EXTRA_NOTIFY_SUSPENDED_REASON, 8000));
                    }
                }
                if (mSessions != null) {
                    Status statusC = null;
                    long currentKey;
                    List<PSessionHolder> cS;
                    if (mSessionKeys!=null && mSessionKeys.length>0) {
                        LongSparseArray<List<PSessionHolder>> newv = new LongSparseArray<>();
                        List<PSessionHolder> rx;
                        for (int i = 0; i<mSessionKeys.length; i++) {
                            if ((rx = mSessions.get(mSessionKeys[i]))!=null) {
                                newv.put(mSessionKeys[i],rx);
                            }
                        }
                        if (newv.size()>0)
                            mSessions = newv;
                    }
                    else if (mSessionKey >= 0) {
                        int off = mSessions.indexOfKey(mSessionKey);
                        if (off>=0)
                            mSessionOffset = mSessionOffset>=0?mSessionOffset+off:off;
                    }
                    mSessionKey = -1;
                    mSessionKeys = null;
                    if (mSessionOffset < 0)
                        mSessionOffset = 0;
                    if (mSessionNumber < 0)
                        n = mSessions.size();
                    else
                        n = mSessionNumber;
                    Timber.tag(TAG).i("Session number is " + mSessions.size());

                    if (mSessionOperation == FIT_EXTRA_SESSION_OPERATION_DELETEALL) {
                        if (mSessionDateSta >= 0 && mSessionDateSto >= 0) {
                            statusC = deleteSessions(mSessionDateSta, mSessionDateSto);
                            if (!statusC.isSuccess())
                                Timber.tag(TAG).e("Interrupped DeleteAll SC=" + statusC.getStatusCode());
                            else {
                                for (int i = 0; i < mSessions.size() && mIsRunning == MyStatus.RUNNING; i++) {
                                    currentKey = mSessions.keyAt(i);
                                    mSQL.setSessionExported(currentKey, PSessionHolder.NOT_EXPORTED);
                                }
                            }
                        }
                        repeatAborted = true;

                    } else {
                        String outputStr = "";
                        for (int i = mSessionOffset; i < mSessions.size() && i < mSessionOffset + n && mIsRunning == MyStatus.RUNNING; i++) {
                            cS = mSessions.get(currentKey = mSessions.keyAt(i));
                            Timber.tag(TAG).i("Processing session " + i + "th [K=" + currentKey + "]");
                            if (mSessionOperation!=FIT_EXTRA_SESSION_OPERATION_INSERT || !isExported(cS)) {
                                try {
                                    if (i == mSessionOffset)
                                        setAutoSynch(false);
                                    if ((mSessionOperation == FIT_EXTRA_SESSION_OPERATION_READ && !(statusC = readSession(currentKey, cS)).isSuccess())
                                            || (mSessionOperation == FIT_EXTRA_SESSION_OPERATION_DELETE && !(statusC = deleteSession(currentKey, cS)).isSuccess())
                                            || (mSessionOperation == FIT_EXTRA_SESSION_OPERATION_DELETEALL_BY_SESSION && !(statusC = deleteAllBySession(currentKey, cS)).isSuccess())
                                            || (mSessionOperation == FIT_EXTRA_SESSION_OPERATION_INSERT && !(statusC = insertSession(currentKey, cS)).isSuccess())
                                            || (mSessionOperation == FIT_EXTRA_SESSION_OPERATION_INSCHECK && !(statusC = insertSessionCheck(currentKey, cS)).isSuccess())
                                            || (mSessionOperation == FIT_EXTRA_SESSION_OPERATION_FORCEINSERT && !(statusC = insertSession(currentKey, cS)).isSuccess())) {
                                        CA.lbm.sendBroadcast(new Intent(FIT_NOTIFY_INTENT)
                                                .putExtra(FIT_EXTRA_NOTIFY_SUSPENDED_REASON, statusC.getStatusCode())
                                                .putExtra(FIT_EXTRA_NOTIFY_SUSPENDED_SESSION, currentKey));
                                        Timber.tag(TAG).e("Interrupped SC=" + statusC.getStatusCode() + " " + " at " + currentKey);
                                    }
                                    if (statusC != null) {
                                        if (mSessionOperation==FIT_EXTRA_SESSION_OPERATION_INSCHECK) {
                                            if (statusC.getStatusCode()==CommonStatusCodes.SERVICE_VERSION_UPDATE_REQUIRED)
                                                outputStr+=","+currentKey;

                                        }

                                        if (mSessionOperation == FIT_EXTRA_SESSION_OPERATION_INSERT ||
                                                mSessionOperation == FIT_EXTRA_SESSION_OPERATION_FORCEINSERT)
                                            mSQL.setSessionExported(currentKey, statusC.getStatusCode());
                                        else if (mSessionOperation == FIT_EXTRA_SESSION_OPERATION_DELETE)
                                            mSQL.setSessionExported(currentKey, PSessionHolder.NOT_EXPORTED);
                                    }
                                    for (PSessionHolder ss : cS) {
                                        ss.prepareToValues(5);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    repeatAborted = true;
                                    break;
                                }
                            }
                        }
                        if (!outputStr.isEmpty())
                            Timber.tag(TAG).e("INSCHECK "+outputStr.substring(1));
                    }
                }
                else
                    repeatAborted = true;
                if (mRepeatAfterSynch>=0 && !repeatAborted &&  mIsRunning == MyStatus.RUNNING) {
                    mIsRunning = MyStatus.STARTING;
                    mSessionOffset += n;
                    if (mSessionOffset>=mSessions.size() ||
                            (mSessionOperation != FIT_EXTRA_SESSION_OPERATION_INSERT && mSessionOperation != FIT_EXTRA_SESSION_OPERATION_FORCEINSERT))
                        mRepeatAfterSynch = -1;
                    else
                        mRepeatAfterSynch--;
                    requestSynch();
                }
                else
                    GoogleFitService.this.stop();
            }
        }

        private boolean isExported(List<PSessionHolder> cS) {
            int exp;
            for (PSessionHolder sesh:cS)
                if ((exp = sesh.getExported())==65535 || exp==15)
                    return false;
            return true;
        }
    };


    //https://github.com/k2s/zephyr-hxm-bt-for-google-fit/blob/master/app/src/main/java/com/xtmotion/zephyrhxmbtforgooglefit/MainActivity.java
    private void buildFitnessClient() {
        dumpAccounts();
        if (mClient != null)
            return;

        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.CONFIG_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.SESSIONS_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(
                        new ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Timber.tag(TAG).i("Connected!!!");
                                new WorkerThread().start();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Timber.tag(TAG).e("Connection lost.  Cause: Network Lost.");
                                } else if (i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Timber.tag(TAG).e("Connection lost.  Reason: Service Disconnected");
                                }
                                else {
                                    Timber.tag(TAG).e("Connection error SC="+i);
                                }
                                CA.lbm.sendBroadcast(new Intent(FIT_NOTIFY_INTENT).putExtra(FIT_EXTRA_NOTIFY_SUSPENDED_REASON, i));
                                stop();
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Timber.tag(TAG).i("Connection failed.  Reason: " + result.getErrorCode());
                                notifyUiFailedConnection(result);
                            }
                        }
                )
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        accountList = AccountManager.get(this).getAccountsByType("com.google");
        Timber.tag(TAG).i("Service Started.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular
        // example, close() is
        // invoked when the UI is disconnected from the Service.
        return super.onUnbind(intent);
    }

    public static MyStatus getStatus() {
        // TODO Auto-generated method stub
        return mIsRunning;
    }

}
