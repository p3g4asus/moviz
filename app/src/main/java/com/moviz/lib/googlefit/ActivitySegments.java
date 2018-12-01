package com.moviz.lib.googlefit;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.moviz.gui.app.CA;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ActivitySegments extends ArrayList<ActivitySegment> {

    private static final long serialVersionUID = -1843104759171703708L;
    public final static DataSource DATASOURCE = new DataSource.Builder()
            .setType(DataSource.TYPE_RAW)
            .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
            .setAppPackageName(CA.PACKAGE_NAME)
            .build();

    public boolean c(long t) {
        for (ActivitySegment as : this) {
            if (as.c(t))
                return true;
        }
        return false;
    }

    public void add(long... ts) {
        for (long t : ts) {
            if (!closed()) {
                close(t);
            } else
                add(new ActivitySegment(t));
        }
    }

    public boolean closed() {
        if (size() > 0)
            return get(size() - 1).valid();
        else
            return true;
    }

    public void close(long t) {
        if (size() > 0) {
            ActivitySegment as = get(size() - 1);
            as.tStop = t;
            if (!as.valid())
                remove(size() - 1);
        }
    }

    public DataSet createDataset(String act) {
        DataSet segmDataSet = null;
        segmDataSet = DataSet.create(DATASOURCE);
        for (ActivitySegment as : this) {
            if (as.valid()) {
                DataPoint dp = segmDataSet.createDataPoint().setTimeInterval(as.tStart, as.tStop, TimeUnit.MILLISECONDS);
                dp.getValue(Field.FIELD_ACTIVITY).setActivity(act);
                segmDataSet.add(dp);
            }
        }
        return segmDataSet;
    }

    public long endInterval(long tStart) {
        for (ActivitySegment as : this) {
            if (as.c(tStart))
                return as.tStop;
        }
        return -1;
    }

    public long getEnd() {
        if (size() > 0)
            return get(size() - 1).tStop;
        else
            return -1;
    }
}
