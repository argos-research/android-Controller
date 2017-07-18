package de.tum.androidcontroller.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.ColorRes;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.codemonkeylabs.fpslibrary.TinyDancer;

import org.json.JSONException;

import java.util.Locale;

import de.tum.androidcontroller.R;
import de.tum.androidcontroller.UInput.uInputValuesHolder;
import de.tum.androidcontroller.connections.models.ConnectionRunnableModels;
import de.tum.androidcontroller.connections.models.EncodedSentModel;
import de.tum.androidcontroller.connections.models.ReceivedDataModel;
import de.tum.androidcontroller.connections.utils.BluetoothUtils;
import de.tum.androidcontroller.connections.utils.ConnectionUtils;
import de.tum.androidcontroller.data.SettingsService;
import de.tum.androidcontroller.models.SensorBaseModel;
import de.tum.androidcontroller.connections.SocketConnectionThread;
import de.tum.androidcontroller.sensors.EventListener;
import de.tum.androidcontroller.sensors.SensorDataSettings;
import de.tum.androidcontroller.sensors.SensorListener;
import de.tum.androidcontroller.sensors.Vibration;
import de.tum.androidcontroller.views.SteeringWheelView;

public class MainActivity   extends AppCompatActivity
                            implements EventListener,
                            SocketConnectionThread.ConnectionCallback{

    private static final String TAG = "MainActivity";
    private static final boolean logging = false;

    private SensorManager mSensorManager;
    private SensorListener mSensorListener;

    //the parent linear layout in the scrollView
    private LinearLayout mParentCalibrationLayout;
    private ScrollView mCalibrationScrollView;
    private volatile boolean isCalibrationViewActive = false; //dummy boolean for not changing the whole application because of the sensor callback which can be 1 to 1 and I will have to use Broadcast. TODO use broadcast

    /* the main activity holders */
    private LinearLayout mMainPlayActivityLayout;
    private TextView mPositionText;
    private TextView mGearText;
    private TextView mSpeedText;


    //Used for holder each included sensor layouts
    private LinearLayout layout_accelerometer;
    private LinearLayout layout_gyro;
    private LinearLayout layout_linear_accelerometer;
    private LinearLayout layout_magnetic_field;
    private LinearLayout layout_rotation_vector;

    //Used for holding and presenting each and for less memory usage
    private volatile TextView mAccelerometerValueHolder;
    private volatile TextView mGyroValueHolder;
    private volatile TextView mLinearAccelerometerValueHolder;
    private volatile TextView mMagneticFieldValueHolder;
    private volatile TextView mRotationVectorValueHolder;

    private SteeringWheelView steeringWheelForwardView;
    private SteeringWheelView steeringWheelSidewaysView;

    private SensorBaseModel mLocalAccelerationHolder;
    private SensorBaseModel mLocalGyroLastSendHolder;

    private EncodedSentModel encodedGyroData;



    private Toast mGyroToast;

    private SocketConnectionThread mCommunicationThread;


    private final int SETTINGS_ACTIVITY_IDENTIFIER = 1;    //used for starting new activity for result

    private volatile boolean sending = false;   //used to stop sending data to the server when there is no connection or a initialization is done in the background

    private boolean significantAccChange  = false;
    private boolean significantGyroChange = false;

    private ProgressDialog mProgressDialog;

    //marks whether the accelerometer data should be used or not
    private volatile boolean isAccelerometerChecked = true;

    //used for preventing the error dialog to be shown more than once and to build on top of each other
    private volatile boolean isErrorDialogShown = false;

    //used for preventing the error dialog to pop up when we stop the communication in order to go to the settings activity
    private volatile boolean isGoingToSettingsActivity = false;

    private volatile Vibrator vibrator = null;


    private volatile BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null){
                if(action.equals(ConnectionRunnableModels.BROADCAST_ACTION_FAILURE)){
                    if(intent.hasExtra(ConnectionRunnableModels.BROADCAST_INFORMATION_KEY)){
                        onConnectionError(intent.getStringExtra(ConnectionRunnableModels.BROADCAST_INFORMATION_KEY));
                    }
                }else if(action.equals(ConnectionRunnableModels.BROADCAST_ACTION_RECEIVE)){
                    if(intent.hasExtra(ConnectionRunnableModels.BROADCAST_INFORMATION_KEY)){
                        //Log.e(TAG, "BROADCAST RECEIVED " + intent.getStringExtra(ConnectionRunnableModels.BROADCAST_INFORMATION_KEY));
                        final ReceivedDataModel model = intent.getParcelableExtra(ConnectionRunnableModels.BROADCAST_INFORMATION_KEY);
                        //update only if the correct element is shown
                        if(!isCalibrationViewActive && model != null){
                            /* The position */
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String lastPosition = mPositionText.getText().toString();
                                    String currentPosition = String.format("%d/%d",model.getCurrentPosition(),model.getNumberOfCars());
                                    mPositionText.setText(currentPosition);
                                    //if the current position has changed then vibrate`
                                    if(!lastPosition.equals(currentPosition)){
                                        Vibration.getInstance(vibrator).onPositionChangedVibration();
                                    }

                                    /* The gear */
                                    String lastGear= mGearText.getText().toString();
                                    String currentGear = String.format("%s/%d",model.getGearString(),model.getGearTotalWithoutRandN());
                                    mGearText.setText(currentGear);
                                    //if the gear has changed, vibrate
                                    if(!lastGear.equals(currentGear)){
                                        Vibration.getInstance(vibrator).onGearChangedVibration();
                                    }

                                    /* The speed */
                                    mSpeedText.setText(String.format("%.1f",model.getSpeedInKmPerHour()));
                                }
                            });

                        }
                    }
                }
            }


        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if we are coming from the settings activity
        if(requestCode == SETTINGS_ACTIVITY_IDENTIFIER) {

            isGoingToSettingsActivity = false;

            //new settings are saved
            if (resultCode == Activity.RESULT_OK) {

                final Context myInstance = this;

                initWaitDialog();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "run: in the main thread-> Shutting down the communication thread");
                        //stop sending
                        sending = false;

                        //close the current communication
                        //mCommunicationThread.closeConnection();
                        mCommunicationThread.shutdownNow();

                        //wait for it to finish
                        //while(mCommunicationThread.getActiveCount() + mCommunicationThread.getQueue().size() > 0);

                        Log.e(TAG, "onActivityResult: connection closed");

                        //init with the new communication method
                        mCommunicationThread = new SocketConnectionThread(SettingsService.ConnectionType.fromText(getSettingsData().getConnectionType()), myInstance);

                        Log.e(TAG, "run: in the main thread READY");

                    }
                }).start();

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initLayoutsAndHeadlines();

        initWaitDialog();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        mGyroToast = Toast.makeText(this,"",Toast.LENGTH_LONG);

        //as we will start with the main view, disable it for now although it does not change anything
        changeAccelerometerToggle(false);

        startBluetooth();

        mCommunicationThread = new SocketConnectionThread(SettingsService.ConnectionType.fromText(getSettingsData().getConnectionType()),this);


        if(mSensorListener == null){
            mSensorListener = de.tum.androidcontroller.sensors.SensorModel.getInstance(this);
        }


        //get the instance of the sensor manager
        if(mSensorManager == null){
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }

        //register the broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectionRunnableModels.BROADCAST_ACTION_FAILURE);
        intentFilter.addAction(ConnectionRunnableModels.BROADCAST_ACTION_RECEIVE);
        registerReceiver(broadcastReceiver,intentFilter);


        if(logging)
            Log.e(TAG, "onCreate");

        //prevent from auto lock
        keepOnScreen();
        //set max brightness
        //setBrightness(0.8f);

        //load the FPS widget
        //loadFPSwidget();

        //initialize the button listeners
        initButtonListeners();
    }

    private void changeAccelerometerToggle(boolean state){
        ToggleButton accelToggle = (ToggleButton) findViewById(R.id.accelerometer_toggle);
        isAccelerometerChecked = state;
        accelToggle.setChecked(isAccelerometerChecked);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mCommunicationThread.closeConnection();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * Enable the bluetooth if the device has a bluetooth adapter.
     */
    private void startBluetooth(){
        //TODO add bluetooth pairing https://stackoverflow.com/questions/17168263/how-to-pair-bluetooth-device-programmatically-android
        boolean isBluetoothEnabled = false;
        boolean isBluetoothAvailable = false;
        try{
            isBluetoothEnabled = BluetoothUtils.isBluetoothEnabled();
            isBluetoothAvailable = true;
        } catch (Exception e) {
            this.onConnectionError(e.getMessage());
        }

        if(!isBluetoothEnabled && isBluetoothAvailable){
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothAdapter.enable();
            Log.e(TAG, "startBluetooth: enabled");

        }


    }
    // Loads the FPS widget https://github.com/friendlyrobotnyc/TinyDancer
    private void loadFPSwidget(){
        TinyDancer.create()
                .redFlagPercentage(.1f) // set red indicator for 10%....different from default
                .startingXPosition(800)
                .startingYPosition(0)
                .show(this);
    }

    private void hideView(View someView){
        someView.setVisibility(View.GONE);
    }

    private void showView(View someView){
        someView.setVisibility(View.VISIBLE);
    }

    /**
     * Initializes the button listener for the main three buttons in the activity
     */
    private void initButtonListeners() {
        Button btnL         = (Button) findViewById(R.id.main_button_left);
        Button btnR         = (Button) findViewById(R.id.main_button_right);
        Button btnStart     = (Button) findViewById(R.id.main_button_start);

        btnL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sending){
                    try {
                        if(isPresentationClicked){
                            int button = uInputValuesHolder.KEY_LEFT;
                            mCommunicationThread.sendMsg(ConnectionUtils.buildKeyPressJSON(button).toString());
                        }else{
                            mCommunicationThread.sendMsg(ConnectionUtils.buildKeyPressJSON(EncodedSentModel.BTN_LEFT_CODE).toString());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btnR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sending){
                    try {
                        if(isPresentationClicked){
                            int button = uInputValuesHolder.KEY_RIGHT;
                            mCommunicationThread.sendMsg(ConnectionUtils.buildKeyPressJSON(button).toString());
                        }else{
                            mCommunicationThread.sendMsg(ConnectionUtils.buildKeyPressJSON(EncodedSentModel.BTN_RIGHT_CODE).toString());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sending){
                    try {
                        mCommunicationThread.sendMsg(ConnectionUtils.buildKeyPressJSON(EncodedSentModel.BTN_START_GAME_CODE).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    /**
     * Used for refreshing the toast message from the gyro sensor events
     * @param message the message to be displayed
     */
    private void refreshToast(String message){
        mGyroToast.setText(message);
        mGyroToast.show();
    }

    private void initLayoutsAndHeadlines(){
        //init the steering wheels
        steeringWheelForwardView = (SteeringWheelView) findViewById(R.id.steering_wheel_forward);
        steeringWheelSidewaysView = (SteeringWheelView) findViewById(R.id.steering_wheel_sideways);


        //init the both main activities here
        mMainPlayActivityLayout         = (LinearLayout) findViewById(R.id.main_main_play_linear_layout);
        mCalibrationScrollView          = (ScrollView) findViewById(R.id.main_scroll_view_calibration);

        //init the parent layout
        mParentCalibrationLayout        = (LinearLayout) mCalibrationScrollView.findViewById(R.id.main_fragment_calibration_linear_layout);

        //init the main play screen edit text holders
        mPositionText                   = (TextView) mMainPlayActivityLayout.findViewById(R.id.main_position_value);
        mGearText                       = (TextView) mMainPlayActivityLayout.findViewById(R.id.main_gear_value);
        mSpeedText                      = (TextView) mMainPlayActivityLayout.findViewById(R.id.main_speed_value);


        //init the included layouts
        layout_gyro                     = (LinearLayout) findViewById(R.id.content_main_gyro);
        layout_accelerometer            = (LinearLayout) findViewById(R.id.content_main_acceleration);

        TextView headline = (TextView) layout_gyro.findViewById(R.id.fragmet_sensor_data_type_sensor);
        headline.setText(R.string.headline_gyro);



        headline = (TextView) layout_accelerometer.findViewById(R.id.fragmet_sensor_data_type_sensor);
        headline.setText(R.string.headline_accelerometer);

    }
    @Override
    protected void onResume() {
        super.onResume();

        if(isCalibrationViewActive){
            showView(mCalibrationScrollView);
            hideView(mMainPlayActivityLayout);
        }else{
            showView(mMainPlayActivityLayout);
            hideView(mCalibrationScrollView);
        }
        mSensorListener.onResume(mSensorManager);
        if(logging)
            Log.e(TAG, "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorListener.onPause(mSensorManager);

        if(logging)
            Log.e(TAG, "onPause");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    private boolean isPresentationClicked = false;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this,SettingsActivity.class), SETTINGS_ACTIVITY_IDENTIFIER);
            isGoingToSettingsActivity = true;
            mCommunicationThread.closeConnection();
            return true;
        } else if(id == R.id.action_calibration){
            showView(mCalibrationScrollView);
            hideView(mMainPlayActivityLayout);
            //make sure it is enabled
            changeAccelerometerToggle(true);
            isCalibrationViewActive = true;
            return true;
        } else if(id == R.id.action_main_view){
            showView(mMainPlayActivityLayout);
            hideView(mCalibrationScrollView);
            isCalibrationViewActive = false;
            return true;
        } else if(id == R.id.action_presentation_mode){
            isPresentationClicked = !isPresentationClicked;
            if(isPresentationClicked){
                Toast.makeText(this, "Presentation mode enabled",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Presentation mode disabled",Toast.LENGTH_SHORT).show();
            }




        }

        return super.onOptionsItemSelected(item);
    }



    private void initWaitDialog(){
        mProgressDialog = getProgressDialog(getString(R.string.activity_main_progress_dialog_init_message),getString(R.string.activity_main_progress_dialog_wait_message));
        mProgressDialog.show();
    }

    /**
     * An instance of {@link AlertDialog} for displaying additional
     * information on the screen while initializing information in the background
     * or showing if the connection was successful or not.
     * @param title the title of it
     * @param msg the message that will be shown
     * @return a custom instance of {@link AlertDialog}
     */
    public AlertDialog getAlertDialog(String title, String msg){
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
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

    /**
     * Helper method for displaying additional information from the
     * communication with the server from {@link SocketConnectionThread}.
     * @param state whether the initialization of the the given
     *              communication technology was successful or not.
     */
    @Override
    public void onConnectionInitResponse(boolean state, final String additionInformation) {

        mProgressDialog.dismiss();
        final Context myInstance = this;

        if(state){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //AlertDialog alertDialog = getAlertDialog("Initialization successful",String.format("Connection established with %s:%d",getSettingsData().getServerIP(),getSettingsData().getServerPort()));
                    AlertDialog alertDialog = getAlertDialog("Initialization successful",additionInformation);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    //resume sending
                                    sending = true;
                                }
                            });
                    /**
                     * android.view.WindowLeaked: Activity de.tum.androidcontroller.activities.MainActivity has leaked window com.android.internal.policy.PhoneWindow$DecorView{d0dbb06 V.E...... R....... 0,0-1026,602} that was originally added here
                     at android.view.ViewRootImpl.<init>(ViewRootImpl.java:375)
                     at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:299)
                     at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:86)
                     at android.app.Dialog.show(Dialog.java:319)
                     at de.tum.androidcontroller.activities.MainActivity$2.run(MainActivity.java:322)F
                     */
                    alertDialog.show();
                }
            });

        }else{

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //AlertDialog alertDialog = getAlertDialog("Initialization failed","There was a problem connecting to the server. Please check the your settings and make sure the server is running!");
                    AlertDialog alertDialog = getAlertDialog("Initialization failed",additionInformation);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Go to Settings",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    startActivityForResult(new Intent(myInstance,SettingsActivity.class), SETTINGS_ACTIVITY_IDENTIFIER);
                                }
                            });
                    /**
                     * android.view.WindowLeaked: Activity de.tum.androidcontroller.activities.MainActivity has leaked window com.android.internal.policy.PhoneWindow$DecorView{d0dbb06 V.E...... R....... 0,0-1026,602} that was originally added here
                     at android.view.ViewRootImpl.<init>(ViewRootImpl.java:375)
                     at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:299)
                     at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:86)
                     at android.app.Dialog.show(Dialog.java:319)
                     at de.tum.androidcontroller.activities.MainActivity$2.run(MainActivity.java:322)F
                     */
                    alertDialog.show();
                }
            });
        }
    }

    private void onConnectionError(final String errorInformation) {
        final Context myInstance = this;

        sending = false; //set it to false because something wen't wrong and the settings activity should be opened and in onActivityResult this value will be set to true.


        if (!isErrorDialogShown && !isGoingToSettingsActivity) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //AlertDialog alertDialog = getAlertDialog("Initialization failed","There was a problem connecting to the server. Please check the your settings and make sure the server is running!");
                    isErrorDialogShown = true;
                    AlertDialog alertDialog = getAlertDialog("Server not reachable",errorInformation);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Go to Settings",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    isErrorDialogShown = false;
                                    startActivityForResult(new Intent(myInstance,SettingsActivity.class), SETTINGS_ACTIVITY_IDENTIFIER);
                                }
                            });
                    alertDialog.show();
                }
            });
        }
    }

    @Override
    public void onGyroChanged(SensorBaseModel data) {
        long currentTime = System.currentTimeMillis();
        
        if(mLocalGyroLastSendHolder == null) {
            mLocalGyroLastSendHolder = data;
            mLocalGyroLastSendHolder.setLastTimeDatSend(currentTime);
        }

        if(encodedGyroData != null)
            encodedGyroData.resetValues();
        else
            encodedGyroData = new EncodedSentModel(0,0,0,0);


        //consider any change only if enough time went by
        //done for reducing for same event send many times because when you rotate the phone very fast
        //then the gyro value for the given axis very quickly increases which make the program to send
        //this event more than once per a single fast rotation in some direction. That is why
        //this if is required in order to fire these events once per SensorDataSettings.MINIMUM_TIME_TO_WAIT_GYRO milis.
        if (Math.abs(currentTime - mLocalGyroLastSendHolder.getLastTimeDatSend()) > SensorDataSettings.MINIMUM_TIME_TO_WAIT_GYRO) {
            //Log.e(TAG, "onGyroChanged: here");

            //consider it only if is a significant change
            //fast forward rotated
            if(data.getY() > SensorDataSettings.MINIMUM_CHANGE_TRIGGER_GYRO_FORWARD_BACKWARD ){
                refreshToast("Detected fast forward rotation");
                significantGyroChange = true;
                encodedGyroData.setForward(EncodedSentModel.FORWARD_KEY_CODE);
            }

            //fast backward rotated
            if(data.getY() < - SensorDataSettings.MINIMUM_CHANGE_TRIGGER_GYRO_FORWARD_BACKWARD ){
                refreshToast("Detected fast backward rotation");
                significantGyroChange = true;
                encodedGyroData.setBackward(EncodedSentModel.BACKWARD_KEY_CODE);
            }

            //fast right rotation
            if(data.getX() > SensorDataSettings.MINIMUM_CHANGE_TRIGGER_GYRO_LEFT_RIGHT){
                refreshToast("Detected fast right rotation");
                significantGyroChange = true;
                encodedGyroData.setRight(EncodedSentModel.RIGHT_KEY_CODE);
            }

            //fast left rotation
            if(data.getX() < -SensorDataSettings.MINIMUM_CHANGE_TRIGGER_GYRO_LEFT_RIGHT){
                refreshToast("Detected fast left rotation");
                significantGyroChange = true;
                encodedGyroData.setLeft(EncodedSentModel.LEFT_KEY_CODE);
            }

            //if it is a significant change and the connection is established
            // => send it to the server
            if(significantGyroChange && sending) {
                Log.e(TAG, "onGyroChanged: SENDING");
                try {
                    mCommunicationThread.sendMsg(ConnectionUtils.buildGyroJSON(encodedGyroData.toJSONObject()).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mLocalGyroLastSendHolder = data;
                mLocalGyroLastSendHolder.setLastTimeDatSend(currentTime);

                significantGyroChange = false;
            }
        }

        setSensorDataToLayout(data,layout_gyro,mGyroValueHolder,3);
    }

    @Override
    public void onAccelerometerChanged(SensorBaseModel data) {
        if(mLocalAccelerationHolder == null){
            mLocalAccelerationHolder = data;
        }
        else{

            //consider it only if is a significant change
            //the acceleration/breaking point
            if(Math.abs(data.getX() - mLocalAccelerationHolder.getX()) > SensorDataSettings.MINIMUM_CHANGE_TRIGGER_ACCELERATION_BREAK  && Math.abs(data.getX()) <= SensorDataSettings.maximumAccelerationBreakDeviation){
                //if(Math.abs(data.getX() - mLocalAccelerationHolder.getX()) > SensorDataSettings.MINIMUM_CHANGE_TRIGGER_ACCELERATION_BREAK ){
                significantAccChange = true;
                if (isCalibrationViewActive) {
                    if(isAccelerometerChecked)
                        steeringWheelForwardView.drawAccelerationBrake(data.getX());
                }
                mLocalAccelerationHolder = data;
            }
//            if(Math.abs(data.getX()) == SensorDataSettings.maximumAccelerationBreakDeviation){
//
//            }

            //the steering point
//            else if(Math.abs(data.getY() - mLocalAccelerationHolder.getY()) > SensorDataSettings.MINIMUM_CHANGE_TRIGGER_STEERING ){
           if(Math.abs(data.getY() - mLocalAccelerationHolder.getY()) > SensorDataSettings.MINIMUM_CHANGE_TRIGGER_STEERING && Math.abs(data.getY()) <= SensorDataSettings.maximumLeftRightDeviation){
                significantAccChange = true;
                if (isCalibrationViewActive) {
                    if(isAccelerometerChecked)
                        steeringWheelSidewaysView.drawLeftRight(data.getY());
                }
                mLocalAccelerationHolder = data;
            }

            //if it is a significant change, the connection is established and isAccelerometerChecked is checked (used to disable the this sensor in order to use just the gyro buttons in Speed Dreams)
            // => send it to the server
            if(significantAccChange && sending) {
                //send the proper values to the server
//                Log.e(TAG, "onAccelerometerChanged: SENDING");
//                mCommunicationThread.sendMsg(buildTestJSON(i++).toString());
                try {
                    if (isCalibrationViewActive) {
                        if (isAccelerometerChecked) {
                            mCommunicationThread.sendMsg(ConnectionUtils.buildAccelerometerJSON(ConnectionUtils.toEncodedAccelerometerModel(data).toJSONObject()).toString());
                        }
                    } else{
                        mCommunicationThread.sendMsg(ConnectionUtils.buildAccelerometerJSON(ConnectionUtils.toEncodedAccelerometerModel(data).toJSONObject()).toString());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                significantAccChange = false;
            }
        }
        if(isCalibrationViewActive)
            setSensorDataToLayout(data,layout_accelerometer,mAccelerometerValueHolder,3);
    }

    /**
     * Keeps the screen always awake
     */
    private void keepOnScreen(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Change the phone brightness pragmatically
     * @param brightness the new brightness. True values are in [0,1]
     */
    private void setBrightness(float brightness){
        if(brightness>1.0f || brightness <= 0.0f)
            return;
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = brightness;
        getWindow().setAttributes(layout);
    }


    /**
     * Used for setting custom string format on the screen for each sensor
     * @param sensorValue the value from the SensorBaseModel
     * @param decimalDigits the maximal digits to be shown on the screen. They should be in [0,5]
     * @return equal format for each sensor
     */
    private String getFormattedValue(float sensorValue, int decimalDigits){
        switch (decimalDigits){
            case 0: return String.format("%.0f",sensorValue);
            case 1: return String.format("%.1f",sensorValue);
            case 2: return String.format("%.2f",sensorValue);
            case 3: return String.format("%.3f",sensorValue);
            case 4: return String.format("%.3f",sensorValue);
            default: return String.format("%.5f",sensorValue);
        }
    }

    /**
     * Instead of doing it for each interface.
     * @param data the data provided from the callback interface
     * @param layout the according layout where the data should be provided
     * @param textView the textView holder of the element
     * @param decimalDigits the maximal digits to be shown on the screen. They should be in [0,5]
     */
    private void setSensorDataToLayout(SensorBaseModel data, LinearLayout layout, TextView textView, int decimalDigits){
        textView = (TextView) layout.findViewById(R.id.value_x);
        textView.setText(getFormattedValue(data.getX(),decimalDigits));
        textView = (TextView) layout.findViewById(R.id.value_y);
        textView.setText(getFormattedValue(data.getY(),decimalDigits));
        textView = (TextView) layout.findViewById(R.id.value_z);
        textView.setText(getFormattedValue(data.getZ(),decimalDigits));

        //set the max and min values
        setMinMaxValues(data.getX(), layout, textView, decimalDigits, R.id.value_min_x, R.id.value_max_x);
        setMinMaxValues(data.getY(), layout, textView, decimalDigits, R.id.value_min_y, R.id.value_max_y);
        setMinMaxValues(data.getZ(), layout, textView, decimalDigits, R.id.value_min_z, R.id.value_max_z);
    }

    /**
     * This function sets the min and max of every sensor and every axis
     * @param in the new value from the sensor data
     * @param layout the layout of the sensor
     * @param textView the textview holder of that layout
     * @param decimalDigits the maximal digits to be shown on the screen. They should be in [0,5]
     * @param R_id_min the R.id location of the min component
     * @param R_id_max the R.id location of the max component
     */
    private void setMinMaxValues(float in,LinearLayout layout,TextView textView, int decimalDigits, int R_id_min, int R_id_max){
        float minValue,maxValue;
        textView = (TextView) layout.findViewById(R_id_min);
        minValue = Float.valueOf(textView.getText().toString().equals("") ? "0.0" : textView.getText().toString());
        if(in < minValue)
            textView.setText(getFormattedValue(in,decimalDigits));

        textView = (TextView) layout.findViewById(R_id_max);
        maxValue = Float.valueOf(textView.getText().toString().equals("") ? "0.0" : textView.getText().toString());
        if(in > maxValue)
            textView.setText(getFormattedValue(in,decimalDigits));
    }

    /**
     * This function reset the min/max values on each sensor
     * @param view
     */
    public void resetMaxMinValues(View view) {
        //test vibration
        Vibration.getInstance(vibrator).onPositionChangedVibration();

        LinearLayout subIncludedLayout; //the included layout
        LinearLayout linearLayoutLevel1; //the included layout
        TextView keepMemoryLow = null;

        //for more details on the level-ing thing see the comments in content_main.xml
        for(int level1 = 0; level1 < mParentCalibrationLayout.getChildCount(); level1++){
            if(mParentCalibrationLayout.getChildAt(level1) instanceof LinearLayout){
                linearLayoutLevel1 = (LinearLayout) mParentCalibrationLayout.getChildAt(level1);
                for(int level2 = 0 ; level2 < linearLayoutLevel1.getChildCount() ; level2++){
                    if(linearLayoutLevel1.getChildAt(level2) instanceof LinearLayout){
                        //the included layout in the content main
                        subIncludedLayout = (LinearLayout) linearLayoutLevel1.getChildAt(level2);
                        resetMaxMinOnLayout(subIncludedLayout, keepMemoryLow);
                    }
                }
            }

        }
    }

    /**
     * This function reset the min/max values on custom sensor
     * @param subIncludedLayout the ll of the included sensor layout
     * @param memoryHelper prevents from many initializations
     */
    private void resetMaxMinOnLayout(LinearLayout subIncludedLayout, TextView memoryHelper){
        //first the min values
        memoryHelper = (TextView) subIncludedLayout.findViewById(R.id.value_min_x);
        memoryHelper.setText(R.string.default_empty_text_view_value);
        memoryHelper = (TextView) subIncludedLayout.findViewById(R.id.value_min_y);
        memoryHelper.setText(R.string.default_empty_text_view_value);
        memoryHelper = (TextView) subIncludedLayout.findViewById(R.id.value_min_z);
        memoryHelper.setText(R.string.default_empty_text_view_value);

        //after that the max values
        memoryHelper = (TextView) subIncludedLayout.findViewById(R.id.value_max_x);
        memoryHelper.setText(R.string.default_empty_text_view_value);
        memoryHelper = (TextView) subIncludedLayout.findViewById(R.id.value_max_y);
        memoryHelper.setText(R.string.default_empty_text_view_value);
        memoryHelper = (TextView) subIncludedLayout.findViewById(R.id.value_max_z);
        memoryHelper.setText(R.string.default_empty_text_view_value);
    }

//    public JSONObject buildTestJSON(int i){
//        JSONObject ob = new JSONObject();
//        JSONArray accVal = new JSONArray();
//
//        try {
////            accVal.put(3.56);
////            accVal.put(-2.56);
////            accVal.put(23.56);
//
//            accVal.put(mLocalAccelerationHolder.getX());
//            accVal.put(mLocalAccelerationHolder.getY());
//            accVal.put(mLocalAccelerationHolder.getZ());
//
//            ob.put("Loop"               ,i);
//            ob.put("Loops count"        ,TEST_CALLS_COUNT);
//            ob.put("Accelerometer data" ,accVal);
//            ob.put("Gyro data"          ,accVal);
//            ob.put("Created time"       ,System.currentTimeMillis());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return ob;
//    }


    public void sendData(View view) {
//        if (sending) {
//            workerDummy = new Thread(new Runnable() {     //WHY?!
//                @Override
//                public void run() {
//                    for(int i = 1; i < TEST_CALLS_COUNT; i++) {
//                        if (sending) {
//                            mCommunicationThread.sendMsg(buildTestJSON(i).toString());
////                            try {
////                                Thread.sleep(500);
////                            } catch (InterruptedException e) {
////                                e.printStackTrace();
////                            }
//                        }else
//                            break;
//                    }
//                }
//            });
//            workerDummy.start();
//        }

//        for(int i = 1; i < TEST_CALLS_COUNT; i++) {
//            final String msg = buildTestJSON(i).toString();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    mCommunicationThread.sendMsg(msg);
//                }
//            }).start();
//        }
    }

    /**
     * Obtaining the singleton instane of the <b>SettingsService</b>
     * @return {@link SettingsService}
     */
    private SettingsService getSettingsData(){
        return SettingsService.getInstance(this);
    }

    public void changeAccelerometerSendingState(View view) {
        ToggleButton accelToggle = (ToggleButton) view;
        isAccelerometerChecked = accelToggle.isChecked();
    }
}
