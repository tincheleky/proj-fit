package com.tin.proj_fit.models;

/**
 * Created by mbp on 11/27/16.
 */

public class WorkoutSession
{
    double distance;
    long duration;
    int caloriesBurnt;

    public WorkoutSession(double distance, long duration, int caloriesBurnt) {
        this.distance = distance;
        this.duration = duration;
        this.caloriesBurnt = caloriesBurnt;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getCaloriesBurnt() {
        return caloriesBurnt;
    }

    public void setCaloriesBurnt(int caloriesBurnt) {
        this.caloriesBurnt = caloriesBurnt;
    }
}
