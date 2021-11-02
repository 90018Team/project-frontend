package com.example.help.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
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
            // TODO
        }
    }

    public String getAddress(Location location) {
        try {
            Geocoder geocoder = new Geocoder(activity,
                    Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1
            );
            return addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface LocationCallback {
        void onCallback(Location location);
    }

}
