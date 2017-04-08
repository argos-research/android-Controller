package de.tum.androidcontroller;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tum.androidcontroller.data.SensorData;
import de.tum.androidcontroller.sensors.EventListener;
import de.tum.androidcontroller.sensors.SensorListener;
import de.tum.androidcontroller.sensors.SensorModel;

public class MainActivity extends AppCompatActivity implements EventListener{

    private static final String TAG = "android_controller_tag";
    private static final boolean logging = true;

    private SensorManager mSensorManager;
    private SensorListener mSensorListener;

    //Used for holder each included sensor layouts
    private LinearLayout layout_accelerometer;
    private LinearLayout layout_gyro;
    private LinearLayout layout_linear_accelerometer;
    private LinearLayout layout_magnetic_field;
    private LinearLayout layout_rotation_vector;

    //Used for holding and presenting each
    private volatile TextView mAccelerometerValueHolder;
    private volatile TextView mGyroValueHolder;
    private volatile TextView mLinearAccelerometerValueHolder;
    private volatile TextView mMagneticFieldValueHolder;
    private volatile TextView mRotationVectorValueHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        initLayoutsAndHeadlines();

        mSensorListener = SensorModel.getInstance(this);

        //get the instance of the sensor manager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(logging)
            Log.e(TAG, "onCreate");

        //prevent from auto lock
        keepOnScreen();
        //set max brightness
        setBrightness(0.8f);
    }

    private void initLayoutsAndHeadlines(){
        //init the included layouts
        layout_accelerometer            = (LinearLayout) findViewById(R.id.content_main_accelerometer);
        layout_gyro                     = (LinearLayout) findViewById(R.id.content_main_gyro);
        layout_linear_accelerometer     = (LinearLayout) findViewById(R.id.content_main_linear_accelerometer);
        layout_magnetic_field           = (LinearLayout) findViewById(R.id.content_main_magnetic_field);
        layout_rotation_vector          = (LinearLayout) findViewById(R.id.content_main_rotation_vector);

        TextView headline = (TextView) layout_accelerometer.findViewById(R.id.fragmet_sensor_data_type_sensor);
        headline.setText(R.string.headline_accelerometer);

        headline = (TextView) layout_gyro.findViewById(R.id.fragmet_sensor_data_type_sensor);
        headline.setText(R.string.headline_gyro);

        headline = (TextView) layout_linear_accelerometer.findViewById(R.id.fragmet_sensor_data_type_sensor);
        headline.setText(R.string.headline_lin_accel);

        headline = (TextView) layout_magnetic_field.findViewById(R.id.fragmet_sensor_data_type_sensor);
        headline.setText(R.string.headline_magnetic);

        headline = (TextView) layout_rotation_vector.findViewById(R.id.fragmet_sensor_data_type_sensor);
        headline.setText(R.string.headline_rot_vector);

    }
    @Override
    protected void onStart() {
        super.onStart();
        mSensorListener.onStart(mSensorManager);
        if(logging)
            Log.e(TAG, "onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorListener.onDestroy(mSensorManager);
        if(logging)
            Log.e(TAG, "onDestroy");
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

    /**
     * Used for setting custom string format on the screen for each sensor
     * @param sensorValue the value from the SensorData
     * @return equal format for each sensor
     */
    private String getFormattedValue(float sensorValue){
        return String.format("%.3f",sensorValue);
    }

    @Override
    public void onAccelerometerChanged(SensorData data) {
        setSensorDataToLayout(data,layout_accelerometer,mAccelerometerValueHolder);

    }

    /**
     * Because the data is in rad/s I will transform it to degree
     * in seconds for easier understanding of the output data
     * @param data the data provided from the sensors listener in rad/s
     */
    @Override
    public void onGyroChanged(SensorData data) {
        setSensorDataToLayout(data,layout_gyro,mGyroValueHolder);
    }

    @Override
    public void onLinearAccelerometerChanged(SensorData data) {
        setSensorDataToLayout(data,layout_linear_accelerometer,mLinearAccelerometerValueHolder);
    }

    @Override
    public void onMagneticFieldChanged(SensorData data) {
        setSensorDataToLayout(data,layout_magnetic_field,mMagneticFieldValueHolder);
    }

    @Override
    public void onRotationVectorChanged(SensorData data) {
        setSensorDataToLayout(data,layout_rotation_vector,mRotationVectorValueHolder);
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
     * Instead of doing it for each interface.
     * @param data the data provided from the callback interface
     * @param layout the according layout where the data should be provided
     * @param textView the textView holder of the element
     */
    private void setSensorDataToLayout(SensorData data, LinearLayout layout, TextView textView){
        textView = (TextView) layout.findViewById(R.id.value_x);
        textView.setText(getFormattedValue(data.getX()));
        textView = (TextView) layout.findViewById(R.id.value_y);
        textView.setText(getFormattedValue(data.getY()));
        textView = (TextView) layout.findViewById(R.id.value_z);
        textView.setText(getFormattedValue(data.getZ()));
    }
}
