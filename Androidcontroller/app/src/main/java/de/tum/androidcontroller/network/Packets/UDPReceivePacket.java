package de.tum.androidcontroller.network.Packets;

import android.util.Log;

/**
 * Created by chochko on 05/05/17.
 */

public class UDPReceivePacket extends Packet {

    public UDPReceivePacket(String msg) {
        super(msg);
    }

    @Override
    public void run() {
        Log.e("UDPReceivePacket", "run:  msg " + super.getMsg());
    }
}
