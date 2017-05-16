package de.tum.androidcontroller.network.packets;

import android.bluetooth.BluetoothSocket;

/**
 * Created by chochko on 16/05/17.
 */

public class BluetoothReceivePacket extends Packet {

    BluetoothReceivePacket(String threadName, String msg, BluetoothSocket socketBt) {
        super(threadName, msg, socketBt);
    }

    @Override
    public void run() {
        super.run(); //give the thread a name

    }
}
