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

public class WorkoutDataDbHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WorkoutData.db";
    private static WorkoutDataDbHelper instance;

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INT_TYPE = " INTEGER";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_WORKOUT_DATA =
            "CREATE TABLE " + WorkoutDataContract.WorkoutDataEntry.TABLE_NAME + " (" +
                    WorkoutDataContract.WorkoutDataEntry._ID + " INTEGER PRIMARY KEY," +
                    WorkoutDataContract.WorkoutDataEntry.COLUMN_NAME_DIS + REAL_TYPE + COMMA_SEP +
                    WorkoutDataContract.WorkoutDataEntry.COLUMN_NAME_DUR + INT_TYPE + COMMA_SEP +
                    WorkoutDataContract.WorkoutDataEntry.COLUMN_NAME_CALO + INT_TYPE + " )";

    private static final String SQL_DELETE_WORKOUT_DATA =
            "DROP TABLE IF EXISTS " + WorkoutDataContract.WorkoutDataEntry.TABLE_NAME;



    public WorkoutDataDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        instance = this;
    }

    public static WorkoutDataDbHelper getInstance()
    {
        return instance;
    }

    public static boolean putData(WorkoutSession data)
    {
        // Gets the data repository in write mode
        SQLiteDatabase db = instance.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(WorkoutDataContract.WorkoutDataEntry.COLUMN_NAME_DIS, data.getDistance());
        values.put(WorkoutDataContract.WorkoutDataEntry.COLUMN_NAME_DUR, data.getDuration());
        values.put(WorkoutDataContract.WorkoutDataEntry.COLUMN_NAME_CALO, data.getCaloriesBurnt());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(WorkoutDataContract.WorkoutDataEntry.TABLE_NAME, null, values);

        return newRowId != -1;
    }

    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                WorkoutDataContract.WorkoutDataEntry._ID,
                WorkoutDataContract.WorkoutDataEntry.COLUMN_NAME_DIS,
                WorkoutDataContract.WorkoutDataEntry.COLUMN_NAME_DUR,
                WorkoutDataContract.WorkoutDataEntry.COLUMN_NAME_CALO
        };


        Cursor cursor = db.query(
                WorkoutDataContract.WorkoutDataEntry.TABLE_NAME,         // The table to query
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
        db.execSQL(SQL_CREATE_WORKOUT_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQL_DELETE_WORKOUT_DATA);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
