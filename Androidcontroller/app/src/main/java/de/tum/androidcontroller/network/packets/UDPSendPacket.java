package de.tum.androidcontroller.network.packets;

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

    public UDPSendPacket(String msg, DatagramSocket socketUDP) {
        super(msg, socketUDP);
        try {
            packet = new DatagramPacket(msg.getBytes("UTF-8"),msg.length());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Constructor: unable to get decode the byts from this message '"+msg+"' to UTF-8...");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            getSocketUDP().send(packet);
        } catch (IOException e) {
            Log.e(TAG, "run: Unable to send the datagram packet with the given UDP socket...");
            e.printStackTrace();
        } catch (NullPointerException np){
            Log.e(TAG, "The connection with the UDP server is closed so skipping the send of " + super.getMsg());
        }
    }

}
