package de.tum.androidcontroller.models;

import android.hardware.SensorEvent;

import de.tum.androidcontroller.sensors.SensorDataSettings;

/**
 * This class will be used for saving the sensor data for each axis.
 */

public class SensorBaseModel {

    private float x;
    private float y;
    private float z;

    private boolean isAccelerometerData;

    private long lastTimeDatSend = 0; //the time when this base model data was sent to the server. For I am using it only for the gyro data

    public SensorBaseModel(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SensorBaseModel(float x, float y, float z, int decimalDigits, boolean isAccelerometerData){
    //Optional
//        this.x = MathUtils.toDigitsClean(x,decimalDigits);
//        this.y = MathUtils.toDigitsClean(y,decimalDigits);
//        this.z = MathUtils.toDigitsClean(z,decimalDigits);
        /*
        Prevent the values to exceed their maxim because in the scenario when there is a change in the Y
        axis and this event should be send to the server but in the same time the X value is way above
        SensorDataSettings.maximumAccelerationBreakDeviation, then the big X axis value will be send
        and this will be outside the range. Thus, it will supply the linux machine with false values
        which will have negative effect on calibrating the virtual joystick with the Speed Dreams 2
        because there is the maximum values produced by the sensors are needed.
         */
        this.isAccelerometerData = isAccelerometerData;

        this.x = correctValueX(x);
        this.y = correctValueY(y);
        this.z = z;
    }

    //as described in the constructor...
    private float correctValueX(float x){
        if (isAccelerometerData) {
            if(x > 0){
                return x > SensorDataSettings.maximumAccelerationBreakDeviation ? SensorDataSettings.maximumAccelerationBreakDeviation : x;
            }else if(x < 0){
                return x < - SensorDataSettings.maximumAccelerationBreakDeviation ? - SensorDataSettings.maximumAccelerationBreakDeviation : x;
            }else
                return 0;
        } else {
            //dont change the gyro values!
            return x;
        }
    }

    //as described in the constructor...
    private float correctValueY(float y){
        if (isAccelerometerData) {
            if(y > 0){
                return y > SensorDataSettings.maximumLeftRightDeviation ? SensorDataSettings.maximumLeftRightDeviation : y;
            }else if(y < 0){
                return y < - SensorDataSettings.maximumLeftRightDeviation ? - SensorDataSettings.maximumLeftRightDeviation : y;
            }else
                return 0;
        } else {
            //dont change the gyro values!
            return y;
        }
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

    /**
     * A data transformation method for changing data from {@link SensorEvent}
     * to custom {@link SensorBaseModel} data.
     * @param event the taken event from the {@link android.hardware.SensorManager}.
     * @param decimalDigits the number precision after the coma. Currently it is not used.
     * @param isAccelerometerData whether the provided event is accelerometer data. True
     *                            means accelerometer data and false gyro data.
     * @return an ignace of {@link SensorBaseModel}
     *
     * @see SensorBaseModel
     */
    public static SensorBaseModel toSensorData(SensorEvent event, int decimalDigits, boolean isAccelerometerData){
        return new SensorBaseModel(event.values[0],event.values[1],event.values[2],decimalDigits, isAccelerometerData);
    }

    public String toString(){
        return String.format("X: %.3f\tY: %.3f\tZ: %.3f",x,y,z);
    }


}
