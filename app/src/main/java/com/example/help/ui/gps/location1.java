package com.example.help.ui.gps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.help.ui.chatRoom.ChatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class location1 extends AppCompatActivity {
    private Button btn;
    private TextView textView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private static final int REQUEST_CHECK_SETTINGS = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.loca);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //btn = findViewById(R.id.location_btn);
        //textView = findViewById(R.id.location_text);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(30000);

//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(location1.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

//                                            LocationServices.getFusedLocationProviderClient(MainActivity.this)
//                                                    .removeLocationUpdates(this);
////
//                                            if (locationResult != null && locationResult.getLocations().size() >0){
//
//                                                int index = locationResult.getLocations().size() - 1;
//                                                double latitude = locationResult.getLocations().get(index).getLatitude();
//                                                double longitude = locationResult.getLocations().get(index).getLongitude();
//
//                                        //        textView.setText("Latitude: "+ latitude + "\n" + "Longitude: "+ longitude);
//                                            }
                                }
                            }, Looper.getMainLooper());
                    fusedLocationProviderClient.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {

                                    if (location != null) {
                                        Double lat = location.getLatitude();
                                        Double longt = location.getLongitude();
//                                                Intent intent = new Intent();
//                                                intent.putExtra("Longitude", longt);
//                                                intent.putExtra("Latitude", lat);
//                                                setResult(1,intent);
                                        //finish();
                                        //textView.setText(lat + "," + longt);
                                        Toast.makeText(location1.this, "Success! Latitude:"+lat + ", Longitude," + longt, Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(location1.this,ChatActivity.class);
                                        startActivity(intent);
                                    }
                                    if (location == null) {
                                        //  textView.setText("happy");
                                        Toast.makeText(location1.this, "null", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    turnOnGPS();
              }}
//                else{
//                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
//            }
        }
//            }
//
//        });

    }

    private void turnOnGPS() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(location1.this, "GPS is already turned on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException)e;
                                resolvableApiException.startResolutionForResult(location1.this,REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(location1.this, "Permission granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(location1.this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean isGPSEnabled(){
        LocationManager locationManager = null;
        boolean isEnabled = false;
        if(locationManager == null){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }
}