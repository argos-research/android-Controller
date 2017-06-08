package de.tum.androidcontroller.sensors;

import android.os.Vibrator;

/**
 * https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate
 */

public class Vibration {

    private final long VIBRATION_DURATION   = 100;
    private final long VIBRATION_SLEEP      = 100;


    // Each element then alternates between vibrate, sleep, vibrate, sleep and start without waiting. This pattern makes it to vibrate twice
    private final long[] VIBRATION_PATTERN  = {0,VIBRATION_DURATION, VIBRATION_SLEEP, VIBRATION_DURATION, VIBRATION_SLEEP};

    private Vibrator mVibrator = null;

    private static Vibration ourInstance = null;

    public static Vibration getInstance(Vibrator vibrator) {
        if(ourInstance == null){
            ourInstance = new Vibration(vibrator);
        }

        return ourInstance;
    }

    private Vibration(Vibrator vibrator) {
        this.mVibrator  = vibrator;
    }

    public void onPositionChangedVibration(){
        if (mVibrator.hasVibrator()) {
            // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
            mVibrator.vibrate(VIBRATION_PATTERN, -1);
        }
    }
}
