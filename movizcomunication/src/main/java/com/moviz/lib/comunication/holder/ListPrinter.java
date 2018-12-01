package com.moviz.lib.comunication.holder;

import java.util.List;

public class ListPrinter implements HolderPrinter {
    public ListPrinter() {

    }

    @Override
    public String printVal(Holder h) {
        List<?> l = h.getList();
        if (l == null)
            return "null";
        else {
            String rv = "[";
            int i = 0;
            for (Object o : l) {
                if (i++ != 0)
                    rv += ",";
                if (o == null) {
                    rv += "null";
                } else {
                    rv += o;
                }
            }
            return rv;
        }
    }

    @Override
    public String printVal(Holder h, Object form) {
        return printVal(h);
    }

}