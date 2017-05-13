package de.tum.androidcontroller.network.packets;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by chochko on 05/05/17.
 */

public class UDPReceivePacket extends Packet {

    private final String TAG = "UDPReceivePacket";

    public UDPReceivePacket() {
    }

    @Override
    public void run() {
        while (getSocketUDP() != null) {
            if(getSocketUDP().isConnected()) {

                Log.e(TAG, String.format("Listening for data on port %d...", super.getSocketUDP().getPort()));
                try {
                    byte inputBuffer[] = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(inputBuffer, inputBuffer.length);

                    getSocketUDP().receive(packet); //this code block the program flow

                    String received = new String(inputBuffer); //TODO try this approach to the TCP
                    Log.e(TAG, "Message received from the server: " + received);

                } catch (IOException e) {
                    Log.e(TAG, "Unable to read this UDP socket or the connection is closed!");
                    e.printStackTrace();
                }
            }
        }

    }
}
