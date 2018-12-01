package com.moviz.lib.comunication.holder;

import java.io.File;

public class FileNoExtPrinter implements HolderPrinter {

    private static String removeExt(String name) {
        int idx = name.lastIndexOf(".");
        if (idx > 0)
            name = name.substring(0, idx);
        return name;
    }

    @Override
    public String printVal(Holder h) {

        return removeExt(new File(h.getString()).getName());
    }

    @Override
    public String printVal(Holder h, Object form) {
        return printVal(h);
    }

}
