package com.tin.proj_fit.storage;

import android.provider.BaseColumns;

/**
 * Created by mbp on 11/12/16.
 */

public final class LocationHistoryContract
{
    private LocationHistoryContract(){}

    public static class LocationEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "location_history";
        public static final String COLUMN_NAME_LAT = "latitude";
        public static final String COLUMN_NAME_LON = "longitude";
        public static final String COLUMN_NAME_TIME = "time";
    }

}

