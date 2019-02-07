package com.moviz.lib.hw;

import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

public abstract class NonConnectableDataProcessor extends DeviceDataProcessor {
    protected Runnable mTimeoutRunnable = null;

    public long getScanTimeout() {
        return mScanTimeout;
    }

    public static int twoByteConcat(int lower, int higher)
    {
        return (higher << 8) | lower;
    }

    public void setScanTimeout(long mScanTimeout,int maxtimeouts) {
        this.mScanTimeout = mScanTimeout;
    }

    protected long mScanTimeout = 300;

    public long getScanBetween() {
        return mScanBetween;
    }

    public void setScanBetween(long mScanBetween) {
        this.mScanBetween = mScanBetween;
    }

    protected long mScanBetween = 500;
    protected int mMaxTimeouts = 10;

    protected int nErrors = 0;

    public NonConnectableDataProcessor() {
        super();
    }

    public NonConnectableDataProcessor(long tim,long bte) {
        this();
        mScanTimeout = tim;
        mScanBetween = bte;
    }
    @Override
    public BaseMessage processCommand(BaseMessage hs2) {
        return null;
    }

    public boolean timeout() {
        nErrors++;
        if (nErrors>=mMaxTimeouts)
            return true;
        else
            return false;
    }

    @Override
    public boolean onReadData(GenericDevice dev, PDeviceHolder devh, byte[] arr, int length) {
        nErrors = 0;
        return parseData(dev, devh, arr, length);
    }

    protected abstract boolean parseData(GenericDevice dev, PDeviceHolder devh, byte[] arr, int length);


    public void setTimeoutRunnable(Runnable mTimeoutRunnable) {
        this.mTimeoutRunnable = mTimeoutRunnable;
    }

    public Runnable getTimeoutRunnable() {
        return mTimeoutRunnable;
    }
}
