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

    private OutputStream mOut;

    private final String TAG = "TCPSendPacket";

    public TCPSendPacket(String msg, Socket socket) {
        super(msg, socket);
        try{
            mOut = super.getSocket().getOutputStream();
        }catch (IOException e){
            Log.e(TAG, "Unable to get the output stream!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        DataOutputStream out = new DataOutputStream(mOut);
        try{
            out.writeUTF(super.getMsg());
            out.flush();
        }catch (IOException e){
            Log.e(TAG, "Unable to write on the output stream!");
            e.printStackTrace();
        }
    }
}
