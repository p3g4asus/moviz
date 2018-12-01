package com.moviz.lib.comunication.holder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatePrinter implements HolderPrinter {
    protected String format = "dd/MM/yy HH:mm:ss";
    protected SimpleDateFormat sdf;

    public DatePrinter(String form) {
        format = form;
        sdf.applyPattern(form);
    }

    public DatePrinter() {
        sdf = new SimpleDateFormat(format);
    }

    @Override
    public String printVal(Holder h) {
        return sdf.format(new Date(h.getLong()));
    }

    @Override
    public String printVal(Holder h, Object form) {
        sdf.applyPattern((String) form);
        return sdf.format(new Date(h.getLong()));
    }

}
