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
import com.moviz.lib.comunication.holder.DeviceHolder;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.HeartUpdate;

import java.util.ArrayList;
import java.util.List;

public abstract class HeartFitTransformer implements GoogleFitPointTransformer {
    private ArrayList<DataSource> dsources = new ArrayList<DataSource>();
    private ArrayList<DataSet> dsets = new ArrayList<DataSet>();
    private AggregateCalculator agg = new AggregateCalculator();
    private int maxPoints = -1;
    private DataType dataType;

    public HeartFitTransformer(DataType t) {
        dataType = t;
    }

    @Override
    public int getMaxPoints() {
        return maxPoints;
    }

    @Override
    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    @Override
    public List<DataSet> pointsToSend() {
        List<DataSet> fullDs = new ArrayList<DataSet>();
        DataSet d;
        if (!(d = dsets.get(0)).isEmpty())
            fullDs.add(d);
        return fullDs;
    }


    @Override
    public List<DataSet> getDataSets() {
        return dsets;
    }

    @Override
    public List<DataSource> getDataSources() {
        return dsources;
    }

    protected abstract DataPoint fillDataPoint(AggregateCalculator.AggregateValue av, DataPoint d);

    @Override
    public List<DataSet> insertDataPoint(DeviceUpdate value, long baseTs, long dist, ActivitySegments segms) {
        List<DataSet> fullDs = new ArrayList<DataSet>();
        HeartUpdate h = (HeartUpdate) value;
        DataSet c;
        long ts = h == null ? segms.getEnd() : baseTs + value.getAbsTs();
        AggregateCalculator.AggregateValue av = agg.newData(h == null ? Double.NaN : h.getPulse(), ts, dist, segms);
        if (av != null) {
            DataPoint d = (c = dsets.get(0)).createDataPoint();
            c.add(fillDataPoint(av, d));
            if (c.getDataPoints().size() >= maxPoints && maxPoints > 0) {
                fullDs.add(c);
                dsets.set(0, DataSet.create(dsources.get(0)));
            }
            return fullDs;
        } else
            return null;
    }

    @Override
    public String fitActivity() {
        return FitnessActivities.UNKNOWN;
    }

    @Override
    public long fitPauseDetectTh() {
        return 5000;
    }

    @Override
    public Status createDataSources(GoogleApiClient client, DeviceHolder dev) {
        reset();
        String dSourceNamePrefix = dev.getAlias();
        String appPackageName = CA.PACKAGE_NAME;
        Device dsDevice = new Device(
                dSourceNamePrefix,
                "BLE HR Device",
                dev.getAddress(),
                Device.TYPE_UNKNOWN);
        dSourceNamePrefix += "_";
        dsources.add(new DataSource.Builder()
                        .setDataType(dataType)
                        .setType(DataSource.TYPE_RAW)
                        .setDevice(dsDevice)
                        .setName(dSourceNamePrefix + "HEART_RATE")
                        .setAppPackageName(appPackageName)
                        .build()
        );
        for (DataSource ds : dsources) {
            dsets.add(DataSet.create(ds));
        }
        return new Status(CommonStatusCodes.SUCCESS);
    }

    @Override
    public void reset() {
        dsources.clear();
        dsets.clear();
        agg.reset();
    }

}
