package de.tum.androidcontroller.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        spinnerConnectionType = (Spinner) findViewById(R.id.connection_type);
        ArrayAdapter<String> adapterConnectionTypes = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,optionsConnectionTypes);
        spinnerConnectionType.setAdapter(adapterConnectionTypes);
        adapterConnectionTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        restoreUI();

    }

    /**
     * This method restores the settings from the shared preferences.
     */
    private void restoreUI() {
        SettingsService data = SettingsService.getInstance(getApplicationContext());

        spinnerConnectionType.setSelection(getPositionFromArray(optionsConnectionTypes, data.getConenctionType()));
    }


    private SettingsModel getSettingsFromUI() {
        SettingsModel settingsData = new SettingsModel();

        settingsData.setConnectionType(SettingsService.ConnectionType.fromPosition(spinnerConnectionType.getSelectedItemPosition()));

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
     * Just close the view with no effect on the app
     * @param view the button view
     */
    public void onCancelButtonClicked(View view) {
        finish();
    }

    /**
     * Save the the UI as settings to the shared preferences
     * @param view the button view
     */
    public void onSaveButtonClicked(View view) {
        SettingsService.getInstance(getApplicationContext()).saveSettings(getSettingsFromUI());
        finish();
    }

    public void onResetButtonClicked(View view) {
        SettingsService.getInstance(getApplicationContext()).resetSettings();
        restoreUI();
    }
}
