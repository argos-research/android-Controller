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

import de.tum.androidcontroller.connections.models.ConnectionRunnableModels;
import de.tum.androidcontroller.data.SettingsService;
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
    }

    private ConnectionCallback mCallback;

    private static final String TAG = "SocketConnectionThread";

    private SettingsService.ConnectionType connectionType = null;

    private final boolean LOGGING = false;

    private volatile boolean amIReceiving;


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
                new LinkedBlockingQueue<Runnable>(10)

        );

        this.connectionType = connectionType;

        this.context        = context;

        this.mCallback      = (ConnectionCallback) context;

        this.amIReceiving   = false;

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

        this.execute(new Packet(ConnectionRunnableModels.RUNNABLE_NAME_TCP_INIT) {
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

        this.execute(new Packet(ConnectionRunnableModels.RUNNABLE_NAME_UDP_INIT) {
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

        this.execute(new Packet(ConnectionRunnableModels.RUNNABLE_NAME_BT_INIT) {
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

                this.execute(new Packet(ConnectionRunnableModels.RUNNABLE_NAME_CLOSE_CONNECTION) {
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
                //send the close message to the server needed for closing the connection
                this.sendUDP("close",true);

                this.execute(new Packet(ConnectionRunnableModels.RUNNABLE_NAME_CLOSE_CONNECTION) {
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
        if(mSocketBt != null){
            if(mSocketBt.isConnected()){

                this.execute(new Packet(ConnectionRunnableModels.RUNNABLE_NAME_CLOSE_CONNECTION) {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            mSocketBt.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }
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
                    sendUDP(msg,false);
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
                TCPSendPacket p = new TCPSendPacket(ConnectionRunnableModels.RUNNABLE_NAME_TCP_SEND, msg, mSocketTCP, context);
                /**
                 * SOLUTION https://stackoverflow.com/questions/19529309/rejectedexecutionexception-from-asynctask-but-havent-hit-limits
                 *  java.util.concurrent.RejectedExecutionException: Task de.tum.androidcontroller.connections.packets.TCPSendPacket@f985737 rejected from de.tum.androidcontroller.connections.SocketConnectionThread@9fa14a4[Running, pool size = 4, active threads = 1, queued tasks = 0, completed tasks = 3306]
                 at java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2014)
                 at java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:794)
                 at java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:1340)
                 at de.tum.androidcontroller.connections.SocketConnectionThread.sendTCP(SocketConnectionThread.java:404)
                 at de.tum.androidcontroller.connections.SocketConnectionThread.sendMsg(SocketConnectionThread.java:382)
                 at de.tum.androidcontroller.activities.MainActivity.onAccelerometerChanged(MainActivity.java:471)
                 at de.tum.androidcontroller.sensors.SensorModel.onSensorChanged(SensorModel.java:78)

                 * JNI DETECTED ERROR IN APPLICATION: JNI CallObjectMethod called with pending exception java.util.concurrent.RejectedExecutionException: Task de.tum.androidcontroller.connections.packets.TCPSendPacket@f985737 rejected from de.tum.androidcontroller.connections.SocketConnectionThread@9fa14a4[Running, pool size = 4, active threads = 1, queued tasks = 1, completed tasks = 1711]
                 05-27 22:56:21.173 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(java.lang.Runnable, java.util.concurrent.ThreadPoolExecutor) (ThreadPoolExecutor.java:2014)
                 05-27 22:56:21.173 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void java.util.concurrent.ThreadPoolExecutor.reject(java.lang.Runnable) (ThreadPoolExecutor.java:794)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void java.util.concurrent.ThreadPoolExecutor.execute(java.lang.Runnable) (ThreadPoolExecutor.java:1340)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void de.tum.androidcontroller.connections.SocketConnectionThread.sendTCP(java.lang.String) (SocketConnectionThread.java:415)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void de.tum.androidcontroller.connections.SocketConnectionThread.sendMsg(java.lang.String) (SocketConnectionThread.java:382)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void de.tum.androidcontroller.activities.MainActivity.onAccelerometerChanged(de.tum.androidcontroller.models.SensorBaseModel) (MainActivity.java:479)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void de.tum.androidcontroller.sensors.SensorModel.onSensorChanged(android.hardware.SensorEvent) (SensorModel.java:78)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void android.hardware.SystemSensorManager$SensorEventQueue.dispatchSensorEvent(int, float[], int, long) (SystemSensorManager.java:491)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void android.os.MessageQueue.nativePollOnce(long, int) (MessageQueue.java:-2)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at android.os.Message android.os.MessageQueue.next() (MessageQueue.java:323)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void android.os.Looper.loop() (Looper.java:135)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void android.app.ActivityThread.main(java.lang.String[]) (ActivityThread.java:5539)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at java.lang.Object java.lang.reflect.Method.invoke!(java.lang.Object, java.lang.Object[]) (Method.java:-2)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run() (ZygoteInit.java:726)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]   at void com.android.internal.os.ZygoteInit.main(java.lang.String[]) (ZygoteInit.java:616)
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]
                 05-27 22:56:21.174 13870-13870/de.tum.androidcontroller A/art: art/runtime/java_vm_ext.cc:410]     in call to CallObjectMethod
                 */

                this.execute(p);

            } else {
                if(LOGGING)
                    Log.e(TAG, "Too many running threads... Skipping thread! Alive threads "+ alive + " and on the queue "+queued + ".");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * The UDP function for sending data to the
     * UDP server.
     *
     * @param msg the message sent to the server
     * @param isCloseUDPMessage because UDP need additional
     *                          mechanisms for closing the connection
     *                          (the server can not detect if the client
     *                          has been disconnected),
     *                          this value represent if this method
     *                          should send a closing string to the
     *                          server in order manually to create
     *                          disconnect event and to stop server
     *                          for sending back data to the client.
     */
    private void sendUDP(String msg,boolean isCloseUDPMessage){
        if (isCloseUDPMessage) {
            //don't wait and send immediately the close message
            UDPSendPacket p = new UDPSendPacket(ConnectionRunnableModels.RUNNABLE_NAME_UDP_SEND, "close",mSocketUDP, context);
            this.execute(p);
        } else {
            try {
                int alive = this.getActiveCount();
                int queued = this.getQueue().size();
                if(queued + alive < this.getMaximumPoolSize()) {
                    UDPSendPacket p = new UDPSendPacket(ConnectionRunnableModels.RUNNABLE_NAME_UDP_SEND, msg,mSocketUDP, context);
                    this.execute(p);

                } else {
                    if(LOGGING)
                        Log.e(TAG, "Too many running threads... Skipping thread!");
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendBluetooth(String msg){
        int alive = this.getActiveCount();
        int queued = this.getQueue().size();
        if(queued + alive < this.getMaximumPoolSize()) {
            BluetoothSendPacket p = new BluetoothSendPacket(ConnectionRunnableModels.RUNNABLE_NAME_BT_SEND, msg,mSocketBt, context);
            this.execute(p);

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
                TCPReceivePacket p = new TCPReceivePacket(ConnectionRunnableModels.RUNNABLE_NAME_TCP_RECEIVE, mSocketTCP, context);
                this.execute(p);


                //this.execute(new TCPReceivePacket(ConnectionRunnableModels.RUNNABLE_NAME_TCP_RECEIVE, mSocketTCP));
            } else
                Log.e(TAG, "receiveTCP: The server's socket is not connected! receiveTCP will not be called..");
        }else{
            Log.e(TAG, "receiveTCP: The server's socket is not initialized! receiveTCP will not be called...");
        }
    }


    private void receiveUDP(){
        if(mSocketUDP != null){
            if(mSocketUDP.isBound()){
                this.execute(new UDPReceivePacket(ConnectionRunnableModels.RUNNABLE_NAME_UDP_RECEIVE,mSocketUDP, context));
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
                this.execute(new BluetoothReceivePacket(ConnectionRunnableModels.RUNNABLE_NAME_BT_RECEIVE,mSocketBt, context));
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

        //check if the init was successful and inform the MainActivity with the callback
        if(finishedThreadName.equals(ConnectionRunnableModels.RUNNABLE_NAME_TCP_INIT) ||
                finishedThreadName.equals(ConnectionRunnableModels.RUNNABLE_NAME_UDP_INIT) ||
                finishedThreadName.equals(ConnectionRunnableModels.RUNNABLE_NAME_BT_INIT)    ){

            if(isConnectionEstablished()) {   // only if its connected
                mCallback.onConnectionInitResponse(true, initializationMsg);   //this is started on thread as well so a type initialization is required


            }else{
                mCallback.onConnectionInitResponse(false, initializationMsg);  //this is started on thread as well so a type initialization is required
            }
        }

        //Something was sent => start immediately the receiver
        if(finishedThreadName.equals(ConnectionRunnableModels.RUNNABLE_NAME_TCP_SEND) ||
                finishedThreadName.equals(ConnectionRunnableModels.RUNNABLE_NAME_UDP_SEND) ||
                finishedThreadName.equals(ConnectionRunnableModels.RUNNABLE_NAME_BT_SEND)    ) {
            if(!amIReceiving){
                amIReceiving = true;
                this.startReceiving();
                Log.e(TAG, "afterExecute: Starting the receiver thread.");
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
