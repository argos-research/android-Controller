package de.tum.androidcontroller.network.Packets;

import android.util.Log;

/**
 * Created by chochko on 05/05/17.
 */

public class TCPReceivePacket extends Packet{

    public TCPReceivePacket(String msg) {
        super(msg);
    }

    @Override
    public void run() {
        Log.e("TCPReceivePacket", "run msg " + super.getMsg());
    }
}
