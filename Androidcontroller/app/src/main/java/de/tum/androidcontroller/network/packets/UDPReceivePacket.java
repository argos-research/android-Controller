package de.tum.androidcontroller.network.packets;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by chochko on 05/05/17.
 */

public class UDPReceivePacket extends Packet {

    private final String TAG = "UDPReceivePacket";

    public UDPReceivePacket(String threadName, DatagramSocket socketUDP) {
        super(threadName, "", socketUDP);

    }

    /**
     * Receives a packet from this socket and stores it in the argument pack.
     * All fields of pack must be set according to the data received. If the
     * received data is longer than the packet buffer size it is truncated.
     * This method blocks until a packet is received or a timeout has expired.
     * <i>from http://stackoverflow.com/questions/19540715/send-and-receive-data-on-udp-socket-java-android</i>
     */
    @Override
    public void run() {
        super.run(); //give the thread a name

        while (getSocketUDP() != null) {
            if(getSocketUDP().isConnected()) {

                Log.e(TAG, String.format("Listening for data on port %d...", super.getSocketUDP().getPort()));
                try {

                    byte inputBuffer[] = new byte[256];
                    DatagramPacket packet = new DatagramPacket(inputBuffer, inputBuffer.length);

                    getSocketUDP().receive(packet); //this code block the program flow

                    String received = new String(inputBuffer).trim();
                    Log.e(TAG, "Message received from the server: " + received);

                } catch (IOException e) {
                    Log.e(TAG, "Unable to read this UDP socket or the connection is closed!");
                    e.printStackTrace();
                    break;
                }
            }
        }

    }
}
