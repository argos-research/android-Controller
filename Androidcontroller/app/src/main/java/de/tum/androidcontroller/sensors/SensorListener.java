package de.tum.androidcontroller.sensors;

import android.hardware.SensorManager;

/**
 * Created by konstantin on 07/04/17.
 */

public interface SensorListener {
    public void onResume(SensorManager sm);
    public void onPause(SensorManager sm);
}
