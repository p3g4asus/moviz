package com.moviz.lib.hw;

import android.content.Context;

import com.moviz.lib.hw.GenericDevice;

import java.util.List;

/**
 * Created by Matteo on 22/10/2016.
 */

public interface DeviceSearcher {
    public static final String DEVICE_SEARCH_END = "DeviceSearchear.DEVICE_SEARCH_END";
    public static final String DEVICE_SEARCH_ERROR = "DeviceSearchear.DEVICE_SEARCH_ERROR";
    public static final String DEVICE_REBIND_OK = "DeviceSearchear.DEVICE_REBIND_OK";
    public static final String DEVICE_REBIND_ERROR = "DeviceSearchear.DEVICE_REBIND_ERROR";
    public static final String DEVICE_FOUND = "DeviceSearchear.DEVICE_FOUND";
    public static final String DEVICE_ERROR_CODE = "DeviceSearchear.DEVICE_ERROR_CODE";
    public static final String DEVICE_ERROR_IDX = "DeviceSearchear.DEVICE_ERROR_IDX";
    public static final String TAG = "DeviceSearcher";

    void startSearch(Context ct);

    void stopSearch();

    int needsRebind(GenericDevice d);

    void startRebind(Context ct, List<GenericDevice> d);

    void stopRebind();
}
