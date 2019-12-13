package com.moviz.lib.hw;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;

import com.moviz.gui.app.CA;
import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.utils.ParcelableMessage;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import timber.log.Timber;

/**
 * Created by Matteo on 22/10/2016.
 */

public class BLDeviceSearcher implements DeviceSearcher {
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothStateReceiver bluetoothReceiver = new BluetoothStateReceiver();
    private Context ctx;
    private List<GenericDevice> rebindDevs = null;
    private boolean doExit = false;
    private boolean pairing = false;

    private class BluetoothStateReceiver extends BroadcastReceiver {
        private Vector<BluetoothDevice> searchDeviceListFound = new Vector<BluetoothDevice>();
        private int searchDevicePin = 0;
        private boolean searchDevicePairingReq = false;
        int currentDev = 0;
        private String[] deviceFound = null;

        private void pairDevice(BluetoothDevice device) {
            try {
                Method method = device.getClass().getMethod("createBond", (Class[]) null);
                boolean rv = (Boolean) method.invoke(device, (Object[]) null);
                if (!rv)
                    throw new Exception("Cannot Bind to " + device.getAddress());
            } catch (Exception e) {
                exitPair("exm_errs_pairingerror");
            }
        }

        private void exitPair(String error) {
            setupBluetoothIO(false, null);
            if (error != null) {
                Timber.tag(TAG).e(error);
                CA.lbm.sendBroadcast(new Intent(DEVICE_REBIND_ERROR).putExtra(DEVICE_ERROR_CODE, (Parcelable) new ParcelableMessage(error)).putExtra(DEVICE_ERROR_IDX, currentDev));
            } else
                CA.lbm.sendBroadcast(new Intent(DEVICE_REBIND_OK).putExtra(DEVICE_FOUND, deviceFound));
        }

        private boolean bindNextOrExit() {
            if (currentDev + 1 < searchDeviceListFound.size() && searchDevicePin < 3) {
                BluetoothDevice bd;
                while (true) {
                    if ((bd = searchDeviceListFound.get(++currentDev)) != null) {
                        pairDevice(bd);
                        return true;
                    }
                    if (currentDev==searchDeviceListFound.size()-1)
                        break;
                }

            }
            exitPair(null);
            return false;
        }

        private void confirmBluetoothPasskey(BluetoothDevice device) {
            try {
                Timber.tag(TAG).w("Try confirm");
                Method m = device.getClass().getMethod("setPairingConfirmation", boolean.class);
                boolean rv = (Boolean) m.invoke(device, true);
                if (!rv)
                    throw new Exception("Cannot Pair to " + device.getAddress());
            } catch (Exception e) {
                exitPair("exm_errs_pinerror");
            }
        }

        private void cancelPairing(BluetoothDevice device) {
            try {
                Timber.tag(TAG).w("Try cancel");
                Method m = device.getClass().getMethod("cancelPairingUserInput");
                m.invoke(device);
            } catch (Exception e) {
                exitPair("exm_errs_pairingerror");
            }
        }

        private void setBluetoothPairingPin(BluetoothDevice device, String pin) {
            byte[] pinBytes = pin.getBytes(Charset.forName("UTF-8"));
            try {
                Timber.tag(TAG).w("Try to set the PIN");
                Method m = device.getClass().getMethod("setPin", byte[].class);
                boolean rv = (Boolean) m.invoke(device, pinBytes);
                if (rv) {
                    Timber.tag(TAG).w("Success to add the PIN.");
                    rv = (Boolean) (device.getClass().getMethod(
                            "setPairingConfirmation", boolean.class)
                            .invoke(device, true));
                    if (rv)
                        Timber.tag(TAG).w("Success to setPairingConfirmation.");
                }
                if (!rv)
                    throw new Exception("Cannot Pair to " + device.getAddress());
            } catch (Exception e) {
                exitPair("exm_errs_pinerror");
            }
        }

        private Vector<BluetoothDevice> checkDeviceFound() {
            String addr;
            Vector<BluetoothDevice> tmpdevL = new Vector<BluetoothDevice>();
            int i = 0;
            BluetoothDevice badd;
            for (GenericDevice gdev : rebindDevs) {
                addr = gdev.getAddress();
                badd = null;
                for (BluetoothDevice bdev : searchDeviceListFound) {
                    if (addr.equals(bdev.getAddress())) {
                        deviceFound[i] = "F";
                        badd = bdev;
                        break;
                    }
                }
                tmpdevL.add(badd);
                i++;
            }
            return tmpdevL;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Timber.tag(TAG).w("Pairing " + action);
            if (doExit) {
                mBluetoothAdapter.cancelDiscovery();
                exitPair(null);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                deviceFound = new String[rebindDevs.size()];
                searchDeviceListFound.clear();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) || BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null)
                    searchDeviceListFound.add(device);
                Vector<BluetoothDevice> tmpdevL = checkDeviceFound();
                if (tmpdevL.size() == rebindDevs.size() || (device == null && tmpdevL.size() > 0)) {
                    if (device != null)
                        mBluetoothAdapter.cancelDiscovery();
                    setupBluetoothIO(false, null);
                    searchDeviceListFound = tmpdevL;
                    IntentFilter filt = new IntentFilter();
                    filt.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    filt.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
                    setupBluetoothIO(true, filt);
                    searchDevicePin = 0;
                    currentDev = -1;
                    searchDevicePairingReq = false;
                    bindNextOrExit();
                } else if (device == null)
                    exitPair(null);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    deviceFound[currentDev] = "B";
                    searchDevicePin = 0;
                    searchDevicePairingReq = false;
                    bindNextOrExit();
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDING) {
                    if (searchDevicePairingReq) {
                        searchDevicePin++;
                        searchDevicePairingReq = false;
                    } else
                        cancelPairing(searchDeviceListFound.get(currentDev));
                    bindNextOrExit();
                } else if (state == BluetoothDevice.ERROR) {
                    exitPair("exm_errs_stateerror");
                }
                Timber.tag(TAG).w(String.format("prevs=%d sta=%d", prevState, state));
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                searchDevicePairingReq = true;
                int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT,
                        BluetoothDevice.ERROR);
                if (type == BluetoothDevice.PAIRING_VARIANT_PIN) {
                    if (searchDevicePin == 0)
                        setBluetoothPairingPin(searchDeviceListFound.get(currentDev), "0000");
                    else if (searchDevicePin == 1)
                        setBluetoothPairingPin(searchDeviceListFound.get(currentDev), "1234");
                    else if (searchDevicePin == 2)
                        setBluetoothPairingPin(searchDeviceListFound.get(currentDev), "00000000");
                } else if (type == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION) {
                    confirmBluetoothPasskey(searchDeviceListFound.get(currentDev));
                }
            }
        }

    }

    public BLDeviceSearcher() {
    }

    private void setupBluetoothIO(boolean start, IntentFilter filt) {
        if (start) {
            ctx.registerReceiver(bluetoothReceiver, filt);
        } else {
            try {
                ctx.unregisterReceiver(bluetoothReceiver);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void startSearch(Context ct) {
        doExit = false;
        ctx = ct;
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            PDeviceHolder[] devs = new PDeviceHolder[pairedDevices.size()];
            int i = 0;
            for (BluetoothDevice bt : pairedDevices) {
                devs[i++] = new PDeviceHolder(-1, bt.getAddress(), bt.getName(), "", DeviceType.hrdevice, "", "", true);
            }
            CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_END).putExtra(DEVICE_FOUND, devs));
        } else {
            CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_ERROR).putExtra(DEVICE_ERROR_CODE, (Parcelable) new ParcelableMessage("exm_errs_adapter")));
        }
    }

    @Override
    public void stopSearch() {
        doExit = true;
    }

    @Override
    public int needsRebind(GenericDevice d) {
        String device = d.getAddress();
        int needs = -1;
        if (device != null && !device.isEmpty()) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                needs = 1;
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    if (bt.getAddress().equals(device)) {
                        return 0;
                    }
                }
            }

        }
        return needs;
    }

    @Override
    public void startRebind(Context ct, List<GenericDevice> d) {
        rebindDevs = d;
        ctx = ct;
        doExit = false;

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        setupBluetoothIO(true, filter);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.startDiscovery()) {
            setupBluetoothIO(false, null);
            CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_ERROR).putExtra(DEVICE_ERROR_CODE, (Parcelable) new ParcelableMessage("exm_errs_discovery")));
        } else
            pairing = true;
    }

    @Override
    public void stopRebind() {
        if (pairing)
            doExit = true;
        else
            bluetoothReceiver.exitPair(null);
    }
}
