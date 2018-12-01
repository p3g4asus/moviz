package com.moviz.lib.hw;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.utils.ParcelableMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

public abstract class BluetoothChatDataProcessor<T extends Enum<T>> extends DeviceDataProcessor {
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    private byte[] remainParseA = new byte[0];
    private boolean isWaiting = false;
    private int waitTarget = -1;
    private boolean findTarget = false;

    public BluetoothChatDataProcessor() {
        initMessages();
    }

    protected T[] messageValues;
    protected byte[][] messages;

    protected int arrayIndexOf(byte[] A, byte b, int ret, int length) {
        for (int i = ret; i < length; i++)
            if (A[i] == b)
                return i;
        return -1;
    }

    protected int searchAt(byte[] pagliaio, byte[] ago, int startPagliaio, int lengthPagliaio) {
        int end;
        boolean reduced;
        if (ago.length > lengthPagliaio - startPagliaio) {
            end = lengthPagliaio;
            reduced = true;
        } else {
            end = ago.length + startPagliaio;
            reduced = false;
        }
        for (int i = startPagliaio; i < end; i++) {
            if (pagliaio[i] != ago[i - startPagliaio]) {
                return 0;
            }
        }
        return reduced ? -1 : 1;
    }

    protected byte[] concat(byte[] A, int aLen, byte[] B, int bLen) {
        byte[] C = new byte[aLen + bLen];
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);
        return C;
    }

    @Override
    public void onDeviceConnectionFailed(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceConnectionFailed(dev, devh);
        remainParseA = new byte[0];
    }

    @Override
    public void onDeviceDisconnected(GenericDevice dev, PDeviceHolder devh) {
        super.onDeviceDisconnected(dev, devh);
        remainParseA = new byte[0];
    }

    @Override
    public boolean onReadData(GenericDevice dev, PDeviceHolder devh, byte[] buf, int length) {
        boolean rv = super.onReadData(mDeviceHolder, mDeviceHolder.innerDevice(), buf, length);
        //Log.d("BluetoothChatService", "RX "+hexValues(buf,length));
        byte[] buffer;
        if (remainParseA.length == 0) {
            buffer = buf;
        } else {
            buffer = concat(remainParseA, remainParseA.length, buf, length);
            length = buffer.length;
        }
        boolean finished = false;
        int foundMsg = -1;
        int ret = 0;
        byte msgs = getMessageStart();
        while (!finished && ret < length) {
            ret = arrayIndexOf(buffer, msgs, ret, length);
            if (ret < 0) {
                finished = true;
                ret = length;
            } else if (ret == length - 1) {
                finished = true;
            } else {
                int l2;
                byte[] ago;
                foundMsg = -1;
                for (int i = 0; foundMsg < 0 && i < messages.length; i++) {
                    ago = messages[i];
                    foundMsg = i;
                    if ((l2 = searchAt(buffer, ago, ret + 1, length)) < 0)
                        finished = true;
                    else if (l2 > 0) {
                        l2 = processMessage(messageValues[i], buffer,
                                ret, length);
                        rv = true;
                        if (l2 > 0) {
                            ret += l2;
                            finished = ret == length;
                        } else if (l2 < 0)
                            finished = true;
                        else //if (l2==0)
                            foundMsg = -1;
                        //Log.d("BluetoothChatService","Parsed message "+Message.values()[foundMsg]);
                    } else
                        foundMsg = -1;
                }
                if (foundMsg < 0)
                    ret++;
                else if (isWaiting() && foundMsg == getWaitTarget())
                    setFindTarget(true);
            }
        }
        if (ret == length) {
            if (remainParseA.length > 0)
                remainParseA = new byte[0];
        } else {
            remainParseA = Arrays.copyOfRange(buffer, ret, length);
        }
        return rv;
    }

    public static String hexValues(byte[] buf, int length) {
        String rv = "";
        for (int i = 0; i < length; i++) {
            rv += String.format("%02X ", buf[i]);
        }
        return rv;
    }


    public boolean isWaiting() {
        return isWaiting;
    }

    public void setWaiting(boolean isWaiting) {
        this.isWaiting = isWaiting;
    }

    public int getWaitTarget() {
        return waitTarget;
    }

    public void setWaitTarget(int waitTarget) {
        this.waitTarget = waitTarget;
    }

    public boolean isFindTarget() {
        return findTarget;
    }

    public void setFindTarget(boolean findTarget) {
        this.findTarget = findTarget;
    }

    public boolean waitMsg(int target, long time) {
        long waitlimit = time;
        this.isWaiting = false;
        this.findTarget = false;
        this.waitTarget = target;
        this.isWaiting = true;

        long stime = System.currentTimeMillis();
        while (!this.findTarget) {
            long ctime = System.currentTimeMillis();
            if ((waitlimit != 0L) && (ctime - stime > waitlimit)) {
                this.isWaiting = false;
                this.findTarget = false;

                return false;
            }
        }
        this.isWaiting = false;
        this.findTarget = false;
        return true;
    }

    @Override
    public void onDataWrite(GenericDevice dev, PDeviceHolder devh,byte[] arr, int length) {
        super.onDataWrite(dev, devh, arr, length);
    }

    @Override
    public boolean onReadData(GenericDevice dev, PDeviceHolder devh, BluetoothGattCharacteristic bcc) {
        return super.onReadData(dev, devh, bcc);
    }

    public abstract byte getMessageStart();

    protected abstract int processMessage(T msg, byte[] buffer, int ret, int length);

    protected abstract void initMessages();

    public synchronized void startTh() {
        System.out.println(" ** start 104 **");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread == null) {
            this.mAcceptThread = new AcceptThread(this);
            this.mAcceptThread.start();
        }
        setBluetoothState(BluetoothState.LISTEN);
    }


    public synchronized void connectedTh(BluetoothSocket socket) {
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
        this.mConnectedThread = new ConnectedThread(this, socket);
        this.mConnectedThread.start();

        setBluetoothState(BluetoothState.CONNECTED);
    }

    public synchronized void stopTh() {
        boolean cancelled = false;
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
            cancelled = true;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
            cancelled = true;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
            cancelled = true;
        }
        setBluetoothState(cancelled ? BluetoothState.DISCONNECTING : BluetoothState.IDLE);
    }

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (this.mBluetoothState != BluetoothState.CONNECTED) {
                return;
            }
            r = this.mConnectedThread;
        }
        r.write(out);
    }

    public synchronized void connectTh() {
        System.out.println("126 mState = " + this.mBluetoothState);
        if (this.mBluetoothState == BluetoothState.CONNECTING) {
            if (this.mConnectThread != null) {
                this.mConnectThread.cancel();
                this.mConnectThread = null;
            }
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        this.mConnectThread = new ConnectThread(this);
        this.mConnectThread.start();
        setBluetoothState(BluetoothState.CONNECTING);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private final BluetoothChatDataProcessor<? extends Enum<?>> mDev;

        public AcceptThread(BluetoothChatDataProcessor<? extends Enum<?>> dev) {
            BluetoothServerSocket tmp = null;
            mDev = dev;
            try {
                Log.i("BT", "Creating listening socket");
                if (mAdapter != null) {
                    tmp = mAdapter
                            .listenUsingRfcommWithServiceRecord("TreadMill",
                                    BluetoothChatDataProcessor.MY_UUID);
                }
            } catch (IOException e) {
                Log.e("BluetoothChatService", "listen() failed", e);
            }
            this.mmServerSocket = tmp;
        }

        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket = null;

            System.out.println("mState 265 = "
                    + mDev.mBluetoothState);
            while (mDev.mBluetoothState != BluetoothState.CONNECTED) {
                try {
                    Log.i("BT", "Accepting now");
                    System.out.println(" mmServerSocket.accept() 272 ");
                    socket = this.mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e("BluetoothChatService", "accept() failed 272 ", e);
                    mDev.postDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
                    ;
                    break;
                } catch (NullPointerException e) {
                    Log.e("BluetoothChatService", "mmServerSocket null");
                    mDev.postDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
                    ;
                    break;
                }
                if (socket != null) {
                    BluetoothDevice dev = socket.getRemoteDevice();
                    Log.i("BT", "Connected with " + dev.getName() + " (" + dev.getAddress() + ")");
                    synchronized (mDev) {
                        if (mDev.mBluetoothState == BluetoothState.LISTEN || mDev.mBluetoothState == BluetoothState.CONNECTING) {
                            Log.i("BluetoothChatService", "Calling connected from acceptthread");
                            mDev.connectedTh(socket);
                        } else if (mDev.mBluetoothState == BluetoothState.IDLE || mDev.mBluetoothState == BluetoothState.CONNECTED) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e("BluetoothChatService",
                                        "Could not close unwanted socket", e);
                            }
                        }
                    }
                }
                mDev.postDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
                ;
            }
        }

        public void cancel() {
            try {
                this.mmServerSocket.close();
            } catch (IOException e) {
                Log.e("BluetoothChatService", "close() of server failed", e);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothChatDataProcessor<? extends Enum<?>> mDev;

        public ConnectThread(BluetoothChatDataProcessor<? extends Enum<?>> dev) {
            this.mDev = dev;
            BluetoothSocket tmp = null;
            try {
                Log.i("BT", "Creating connecting socket");
                tmp = mDev.mBluetoothDevice
                        .createInsecureRfcommSocketToServiceRecord(BluetoothChatDataProcessor.MY_UUID);
            } catch (IOException e) {
                Log.e("BluetoothChatService", "create() failed", e);
            }
            this.mmSocket = tmp;
            System.out.println("UUID = " + tmp);
        }

        public void run() {
            Log.i("BluetoothChatService", "BEGIN mConnectThread 334");
            setName("ConnectThread");

            mAdapter.cancelDiscovery();
            try {
                Log.i("BluetoothChatService", "BEGIN mConnectThread 406");
                this.mmSocket.connect();
                Log.i("BluetoothChatService", "BEGIN mConnectThread 408");
            } catch (IOException e) {
                Log.i("BluetoothChatService", "mConnectThread err " + e);
                e.printStackTrace();
                mDev.postDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
                try {
                    this.mmSocket.close();
                } catch (IOException e2) {
                    Log.e("BluetoothChatService",
                            "unable to close() socket during connection failure",
                            e2);
                }
                //BluetoothChatService.this.start();
                return;
            }
            synchronized (mDev) {
                mDev.mConnectThread = null;
            }
            Log.i("BluetoothChatService", "Calling connected from connectthread");
            mDev.connectedTh(this.mmSocket);
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
                Log.e("BluetoothChatService",
                        "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothChatDataProcessor<? extends Enum<?>> mDev;

        public ConnectedThread(BluetoothChatDataProcessor<? extends Enum<?>> dev, BluetoothSocket socket) {
            Log.d("BluetoothChatService", "create ConnectedThread");
            this.mmSocket = socket;
            this.mDev = dev;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("BluetoothChatService", "temp sockets not created", e);
            }
            this.mmInStream = tmpIn;

            this.mmOutStream = tmpOut;
        }

        public void run() {
            Log.i("BluetoothChatService", "BEGIN mConnectedThread 437 ");
            byte[] buffer = new byte[2048];
            int bytes = 7;
            try {
                for (; ; ) {
                    bytes = this.mmInStream.read(buffer);
                    if (mDev != null) {
                        mDev.postReadData(buffer, bytes);
                    }
                }
            } catch (IOException e) {
                Log.e("BluetoothChatService", "disconnected 451 ", e);
                mDev.postDeviceError(new ParcelableMessage("exm_errr_connectionlost"));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("BluetoothChatService", "error ", e);
            }
        }

        public void write(byte[] buffer) {
            try {
                this.mmOutStream.write(buffer);
                if (mDev != null) {
                    mDev.onDataWrite(mDeviceHolder, mDeviceHolder.innerDevice(), buffer, buffer.length);
                }
            } catch (IOException e) {
                Log.e("BluetoothChatService", "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
                Log.e("BluetoothChatService",
                        "close() of connect socket failed", e);
            }
        }
    }

    @Override
    public BaseMessage processCommand(BaseMessage hs) {
        return null;
    }
}