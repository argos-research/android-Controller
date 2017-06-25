package de.tum.androidcontroller.connections.packets;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by chochko on 05/05/17.
 */

public class UDPSendPacket extends Packet  {


    private final String TAG = "UDPSendPacket";

    private DatagramPacket packet;

    public UDPSendPacket(String threadName, String msg, DatagramSocket socketUDP, Context context) {
        super(threadName, msg, socketUDP, context);
        try {
            packet = new DatagramPacket(msg.getBytes("UTF-8"),msg.length());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Constructor: unable to get decode the bytes from this message '"+msg+"' to UTF-8...");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run(); //give the thread a name

        try {
            getSocketUDP().send(packet);
        } catch (IOException e) {
            String error = "Unable to send the datagram packet with the given UDP socket... Please check if the server is running with the given IP and port.";
            Log.e(TAG, "run: " + error);
            e.printStackTrace();
            super.sendBroadcastOnFailure(error);
        } catch (NullPointerException np){
            String error = "The connection with the UDP server is closed so skipping the send of " + super.getMsg();
            Log.e(TAG, error);
            np.printStackTrace();
            super.sendBroadcastOnFailure(error); //inform the main activity for this interruption.
        }
    }

}
