package de.tum.androidcontroller.network.Packets;

/**
 * Created by chochko on 05/05/17.
 */

public abstract class Packet implements Runnable {
    private String msg = "";

    public Packet(String msg){
        this.msg = msg;
    }

    public String getMsg(){
        return msg;
    }

    public void setMsg(String newMsg){
        this.msg = newMsg;
    }
}
