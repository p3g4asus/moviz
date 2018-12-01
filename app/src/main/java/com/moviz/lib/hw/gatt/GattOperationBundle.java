package com.moviz.lib.hw.gatt;

import com.moviz.lib.hw.gatt.operations.GattOperation;

import java.util.ArrayList;

public class GattOperationBundle {
    final ArrayList<GattOperation> operations;

    public GattOperationBundle() {
        operations = new ArrayList<>();
    }

    public void addOperation(GattOperation operation) {
        operations.add(operation);
        operation.setBundle(this);
    }

    public ArrayList<GattOperation> getOperations() {
        return operations;
    }
}
