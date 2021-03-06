package com.tin.proj_fit.activities;

import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.vision.text.Text;
import com.tin.proj_fit.R;
import com.tin.proj_fit.models.User;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class ProfileActivity extends AppCompatActivity
{
    public static AutoCompleteTextView tvGender;
    public static EditText etName;
    public static EditText etWeight;
    public static TextView tvAvgDistance;
    public static TextView tvAvgTime;
    public static TextView tvAvgSessionCounts;
    public static TextView tvAvgCaloBurnt;
    public static TextView tvAllTimeDistance;
    public static TextView tvAllTimeTime;
    public static TextView tvAllTimeSessionCounts;
    public static TextView tvAllTimeCaloBurnt;
    DecimalFormat decimalFormat = new DecimalFormat("#0.000");

    public static User user;
    SharedPreferences sharedPreferences;

    private static final String[] GENDERS = new String[] {
            "Male", "Female"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if(getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);


        sharedPreferences = getSharedPreferences(FitnessActivity.PREFERENCE, MODE_PRIVATE);
        user = FitnessActivity.user;

        etName = (EditText) findViewById(R.id.user_name);
        etWeight = (EditText) findViewById(R.id.user_weight);
        tvGender = (AutoCompleteTextView) findViewById(R.id.user_gender);
        tvAvgDistance = (TextView) findViewById(R.id.user_avg_distance);
        tvAvgTime = (TextView) findViewById(R.id.user_avg_time);
        tvAvgSessionCounts = (TextView) findViewById(R.id.user_avg_workouts);
        tvAvgCaloBurnt = (TextView) findViewById(R.id.user_avg_cal);

        tvAllTimeDistance = (TextView) findViewById(R.id.user_alltime_distance);
        tvAllTimeTime = (TextView) findViewById(R.id.user_alltime_time);
        tvAllTimeSessionCounts = (TextView) findViewById(R.id.user_alltime_workouts);
        tvAllTimeCaloBurnt = (TextView) findViewById(R.id.user_alltime_cal);

        etName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String input;
                if(i == EditorInfo.IME_ACTION_DONE)
                {
                    input= textView.getText().toString();
                    if(input.length() > 0) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("name", input);
                        editor.commit();
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
                    }
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });

        etWeight.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String input;
                if(i == EditorInfo.IME_ACTION_DONE)
                {
                    input= textView.getText().toString();
                    if(input.length() > 0) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("weight", Integer.valueOf(input));
                        editor.commit();
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etWeight.getWindowToken(), 0);
                    }
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, GENDERS);
        tvGender.setAdapter(adapter);

        tvGender.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                String input = (String) adapterView.getItemAtPosition(i);
                if(input.length() > 0) {
                    tvGender.setText(input);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("gender", input);
                    editor.commit();
                }
            }
        });

        tvGender.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String input;
                if(i == EditorInfo.IME_ACTION_DONE)
                {
                    input= textView.getText().toString();
                    if(input.length() > 0) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("gender", input);
                        editor.commit();
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(tvGender.getWindowToken(), 0);
                    }
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });

        if(user.getUserName() != null)
        {
            etName.setText(user.getUserName());
        }

        if(user.getWeight() > 1)
        {
            etWeight.setText(String.valueOf(user.getWeight()));
        }

        if(user.getGender() != null)
        {
            tvGender.setText(user.getGender());
        }

        updateProfileActivity();

    }


    private void updateProfileActivity()
    {
        if (ProfileActivity.tvAvgDistance != null) {
            ProfileActivity.tvAvgDistance.setText(decimalFormat.format(user.getWeeklyDistance()) + " km");
        }

        if (ProfileActivity.tvAvgTime != null) {
            ProfileActivity.tvAvgTime.setText(formatTime(user.getWeeklyDuration()));
        }

        if(ProfileActivity.tvAvgCaloBurnt != null)
        {
            int avgCalo = user.getWeeklyCaloBurnt();
            ProfileActivity.tvAvgCaloBurnt.setText(String.valueOf(avgCalo));
        }

        if(ProfileActivity.tvAvgSessionCounts != null)
        {
            ProfileActivity.tvAvgSessionCounts.setText(String.valueOf(user.getWeeklyList().size()));
        }

        if(ProfileActivity.tvAllTimeDistance != null)
        {
            double alltimeDistance = user.getAllTimeDistance();
            ProfileActivity.tvAllTimeDistance.setText(decimalFormat.format(alltimeDistance) + " km");
        }

        if(ProfileActivity.tvAllTimeTime != null)
        {
            long alltimeValue = user.getAllTimeDuration();
            ProfileActivity.tvAllTimeTime.setText(formatTime(alltimeValue));

        }

        if(ProfileActivity.tvAllTimeCaloBurnt != null)
        {
            int alltimeCalo = user.getAllTimeCaloBurnt();
            ProfileActivity.tvAllTimeCaloBurnt.setText(String.valueOf(alltimeCalo));
        }

        if(ProfileActivity.tvAllTimeSessionCounts != null)
        {
            ProfileActivity.tvAllTimeSessionCounts.setText(String.valueOf(user.getAllTimeList().size()));
        }
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

    private String formatTime (long time)
    {
        String temp = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));

        return temp;
    }
}
