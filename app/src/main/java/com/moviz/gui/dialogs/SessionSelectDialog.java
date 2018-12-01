package com.moviz.gui.dialogs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.LongSparseArray;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

import com.moviz.gui.R;
import com.moviz.gui.util.SessionLoadMemProgress;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class SessionSelectDialog extends SessionDateDialog {
    protected ArrayList<Long> selectionInit = new ArrayList<Long>();
    protected AlertDialog mDialog = null;

    public ArrayList<Long> getSelectionInit() {
        return selectionInit;
    }

    public void setSelectionInit(ArrayList<Long> selectionInit) {
        if (selectionInit == null)
            this.selectionInit.clear();
        else
            this.selectionInit = selectionInit;
    }

    protected void onLoadFinish(LongSparseArray<List<PSessionHolder>> res) {
        if (res.size() == 0)
            openDialog(res, new CharSequence[0]);
        else
            new SessionLoadMemTask().execute(res);
    }

    private class SessionLoadMemTask extends AsyncTask<LongSparseArray<List<PSessionHolder>>, SessionLoadMemProgress, CharSequence[]> {
        private LongSparseArray<List<PSessionHolder>> sessionsall = null;

        public SessionLoadMemTask() {
        }

        @Override
        protected void onPreExecute() {
            setupProgressDialog();
            progress.setMessage("c");
            progress.setIndeterminate(false);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.show();
        }

        @Override
        protected CharSequence[] doInBackground(
                LongSparseArray<List<PSessionHolder>>... params) {
            sessionsall = params[0];
            int ssize = sessionsall.size();

            CharSequence[] items = new CharSequence[ssize];
            List<PSessionHolder> ls;
            long key;
            progress.setMax(ssize);
            for (int i = 0; i < ssize; i++) {
                key = sessionsall.keyAt(i);
                // get the object by the key.
                ls = sessionsall.get(key);
                items[i] = printSessions(ls);
                publishProgress(new SessionLoadMemProgress(i, key, ls));
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
        protected void onPostExecute(CharSequence[] items) {
            progress.dismiss();
            openDialog(sessionsall, items);
        }


    }

    public class CheckBoxClick {
        private LongSparseArray<List<PSessionHolder>> sessionsall;

        public CheckBoxClick(final LongSparseArray<List<PSessionHolder>> sessions) {
            sessionsall = sessions;
        }

        public void onClick(Pair<Long, List<PSessionHolder>> item, boolean checked) {
            onCheckBoxClick(mDialog, sessionsall, item, checked);
        }
    }

    private void openDialog(final LongSparseArray<List<PSessionHolder>> sessions, final CharSequence[] items) {
        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(res.getString(R.string.sessiondlg_title));
        SessionArrayAdapter choiceArrayAdapter = SessionArrayAdapter.newInstance(activity, sessions, items, selectionInit, new CheckBoxClick(sessions));
        builder.setNeutralButton(res.getString(R.string.sessiondlg_no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSessionSelect(sessions, null);
                    }
                });
        builder.setAdapter(choiceArrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        DialogInterface.OnClickListener posb = onPositiveClick(sessions);
        if (posb != null) {
            builder.setPositiveButton(res.getString(R.string.sessiondlg_select), posb);
        }
        //setDialogChoices(builder, sessions, items);
        mDialog = builder.create();
        mDialog.getListView().setChoiceMode(getChoicheMode());
        mDialog.show();
    }

    protected DialogInterface.OnClickListener onPositiveClick(final LongSparseArray<List<PSessionHolder>> sessions) {
        return null;
    }


    public SessionSelectDialog(Date ifd, Date itd, String datef) {
        super(ifd, itd, datef);
    }

    protected abstract int getChoicheMode();

    protected abstract void onSessionSelect(
            LongSparseArray<List<PSessionHolder>> sessions, List<Long> keys);

    protected abstract void onCheckBoxClick(DialogInterface dialog,
                                            LongSparseArray<List<PSessionHolder>> sessions, Pair<Long, List<PSessionHolder>> item, boolean checked);

    //protected abstract void setDialogChoices(AlertDialog.Builder builder,LongSparseArray<List<PSessionHolder>> sessions,CharSequence[] items);
}

class SessionArrayAdapter extends ArrayAdapter<Pair<Long, List<PSessionHolder>>> {
    private final Context context;
    private final Resources res;
    private final CharSequence[] sessionstext;
    private final List<Pair<Long, List<PSessionHolder>>> sessions;
    private final List<Long> selectionInit;
    private final SessionSelectDialog.CheckBoxClick mCheckBoxClick;

    public static SessionArrayAdapter newInstance(Context context, LongSparseArray<List<PSessionHolder>> sessionsall, CharSequence[] items, List<Long> sesInit, SessionSelectDialog.CheckBoxClick cbx) {
        List<Pair<Long, List<PSessionHolder>>> all = new ArrayList<Pair<Long, List<PSessionHolder>>>();
        long key;
        int ssize = sessionsall.size();
        List<PSessionHolder> ls;
        for (int i = 0; i < ssize; i++) {
            key = sessionsall.keyAt(i);
            // get the object by the key.
            ls = sessionsall.get(key);
            all.add(new Pair<Long, List<PSessionHolder>>(key, ls));
        }
        return new SessionArrayAdapter(context, all, items, sesInit, cbx);
    }

    public SessionArrayAdapter(Context ctx, List<Pair<Long, List<PSessionHolder>>> all, CharSequence[] items, List<Long> sesInit, SessionSelectDialog.CheckBoxClick cbx) {
        super(ctx, R.layout.sessionselect_dlg, all);
        context = ctx;
        sessions = all;
        selectionInit = sesInit;
        sessionstext = items;
        mCheckBoxClick = cbx;
        res = ctx.getResources();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.sessionselect_dlg, parent, false);
        CheckedTextView textView = (CheckedTextView) rowView.findViewById(R.id.textDialog);
        Pair<Long, List<PSessionHolder>> item = sessions.get(position);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
                mCheckBoxClick.onClick((Pair<Long, List<PSessionHolder>>) v.getTag(), ((CheckedTextView) v).isChecked());
            }
        });
        textView.setTag(item);
        LinearLayout outBox = (LinearLayout) rowView.findViewById(R.id.outBox);
        outBox.setBackgroundColor(res.getColor(((position % 2) == 0 ? R.color.colorHighlight : R.color.colorWhite)));

        textView.setText(sessionstext[position]);
        textView.setChecked(selectionInit.indexOf(item.first) >= 0);

        return rowView;
    }
}