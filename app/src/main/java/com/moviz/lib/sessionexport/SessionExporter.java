package com.moviz.lib.sessionexport;

import com.jmatio.types.MLArray;
import com.moviz.lib.plot.ProgressPub;

import java.io.IOException;
import java.util.ArrayList;

public interface SessionExporter {

    public ArrayList<MLArray> export(com.moviz.lib.comunication.holder.SessionHolder ses, double offsetms, String path, ProgressPub<Integer[]> pp) throws IOException;
}
