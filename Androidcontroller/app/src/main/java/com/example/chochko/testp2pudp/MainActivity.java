package com.example.chochko.testp2pudp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    TextView infoIp, infoPort;
    TextView textViewState, textViewPrompt;

    static final int UdpServerPORT = 8001;
    static final String SERVER_IP = "192.168.2.118";
    UdpServerThread udpServerThread;

    private int TEST_CALLS_COUNT = 100000;
    private final long RECEIVE_WINDOW = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    udpServerThread = new UdpServerThread(UdpServerPORT);
                    udpServerThread.start(); //fix http://simpledeveloper.com/network-on-main-thread-error-solution/
                }
            }).start();

            }
        });

        infoIp = (TextView) findViewById(R.id.infoip);
        infoPort = (TextView) findViewById(R.id.infoport);
        textViewState = (TextView)findViewById(R.id.state);
        textViewPrompt = (TextView)findViewById(R.id.prompt);

        infoIp.setText(getIpAddress());
        infoPort.setText(String.valueOf(UdpServerPORT));
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

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onStop() {
        if(udpServerThread != null){
            udpServerThread.setRunning(false);
            udpServerThread = null;
        }

        super.onStop();
    }

    private void updateState(final String state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewState.setText(state);
            }
        });
    }

    private void updatePrompt(final String prompt){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewPrompt.append(prompt);
            }
        });
    }

    private class UdpServerThread extends Thread{

        int serverPort;
        DatagramSocket socket;
        Thread receiveThread = new Thread();
        boolean receiving = true;

        boolean running;

        public UdpServerThread(int serverPort) {
            super();
            this.serverPort = serverPort;
        }

        public void setRunning(boolean running){
            this.running = running;
        }

        @Override
        public void run() {

            running = true;

            try {
                updateState("Starting UDP Server");
                socket = new DatagramSocket(serverPort);

                updateState("UDP Server is running");
                //final ProgressDialog ringProgressDialog = getProgressDialog("Sending data","Please wait...");
                //ringProgressDialog.show();

                Log.e(TAG, "UDP Server is running");

                DatagramPacket packet;

                for(int i = 1; i <= TEST_CALLS_COUNT ; i ++){

                    byte[] buf = buildTestJSON(i).toString().getBytes();

                    // send the response to the client at "address" and "port"
                    InetAddress address = InetAddress.getByName(SERVER_IP);
                    int port = serverPort;
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    socket.send(packet);

                    updatePrompt("Request from: " + address + ":" + port + "\n");

                    String dString = new Date().toString() + "\n"
                            + "Your address " + address.toString() + ":" + String.valueOf(port);
                    buf = dString.getBytes();

                    // receive request
                    receivePackets(socket);

                    //Log.e(TAG, "received " + new String(buf));

                }

                //ringProgressDialog.dismiss();
                Log.e(TAG, "UDP Server ended");

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(socket != null){
                    socket.close();
                    Log.e(TAG, "socket.close()");
                }
            }
        }

        private void receivePackets(final DatagramSocket socket) throws IOException {
            if(receiving){
                if(!receiveThread.isAlive()){
                    Log.e(TAG, "receivePackets: here");
                    receiveThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] buf = new byte[256];
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            try {
                                socket.receive(packet);     //this code block the program flow
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String response = new String(buf);
                            Log.e(TAG, "String Response " + response);

                            try {
                                JSONObject responseJSON = new JSONObject(response);
                                Log.e(TAG, "JSON Response " + responseJSON);
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON creation failed.");
                            }
                            try {
                                receiveThread.sleep(RECEIVE_WINDOW);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    receiveThread.start();

                }
            }
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    private class UdpServerAsyncTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            return null;
        }
    }
}
