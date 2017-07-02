package de.tum.androidcontroller.connections.packets;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;

import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Socket;

import de.tum.androidcontroller.connections.models.ConnectionRunnableModels;
import de.tum.androidcontroller.connections.models.ReceivedDataModel;

/**
 * This is the parent class of every packet sent/received from the server.
 * Each communication technology packet should implement this class.
 */

public class Packet implements Runnable {
    private String msg = ""; //the msg that should be send

    private volatile String threadName = "";

    private Socket socketTCP; //used for the TCP communication

    private DatagramSocket socketUDP; //used for the UDP communication

    private BluetoothSocket socketBt;   //used for the Bluetooth communication

    private OutputStream outputStream;

    private volatile Context context;    // used for sending broadcast events

    private volatile Intent broadcastIntent = new Intent();

   private final boolean LOGGING = false;

    /**
     * Constructor for {@link Packet}.
     * @param threadName the name that the running thread should have when executing this runnable
     */
    protected Packet(String threadName){
        this.threadName = threadName;
    }

    /**
     * Constructor for {@link Packet}.
     * @param threadName the name that the running thread should have when executing this runnable
     * @param msg the message that should be send to the server or a empty String if nothing should be send
     * @param socketUDP the instance of an initialized UDP {@link DatagramSocket}.
     * @param context the context used for sending different broadcast events
     */
    Packet(String threadName, String msg,  DatagramSocket socketUDP, Context context){
        this.threadName = threadName;
        this.msg        = msg;
        this.socketUDP  = socketUDP;
        this.context    = context;
    }

    /**
     * Constructor for {@link Packet}.
     * @param threadName the name that the running thread should have when executing this runnable
     * @param msg the message that should be send to the server or a empty String if nothing should be send
     * @param socketTCP the instance of an initialized TCP {@link Socket}.
     * @param context the context used for sending different broadcast events
     */
    Packet(String threadName, String msg, Socket socketTCP, Context context){
        this.threadName = threadName;
        this.msg        = msg;
        this.socketTCP  = socketTCP;
        this.context    = context;
    }

    /**
     * Constructor for {@link Packet}.
     * @param threadName the name that the running thread should have when executing this runnable
     * @param msg the message that should be send to the server or a empty String if nothing should be send
     * @param socketBt the instance of an initialized {@link BluetoothSocket}.
     * @param context the context used for sending different broadcast events
     */
    Packet(String threadName, String msg, BluetoothSocket socketBt, Context context){
        this.threadName = threadName;
        this.msg        = msg;
        this.socketBt   = socketBt;
        this.context    = context;
    }

    Socket getSocketTCP() {
        return socketTCP;
    }


    DatagramSocket getSocketUDP(){
        return socketUDP;
    }


    BluetoothSocket getSocketBluetooth(){
        return socketBt;
    }


    String getMsg(){
        return msg;
    }


    OutputStream getOutputStream() {
        return outputStream;
    }

    boolean isLOGGING() {
        return LOGGING;
    }

    void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

//    public synchronized Context getContext(){
//        return this.context;
//    }

    private String getThreadName() {
        return threadName;
    }


    /**
     * Send a broadcast event to the {@link de.tum.androidcontroller.activities.MainActivity}
     * to handle each given failure (connection with the server interrupted).
     *
     * @param errorInformation more detailed information of the reason for the connection interruption
     */
    synchronized void sendBroadcastOnFailure(String errorInformation){
        Log.e("sendBroadcastOnFailure", "sendBroadcastOnFailure: ");
        broadcastIntent.setAction(ConnectionRunnableModels.BROADCAST_ACTION_FAILURE);
        broadcastIntent.putExtra(ConnectionRunnableModels.BROADCAST_INFORMATION_KEY,errorInformation);
        this.context.sendBroadcast(broadcastIntent);
    }

    /**
     * Send a broadcast event to the {@link de.tum.androidcontroller.activities.MainActivity}
     * to handle the received {@link de.tum.androidcontroller.connections.models.ReceivedDataModel}
     * from the server.
     *
     * @param receivedJSON the received {@link de.tum.androidcontroller.connections.models.ReceivedDataModel}
     *                     JSON representation as a String.
     */
    synchronized void sendBroadcastOnReceive(String receivedJSON){
        //Log.e("sendBroadcastOnReceive", "sendBroadcastOnReceive: "+receivedJSON);

        try {
            broadcastIntent.putExtra(ConnectionRunnableModels.BROADCAST_INFORMATION_KEY, new ReceivedDataModel(receivedJSON));
            broadcastIntent.setAction(ConnectionRunnableModels.BROADCAST_ACTION_RECEIVE);
        } catch (JSONException e) {
            Log.e("sendBroadcastOnReceive", "ERROR: received JSON is " + receivedJSON,e);
            //e.printStackTrace();
            broadcastIntent.setAction(ConnectionRunnableModels.BROADCAST_ACTION_FAILURE);
            broadcastIntent.putExtra(ConnectionRunnableModels.BROADCAST_INFORMATION_KEY, e.getMessage());
        }
        this.context.sendBroadcast(broadcastIntent);
    }

    /**
     * Because of the different chanel encoding sometimes there are chars at the beginning ot the
     * input channel which should be deleted
     *
     * @param inputString the received JSON string
     * @return
     */
    String extractJSON(String inputString){
        inputString = inputString.trim();
        for(int i = 0; i < inputString.length() ; i++){
            if(inputString.charAt(i) == '{'){
                return inputString.substring(i,inputString.length());
            }
        }
        return "";
    }

    /**
     * This should be implemented from each class which is extending this one.
     * You have to always let the method set the Thread name to the
     * specified one in order in {@link de.tum.androidcontroller.connections.SocketConnectionThread}
     * to be handled properly after execution.
     */
    @Override
    public void run(){
        Thread.currentThread().setName(getThreadName());
    }
}
