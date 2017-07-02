package de.tum.androidcontroller.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import de.tum.androidcontroller.models.SensorBaseModel;


/**
 * This class is used for taking the sensor data
 */

public class SensorModel implements SensorListener, SensorEventListener {

    private EventListener mEventListener;
    private Sensor mAccelerometer;
    private Sensor mGyro;
    private Sensor mLinearAccelerometer;
    private Sensor mMagneticField;
    private Sensor mRotationVector;
    private Context mContext;
    private static SensorModel mInstance;
    private final int DATA_DECIMAL_DIGITS = 2;


    public static synchronized SensorModel getInstance(Context co){
        if(mInstance == null)
            mInstance = new SensorModel(co);
        return mInstance;
    }

    private SensorModel(Context co){
        this.mContext = co.getApplicationContext();
        //make sure you call this class only from the main activity! TODO fix this
        this.mEventListener = (EventListener) co;
    }

    private void initSensors(SensorManager sm){
        mAccelerometer                  =  sm.getDefaultSensor(SensorTypes.accelerometer);
        mGyro                           =  sm.getDefaultSensor(SensorTypes.gyroscope);
        //mLinearAccelerometer            =  sm.getDefaultSensor(SensorTypes.linearAccelerometer);
        //mMagneticField                  =  sm.getDefaultSensor(SensorTypes.magneticField);
        //mRotationVector                 =  sm.getDefaultSensor(SensorTypes.rotationVector);
    }

    @Override
    public void onResume(SensorManager sm) {
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
        //sm.registerListener(this,mLinearAccelerometer,SensorManager.SENSOR_DELAY_GAME);
        //sm.registerListener(this,mMagneticField,SensorManager.SENSOR_DELAY_GAME);
        //sm.registerListener(this,mRotationVector,SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause(SensorManager sm) {
        sm.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if(sensor.getType() == SensorTypes.gyroscope){
            mEventListener.onGyroChanged(SensorBaseModel.toSensorData(event,DATA_DECIMAL_DIGITS,false));
        }
        else if(sensor.getType() == SensorTypes.accelerometer){
            mEventListener.onAccelerometerChanged(SensorBaseModel.toSensorData(event,DATA_DECIMAL_DIGITS,true));
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static final class SensorTypes{
        private static final int accelerometer = Sensor.TYPE_ACCELEROMETER;
        private static final int gyroscope = Sensor.TYPE_GYROSCOPE;
        private static final int linearAccelerometer = Sensor.TYPE_LINEAR_ACCELERATION;
        private static final int magneticField = Sensor.TYPE_MAGNETIC_FIELD;
        private static final int rotationVector = Sensor.TYPE_ROTATION_VECTOR;
    }






}
