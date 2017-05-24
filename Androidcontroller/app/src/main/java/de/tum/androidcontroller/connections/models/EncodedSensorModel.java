package de.tum.androidcontroller.connections.models;

import org.json.JSONException;
import org.json.JSONObject;
import de.tum.androidcontroller.UInput.uInputValuesHolder;

/**
 * This model class is used for encoding the accelerometer/gyro values in
 * proper format in order the server to be able to distinguish the
 * differences in all 4 axes - forward, backward, left and right.
 *
 * The value range of every value from {@link EncodedSensorModel}
 * will be from [0,MAXIMUM_ACCELEROMETER_STEPS] in case that this
 * class is used for mapping the accelerometer values, otherwise
 * each value will represent a key code from
 * {@link de.tum.androidcontroller.UInput.uInputValuesHolder}.
 *
 * The steps for the accelerometer values can be found in
 * {@link de.tum.androidcontroller.sensors.SensorDataSettings}
 * as <b>MAXIMUM_ACCELEROMETER_STEPS</b> parameter.
 */

public class EncodedSensorModel {
    /**
     * Those values are mapped directly to the gyro fast forward/backward/left or right
     * change. These values are also send to the server and handle as normal keyboard
     * input with the linux UInput class.
     */
//    public static final int FORWARD_KEY_CODE    = uInputValuesHolder.KEY_Y,
//                            BACKWARD_KEY_CODE   = uInputValuesHolder.KEY_E,
//                            LEFT_KEY_CODE       = uInputValuesHolder.KEY_A,
//                            RIGHT_KEY_CODE      = uInputValuesHolder.KEY_H;

    public static final int FORWARD_KEY_CODE    = uInputValuesHolder.BTN_NORTH,
                            BACKWARD_KEY_CODE   = uInputValuesHolder.BTN_SOUTH,
                            LEFT_KEY_CODE       = uInputValuesHolder.BTN_WEST,
                            RIGHT_KEY_CODE      = uInputValuesHolder.BTN_EAST;


    private int forward, backward, left, right;
    private final String    KEY_FORWARD     = "forward",
                            KEY_BACKWARD    = "backward",
                            KEY_LEFT        = "left",
                            KEY_RIGHT       = "right";


    public EncodedSensorModel(int forward, int backward, int left, int right){
        this.forward    = forward;
        this.backward   = backward;
        this.left       = left;
        this.right      = right;
    }

    /**
     * Method for saving the current instance of {@link EncodedSensorModel}
     * as a {@link JSONObject}.
     * @return the mapped {@link EncodedSensorModel} values as a {@link JSONObject}.
     * @throws JSONException if it is unsuccessful
     */
    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject() .put(KEY_FORWARD,forward)
                                .put(KEY_BACKWARD,backward)
                                .put(KEY_LEFT,left)
                                .put(KEY_RIGHT,right);
    }

    public void resetValues(){
        this.forward    = 0;
        this.backward   = 0;
        this.left       = 0;
        this.right      = 0;
    }

    public int getForward() {
        return forward;
    }

    public void setForward(int forward) {
        this.forward = forward;
    }

    public int getBackward() {
        return backward;
    }

    public void setBackward(int backward) {
        this.backward = backward;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }


}
