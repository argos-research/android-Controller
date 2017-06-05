package de.tum.androidcontroller.connections.utils;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by chochko on 05.06.17.
 */

public class BluetoothUtils {

    public static boolean isBluetoothEnabled() throws Exception {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            throw new Exception("This device does not support bluetooth! Try with different communication technology!");
        } else {
            return mBluetoothAdapter.isEnabled();
        }
    }
}
