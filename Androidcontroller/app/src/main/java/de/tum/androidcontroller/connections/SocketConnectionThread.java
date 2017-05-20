package de.tum.androidcontroller.connections;

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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.tum.androidcontroller.data.SettingsService;
import de.tum.androidcontroller.connections.models.PacketsModel;
import de.tum.androidcontroller.connections.packets.BluetoothReceivePacket;
import de.tum.androidcontroller.connections.packets.BluetoothSendPacket;
import de.tum.androidcontroller.connections.packets.Packet;
import de.tum.androidcontroller.connections.packets.TCPReceivePacket;
import de.tum.androidcontroller.connections.packets.TCPSendPacket;
import de.tum.androidcontroller.connections.packets.UDPReceivePacket;
import de.tum.androidcontroller.connections.packets.UDPSendPacket;



public class SocketConnectionThread extends ThreadPoolExecutor{


    public interface ConnectionCallback{
        /**
         * Callback method called after initialization of each
         * communication method.
         * @param state either <b>true</b> for successful or <b>false</b> otherwise.
         * @param additionInformation some additional information that should be shown
         *                            in the {@link de.tum.androidcontroller.activities.MainActivity}
         *                            on an {@link android.app.AlertDialog}.
         */
        void onConnectionInitResponse(boolean state, final String additionInformation);

        /**
         * Callback method called upon unsuccessful send request to the server.
         * @param errorInformation the type of error provided from the unsuccessful
         *                         sent packet.
         */
        void onConnectionError(final String errorInformation);
    }

    private ConnectionCallback mCallback;

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

    // Well known SPP UUID
    // http://stackoverflow.com/questions/13964342/android-how-do-bluetooth-uuids-work
    private final UUID MY_UUID =
            //UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");    //TODO check if with this configuration it will work every time and the problem with "socket null" won't happend


    private volatile String initializationMsg = ""; //used for additional info from the init.
    // the above combined with http://stackoverflow.com/questions/9148899/returning-value-from-thread


    private SocketConnectionThread(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public SocketConnectionThread(SettingsService.ConnectionType connectionType, Context context) {
        this(
                //Runtime.getRuntime().availableProcessors(), // if this value is 2 than we can not use this class for parallel sending and receiving :(
                //Runtime.getRuntime().availableProcessors(), // should be the some as the one above otherwise exception is thrown :<
                4, // if this value is 2 than we can not use this class for parallel sending and receiving :(
                4, // should be the some as the one above otherwise exception is thrown :<
                1000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1)

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

        this.execute(new Packet(PacketsModel.RUNNABLE_NAME_TCP_INIT) {
            @Override
            public void run() {
                super.run();

                try {

                    //mSocketTCP = new Socket(IP,port); //TODO check docu for the other constructors
                    mSocketTCP = new Socket();
                    mSocketTCP.connect(new InetSocketAddress(IP, port),socketTimeOut);
                    initializationMsg =  String.format("The TCP socket communication was successful initialized on %s:%d.",IP,port);
                } catch (IOException e) {
                    Log.e(TAG, String.format("initTCPConnection: unable to initialize the socket. Is the server is really running on %s:%d?",IP,port));
                    initializationMsg = String.format("Unable to initialize the TCP socket. Is the server really running on %s:%d?",IP,port);
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    //USEFUL information for UDP http://stackoverflow.com/questions/6361741/some-java-datagram-socket-questions
    /**
     * When you "connect" a DatagramSocket to a remote IP/port you are just telling
     * the socket not to accept packets to or from any other remote host. => isConnected() is
     * going to be always true so this concept won't work here. <b>SOLUTION</b> would be to
     * handle each finished Thread in afterExecute and receive from them a String with
     * an additional information of the communication in case of connection closed or
     * some other issues.
     */
    private void initUDPConnection(){
        final String IP             = getSettingsData().getServerIP();
        final int port              = getSettingsData().getServerPort();
        final int socketTimeOut     = getSettingsData().getSocketTimeout();

        this.execute(new Packet(PacketsModel.RUNNABLE_NAME_UDP_INIT) {
            @Override
            public void run() {
                super.run();

                try {
                    //mSocketUDP = new DatagramSocket(port); //standard

                    mSocketUDP = new DatagramSocket();
                    mSocketUDP.connect(InetAddress.getByName(IP),port);


                    /**
                     * I can use this but the problem is that if the after each received packet
                     * the socket will wait the given timeout and after he doesn't receive
                     * nothing it will stop receiving data. This is not desired effect that
                     * is why this won't be used
                     */
                    //mSocketUDP.setSoTimeout(socketTimeOut);

                    initializationMsg =  String.format("The UDP socket communication was successful initialized on %s:%d. Keep in mind that this does not mean that the server is running because this is the way UDP connection works.",IP,port);
                } catch (IOException e) {
                    initializationMsg = String.format("Unable to initialize the UDP socket. Is the server really running on %s:%d?",IP,port);
                    Log.e(TAG, initializationMsg);
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

        this.execute(new Packet(PacketsModel.RUNNABLE_NAME_BT_INIT) {
            @Override
            public void run() {
                super.run();

                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter(); //TODO move this to the constructor?

                BluetoothDevice device = btAdapter.getRemoteDevice(serverMac);
                Log.e(TAG, "run: PROBLEM 0" + initializationMsg);
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
                    Log.e(TAG, "run: PROBLEM 1" + initializationMsg);
                }

                // Discovery is resource intensive.  Make sure it isn't going on
                // when you attempt to connect and pass your message.
                btAdapter.cancelDiscovery();

                // Establish the connection.  This will block until it connects.
                try {
                    mSocketBt.connect();
                    initializationMsg += String.format("The Bluetooth connection is successfully established with the server %s and data link is opened!\n",serverMac);
                } catch (IOException e) {
                    try {
                        mSocketBt.close();
                        initializationMsg += "The Bluetooth server is not reachable. Please start it and provide the right MAC address of it!";
                        Log.e(TAG, "run: PROBLEM 3" + initializationMsg);
                    } catch (IOException e2) {
                        initializationMsg += "Fatal Error:  Unable to close the bluetooth socket during the connection failure" + e2.getMessage() + ".\n";
                        Log.e(TAG, "run: PROBLEM 1" + initializationMsg);
                    }
                }
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

                this.execute(new Packet(PacketsModel.RUNNABLE_NAME_CLOSE_CONNECTION) {
                    @Override
                    public void run() {
                        super.run();
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
                this.execute(new Packet(PacketsModel.RUNNABLE_NAME_CLOSE_CONNECTION) {
                    @Override
                    public void run() {
                        super.run();
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
                TCPSendPacket p = new TCPSendPacket(PacketsModel.RUNNABLE_NAME_TCP_SEND, msg, mSocketTCP);
                this.execute(p);
                if(p.getErrorInformation().length() > 1)
                    mCallback.onConnectionError(p.getErrorInformation());
            } else {
                if(LOGGING)
                    Log.e(TAG, "Too many running threads... Skipping thread!");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void sendUDP(String msg){
        try {
            int alive = this.getActiveCount();
            int queued = this.getQueue().size();
            if(queued + alive < this.getMaximumPoolSize()) {
                UDPSendPacket p = new UDPSendPacket(PacketsModel.RUNNABLE_NAME_UDP_SEND, msg,mSocketUDP);
                this.execute(p);
                if(p.getErrorInformation().length() > 1)
                    mCallback.onConnectionError(p.getErrorInformation());
            } else {
                if(LOGGING)
                    Log.e(TAG, "Too many running threads... Skipping thread!");
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void sendBluetooth(String msg){
        int alive = this.getActiveCount();
        int queued = this.getQueue().size();
        if(queued + alive < this.getMaximumPoolSize()) {
            BluetoothSendPacket p = new BluetoothSendPacket(PacketsModel.RUNNABLE_NAME_BT_SEND, msg,mSocketBt);
            this.execute(p);
            if(p.getErrorInformation().length() > 1)
                mCallback.onConnectionError(p.getErrorInformation());
        } else {
            if(LOGGING)
                Log.e(TAG, "Too many running threads... Skipping thread!");
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
                    receiveBluetooth();
                    break;
                default:
                    break;
            }
        }
    }

    private void receiveTCP(){
        if(mSocketTCP != null){
            if(mSocketTCP.isConnected()) {
                TCPReceivePacket p = new TCPReceivePacket(PacketsModel.RUNNABLE_NAME_TCP_RECEIVE, mSocketTCP);
                this.execute(p);
                if(p.getErrorInformation().length() > 1)
                    mCallback.onConnectionError(p.getErrorInformation());

                //this.execute(new TCPReceivePacket(PacketsModel.RUNNABLE_NAME_TCP_RECEIVE, mSocketTCP));
            } else
                Log.e(TAG, "receiveTCP: The server's socket is not connected! receiveTCP will not be called..");
        }else{
            Log.e(TAG, "receiveTCP: The server's socket is not initialized! receiveTCP will not be called...");
        }
    }


    private void receiveUDP(){
        if(mSocketUDP != null){
            if(mSocketUDP.isBound()){
                this.execute(new UDPReceivePacket(PacketsModel.RUNNABLE_NAME_UDP_RECEIVE,mSocketUDP));
            }else {
                Log.e(TAG, "receiveUDP: The server's socket is not bounded! receiveUDP will not be called..");
            }
        }else{
            Log.e(TAG, "receiveUDP: The server's socket is not initialized! receiveUDP will not be called...");
        }
    }


    private void receiveBluetooth(){
        if(mSocketBt != null){
            if(mSocketBt.isConnected()){
                this.execute(new BluetoothReceivePacket(PacketsModel.RUNNABLE_NAME_BT_RECEIVE,mSocketBt));
            }else {
                Log.e(TAG, "receiveBluetooth: The server's socket is not connected! receiveBluetooth will not be called..");
            }
        }else{
            Log.e(TAG, "receiveBluetooth: The server's socket is not initialized! receiveBluetooth will not be called...");
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        String finishedThreadName = Thread.currentThread().getName();

        if(LOGGING)
            Log.e(TAG, "afterExecute current thread name"+ Thread.currentThread().getName()+" Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());

        //the TCP init has finished => start immediately the receiver
        if(finishedThreadName.equals(PacketsModel.RUNNABLE_NAME_TCP_INIT) ||
                finishedThreadName.equals(PacketsModel.RUNNABLE_NAME_UDP_INIT) ||
                finishedThreadName.equals(PacketsModel.RUNNABLE_NAME_BT_INIT)    ){

            if(isConnectionEstablished()) {   // only if its connected
                mCallback.onConnectionInitResponse(true, initializationMsg);   //this is started on thread as well so a type initialization is required

                this.startReceiving();
            }else{
                mCallback.onConnectionInitResponse(false, initializationMsg);  //this is started on thread as well so a type initialization is required
            }
        }

        //in the case that some connection has failed for whatever reason then it should be reopened
        if(finishedThreadName.equals(PacketsModel.RUNNABLE_NAME_TCP_SEND) ||
                finishedThreadName.equals(PacketsModel.RUNNABLE_NAME_UDP_SEND) ||
                finishedThreadName.equals(PacketsModel.RUNNABLE_NAME_BT_SEND)    ) {

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
