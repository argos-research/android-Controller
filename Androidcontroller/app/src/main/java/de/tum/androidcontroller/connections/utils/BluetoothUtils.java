package de.tum.androidcontroller.connections.utils;

import android.bluetooth.BluetoothAdapter;
import android.support.annotation.NonNull;


/**
 * Created by chochko on 05.06.17.
 */

public class BluetoothUtils {

    /**
     * Checks if the device has enabled bluetooth.
     * @return true if the Bluetooth is enabled or false otherwise.
     * @throws Exception if the device doesn't support bluetooth.
     */
    public static boolean isBluetoothEnabled() throws Exception {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            throw new Exception("This device does not support bluetooth! Try with different communication technology!");
        } else {
           return mBluetoothAdapter.isEnabled();
        }
    }

    /**
     * Change the format of a normal String to actual MAC string separated with ':'.
     * In example if you provide "303A64D23E93" you will get "30:3A:64:D2:3E:93".
     *
     * @param address some MAC address in not MAC format (without ':').
     * @return the MAC address separated with ':'.
     */
    @NonNull
    public static String toMACFormat(String address){
        char divisionChar = ':';
        return address.replaceAll("(.{2})", "$1"+divisionChar).substring(0,17);
    }
}
