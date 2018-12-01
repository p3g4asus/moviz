package com.moviz.gui.dialogs;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.LongSparseArray;

import com.moviz.gui.R;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;
import com.moviz.lib.db.MySQLiteHelper;
import com.moviz.lib.db.SessionLoadDBProgress;
import com.moviz.lib.plot.ProgressPub;
import com.moviz.lib.utils.DeviceTypeMaps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class SessionDateDialog extends DateDialog {
    protected String dateFormat;
    protected ProgressDialog progress;

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public static String printSessions(List<PSessionHolder> ls, Resources res, String dateFormat) {
        String ev = "";
        if (ls.size() > 1) {
            int j = 0;
            for (PSessionHolder u : ls) {
                if (j > 0)
                    ev += ";\n";
                j++;
                ev += "[" + u.getMainSessionId() + "] " + u.getId() + ") " + DeviceTypeMaps.setSessionResources(u, res).toString(dateFormat);
            }
        } else
            ev = DeviceTypeMaps.setSessionResources(ls.get(0), res).toString(dateFormat);
        return ev;
    }

    protected String printSessions(List<PSessionHolder> ls) {
        return printSessions(ls, res, dateFormat);
    }

    private class SessionLoadDBTask extends AsyncTask<Date, SessionLoadDBProgress, LongSparseArray<List<PSessionHolder>>> {
        private MySQLiteHelper sqlite;
        private SimpleDateFormat sdf = new SimpleDateFormat(dateFormat + " HH:mm:SS");

        public SessionLoadDBTask(MySQLiteHelper sql) {
            sqlite = sql;
        }

        @Override
        protected void onPreExecute() {
            setupProgressDialog();
            progress.setMessage(res.getString(R.string.sessiondlg_dbquery));
            progress.setIndeterminate(true);
            progress.show();
        }

        @Override
        protected LongSparseArray<List<PSessionHolder>> doInBackground(
                Date... params) {
            return sqlite.getAllSessions(params[0], params[1], progressPrinter);
        }

        @Override
        protected void onProgressUpdate(SessionLoadDBProgress... s) {
            if (progress.isIndeterminate()) {
                progress.dismiss();
                setupProgressDialog();
                progress.setMessage("c");
                progress.setIndeterminate(false);
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.show();
            }
            SessionLoadDBProgress r = s[0];
            progress.setMax(r.tot);
            progress.setMessage(r.session.getDevice().getAlias() + " " + sdf.format(new Date(r.session.getDateStart())));
            progress.setProgress(r.cur);
        }

        @Override
        protected void onPostExecute(LongSparseArray<List<PSessionHolder>> res) {
            progress.dismiss();
            onLoadFinish(res);
        }

        private ProgressPub<SessionLoadDBProgress> progressPrinter = new ProgressPub<SessionLoadDBProgress>() {

            @Override
            public void publishProgress(SessionLoadDBProgress s, int cur,
                                        int tot) {
                s.cur = cur;
                s.tot = tot;
                SessionLoadDBTask.this.publishProgress(s);
            }
        };

    }

    protected void setupProgressDialog() {
        progress = new ProgressDialog(activity);
        progress.setCancelable(false);
    }

    public SessionDateDialog(Date ifd, Date itd, String datef) {
        super(ifd, itd);
        dateFormat = datef;
        setOnClickListner(new DateDialogOnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, Date d1, Date d2) {

                MySQLiteHelper sqlite = MySQLiteHelper.newInstance(null, null);
                if (sqlite != null) {
                    new SessionLoadDBTask(sqlite).execute(d1, d2);
                } else {
                    onLoadFinish(new LongSparseArray<List<PSessionHolder>>());
                }
            }
        });
    }

    protected abstract void onLoadFinish(LongSparseArray<List<PSessionHolder>> res);
}
