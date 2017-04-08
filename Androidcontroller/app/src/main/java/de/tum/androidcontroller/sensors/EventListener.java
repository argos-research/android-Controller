package de.tum.androidcontroller.sensors;

import de.tum.androidcontroller.data.SensorData;

/**
 * Drives the changes from the sensors. For more info
 * please visit https://developer.android.com/guide/topics/sensors/sensors_overview.html
 */

public interface EventListener {

    /**
     * The accelerometer measures the acceleration force in m/s2 that
     * is applied to a device on all three physical axes (x, y, and z),
     * including the force of gravity.
     * @param data the data provided from the sensors listener in m/s2
     */
    void onAccelerometerChanged(SensorData data);

    /**
     * Measures a device's rate of rotation in rad/s around each of
     * the three physical axes (x, y, and z).
     * @param data the data provided from the sensors listener in rad/s
     */
    void onGyroChanged(SensorData data);

    /**
     * Measures the acceleration force in m/s2 that is applied to a
     * device on all three physical axes (x, y, and z),
     * excluding the force of gravity.
     * @param data the data provided from the sensors listener in m/s2
     */
    void onLinearAccelerometerChanged(SensorData data);

    /**
     * Measures the ambient geomagnetic field for all
     * three physical axes (x, y, z) in μT.
     * @param data the data provided from the sensors listener in μT
     */
    void onMagneticFieldChanged(SensorData data);

    /**
     * Measures the orientation of a device by providing the
     * three elements of the device's rotation vector.
     * @param data the data provided from the sensors listener as a number
     */
    void onRotationVectorChanged(SensorData data);

}
