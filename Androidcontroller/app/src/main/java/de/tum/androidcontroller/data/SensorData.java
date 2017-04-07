package de.tum.androidcontroller.data;

import android.hardware.SensorEvent;

/**
 * Created by konstantin on 07/04/17.
 */

public class SensorData {
    private float x;
    private float y;
    private float z;

    public SensorData(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public static SensorData toSensorData(SensorEvent event){
        return new SensorData(event.values[0],event.values[1],event.values[2]);
    }
}
