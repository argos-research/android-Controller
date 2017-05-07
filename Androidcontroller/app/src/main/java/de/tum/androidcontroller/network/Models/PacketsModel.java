package de.tum.androidcontroller.network.Models;

/**
 * Created by chochko on 07/05/17.
 */

public class PacketsModel {
    public static final String RUNNABLE_NAME_TCP_SEND         = "TCPSendPacket";
    public static final String RUNNABLE_NAME_TCP_RECEIVE      = "TCPReceivePacket";
    public static final String RUNNABLE_NAME_TCP_INIT         = "TCPInitCommunication";
    public static final String RUNNABLE_NAME_UDP_SEND         = "UDPSendPacket";
    public static final String RUNNABLE_NAME_UDP_RECEIVE      = "UDPReceivePacket";

    public static enum ConnectionType {
        UDP,
        TCP,
        Bluetooth
    }
}
