package com.tin.proj_fit.activities;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;
import com.tin.proj_fit.R;

public class ProfileActivity extends AppCompatActivity
{
    public static TextView tvAvgDistance;
    public static TextView tvAvgTime;
    public static TextView tvAvgSessionCounts;
    public static TextView tvAvgCaloBurnt;
    public static TextView tvAllTimeDistance;
    public static TextView tvAllTimeTime;
    public static TextView tvAllTimeSessionCounts;
    public static TextView tvAllTimeCaloBurnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if(getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);

        tvAvgDistance = (TextView) findViewById(R.id.user_avg_distance);
        tvAvgTime = (TextView) findViewById(R.id.user_avg_time);
        tvAvgSessionCounts = (TextView) findViewById(R.id.user_avg_workouts);
        tvAvgCaloBurnt = (TextView) findViewById(R.id.user_avg_cal);

        tvAllTimeDistance = (TextView) findViewById(R.id.user_alltime_distance);
        tvAllTimeTime = (TextView) findViewById(R.id.user_alltime_time);
        tvAllTimeSessionCounts = (TextView) findViewById(R.id.user_alltime_workouts);
        tvAllTimeCaloBurnt = (TextView) findViewById(R.id.user_alltime_cal);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
