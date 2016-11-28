package com.tin.proj_fit.models;

import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * Created by mbp on 11/28/16.
 */

public class SensorsHelper
{
    static SensorManager sensorManager;
    static Sensor stepCounterSensor;

    public SensorsHelper(SensorManager sensorManager, Sensor stepCounterSensor) {
        this.sensorManager = sensorManager;
        this.stepCounterSensor = stepCounterSensor;
    }

    public Sensor getStepCounterSensor() {
        return stepCounterSensor;
    }

    public void setStepCounterSensor(Sensor stepCounterSensor) {
        this.stepCounterSensor = stepCounterSensor;
    }

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }
}
