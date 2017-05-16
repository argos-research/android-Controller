package de.tum.androidcontroller.network.packets;

import android.bluetooth.BluetoothSocket;

import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 * Created by chochko on 05/05/17.
 */

abstract class Packet implements Runnable {
    private String msg = ""; //the msg that should be send

    private Socket socketTCP; //used for the TCP communication

    private DatagramSocket socketUDP; //used for the UDP communication

    private BluetoothSocket socketBt;   //used for the Bluetooth communication

    private volatile String runningInformation; //used for passing additional information to the main activity

    private OutputStream outputStream;

    Packet(){}

    Packet(String msg, DatagramSocket socketUDP){
        this.msg        = msg;
        this.socketUDP  = socketUDP;
    }

    Packet(String msg, Socket socketTCP){
        this.msg        = msg;
        this.socketTCP  = socketTCP;
    }

    Packet(String msg, BluetoothSocket socketBt){
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

    String getRunningInformation() {
        return runningInformation;
    }

    void setRunningInformation(String runningInformation) {
        this.runningInformation = runningInformation;
    }

    OutputStream getOutputStream() {
        return outputStream;
    }

    void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

}
