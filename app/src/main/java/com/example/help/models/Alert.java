package com.example.help.models;

import com.google.android.gms.maps.model.LatLng;


public class Alert {
    private String name;
    private LatLng location;

    public Alert(String name, String latitude, String longitude) {

        this.name = name;
        this.location = new LatLng(Float.parseFloat(latitude), Float.parseFloat(longitude));
    }
    public Alert( String name, LatLng location) {
        this.name = name;
        this.location = location;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

}
