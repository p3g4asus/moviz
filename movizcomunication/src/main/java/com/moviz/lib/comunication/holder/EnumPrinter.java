package com.moviz.lib.comunication.holder;

public class EnumPrinter implements HolderPrinter {

    protected Enum<?>[] reference;

    public EnumPrinter(Enum<?>[] values) {
        reference = values;
    }

    public EnumPrinter() {
    }

    protected static String pV(com.moviz.lib.comunication.holder.Holder h, Enum<?>[] reference) {
        int l = (int) h.getLong();
        if (reference == null || l < 0 || l >= reference.length)
            return "N/A";
        else
            return reference[l].name().toLowerCase();
    }

    @Override
    public String printVal(com.moviz.lib.comunication.holder.Holder h) {
        return pV(h, reference);
    }

    @Override
    public String printVal(com.moviz.lib.comunication.holder.Holder h, Object form) {
        Enum<?>[] v = (Enum<?>[]) form;
        return pV(h, v);
    }

}
