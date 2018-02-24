package com.exampledemo.shalvi.gpsdemocode.model;

/**
 * Created by Windows on 2/24/2018.
 */

public class TrckerData  {

    public double Latiutude;
    public double Longitude;
    public  int Speed;

    public double getLatiutude() {
        return Latiutude;
    }

    public void setLatiutude(double latiutude) {
        Latiutude = latiutude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public int getSpeed() {
        return Speed;
    }

    public void setSpeed(int speed) {
        Speed = speed;
    }

    public String getVechile_id() {
        return Vechile_id;
    }

    public void setVechile_id(String vechile_id) {
        Vechile_id = vechile_id;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String Vechile_id;

    public String Time;

    public TrckerData( double Lat, double longitude, String time,int speed) {

        this.Latiutude = Lat;
        this.Longitude = longitude;
        this.Time = time;
        this.Speed=speed;
    }


}
