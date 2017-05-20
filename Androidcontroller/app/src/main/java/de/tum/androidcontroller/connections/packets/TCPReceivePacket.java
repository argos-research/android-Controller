package de.tum.androidcontroller.connections.packets;

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

    public TCPReceivePacket(String threadName, Socket socket) {
        super(threadName, "", socket);
        try{
            mIn = super.getSocketTCP().getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Unable to get the input stream!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run(); //give the thread a name

        while(getSocketTCP() != null) {
            if (getSocketTCP().isConnected()) {
                Log.e(TAG, String.format("Listening for data on port %d...", super.getSocketTCP().getPort()));
                try {
                    byte inputBuffer[] = new byte[1024];
                    int bytes_read = mIn.read(inputBuffer);
                    String received = new String(inputBuffer, 0, bytes_read);
                    Log.e(TAG, "Message received from the server: " + received);

                } catch (IOException e) {
                    String error = "Unable to read from the input stream or the connection is closed!";
                    Log.e(TAG, error);
                    e.printStackTrace();
                    super.setErrorInformation(error); //provide it here for the SocketConnectionThread
                    break;
                } catch (StringIndexOutOfBoundsException s){
                    String error = "The server has closed the connection/socket!";
                    Log.e(TAG, error);
                    s.printStackTrace();
                    super.setErrorInformation(error); //provide it here for the SocketConnectionThread //TODO use callback instead
                    break;
                }
            }
        }
    }
}
