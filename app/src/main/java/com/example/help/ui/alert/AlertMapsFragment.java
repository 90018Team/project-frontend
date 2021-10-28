package com.example.help.ui.alert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.help.R;
import com.example.help.models.Alert;
import com.example.help.ui.alert.alertRoom.AlertRoomActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AlertMapsFragment extends Fragment {

    private static final String TAG = "gagaga";
    private static String MESSAGES_CHILD = "/emergency_event/";
    public DatabaseReference mFirebaseDatabaseReference;



    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            // get current location
            LatLng curLocation = new LatLng(-33.852, 151.211);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            googleMap.setMyLocationEnabled(true);


            // marker format
            int height = 200;
            int width = 250;
            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);




            // setBounds
            Double padding = 0.001;
            LatLngBounds viewBounds = new LatLngBounds(
                    new LatLng(curLocation.latitude - padding, curLocation.longitude - padding), // SW bounds
                    new LatLng(curLocation.latitude + padding, curLocation.longitude + padding) // NE bounds
                    );

            // get alerts
            Alert[] alerts = new Alert[3];
            alerts[0] = new Alert( "gaga111",  "-33.853", "151.212");
            alerts[1] = new Alert( "gaga222", "-33.853", "151.211");
            alerts[2] = new Alert( "gaga333","-33.852", "151.212");

            // get contact lists
            List<String> phoneNumbers = Arrays.asList("111", "456", "12332", "", "zzz", "David Price" );

            ArrayList <Alert> allAlerts = new ArrayList<>();
            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD);
            messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for(DataSnapshot d : snapshot.getChildren()) {
                        Log.d(TAG, "dddd: " + d.getKey());
                        HashMap message = (HashMap)snapshot.child(d.getKey()).getChildren().iterator().next().getValue();
                        String location = (String) message.get("text");
                        String number = (String) message.get("name");

                        Log.d(TAG, "phone number: " + number);
                        if (phoneNumbers.contains(number)) {
                            Alert a =  new Alert((String) d.getKey(), location.split(" ")[0], location.split(" ")[1]);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(a.getLocation())
                                    .title(a.getName())
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: Something wrong?");
                }

            });

            googleMap.setOnMarkerClickListener(marker -> {
                Toast.makeText(getContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getContext(), AlertRoomActivity.class);
                i.putExtra("name", marker.getTitle());
                startActivity(i);
                return true;
            });

            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(viewBounds, 0));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert_maps, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }


}