package com.example.help.models;

import com.google.android.gms.maps.model.LatLng;

public class Alert {
    private int id;
    private String name;
    private LatLng location;

    public Alert(int id, String name, String latitude, String longitude) {
        this.id = id;
        this.name = name;
        this.location = new LatLng(Float.parseFloat(latitude), Float.parseFloat(longitude));
    }
    public Alert(int id, String name, LatLng location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
