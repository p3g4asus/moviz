package com.moviz.lib.googlefit;

public class AggregateCalculator {
    public class AggregateValue {
        public double sum = 0.0, max = Double.MIN_NORMAL, min = Double.MAX_VALUE;
        public long tStart = -1, tStop = -1;
        public int n = 0;
        public double mean = 0.0;
        public double firstV = Double.NaN, lastV = Double.NaN;
    }

    private AggregateValue tmpAgg = new AggregateValue();
    private boolean diffAgg = false;
    private double lastV = Double.NaN;
    private long lastT = Long.MIN_VALUE;
    private double currOffset = 0.0;

    public AggregateCalculator() {

    }

    public AggregateCalculator(boolean diff) {
        diffAgg = diff;
    }


    public AggregateValue newData(double v, long t, long dist, ActivitySegments acts) {
        AggregateValue rv = null;
        if (lastT > t)
            return null;
        lastT = t;
        boolean inInterval = acts.c(t);
        if (!Double.isNaN(v) && !Double.isNaN(lastV) && lastV > v && diffAgg)
            currOffset += lastV;
        if (!Double.isNaN(v)) {
            lastV = v;
            v += currOffset;
        }
        if (tmpAgg.tStart >= 0) {
            if (Double.isNaN(v) || !inInterval || t - tmpAgg.tStart > dist) {
                rv = tmpAgg;
                rv.mean = diffAgg ?
                        (Double.isNaN(v) ? rv.lastV - rv.firstV : (rv.lastV = v) - rv.firstV) :
                        rv.sum / rv.n;
                rv.tStop = inInterval ? t - 1 : acts.endInterval(tmpAgg.tStart);
                if (rv.tStop <= rv.tStart || (diffAgg && rv.mean == 0.0))
                    rv = null;
                tmpAgg = new AggregateValue();
            }
        }
        if (Double.isNaN(v))
            return rv;
        if (tmpAgg.tStart < 0) {
            if (inInterval) {
                tmpAgg.tStart = t;
                tmpAgg.firstV = v;
            } else
                return rv;
        }
        if (diffAgg)
            tmpAgg.lastV = v;
        else {
            tmpAgg.sum += v;
            if (v > tmpAgg.max)
                tmpAgg.max = v;
            if (v < tmpAgg.min)
                tmpAgg.min = v;
            tmpAgg.n++;
        }

        return rv;
    }

    void reset() {
        tmpAgg = new AggregateValue();
        currOffset = 0.0;
        lastV = Double.NaN;
        lastT = Long.MIN_VALUE;
    }

}
