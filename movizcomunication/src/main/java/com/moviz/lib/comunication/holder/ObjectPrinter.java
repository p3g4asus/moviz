package com.moviz.lib.comunication.holder;

public class ObjectPrinter implements HolderPrinter {
    public ObjectPrinter() {

    }

    @Override
    public String printVal(com.moviz.lib.comunication.holder.Holder h) {
        return h.getObject().toString();
    }

    @Override
    public String printVal(com.moviz.lib.comunication.holder.Holder h, Object form) {
        return printVal(h);
    }

}