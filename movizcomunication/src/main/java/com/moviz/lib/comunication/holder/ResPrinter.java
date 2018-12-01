package com.moviz.lib.comunication.holder;

public class ResPrinter implements HolderPrinter {

    public ResPrinter() {

    }

    @Override
    public String printVal(com.moviz.lib.comunication.holder.Holder h) {
        return String.format(h.getFmtString(), h.getLong(), h.getDouble(), h.getString(), h.getObject(), h.getList());
    }

    @Override
    public String printVal(com.moviz.lib.comunication.holder.Holder h, Object form) {
        return String.format((String) form, h.getLong(), h.getDouble(), h.getObject(), h.getList());
    }

}
