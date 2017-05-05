package de.tum.androidcontroller.network;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.tum.androidcontroller.network.Packets.TCPSendPacket;
import de.tum.androidcontroller.network.Packets.UDPReceivePacket;
import de.tum.androidcontroller.network.Packets.UDPSendPacket;



public class SocketConnectionThread extends ThreadPoolExecutor {

    private static SocketConnectionThread sInstance = null;

    private static final String TAG = "SocketConnectionThread";

    private SocketConnectionThread(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * Singleton of this class
     */
    private static SocketConnectionThread init(){
        final int NUMBER_OF_CORES =
                Runtime.getRuntime().availableProcessors(); // if this value is 2 than we can not use this class for parallel sending and receiving :(
                //4; // if this value is 2 than we can not use this class for parallel sending and receiving :(

        final long KEEP_ALIVE_TIME = 1000; //used only as receiver window for the send server's packets

        final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

        ThreadFactory threadFactory = new ConnectionThreadFactory();

        return new SocketConnectionThread(  NUMBER_OF_CORES,
                                            NUMBER_OF_CORES,    //should be equal to the first one otherwise exception (IllegalArgumentException)
                                            KEEP_ALIVE_TIME,
                                            TIME_UNIT,
                                            new LinkedBlockingQueue<Runnable>(),
                                            threadFactory);

    }

    public void UDPSend(String msg){

        Log.e(TAG, "getInstance: maxpoolsize "+ getMaximumPoolSize());
        setCorePoolSize(4);
        //this.execute(new UDPSendPacket(msg));
        Log.e(TAG, "getInstance: maxpoolsize "+ getMaximumPoolSize());
    }

    public void UDPReceive(){
        this.execute(new UDPReceivePacket(""));
    }

    public void TCPSend(String msg){
        this.execute(new TCPSendPacket(msg));
    }

    public void TCPReceive(){
        this.execute(new UDPSendPacket(""));
    }

    /**
     * Singleton instance of this class
     * @return mine instance
     */
    public static synchronized SocketConnectionThread getInstance() {
        if(sInstance == null) {
            sInstance = init();
        }
        return sInstance;
    }
}
