package de.tum.androidcontroller.connections.packets;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chochko on 16/05/17.
 */

public class BluetoothReceivePacket extends Packet {

    private InputStream mIn;
    private final String TAG = "BluetoothReceivePacket";

    public BluetoothReceivePacket(String threadName, BluetoothSocket socketBt, Context context) {
        super(threadName, "", socketBt, context);

        try{
            mIn = super.getSocketBluetooth().getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Unable to get the input stream!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run(); //give the thread a name

        while(getSocketBluetooth() != null) {
            if (getSocketBluetooth().isConnected()) {
                Log.e(TAG, String.format("Listening for data from server %s running on %s...", super.getSocketBluetooth().getRemoteDevice().getName(),super.getSocketBluetooth().getRemoteDevice().getAddress()));
                try {
                    byte inputBuffer[] = new byte[1024];
                    int bytes_read = mIn.read(inputBuffer);
                    String received = new String(inputBuffer, 0, bytes_read);
                    Log.e(TAG, "Message received from the server: " + received);

                    super.sendBroadcastOnReceive(received);

                } catch (IOException e) {
                    String error = "Unable to read from the input stream or the connection is closed!";
                    Log.e(TAG, error,e);
                    super.sendBroadcastOnFailure(error); //inform the main activity for this interruption.
                    break;
                }
            }
        }

    }
}
