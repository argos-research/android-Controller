package de.tum.androidcontroller.network;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.tum.androidcontroller.network.Packets.TCPSendPacket;
import de.tum.androidcontroller.network.Packets.UDPReceivePacket;
import de.tum.androidcontroller.network.Packets.UDPSendPacket;



public class SocketConnectionThread extends ThreadPoolExecutor{

    private static SocketConnectionThread sInstance = null;

    private ConnectionThreadFactory mThreadFactory = null;

    private static final String TAG = "SocketConnectionThread";

    private ExecutorService receivingExecutor = null;

    private SocketConnectionThread(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        mThreadFactory = (ConnectionThreadFactory) threadFactory;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        Log.e(TAG, "afterExecute current thread name"+ Thread.currentThread().getName()+" Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
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
//        //only once start it
//        if(receivingExecutor == null){
//            receivingExecutor =
//        }

        UDPSendPacket r = new UDPSendPacket(msg);
        try {
            mThreadFactory.setType(ConnectionThreadFactory.Type.UDPSend);
            Log.e(TAG, "Before Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
            this.execute(r);

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

            Log.e(TAG, "Before Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
            this.execute(new TCPSendPacket(msg));

            Log.e(TAG, "After Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void TCPReceive(){
        try {
            mThreadFactory.setType(ConnectionThreadFactory.Type.TCPReceive);

            Log.e(TAG, "Before Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
            this.execute(new UDPSendPacket(""));

            Log.e(TAG, "After Activecount "+this.getActiveCount() + " pool size " +this.getPoolSize() + " queued " + this.getQueue().size());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
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
