package com.tin.proj_fit.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.tin.proj_fit.storage.WorkoutSessionContract;
import com.tin.proj_fit.storage.WorkoutSessionDbHelper;

import java.util.HashMap;

public class WorkoutSessionContentProvider extends ContentProvider {

    public static String TAG = WorkoutSessionContentProvider.class.getSimpleName();
    static final String PROVIDER = "com.tin.proj_fit.providers.WorkoutSessionContentProvider";
    static final String URL = "content://" + PROVIDER + "/workouts";
    public static final Uri URI = Uri.parse(URL);

    public static final String _ID = "_id";
    public static final String DISTANCE = "distance";
    public static final String DURATION = "duration";
    public static final String CALORIES = "calories";

    static final int WORKOUTS = 1;
    static final int WORKOUT_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER, "workouts", WORKOUTS);
        uriMatcher.addURI(PROVIDER, "workouts/#", WORKOUT_ID);
    }

    Context mContext;

    private static HashMap<String, String> WORKOUTS_PROJECTION_MAP;

    private SQLiteDatabase db;
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WorkoutSession.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INT_TYPE = " INTEGER";
    private static final String TABLE_NAME = WorkoutSessionContract.WorkoutSessionEntry.TABLE_NAME;

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_WORKOUT_SESSION =
            "CREATE TABLE " + WorkoutSessionContract.WorkoutSessionEntry.TABLE_NAME + " (" +
                    WorkoutSessionContract.WorkoutSessionEntry._ID + " INTEGER PRIMARY KEY," +
                    DISTANCE + REAL_TYPE + COMMA_SEP +
                    DURATION + INT_TYPE + COMMA_SEP +
                    CALORIES + INT_TYPE + " )";

    private static final String SQL_DELETE_WORKOUT_SESSION =
            "DROP TABLE IF EXISTS " + WorkoutSessionContract.WorkoutSessionEntry.TABLE_NAME;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_WORKOUT_SESSION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  WorkoutSessionContract.WorkoutSessionEntry.TABLE_NAME);
            onCreate(db);
        }
    }

    public WorkoutSessionContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case WORKOUTS:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;

            case WORKOUT_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( TABLE_NAME, _ID +  " = " + id +
                                (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all student records
             */
            case WORKOUTS:
                return "vnd.android.cursor.dir/vnd.tin.proj_fit.workouts";
            /**
             * Get a particular student
             */
            case WORKOUT_ID:
                return "vnd.android.cursor.item/vnd.tin.proj_fit.workouts";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new student record
         */
        long rowID = db.insert(WorkoutSessionContract.WorkoutSessionEntry.TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }



    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(TABLE_NAME);

            switch (uriMatcher.match(uri)) {
                case WORKOUTS:
                    qb.setProjectionMap(WORKOUTS_PROJECTION_MAP);
                    break;

                case WORKOUT_ID:
                    qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                    break;

                default:
            }

            if (sortOrder == null || sortOrder == ""){
                /**
                 * By default sort on student names
                 */
                sortOrder = _ID;
            }

            Cursor c = qb.query(db,	projection,	selection,
                    selectionArgs,null, null, sortOrder);
            /**
             * register to watch a content URI for changes
             */
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case WORKOUTS:
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;

            case WORKOUT_ID:
                count = db.update(TABLE_NAME, values,
                        _ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
