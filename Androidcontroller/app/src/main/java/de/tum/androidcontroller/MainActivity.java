package de.tum.androidcontroller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

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
    private String getFormatedValue(float sensorValue){
        return String.format("%.3f",sensorValue);
    }

    @Override
    public void onAccelerometerChanged(SensorData data) {
        mAccelerometerValueHolder = (TextView) layout_accelerometer.findViewById(R.id.value_x);
        mAccelerometerValueHolder.setText(getFormatedValue(data.getX()));
        mAccelerometerValueHolder = (TextView) layout_accelerometer.findViewById(R.id.value_y);
        mAccelerometerValueHolder.setText(getFormatedValue(data.getY()));
        mAccelerometerValueHolder = (TextView) layout_accelerometer.findViewById(R.id.value_z);
        mAccelerometerValueHolder.setText(getFormatedValue(data.getZ()));

    }

    @Override
    public void onGyroChanged(SensorData data) {
        mGyroValueHolder = (TextView) layout_gyro.findViewById(R.id.value_x);
        mGyroValueHolder.setText(getFormatedValue(data.getX()));
        mGyroValueHolder = (TextView) layout_gyro.findViewById(R.id.value_y);
        mGyroValueHolder.setText(getFormatedValue(data.getY()));
        mGyroValueHolder = (TextView) layout_gyro.findViewById(R.id.value_z);
        mGyroValueHolder.setText(getFormatedValue(data.getZ()));
    }

    @Override
    public void onLinearAccelerometerChanged(SensorData data) {
        mLinearAccelerometerValueHolder = (TextView) layout_linear_accelerometer.findViewById(R.id.value_x);
        mLinearAccelerometerValueHolder.setText(getFormatedValue(data.getX()));
        mLinearAccelerometerValueHolder = (TextView) layout_linear_accelerometer.findViewById(R.id.value_y);
        mLinearAccelerometerValueHolder.setText(getFormatedValue(data.getY()));
        mLinearAccelerometerValueHolder = (TextView) layout_linear_accelerometer.findViewById(R.id.value_z);
        mLinearAccelerometerValueHolder.setText(getFormatedValue(data.getZ()));
    }

    @Override
    public void onMagneticFieldChanged(SensorData data) {
        mMagneticFieldValueHolder = (TextView) layout_magnetic_field.findViewById(R.id.value_x);
        mMagneticFieldValueHolder.setText(getFormatedValue(data.getX()));
        mMagneticFieldValueHolder = (TextView) layout_magnetic_field.findViewById(R.id.value_y);
        mMagneticFieldValueHolder.setText(getFormatedValue(data.getY()));
        mMagneticFieldValueHolder = (TextView) layout_magnetic_field.findViewById(R.id.value_z);
        mMagneticFieldValueHolder.setText(getFormatedValue(data.getZ()));
    }

    @Override
    public void onRotationVectorChanged(SensorData data) {
        mRotationVectorValueHolder = (TextView) layout_rotation_vector.findViewById(R.id.value_x);
        mRotationVectorValueHolder.setText(getFormatedValue(data.getX()));
        mRotationVectorValueHolder = (TextView) layout_rotation_vector.findViewById(R.id.value_y);
        mRotationVectorValueHolder.setText(getFormatedValue(data.getY()));
        mRotationVectorValueHolder = (TextView) layout_rotation_vector.findViewById(R.id.value_z);
        mRotationVectorValueHolder.setText(getFormatedValue(data.getZ()));
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
}
