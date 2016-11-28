package com.tin.proj_fit.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tin.proj_fit.models.WorkoutSession;

/**
 * Created by mbp on 11/27/16.
 */

public class WorkoutSessionDbHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WorkoutSession.db";
    private static WorkoutSessionDbHelper instance;

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INT_TYPE = " INT";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_WORKOUT_SESSION =
            "CREATE TABLE " + WorkoutSessionContract.WorkoutSessionEntry.TABLE_NAME + " (" +
                    WorkoutSessionContract.WorkoutSessionEntry._ID + " INTEGER PRIMARY KEY," +
                    WorkoutSessionContract.WorkoutSessionEntry.COLUMN_NAME_DIS + REAL_TYPE + COMMA_SEP +
                    WorkoutSessionContract.WorkoutSessionEntry.COLUMN_NAME_DUR + INT_TYPE + COMMA_SEP +
                    WorkoutSessionContract.WorkoutSessionEntry.COLUMN_NAME_CALO + INT_TYPE + " )";

    private static final String SQL_DELETE_WORKOUT_SESSION =
            "DROP TABLE IF EXISTS " + WorkoutSessionContract.WorkoutSessionEntry.TABLE_NAME;



    public WorkoutSessionDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        instance = this;
    }

    public static WorkoutSessionDbHelper getInstance()
    {
        return instance;
    }

    public static boolean putData(WorkoutSession data)
    {
        // Gets the data repository in write mode
        SQLiteDatabase db = instance.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(WorkoutSessionContract.WorkoutSessionEntry.COLUMN_NAME_DIS, data.getDistance());
        values.put(WorkoutSessionContract.WorkoutSessionEntry.COLUMN_NAME_DUR, data.getDuration());
        values.put(WorkoutSessionContract.WorkoutSessionEntry.COLUMN_NAME_CALO, data.getCaloriesBurnt());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(WorkoutSessionContract.WorkoutSessionEntry.TABLE_NAME, null, values);

        return newRowId != -1;
    }

    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                WorkoutSessionContract.WorkoutSessionEntry._ID,
                WorkoutSessionContract.WorkoutSessionEntry.COLUMN_NAME_DIS,
                WorkoutSessionContract.WorkoutSessionEntry.COLUMN_NAME_DUR,
                WorkoutSessionContract.WorkoutSessionEntry.COLUMN_NAME_CALO
        };


        Cursor cursor = db.query(
                WorkoutSessionContract.WorkoutSessionEntry.TABLE_NAME,         // The table to query
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
        db.execSQL(SQL_CREATE_WORKOUT_SESSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQL_DELETE_WORKOUT_SESSION);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
