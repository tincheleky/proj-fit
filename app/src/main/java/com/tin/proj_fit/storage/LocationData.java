package com.tin.proj_fit.storage;

/**
 * Created by mbp on 11/12/16.
 */

public final class LocationData
{
    private double latitude;
    private double longitude;
    private String time;

    public LocationData(double latitude, double longitude, String time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTime() {
        return time;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
