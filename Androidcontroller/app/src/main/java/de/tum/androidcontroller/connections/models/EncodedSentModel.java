package de.tum.androidcontroller.connections.models;

import org.json.JSONException;
import org.json.JSONObject;
import de.tum.androidcontroller.UInput.uInputValuesHolder;

/**
 * This model class is used for encoding the accelerometer/gyro values in
 * proper format in order the server to be able to distinguish the
 * differences in all 4 axes - forward, backward, left and right.
 *
 * The value range of every value from {@link EncodedSentModel}
 * will be from [0,MAXIMUM_ACCELEROMETER_STEPS] in case that this
 * class is used for mapping the accelerometer values, otherwise
 * each value will represent a key code from
 * {@link de.tum.androidcontroller.UInput.uInputValuesHolder}.
 *
 * The steps for the accelerometer values can be found in
 * {@link de.tum.androidcontroller.sensors.SensorDataSettings}
 * as <b>MAXIMUM_ACCELEROMETER_STEPS</b> parameter.
 *
 * A good example of different buttons can be seen here
 * https://s32.postimg.org/zcs0wosth/xbox.jpg  or
 * http://www.gamecontrols.net/wp-content/uploads/2015/02/Uncharted-3-PS3-Game-Contro1.jpg .
 */

public class EncodedSentModel {
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

    public static final int BTN_LEFT_CODE       = uInputValuesHolder.BTN_TL;
    public static final int BTN_RIGHT_CODE      = uInputValuesHolder.BTN_TR;


    /*
    TODO READ THIS!
    Currently there is a bug in the SpeedDream 2 HTTP API which provides the data from the game. The bug is
    that when you choose to play a game and the loading screen is ready, you need to press Enter to start
    racing. Unfortunately, in this time the HTTP socket will be initialized but if you try to send a HTTP GET,
    this will force the game to crash and it can even harm you PC. That is why you will need to press the
    'start game' button in the main activity, which will send a simple "enter" key press which will trigger
    the game to start and will also start the sending thread on the server side which will start sending
    the provided JSON from the SP2 HTTP API.
     */
    public static final int BTN_START_GAME_CODE = uInputValuesHolder.KEY_ENTER;

//    public static final int FORWARD_KEY_CODE    = uInputValuesHolder.BTN_TR,
//                            BACKWARD_KEY_CODE   = uInputValuesHolder.BTN_TL,
//                            LEFT_KEY_CODE       = uInputValuesHolder.BTN_WEST,
//                            RIGHT_KEY_CODE      = uInputValuesHolder.BTN_EAST;


    private int forward, backward, left, right;
    private final String    KEY_FORWARD     = "forward",
                            KEY_BACKWARD    = "backward",
                            KEY_LEFT        = "left",
                            KEY_RIGHT       = "right";


    public EncodedSentModel(int forward, int backward, int left, int right){
        this.forward    = forward;
        this.backward   = backward;
        this.left       = left;
        this.right      = right;
    }

    /**
     * Method for saving the current instance of {@link EncodedSentModel}
     * as a {@link JSONObject}.
     * @return the mapped {@link EncodedSentModel} values as a {@link JSONObject}.
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
