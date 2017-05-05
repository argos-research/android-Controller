package de.tum.androidcontroller.network.Packets;

import android.util.Log;

/**
 * Created by chochko on 05/05/17.
 */

public class TCPSendPacket extends Packet{

    public TCPSendPacket(String msg) {
        super(msg);
    }

    @Override
    public void run() {
        Log.e("TCPSendPacket", "run msg " + super.getMsg());
    }
}
