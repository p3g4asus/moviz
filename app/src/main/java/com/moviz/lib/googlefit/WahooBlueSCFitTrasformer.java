package com.moviz.lib.googlefit;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.moviz.gui.app.CA;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.WahooBlueSCHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Matteo on 30/10/2016.
 */

public class WahooBlueSCFitTrasformer implements GoogleFitPointTransformer {
    private ArrayList<DataSource> dsources = new ArrayList<DataSource>();
    private ArrayList<DataSet> dsets = new ArrayList<DataSet>();
    private final static int IDX_DISTANCE_DELTA = 0;
    private final static int IDX_CALORIES_EXPENDED = 1;
    private final static int IDX_SPEED = 2;
    private final static int IDX_CYCLING_WHEEL_REVOLUTION = 3;
    private final static int IDX_PEDALING_CUMULATIVE = 4;
    private AggregateCalculator distanceAgg = new AggregateCalculator(true);
    private AggregateCalculator caloriesAgg = new AggregateCalculator(true);
    private AggregateCalculator speedAgg = new AggregateCalculator();
    private AggregateCalculator wheelRevAgg = new AggregateCalculator();
    private AggregateCalculator pedalingAgg = new AggregateCalculator();
    private int maxPoints = -1;

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
        dsets.clear();
        dsources.clear();
        distanceAgg.reset();
        caloriesAgg.reset();
        speedAgg.reset();
        wheelRevAgg.reset();
        pedalingAgg.reset();
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
                        .setDataType(DataType.TYPE_CYCLING_WHEEL_REVOLUTION)
                        .setType(DataSource.TYPE_RAW)
                        .setDevice(dsDevice)
                        .setName(dSourceNamePrefix + "CYCLING_WHEEL_REVOLUTION")
                        .setAppPackageName(appPackageName)
                        .build()
        );
        dsources.add(new DataSource.Builder()
                        .setDataType(DataType.TYPE_CYCLING_PEDALING_CUMULATIVE)
                        .setType(DataSource.TYPE_RAW)
                        .setDevice(dsDevice)
                        .setName(dSourceNamePrefix + "CYCLING_PEDALING_CUMULATIVE")
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
    public List<DataSet> insertDataPoint(DeviceUpdate value, long basTs, long dist, ActivitySegments acts) {
        WahooBlueSCHolder u = (WahooBlueSCHolder) value;
        long t = u == null ? acts.getEnd() : u.getAbsTs() + basTs;
        DataPoint d;
        boolean rv = false;
        List<DataSet> fullDs = new ArrayList<DataSet>();
        DataSet c;
        AggregateCalculator.AggregateValue av;
        if (u.sensType == WahooBlueSCHolder.SensorType.WHEEL) {
            av = wheelRevAgg.newData(u == null ? Double.NaN : u.sensVal, t, dist, acts);
            if (av != null) {
                d = (c = dsets.get(IDX_CYCLING_WHEEL_REVOLUTION)).createDataPoint();
                c.add(d.setIntValues((int) u.sensVal).setTimestamp((long) (av.tStart + ((double) (av.tStop - av.tStart)) / 2.0 + 0.5), TimeUnit.MILLISECONDS));
                if (c.getDataPoints().size() >= maxPoints && maxPoints > 0) {
                    fullDs.add(c);
                    dsets.set(IDX_CYCLING_WHEEL_REVOLUTION, DataSet.create(dsources.get(IDX_CYCLING_WHEEL_REVOLUTION)));
                }
            }
            av = distanceAgg.newData(u == null ? Double.NaN : u.distance * 1000.0, t, dist, acts);
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
            av = speedAgg.newData(u == null ? Double.NaN : u.speedKmHmn * 1000.0 / 3600.0, t, dist, acts);
            if (av != null) {
                d = (c = dsets.get(IDX_SPEED)).createDataPoint();
                c.add(d.setFloatValues((float) av.mean, (float) av.max, (float) av.min).
                        setTimeInterval(av.tStart, av.tStop, TimeUnit.MILLISECONDS));
                if (c.getDataPoints().size() >= maxPoints && maxPoints > 0) {
                    fullDs.add(c);
                    dsets.set(IDX_SPEED, DataSet.create(dsources.get(IDX_SPEED)));
                }
            }
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
        } else {
            av = pedalingAgg.newData(u == null ? Double.NaN : u.sensVal, t, dist, acts);
            if (av != null) {
                d = (c = dsets.get(IDX_PEDALING_CUMULATIVE)).createDataPoint();
                c.add(d.setIntValues((int) u.sensVal).setTimestamp((long) (av.tStart + ((double) (av.tStop - av.tStart)) / 2.0 + 0.5), TimeUnit.MILLISECONDS));
                if (c.getDataPoints().size() >= maxPoints && maxPoints > 0) {
                    fullDs.add(c);
                    dsets.set(IDX_PEDALING_CUMULATIVE, DataSet.create(dsources.get(IDX_PEDALING_CUMULATIVE)));
                }
            }
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