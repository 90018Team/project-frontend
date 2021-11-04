package com.example.help.ui.alert;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.help.R;
import com.example.help.databinding.AlertFragmentBinding;
import com.example.help.models.Alert;
import com.example.help.models.Contact;
import com.example.help.ui.chatRoom.ChatActivity;
import com.example.help.util.FirestoreUserHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AlertFragment extends Fragment {


    private static final String TAG = "gagaga";
    private static String MESSAGES_CHILD = "/emergency_event/";
    public DatabaseReference mFirebaseDatabaseReference;
    Location currentLocation;

    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    LatLng curLocation;
    GoogleMap gMap;
    private FirestoreUserHelper userHelper;
    List<String> phoneNumbers = new ArrayList<>();

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            // get current location
            //LatLng curLocation = new LatLng(-33.852, 151.211);
            gMap = googleMap;
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            googleMap.setMyLocationEnabled(true);


            // marker format
            int height = 200;
            int width = 250;
            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);


            phoneNumbers.add("");
            userHelper = FirestoreUserHelper.getInstance();
            userHelper.retrieveContacts(new FirestoreUserHelper.ContactListCallback() {
                @Override
                public void onCallback(ArrayList<Contact> contactList) {
                    for (Contact c : contactList){
                        phoneNumbers.add(c.getPhoneNumber());
                    }
                }
            });

            ArrayList<Alert> allAlerts = new ArrayList<>();
            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD);
            messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for(DataSnapshot d : snapshot.getChildren()) {
                        HashMap message = (HashMap)snapshot.child(d.getKey()).getChildren().iterator().next().getValue();
                        if (message.get("id") != null) {
                            String[] info = ((String)message.get("id")).split(" ");
                            if (info.length == 3) {
                                String location = info[1] + " " + info[2];
                                String number =info[0];
                                if (phoneNumbers.contains(number)) {
                                    Alert a =  new Alert((String) d.getKey(), location.split(" ")[0], location.split(" ")[1]);
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(a.getLatLng())
                                            .title(a.getName())
                                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                                }
                            }
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
                Intent i = new Intent(getContext(), ChatActivity.class);
                i.putExtra("from", "visitor");
                i.putExtra("name", marker.getTitle());
                startActivity(i);
                return true;
            });

            if (curLocation != null) {
                Double padding = 0.001;
                LatLngBounds viewBounds = new LatLngBounds(
                        new LatLng(curLocation.latitude - padding, curLocation.longitude - padding), // SW bounds
                        new LatLng(curLocation.latitude + padding, curLocation.longitude + padding) // NE bounds
                );
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(viewBounds, 0));
            }

        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fetchLocation();

        return inflater.inflate(R.layout.fragment_alert_maps, container, false);
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    //Toast.makeText(getContext(), currentLocation.getLatitude() + " : " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    curLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    Double padding = 0.001;
                    LatLngBounds viewBounds = new LatLngBounds(
                            new LatLng(curLocation.latitude - padding, curLocation.longitude - padding), // SW bounds
                            new LatLng(curLocation.latitude + padding, curLocation.longitude + padding) // NE bounds
                    );
                    if(gMap != null) {
                        gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(viewBounds, 0));
                    }
                }
            }
        });
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