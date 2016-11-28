package com.tin.proj_fit.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mbp on 11/12/16.
 */

public class LocationHistoryDbHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "LocationHistory.db";
    private static LocationHistoryDbHelper instance;

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_LOCATION_HISTORY =
            "CREATE TABLE " + LocationHistoryContract.LocationEntry.TABLE_NAME + " (" +
                    LocationHistoryContract.LocationEntry._ID + " INTEGER PRIMARY KEY," +
                    LocationHistoryContract.LocationEntry.COLUMN_NAME_LAT + REAL_TYPE + COMMA_SEP +
                    LocationHistoryContract.LocationEntry.COLUMN_NAME_LON + REAL_TYPE + COMMA_SEP +
                    LocationHistoryContract.LocationEntry.COLUMN_NAME_TIME + TEXT_TYPE + " )";

    private static final String SQL_DELETE_LOCATION_HISTORY =
            "DROP TABLE IF EXISTS " + LocationHistoryContract.LocationEntry.TABLE_NAME;



    public LocationHistoryDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        instance = this;
    }

    public static LocationHistoryDbHelper getInstance()
    {
        return instance;
    }

    public static boolean putData(LocationData data)
    {
        // Gets the data repository in write mode
        SQLiteDatabase db = instance.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationHistoryContract.LocationEntry.COLUMN_NAME_LAT, data.getLatitude());
        values.put(LocationHistoryContract.LocationEntry.COLUMN_NAME_LON, data.getLongitude());
        values.put(LocationHistoryContract.LocationEntry.COLUMN_NAME_TIME, data.getTime());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(LocationHistoryContract.LocationEntry.TABLE_NAME, null, values);

        return newRowId != -1;
    }

    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                LocationHistoryContract.LocationEntry._ID,
                LocationHistoryContract.LocationEntry.COLUMN_NAME_LAT,
                LocationHistoryContract.LocationEntry.COLUMN_NAME_LON,
                LocationHistoryContract.LocationEntry.COLUMN_NAME_TIME
        };


        Cursor cursor = db.query(
                LocationHistoryContract.LocationEntry.TABLE_NAME,         // The table to query
                projection,                                               // The columns to return
                null,                                                     // The columns for the WHERE clause
                null,                                                     // The values for the WHERE clause
                null,                                                     // don't group the rows
                null,                                                     // don't filter by row groups
                null                                                      // The sort order
        );

        return cursor;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_LOCATION_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQL_DELETE_LOCATION_HISTORY);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
