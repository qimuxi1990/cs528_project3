package com.project3.step_counter;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.Date;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;


/**
 * Created by lxybi_000 on 2015/3/15.
 */
public class CounterDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "counter.sqlite";
    private static final int VERSION = 1;

    private static final String TABLE_COUNTER = "Counter";
    private static final String COLUMN_COUNTER_ID = "_id";
    private static final String COLUMN_COUNTER_STEPS= "steps";
    private static final String COLUMN_COUNTER_START_DATE = "start_date";
    private boolean table_dropped;// = true;

    // public constructor of this class
    public CounterDatabaseHelper (Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create the "counter" table
        db.execSQL("create table if not exists Counter (_id integer primary key autoincrement, steps integer)");
        //table_dropped = true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // implement schema and data changes here when upgrading
        //execSQL("DROP TABLE Counter if exists");
    }

    public long insertCounter(Counter counter) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_COUNTER_STEPS, counter.getSteps());
        //cv.put(COLUMN_COUNTER_START_DATE, counter.getStartDate().getTime());
        return getWritableDatabase().insert(TABLE_COUNTER, null, cv);

    }

    public CounterCursor queryCounter() {
        // equivalent to "select * from Counter"
        SQLiteDatabase db = getReadableDatabase();

        db.execSQL("create table if not exists Counter (_id integer primary key autoincrement, steps integer)");

        Cursor wrapped = getReadableDatabase().query(
                TABLE_COUNTER, // tb_name - String
                null, // column_name - String
                null, // selector exclude "where" - String (like id + "= ?")
                null, // list of values - String[] ({ String.valueOf(id) })
                null, // group by - String
                null,  // having - String
                COLUMN_COUNTER_ID + " asc" // order by - String
                // limit number - String
        );
        return new CounterCursor(wrapped);
    }

    public CounterCursor queryCounter(int id) {
        // equivalent to "select * from Counter where Id = id"
        Cursor wrapped = getReadableDatabase().query(
                TABLE_COUNTER, // tb_name - String
                null, // column_name - String
                COLUMN_COUNTER_ID + " = ?", // selector exclude "where" - String (like id + "= ?")
                new String[]{ String.valueOf(id) }, // list of values - String[] ({ String.valueOf(id) })
                null, // group by - String
                null,  // having - String
                COLUMN_COUNTER_ID + " asc" // order by - String
                // limit number - String
        );
        return new CounterCursor(wrapped);
    }

    /**
     * A convenience class to wrap a cursor that returns rows from the "run" table.
     * The {@link \getCounter()} method will give you a Run instance representing the current row.
     */
    public static class CounterCursor extends CursorWrapper {

        public CounterCursor(Cursor c) {
            super(c);
        }

        /**
         * Returns a Run object configured for the current row, or null if the current row is invalid.
         */
        public Counter getCounter() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            Counter counter = new Counter();
            counter.setSegment(getInt(getColumnIndex(COLUMN_COUNTER_ID)));
            counter.setSteps(getInt(getColumnIndex(COLUMN_COUNTER_STEPS)));
            //counter.setStartDate(new Date(getLong(getColumnIndex(COLUMN_COUNTER_START_DATE))));
            return counter;
        }

    }

    public boolean isTable_dropped() {
        return table_dropped;
    }

    public void dropTable() {
        table_dropped = true;
        getReadableDatabase().execSQL("Drop table Counter");
    }

}
