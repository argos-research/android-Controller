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
    public static final float MINIMUM_CHANGE_TRIGGER_ACCELERATION_BREAK = 0.4f;
    //the minimum change that should trigger the steering change
    public static final float MINIMUM_CHANGE_TRIGGER_STEERING = 0.2f;
    // --------------- For the Acceleration sensor ------------------------


    // +++++++++++++++++++++ For the Gyro sensor +++++++++++++++++++++++++++
    //the minimum change that should trigger the forward/backward gyro change
    public static final float MINIMUM_CHANGE_TRIGGER_GYRO_FORWARD_BACKWARD = 7.f;
    //the minimum change that should trigger the steering change
    public static final float MINIMUM_CHANGE_TRIGGER_GYRO_LEFT_RIGHT = 5.f;

    // --------------------- For the Gyro sensor ---------------------------

}
