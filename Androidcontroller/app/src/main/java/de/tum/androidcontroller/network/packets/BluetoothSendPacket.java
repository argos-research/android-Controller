package de.tum.androidcontroller.network.packets;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * Created by chochko on 16/05/17.
 */

public class BluetoothSendPacket extends Packet {


    private final String TAG = "BluetoothSendPacket";

    public BluetoothSendPacket(String threadName,String msg, BluetoothSocket socketBt) {
        super(threadName, msg, socketBt);

        try{
            super.setOutputStream(super.getSocketBluetooth().getOutputStream());
        }catch (IOException e){
            Log.e(TAG, "Unable to get the output stream!");
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        super.run(); //give the thread a name

        try {
            super.getOutputStream().write(super.getMsg().getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Unable to write on the output stream!");
            //super.setRunningInformation("Unable to write on the output stream! Are you connected to the bluetooth server?");
            e.printStackTrace();
        }
    }
}
