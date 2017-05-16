package de.tum.androidcontroller.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.tum.androidcontroller.data.SettingsService;
import de.tum.androidcontroller.network.models.PacketsModel;
import de.tum.androidcontroller.network.packets.BluetoothSendPacket;
import de.tum.androidcontroller.network.packets.TCPReceivePacket;
import de.tum.androidcontroller.network.packets.TCPSendPacket;
import de.tum.androidcontroller.network.packets.UDPReceivePacket;
import de.tum.androidcontroller.network.packets.UDPSendPacket;



public class SocketConnectionThread extends ThreadPoolExecutor{


    public interface ConnectionCallback{
        void onConnectionInitResponse(boolean state, String additionInformation);
    }

    private ConnectionCallback mCallback;

    private ConnectionThreadFactory mThreadFactory = null;

    private static final String TAG = "SocketConnectionThread";

    private SettingsService.ConnectionType connectionType = null;

    private final boolean LOGGING = false;


    //For the TCP connection
    private Socket          mSocketTCP  = null;

    //for the UDP connection
    private DatagramSocket  mSocketUDP  = null;

    private Context context;

    //for the bluetooth part
    private BluetoothSocket mSocketBt   = null;
//    private OutputStream btOutStream    = null;
//    private InputStream btInStream      = null;

    // Well known SPP UUID
    private final UUID MY_UUID =
            //UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");    //TODO check if with this configuration it will work every time and the problem with "socket null" won't happend


    private volatile String initializationMsg = ""; //used for additional info from the init.
    // the above combined with http://stackoverflow.com/questions/9148899/returning-value-from-thread


    private SocketConnectionThread(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        mThreadFactory = (ConnectionThreadFactory) threadFactory;
    }

    public SocketConnectionThread(SettingsService.ConnectionType connectionType, Context context) {
        this(
                //Runtime.getRuntime().availableProcessors(), // if this value is 2 than we can not use this class for parallel sending and receiving :(
                //Runtime.getRuntime().availableProcessors(), // should be the some as the one above otherwise exception is thrown :<
                4, // if this value is 2 than we can not use this class for parallel sending and receiving :(
                4, // should be the some as the one above otherwise exception is thrown :<
                1000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ConnectionThreadFactory()               // used for custom name assignment

        );

        this.connectionType = connectionType;

        this.context        = context;

        this.mCallback      = (ConnectionCallback) context;

        initCommunication(); //init the given communication

    }

    private void initCommunication(){
        if(LOGGING)
            Log.e(TAG, "initCommunication: with connectionType " + connectionType.toString());
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

        final String IP             = getSettingsData().getServerIP();
        final int port              = getSettingsData().getServerPort();
        final int socketTimeOut     = getSettingsData().getSocketTimeout();

        mThreadFactory.setType(ConnectionThreadFactory.Type.InitTCPCommunication);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {

                    //mSocketTCP = new Socket(IP,port); //TODO check docu for the other constructors
                    mSocketTCP = new Socket();
                    mSocketTCP.connect(new InetSocketAddress(IP, port),socketTimeOut);
                } catch (IOException e) {
                    Log.e(TAG, String.format("initTCPConnection: unable to initialize the socket. Is the server is really running on %s:%d?",IP,port));
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        };
        this.execute(r);
    }

    //USEFUL information for UDP http://stackoverflow.com/questions/6361741/some-java-datagram-socket-questions
    /**
     * When you "connect" a DatagramSocket to a remote IP/port you are just telling
     * the socket not to accept packets to or from any other remote host. => isConnected() is
     * going to be always true so this concept won't work here. <b>SOLUTION</b> would be to
     * handle each finished Thread in afterExecute and receive from them a String with
     * an additional information of the communication in case of connection closed or
     * some other issues. TODO use this concept!
     */
    private void initUDPConnection(){
        final String IP             = getSettingsData().getServerIP();
        final int port              = getSettingsData().getServerPort();
        final int socketTimeOut     = getSettingsData().getSocketTimeout();

        mThreadFactory.setType(ConnectionThreadFactory.Type.InitUDPCommunication);

        this.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    //mSocketUDP = new DatagramSocket(port); //standard

                    mSocketUDP = new DatagramSocket();
                    mSocketUDP.connect(InetAddress.getByName(IP),port);
                    mSocketUDP.setSoTimeout(socketTimeOut); //TODO use this for the TCP as well?

                } catch (IOException e) {
                    Log.e(TAG, String.format("initTCPConnection: unable to initialize the socket. Is the server is really running on %s:%d?",IP,port));
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * <b>IMPORTANT!</b> The both devices should be paired otherwise it is not working!
     */
    private void initBluetoothConnection(){
        final String serverMac      = "30:3A:64:D2:3E:93"; //TODO Add this to the settings
        Log.e(TAG, "initBluetoothConnection: ");
        mThreadFactory.setType(ConnectionThreadFactory.Type.InitBluetoothCommunication);

        this.execute(new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter(); //TODO move this to the constructor?

                BluetoothDevice device = btAdapter.getRemoteDevice(serverMac);

                // Two things are needed to make a connection:
                //   A MAC address, which we got above.
                //   A Service ID or UUID.  In this case we are using the
                //     UUID for SPP.
                try {
                    //for other options and diff between them see here: (Also here more info for what is happening!)
                    //http://stackoverflow.com/questions/16457693/the-differences-between-createrfcommsockettoservicerecord-and-createrfcommsocket
                    //the official documentation can be found here:
                    //https://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
                    mSocketBt = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);

                    //btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                    initializationMsg += "Fatal Error: Bluetooth socket creation failed: " + e.getMessage() + ".\n";
                }

                // Discovery is resource intensive.  Make sure it isn't going on
                // when you attempt to connect and pass your message.
                btAdapter.cancelDiscovery();

                // Establish the connection.  This will block until it connects.
                try {
                    mSocketBt.connect();
                    initializationMsg += "Connection established and data link opened...\n";
                } catch (IOException e) {
                    try {
                        mSocketBt.close();
                    } catch (IOException e2) {
                        initializationMsg += "Fatal Error:  Unable to close the bluetooth socket during the connection failure" + e2.getMessage() + ".\n";
                    }
                }

                Log.e(TAG, "run: READY BT INIT");
            }
        });
    }

    /**
     * Standard method for closing the current connection
     * depending on the chosen {@link SettingsService.ConnectionType}.
     */
    public void closeConnection(){
        if(LOGGING)
            Log.e(TAG, "closeConnection: contype " + connectionType.toString());
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

        if (mSocketTCP != null) {
            if (mSocketTCP.isConnected()) {
                mThreadFactory.setType(ConnectionThreadFactory.Type.CloseSomeCommunication);

                this.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mSocketTCP.close();
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
        if(mSocketUDP != null){
            if(mSocketUDP.isConnected()){
                mThreadFactory.setType(ConnectionThreadFactory.Type.CloseSomeCommunication);
                this.execute(new Runnable() {
                    @Override
                    public void run() {
                        mSocketUDP.close();
                    }
                });
            }
        }

    }

    private void closeBluetoothConnection() {

    }


    /**
     * Helper method for deciding if a connection is established
     * @return true if the connection is connection
     */
    private boolean isConnectionEstablished(){
        switch (connectionType){
            case TCP:
                if(mSocketTCP != null){
                    if(mSocketTCP.isConnected())
                        return true;
                }
                break;
            case UDP:
                if(mSocketUDP != null){
                    if(mSocketUDP.isConnected() && mSocketUDP.isBound())
                        return true;
                }
                break;

            case Bluetooth:
                if(mSocketBt != null){
                    if(mSocketBt.isConnected())
                        return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }

    /**
     * The methos is sending the <b>msg</b> with the
     * corresponding communication technology.
     * @param msg The message that is going to be send.
     */
    public void sendMsg(String msg){
        if(LOGGING)
            Log.e(TAG, "sendMsg with connectionType " + connectionType.toString());

        if (isConnectionEstablished()) {
            switch (connectionType){
                case TCP:
                    sendTCP(msg);
                    break;

                case UDP:
                    sendUDP(msg);
                    break;

                case Bluetooth:
                    sendBluetooth(msg);
                    break;
                default:
                    break;
            }
        }
    }

    private void sendTCP(String msg){
        try {
            int alive = this.getActiveCount();
            int queued = this.getQueue().size();
            if(queued + alive < this.getMaximumPoolSize()) {
                mThreadFactory.setType(ConnectionThreadFactory.Type.TCPSend);
                this.execute(new TCPSendPacket(msg, mSocketTCP));
            } else
            if(LOGGING)
                Log.e(TAG, "Too much processes... Skipping thread!");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void sendUDP(String msg){
        try {
            int alive = this.getActiveCount();
            int queued = this.getQueue().size();
            if(queued + alive < this.getMaximumPoolSize()) {
                mThreadFactory.setType(ConnectionThreadFactory.Type.UDPSend);
                this.execute(new UDPSendPacket(msg,mSocketUDP));
            } else
                Log.e(TAG, "Too much processes... Skipping thread!");

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void sendBluetooth(String msg){
        try {
            int alive = this.getActiveCount();
            int queued = this.getQueue().size();
            if(queued + alive < this.getMaximumPoolSize()) {
                mThreadFactory.setType(ConnectionThreadFactory.Type.BluetoothSend);
                this.execute(new BluetoothSendPacket(msg,mSocketBt));
            } else
                Log.e(TAG, "Too much processes... Skipping thread!");

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void startReceiving(){
        if (isConnectionEstablished()) {
            switch (connectionType){
                case TCP:
                    receiveTCP();
                    break;

                case UDP:
                    receiveUDP();
                    break;

                case Bluetooth:
                    //TODO add
                    break;
                default:
                    break;
            }
        }
    }

    private void receiveUDP(){
        try {
            mThreadFactory.setType(ConnectionThreadFactory.Type.UDPReceive);
            this.execute(new UDPReceivePacket());

       } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    private void receiveTCP(){
        try {
            if(mSocketTCP != null){
                if(mSocketTCP.isConnected()) {
                    mThreadFactory.setType(ConnectionThreadFactory.Type.TCPReceive);
                    this.execute(new TCPReceivePacket("", mSocketTCP));
                } else
                    Log.e(TAG, "receiveTCP: The server's socket is not connected! receiveTCP will not be called..");
            }else{
                Log.e(TAG, "receiveTCP: The server's socket is not initialized! receiveTCP will not be called...");
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        String finishedThreadName = Thread.currentThread().getName();

        //if(LOGGING)
        Log.e(TAG, "afterExecute current thread name"+ Thread.currentThread().getName()+" Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());

        //the TCP init has finished => start immediately the TCP receiver
        if(finishedThreadName.equals(PacketsModel.RUNNABLE_NAME_TCP_INIT) ||
                finishedThreadName.equals(PacketsModel.RUNNABLE_NAME_UDP_INIT) ||
                finishedThreadName.equals(PacketsModel.RUNNABLE_NAME_BT_INIT)    ){


            if(isConnectionEstablished()) {   // only if its connected
                mThreadFactory.setType(ConnectionThreadFactory.Type.CallbackThread);
                mCallback.onConnectionInitResponse(true, initializationMsg);   //this is started on thread as well so a type initialization is required

                this.startReceiving();
            }else{
                mThreadFactory.setType(ConnectionThreadFactory.Type.CallbackThread);
                mCallback.onConnectionInitResponse(false, initializationMsg);  //this is started on thread as well so a type initialization is required
            }

        }
    }


    /**
     * Obtaining the singleton instane of the <b>SettingsService</b>
     * @return {@link SettingsService}
     */
    private SettingsService getSettingsData(){
        return SettingsService.getInstance(context);
    }

}
