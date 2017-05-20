package de.tum.androidcontroller.connections.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.tum.androidcontroller.connections.models.EncodedSensorModel;
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
     * The value range of every value from {@link EncodedSensorModel}
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
     * @return an instance of {@link EncodedSensorModel} ready to be send to the server
     *
     * @see EncodedSensorModel ,de.tum.androidcontroller.sensors.EventListener,de.tum.androidcontroller.sensors.SensorDataSettings
     */
    private static EncodedSensorModel toEncodedAccelerometerModel(SensorBaseModel data){
        int forward,backward,left,right;
        //set forward/ backward
        //acceleration
        if(data.getX() < SensorDataSettings.idleAccelerationBreakState){
            forward     = Math.abs((int) (data.getX()/SensorDataSettings.MAXIMUM_ACCELEROMETER_STEPS));
            backward    = 0;
        }else{  //breaking
            forward     = 0;
            backward    = Math.abs((int) (data.getX()/SensorDataSettings.MAXIMUM_ACCELEROMETER_STEPS));
        }

        //set right/left
        //left
        if(data.getY() < SensorDataSettings.idleLeftRightState){
            right       = 0;
            left        = Math.abs((int) (data.getY()/SensorDataSettings.MAXIMUM_ACCELEROMETER_STEPS));
        }else{ //right
            right       = Math.abs((int) (data.getY()/SensorDataSettings.MAXIMUM_ACCELEROMETER_STEPS));
            left        = 0;
        }

        return new EncodedSensorModel(forward,backward,left,right);

    }

    private static int i= 0;
    private static long TEST_CALLS_COUNT = 100000; //TODO remove it

    public static JSONObject buildGyroJSON(JSONObject gyroJSON) throws JSONException{
        return new JSONObject()
                        .put("Gyro data"    , gyroJSON)
                        .put("Created time" , System.currentTimeMillis());
    }

    /**
     * This method provides the exact model of that how
     * the sensor data should be send to the server
     * in order the server to recognize it
     * @param sensorBaseModel the raw accelerometer values
     * @return the JSON that should be send to the server
     *
     * @throws JSONException
     */
    public static JSONObject buildAccelerometerJSON(SensorBaseModel sensorBaseModel) throws JSONException{
        JSONObject ob = new JSONObject();

        JSONObject accValues = toEncodedAccelerometerModel(sensorBaseModel).toJSONObject();


        ob.put("Loop"               ,i);
        ob.put("Loops count"        ,TEST_CALLS_COUNT);
        ob.put("Accelerometer data" ,accValues);
        ob.put("Gyro data"          ,accValues);
        ob.put("Created time"       ,System.currentTimeMillis());

        return ob;
    }
}
