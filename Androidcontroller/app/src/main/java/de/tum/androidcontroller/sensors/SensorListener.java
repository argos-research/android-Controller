package de.tum.androidcontroller.sensors;

import android.hardware.SensorManager;

/**
 * Created by konstantin on 07/04/17.
 */

public interface SensorListener {
    public void onStart(SensorManager sm);
    public void onDestroy(SensorManager sm);
}
