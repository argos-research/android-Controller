package com.example.konstantinvankov.testp2pwifi;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 5000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    private int queuedThreds,activeThreads, notCompleatedThreads;
    private String  msg = "Test\n" + "Test2\n" + "Test3\n"  + "Test4\n",
                    mServerIP = "192.168.2.122";
    private int port = 8000;
    private Socket mSocket;
    private final String TAG = "Test App";
    private DataOutputStream mOut = null;
    private DataInputStream mIn = null;
    private int TEST_CALLS_COUNT = 100000;

    Thread receiveThread = new Thread();
    //if this window is bigger than the sendWindow on the server THEN MSG WILL BE LOST!
    //in case this is smaller than the thread is getting stuck on readUTF and just waits there which is kind of good for me now.

    //CORRECTION TO ABOVE! IF WE ARE USING THE SAME BLUETOOTH LOGIC (write to buffer instead of readLine()) then it is WORKING
    //AS THE BLUETOOTH SERVER!
    private final long RECEIVE_WINDOW = 1000; //the receive window in millisecond. In that much second the input stream will be read
    private boolean portChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initEditTexts();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIn = null;
                mOut = null;
                startThread(false);
            }
        });
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startThread(true);
            }
        });
    }

    private void initEditTexts() {
        EditText packetsToSendText = (EditText) findViewById(R.id.packets_to_send);
        packetsToSendText.setText(String.format("%d",TEST_CALLS_COUNT));
        //http://stackoverflow.com/questions/2434532/android-set-hidden-the-keybord-on-press-enter-in-a-edittext
        packetsToSendText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


                    in.hideSoftInputFromWindow(v
                                    .getApplicationWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    // Must return true here to consume event
                    return true;
                }
                TEST_CALLS_COUNT = Integer.parseInt(v.getText().toString());


                return false;
            }
        });

        EditText portText = (EditText) findViewById(R.id.port);
        portText.setText(String.format("%d",port));
        //http://stackoverflow.com/questions/2434532/android-set-hidden-the-keybord-on-press-enter-in-a-edittext
        portText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


                    in.hideSoftInputFromWindow(v
                                    .getApplicationWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    // Must return true here to consume event
                    return true;
                }
                port = Integer.parseInt(v.getText().toString());

                if (mSocket != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(mSocket.isConnected())
                                    closeSocket();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });
    }

    public void startThread(final boolean closingAfterEach){
        queuedThreds = executor.getQueue().size();
        activeThreads = executor.getActiveCount();
        notCompleatedThreads = queuedThreds + activeThreads;
        //only if everything is ready
        if(notCompleatedThreads == 0){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(closingAfterEach){
                            for(int i = 1; i <= TEST_CALLS_COUNT ; i ++){
                                initSocket();
                                sendBytesWithClosing(buildTestJSON(i).toString());
                                readBytesWithClosing();
                                closeSocket();
                            }
                        }else{
                            initSocket();
                            for(int i = 1 ; i <= TEST_CALLS_COUNT; i++) {

                                sendBytesWithoutClosingBluetoothVersion(buildTestJSON(i).toString());
                                readBytesWithoutClosingBluetoothVersion();
                            }
                        }


                        //closeSocket();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSocket.isConnected())
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    private void initSocket() throws IOException {
        mSocket = new Socket(mServerIP,port);
    }

    public void closeSocket() throws IOException{
        mSocket.close();
    }


    public ProgressDialog getProgressDialog(String title, String msg){
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public JSONObject buildTestJSON(int i){
        JSONObject ob = new JSONObject();
        JSONArray accVal = new JSONArray();

        try {
            accVal.put(3.56);
            accVal.put(-2.56);
            accVal.put(23.56);

            ob.put("Loop"               ,i);
            ob.put("Loops count"        ,TEST_CALLS_COUNT);
            ob.put("Accelerometer data" ,accVal);
            ob.put("Gyro data"          ,accVal);
            ob.put("Created time"       ,System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ob;
    }

    public void sendBytesWithClosing(String message)throws IOException{

        OutputStream out = mSocket.getOutputStream();
        mOut= new DataOutputStream(out);
        mOut.writeUTF(message);
        mOut.flush();
    }

    public void readBytesWithClosing() throws IOException{

        mIn = new DataInputStream(mSocket.getInputStream());
        Log.e(TAG, "readBytes-begin");
        Log.e(TAG, "readBytes: " + mIn.readUTF());
    }

    //BEST!
    public void sendBytesWithoutClosing(String message) throws IOException{
        if(mOut == null){
            OutputStream out = mSocket.getOutputStream();
            mOut= new DataOutputStream(out);
        }
        mOut.writeUTF(message);
        mOut.flush();
    }

    //BEST!
    public void readBytesWithoutClosing() throws IOException
    {
        if(mIn == null)
            mIn = new DataInputStream(mSocket.getInputStream());
        Log.e(TAG, "readBytes-begin");
        Log.e(TAG, "readBytes: " + mIn.readUTF());

    }

    private int threadCount = 1;
    public void readBytesWithoutClosingBluetoothVersion() throws IOException
    {
        if(!receiveThread.isAlive()) {

            //according to http://stackoverflow.com/questions/14494352/can-you-write-to-a-sockets-input-and-output-stream-at-the-same-time
            // read should be in a separate thread
            receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "run: running thread "+ threadCount++);
                        if(mIn == null)
                            mIn = new DataInputStream(mSocket.getInputStream());
                        if(mSocket.isConnected()) //otherwise is get stuck when it return from the sleep state
                            //Log.e(TAG, "readBytes: " + mIn.readUTF());
                        {
                            byte inputBuffer[] = new byte[1024];
                            int bytes_read = mIn.read( inputBuffer );
                            String received = new String(inputBuffer, 0, bytes_read);
                            Log.e(TAG, "Message received from the server: "+ received);
                        }
                        receiveThread.sleep(RECEIVE_WINDOW);
                    } catch (InterruptedException | IOException e) {
                        Log.e(TAG, "An exception has occurred during reading the input stream: " + e.toString());
                    }
                }
            });
            receiveThread.start();


        }

    }

    public void sendBytesWithoutClosingBluetoothVersion(String message) throws IOException{
        if(mOut == null){
            OutputStream out = mSocket.getOutputStream();
            mOut= new DataOutputStream(out);
        }
        mOut.writeUTF(message);
        mOut.flush();
    }

    public void readBytes() throws IOException
    {
        String finalText = "";

        Log.e(TAG, "readBytes-begin");
        InputStream in = mSocket.getInputStream();
        BufferedReader buffreader = new BufferedReader(new InputStreamReader(in));

        String text = "";
        while ((text = buffreader.readLine()) != null)
        {
            finalText += text;
            System.out.println(text);
        }

        Log.e(TAG, "readBytes: " + finalText);

        buffreader.close();


//        Log.e(TAG, "readBytes-begin");
//        InputStream in = mSocket.getInputStream();
//        BufferedReader buffreader = new BufferedReader(new InputStreamReader(in));
//        String line = "";
//        while (buffreader.ready())
//        {
//            line = buffreader.readLine();
//            finalText += line;
//            Log.d(TAG, "readBytes: current line "+line);
//        }
//
//        //byte[] myByteArray = finalText.getBytes();
//        Log.e(TAG, "readBytes: " + finalText);
//
//        buffreader.close();


//        Log.e(TAG, "readBytes-begin");
//        Scanner in = new Scanner(mSocket.getInputStream());
//        Log.d(TAG, "readBytes: after scanner");
//        while (in.hasNextLine())     // infinity problem http://stackoverflow.com/questions/8352040/scanner-on-text-file-hasnext-is-infinite
//        {
//            Log.d(TAG, "readBytes: In while");
//            finalText += in.nextLine();//IF THERE IS INPUT THEN MAKE A NEW VARIABLE input AND READ WHAT THEY TYPED
//            Log.d(TAG, "readBytes: Server Said: " + finalText);//PRINT IT OUT TO THE SCREEN
//
//            //out.flush();//FLUSH THE STREAM
//        }
//
//        in.close();
//        Log.e(TAG, "readBytes: " + finalText);


    }

    public int sendBytes(byte[] myByteArray) throws IOException
    {
        return sendBytes(myByteArray, 0, myByteArray.length);
    }//sendBytes end

    public int sendBytes(byte[] myByteArray, int start, int len) throws IOException
    {
        Log.e(TAG, "sendBytes-begin");
        if (len < 0)
        {
            throw new IllegalArgumentException("Negative length not allowed");
        }
        if (start < 0 || start >= myByteArray.length)
        {
            throw new IndexOutOfBoundsException("Out of bounds: " + start);
        }
        OutputStream out = mSocket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        // dos.writeInt(len);
//        if (len > 0)
//        {
//            dos.write(myByteArray, start, len);
//        }
//        int size=dos.size();
//        dos.flush();

        dos.writeUTF(msg);
        dos.flush();
        Log.e(TAG, "sendBytes-end");

        return 0;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onPacketsToSendChange(View view) {

    }
}
