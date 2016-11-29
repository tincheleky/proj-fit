package com.tin.proj_fit.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.tin.proj_fit.AidlFitnessService;
import com.tin.proj_fit.activities.FitnessActivity;
import com.tin.proj_fit.models.WorkoutSession;
import com.tin.proj_fit.storage.LocationData;
import com.tin.proj_fit.storage.LocationHistoryDbHelper;
import com.tin.proj_fit.storage.WorkoutDataDbHelper;

/**
 * Created by mbp on 11/19/16.
 */

public class FitnessService extends Service
{

    WorkoutDataDbHelper db;
    AidlFitnessService.Stub mBinder;

    public FitnessService()
    {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = new WorkoutDataDbHelper(this);
        mBinder = new AidlFitnessService.Stub() {
            @Override
            public void putData(double distance, long duration, int calories) throws RemoteException {
                System.out.println("Remote Service put data: " + distance + ", " + duration + ", " + calories);
                if(db != null) {
                    db.putData(new WorkoutSession(distance, duration, calories));
                }

            }
            @Override
            public String debugPrint(double distance, long duration, int calories)
            {
                return "Get Called " + distance + ", " + duration + ", " + calories;
            }

            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

            }
        };
    }

//    @Override
//    public boolean onUnbind(Intent intent) {
//        return super.onUnbind(intent);
//    }
}
