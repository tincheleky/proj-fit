package com.tin.proj_fit.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.realtime.internal.event.TextInsertedDetails;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tin.proj_fit.AidlFitnessService;
import com.tin.proj_fit.AidlFitnessServiceCallback;
import com.tin.proj_fit.R;
import com.tin.proj_fit.models.User;
import com.tin.proj_fit.models.WorkoutSession;
import com.tin.proj_fit.services.FitnessService;
import com.tin.proj_fit.storage.LocationData;
import com.tin.proj_fit.storage.LocationHistoryContract;
import com.tin.proj_fit.storage.LocationHistoryDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FitnessActivity extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener{
    public static User user;

    private GoogleMap mMap;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private static LocationManager locationManager = null;
    public static TextView tvStart;
    public static TextView tvLat;
    public static TextView tvLong;
    public static TextView tvDistance;
    public static TextView tvDuration;
    public static LineChart mChart;

    private TextView tvRS;

    private static boolean isInSession = false;

    FitnessActivity thisActivity;
    AidlFitnessService rService;
    public static LocationHistoryDbHelper db;

    Location initalLocation;
    RemoteConnection remoteConnection = null;
    static String hms;
    static double curLat;
    static double curLng;
    static long curSecond = 0;
    boolean firstRun = false;
    ArrayList<LatLng> sessionLocation;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity = this;
        setContentView(R.layout.activity_fitness);
        checkPermission();

        db = new LocationHistoryDbHelper(this);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            Toast.makeText(thisActivity, "Landscape", Toast.LENGTH_SHORT).show();
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
        else
        {

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            if(locationManager == null)
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            tvStart = (TextView) findViewById(R.id.btn_start);

            tvStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(tvStart.getText().toString().compareToIgnoreCase("Start") == 0)
                    {
                        sessionLocation = new ArrayList<LatLng>();
                        remoteConnection = new RemoteConnection();
                        Intent intent = new Intent(thisActivity, FitnessService.class);
                        intent.setAction(AidlFitnessService.class.getName());
                        //intent.setClassName("com.tin.proj_fit.services", com.tin.proj_fit.services.FitnessService.class.getName());
                        if(!bindService(intent, remoteConnection, Context.BIND_AUTO_CREATE))
                        {
                            tvRS.setText("Failed to bind Remote Service");
                        }
                        else
                        {
                            tvRS.setText("RS binded");
                        }

                        tvStart.setText("Stop");
                        isInSession = true;
                        Toast.makeText(thisActivity, "Start training", Toast.LENGTH_SHORT).show();
                        try {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.1f, thisActivity);
                            System.out.println("REGISTER LOCATION UPDATE");
                            initalLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            curLat = initalLocation.getLatitude();
                            curLng = initalLocation.getLongitude();
                            System.out.println("Init Location: " + initalLocation.getLatitude() + initalLocation.getLongitude());
                        }catch(SecurityException e){
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        tvStart.setText("Start");
                        isInSession = false;
                        Cursor cursor = db.getData();
                        int cnt = 0;
                        while(cursor.moveToNext())
                            cnt++;
                        System.out.println("Total Session entries: " + cnt);
                        Toast.makeText(thisActivity, "Stop training", Toast.LENGTH_SHORT).show();

                        //SESSION END Save each session, add the session to user
                        WorkoutSession workoutSession = new WorkoutSession(0, curSecond / 1000, 0);

//                        user.getAllTimeList().add(workoutSession);
//                        user.getWeeklyList().add(workoutSession);

                        if(remoteConnection != null) {
                            unbindService(remoteConnection);
                            remoteConnection = null;
                        }
                        try {
                            locationManager.removeUpdates(thisActivity);
                        }catch(SecurityException e){
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

            if(curSecond > 1000)
            {
                tvDuration.setText(hms);
            }

        }
    }

    private void checkPermission()
    {
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
        }
        else
        {

        }
    }

    @Override
    public void onLocationChanged(Location location)
    {

        if(isInSession) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 18));
//            System.out.println("Lat-Lng: " + lat + "," + lng);
            tvLat.setText(String.valueOf(lat));
            tvLong.setText(String.valueOf(lng));
            try {
                if (rService != null)
                {
                    sessionLocation.add(new LatLng(lat, lng));
                    rService.putData(lat, lng);
                    System.out.println("rService.debugPrint() " + rService.debugPrint(lat, lng));
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            updateDistance(curLat, curLng, lat, lng);
            updateDuration();
            updatePath();
        }
    }

    private void updatePath()
    {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(sessionLocation);
        polylineOptions.color(Color.GREEN);
        polylineOptions.width(5);
        mMap.clear();
        mMap.addPolyline(polylineOptions);
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        for (LatLng latLng : sessionLocation) {
//            builder.include(latLng);
//        }
//        final LatLngBounds bounds = builder.build();
//        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 15);
//        mMap.animateCamera(cu);
    }

    private void updateDistance(double curLat, double curLng, double lat, double lng)
    {

    }

    private void updateDuration()
    {
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

        if(isInSession)
        {
            tvStart.setText("Stop");
        }
        else
        {
            tvStart.setText("Start");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(remoteConnection != null) {
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
            if(initLoc != null)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(initLoc.getLatitude(), initLoc.getLongitude()),
                        18));
            mMap.setMyLocationEnabled(true);
        }catch(SecurityException e)
        {
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

    public void showProfileActivity(View view)
    {
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
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
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

}
