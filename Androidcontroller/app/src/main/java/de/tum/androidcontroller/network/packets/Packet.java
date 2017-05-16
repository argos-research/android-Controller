package de.tum.androidcontroller.network.packets;

import android.bluetooth.BluetoothSocket;

import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 * Created by chochko on 05/05/17.
 */

public class Packet implements Runnable {
    private String msg = ""; //the msg that should be send

    private volatile String threadName = "";

    private Socket socketTCP; //used for the TCP communication

    private DatagramSocket socketUDP; //used for the UDP communication

    private BluetoothSocket socketBt;   //used for the Bluetooth communication

    private volatile String errorInformation =""; //used for passing additional information to the main activity

    private OutputStream outputStream;

    /**
     * Constructor for {@link Packet}.
     * @param threadName the name that the running thread should have when executing this runnable
     */
    public Packet(String threadName){
        this.threadName = threadName;
    }

    /**
     * Constructor for {@link Packet}.
     * @param threadName the name that the running thread should have when executing this runnable
     * @param msg the message that should be send to the server or a empty String if nothing should be send
     * @param socketUDP the instance of an initialized UDP {@link DatagramSocket}.
     */
    public Packet(String threadName, String msg,  DatagramSocket socketUDP){
        this.threadName = threadName;
        this.msg        = msg;
        this.socketUDP  = socketUDP;
    }

    /**
     * Constructor for {@link Packet}.
     * @param threadName the name that the running thread should have when executing this runnable
     * @param msg the message that should be send to the server or a empty String if nothing should be send
     * @param socketTCP the instance of an initialized TCP {@link Socket}.
     */
    public Packet(String threadName, String msg, Socket socketTCP){
        this.threadName = threadName;
        this.msg        = msg;
        this.socketTCP  = socketTCP;
    }

    /**
     * Constructor for {@link Packet}.
     * @param threadName the name that the running thread should have when executing this runnable
     * @param msg the message that should be send to the server or a empty String if nothing should be send
     * @param socketBt the instance of an initialized {@link BluetoothSocket}.
     */
    public Packet(String threadName, String msg, BluetoothSocket socketBt){
        this.threadName = threadName;
        this.msg        = msg;
        this.socketBt   = socketBt;
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

    public String getErrorInformation() {
        return errorInformation;
    }

    void setErrorInformation(String errorInformation) {
        this.errorInformation = errorInformation;
    }

    OutputStream getOutputStream() {
        return outputStream;
    }

    void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }


    private String getThreadName() {
        return threadName;
    }

    @Override
    public void run(){
        Thread.currentThread().setName(getThreadName());
    }
}
