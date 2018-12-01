package com.moviz.lib.plot;

import com.androidplot.xy.XYSeries;

import java.util.List;

public class MinimalXYSeries implements XYSeries {
    private String title = "";
    private List<? extends Number> xVals = null;
    private List<? extends Number> yVals = null;

    @Override
    public String getTitle() {
        // TODO Auto-generated method stub
        return title;
    }

    public MinimalXYSeries(String tit, List<? extends Number> xv, List<? extends Number> yv) {
        if (xv == null || yv == null || xv.size() != yv.size())
            throw new IllegalArgumentException("xv and yv must be list of the same length");
        title = tit;
        xVals = xv;
        yVals = yv;
    }

    @Override
    public Number getX(int arg0) {

        return xVals.get(arg0);
    }

    @Override
    public Number getY(int arg0) {
        return yVals.get(arg0);
    }

    @Override
    public int size() {
        return yVals.size();
    }

}
