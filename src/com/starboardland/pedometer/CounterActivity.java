package com.starboardland.pedometer;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class CounterActivity extends Activity implements SensorEventListener {

    private boolean activityRunning;
    private MapView map;
    private SensorManager sensorManager;
    private TextView count;

    private TextView count1, count2, count3, count4,
                count5, count6, count7, count8;
    private TextView totalCount;

    private int segmentCounter = 1;
    private int steps_this_segment;
    private int totalSteps = 0;

    private Handler mHandler = new Handler();
    private Runnable timeTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        activityRunning = true;// start running the sensor detector
        map = (MapView) findViewById(R.id.mapview);
        map.onCreate(savedInstanceState);

        // register for sum/total segment view
        count = (TextView) findViewById(R.id.stepCount);

        // register for single segment views
        count1 = (TextView) findViewById(R.id.stepCount1);
        count2 = (TextView) findViewById(R.id.stepCount2);
        count3 = (TextView) findViewById(R.id.stepCount3);
        count4 = (TextView) findViewById(R.id.stepCount4);
        count5 = (TextView) findViewById(R.id.stepCount5);
        count6 = (TextView) findViewById(R.id.stepCount6);
        count7 = (TextView) findViewById(R.id.stepCount7);
        count8 = (TextView) findViewById(R.id.stepCount8);

        TextView[] countText = {count1, count2, count3, count4,
                            count5, count6, count7, count8};
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        final CounterManager cManager = CounterManager.get(this);

        List<Counter> counterList = cManager.getCounterList();
        if (counterList != null) {
            if (counterList.size() > 1) {
                segmentCounter = counterList.size() + 1;
                int i = 0;
                int sum = 0;
                // update UI of the component of the counter TextViews
                for (Counter counter : counterList) {
                    if (i < 8)
                        countText[i].setText(String.valueOf(counter.getSteps()));
                    else
                        break;
                    sum += counter.getSteps();
                    i++;
                }
                totalSteps = sum;
            }
        }

        // initiate a new thread. kill it when activity changes
        Runnable updateTimeTask = new Runnable() {
            public void run() {
                // some actions to be performed here
                try {
                    // do something here add some actions
                    if (segmentCounter <= 8) {
                        cManager.insertCounter(steps_this_segment);
                        // make and display toast like: "You took 120 steps in Segment 5"
                        Toast.makeText(CounterActivity.this, "You took " + steps_this_segment + " steps in Segment " + segmentCounter + "!",
                                Toast.LENGTH_LONG).show();
                        // new round for step counting
                        segmentCounter++;
                        steps_this_segment = 0;
                    }
                } catch (Exception exp) {
                    // process the exceptions here
                    exp.printStackTrace();
                } finally {
                    // delay function
                    if (segmentCounter <= 8) {
                        // assume duration of 10 seconds for test
                        long delayedDuration = 15*1000;
                        mHandler.postDelayed(this, delayedDuration);
                    } else {
                        // 8 segments are finished, stop counting and clean the database.
                        activityRunning = false;
                        mHandler.removeCallbacks(this);
                        try {
                            cManager.reset();
                            sensorManager.unregisterListener(CounterActivity.this);
                        } catch (SQLException exp) {}
                    }
                }
            }
        };
        timeTask = updateTimeTask;
        // import java.util.Timer class to as time counter(count at an interval of 2 minutes)

        // create new handler for counting time period
        long duration = 10 * 1000; // 20 seconds for test only
        mHandler.removeCallbacks(updateTimeTask);
        mHandler.postDelayed(updateTimeTask, duration);
        // condt to stop the handle
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        activityRunning = false;
        // if you unregister the last listener, the hardware will stop detecting step events
//        sensorManager.unregisterListener(this); 
    }

    @Override
    protected void onDestroy() {
        // when the activity is destroyed
        super.onDestroy();
        sensorManager.unregisterListener(this);

        // remove the running task from the thread
        mHandler.removeCallbacks(timeTask);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //int initVal= (int) event.values[0];
        if (activityRunning) {
            steps_this_segment++;
            totalSteps++;
            switch(segmentCounter) {
                case 1:
                    count1.setText(String.valueOf(steps_this_segment));
                    break;
                case 2:
                    count2.setText(String.valueOf(steps_this_segment));
                    break;
                case 3:
                    count3.setText(String.valueOf(steps_this_segment));
                    break;
                case 4:
                    count4.setText(String.valueOf(steps_this_segment));
                    break;
                case 5:
                    count5.setText(String.valueOf(steps_this_segment));
                    break;
                case 6:
                    count6.setText(String.valueOf(steps_this_segment));
                    break;
                case 7:
                    count7.setText(String.valueOf(steps_this_segment));
                    break;
                case 8:
                    count8.setText(String.valueOf(steps_this_segment));
                    break;
            }
            count.setText(String.valueOf(totalSteps));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class DBAccessTask extends TimerTask {
        @Override
        public void run() {
            Log.v("DBAccessTask", "I am the Database_Access_Task!");
            // once started, access and store values to the the database
           Log.v("newModel to code", "read and write");
            // and then write values back
        }
    }
}
