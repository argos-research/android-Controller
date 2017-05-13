package de.tum.androidcontroller.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import de.tum.androidcontroller.data.SettingsService;
import de.tum.androidcontroller.network.packets.Packet;
import de.tum.androidcontroller.network.packets.TCPReceivePacket;
import de.tum.androidcontroller.network.packets.TCPSendPacket;
import de.tum.androidcontroller.network.packets.UDPReceivePacket;
import de.tum.androidcontroller.network.packets.UDPSendPacket;

/**
 * Created by chochko on 13/05/17.
 */

public class SocketConnectionAsyncTask extends AsyncTask<Packet,Void,String> {


    private final String TAG = "SocketConnectionTask";

    private SettingsService.ConnectionType mConnectionType = null;

    private Context mContext;

    //For the TCP connection
    private Socket mSocket;

    private volatile boolean running;

    public SocketConnectionAsyncTask(SettingsService.ConnectionType connectionType, Context context){
        this.mConnectionType    = connectionType;
        this.mContext           = context;

        initCommunication();
    }

    private void initCommunication(){
        Log.e(TAG, "initCommunication: with mConnectionType " + mConnectionType.toString());
        running = true;
        switch (mConnectionType){
            case TCP:
                initTCPConnection();

                break;

            case UDP:
                //initUDPConnection();
                break;

            case Bluetooth:
                //initBluetoothConnection();
                break;
            default:
                break;
        }
    }

    private void initTCPConnection(){


        final String IP             = getSettingsData().getServerIP();
        final int port              = getSettingsData().getServerPort();
        final int socketTimeOut     = getSettingsData().getSocketTimeout();

        this.execute(new Packet("") {

            @Override
            public void run() {
                try {
                    //mSocket = new Socket(IP,port); //TODO check docu for the other constructors
                    mSocket = new Socket();
                    mSocket.connect(new InetSocketAddress(IP, port),socketTimeOut);
                } catch (IOException e) {
                    Log.e(TAG, String.format("initTCPConnection: unable to initialize the socket. Is the server is really running on %s:%d?",IP,port));
                    e.printStackTrace();
                }
            }
        });
    }

    private void initUDPConnection(){

    }

    private void initBluetoothConnection(){

    }

    public void closeConnection(){
        Log.e(TAG, "closeConnection: contype " + mConnectionType.toString());
        switch (mConnectionType){
            case TCP:
                closeTCPConnection();
                break;

            case UDP:
                closeUDPConnection();
                break;

            case Bluetooth:
                closeBluetoothConnection();
                break;
            default:
                break;
        }
    }

    private void closeTCPConnection() {
        running = false;
        if (mSocket != null) {
            if (mSocket.isConnected()) {

                this.execute(new Packet("") {
                    @Override
                    public void run() {
                        try {
                            mSocket.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Unable to close the TCP communication...");
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private void closeUDPConnection() {

    }

    private void closeBluetoothConnection() {

    }


    /**
     * The methos is sending the <b>msg</b> with the
     * corresponding communication technology.
     * @param msg The message that is going to be send.
     */
    public void sendMsg(String msg){
        Log.e(TAG, "sendMsg with mConnectionType " + mConnectionType.toString());
        switch (mConnectionType){
            case TCP:
                TCPSend(msg);
                break;

            case UDP:
                UDPSend(msg);
                break;

            case Bluetooth:
                //TODO add
                break;
            default:
                break;
        }
    }

    private void TCPSend(String msg){
//        try {
//            mThreadFactory.setType(ConnectionThreadFactory.Type.TCPSend);
//            int alive = this.getActiveCount();
//            int queued = this.getQueue().size();
//            if(queued + alive < this.getMaximumPoolSize())
//                this.execute(new TCPSendPacket(msg,mSocket));
//            else
//                Log.e(TAG, "Too much processes... Skipping thread!");
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        }
        this.execute(new TCPSendPacket(msg,mSocket));
    }

    private void UDPSend(String msg){
//        try {
//            mThreadFactory.setType(ConnectionThreadFactory.Type.UDPSend);
//            int alive = this.getActiveCount();
//            int queued = this.getQueue().size();
//            if(queued + alive < this.getMaximumPoolSize())
//                this.execute(new UDPSendPacket(msg));
//            else
//                Log.e(TAG, "Too much processes... Skipping thread!");
//
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        }
    }

    private void startReceiving(){
        switch (mConnectionType){
            case TCP:
                TCPReceive();
                break;

            case UDP:
                //TODO add
                break;

            case Bluetooth:
                UDPReceive();
                break;
            default:
                break;
        }
    }

    private void TCPReceive(){
        if(mSocket != null){
            if(mSocket.isConnected())
                this.execute(new TCPReceivePacket("",mSocket));
            else
                Log.e(TAG, "TCPReceive: The server's socket is not connected! TCPReceive will not be called..");
        }else{
            Log.e(TAG, "TCPReceive: The server's socket is not initialized! TCPReceive will not be called...");
        }
    }


    private void UDPReceive(){
            this.execute(new UDPReceivePacket(""));

    }


    /**
     * Obtaining the singleton instane of the <b>SettingsService</b>
     * @return {@link SettingsService}
     */
    private SettingsService getSettingsData(){
        return SettingsService.getInstance(mContext);
    }

    @Override
    protected String doInBackground(Packet... params) {
        params[0].run();
        return "1";
    }
}
