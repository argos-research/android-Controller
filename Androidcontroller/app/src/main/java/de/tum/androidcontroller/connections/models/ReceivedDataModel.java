package de.tum.androidcontroller.connections.models;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Store the data from the server in a proper way.
 */

public class ReceivedDataModel implements Parcelable {

//    public static final String RECEIVED_DATA_MODEL_KEY = "ReceivedDataModelKey";

    /*an example "speed":"-0.534997" which points to the current speed of the car in m/s => it should be converted to km/h *multiplied by (18/5). */
    private double speed;
    /*an example "gear":"-1" which points to the current gear of the car. -1 is reverse, 0 is neutral, 1 first , 2 second etc... */
    private int gear;
    /*an example "gearNb":"7" which points to the total number of gears of the car. It counts also the reverse and the neutral one! In this case 7 means: R,N,1,2,3,4,5 */
    private int gearTotal;
    /*an example "pos":"6" which points to the current position of the car. */
    private int currentPosition;
    /*an example "ncars":"6" which points to the total cars in the race. */
    private int numberOfCars;

    private double kmInHourMultiplier = 18.0f/5.0f;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.speed);
        dest.writeInt(this.gear);
        dest.writeInt(this.gearTotal);
        dest.writeInt(this.currentPosition);
        dest.writeInt(this.numberOfCars);
    }

    public ReceivedDataModel(String JSONString) throws JSONException {
        JSONObject toJSON = new JSONObject(JSONString);
        this.extractData(toJSON);
    }

    public double getSpeedInKmPerHour() {
        return speed * this.kmInHourMultiplier;
    }

    public double getAbsoluteSpeedInKmPerHour(){
        return Math.abs(this.getSpeedInKmPerHour());
    }

    public int getGear() {
        return gear;
    }

    public String getGearString(){
        return this.gear == -1 ? "R" : this.gear == 0 ? "N" : ""+this.gear;
    }

    public int getGearTotal() {
        return gearTotal;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getNumberOfCars() {
        return numberOfCars;
    }

    /**
     * Extracts the provided data from the SpeedDreams2 HTTP API.
     * An example of such sent JSON is:
     * {"speed":"-0.534997","name":"Player","rpm":"104.719757","gear":"-1","gearNb":"7","pos":"6","ncars":"6"}
     * @param JSON the received JSON from the SD2 HTTP API.
     */
    private void extractData(JSONObject JSON) throws JSONException {
        this.speed           = JSON.getDouble("speed");
        this.gear            = JSON.getInt("gear");
        this.gearTotal       = JSON.getInt("gearNb");
        this.currentPosition = JSON.getInt("pos");
        this.numberOfCars    = JSON.getInt("ncars");
    }

    protected ReceivedDataModel(Parcel in) {
        this.speed = in.readDouble();
        this.gear = in.readInt();
        this.gearTotal = in.readInt();
        this.currentPosition = in.readInt();
        this.numberOfCars = in.readInt();
    }

    public static final Parcelable.Creator<ReceivedDataModel> CREATOR = new Parcelable.Creator<ReceivedDataModel>() {
        @Override
        public ReceivedDataModel createFromParcel(Parcel source) {
            return new ReceivedDataModel(source);
        }

        @Override
        public ReceivedDataModel[] newArray(int size) {
            return new ReceivedDataModel[size];
        }
    };


    @Override
    public String toString(){
        return String.format(Locale.US,"Speed: %.2f, gear: %d/%d, position: %d/%d.",this.getSpeedInKmPerHour(),this.gear,this.gearTotal,this.currentPosition,this.numberOfCars);
    }
}
