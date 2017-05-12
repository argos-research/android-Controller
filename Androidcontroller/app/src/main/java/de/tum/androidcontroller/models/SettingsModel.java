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

    private String IP;

    private int port;

    private int socketTimeoutMilis;

    public SettingsModel(){

    }

    public SettingsModel(SettingsService.ConnectionType connectionType,
                         String IP,
                         int port,
                         int socketTimeoutMilis){
        this.connectionType     = connectionType;
        this.IP                 = IP;
        this.port               = port;
        this.socketTimeoutMilis = socketTimeoutMilis;
    }


    public SettingsService.ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(SettingsService.ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getSocketTimeoutMilis(){
        return socketTimeoutMilis;
    }

    public void setSocketTimeoutMilis(int socketTimeoutMilis){
        this.socketTimeoutMilis = socketTimeoutMilis;
    }

}
