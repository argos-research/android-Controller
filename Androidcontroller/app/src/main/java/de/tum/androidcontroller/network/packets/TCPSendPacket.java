package de.tum.androidcontroller.network.packets;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by chochko on 05/05/17.
 */

public class TCPSendPacket extends Packet{

    //sending the TCP packets without to close the connection after each packet

    //private OutputStream mOut; //TODO move this to the Packet class

    private final String TAG = "TCPSendPacket";

    public TCPSendPacket(String msg, Socket socket) {
        super(msg, socket);
        try{
            super.setOutputStream(super.getSocketTCP().getOutputStream());
        }catch (IOException e){
            Log.e(TAG, "Unable to get the output stream!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        DataOutputStream out = new DataOutputStream(super.getOutputStream());
        try{
            out.writeUTF(super.getMsg());
            out.flush();
        }catch (IOException e){
            Log.e(TAG, "Unable to write on the output stream!");
            e.printStackTrace();
        }catch (NullPointerException np){
            Log.e(TAG, "The connection with the TCP server is closed so skipping the send of " + super.getMsg());
        }
    }
}
