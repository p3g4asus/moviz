package com.moviz.gui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.LongSparseArray;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.androidplot.Plot;
import com.androidplot.PlotListener;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.moviz.gui.R;
import com.moviz.gui.dialogs.SingleSessionSelectDialog;
import com.moviz.gui.util.MapEntryHolder;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;
import com.moviz.lib.db.MySQLiteHelper;
import com.moviz.lib.plot.MinimalXYSeries;
import com.moviz.lib.plot.PlotProcessor;
import com.moviz.lib.plot.ProgressPub;
import com.moviz.lib.utils.DeviceTypeMaps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlotFragment extends Fragment {
    private MySQLiteHelper sqlite = null;
    private Button sessionBTN;
    private Button varsBTN;
    private Button statsBTN;
    private XYPlot plot;
    private List<PSessionHolder> currentSession = null;
    private Map<PDeviceHolder, PlotProcessor> plotProcessors = new HashMap<PDeviceHolder, PlotProcessor>();
    /*private ArrayList<Double> minutes = new ArrayList<Double>();
    private ArrayList<Short> watt = new ArrayList<Short>();
    private ArrayList<Integer> rpm = new ArrayList<Integer>();
    private ArrayList<Integer> pulse = new ArrayList<Integer>();
    private ArrayList<Double> speed = new ArrayList<Double>();
    private ArrayList<Byte> incline = new ArrayList<Byte>();*/
    private List<Integer> colors = new ArrayList<Integer>();
    private Map<PSessionHolder, PHolderSetter> plotVars = new HashMap<PSessionHolder, PHolderSetter>();
    private Map<PSessionHolder, List<MinimalXYSeries>> xyVars = new HashMap<PSessionHolder, List<MinimalXYSeries>>();
    private List<MapEntryHolder> currentSelection = null;
    private List<MapEntryHolder> allPlottable = new ArrayList<MapEntryHolder>();
    private List<CharSequence> allPlottableChar = new ArrayList<CharSequence>();
    private int currentColor = 0;
    private ProgressDialog progress;
    private boolean inWork = false;
    //private long timeStartPlot = 0;
    private SharedPreferences sharedPref;
    private String dateFormat = "";
    private int sessionPoints = 0;
    private long currentMainSessionId = -1;
    private Resources res;
    private Object synch = new Object();

    private SingleSessionSelectDialog sessionDateDialog = new SingleSessionSelectDialog(null, null, dateFormat) {

        @Override
        protected void onSessionSelect(
                LongSparseArray<List<PSessionHolder>> sessions, List<Long> keys) {
            if (keys != null && keys.size() > 0)
                asynchWork(sessions.get(keys.get(0)), null);
        }
    };

    @Override
    public void onViewStateRestored(Bundle s) {
        super.onViewStateRestored(s);
        if (s != null) {
            Boolean b;
            if ((b = s.getBoolean("restoreSel")) != null && b)
                currentSelection = s.getParcelableArrayList("selection");
            if ((b = s.getBoolean("restoreSes")) != null && b) {
                currentSession = s.getParcelableArrayList("session");
                asynchWork(null, null);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle s) {
        super.onSaveInstanceState(s);
        s.putBoolean("restoreSes", currentSession != null);
        s.putBoolean("restoreSel", currentSelection != null);
        if (currentSession != null)
            s.putParcelableArrayList("session", (ArrayList<? extends Parcelable>) currentSession);
        if (currentSelection != null)
            s.putParcelableArrayList("selection", (ArrayList<? extends Parcelable>) currentSelection);
    }

    private PlotListener plotLST = new PlotListener() {

        @Override
        public void onBeforeDraw(Plot arg0, Canvas arg1) {

        }

        @Override
        public void onAfterDraw(Plot arg0, Canvas arg1) {

            //long now = System.currentTimeMillis();
            //Log.d("ciao", "t = "+(now-timeStartPlot)/1000.0);
            synchronized (synch) {
                if (inWork)
                    synch.notifyAll();
            }
        }
    };

    private OnSharedPreferenceChangeListener sharedPrefChange = new OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_datef")) {
                dateFormat = sharedPref.getString("pref_datef", "dd/MM/yy");
                sessionDateDialog.setDateFormat(dateFormat);
            } else if (key.equals("pref_sessionpoints"))
                sessionPoints = Integer.parseInt(sharedPref.getString("pref_sessionpoints", "0"));
        }

    };

    private void setupProgressDialog() {
        progress = new ProgressDialog(getActivity());
        progress.setCancelable(false);
    }

    private String getProgressMsg(List<PSessionHolder> list) {
        return String.format(getString(R.string.plt_graph), printSessions(list));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getResources();
        sqlite = MySQLiteHelper.newInstance(null, null);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPrefChange);
        dateFormat = sharedPref.getString("pref_datef", "dd/MM/yy");
        sessionDateDialog.setDateFormat(dateFormat);
        sessionPoints = Integer.parseInt(sharedPref.getString("pref_sessionpoints", "0"));
        colors.add(Color.BLUE);
        colors.add(Color.GREEN);
        colors.add(Color.RED);
        colors.add(Color.YELLOW);
        colors.add(Color.MAGENTA);
    }


    private class PreparePlotTask extends AsyncTask<Pair<List<PSessionHolder>, List<MapEntryHolder>>, Integer, Void> {

        private List<PSessionHolder> newsession = null;
        private String msg;
        private ProgressPub<Integer[]> progPrint = new ProgressPub<Integer[]>() {
            @Override
            public void publishProgress(Integer[] s, int cur, int tot) {
                PreparePlotTask.this.publishProgress(cur, tot);
            }

        };

        @Override
        protected void onPreExecute() {
            setupProgressDialog();
            progress.setMessage(getString(R.string.plt_graph_title));
            progress.setIndeterminate(true);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
        }

        @Override
        protected void onProgressUpdate(Integer... s) {
            if (progress.isIndeterminate()) {
                progress.dismiss();
                setupProgressDialog();
                progress.setMessage("c");
                progress.setIndeterminate(false);
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.show();
            }
            progress.setMax(s[1]);
            progress.setProgress(s[0]);
            progress.setMessage(msg);
        }

        @Override
        protected void onPostExecute(Void items) {
            progress.dismiss();
            varsBTN.setEnabled(currentSession != null);
            statsBTN.setEnabled(currentSession != null);
            synchronized (synch) {
                inWork = false;
            }
        }

        @Override
        protected Void doInBackground(
                Pair<List<PSessionHolder>, List<MapEntryHolder>>... params) {
            newsession = params[0].first;
            List<MapEntryHolder> lmeo = params[0].second;
            synchronized (synch) {
                inWork = true;
            }
            if (newsession == null && currentSession == null) {
            } else if (sqlite == null) {
            } else if (lmeo == null) {
                if (newsession != null) {
                    for (PSessionHolder s : newsession) {
                        msg = String.format(res.getString(R.string.plt_loading_db), s.getDevice().getAlias(), formatDate(s).toString());
                        sqlite.loadSessionValues(s, progPrint);
                    }
                    currentSelection = null;
                    allPlottable.clear();
                    allPlottableChar.clear();
                    plotVars.clear();
                    currentColor = 0;
                    currentSession = newsession;
                    currentMainSessionId = newsession.get(0).getMainSessionId();
                }
                long startfirst = -1;
                for (PSessionHolder s : newsession) {
                    if (startfirst < 0)
                        startfirst = s.getDateStart();
                    PlotProcessor plth = plotProcessors.get(s.getDevice());
                    if (plth != null) {
                        msg = String.format(res.getString(R.string.plt_loading_list), s.getDevice().getAlias(), formatDate(s).toString());
                        plotVars.put(s, plth.getPlotVars(s, sessionPoints, s.getDateStart() - startfirst, progPrint));
                    }
                }
                createListValues();
                refreshSessionDesc();
            }

            synchronized (synch) {
                if (refreshPlot(lmeo)) {
                    try {
                        synch.wait();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

    }

    ;

    private void asynchWork(final List<PSessionHolder> newsession, final List<MapEntryHolder> lmeo) {
        if (newsession != null) {
            com.moviz.lib.comunication.holder.DeviceHolder devh;
            for (PSessionHolder s : newsession) {
                PlotProcessor plth = plotProcessors.get(devh = s.getDevice());
                if (plth == null) {
                    try {
                        plth = DeviceTypeMaps.type2pltprocclass.get(devh.getType()).newInstance();
                        plotProcessors.put((PDeviceHolder) devh, plth);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        new PreparePlotTask().execute(new Pair(newsession, lmeo));
    }


    private String printSessions(List<PSessionHolder> ls) {
        String ev = "";
        if (ls.size() > 1) {
            int j = 0;
            for (PSessionHolder u : ls) {
                if (j > 0)
                    ev += ";\n";
                j++;
                ev += j + ") " + DeviceTypeMaps.setSessionResources(u, res).toString(dateFormat);
            }
        } else
            ev = DeviceTypeMaps.setSessionResources(ls.get(0), res).toString(dateFormat);
        return ev;
    }

    private void openDateDialog() {
        Activity a = getActivity();
        if (sqlite != null && !inWork && a != null) {
            sessionDateDialog.show(a);
        }
    }

	/*private void openSessionDialog(LongSparseArray<List<PSessionHolder>> sessl, CharSequence[] items) {
        sessions = sessl;
		AlertDialog levelDialog = null;
		// Creating and Building the Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.plt_session_dialog));
		builder.setNeutralButton(getString(R.string.plt_session_dialog_no),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		int idx = -1;
		if (currentSession != null)
			idx = sessions.indexOfKey(currentMainSessionId);
		builder.setSingleChoiceItems(items, idx,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						if (item >= 0 && item < sessions.size()) {
							List<PSessionHolder> ses = sessions.valueAt(item);
							long msid = ses.get(0).getMainSessionId();
							if (msid != currentMainSessionId) {
								varsBTN.setEnabled(true);
								varsBTN.setEnabled(true);
								statsBTN.setEnabled(true);
								plot.clear();
								plot.redraw();
								asynchWork(ses, null);
							}
						}
						dialog.dismiss();
					}
				});
		levelDialog = builder.create();
		levelDialog.show();
	}*/

    private void refreshSessionDesc() {
    }

    private CharSequence formatDate(PSessionHolder ses) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat + " HH:mm:ss");
        return sdf.format(ses.getDateStart());
    }
	
	/*private void createListValues2() {
		if (currentSession != null) {
			List<PafersHolder> vals = currentSession.getValues();
			
			for (int i = 0; i < TOT_IDX; i++) {
				try {
					xySeries[i] = new ReflectionXYSeries(titles[i],vals,"timeRms",names[i],1.0/60000.0,1.0);
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			for (int i = 0; i < TOT_IDX; i++) {
				xySeries[i] = null;
			}
		}
		
	}*/

    private void createListValues() {
        if (currentSession != null) {
            for (PSessionHolder s : currentSession) {
                List<MinimalXYSeries> xy = new ArrayList<MinimalXYSeries>();
                xyVars.put(s, xy);
                List<? extends Number> xaxis = null;
                PHolderSetter lv = plotVars.get(s);
                if (lv != null) {
                    for (com.moviz.lib.comunication.holder.Holder h : lv) {
                        List<? extends Number> num = (List<? extends Number>) h.getList();
                        if (h.isAbout("plot.x"))
                            xaxis = num;
                        else if (xaxis != null && num.size() > 0) {
                            MinimalXYSeries xys = new MinimalXYSeries(DeviceTypeMaps.setHolderResource(h, res), xaxis,
                                    (List<? extends Number>) num);
                            xy.add(xys);
                            MapEntryHolder entry;
                            allPlottable.add(entry = new MapEntryHolder(s, xys, (PHolder) h, allPlottable.size()));
                            allPlottableChar.add(entry.toString());
                            if (currentSelection != null) {
                                int idx = currentSelection.indexOf(entry);
                                if (idx >= 0) {
                                    currentSelection.get(idx).setXY(xys);
                                }
                            }
                        }
                    }
                }

            }
        } else {
            xyVars.clear();
        }
    }

    private boolean refreshPlot(List<MapEntryHolder> newsel) {
        if (newsel == null && currentSelection == null) {
            plot.clear();
            plot.redraw();
            return false;
        } else {
            int col, sz = colors.size();
            if ((newsel == null && currentSelection != null) || (newsel != null && currentSelection == null)) {
                plot.clear();
                List<MapEntryHolder> s = currentSelection == null ? newsel : currentSelection;
                for (MapEntryHolder meo : s) {
                    col = colors.get(currentColor % sz);
                    plot.addSeries(meo.getXY(), new LineAndPointFormatter(
                            col, col, Color.TRANSPARENT, null));
                    currentColor++;
                }
            } else {
                for (MapEntryHolder meo : newsel) {
                    if (currentSelection.indexOf(meo) < 0) {
                        col = colors.get(currentColor % sz);
                        plot.addSeries(meo.getXY(), new LineAndPointFormatter(
                                col, col, Color.TRANSPARENT, null));
                        currentColor++;
                    }
                }
                for (MapEntryHolder meo : currentSelection) {
                    if (newsel.indexOf(meo) < 0) {
                        plot.removeSeries(meo.getXY());
                    }
                }
            }
            currentSelection = newsel;
            plot.setTicksPerRangeLabel(3);
            plot.getGraphWidget().setDomainLabelOrientation(-45);
            plot.redraw();
            return true;
        }
    }

    private void openStatsDialog() {
        Activity act = getActivity();
        if (act != null && currentSession != null && !inWork) {
            String message = "", tmpstr;
            for (PSessionHolder s : currentSession) {
                com.moviz.lib.comunication.holder.DeviceHolder d = s.getDevice();
                message += res.getString(R.string.plt_device) + ": " + d.getAlias() + " [" + d.getName() + " / " + d.getAddress() + "]\n";
                message += res.getString(R.string.plt_device_desc) + ": " + d.getDescription() + "\n";
                message += res.getString(R.string.plt_date) + ": " + formatDate(s) + "\n";
                message += res.getString(R.string.plt_device_sett) + ": " + s.getSettings() + "\n";
                message += res.getString(R.string.plt_device_user) + ": " + s.getUser().getName() + "\n";
                PHolderSetter holders = s.getPHolders();
                for (com.moviz.lib.comunication.holder.Holder h : holders) {
                    tmpstr = h.getResString();
                    message += tmpstr.isEmpty() ? h.getId() : tmpstr + ": " + h.toString() + "\n";
                }
                message += "/--------------------/\n";
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(R.string.plt_stats_dialog);
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
            builder.setMessage(message);

            AlertDialog dialog = builder.create();// AlertDialog dialog; create like this
            // outside onClick
            dialog.show();
        }
    }

    private void openVarsDialog() {
        Activity act = getActivity();
        if (act != null && !inWork) {
            // arraylist to keep the selected items
            final ArrayList<MapEntryHolder> seletedItems = new ArrayList<MapEntryHolder>();
            if (currentSelection != null)
                seletedItems.addAll(currentSelection);
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(R.string.plt_vars_dialog);
            builder.setMultiChoiceItems(allPlottableChar.toArray(new CharSequence[0]), null,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        // indexSelected contains the index of item (of which
                        // checkbox checked)
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int indexSelected, boolean isChecked) {
                            MapEntryHolder meo = allPlottable.get(indexSelected);
                            if (isChecked) {
                                // If the user checked the item, add it to the
                                // selected items
                                // write your code when user checked the
                                // checkbox
                                seletedItems.add(meo);
                            } else if (seletedItems.contains(meo)) {
                                // Else, if the item is already in the array,
                                // remove it
                                // write your code when user Uchecked the
                                // checkbox
                                seletedItems.remove(meo);
                            }
                        }
                    })
                    // Set the action buttons
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // Your code when user clicked on OK
                                    // You can write the code to save the
                                    // selected item here
                                    asynchWork(null, seletedItems);
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // Your code when user clicked on Cancel
                                    dialog.dismiss();
                                }
                            });

            AlertDialog dialog = builder.create();// AlertDialog dialog; create like this
            // outside onClick
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    if (currentSelection != null) {
                        final AlertDialog alert = (AlertDialog) dialog;
                        final ListView list = alert.getListView();
                        for (MapEntryHolder meo : currentSelection) {
                            list.setItemChecked(meo.getPosition(), true);
                        }
                    }
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // get the url to open
        View v = getView();
        sessionBTN = (Button) v.findViewById(R.id.sessionBTN);
        varsBTN = (Button) v.findViewById(R.id.varsBTN);
        statsBTN = (Button) v.findViewById(R.id.statsBTN);
        plot = (XYPlot) v.findViewById(R.id.sessionPLT);
        //plot.setOnTouchListener(zoomLST);
        plot.addListener(plotLST);
        sessionBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openDateDialog();
            }
        });

        varsBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openVarsDialog();
            }
        });

        statsBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openStatsDialog();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.plot, container, false);
        return view;
    }
}
