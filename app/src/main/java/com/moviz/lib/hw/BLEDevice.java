package com.moviz.lib.hw;

import android.content.SharedPreferences;

import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.message.DeviceChangeRequestMessage;

import java.util.ArrayList;

public abstract class BLEDevice extends GenericDevice {

    @Override
    protected void prepareServiceConnection() {
        ((BLEBinder) mBluetoothService).setConnectionParams(this, getReadOnceCharacteristicUids(), getNotifyCharacteristicUids());
    }

    public long getFoundOnce() {
        return foundOnce;
    }

    public void setFoundOnce(long foundOnce) {
        this.foundOnce = foundOnce;
    }

    protected long foundOnce = 0;

    @Override
    protected Class<? extends BaseMessage>[] getAcceptedMessages() {
        return new Class[0];
    }
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read
    // or notification operations.

    protected abstract ArrayList<UUIDBundle> getNotifyCharacteristicUids();

    protected abstract ArrayList<UUIDBundle> getReadOnceCharacteristicUids();

    @Override
    public void loadTransientPref(SharedPreferences sharedPreferences) {
        foundOnce = Long.parseLong(sharedPreferences.getString(DeviceChangeRequestMessage.fullkeyFromKey("tmpfoundonce",device),"0"));
    }

}
