package de.tum.androidcontroller.models;

import android.hardware.SensorEvent;

/**
 * This class will be used for saving the sonsor data
 */

public class SensorBaseModel {

    private float x;
    private float y;
    private float z;

    public SensorBaseModel(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SensorBaseModel(float x, float y, float z, int decimalDigits){
    //Optional
//        this.x = MathUtils.toDigitsClean(x,decimalDigits);
//        this.y = MathUtils.toDigitsClean(y,decimalDigits);
//        this.z = MathUtils.toDigitsClean(z,decimalDigits);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }


    public float getY() {
        return y;
    }


    public float getZ() {
        return z;
    }


    public static SensorBaseModel toSensorData(SensorEvent event, int decimalDigits){
        return new SensorBaseModel(event.values[0],event.values[1],event.values[2],decimalDigits);
    }
}
