package com.moviz.lib.comunication.holder;

public class FloatPrinter implements HolderPrinter {
    protected String format = "%.2f";

    public FloatPrinter(String form) {
        format = form;
    }

    public FloatPrinter() {

    }

    @Override
    public String printVal(com.moviz.lib.comunication.holder.Holder h) {
        return String.format(format, h.getDouble());
    }

    @Override
    public String printVal(com.moviz.lib.comunication.holder.Holder h, Object form) {
        return String.format((String) form, h.getDouble());
    }

}