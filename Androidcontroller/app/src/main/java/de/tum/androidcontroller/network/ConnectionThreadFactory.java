package de.tum.androidcontroller.network;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.ThreadFactory;

import de.tum.androidcontroller.network.models.PacketsModel;

/**
 * Created by chochko on 05/05/17.
 */

class ConnectionThreadFactory implements ThreadFactory {



    private Type type = null;
    enum Type{
        UDPSend,
        UDPReceive,
        TCPSend,
        TCPReceive,
        InitTCPCommunication,
        InitUDPCommunication,
        CloseSomeCommunication
    }

    private final String TAG = "ConnectionThreadFactory";

    @Override
    public Thread newThread(@NonNull Runnable r) {
        if(getType() == null)
            throw new IllegalArgumentException("Specify type argument!");
        else if(getType() == Type.TCPReceive){
             setType(null);
             Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_TCP_RECEIVE);
             return new Thread(r,PacketsModel.RUNNABLE_NAME_TCP_RECEIVE);
        }else if(getType() == Type.TCPSend){
             setType(null);
             Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_TCP_SEND);
             return new Thread(r,PacketsModel.RUNNABLE_NAME_TCP_SEND);
        }else if(getType() == Type.UDPReceive){
             setType(null);
             Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_UDP_RECEIVE);
             return new Thread(r,PacketsModel.RUNNABLE_NAME_UDP_RECEIVE);
        }else if(getType() == Type.UDPSend){
             setType(null);
             Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_UDP_SEND);
             return new Thread(r,PacketsModel.RUNNABLE_NAME_UDP_SEND);
        }else if(getType() == Type.InitTCPCommunication){
            setType(null);
            Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_TCP_INIT);
            return new Thread(r,PacketsModel.RUNNABLE_NAME_TCP_INIT);
        }else if(getType() == Type.CloseSomeCommunication){
            setType(null);
            return  new Thread(r, PacketsModel.RUNNABLE_NAME_TCP_CLOSE);
        }
        else {
            throw new IllegalArgumentException("Unknown type argument!");
        }
    }


    private Type getType() {
        return type;
    }

    void setType(Type type) {
        this.type = type;
    }
}
