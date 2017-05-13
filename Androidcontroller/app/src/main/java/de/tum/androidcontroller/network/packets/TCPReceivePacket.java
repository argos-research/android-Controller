package de.tum.androidcontroller.network.packets;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by chochko on 05/05/17.
 */

public class TCPReceivePacket extends Packet{

    private InputStream mIn;
    private final String TAG = "TCPReceivePacket";

    public TCPReceivePacket(String msg, Socket socket) {
        super(msg, socket);
        try{
            mIn = super.getSocketTCP().getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Unable to get the input stream!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(getSocketTCP() != null) {
            if (getSocketTCP().isConnected()) {
                Log.e(TAG, String.format("Listening for data on port %d...", super.getSocketTCP().getPort()));
                try {
                    byte inputBuffer[] = new byte[1024];
                    int bytes_read = mIn.read(inputBuffer);
                    String received = new String(inputBuffer, 0, bytes_read);
                    Log.e(TAG, "Message received from the server: " + received);

                } catch (IOException e) {
                    Log.e(TAG, "Unable to read from the input stream or the connection is closed!");
                    e.printStackTrace();
                }
            }
        }
    }
}
