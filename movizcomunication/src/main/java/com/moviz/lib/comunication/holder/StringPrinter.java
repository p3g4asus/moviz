package com.moviz.lib.comunication.holder;

public class StringPrinter implements HolderPrinter {
    protected String format = "%s";

    public StringPrinter(String form) {
        format = form;
    }

    public StringPrinter() {

    }

    @Override
    public String printVal(Holder h) {
        return String.format(format, h.getString());
    }

    @Override
    public String printVal(Holder h, Object form) {
        return String.format((String) form, h.getString());
    }

}