package de.tum.androidcontroller.data;

import android.hardware.SensorEvent;
import android.icu.text.DecimalFormat;

import de.tum.androidcontroller.utils.MathUtils;

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

    public SensorData(float x, float y, float z, int decimalDigits){
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


    public static SensorData toSensorData(SensorEvent event, int decimalDigits){
        return new SensorData(event.values[0],event.values[1],event.values[2],decimalDigits);
    }
}
