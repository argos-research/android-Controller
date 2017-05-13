package de.tum.androidcontroller.network.packets;

import java.net.DatagramSocket;
import java.net.Socket;

/**
 * Created by chochko on 05/05/17.
 */

abstract class Packet implements Runnable {
    private String msg = ""; //the msg that should be send

    private Socket socketTCP; //used for the TCP communication

    private DatagramSocket socketUDP;


    Packet(){}

    Packet(String msg){
        this.msg = msg;
    }

    Packet(String msg, DatagramSocket socketUDP){
        this.msg        = msg;
        this.socketUDP  = socketUDP;
    }

    Packet(String msg, Socket socketTCP){
        this.msg        = msg;
        this.socketTCP  = socketTCP;
    }

    Socket getSocketTCP() {
        return socketTCP;
    }

    DatagramSocket getSocketUDP(){
        return socketUDP;
    }

    String getMsg(){
        return msg;
    }

}
