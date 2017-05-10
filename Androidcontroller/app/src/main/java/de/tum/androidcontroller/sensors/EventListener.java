package de.tum.androidcontroller.sensors;

import de.tum.androidcontroller.models.SensorModel;

/**
 * Drives the changes from the sensors. For more info
 * please visit https://developer.android.com/guide/topics/sensors/sensors_overview.html
 */

public interface EventListener {

    /**
     * The accelerometer measures the acceleration force in m/s2 that
     * is applied to a device on all three physical axes (x, y, and z),
     * including the force of gravity.
     * Here the X axis will be used for acceleration(on forward rotation
     * the value decreases) and braking(keep decreasing) and the Y axis for steering.
     * @param data the data provided from the sensors listener in m/s2
     */
    void onAccelerometerChanged(SensorModel data);

    /**
     * Measures a device's rate of rotation in rad/s around each of
     * the three physical axes (x, y, and z).
     * For the app are only the X and Y axis important, where Y measures forward and
     * backward fast movements ( negative values for backward and positive for forward)
     * and the X axis measures the left/right fast movements (negative means left and
     * positive means right).
     * @param data the data provided from the sensors listener in rad/s
     */
    void onGyroChanged(de.tum.androidcontroller.models.SensorModel data);

    /**
     * Measures the acceleration force in m/s2 that is applied to a
     * device on all three physical axes (x, y, and z),
     * excluding the force of gravity.
     * @param data the data provided from the sensors listener in m/s2
     */
    //void onLinearAccelerometerChanged(SensorModel data);

    /**
     * Measures the ambient geomagnetic field for all
     * three physical axes (x, y, z) in μT.
     * @param data the data provided from the sensors listener in μT
     */
    //void onMagneticFieldChanged(SensorModel data);

    /**
     * Measures the orientation of a device by providing the
     * three elements of the device's rotation vector.
     * @param data the data provided from the sensors listener as a number
     */
    //void onRotationVectorChanged(SensorModel data);

}
