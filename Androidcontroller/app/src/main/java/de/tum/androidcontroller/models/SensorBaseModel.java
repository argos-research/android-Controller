package de.tum.androidcontroller.models;

import android.hardware.SensorEvent;

/**
 * This class will be used for saving the sensor data for each axis.
 */

public class SensorBaseModel {

    private float x;
    private float y;
    private float z;

    private long lastTimeDatSend = 0; //the time when this base model data was sent to the server. For I am using it only for the gyro data

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

    public long getLastTimeDatSend() {
        return lastTimeDatSend;
    }

    public void setLastTimeDatSend(long lastTimeDatSend) {
        this.lastTimeDatSend = lastTimeDatSend;
    }

    public static SensorBaseModel toSensorData(SensorEvent event, int decimalDigits){
        return new SensorBaseModel(event.values[0],event.values[1],event.values[2],decimalDigits);
    }

    public String toString(){
        return String.format("X: %.3f\tY: %.3f\tZ: %.3f",x,y,z);
    }


}
