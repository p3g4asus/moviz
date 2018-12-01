package com.moviz.lib.hw;


/**
 * Created by Fujitsu on 25/10/2016.
 */
public class WahooBlueSCBinder extends WahooBinder {
    public void setWheelDiam(GenericDevice d, long diam) {
        WahooBlueSCDataProcessor dp = (WahooBlueSCDataProcessor) newDp(d);
        dp.setWheelDiam(diam);
    }

    public long getWheelDiam(GenericDevice d) {
        WahooBlueSCDataProcessor dp = (WahooBlueSCDataProcessor) newDp(d);
        return dp.getWheelDiam();
    }

    public void setCurrentGear(GenericDevice d, int n) {
        WahooBlueSCDataProcessor dp = (WahooBlueSCDataProcessor) newDp(d);
        dp.setCurrentGear(n);
    }

    public int getCurrentGear(GenericDevice d) {
        WahooBlueSCDataProcessor dp = (WahooBlueSCDataProcessor) newDp(d);
        return dp.getCurrentGear();
    }



}
