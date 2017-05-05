package de.tum.androidcontroller.network;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;

import de.tum.androidcontroller.network.Packets.TCPReceivePacket;
import de.tum.androidcontroller.network.Packets.TCPSendPacket;
import de.tum.androidcontroller.network.Packets.UDPReceivePacket;
import de.tum.androidcontroller.network.Packets.UDPSendPacket;

/**
 * Created by chochko on 05/05/17.
 */

public class ConnectionThreadFactory implements ThreadFactory {

    private final String RUNNABLE_NAME_TCP_SEND         = "TCPSendPacket";
    private final String RUNNABLE_NAME_TCP_RECEIVE      = "TCPReceivePacket";
    private final String RUNNABLE_NAME_UDP_SEND         = "UDPSendPacket";
    private final String RUNNABLE_NAME_UDP_RECEIVE      = "UDPReceivePacket";

    @Override
    public Thread newThread(@NonNull Runnable r) {

        if(r instanceof TCPReceivePacket){
        //if(r.getClass().equals(TCPReceivePacket)){
            return new Thread(r,RUNNABLE_NAME_TCP_RECEIVE);
        }else if(r instanceof TCPSendPacket){
            return new Thread(r,RUNNABLE_NAME_TCP_SEND);
        }else if(r instanceof UDPReceivePacket){
            return new Thread(r,RUNNABLE_NAME_UDP_RECEIVE);
        }else if(r instanceof UDPSendPacket){
            return new Thread(r,RUNNABLE_NAME_UDP_SEND);
        }
        else {
            throw new IllegalArgumentException("Wrong runnable argument! "+r.toString());
        }
    }
}
