package de.tum.androidcontroller.models;

import de.tum.androidcontroller.data.SettingsService;

/**
 * This class represent a data model for the settings activity.
 * More details on each variable and their default values
 * please see the class {@link de.tum.androidcontroller.data.SettingsService}.
 * @see de.tum.androidcontroller.data.SettingsService
 */

public class SettingsModel {

    private SettingsService.ConnectionType connectionType;


    public SettingsModel(){

    }

    public SettingsModel(SettingsService.ConnectionType connectionType){
        this.connectionType = connectionType;
    }


    public SettingsService.ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(SettingsService.ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

}
