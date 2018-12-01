package com.moviz.lib.googlefit;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;

import java.util.concurrent.TimeUnit;

public abstract class HeartAggFitTransformer extends HeartFitTransformer {

    public HeartAggFitTransformer() {
        super(DataType.AGGREGATE_HEART_RATE_SUMMARY);
    }

    @Override
    protected DataPoint fillDataPoint(AggregateCalculator.AggregateValue av, DataPoint d) {
        return d.setFloatValues((float) av.mean, (float) av.max, (float) av.min).setTimeInterval(av.tStart, av.tStop, TimeUnit.MILLISECONDS);
    }

}
