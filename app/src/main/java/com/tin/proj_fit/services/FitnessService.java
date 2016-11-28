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
import com.tin.proj_fit.storage.LocationData;
import com.tin.proj_fit.storage.LocationHistoryDbHelper;

/**
 * Created by mbp on 11/19/16.
 */

public class FitnessService extends Service
{

    LocationHistoryDbHelper db;
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
        db = new LocationHistoryDbHelper(this);
        mBinder = new AidlFitnessService.Stub() {
            @Override
            public void putData(double lat, double lng) throws RemoteException {
                System.out.println("Remote Service put data: " + lat + ", " + lng);
                if(db != null) {
                    FitnessActivity.db.putData(new LocationData(lat, lng, "time"));
                }

            }
            @Override
            public String debugPrint(double lat, double lng)
            {
                System.out.println("Get Called " + lat + lng);
                return "Get Called " + lat + lng;
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
