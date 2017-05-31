package de.tum.androidcontroller.connections.packets;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by chochko on 05/05/17.
 */

public class UDPReceivePacket extends Packet {

    private final String TAG = "UDPReceivePacket";

    public UDPReceivePacket(String threadName, DatagramSocket socketUDP, Context context) {
        super(threadName, "", socketUDP, context);

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

                    super.sendBroadcastOnReceive(received);

                } catch (IOException e) {
                    String error = "Unable to read this UDP socket or the connection is closed!";
                    Log.e(TAG, error,e);
                    super.sendBroadcastOnFailure(error); //inform the main activity for this interruption.
                    break;
                }
            }
        }

    }
}
