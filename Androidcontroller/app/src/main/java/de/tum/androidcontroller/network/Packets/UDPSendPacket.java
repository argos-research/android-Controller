package de.tum.androidcontroller.network.Packets;

import android.util.Log;

import java.util.concurrent.Callable;

/**
 * Created by chochko on 05/05/17.
 */

public class UDPSendPacket extends Packet  {


    public UDPSendPacket(String msg) {
        super(msg);
    }

    @Override
    public void run() {
        Log.e("UDPSendPacket", "run: msg " + super.getMsg());
    }

}
