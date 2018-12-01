package com.moviz.gui.dialogs;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.LongSparseArray;
import android.util.Pair;
import android.widget.ListView;

import com.moviz.lib.comunication.plus.holder.PSessionHolder;

import java.util.Date;
import java.util.List;

public abstract class MultipleSessionSelectDialog extends SessionSelectDialog {

    public MultipleSessionSelectDialog(Date ifd, Date itd, String datef) {
        super(ifd, itd, datef);
    }

    @Override
    protected void onCheckBoxClick(DialogInterface dialog,
                                   LongSparseArray<List<PSessionHolder>> sessions, Pair<Long, List<PSessionHolder>> item, boolean checked) {
        if (checked) {
            if (!selectionInit.contains(item.first))
                selectionInit.add(item.first);
        } else {
            selectionInit.remove(item.first);
        }
    }

    @Override
    protected int getChoicheMode() {
        return ListView.CHOICE_MODE_MULTIPLE;
    }

    @Override
    protected DialogInterface.OnClickListener onPositiveClick(final LongSparseArray<List<PSessionHolder>> sessions) {
        return new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectionInit.isEmpty())
                    onSessionSelect(sessions, null);
                else
                    onSessionSelect(sessions, selectionInit);
                dialog.dismiss();
            }
        };
    }

}
