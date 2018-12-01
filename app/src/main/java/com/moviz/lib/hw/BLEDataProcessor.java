package com.moviz.lib.hw;

import com.movisens.smartgattlib.Characteristic;
import com.moviz.lib.comunication.message.BaseMessage;

import java.util.ArrayList;

public abstract class BLEDataProcessor extends DeviceDataProcessor {

    protected ArrayList<UUIDBundle> readOnceChar;
    protected ArrayList<UUIDBundle> notifyChar;
    protected static final String INFOMAP_MANUFACTURERDATA = Characteristic.MANUFACTURER_NAME_STRING.toString();
    protected static final String INFOMAP_MODELDATA = Characteristic.MODEL_NUMBER_STRING.toString();
    protected static final String INFOMAP_SERIALNUMBERDATA = Characteristic.SERIAL_NUMBER_STRING.toString();
    protected static final String INFOMAP_FIRMWAREREVDATA = Characteristic.FIRMWARE_REVISION_STRING.toString();
    protected static final String INFOMAP_HARDWAREREVDATA = Characteristic.HARDWARE_REVISION_STRING.toString();
    protected static final String INFOMAP_SOFTWAREREVDATA = Characteristic.SOFTWARE_REVISION_STRING.toString();

    public ArrayList<UUIDBundle> getReadOnceChar() {
        return readOnceChar;
    }

    public ArrayList<UUIDBundle> getNotifyChar() {
        return notifyChar;
    }

    public void setCharacteristic(ArrayList<UUIDBundle> readO, ArrayList<UUIDBundle> notify) {
        readOnceChar = readO;
        notifyChar = notify;
    }

    @Override
    public BaseMessage processCommand(BaseMessage hs) {
        return null;
    }

}
