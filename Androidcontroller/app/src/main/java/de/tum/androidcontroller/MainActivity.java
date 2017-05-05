package de.tum.androidcontroller;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codemonkeylabs.fpslibrary.TinyDancer;

import de.tum.androidcontroller.data.SensorData;
import de.tum.androidcontroller.network.SocketConnectionThread;
import de.tum.androidcontroller.sensors.EventListener;
import de.tum.androidcontroller.sensors.SensorDataSettings;
import de.tum.androidcontroller.sensors.SensorListener;
import de.tum.androidcontroller.sensors.SensorModel;
import de.tum.androidcontroller.views.SteeringWheelView;

public class MainActivity extends AppCompatActivity implements EventListener{

    private static final String TAG = "android_controller_tag";
    private static final boolean logging = false;

    private SensorManager mSensorManager;
    private SensorListener mSensorListener;

    //the parent linear layout in the scrollView
    private LinearLayout mParentLayout;

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

    private SensorData mLocalAccelerationHolder;
    private SensorData mLocalGyroHolder;

    private Toast mGyroToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initLayoutsAndHeadlines();

        mGyroToast = Toast.makeText(this,"",Toast.LENGTH_LONG);

        if(mSensorListener == null){
            mSensorListener = SensorModel.getInstance(this);
        }


        //get the instance of the sensor manager
        if(mSensorManager == null){
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }

        if(logging)
            Log.e(TAG, "onCreate");

        //prevent from auto lock
        keepOnScreen();
        //set max brightness
        //setBrightness(0.8f);

        //load the FPS widget
        loadFPSwidget();
    }

    // Loads the FPS widget https://github.com/friendlyrobotnyc/TinyDancer
    private void loadFPSwidget(){
        TinyDancer.create()
                .redFlagPercentage(.1f) // set red indicator for 10%....different from default
                .startingXPosition(800)
                .startingYPosition(0)
                .show(this);
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


        //init the parent layout
        mParentLayout                   = (LinearLayout) findViewById(R.id.main_fragment_parent_linear_layout);

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
    public void onGyroChanged(SensorData data) {
        mLocalGyroHolder = data; //TODO maybe not needed

        //consider it only if is a significant change
        //fast forward rotated
        if(data.getY() > SensorDataSettings.MINIMUM_CHANGE_TRIGGER_GYRO_FORWARD_BACKWARD){
            refreshToast("Detected fast forward rotation");
        }

        //fast backward rotated
        if(data.getY() < -SensorDataSettings.MINIMUM_CHANGE_TRIGGER_GYRO_FORWARD_BACKWARD){
            refreshToast("Detected fast backward rotation");
        }

        //fast right rotation
        if(data.getX() > SensorDataSettings.MINIMUM_CHANGE_TRIGGER_GYRO_LEFT_RIGHT){
            refreshToast("Detected fast right rotation");
        }

        //fast left rotation
        if(data.getX() < -SensorDataSettings.MINIMUM_CHANGE_TRIGGER_GYRO_LEFT_RIGHT){
            refreshToast("Detected fast left rotation");
        }

        setSensorDataToLayout(data,layout_gyro,mGyroValueHolder,3);
    }

    @Override
    public void onAccelerometerChanged(SensorData data) {
        if(mLocalAccelerationHolder == null){
            mLocalAccelerationHolder = data;
        }
        else{
            //consider it only if is a significant change
            //the acceleration/breaking point
            if(Math.abs(data.getX() - mLocalAccelerationHolder.getX()) > SensorDataSettings.MINIMUM_CHANGE_TRIGGER_ACCELERATION_BREAK){
                steeringWheelForwardView.drawAccelerationBrake(data.getX());
                mLocalAccelerationHolder = data;
            }//the steering point
            else if(Math.abs(data.getY() - mLocalAccelerationHolder.getY()) > SensorDataSettings.MINIMUM_CHANGE_TRIGGER_STEERING){
                steeringWheelSidewaysView.drawLeftRight(data.getY());
                mLocalAccelerationHolder = data;
            }
        }
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
     * @param sensorValue the value from the SensorData
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
    private void setSensorDataToLayout(SensorData data, LinearLayout layout, TextView textView, int decimalDigits){
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
        LinearLayout subIncludedLayout; //the included layout
        LinearLayout linearLayoutLevel1; //the included layout
        TextView keepMemoryLow = null;

        //for more details on the level-ing thing see the comments in content_main.xml
        for(int level1 = 0; level1 < mParentLayout.getChildCount(); level1++){
            if(mParentLayout.getChildAt(level1) instanceof LinearLayout){
                linearLayoutLevel1 = (LinearLayout) mParentLayout.getChildAt(level1);
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

    public void UDPSend(View view) {
        SocketConnectionThread.getInstance().UDPSend("udp send");
    }

    public void UDPReceive(View view) {
        SocketConnectionThread.getInstance().UDPReceive();
    }

    public void TCPSend(View view) {
        SocketConnectionThread.getInstance().TCPSend("tcp send");
    }

    public void TCPReceive(View view) {
        SocketConnectionThread.getInstance().TCPReceive();
    }
}
