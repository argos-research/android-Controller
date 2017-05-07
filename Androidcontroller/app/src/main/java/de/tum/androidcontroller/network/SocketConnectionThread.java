package de.tum.androidcontroller.network;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.tum.androidcontroller.network.Models.PacketsModel;
import de.tum.androidcontroller.network.Packets.TCPReceivePacket;
import de.tum.androidcontroller.network.Packets.TCPSendPacket;
import de.tum.androidcontroller.network.Packets.UDPReceivePacket;
import de.tum.androidcontroller.network.Packets.UDPSendPacket;



public class SocketConnectionThread extends ThreadPoolExecutor{


    private ConnectionThreadFactory mThreadFactory = null;

    private static final String TAG = "SocketConnectionThread";

    private PacketsModel.ConnectionType connectionType = null;

    //For the TCP connection
    private Socket mSocket;

    //TODO add this to the settings activity
    private String IP   = "192.168.2.118";
    private int port    = 8000;

    private SocketConnectionThread(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        mThreadFactory = (ConnectionThreadFactory) threadFactory;
    }

    public SocketConnectionThread(PacketsModel.ConnectionType connectionType) {
        this(
                Runtime.getRuntime().availableProcessors(), // if this value is 2 than we can not use this class for parallel sending and receiving :(
                Runtime.getRuntime().availableProcessors(), // should be the some as the one above otherwise exception is thrown :<
                1000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ConnectionThreadFactory()               // used for custom name assignment

        );

        this.connectionType = connectionType;

        initCommunication(); //init the given communication
    }

    private void initCommunication(){
        switch (connectionType){
            case TCP:
                initTCPConnection();
                break;

            case UDP:
                initUDPConnection();
                break;

            case Bluetooth:
                initBluetoothConnection();
                break;
            default:
                break;
        }
    }

    private void checkWiFiState() {

        //TODO add this to the settings activity

        // link http://stackoverflow.com/questions/16539196/ask-user-to-turn-on-wi-fi
        // and http://stackoverflow.com/questions/3930990/android-how-to-enable-disable-wifi-or-internet-connection-programmatically

//        WifiManager wifi = (WifiManager)
//                mContext.getSystemService(Context.WIFI_SERVICE);
//        if(!wifi.isWifiEnabled())
//            wifi.setWifiEnabled(true);


        //startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
    }

    private void initTCPConnection(){

        mThreadFactory.setType(ConnectionThreadFactory.Type.InitTCPCommunication);

        this.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket(IP, port); //TODO check docu for the other constructors
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
        switch (connectionType){
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
        mThreadFactory.setType(ConnectionThreadFactory.Type.CloseSomeCommunication);

        this.execute(new Runnable() {
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

    private void closeUDPConnection() {

    }

    private void closeBluetoothConnection() {

    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        String finishedThreadName = Thread.currentThread().getName();

        //Log.e(TAG, "afterExecute current thread name"+ Thread.currentThread().getName()+" Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());

        //the TCP init has fisnished => start immediately the TCP receiver
        if(finishedThreadName.equals(PacketsModel.RUNNABLE_NAME_TCP_INIT)){
            this.TCPReceive();
        }
    }

    public void UDPSend(String msg){
        try {
            mThreadFactory.setType(ConnectionThreadFactory.Type.UDPSend);
            Log.e(TAG, "Before Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
            this.execute(new UDPSendPacket(msg));

            Log.e(TAG, "After Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void UDPReceive(){
        try {
            mThreadFactory.setType(ConnectionThreadFactory.Type.UDPReceive);

            Log.e(TAG, "Before Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
            this.execute(new UDPReceivePacket(""));

            Log.e(TAG, "After Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void TCPSend(String msg){
        try {
            mThreadFactory.setType(ConnectionThreadFactory.Type.TCPSend);
            int alive = this.getActiveCount();
            int queued = this.getQueue().size();
            if(queued > 0)
                Log.e(TAG, "TCPSend: QEUED is not 0! It is " +queued);
            if(this.getMaximumPoolSize() - alive > 0)
                this.execute(new TCPSendPacket(msg,mSocket));
            else
                Log.e(TAG, "Skipping thread!");
            //Log.e(TAG, "Before Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());


           // Log.e(TAG, "After Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void TCPReceive(){
        try {
            mThreadFactory.setType(ConnectionThreadFactory.Type.TCPReceive);

            //Log.e(TAG, "Before Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
            this.execute(new TCPReceivePacket("",mSocket));

           // Log.e(TAG, "After Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}
