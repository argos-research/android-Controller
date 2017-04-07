package de.tum.androidcontroller.sensors;

import de.tum.androidcontroller.data.SensorData;

/**
 * Created by konstantin on 07/04/17.
 */

public interface EventListener {

    void onAccelerometerChanged(SensorData data);
    void onGyroChanged(SensorData data);
}
