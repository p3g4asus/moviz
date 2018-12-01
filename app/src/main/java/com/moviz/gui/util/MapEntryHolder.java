package com.moviz.gui.util;

import android.os.Parcel;
import android.os.Parcelable;

import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;
import com.moviz.lib.plot.MinimalXYSeries;

public class MapEntryHolder implements Parcelable {
    private MinimalXYSeries xy;
    private PSessionHolder session;
    private int position = -1;
    private PHolder holder = null;

    public MinimalXYSeries getXY() {
        return xy;
    }

    public void setXY(MinimalXYSeries xy2) {
        xy = xy2;
    }

    public PSessionHolder getSession() {
        return session;
    }

    public int getPosition() {
        return position;
    }

    public PHolder getHolder() {
        return holder;
    }

    public MapEntryHolder(PSessionHolder sesh, MinimalXYSeries xyp, PHolder hld, int pos) {
        session = sesh;
        xy = xyp;
        position = pos;
        holder = hld;
    }

    public MapEntryHolder(Parcel parcel) {
        session = parcel.readParcelable(PSessionHolder.class.getClassLoader());
        holder = parcel.readParcelable(PHolder.class.getClassLoader());
        position = parcel.readInt();
    }

    @Override
    public String toString() {
        return session.getDevice().getAlias() + " " + xy.getTitle();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof MapEntryHolder))
            return false;
        else {
            MapEntryHolder meo = (MapEntryHolder) o;
            return position == meo.position;
            /*return ((meo.xy==null && xy==null) ||
					(xy!=null && meo.xy!=null && xy.equals(meo.xy))) && 
					((meo.session==null && session==null) || 
							(session!=null && meo.session!=null && session.equals(meo.session)));*/
        }

    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(session, flags);
        dest.writeParcelable(holder, flags);
        dest.writeInt(position);
    }

    public static final Creator<MapEntryHolder> CREATOR = new Creator<MapEntryHolder>() {
        @Override
        public MapEntryHolder createFromParcel(Parcel parcel) {
            return new MapEntryHolder(parcel);
        }

        @Override
        public MapEntryHolder[] newArray(int i) {
            return new MapEntryHolder[i];
        }
    };
}
