package de.tum.androidcontroller.sensors;

import android.graphics.Color;


/**
 * Here will be stored the default values for controlling
 * the game with the sensors.
 */

public class SensorDataSettings {

    // +++++++++++++ For the Acceleration sensor +++++++++++++++++++++
    //The maximum sensor value change
    public static float maximumAccelerationBreakDeviation   = 5.f;
    public static float maximumLeftRightDeviation           = 5.f;

    //Those values are used for default phone position where
    //nothing should happen (meaning the controller is in idle state)
    public static float idleAccelerationBreakState          = 0.f;
    public static float idleLeftRightState                  = 0.f;


    public static final int COLOR_ACCELERATION  = Color.GREEN;
    public static final int COLOR_BREAKING      = Color.RED;
    public static final int COLOR_LEFT          = Color.CYAN;
    public static final int COLOR_RIGHT         = Color.BLUE;

    //the minimum change that should trigger the acceleration/breaking change

    //shows the steps we can have for different acc values
    public static final int MAXIMUM_ACCELEROMETER_STEPS                 = 10;

    //public static final float MINIMUM_CHANGE_TRIGGER_ACCELERATION_BREAK = 0.4f;
    // The minimum change that should trigger the acceleration/breaking change according to the send value type length
    public static final float MINIMUM_CHANGE_TRIGGER_ACCELERATION_BREAK = maximumAccelerationBreakDeviation /  ((float)MAXIMUM_ACCELEROMETER_STEPS);
    //the minimum change that should trigger the steering change according to the send value type length
    //public static final float MINIMUM_CHANGE_TRIGGER_STEERING = 0.2f;
    public static final float MINIMUM_CHANGE_TRIGGER_STEERING           = maximumLeftRightDeviation / ((float)MAXIMUM_ACCELEROMETER_STEPS);

    // --------------- For the Acceleration sensor ------------------------


    // +++++++++++++++++++++ For the Gyro sensor +++++++++++++++++++++++++++
    //the minimum change that should trigger the forward/backward gyro change
    public static final float MINIMUM_CHANGE_TRIGGER_GYRO_FORWARD_BACKWARD  = 7.f;
    //the minimum change that should trigger the steering change
    public static final float MINIMUM_CHANGE_TRIGGER_GYRO_LEFT_RIGHT        = 5.f;

    //the minimum amount of time in milliseconds for triggering new gyro change event on the same axis
    //this value takes care of sending gyro data once per that amount of time
    public static final long MINIMUM_TIME_TO_WAIT_GYRO                      = 300;

    // --------------------- For the Gyro sensor ---------------------------

}