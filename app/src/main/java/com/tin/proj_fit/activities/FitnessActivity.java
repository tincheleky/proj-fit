package com.tin.proj_fit.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.text.Text;
import com.tin.proj_fit.AidlFitnessService;
import com.tin.proj_fit.R;
import com.tin.proj_fit.models.SensorsHelper;
import com.tin.proj_fit.models.User;
import com.tin.proj_fit.models.WorkoutSession;
import com.tin.proj_fit.providers.WorkoutSessionContentProvider;
import com.tin.proj_fit.services.FitnessService;
import com.tin.proj_fit.storage.LocationHistoryDbHelper;
import com.tin.proj_fit.storage.WorkoutDataDbHelper;
import com.tin.proj_fit.storage.WorkoutSessionDbHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FitnessActivity extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener,
        SensorEventListener {
    public static final double STEP_TO_KM = 0.000762;
    public static final String PREFERENCE = "PREFERENCE";
    public static final int STORING_DATA_INTERVAL = 5000;

    private GoogleMap mMap;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private static LocationManager locationManager = null;
    public static TextView tvStart;
    public static TextView tvLat;
    public static TextView tvLong;
    public static TextView tvDistance;
    public static TextView tvDuration;
    public static TextView tvStepCounter;
    public static TextView tvAvgMinKm;
    public static TextView tvMaxMinKm;
    public static TextView tvMinMinKm;
    public static LineChart mChart;

    private TextView tvRS;

    private static boolean isInSession = false;

    FitnessActivity thisActivity;
    AidlFitnessService rService;
    public static WorkoutDataDbHelper db;
    public static WorkoutSessionDbHelper sessionDb;

    Location initalLocation;
    RemoteConnection remoteConnection = null;
    static String hms;
    static double curLat;
    static double curLng;
    static double sessionDistance;
    static long curSecond = 0;
    static ArrayList<LatLng> sessionLocation;
    static boolean isLandscape = false;
    SharedPreferences sharedPreferences;

    static SensorManager sensorManager;
    static Sensor stepCounterSensor;
    static SensorsHelper sensorsHelper;
    static boolean isInit = false;
    static int initStepCounts;
    static int curStepCounts;
    static NumberFormat decimalFormatter = new DecimalFormat("#0.000");
    static CountDownTimer durationUpdateCountDownTimer;
    static CountDownTimer storingDataCountDownTimer;
    static Location curLocation;

    static double maxMinKm;
    static double minMinKm;
    static double avgMinKm;
    static double curMinKm;
    static long initTime;
    static long curTime;
    static int totalCaloBurnt;
    public static User user;
    static double distanceToBeStored;
    static long durationToBeStored;
    static int caloToBeStored;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity = this;
        setContentView(R.layout.activity_fitness);
        checkPermission();
        System.out.println("onCreate called");

        db = new WorkoutDataDbHelper(this);
        //sessionDb = new WorkoutSessionDbHelper(this);

        sharedPreferences = getSharedPreferences(PREFERENCE, MODE_PRIVATE);

        user = new User();
        user.setUserName(sharedPreferences.getString("name", ""));
        user.setGender(sharedPreferences.getString("gender", ""));
        user.setWeight(sharedPreferences.getInt("weight", 0));

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "Landscape", Toast.LENGTH_SHORT).show();
            setupChart();
            tvAvgMinKm = (TextView) findViewById(R.id.avg_min_km_display);
            tvMaxMinKm = (TextView) findViewById(R.id.max_min_km_display);
            tvMinMinKm = (TextView) findViewById(R.id.min_min_km_display);
            isLandscape = true;
        } else {

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            isLandscape = false;

            if (locationManager == null)
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            tvStart = (TextView) findViewById(R.id.btn_start);

            tvStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (tvStart.getText().toString().compareToIgnoreCase("Start") == 0) {
                        initSession();
                        sessionLocation = new ArrayList<LatLng>();
                        remoteConnection = new RemoteConnection();
                        Intent intent = new Intent(thisActivity, FitnessService.class);
                        intent.setAction(AidlFitnessService.class.getName());
                        //intent.setClassName("com.tin.proj_fit.services", com.tin.proj_fit.services.FitnessService.class.getName());
                        if (!bindService(intent, remoteConnection, Context.BIND_AUTO_CREATE)) {
                            tvRS.setText("Failed to bind Remote Service");
                        } else {
                            tvRS.setText("RS binded");
                        }

                        tvStart.setText("Stop");
                        Toast.makeText(thisActivity, "Start training", Toast.LENGTH_SHORT).show();


                        //Get step sensor
                        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                        sensorsHelper = new SensorsHelper(sensorManager, stepCounterSensor);

                        //Register sensor:
                        sensorManager.registerListener(thisActivity, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);

                        try {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.1f, thisActivity);
                            System.out.println("REGISTER LOCATION UPDATE");
                            initalLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            curLat = initalLocation.getLatitude();
                            curLng = initalLocation.getLongitude();
                            System.out.println("Init Location: " + initalLocation.getLatitude() + initalLocation.getLongitude());
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    } else {
                        tvStart.setText("Start");
                        endSesstion();
                        Cursor cursor = db.getData();
                        int cnt = 0;
                        while (cursor.moveToNext())
                            cnt++;
                        System.out.println("Total Session entries: " + cnt);
                        Toast.makeText(thisActivity, "Stop training", Toast.LENGTH_SHORT).show();

                        //SESSION END Save each session, add the session to user
                        WorkoutSession workoutSession = new WorkoutSession(0, curSecond / 1000, 0);

//                        user.getAllTimeList().add(workoutSession);
//                        user.getWeeklyList().add(workoutSession);

                        if (remoteConnection != null) {
                            unbindService(remoteConnection);
                            remoteConnection = null;
                        }

                        //UNREGISTER SENSORS, LOCATIONLISTENNER
                        if (sensorsHelper.getSensorManager() != null) {
                            System.out.println("Successful unregister sensor");
                            if (stepCounterSensor != null)
                                sensorsHelper.getSensorManager().unregisterListener(thisActivity, stepCounterSensor);
                        } else {
                            System.out.println("Failed to unregister sensor");
                        }

                        try {
                            locationManager.removeUpdates(thisActivity);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

            tvLat = (TextView) findViewById(R.id.debug_lat);
            tvLong = (TextView) findViewById(R.id.debug_long);
            tvRS = (TextView) findViewById(R.id.debug_rs);
            tvDistance = (TextView) findViewById(R.id.distance_display);
            tvDuration = (TextView) findViewById(R.id.duration_display);
            tvStepCounter = (TextView) findViewById(R.id.debug_step_counter);

        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        } else {

        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isInSession) {
            Sensor sensor = sensorEvent.sensor;
            float[] values = sensorEvent.values;
            int value = -1;

            if (values.length > 0) {
                if (!isInit) {
                    initStepCounts = (int) values[0];
                    isInit = true;
                    curStepCounts = initStepCounts;
                } else {
                    value = (int) values[0] - curStepCounts;
                    curStepCounts += value;
                    updateWorkoutDetail(value);
                    updateWorkoutSession(value);
                }

            }

            //Debug purpose
            if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                tvStepCounter.setText("Step Detected: " + value);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        curLocation = location;
        if (isInSession) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 18));
//            System.out.println("Lat-Lng: " + lat + "," + lng);
            tvLat.setText(String.valueOf(lat));
            tvLong.setText(String.valueOf(lng));
            sessionLocation.add(new LatLng(lat, lng));
            updatePath();
        }
    }

    private void updatePath() {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(sessionLocation);
        polylineOptions.color(Color.GREEN);
        polylineOptions.width(10);
        mMap.clear();
        mMap.addPolyline(polylineOptions);
    }

    private void updateWorkoutSession(int steps) {
        sessionDistance += steps * STEP_TO_KM;

        if (tvDistance != null) {
            String temp = decimalFormatter.format(sessionDistance) + " km";
            tvDistance.setText(temp);
        }
    }

    private void updateDuration() {
        curSecond += 1000;
        hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(curSecond),
                TimeUnit.MILLISECONDS.toMinutes(curSecond) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(curSecond)),
                TimeUnit.MILLISECONDS.toSeconds(curSecond) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(curSecond)));

        if (tvDuration != null)
            tvDuration.setText(hms);
    }


    class RemoteConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            System.out.println("STEP 1 ------------------");
            rService = AidlFitnessService.Stub.asInterface(iBinder);
            Toast.makeText(FitnessActivity.this, "RS connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            rService = null;
            Toast.makeText(thisActivity, "RS Disconnected", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        System.out.println("onResume Called");
        setCurrentStatus();
        if (isInSession) {
            if (tvStart != null)
                tvStart.setText("Stop");
        } else {
            if (tvStart != null)
                tvStart.setText("Start");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPauseCalled");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (remoteConnection != null) {
            unbindService(remoteConnection);
            remoteConnection = null;
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
            Location initLoc = ((LocationManager) getSystemService(LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (initLoc != null)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(initLoc.getLatitude(), initLoc.getLongitude()),
                        18));
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void showProfileActivity(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }


    private void setData(int count, float range) {

        ArrayList<Entry> values = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {

            float val = (float) (Math.random() * range) + 3;
            values.add(new Entry(i, val));
        }

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

//            if (Utils.getSDKInt() >= 18) {
//                // fill drawable only supported on api level 18 and above
//                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
//                set1.setFillDrawable(drawable);
//            }
//            else {
//                set1.setFillColor(Color.BLACK);
//            }

            set1.setFillColor(Color.BLACK);
            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);
        }
    }

    private void setupChart() {
        mChart = (LineChart) findViewById(R.id.chart);

        //SETUP CHART
        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setAxisMaximum(24f);
        xAxis.setAxisMinimum(0f);
        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line


//            Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        LimitLine ll1 = new LimitLine(150f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
//            ll1.setTypeface(tf);

        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
//            ll2.setTypeface(tf);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaximum(200f);
        leftAxis.setAxisMinimum(-50f);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);

        setData(45, 100);
    }

    private void initSession() {
        sessionDistance = 0.0;
        avgMinKm = 0.0;
        maxMinKm = 0.0;
        minMinKm = 9999.0;
        curMinKm = 0.0;
        initTime = System.currentTimeMillis();
        curTime = initTime;

        isInit = false;
        isInSession = true;

        if (tvDistance != null) {
            tvDistance.setText("0.000 km");
        }
        if (tvDuration != null) {
            tvDuration.setText("00:00:00");
        }

        curSecond = 0;
        durationUpdateCountDownTimer = new CountDownTimer(1000, 500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                updateDuration();
                updateProfileActivity();
                durationUpdateCountDownTimer.start();
            }
        };
        durationUpdateCountDownTimer.start();

        startStoringData();
    }

    private void endSesstion() {
        if (durationUpdateCountDownTimer != null) {
            durationUpdateCountDownTimer.cancel();
        }
        isInSession = false;
//        sessionDb.putData(new WorkoutSession(sessionDistance, curSecond, totalCaloBurnt));
        ContentValues contentValues = new ContentValues();
        contentValues.put(WorkoutSessionContentProvider.DISTANCE, sessionDistance);
        contentValues.put(WorkoutSessionContentProvider.DURATION, curSecond);
        contentValues.put(WorkoutSessionContentProvider.CALORIES, totalCaloBurnt);

        Uri uri = getContentResolver().insert(
                WorkoutSessionContentProvider.URI, contentValues);

        Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show();

        // Retrieve student records
        String URL = "content://com.tin.proj_fit.providers.WorkoutSessionContentProvider";

        Uri workouts = Uri.parse(URL);
        Cursor c = managedQuery(workouts, null, null, null, "_id");

        if (c.moveToFirst()) {
            do{
                Toast.makeText(this,
                        c.getString(c.getColumnIndex(WorkoutSessionContentProvider._ID)) +
                                ", " +  c.getString(c.getColumnIndex( WorkoutSessionContentProvider.DISTANCE)) +
                                ", " + c.getString(c.getColumnIndex( WorkoutSessionContentProvider.DURATION)), Toast.LENGTH_SHORT).show();
            } while (c.moveToNext());
        }
    }

    private void setCurrentStatus() {
        if (tvDistance != null) {
            tvDistance.setText(decimalFormatter.format(sessionDistance) + " km");
        }
        if (tvDuration != null) {
            tvDuration.setText(hms);
        }
    }

    private void updateProfileActivity() {
        if (ProfileActivity.tvAvgDistance != null) {
            ProfileActivity.tvAvgDistance.setText(decimalFormatter.format(sessionDistance) + " km");
        }

        if (ProfileActivity.tvAvgTime != null) {
            ProfileActivity.tvAvgTime.setText(hms);
        }
    }

    private void updateWorkoutDetail(int steps) {
        long tempCurTime = System.currentTimeMillis();
        long deltaTime = tempCurTime - curTime;
        curTime += deltaTime;
        double tempMinKm = (deltaTime / 60000.0) / (steps * STEP_TO_KM);

        System.out.println("STEPS COUNTS LANDSCAPE: " + steps);

        if (tempMinKm < minMinKm) {
            minMinKm = tempMinKm;
        }

        if (tempMinKm > maxMinKm) {
            maxMinKm = tempMinKm;
        }
        avgMinKm += tempMinKm;
        avgMinKm /= 2.0;

        if (tvAvgMinKm != null) {
            tvAvgMinKm.setText(decimalFormatter.format(avgMinKm));
        }

        if (tvMinMinKm != null) {
            tvMinMinKm.setText(decimalFormatter.format(maxMinKm));
        }

        if (tvMaxMinKm != null) {
            tvMaxMinKm.setText(decimalFormatter.format(minMinKm));
        }
    }

    private void startStoringData()
    {
        storingDataCountDownTimer = new CountDownTimer(STORING_DATA_INTERVAL, 1000) {
            @Override
            public void onTick(long l)
            {
                if(!isInSession)
                {
                    try {
                        if (rService != null)
                        {
                            rService.putData(distanceToBeStored, durationToBeStored, caloToBeStored);
                            System.out.println("rService.debugPrint() " + rService.debugPrint(distanceToBeStored, durationToBeStored, caloToBeStored));
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    storingDataCountDownTimer.cancel();
                    storingDataCountDownTimer = null;
                }
            }

            @Override
            public void onFinish() {
                try {
                    if (rService != null) {
                        rService.putData(distanceToBeStored, durationToBeStored, caloToBeStored);
                        System.out.println("rService.debugPrint() " + rService.debugPrint(distanceToBeStored, durationToBeStored, caloToBeStored));
                        Cursor cursor = db.getData();
                        int cnt = 0;
                        while (cursor.moveToNext())
                            cnt++;
                        System.out.println("Total Workout Data entries: " + cnt);
                        storingDataCountDownTimer.start();
                    }


                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
        storingDataCountDownTimer.start();
    }
}