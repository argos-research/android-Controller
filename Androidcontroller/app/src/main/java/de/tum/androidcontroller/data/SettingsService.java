package de.tum.androidcontroller.data;

import android.content.Context;
import android.content.SharedPreferences;

import de.tum.androidcontroller.models.SettingsModel;

/**
 * This class is holding the values from the settings activity or their defaults
 * if nothing has been changed. TODO ADD IP,PORT etc
 */

public class SettingsService {

    /**
     * The current possibilities for communication. TODO Add hotspot/AP
     */
    public enum ConnectionType {
        UDP("UDP"),
        TCP("TCP"),
        Bluetooth("Bluetooth");

        private String type;

        ConnectionType(String type){
            this.type = type;
        }

        public static ConnectionType fromText(String someType){
            switch (someType){
                case "UDP":
                    return UDP;
                case "TCP":
                    return TCP;
                case "Bluetooth":
                    return Bluetooth;
                default:
                    throw new IllegalArgumentException("String '"+ someType+"' not recognized in the enum");
            }
        }

        public static ConnectionType fromPosition(int spinnerPosition){
            switch (spinnerPosition){
                case 0:
                    return UDP;
                case 1:
                    return TCP;
                case 2:
                    return Bluetooth;
                default:
                    throw new IndexOutOfBoundsException("Position "+spinnerPosition+" is out of bounds! The values should be in [0,2]");
            }
        }

        public String toString(){
            return type;
        }

        public static String[] toArray(){
            return new String[] {   "UDP",
                    "TCP",
                    "Bluetooth"};
        }
    }


    /**
     * Name of SharedPreferences file.
     */
    private static final String SHARED_PREFERENCES_NAME             = "AndroidControllerSharedPreferences";

    /**
     * Key for the connection type.
     */
    private static final String KEY_CONNECTION_TYPE                 = "KeyConnectionType";

    /**
     * Key for the server's IP.
     */
    private static final String KEY_SERVER_IP                       = "KeyServerIP";

    /**
     * Key for the server's port.
     */
    private static final String KEY_SERVER_PORT                     = "KeyServerPort";

    /**
     * Key for the server's port.
     */
    private static final String KEY_SOCKET_TIMEOUT                  = "KeySocketTimeout";


    /**
     * Default value for the connection type.
     */
    private static final ConnectionType DEFAULT_CONNECTION_TYPE     = ConnectionType.TCP;

    /**
     * Default value for the server's IP
     */
    private static final String DEFAULT_SERVER_IP                   = "192.168.0.0";

    /**
     * Default value for the server's port
     */
    private static final int DEFAULT_SERVER_PORT                    = 8000;

    /**
     * Default value for the TCP socket timeout in milis
     */
    private static final int DEFAULT_SOCKET_TIMEOUT                 = 5000;


    public String getConnectionType(){
        return getPreferences().getString(KEY_CONNECTION_TYPE,DEFAULT_CONNECTION_TYPE.toString());
    }

    public String getServerIP(){
        return getPreferences().getString(KEY_SERVER_IP,DEFAULT_SERVER_IP);
    }

    public int getServerPort(){
        return getPreferences().getInt(KEY_SERVER_PORT,DEFAULT_SERVER_PORT);
    }

    public int getSocketTimeout(){
        return getPreferences().getInt(KEY_SOCKET_TIMEOUT,DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Save the settings from the provided <b>settingsData</b> to the shared preferences of this app.
     * @param settingsData the new settingsData model
     */
    public void saveSettings(SettingsModel settingsData) {
        SharedPreferences.Editor editor = getEditor();

        editor.putString(KEY_CONNECTION_TYPE, settingsData.getConnectionType().toString());
        editor.putString(KEY_SERVER_IP, settingsData.getIP());

        editor.putInt(KEY_SERVER_PORT, settingsData.getPort());
        editor.putInt(KEY_SOCKET_TIMEOUT, settingsData.getSocketTimeoutMilis());

        editor.apply();
    }

    /**
     * Reset the settings to the default values from above.
     */
    public void resetSettings() {
        SharedPreferences.Editor editor = getEditor();

        editor.putString(KEY_CONNECTION_TYPE, DEFAULT_CONNECTION_TYPE.toString());
        editor.putString(KEY_SERVER_IP, DEFAULT_SERVER_IP);

        editor.putInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT);
        editor.putInt(KEY_SOCKET_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);

        editor.apply();
    }



    /**
     * Context.
     */
    private Context appContext;

    /**
     * The singleton instance of this class;
     */
    private static SettingsService ourInstance;

    /**
     * Private constructor to prevent unwanted instantiation.
     *
     * @param context appContext
     */
    private SettingsService(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * Returns singleton instance of {@code {@link SettingsService}}.
     * It is created if it does not exist yet.
     *
     * @param context appContext
     * @return singleton instance of {@code DataService}
     */
    public static synchronized SettingsService getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new SettingsService(context);
        }
        return ourInstance;
    }

    private SharedPreferences getPreferences() {
        return appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     *
     * @return {@link android.content.SharedPreferences.Editor}.
     */
    private SharedPreferences.Editor getEditor() {
        return getPreferences().edit();
    }


}
