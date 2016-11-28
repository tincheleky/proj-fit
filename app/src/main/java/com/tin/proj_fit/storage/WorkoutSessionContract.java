package com.tin.proj_fit.storage;

import android.provider.BaseColumns;

/**
 * Created by mbp on 11/27/16.
 */

public class WorkoutSessionContract
{
    private WorkoutSessionContract(){}

    public static class WorkoutSessionEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "workout_session";
        public static final String COLUMN_NAME_DIS = "distance";
        public static final String COLUMN_NAME_DUR = "duration";
        public static final String COLUMN_NAME_CALO = "calo";
    }
}
