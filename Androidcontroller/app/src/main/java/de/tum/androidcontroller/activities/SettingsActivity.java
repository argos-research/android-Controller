package de.tum.androidcontroller.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import de.tum.androidcontroller.R;
import de.tum.androidcontroller.data.SettingsService;
import de.tum.androidcontroller.models.SettingsModel;

/**
 * Created by chochko on 09/05/17.
 */

public class SettingsActivity extends AppCompatActivity {

    /**
     * According to {@link de.tum.androidcontroller.models.SettingsModel} model,
     * this array represents the enum values from
     * {@link de.tum.androidcontroller.data.SettingsService.ConnectionType}.
     */
    private String[] optionsConnectionTypes          = SettingsService.ConnectionType.toArray();

    /**
     * According to {@link de.tum.androidcontroller.models.SettingsModel} model,
     * this Spinner holds all of the possible values for the enum
     * {@link de.tum.androidcontroller.data.SettingsService.ConnectionType}.
     */
    private Spinner spinnerConnectionType;

    /**
     * According to {@link de.tum.androidcontroller.models.SettingsModel} model,
     * this EditText holds the IP of the server.
     */
    private EditText editTextServerIP;

    /**
     * According to {@link de.tum.androidcontroller.models.SettingsModel} model,
     * this EditText holds the port of the server.
     */
    private EditText editTextServerPort;

    /**
     * According to {@link de.tum.androidcontroller.models.SettingsModel} model,
     * this EditText holds the socket timeout that each socket should have.
     * Unfortunately, this value is not used but you can use it for something.
     */
    private EditText editTextSocketTimeout;

    /**
     * According to {@link de.tum.androidcontroller.models.SettingsModel} model,
     * this EditText holds MAC of the Bluetooth server.
     */
    private EditText editTextBluetoothMAC;


    //the layout holding the WiFi attributes (IP address, port number and socket timeout)
    private LinearLayout llWifi;

    //the layout holding the Bluetooth attributes (MAC address)
    private LinearLayout llBluetooth;

    //for obtaining the screen dimensions
    DisplayMetrics displayMetrics = new DisplayMetrics();

    //the duration of hiding/showing the attributes in this class
    private final long animationDuration = 650;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        editTextServerIP        = (EditText) findViewById(R.id.ip_edit_text);

        editTextServerPort      = (EditText) findViewById(R.id.port_edit_text);

        editTextSocketTimeout   = (EditText) findViewById(R.id.timeout_edit_text);

        editTextBluetoothMAC    = (EditText) findViewById(R.id.text_mac_address);

        llWifi                  = (LinearLayout) findViewById(R.id.ll_wifi);

        llBluetooth             = (LinearLayout) findViewById(R.id.ll_bluetooth);

        initSpinner();

        restoreUI();

    }

    /**
     * Initialization of the spinner holding the values from
     * {@link de.tum.androidcontroller.data.SettingsService.ConnectionType}
     * and the animation applied on changing the layouts
     */
    private void initSpinner(){
        spinnerConnectionType = (Spinner) findViewById(R.id.connection_type);
        ArrayAdapter<String> adapterConnectionTypes = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,optionsConnectionTypes);
        spinnerConnectionType.setAdapter(adapterConnectionTypes);
        adapterConnectionTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerConnectionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(optionsConnectionTypes[position].equals("Bluetooth")){
                    llWifi  .animate()
                            .translationX(displayMetrics.widthPixels)
                            .setDuration(animationDuration)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    super.onAnimationStart(animation);
                                    llBluetooth.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    llWifi.setVisibility(View.GONE);
                                }
                            });
                }else
                    llWifi  .animate()
                            .translationX(0)
                            .setDuration(animationDuration)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    super.onAnimationStart(animation);
                                    llWifi.setVisibility(View.VISIBLE);
                                    llBluetooth.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                }
                            });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * This method restores the settings from the shared preferences.
     */
    private void restoreUI() {
        SettingsService data = SettingsService.getInstance(getApplicationContext());

        spinnerConnectionType.setSelection(getPositionFromArray(optionsConnectionTypes, data.getConnectionType()));

        editTextServerIP.setText(String.valueOf(data.getServerIP()));

        editTextServerPort.setText(String.valueOf(data.getServerPort()));

        editTextSocketTimeout.setText(String.valueOf(data.getSocketTimeout()));

        editTextBluetoothMAC.setText(String.valueOf(data.getBluetoothMAC()));
    }


    /**
     * Obtains the EditText values of each member of the
     * {@link SettingsModel} class.
     * @return an instance of {@link SettingsModel} from the current UI.
     */
    private SettingsModel getSettingsFromUI() {
        SettingsModel settingsData = new SettingsModel();

        settingsData.setConnectionType(SettingsService.ConnectionType.fromPosition(spinnerConnectionType.getSelectedItemPosition()));

        settingsData.setIP(editTextServerIP.getText().toString());

        settingsData.setPort(Integer.valueOf(editTextServerPort.getText().toString()));

        settingsData.setSocketTimeoutMilis(Integer.valueOf(editTextSocketTimeout.getText().toString()));

        settingsData.setBluetoothMAC(editTextBluetoothMAC.getText().toString());

        return settingsData;
    }

    /**
     * Finds the position of a String in some array of Strings.
     * @param array some array
     * @param value the String value that is being searched for
     * @return the position of the value in the array or -1 otherwise
     */
    public int getPositionFromArray(String[] array, String value){
        for(int i = 0 ; i<array.length ; i++){
            if(array[i].equals(value))
                return i;
        }
        return -1;
    }




    /**
     * Save the the UI as settings to the shared preferences
     * @param view the button view
     */
    public void onSaveButtonClicked(View view) {
        SettingsService.getInstance(getApplicationContext()).saveSettings(getSettingsFromUI());
        setResult(Activity.RESULT_OK);
        finish();
    }

    public void onResetButtonClicked(View view) {
        SettingsService.getInstance(getApplicationContext()).resetSettings();
        restoreUI();
    }
}
