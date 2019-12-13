package com.moviz.lib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.LongSparseArray;

import com.moviz.gui.app.CA;
import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.holder.DeviceHolder;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.plus.holder.Databasable;
import com.moviz.lib.comunication.plus.holder.Joinable;
import com.moviz.lib.comunication.plus.holder.PConfHolder;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.comunication.plus.holder.UpdateDatabasable;
import com.moviz.lib.plot.ProgressPub;
import com.moviz.lib.utils.DeviceTypeMaps;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pafersmain";
    private static final int DATABASE_VERSION = 2;
    private String path;
    private SQLiteDatabase db = null;
    private static MySQLiteHelper instance = null;

    public static MySQLiteHelper newInstance(Context context, String path) {
        if ((instance == null || instance.db==null) && context != null && path!=null) {
            try {
                instance = new MySQLiteHelper(context, path);
                instance.openDB();
            } catch (Exception e1) {
                e1.printStackTrace();
                try {
                    instance.closeDB();
                }
                catch (Exception e2) {

                }
                instance = null;
            }
        }
        return instance;
    }

    public MySQLiteHelper(Context context, String path) {
        super(context, path.isEmpty() ? DATABASE_NAME + ".db" : new File(path).isDirectory()?path + '/' + DATABASE_NAME + ".db":path, null, DATABASE_VERSION);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public SQLiteDatabase openDB() {
        if (db == null || !db.isOpen()) {
            db = getWritableDatabase();
            db.rawQuery("PRAGMA foreign_keys = ON;", new String[0]);
        }
        return db;
    }

    public void closeDB() {
        if (db != null && db.isOpen())
            db.close();
        db = null;
    }

    @Override
    /*public void onCreate(SQLiteDatabase database) {
		Databasable dbl = new PUserHolder();
		String sql = dbl.toDBTable();
		 sql = "create table "+ dbl.getTableName() + 
			" ("+sql+");";
		database.execSQL(sql);
		dbl = new PDeviceHolder();
		sql = dbl.toDBTable();
		 sql = "create table "+ dbl.getTableName() + 
			" ("+sql+");";
		database.execSQL(sql);
		dbl = new PSessionHolder();
		sql = dbl.toDBTable();
		 sql = "create table "+ dbl.getTableName() + 
			" ("+sql+");";
		database.execSQL(sql);
		for (Map.Entry<DeviceType,Databasable> entry : DeviceDbPopulator.pop.entrySet())
		{
			dbl = entry.getConf();
			sql = "create table "+ dbl.getTableName() + 
					" ("+dbl.toDBTable()+", "+
					"session Integer not null," +
					"FOREIGN KEY(session) REFERENCES session(_id) ON DELETE CASCADE" +
					");";
			database.execSQL(sql);
		}
	}*/
    public void onCreate(SQLiteDatabase database) {
        Databasable dbl = new PUserHolder();
        String sql = dbl.toDBTable();
        database.execSQL(sql);
        dbl = new PDeviceHolder();
        sql = dbl.toDBTable();
        database.execSQL(sql);
        dbl = new PSessionHolder();
        sql = dbl.toDBTable();
        database.execSQL(sql);
        for (Map.Entry<DeviceType, UpdateDatabasable> entry : DeviceTypeMaps.type2update.entrySet()) {
            dbl = entry.getValue();
            sql = dbl.toDBTable();
            database.execSQL(sql);
        }
        sql = new PConfHolder().toDBTable();
        database.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.tag(MySQLiteHelper.class.getName()).w("Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
		/*Databasable dbl = new PUserHolder();
		db.execSQL("DROP TABLE IF EXISTS " + dbl.getTableName());
		dbl = new PDeviceHolder();
		db.execSQL("DROP TABLE IF EXISTS " + dbl.getTableName());
		dbl = new PSessionHolder();
		db.execSQL("DROP TABLE IF EXISTS " + dbl.getTableName());
		for (Map.Entry<DeviceType,UpdateDatabasable> entry : DeviceTypeMaps.type2update.entrySet())
		{
			dbl = entry.getConfId();
			db.execSQL("DROP TABLE IF EXISTS "+ dbl.getTableName()+";");
		}
		onCreate(db);*/
    }

    /*public String getConfigurationByName(String sc) {
        String rv = null;
        Cursor cursor = db.query("deviceconf", new String[]{"conf"}, "name=?", new String[]{sc}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            rv = cursor.getString(0);
            break;
        }
        cursor.close();
        return rv;
    }

    public Map<String, String> getConfigurations() {
        Map<String, String> rv = new HashMap<>();
        String sql = "SELECT name,conf " +
                "FROM deviceconf;";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            rv.put(cursor.getString(0), cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return rv;
    }

    public long saveConfiguration(String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put("name", key);
        cv.put("conf", value);
        return db.insertWithOnConflict("deviceconf", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }*/

    public long[] getShorterSessions(int mins, StringBuilder sb) {
        UpdateDatabasable dbl;
        int i = 0;
        String aggr = "", join = "", where = " ";
        long mainid = -1;
        mins *= 60000;
        for (DeviceType dType : DeviceType.types) {
            dbl = DeviceTypeMaps.type2update.get(dType);
            dbl.getSessionAggregateVars();
            if (i > 0) {
                where += "AND ";
                aggr += ", ";
            }
            aggr += "MAX(V" + i + ".ctimems) AS V" + i + "ctimems ";
            where += "(V" + i + "ctimems ISNULL OR V" + i + "ctimems<" + mins + ") ";
            join += "LEFT JOIN " + dbl.getTableName() + " AS V" + i + " ON S._id = V" + i + ".session ";
            i++;
        }
        PSessionHolder sesTemp = new PSessionHolder();
        String sql = "SELECT ifnull(S.mainid,S._id) AS Smainid2, " + aggr +
                "FROM " + sesTemp.getTableName() + " AS S " +
                join +
                "GROUP BY  S._id " +
                "HAVING" + where +
                "ORDER BY Smainid2";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        long[] mainids = new long[cursor.getCount()];
        i = 0;
        while (!cursor.isAfterLast()) {
            mainid = cursor.getLong(0);
            if (i > 0)
                sb.append(",");
            sb.append(mainid + "");
            mainids[i++] = mainid;
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return mainids;
    }

    private String selc(Databasable d, String p) {
        String selc = d.selectCols(p);
        String newselc = "";
        int nextindex = 0, firstidx;
        Pattern pattern = Pattern.compile("\\{([^\\}]+)\\}");
        Matcher matcher = pattern.matcher(selc);
        Pattern pattern2 = Pattern.compile("\\[([^\\]]+)\\]");
        // check all occurance
        Cursor c = null;
        while (matcher.find()) {
            firstidx = matcher.start();
            if (firstidx>nextindex) {
                newselc+=selc.substring(nextindex,firstidx);
            }
            nextindex = matcher.end();
            Matcher matcher2 = pattern2.matcher(matcher.group(1));
            if (matcher2.find()) {
                try {
                    if (c == null) {
                        c = db.rawQuery("Select * from " + d.getTableName() + " limit 1", null);
                    }
                    if (c.getColumnIndex(matcher2.group(1)) >= 0) {
                        newselc+=matcher2.replaceAll("$1");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        firstidx = selc.length();
        if (firstidx>nextindex) {
            newselc+=selc.substring(nextindex,firstidx);
        }
        if (c!=null)
            c.close();
        return newselc;
    }

    public LongSparseArray<List<PSessionHolder>> getAllSessions(Date datef, Date datet, Boolean exported) {
        LongSparseArray<List<PSessionHolder>> rv = new LongSparseArray<List<PSessionHolder>>();
        List<PSessionHolder> left = null;
        long mainid = -1, tmpid;
        PSessionHolder sesTemp = new PSessionHolder();
        PUserHolder userTemp = new PUserHolder();
        PDeviceHolder deviceTemp = new PDeviceHolder();
        String where = "";
        if (exported != null) {
            if (exported.booleanValue())
                where = "WHERE S.exported=0 ";
            else
                where = "WHERE S.exported=65535 OR S.exported=15 ";
                //where = "WHERE S.exported<>0 AND exported<>1 ";
        }
        if (datef != null) {
            if (where.isEmpty())
                where += "WHERE ";
            else
                where += "AND ";
            where += "S.datestart>=" + datef.getTime() + " ";
        }
        if (datet != null) {
            if (where.isEmpty())
                where += "WHERE ";
            else
                where += "AND ";
            where += "S.datestart<=" + datet.getTime() + " ";
        }
        String sql = "SELECT " +
                selc(sesTemp,"S") + ", " +
                selc(deviceTemp,"D") + ", " +
                selc(userTemp,"U") +
                ", ifnull(S.mainid,S._id) AS Smainid2 " +
                "FROM " + sesTemp.getTableName() + " AS S " +
                "JOIN " + deviceTemp.getTableName() + " AS D ON S.device = D._id " +
                "JOIN " + userTemp.getTableName() + " AS U ON S.user = U._id " +
                where +
                "GROUP BY  S._id " +
                "ORDER BY Smainid2,Sdatestart";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            PSessionHolder comment = new PSessionHolder();
            comment.fromCursor(cursor, new String[]{"S", "D", "U", "V0"});
            if (mainid != (tmpid = cursor.getLong(cursor.getColumnIndex("Smainid2")))) {
                left = new ArrayList<PSessionHolder>();
                mainid = tmpid;
                rv.append(mainid, left);
            }
            left.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return rv;
    }


    public LongSparseArray<List<PSessionHolder>> getAllSessions(Date datef, Date datet, ProgressPub<SessionLoadDBProgress> prog) {
        LongSparseArray<List<PSessionHolder>> rv = new LongSparseArray<List<PSessionHolder>>();
        UpdateDatabasable dbl;
        int i = 0;
        String aggr = "", vname, fun, join = "";
        List<PSessionHolder> left = null;
        long mainid = -1, tmpid;
        for (DeviceType dType : DeviceType.types) {
            dbl = DeviceTypeMaps.type2update.get(dType);
            PHolderSetter sesh = dbl.getSessionAggregateVars();
            for (Holder h : sesh) {
                vname = h.getChildId();
                fun = h.getParentId(1);
                aggr += fun + "(V" + i + "." + vname + ") AS V" + i + vname + ", ";
            }
            join += "LEFT JOIN " + dbl.getTableName() + " AS V" + i + " ON S._id = V" + i + ".session ";
            i++;
        }
        PSessionHolder sesTemp = new PSessionHolder();
        PUserHolder userTemp = new PUserHolder();
        PDeviceHolder deviceTemp = new PDeviceHolder();
        String where = "";
        if (datef != null)
            where += "WHERE S.datestart>=" + datef.getTime() + " ";
        if (datet != null) {
            if (where.isEmpty())
                where += "WHERE ";
            else
                where += "AND ";
            where += "S.datestart<=" + datet.getTime() + " ";
        }
        String sql = "SELECT " + aggr +
                selc(sesTemp,"S") + ", " +
                selc(deviceTemp,"D") + ", " +
                selc(userTemp,"U") +
                ", ifnull(S.mainid,S._id) AS Smainid2 " +
                "FROM " + sesTemp.getTableName() + " AS S " +
                join +
                "JOIN " + deviceTemp.getTableName() + " AS D ON S.device = D._id " +
                "JOIN " + userTemp.getTableName() + " AS U ON S.user = U._id " +
                where +
                "GROUP BY  S._id " +
                "ORDER BY Smainid2,Sdatestart";
        Cursor cursor = db.rawQuery(sql, null);
        int tot = cursor.getCount(), n = 0;
        cursor.moveToFirst();
        SessionLoadDBProgress slp;
        while (!cursor.isAfterLast()) {
            PSessionHolder comment = new PSessionHolder();
            DeviceType dType = DeviceType.types[i = cursor.getShort(cursor.getColumnIndex("Dtype"))];
            dbl = DeviceTypeMaps.type2update.get(dType);
            comment.getPHolders().set(dbl.getSessionAggregateVars());
            comment.fromCursor(cursor, new String[]{"S", "D", "U", "V" + i});
            if (mainid != (tmpid = cursor.getLong(cursor.getColumnIndex("Smainid2")))) {
                left = new ArrayList<PSessionHolder>();
                mainid = tmpid;
                rv.append(mainid, left);
            }
            left.add(comment);
            if (prog != null) {
                slp = new SessionLoadDBProgress(++n, tot, mainid, comment);
                prog.calcProgress(slp, slp.cur, slp.tot);
            }
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return rv;
    }

    public List<PSessionHolder> getAllSessions(DeviceHolder d) {
        long did = d.getId();
        UpdateDatabasable dbl = DeviceTypeMaps.type2update.get(d.getType());
        List<PSessionHolder> comments = new ArrayList<PSessionHolder>();
        PHolderSetter sesh = dbl.getSessionAggregateVars();
        String aggr = "", vname, fun;
        for (Holder h : sesh) {
            vname = h.getChildId();
            fun = h.getParentId(1);
            aggr += fun + "(V." + vname + ") AS V" + vname + ", ";
        }
        PSessionHolder sesTemp = new PSessionHolder();
        PUserHolder userTemp = new PUserHolder();
        PDeviceHolder deviceTemp = new PDeviceHolder();
        String sql = "SELECT " + aggr +
                selc(sesTemp,"S") + ", " +
                selc(deviceTemp,"D") + ", " +
                selc(userTemp,"U") +
                ", ifnull(S.mainid,S._id) AS Smainid2 " +
                "FROM session AS S " +
                "JOIN " + dbl.getTableName() + " AS V ON S._id = V.session " +
                "JOIN device AS D ON S.device = D._id " +
                "JOIN user AS U ON S.user = U._id " +
                "WHERE D._id=" + did + " " +
                "GROUP BY  S._id " +
                "ORDER BY S.datestart";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            PSessionHolder comment = new PSessionHolder();
            comment.getPHolders().set(sesh);
            comment.fromCursor(cursor, new String[]{"S", "D", "U", "V"});
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }

    public List<? extends Databasable> getAllValues(Databasable dbl, String order) {
        List<Databasable> lst = new ArrayList<Databasable>();
        Cursor cursor = db.query(dbl.getTableName(),
                null, null, null, null, null, order == null ? "_id" : order);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                dbl = dbl.getClass().newInstance();
                dbl.fromCursor(cursor, new String[]{""});
                lst.add((Databasable) dbl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return lst;
    }

    public boolean getValue(Databasable dbl,String cond) {
        boolean rv = false;
        Cursor cursor = db.query(dbl.getTableName(),
                null, cond, null, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            dbl.fromCursor(cursor, new String[]{""});
            rv = true;
        }
        cursor.close();
        return rv;
    }

    public boolean getValue(Databasable dbl) {
        return getValue(dbl,"_id=" + dbl.getId());
    }

    public void loadSessionValues(PSessionHolder ses, ProgressPub<Integer[]> pp) {
        DeviceHolder dh = ses.getDevice();
        UpdateDatabasable dbl = DeviceTypeMaps.type2update.get(dh.getType());
        Cursor cursor = db.query(dbl.getTableName(),
                null, "session=" + ses.getId(), null, null, null, "_id");
        cursor.moveToFirst();
        Integer[] progInt = new Integer[]{0, cursor.getCount()};
        int i = 0;
        ses.prepareToValues(progInt[1]);
        while (!cursor.isAfterLast()) {
            try {
                dbl = dbl.getClass().newInstance();
                dbl.fromCursor(cursor, new String[]{""});
                ses.addValue((DeviceUpdate) dbl);
            } catch (Exception e) {
                break;
            }
            cursor.moveToNext();
            if (pp != null) {
                progInt[0] = ++i;
                pp.calcProgress(progInt, progInt[0], progInt[1]);
            }
        }
        // make sure to close the cursor
        cursor.close();
    }

    public void deleteValue(Databasable dbl) {
        db.delete(dbl.getTableName(), "_id=" + dbl.getId(), null);
    }

    public void deleteValue(String idset, Databasable dbl) {
        db.delete(dbl.getTableName(), "_id in (" + idset + ")", null);
    }

    public void deleteSessionByMainId(long mainid) {
        PSessionHolder dbl = new PSessionHolder();
        db.delete(dbl.getTableName(), "mainid=" + mainid + " OR _id=" + mainid, null);
    }

    public void deleteSessionByMainIds(String idset) {
        PSessionHolder dbl = new PSessionHolder();
        db.delete(dbl.getTableName(), "mainid in (" + idset + ") OR _id in (" + idset + ")", null);
    }

    public void setSessionExported(long mainid, int exp) {
        PSessionHolder dbl = new PSessionHolder();
        dbl.setExported(exp);
        db.update(dbl.getTableName(), dbl.getExportedColumn(), "mainid=" + mainid + " OR _id=" + mainid, null);
    }

    public synchronized void newValue(Databasable dbl) {
        ContentValues[] cvs = dbl.toDBValue();

        long id = dbl.getId();
        if (id >= 0) {
            db.update(dbl.getTableName(), cvs[0], "_id=" + id, null);
        } else {
            try {
                int i = 0;
                for (ContentValues cv : cvs) {
                    id = db.insertWithOnConflict(dbl.getTableName(), null, cv, dbl.getConflictPolicy());
                    if (id >= 0 && i == 0)
                        dbl.setId(id);
                    i++;
                }
            } catch (SQLException sqle) {
                CA.logException(sqle);
                db.execSQL(dbl.toDBTable());
            }
        }
    }

    public long getNextId(Databasable dbl) {
        long rv;
        Cursor cursor = db.query("sqlite_sequence",
                new String[]{"seq"}, "name=?", new String[]{dbl.getTableName()}, null, null, null);
        cursor.moveToFirst();
        rv = 1;
        while (!cursor.isAfterLast()) {
            rv = cursor.getLong(0) + 1;
            break;
        }
        cursor.close();
        return rv;
    }

    private void join(SQLiteDatabase db, Joinable joi, long other, long difftime) {
        String rv = joi.prepareForJoin(other);

        Cursor c = null;
        if (rv != null) {
            c = db.rawQuery(rv, null);
            c.moveToFirst();
        }
        String[] queries = joi.join(c, other, difftime);
        if (c != null)
            c.close();
        if (queries != null) {
            for (String q : queries) {
                db.execSQL(q);
            }
        }
    }

    public void joinSessions(PSessionHolder so, PSessionHolder sn) {
        long other = sn.getId();
        long difftime = sn.getDateStart() - so.getDateStart();
        UpdateDatabasable upd = DeviceTypeMaps.type2update.get(so.getDevice().getType());
        upd.setSessionId(so.getId());
        join(db, upd, other, difftime);
        join(db, so, other, difftime);
    }
	
	/*public void newDevice(PDeviceHolder dev) {
		ContentValues cv = dev.toDBValue();
		SQLiteDatabase db = openDB();
		long id = db.insert(dev.getTableName(), null, cv);
		if (id>=0)
			dev.setId(id);
	}
	
	public void newSession(PSessionHolder ses) {
		ContentValues cv = ses.toDBValue();
		SQLiteDatabase db = openDB();
		long id = db.insert(ses.getTableName(), null, cv);
		if (id>=0)
			ses.setId(id);
	}
	
	public void newValue(DeviceUpdate d,PSessionHolder ses) {
		Databasable dbl = (Databasable) d;
		ContentValues cv = dbl.toDBValue();
		cv.put("session", ses.getId());
		long id = db.insert(dbl.getTableName(), null, cv);
		if (id>=0)
			dbl.setId(id);
	}*/

}