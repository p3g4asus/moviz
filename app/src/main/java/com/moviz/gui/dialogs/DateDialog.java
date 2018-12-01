package com.moviz.gui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

import com.moviz.gui.R;

import java.util.Calendar;
import java.util.Date;

public class DateDialog {
    protected DateDialogOnClickListener mOnClicListner;
    protected DatePicker fromDP, toDP;
    protected AlertDialog intDialog;
    protected Resources res;
    protected Activity activity;
    protected Date initFrom = null, initTo = null;

    public interface DateDialogOnClickListener {
        public void onClick(DialogInterface dialog, Date d1, Date d2);
    }

    public void setOnClickListner(DateDialogOnClickListener l) {
        mOnClicListner = l;
    }

    public DateDialog(Date ifd, Date itd) {
        initFrom = ifd;
        initTo = itd;
    }

    private Calendar dateP2Calendar(DatePicker fromDP, int hr, int min, int sec) {
        Calendar c = Calendar.getInstance();
        c.set(fromDP.getYear(), fromDP.getMonth(), fromDP.getDayOfMonth(), hr, min, sec);
        return c;
    }

    private OnDateChangedListener dateChanged = new OnDateChangedListener() {

        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
            Calendar c1 = dateP2Calendar(fromDP, 0, 0, 1);
            Calendar c2 = dateP2Calendar(toDP, 23, 59, 59);
            Button b = intDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            b.setEnabled(c1.before(c2));
        }
    };

    public void show(Activity a) {
        activity = a;
        res = a.getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        // Get the layout inflater
        LayoutInflater inflater = a.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog
        // layout
        final View layout = inflater.inflate(R.layout.datealert, null);
        builder.setView(layout);
        fromDP = (DatePicker) layout
                .findViewById(R.id.fromDP);
        toDP = (DatePicker) layout
                .findViewById(R.id.toDP);
        final Calendar c = Calendar.getInstance();
        if (initTo != null)
            c.setTime(initTo);
        toDP.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), dateChanged);
        if (initFrom != null)
            c.setTime(initFrom);
        else
            c.add(Calendar.MONTH, -1);
        fromDP.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), dateChanged);
        intDialog = builder.create();
        intDialog.setTitle(res.getString(R.string.da_title));
        intDialog.setButton(AlertDialog.BUTTON_NEGATIVE, res.getString(R.string.da_nothing),// sett
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        intDialog.dismiss();
                        initFrom = initTo = null;
                    }
                });
        intDialog.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.da_nolimit),// sett
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        intDialog.dismiss();
                        initFrom = initTo = null;
                        if (mOnClicListner != null)
                            mOnClicListner.onClick(dialog, initFrom, initTo);
                    }
                });
        intDialog.setButton(AlertDialog.BUTTON_POSITIVE, res.getString(R.string.da_limit),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        intDialog.dismiss();
                        initFrom = dateP2Calendar(fromDP, 0, 0, 1).getTime();
                        initTo = dateP2Calendar(toDP, 23, 59, 59).getTime();
                        if (mOnClicListner != null)
                            mOnClicListner.onClick(dialog, initFrom, initTo);
                    }
                });

        intDialog.show();
    }


}
