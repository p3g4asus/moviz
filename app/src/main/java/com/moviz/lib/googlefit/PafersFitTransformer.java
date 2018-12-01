package com.moviz.lib.googlefit;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.moviz.gui.app.CA;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PafersFitTransformer implements GoogleFitPointTransformer {
    private ArrayList<DataSource> dsources = new ArrayList<DataSource>();
    private ArrayList<DataSet> dsets = new ArrayList<DataSet>();
    private final static int IDX_DISTANCE_DELTA = 0;
    private final static int IDX_CALORIES_EXPENDED = 1;
    private final static int IDX_SPEED = 2;
    private final static int IDX_POWER = 3;
    private final static int IDX_CYCLING_WHEEL_RPM = 4;
    private final static int IDX_CUSTOM_INCLINE = 5;
    private AggregateCalculator distanceAgg = new AggregateCalculator(true);
    private AggregateCalculator caloriesAgg = new AggregateCalculator(true);
    private AggregateCalculator speedAgg = new AggregateCalculator();
    private AggregateCalculator powerAgg = new AggregateCalculator();
    private AggregateCalculator rpmAgg = new AggregateCalculator();
    private int lastIncline = -1;
    private long lastChangeIncline = -1;
    private int maxPoints = -1;
    private double lastDist = -1.0;

    @Override
    public int getMaxPoints() {
        return maxPoints;
    }

    @Override
    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    @Override
    public void reset() {
        lastIncline = -1;
        lastChangeIncline = -1;
        lastDist = -1.0;
        dsets.clear();
        dsources.clear();
        distanceAgg.reset();
        caloriesAgg.reset();
        speedAgg.reset();
        powerAgg.reset();
        rpmAgg.reset();
    }

    @Override
    public List<DataSet> pointsToSend() {
        List<DataSet> fullDs = new ArrayList<DataSet>();
        for (DataSet d : dsets) {
            if (!d.isEmpty())
                fullDs.add(d);
        }
        return fullDs;
    }

    @Override
    public boolean validPoint(com.moviz.lib.comunication.holder.DeviceUpdate upd) {
        return true;
    }

    @Override
    public Status createDataSources(GoogleApiClient client, com.moviz.lib.comunication.holder.DeviceHolder dev) {
        reset();
        String dSourceNamePrefix = dev.getAlias();
        String appPackageName = CA.PACKAGE_NAME;
        Device dsDevice = new Device(
                dSourceNamePrefix,
                dev.getDescription(),
                dev.getAddress(),
                Device.TYPE_UNKNOWN);
        dSourceNamePrefix += "_";
        dsources.add(new DataSource.Builder()
                        .setDataType(DataType.AGGREGATE_DISTANCE_DELTA)
                        .setType(DataSource.TYPE_RAW)
                        .setDevice(dsDevice)
                        .setName(dSourceNamePrefix + "DISTANCE_DELTA")
                        .setAppPackageName(appPackageName)
                        .build()
        );
        dsources.add(new DataSource.Builder()
                        .setDataType(DataType.AGGREGATE_CALORIES_EXPENDED)
                        .setType(DataSource.TYPE_RAW)
                        .setDevice(dsDevice)
                        .setName(dSourceNamePrefix + "CALORIES_EXPENDED")
                        .setAppPackageName(appPackageName)
                        .build()
        );
        dsources.add(new DataSource.Builder()
                        .setDataType(DataType.AGGREGATE_SPEED_SUMMARY)
                        .setType(DataSource.TYPE_RAW)
                        .setDevice(dsDevice)
                        .setName(dSourceNamePrefix + "SPEED")
                        .setAppPackageName(appPackageName)
                        .build()
        );
        dsources.add(new DataSource.Builder()
                        .setDataType(DataType.AGGREGATE_POWER_SUMMARY)
                        .setType(DataSource.TYPE_RAW)
                        .setDevice(dsDevice)
                        .setName(dSourceNamePrefix + "POWER")
                        .setAppPackageName(appPackageName)
                        .build()
        );
        dsources.add(new DataSource.Builder()
                        .setDataType(DataType.TYPE_CYCLING_WHEEL_RPM)
                        .setType(DataSource.TYPE_RAW)
                        .setDevice(dsDevice)
                        .setName(dSourceNamePrefix + "CYCLING_WHEEL_RPM")
                        .setAppPackageName(appPackageName)
                        .build()
        );
        String inclineTypeName = CA.PACKAGE_NAME + "." + dev.getClass().getName() + ".incline";
        DataTypeResult restmp = Fitness.ConfigApi.readDataType(client, inclineTypeName).await(1, TimeUnit.MINUTES);
        Status statustmp = restmp.getStatus();
        DataType dataSourceType;
        if (statustmp.isSuccess()) {
            dataSourceType = restmp.getDataType();
        } else if (statustmp.isInterrupted()) {
            return statustmp;
        } else {
            DataTypeCreateRequest.Builder requestB = new DataTypeCreateRequest.Builder()
                    // The prefix of your data type name must match your app's package name
                    .setName(inclineTypeName);
            requestB.addField("incline", Field.FORMAT_INT32);
            restmp = Fitness.ConfigApi.createCustomDataType(client, requestB.build()).await(1, TimeUnit.MINUTES);
            statustmp = restmp.getStatus();
            if (statustmp.isSuccess()) {
                dataSourceType = restmp.getDataType();
            } else {
                return statustmp;
            }
        }

        dsources.add(new DataSource.Builder()
                        .setDataType(dataSourceType)
                        .setType(DataSource.TYPE_RAW)
                        .setDevice(dsDevice)
                        .setName(dSourceNamePrefix + "CUSTOM_INCLINE")
                        .setAppPackageName(appPackageName)
                        .build()
        );

        for (DataSource ds : dsources)
            dsets.add(DataSet.create(ds));
        return new Status(CommonStatusCodes.SUCCESS);
    }

    @Override
    public List<DataSet> getDataSets() {
        return dsets;
    }

    @Override
    public List<DataSource> getDataSources() {
        return dsources;
    }

    @Override
    public List<DataSet> insertDataPoint(com.moviz.lib.comunication.holder.DeviceUpdate value, long basTs, long dist, ActivitySegments acts) {
        com.moviz.lib.comunication.holder.PafersHolder u = (com.moviz.lib.comunication.holder.PafersHolder) value;
        long t = u == null ? acts.getEnd() : u.getAbsTs() + basTs;
        DataPoint d;
        boolean rv = false;
        List<DataSet> fullDs = new ArrayList<DataSet>();
        DataSet c;
        AggregateCalculator.AggregateValue av;

        av = rpmAgg.newData(u == null ? Double.NaN : u.rpm, t, dist, acts);
        if (av != null) {
            d = (c = dsets.get(IDX_CYCLING_WHEEL_RPM)).createDataPoint();
            c.add(d.setFloatValues((float) av.mean).setTimeInterval(av.tStart, av.tStop, TimeUnit.MILLISECONDS));
            if (c.getDataPoints().size() >= maxPoints && maxPoints > 0) {
                fullDs.add(c);
                dsets.set(IDX_CYCLING_WHEEL_RPM, DataSet.create(dsources.get(IDX_CYCLING_WHEEL_RPM)));
            }
        }
        if (u==null || u.distanceR>=lastDist) {
            av = distanceAgg.newData(u == null ? Double.NaN : u.distanceR * 1000.0, t, dist, acts);
            if (av != null) {
                d = (c = dsets.get(IDX_DISTANCE_DELTA)).createDataPoint();
                c.add(d.setFloatValues((float) av.mean).
                        setTimeInterval(av.tStart, av.tStop, TimeUnit.MILLISECONDS));
                if (c.getDataPoints().size() >= maxPoints && maxPoints > 0) {
                    fullDs.add(c);
                    dsets.set(IDX_DISTANCE_DELTA, DataSet.create(dsources.get(IDX_DISTANCE_DELTA)));
                }
                rv = true;
            }
            if (u!=null)
                lastDist = u.distanceR;
        }
        av = speedAgg.newData(u == null ? Double.NaN : u.speed * 1000.0 / 3600.0, t, dist, acts);
        if (av != null) {
            d = (c = dsets.get(IDX_SPEED)).createDataPoint();
            c.add(d.setFloatValues((float) av.mean, (float) av.max, (float) av.min).
                    setTimeInterval(av.tStart, av.tStop, TimeUnit.MILLISECONDS));
            if (c.getDataPoints().size() >= maxPoints && maxPoints > 0) {
                fullDs.add(c);
                dsets.set(IDX_SPEED, DataSet.create(dsources.get(IDX_SPEED)));
            }
        }
        av = powerAgg.newData(u == null ? Double.NaN : u.watt, t, dist, acts);
        if (av != null) {
            d = (c = dsets.get(IDX_POWER)).createDataPoint();
            c.add(d.setFloatValues((float) av.mean, (float) av.max, (float) av.min).
                    setTimeInterval(av.tStart, av.tStop, TimeUnit.MILLISECONDS));
            if (c.getDataPoints().size() >= maxPoints && maxPoints > 0) {
                fullDs.add(c);
                dsets.set(IDX_POWER, DataSet.create(dsources.get(IDX_POWER)));
            }
        }
        if (u!=null) {
            av = caloriesAgg.newData(u == null ? Double.NaN : u.calorie, t, dist, acts);
            if (av != null) {
                d = (c = dsets.get(IDX_CALORIES_EXPENDED)).createDataPoint();
                c.add(d.setFloatValues((float) av.mean).
                        setTimeInterval(av.tStart, av.tStop, TimeUnit.MILLISECONDS));
                if (c.getDataPoints().size() >= maxPoints && maxPoints > 0) {
                    fullDs.add(c);
                    dsets.set(IDX_CALORIES_EXPENDED, DataSet.create(dsources.get(IDX_CALORIES_EXPENDED)));
                }
            }
        }
        if (acts.c(t) && (u == null || (u != null && lastIncline != u.incline))) {
            if (lastIncline > 0) {
                d = (c = dsets.get(IDX_CUSTOM_INCLINE)).createDataPoint();
                c.add(d.setIntValues(lastIncline).setTimeInterval(lastChangeIncline, t, TimeUnit.MILLISECONDS));
                if (c.getDataPoints().size() >= maxPoints && maxPoints > 0) {
                    fullDs.add(c);
                    dsets.set(IDX_CUSTOM_INCLINE, DataSet.create(dsources.get(IDX_CUSTOM_INCLINE)));
                }
            }
            if (u != null)
                lastIncline = u.incline;
            lastChangeIncline = t;
        }
        if (!fullDs.isEmpty() || rv) {
            return fullDs;
        } else
            return null;
    }

    @Override
    public String fitActivity() {
        return FitnessActivities.BIKING_STATIONARY;
    }

    @Override
    public long fitPauseDetectTh() {
        return 5000;
    }

}
