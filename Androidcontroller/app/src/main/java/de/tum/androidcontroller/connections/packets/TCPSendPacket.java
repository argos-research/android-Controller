package de.tum.androidcontroller.connections.packets;

import android.content.Context;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by chochko on 05/05/17.
 */

public class TCPSendPacket extends Packet{

    //sending the TCP packets without to close the connection after each packet

    private final String TAG = "TCPSendPacket";

    public TCPSendPacket(String threadName,String msg, Socket socket, Context context) {
        super(threadName, msg, socket,context);
        try{
            super.setOutputStream(super.getSocketTCP().getOutputStream());
        }catch (IOException e){
            Log.e(TAG, "Unable to get the output stream!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run(); //give the thread a name


        DataOutputStream out = new DataOutputStream(super.getOutputStream());
        try{
            out.writeUTF(super.getMsg());
            out.flush();
        }catch (IOException e){
            String error = "Unable to write on the TCP output stream! Please check if the server is running with the given IP and port.";
            Log.e(TAG,error,e);
            super.sendBroadcastOnFailure(error);
        }catch (NullPointerException np){
            String error = "The connection with the TCP server is closed so skipping the send of " + super.getMsg();
            Log.e(TAG, error,np);
            super.sendBroadcastOnFailure(error); //inform the main activity for this interruption.
        }
    }
}
