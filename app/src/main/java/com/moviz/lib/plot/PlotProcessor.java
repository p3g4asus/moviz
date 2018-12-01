package com.moviz.lib.plot;

import android.os.Parcel;
import android.os.Parcelable;

import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;

import java.util.Arrays;

public abstract class PlotProcessor implements Parcelable {
    protected PHolderSetter plotVars = new PHolderSetter();

    public PlotProcessor() {
    }

    protected static int calcDecimation(int p, int sessionPoints) {
        if (p > sessionPoints && sessionPoints > 0) {
            double dec = (double) p / (double) sessionPoints;
            return (int) (Math.ceil(dec) + 0.5);
        } else
            return 1;
    }

    public abstract PHolderSetter getPlotVars(com.moviz.lib.comunication.holder.SessionHolder ses, int maxPoints, long oggsetms, ProgressPub<Integer[]> pp);

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getClass().getName());
        dest.writeParcelableArray(plotVars.toArray(new PHolder[0]), flags);

    }

    public void readFromParcel(Parcel value) {
        PHolder[] arr = (PHolder[]) value.readParcelableArray(PHolder.class.getClassLoader());
        plotVars.addAll(Arrays.asList(arr));
    }

    public static final Creator<PlotProcessor> CREATOR = new Creator<PlotProcessor>() {
        @Override
        public PlotProcessor createFromParcel(Parcel parcel) {
            String cn = parcel.readString();
            PlotProcessor inst = null;
            try {
                inst = (PlotProcessor) Class.forName(cn).newInstance();
                inst.readFromParcel(parcel);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return inst;
        }

        @Override
        public PlotProcessor[] newArray(int i) {
            return new PlotProcessor[i];
        }
    };
}
