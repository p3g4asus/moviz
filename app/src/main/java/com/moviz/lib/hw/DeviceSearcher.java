package com.moviz.lib.hw;

import android.content.Context;

import com.moviz.lib.hw.GenericDevice;

import java.util.List;

/**
 * Created by Matteo on 22/10/2016.
 */

public interface DeviceSearcher {
    String DEVICE_SEARCH_END = "DeviceSearchear.DEVICE_SEARCH_END";
    String DEVICE_SEARCH_ERROR = "DeviceSearchear.DEVICE_SEARCH_ERROR";
    String DEVICE_REBIND_OK = "DeviceSearchear.DEVICE_REBIND_OK";
    String DEVICE_REBIND_ERROR = "DeviceSearchear.DEVICE_REBIND_ERROR";
    String DEVICE_FOUND = "DeviceSearchear.DEVICE_FOUND";
    String DEVICE_ERROR_CODE = "DeviceSearchear.DEVICE_ERROR_CODE";
    String DEVICE_ERROR_IDX = "DeviceSearchear.DEVICE_ERROR_IDX";
    String TAG = "DeviceSearcher";

    void startSearch(Context ct);

    void stopSearch();

    int needsRebind(GenericDevice d);

    void startRebind(Context ct, List<GenericDevice> d);

    void stopRebind();
}
