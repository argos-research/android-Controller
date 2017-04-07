package de.tum.androidcontroller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import de.tum.androidcontroller.data.SensorData;
import de.tum.androidcontroller.sensors.EventListener;
import de.tum.androidcontroller.sensors.SensorListener;
import de.tum.androidcontroller.sensors.SensorModel;

public class MainActivity extends AppCompatActivity implements EventListener{

    private static final String TAG = "android_controller_tag";
    private static final boolean logging = true;

    private SensorManager mSensorManager;
    private SensorListener mSensorListener;
    //private SensorModel mSensorModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    @Override
    public void onAccelerometerChanged(SensorData data) {
        setTextValue(R.id.accelerometer_value_x,data.getX());
        setTextValue(R.id.accelerometer_value_y,data.getY());
        setTextValue(R.id.accelerometer_value_z,data.getZ());
    }

    @Override
    public void onGyroChanged(SensorData data) {
        setTextValue(R.id.gyro_value_x,data.getX());
        setTextValue(R.id.gyro_value_y,data.getY());
        setTextValue(R.id.gyro_value_z,data.getZ());
    }

    @SuppressLint("SetTextI18n")
    private void setTextValue(int id, float value){
        TextView tv = (TextView) findViewById(id);
        tv.setText(Float.toString(value));
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
