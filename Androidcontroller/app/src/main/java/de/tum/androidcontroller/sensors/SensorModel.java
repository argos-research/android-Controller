package de.tum.androidcontroller.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import de.tum.androidcontroller.data.SensorData;

/**
 * Created by konstantin on 07/04/17.
 */

public class SensorModel implements SensorListener, SensorEventListener {

    private EventListener mEventListener;
    private Sensor mAccelerometer;
    private Sensor mGyro;
    private Context mContext;
    private static SensorModel mInstance;


    public static synchronized SensorModel getInstance(Context co){
        if(mInstance == null)
            mInstance = new SensorModel(co);
        return mInstance;
    }

    private SensorModel(Context co){
        this.mContext = co.getApplicationContext();
        this.mEventListener = (EventListener) co;
    }

    private void initSensors(SensorManager sm){
        mAccelerometer  =  sm.getDefaultSensor(SensorTypes.accelerometer);
        mGyro           =  sm.getDefaultSensor(SensorTypes.gyroscope);
    }

    @Override
    public void onStart(SensorManager sm) {
        initSensors(sm);
        /*
        * It is not necessary to get accelerometer events at a very high
        * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
        * automatic low-pass filter, which "extracts" the gravity component
        * of the acceleration. As an added benefit, we use less power and
        * CPU resources.
        */
        sm.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(this,mGyro,SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onDestroy(SensorManager sm) {
        sm.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if(sensor.getType() == SensorTypes.accelerometer){
            mEventListener.onAccelerometerChanged(SensorData.toSensorData(event));
        }else if(sensor.getType() == SensorTypes.gyroscope){
            mEventListener.onGyroChanged(SensorData.toSensorData(event));
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static final class SensorTypes{
        private static final int accelerometer = Sensor.TYPE_ACCELEROMETER;
        private static final int gyroscope = Sensor.TYPE_GYROSCOPE;
    }






}
