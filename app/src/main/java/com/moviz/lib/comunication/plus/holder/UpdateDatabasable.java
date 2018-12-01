package com.moviz.lib.comunication.plus.holder;

import com.moviz.lib.googlefit.GoogleFitPoint;

public interface UpdateDatabasable extends Joinable, GoogleFitPoint {

    public PHolderSetter getSessionAggregateVars();

    public long getSessionId();

    public void setSessionId(long id);
}
