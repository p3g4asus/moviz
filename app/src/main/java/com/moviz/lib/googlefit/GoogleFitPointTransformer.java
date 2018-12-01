package com.moviz.lib.googlefit;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.moviz.lib.comunication.holder.DeviceHolder;
import com.moviz.lib.comunication.holder.DeviceUpdate;

import java.util.List;

public interface GoogleFitPointTransformer {
    boolean validPoint(DeviceUpdate upd);

    List<DataSet> getDataSets();

    List<DataSource> getDataSources();

    List<DataSet> insertDataPoint(DeviceUpdate value, long basTs, long dist, ActivitySegments segm);

    String fitActivity();

    long fitPauseDetectTh();

    Status createDataSources(GoogleApiClient client, DeviceHolder dev);

    //	void stopSegment(long stopTime);
//	void startSegment(long startTime);
    void reset();

    List<DataSet> pointsToSend();

    void setMaxPoints(int maxPoints);

    int getMaxPoints();
}
