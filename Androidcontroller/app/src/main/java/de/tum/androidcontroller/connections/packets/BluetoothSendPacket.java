package de.tum.androidcontroller.connections.packets;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

/**
 * Created by chochko on 16/05/17.
 */

public class BluetoothSendPacket extends Packet {


    private final String TAG = "BluetoothSendPacket";

    public BluetoothSendPacket(String threadName, String msg, BluetoothSocket socketBt, Context context) {
        super(threadName, msg, socketBt, context);

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
            String error = "Unable to write on the Bluetooth output stream! Have you connected to the bluetooth server?";
            Log.e(TAG, "run: " + error);

            super.sendBroadcastOnFailure(error);

            e.printStackTrace();
        } catch (NullPointerException np){
            String error = "The connection with the Bluetooth server is closed so skipping the send of " + super.getMsg();
            Log.e(TAG, error);
            np.printStackTrace();
            super.sendBroadcastOnFailure(error); //inform the main activity for this interruption.
        }
    }
}
