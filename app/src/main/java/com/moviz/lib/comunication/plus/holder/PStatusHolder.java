package com.moviz.lib.comunication.plus.holder;

import android.os.Parcel;
import android.os.Parcelable;

import com.moviz.lib.comunication.DeviceStatus;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.holder.StatusHolder;

public class PStatusHolder extends StatusHolder implements Parcelable {

    public PStatusHolder() {
        sessionClass = PSessionHolder.class;
        holderClass = PHolderSetter.class;
    }

    public PStatusHolder(StatusHolder s) {
        super(s);
        sessionClass = PSessionHolder.class;
        holderClass = PHolderSetter.class;
    }

	/*public void fromBundle(Bundle s) {
		if (s != null) {
			lastAction = s.getString("lastAction");
			lastStatus = (DeviceStatus) s
					.getSerializable("lastStatus");
			tcpAddress = s.getString("tcpaddress");
			tcpStatus = (TCPStatus) s
					.getSerializable("tcpstatus");
			updateN = s.getInt("updates");
			session = s.getParcelable("session");
			int n = s.getByte("nholders");
			for (int i = 0; i<n; i++) {
				holders.set((Holder) s.getParcelable("h"+i));
			}
		}
	}
	
	public Bundle toBundle(Bundle s) {
		s.putString("lastAction", lastAction);
		s.putSerializable("lastStatus", lastStatus);
		s.putString("tcpaddress", tcpAddress);
		s.putSerializable("tcpstatus", tcpStatus);
		s.putInt("updates", updateN);
		PSessionHolder u;
		if (session instanceof PSessionHolder)
			u = (PSessionHolder) session;
		else
			session = u = new PSessionHolder(session);
		s.putParcelable("session", u);
		s.putByte("nholders",(byte) holders.size());
		int i = 0;
		for (Holder h:holders) {
			if (!(h instanceof PHolder))
				s.putParcelable("h"+i, new PHolder(h));
			else
				s.putParcelable("h"+i, (PHolder)h);
			i++;
		}
		return s;
	}*/

    public PStatusHolder(Parcel w) {
        lastAction = w.readString();
        lastStatus = (DeviceStatus) w.readSerializable();
        tcpStatus = (com.moviz.lib.comunication.tcp.TCPStatus) w.readSerializable();
        tcpAddress = w.readString();
        updateN = w.readInt();
        session = w.readParcelable(PSessionHolder.class.getClassLoader());
        sessionClass = PSessionHolder.class;
        holderClass = PHolderSetter.class;
        if (holders == null)
            holders = newHolder();
        int n = w.readByte();
        for (int i = 0; i < n; i++) {
            holders.set((Holder) w.readParcelable(PHolder.class.getClassLoader()));
        }
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(lastAction);
        dest.writeSerializable(lastStatus);
        dest.writeSerializable(tcpStatus);
        dest.writeString(tcpAddress);
        dest.writeInt(updateN);
        dest.writeParcelable((Parcelable) session, flags);
        if (holders == null)
            holders = newHolder();
        dest.writeByte((byte) holders.size());
        for (Holder h : holders) {
            if (!(h instanceof PHolder))
                dest.writeParcelable(new PHolder(h), flags);
            else
                dest.writeParcelable((PHolder) h, flags);
        }
    }

    public static final Creator<PStatusHolder> CREATOR = new Creator<PStatusHolder>() {
        @Override
        public PStatusHolder createFromParcel(Parcel parcel) {
            return new PStatusHolder(parcel);
        }

        @Override
        public PStatusHolder[] newArray(int i) {
            return new PStatusHolder[i];
        }
    };

}
