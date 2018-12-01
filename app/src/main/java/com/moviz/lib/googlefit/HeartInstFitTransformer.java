package com.moviz.lib.googlefit;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;

import java.util.concurrent.TimeUnit;

public abstract class HeartInstFitTransformer extends HeartFitTransformer {
    public HeartInstFitTransformer() {
        super(DataType.TYPE_HEART_RATE_BPM);
    }

    @Override
    protected DataPoint fillDataPoint(AggregateCalculator.AggregateValue av, DataPoint d) {
        return d.setFloatValues((float) av.mean).setTimestamp((long) (av.tStart + ((double) (av.tStop - av.tStart)) / 2.0 + 0.5), TimeUnit.MILLISECONDS);
    }
}
