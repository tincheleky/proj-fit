package com.tin.proj_fit.models;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by mbp on 11/27/16.
 */

public class User
{
    String userName;
    String gender;
    int weight;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    ArrayList<WorkoutSession> weeklyList;
    ArrayList<WorkoutSession> allTimeList;

    public User() {
        weeklyList = new ArrayList<>();
        allTimeList = new ArrayList<>();
    }



    public double getWeeklyDistance()
    {
        double sum = 0.0;

        for(WorkoutSession workoutSession :  weeklyList)
        {
            sum += workoutSession.getDistance();
        }

        return sum;
    }

    public double getAllTimeDistance()
    {
        double sum = 0.0;

        for(WorkoutSession workoutSession :  allTimeList)
        {
            sum += workoutSession.getDistance();
        }

        return sum;
    }



    public long getWeeklyDuration()
    {
        long sum = 0;

        for(WorkoutSession workoutSession :  weeklyList)
        {
            sum += workoutSession.getDuration();
        }

        return sum;
    }

    public long getAllTimeDuration()
    {
        long sum = 0;

        for(WorkoutSession workoutSession :  allTimeList)
        {
            sum += workoutSession.getDuration();
        }

        return sum;
    }

    public int getWeeklyCaloBurnt()
    {
        int sum = 0;

        for(WorkoutSession workoutSession :  weeklyList)
        {
            sum += workoutSession.getCaloriesBurnt();
        }

        return sum;
    }

    public int getAllTimeCaloBurnt()
    {
        int sum = 0;

        for(WorkoutSession workoutSession :  allTimeList)
        {
            sum += workoutSession.getCaloriesBurnt();
        }

        return sum;
    }

    public ArrayList<WorkoutSession> getWeeklyList()
    {
        return weeklyList;
    }

    public ArrayList<WorkoutSession> getAllTimeList()
    {
        return allTimeList;
    }

    public int getWeeklyWorkoutSession()
    {
        return weeklyList.size();
    }

    public int getAllTimeWorkoutSession()
    {
        return allTimeList.size();
    }
}
