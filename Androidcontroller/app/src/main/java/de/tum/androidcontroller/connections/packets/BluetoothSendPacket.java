package de.tum.androidcontroller.connections.packets;

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
            super.setErrorInformation(""); //provide it here for the SocketConnectionThread
        } catch (IOException e) {
            String error = "Unable to send the packet with the given Bluetooth socket... Please check if the server is running with the given IP and port.";
            Log.e(TAG, "run: " + error);
            super.setErrorInformation("Unable to write on the Bluetooth output stream! Have you connected to the bluetooth server?");
            e.printStackTrace();
        } catch (NullPointerException np){
            String error = "The connection with the Bluetooth server is closed so skipping the send of " + super.getMsg();
            Log.e(TAG, error);
            np.printStackTrace();
            super.setErrorInformation(error); //provide it here for the SocketConnectionThread
        }
    }
}
