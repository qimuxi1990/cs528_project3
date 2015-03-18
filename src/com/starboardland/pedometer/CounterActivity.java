package com.starboardland.pedometer;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.sql.SQLException;
import java.util.List;
import java.util.TimerTask;

public class CounterActivity extends Activity implements SensorEventListener, OnMapReadyCallback
,LocationListener, LocationSource{

    private static final String TAG = "deactivate_tag"
            ;
    private boolean activityRunning;
    private MapFragment mapFragment;
    private GoogleMap map;
    private SensorManager sensorManager;
    private TextView count;

    private TextView count1, count2, count3, count4,
                count5, count6, count7, count8;
    //private TextView totalCount;
    private int segmentCounter = 1;
    private int steps_this_segment;
    private int totalSteps = 0;

    private Handler mHandler = new Handler();
    private Runnable timeTask;

    // fields to track location changes and updates
    private OnLocationChangedListener mListener;
    private LocationManager locationManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        activityRunning = true;// start running the sensor detector

        // register for the locationManager to make use of the GPS service
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkIsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(gpsIsEnabled)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10F, this);
            }
            else if(networkIsEnabled)
            {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 10F, this);
            }
            else
            {
                //Show an error dialog that GPS is disabled...
            }
        }
        else
        {
            //Show some generic error dialog because something must have gone wrong with location manager.
        }

        // register mapFragment and set up the map
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map != null)
            map.setMyLocationEnabled(true);
        map.setLocationSource(this);
       // map.getMapAsync(this);

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // TODO get location
        // LatLng sydney = new LatLng(-33.867, 151.206);
        googleMap.setMyLocationEnabled(true);
        // TODO set location
        // map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

    }


    @Override
    public void onLocationChanged(Location location) {
        if (mListener != null){
            mListener.onLocationChanged(location);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 15);
            map.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
       // Toast.makeText(this, "status changed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
       // Toast.makeText(this, "provider enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.v(TAG, "something is disabled!");
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener  =  onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        Log.v(TAG, "location service is deactivated!");
    }
}
