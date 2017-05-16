package de.tum.androidcontroller.network;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.ThreadFactory;

import de.tum.androidcontroller.network.models.PacketsModel;

/**
 * Created by chochko on 05/05/17.
 */

class ConnectionThreadFactory implements ThreadFactory {

    private volatile boolean LOGGING = true;

    private Type type = null;

    /**
     * The used types for recognizing the current thread name
     * that should be given to a runnable. This is needed beacuse of
     * the <b>afterExecute</b> method in {@link SocketConnectionThread}
     * for distinguishing between the different Threads created within
     * this {@link ThreadFactory}.
     */
    enum Type{
        InitUDPCommunication,
        UDPSend,
        UDPReceive,

        InitTCPCommunication,
        TCPSend,
        TCPReceive,

        InitBluetoothCommunication,
        BluetoothSend,
        BluetoothReceive,

        CloseSomeCommunication,

        CallbackThread
    }

    private final String TAG = "ConnectionThreadFactory";

    @Override
    public Thread newThread(@NonNull Runnable r) {
        if(getType() == null)
            throw new IllegalArgumentException("Specify type argument!");

        /*
         * TCP part
         */
        else if(getType() == Type.InitTCPCommunication){
            setType(null);
            if(LOGGING)
                Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_TCP_INIT);
            return new Thread(r,PacketsModel.RUNNABLE_NAME_TCP_INIT);
        }

        else if(getType() == Type.TCPReceive){
             setType(null);
            if(LOGGING)
                Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_TCP_RECEIVE);
             return new Thread(r,PacketsModel.RUNNABLE_NAME_TCP_RECEIVE);
        }

        else if(getType() == Type.TCPSend){
             setType(null);
             if(LOGGING)
                Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_TCP_SEND);
             return new Thread(r,PacketsModel.RUNNABLE_NAME_TCP_SEND);
        }

        /*
         * UDP part
         */
        else if(getType() == Type.InitUDPCommunication) {
            setType(null);
            if(LOGGING)
                Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_UDP_INIT);
            return new Thread(r, PacketsModel.RUNNABLE_NAME_UDP_INIT);
        }

        else if(getType() == Type.UDPReceive){
             setType(null);
             if(LOGGING)
                Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_UDP_RECEIVE);
             return new Thread(r,PacketsModel.RUNNABLE_NAME_UDP_RECEIVE);
        }

        else if(getType() == Type.UDPSend){
             setType(null);
             if(LOGGING)
                Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_UDP_SEND);
             return new Thread(r,PacketsModel.RUNNABLE_NAME_UDP_SEND);
        }


        /*
         * Bluetooth part
         */
        else if(getType() == Type.InitBluetoothCommunication){
            setType(null);
            if(LOGGING)
                Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_BT_INIT);
            return  new Thread(r, PacketsModel.RUNNABLE_NAME_BT_INIT);
        }

        else if(getType() == Type.BluetoothReceive){
            setType(null);
            if(LOGGING)
                Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_BT_RECEIVE);
            return  new Thread(r, PacketsModel.RUNNABLE_NAME_BT_RECEIVE);
        }

        else if(getType() == Type.BluetoothSend){
            setType(null);
            if(LOGGING)
                Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_BT_SEND);
            return  new Thread(r, PacketsModel.RUNNABLE_NAME_BT_SEND);
        }



        else if(getType() == Type.CloseSomeCommunication){
            setType(null);
            if(LOGGING)
                Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_CLOSE_CONNECTION);
            return  new Thread(r, PacketsModel.RUNNABLE_NAME_CLOSE_CONNECTION);
        }


        /**
         * Used for the callback interface in {@link SocketConnectionThread}
         */
        else if(getType() == Type.CallbackThread){
            setType(null);
            if(LOGGING)
                Log.e(TAG, "newThread: " + PacketsModel.RUNNABLE_NAME_CALLBACK);
            return  new Thread(r, PacketsModel.RUNNABLE_NAME_CALLBACK);
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
