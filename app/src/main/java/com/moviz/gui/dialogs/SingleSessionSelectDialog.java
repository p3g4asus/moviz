package com.moviz.gui.dialogs;

import android.content.DialogInterface;
import android.util.LongSparseArray;
import android.util.Pair;
import android.widget.ListView;

import com.moviz.lib.comunication.plus.holder.PSessionHolder;

import java.util.Date;
import java.util.List;

public abstract class SingleSessionSelectDialog extends SessionSelectDialog {

    public SingleSessionSelectDialog(Date ifd, Date itd, String datef) {
        super(ifd, itd, datef);
    }

    public long getInitMainSessionId() {
        return selectionInit.isEmpty() ? -1 : selectionInit.get(0);
    }

    public void setInitMainSessionId(long initMainSessionId) {
        selectionInit.clear();
        if (initMainSessionId >= 0)
            selectionInit.add(initMainSessionId);
    }

    @Override
    protected int getChoicheMode() {
        return ListView.CHOICE_MODE_SINGLE;
    }

    @Override
    protected void onCheckBoxClick(DialogInterface dialog,
                                   LongSparseArray<List<PSessionHolder>> sessions, Pair<Long, List<PSessionHolder>> item, boolean checked) {
        if (item != null)
            setInitMainSessionId(item.first);
        else
            setInitMainSessionId(-1);
        onSessionSelect(sessions, selectionInit);
        dialog.dismiss();
    }

}
