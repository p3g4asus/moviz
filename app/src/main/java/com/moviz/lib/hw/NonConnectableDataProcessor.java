package com.moviz.lib.hw;

import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

public abstract class NonConnectableDataProcessor extends DeviceDataProcessor {
    protected Runnable mTimeoutRunnable = null;

    public long getScanTimeout() {
        return mScanTimeout;
    }

    public static int twoByteConcat(byte lower, byte higher)
    {
        return ((higher & 0xFF) << 8) | (lower & 0xFF);
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

    @Override
    public boolean onReadData(GenericDevice dev, PDeviceHolder devh, byte[] arr, int length) {
        super.onReadData(mDeviceHolder, mDeviceHolder.innerDevice(), arr, length);
        return parseData(dev, devh, arr, length);
    }

    protected abstract boolean parseData(GenericDevice dev, PDeviceHolder devh, byte[] arr, int length);


    public void setTimeoutRunnable(Runnable mTimeoutRunnable) {
        this.mTimeoutRunnable = mTimeoutRunnable;
    }

    public Runnable getTimeoutRunnable() {
        return mTimeoutRunnable;
    }

    @Override
    protected byte[] debugFileElementHeader(byte[] pld, int pldlen) {

        int now = (int) System.currentTimeMillis();
        byte[] arrHeader = new byte[] {(byte) 0xAA, (byte) ((now>>0)&0xFF), (byte) ((now>>8)&0xFF), (byte) ((now>>16)&0xFF), (byte) ((now>>24)&0xFF)};

        return arrHeader;
    }

    @Override
    protected byte[] debugFileElementFooter(byte[] header, byte[] pld, int pldlen) {
        return new byte[]{0x55};
    }
}
