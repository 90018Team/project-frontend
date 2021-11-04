package com.example.help.util;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GPSHelper {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Activity activity;
    String TAG = "GPSHelper";



    public GPSHelper(Activity activity) {
        Log.d(TAG, "GPSHelper: started");
        this.activity = activity;
        fusedLocationProviderClient = getFusedLocationProviderClient(activity);

    }

    public void getLocation(LocationCallback callback) {
        Log.d(TAG, "getLocation: getting");
        try {

            fusedLocationProviderClient
                    .getLastLocation()
                    .addOnCompleteListener(activity, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            callback.onCallback(location);
                        }
                    });
        } catch (SecurityException e) {
            Log.d(TAG, "getLocation: location permission not granted");
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
        }
    }


    public String getAddress(Location location) {

        if (location != null) {
            try {
                Geocoder geocoder = new Geocoder(activity,
                        Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1
                );
                return addresses.get(0).getAddressLine(0);
            } catch (IOException e) {
                e.printStackTrace();
                return "Sorry, Location not found. Please try again.";
            }
        }
        return "Sorry, Location not found. Please try again.";
    }

    public interface LocationCallback {
        void onCallback(Location location);
    }

}
