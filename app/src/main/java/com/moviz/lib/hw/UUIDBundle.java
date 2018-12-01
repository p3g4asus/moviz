package com.moviz.lib.hw;

import java.util.UUID;

public class UUIDBundle {
    public final UUID mService;
    public final UUID mCharacteristic;

    public UUIDBundle(UUID service, UUID charact) {
        mService = service;
        mCharacteristic = charact;
    }
}
