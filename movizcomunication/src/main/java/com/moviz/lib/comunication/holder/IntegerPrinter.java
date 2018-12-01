package com.moviz.lib.comunication.holder;

public class IntegerPrinter implements HolderPrinter {
    protected String format = "%d";

    public IntegerPrinter(String form) {
        format = form;
    }

    public IntegerPrinter() {

    }

    @Override
    public String printVal(Holder h) {
        return String.format(format, h.getLong());
    }

    @Override
    public String printVal(Holder h, Object form) {
        return String.format((String) form, h.getLong());
    }

}
