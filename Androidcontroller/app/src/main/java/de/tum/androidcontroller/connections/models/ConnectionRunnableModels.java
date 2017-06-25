package de.tum.androidcontroller.connections.models;

/**
 * Created by chochko on 07/05/17.
 */

public class ConnectionRunnableModels {
    public static final String RUNNABLE_NAME_TCP_INIT         = "TCPInitCommunication";
    public static final String RUNNABLE_NAME_TCP_SEND         = "TCPSendPacket";
    public static final String RUNNABLE_NAME_TCP_RECEIVE      = "TCPReceivePacket";


    public static final String RUNNABLE_NAME_UDP_INIT         = "UDPInitCommunication";
    public static final String RUNNABLE_NAME_UDP_SEND         = "UDPSendPacket";
    public static final String RUNNABLE_NAME_UDP_RECEIVE      = "UDPReceivePacket";


    public static final String RUNNABLE_NAME_BT_INIT          = "BluetoothInitCommunication";
    public static final String RUNNABLE_NAME_BT_SEND          = "BluetoothSendPacket";
    public static final String RUNNABLE_NAME_BT_RECEIVE       = "BluetoothReceivePacket";


    public static final String RUNNABLE_NAME_CALLBACK         = "MainActivityCallbackInterface";

    public static final String RUNNABLE_NAME_CLOSE_CONNECTION = "CloseCommunication";



    public static final String BROADCAST_INFORMATION_KEY      = "AdditionalInformationKey";
    public static final String BROADCAST_ACTION_RECEIVE       = "BroadcastOnReceiveAndroidController";
    public static final String BROADCAST_ACTION_FAILURE       = "BroadcastOnFailureAndroidController";


}
