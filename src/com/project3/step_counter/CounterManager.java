package com.project3.step_counter;

/**
 * Singleton Design Pattern
 * avoid of concurrency problem of accessing database
 * Created by lxybi_000 on 2015/3/15.
 */
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.project3.step_counter.CounterDatabaseHelper.CounterCursor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CounterManager {

    private static final String TAG = "CounterManager";

    private static final String PREFS_FILE = "counts";
    private static final String PREF_CURRENT_SEGMENT_ID = "CounterManager.currentSegmentId";

    public static final String ACTION_LOCATION = "com.bignerdranch.android.runtracker.ACTION_LOCATION";

    private static final String TEST_PROVIDER = "TEST_PROVIDER";

    private static CounterManager sCounterManager;
    private Context mAppContext;
    private CounterDatabaseHelper mHelper;
    private SharedPreferences mPrefs;
    private long mCurrentSegmentId;

    // private constructor
    private CounterManager(Context appContext) {
        mAppContext = appContext;
        mHelper = new CounterDatabaseHelper(mAppContext);
        mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mCurrentSegmentId = mPrefs.getLong(PREF_CURRENT_SEGMENT_ID, -1);
    }
    // public static constructor to instantiate one instance
    public static CounterManager get(Context c) {
        if (sCounterManager == null) {
            // we use the application context to avoid leaking activities
            sCounterManager = new CounterManager(c.getApplicationContext());
        }
        return sCounterManager;
    }

    public void counterStop() {

    }
    public Counter insertCounter(int steps) {
        Counter counter = new Counter();
        counter.setSteps(steps);
        counter.setSegment((int) mHelper.insertCounter(counter));// cautions about the row_id
        return counter;
    }

    public List<Counter> getCounterList() {
        Counter counter = null;
        List<Counter> cList = new ArrayList<Counter>();
        CounterCursor cursor = mHelper.queryCounter();
        cursor.moveToFirst();
        // if we got a row, get a run

       while (!cursor.isAfterLast()) {
           counter = cursor.getCounter();
           if (counter != null)
                cList.add(counter);
           cursor.moveToNext();
        }

        cursor.close();
        return cList;
    }

    public CounterCursor queryCounter(int id) {
        return mHelper.queryCounter(id);
    }

    public Counter getCounter(int id) {
        Counter counter = null;
        CounterCursor cursor = queryCounter(id);
        cursor.moveToFirst();
        // if we got a row, get a run
        if (!cursor.isAfterLast())
            counter = cursor.getCounter();
        cursor.close();
        return counter;
    }

    public void reset () throws SQLException {
        SQLiteDatabase db = mHelper.getWritableDatabase ();
        dropTable();
        db.close ();
       // mHelper.onCreate (db);
    }

    public void dropTable() {
        mHelper.dropTable();
    }
}
