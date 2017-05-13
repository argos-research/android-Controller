package de.tum.androidcontroller.network.packets;

import android.util.Log;

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
