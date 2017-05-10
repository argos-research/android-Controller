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
     * Default value for the connection type.
     */
    private static final  ConnectionType DEFAULT_CONNECTION_TYPE    = ConnectionType.TCP;


    public String getConenctionType(){
        return getPreferences().getString(KEY_CONNECTION_TYPE,DEFAULT_CONNECTION_TYPE.toString());
    }

    public void saveSettings(SettingsModel settingsData) {
        SharedPreferences.Editor editor = getEditor();

        editor.putString(KEY_CONNECTION_TYPE, settingsData.getConnectionType().toString());

        editor.apply();
    }

    public void resetSettings() {
        SharedPreferences.Editor editor = getEditor();

        editor.putString(KEY_CONNECTION_TYPE, DEFAULT_CONNECTION_TYPE.toString());

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

    /**
     * Save value to sharedPreferences
     */
    private void saveValue(String key, boolean value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Save value to sharedPreferences
     */
    private void saveValue(String key, String value){
        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Save value to sharedPreferences
     */
    private void saveValue(String key, int value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Save value to sharedPreferences
     */
    private void saveValue(String key, float value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putFloat(key, value);
        editor.apply();
    }


}
