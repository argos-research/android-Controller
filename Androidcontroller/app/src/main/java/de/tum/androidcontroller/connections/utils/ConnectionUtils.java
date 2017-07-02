package de.tum.androidcontroller.connections.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.tum.androidcontroller.connections.models.EncodedSentModel;
import de.tum.androidcontroller.models.SensorBaseModel;
import de.tum.androidcontroller.sensors.SensorDataSettings;

/**
 * This classed is used for mapping and handling the sensors data
 * in order to send correctly mapped values to the server.
 */

public class ConnectionUtils {

    /**
     * In my model I have 4 axis of movement - forward, backward, left, right.
     * The idea is to have 10 'steps' for each of these axis in order to
     * map if the accelerometer is only 60% rotated in some direction or
     * if its only 10% (in this case the 'step' is 10%). To do so, I will
     * have 10 possible values for each of these 4 axis. This means, that
     * I will have 10^4 possibilities => I will need log2(10^4) bit to
     * send properly my accelerometer data to the server. log2(10^4) is
     * ~ 13.289 => I can map my accelerometer values to 14 bit. The first
     * possibility for this would be to use the java primitive short type
     * since it has 16 bits of length. => for each axis I will have available
     * 4 bit to map some vales. These 4 bits decrease my 'step' from 10% to
     * 100/(2^4) = 6.25 % which make it more accurate because instead of
     * 10 'steps' now I will have 16.
     *
     * Although I need only 4 bits for storing my accelerometer values as it is
     * described above, in a {@link org.json.JSONObject} you can put either
     * {@link Integer}, {@link Long}, {@link Float} or {@link Double} that is
     * why I won't stick to this concept of only 4 bits for an axis
     * because this won't slow down my performance and it will make only my code
     * harder to read.
     *
     * The value range of every value from {@link EncodedSentModel}
     * will be from [0,MAXIMUM_ACCELEROMETER_STEPS]. The steps can be
     * found in {@link de.tum.androidcontroller.sensors.SensorDataSettings}
     * as <b>MAXIMUM_ACCELEROMETER_STEPS</b> parameter.
     *
     * For more details on each accelerometer value please see
     * {@link de.tum.androidcontroller.sensors.EventListener} interface and the
     * comments there.
     *
     *
     *
     * @param data the raw accelerometer values
     * @return an instance of {@link EncodedSentModel} ready to be send to the server
     *
     * @see EncodedSentModel ,de.tum.androidcontroller.sensors.EventListener,de.tum.androidcontroller.sensors.SensorDataSettings
     */
    public static EncodedSentModel toEncodedAccelerometerModel(SensorBaseModel data){
        Log.e("toEncodedAcceler", String.format("X: %.2f, Y: %.2f",data.getX(),data.getY()));
        int forward,backward,left,right;
        int preDefined = 3;
        //TODO make it better and connect it with MAXIMUM_ACCELEROMETER_STEPS!
        //set forward/ backward
        //acceleration
        if(data.getX() < SensorDataSettings.idleAccelerationBreakState){
            //forward     = Math.abs((int) (data.getX()*2/SensorDataSettings.MAXIMUM_ACCELEROMETER_STEPS));
            //forward     = Math.abs((int) (data.getX()*2));
            forward     = Math.abs((int) (data.getX()));
            //forward     = preDefined;

            backward    = 0;
        }else{  //breaking
            forward     = 0;

            //backward    = Math.abs((int) (data.getX()*2/SensorDataSettings.MAXIMUM_ACCELEROMETER_STEPS));
            backward    = Math.abs((int) (data.getX()*2));
            //backward    = preDefined;
        }

        //set right/left
        //left
        if(data.getY() < SensorDataSettings.idleLeftRightState){
            right       = 0;
            //left        = Math.abs((int) (data.getY()*2/SensorDataSettings.MAXIMUM_ACCELEROMETER_STEPS));
            left        = Math.abs((int) (data.getY()*2));
            //left        = preDefined;
        }else{ //right
            //right       = Math.abs((int) (data.getY()*2/SensorDataSettings.MAXIMUM_ACCELEROMETER_STEPS));
            right       = Math.abs((int) (data.getY()*2));
            //right       = preDefined;

            left        = 0;
        }
        //Log.e("ABS", String.format("forward %d backward %d left %d right %d \t\t data %s",forward,backward,left,right,data.toString()));
        return new EncodedSentModel(forward,backward,left,right);

    }

    public static JSONObject buildGyroJSON(JSONObject gyroJSON) throws JSONException{
        return new JSONObject()
                        .put("Gyro data"                , gyroJSON)
                        .put("Created time"             , System.currentTimeMillis());
    }

    /**
     * This method provides the exact model of that how
     * the sensor data should be send to the server
     * in order the server to recognize it
     * @param accJSON the {@link EncodedSentModel} representation from <b>ConnectionUtils.toEncodedAccelerometerModel</b>
     * @return the JSON that should be send to the server
     *
     * @throws JSONException
     */
    public static JSONObject buildAccelerometerJSON(JSONObject accJSON) throws JSONException{
        return new JSONObject()
                        .put("Accelerometer data"       , accJSON)
                        .put("Created time"             , System.currentTimeMillis());
    }


    /**
     * Builds the JSON that is sent to the server holding the pressed button event.
     * This method has nothing to do with the button presses from the gyro sensor.
     * These events are made with the onscreen buttons in the main play activity.
     *
     * @param key
     * @return
     * @throws JSONException
     */
    public static JSONObject buildKeyPressJSON(int key) throws JSONException {
        return  new JSONObject()
                        .put("Key"                      , key)
                        .put("Created time"             , System.currentTimeMillis());
    }
}
