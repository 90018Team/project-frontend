package com.example.help.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsManager;
import android.util.Log;

import com.example.help.util.FirestoreUserHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;


public class Alert {
    private String name;
    private Location location;
    private final FirestoreUserHelper userHelper = new FirestoreUserHelper();

    private static final String TAG = "Alert";

    public Alert(String name, Float latitude, Float longitude) {
        this.name = name;
        this.location = new Location("latitude,longitude");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
    }

    public Alert(String name, String latitude, String longitude) {
        this.name = name;
        this.location = new Location("latitude,longitude");
        location.setLatitude(Float.parseFloat(latitude));
        location.setLongitude(Float.parseFloat(longitude));
    }


    public Alert(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public Alert() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LatLng getLatLng() {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public String getGoogleMapsLink() {
        if (location != null) {
            String link = "http://maps.google.com/?q=";
            link += Double.toString(location.getLatitude()) + ",";
            link += Double.toString(location.getLongitude());
            return link;
        }
        return null;
    }

    public String getTextMessage() {
        // Note: SMS char limit is 160
        String msg = "";
        msg += "This message was sent from the HELP! app. \n\n";
        msg += "Assistance is required at the below location. \n";
        msg += getGoogleMapsLink();
        return msg;
    }

}
