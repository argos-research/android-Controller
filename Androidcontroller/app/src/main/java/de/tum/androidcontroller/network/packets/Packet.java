package de.tum.androidcontroller.network.packets;

import java.net.Socket;

/**
 * Created by chochko on 05/05/17.
 */

public abstract class Packet {
    private String msg = ""; //the msg that should be send

    private Socket socket; //used for the TCP communication


    public Packet(String msg){
        this.msg = msg;
    }

    Packet(String msg, Socket socket){
        this.msg    = msg;
        this.socket = socket;
    }

    Socket getSocket() {
        return socket;
    }

    String getMsg(){
        return msg;
    }

    public abstract void run();

}
