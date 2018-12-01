package com.moviz.gui.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.LongSparseArray;

import com.moviz.gui.R;
import com.moviz.gui.dialogs.MultipleSessionSelectDialog;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;
import com.moviz.lib.db.MySQLiteHelper;
import com.moviz.lib.sessionexport.SessionExporter;
import com.moviz.lib.utils.DeviceTypeMaps;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SessionExporterAction extends MultipleSessionSelectDialog {
    private SessionExportTask task = null;
    private String patho;

    public SessionExporterAction(Date ifd, Date itd, String datef, String outpath) {
        super(ifd, itd, datef);
        patho = outpath;
    }

    private class SessionExportTask extends AsyncTask<LongSparseArray<List<PSessionHolder>>, SessionLoadMemProgress, LongSparseArray<Boolean>> {
        private LongSparseArray<List<PSessionHolder>> sessionsall = null;

        public SessionExportTask() {
        }

        @Override
        protected void onPreExecute() {
            setupProgressDialog();
            progress.setCanceledOnTouchOutside(false);
            progress.setCancelable(true);
            progress.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (task != null) {
                        setupProgressDialog();
                        progress.setCancelable(false);
                        progress.setCanceledOnTouchOutside(false);
                        progress.setIndeterminate(true);
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progress.setMessage(res.getString(R.string.sessionexportdlg_cancelling));
                        progress.show();
                        task.cancel(false);
                    }

                }
            });
            progress.setMessage(res.getString(R.string.sessionexportdlg_exporting));
            progress.setIndeterminate(false);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.show();
        }

        @Override
        protected void onCancelled(LongSparseArray<Boolean> completed) {
            task = null;
            progress.dismiss();
            onPostExecute(completed);
        }

        @Override
        protected LongSparseArray<Boolean> doInBackground(
                LongSparseArray<List<PSessionHolder>>... params) {
            sessionsall = params[0];
            int ssize = sessionsall.size();

            LongSparseArray<Boolean> items = new LongSparseArray<Boolean>();
            List<PSessionHolder> ls;
            long key;
            progress.setMax(ssize);
            MySQLiteHelper sqlite = MySQLiteHelper.newInstance(null, null);
            SessionExporter exporter;
            long startfirst = -1;
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmSS");
            String pth;
            if (sqlite != null) {
                for (int i = 0; i < ssize && !task.isCancelled(); i++) {
                    key = sessionsall.keyAt(i);
                    // get the object by the key.
                    ls = sessionsall.get(key);
                    pth = patho + "/" + String.format("%05d_%s", key, sdf.format(new Date(ls.get(0).getDateStart())));
                    File f = new File(pth);
                    f.mkdirs();
                    startfirst = -1;
                    try {
                        for (PSessionHolder s : ls) {
                            if (startfirst < 0)
                                startfirst = s.getDateStart();
                            sqlite.loadSessionValues(s, null);
                            exporter = DeviceTypeMaps.type2sessionexporter.get(s.getDevice().getType());
                            exporter.export(s, s.getDateStart() - startfirst, pth, null);
                        }
                        items.put(key, true);
                    } catch (IOException ioe) {
                        items.put(key, false);
                    }
                    if (!task.isCancelled())
                        publishProgress(new SessionLoadMemProgress(i + 1, key, ls));
                    else
                        break;
                }
            }
            return items;
        }

        @Override
        protected void onProgressUpdate(SessionLoadMemProgress... s) {
            SessionLoadMemProgress r = s[0];
            progress.setMessage(printSessions(r.sessions));
            progress.setProgress(r.cur);
        }

        @Override
        protected void onPostExecute(LongSparseArray<Boolean> items) {
            progress.dismiss();
            openResultDialog(sessionsall, items);
        }


    }

    public void openResultDialog(LongSparseArray<List<PSessionHolder>> sessionsall, LongSparseArray<Boolean> items) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.sessionexportdlg_title);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        // Your code when user clicked on OK
                        // You can write the code to save the
                        // selected item here
                        dialog.dismiss();
                    }
                });
        String message = "";
        List<PSessionHolder> ls;
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat + " HH:mm:SS");
        int ssize = sessionsall.size(), isize = items.size();
        long key;
        String ok = res.getString(android.R.string.ok);
        String ko = res.getString(R.string.sessionexportdlg_error);
        String np = res.getString(R.string.sessionexportdlg_notdone);
        String result;
        for (int i = 0; i < ssize; i++) {
            if (i < isize) {
                key = items.keyAt(i);
                result = items.valueAt(i) ? ok : ko;
            } else {
                key = sessionsall.keyAt(i);
                result = np;
            }
            ls = sessionsall.get(key);
            for (PSessionHolder s : ls) {
                message += "[" + s.getMainSessionId() + "] " + s.getId() + ") " + sdf.format(new Date(s.getDateStart())) + " " + result + "\n";
            }
        }
        builder.setMessage(message);

        AlertDialog dialog = builder.create();// AlertDialog dialog; create like this
        // outside onClick
        dialog.show();

    }

    @Override
    protected void onSessionSelect(
            LongSparseArray<List<PSessionHolder>> sessions, List<Long> keys) {
        if (keys != null && !keys.isEmpty()) {
            LongSparseArray<List<PSessionHolder>> s2 = new LongSparseArray<List<PSessionHolder>>();
            for (Long k : keys) {
                s2.put(k, sessions.get(k));
            }
            (task = new SessionExportTask()).execute(s2);
        }

    }

}
