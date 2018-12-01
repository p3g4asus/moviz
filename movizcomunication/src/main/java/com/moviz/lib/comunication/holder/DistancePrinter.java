package com.moviz.lib.comunication.holder;

public class DistancePrinter extends FloatPrinter {
    public DistancePrinter(String form) {
        super(form);
    }

    public DistancePrinter() {
        format = "%.2f Km";
    }
}
