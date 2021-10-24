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
import com.example.help.ui.chatRoom.ChatActivity;
import com.example.help.ui.setting.SettingFragment;
import com.example.help.util.jsonUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlertMapsFragment extends Fragment {

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


            // setBounds
            Double padding = 0.005;
            LatLngBounds viewBounds = new LatLngBounds(
                    new LatLng(curLocation.latitude - padding, curLocation.longitude - padding), // SW bounds
                    new LatLng(curLocation.latitude + padding, curLocation.longitude + padding) // NE bounds
                    );
            Alert[] alerts = new Alert[3];
            alerts[0] = new Alert(1, "gaga111", "-33.853", "151.212");
            alerts[1] = new Alert(2, "gaga222", "-33.853", "151.211");
            alerts[2] = new Alert(3, "gaga333", "-33.852", "151.212");

            // marker format
            int height = 100;
            int width = 120;
            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);



            for(Alert a: alerts) {
                googleMap.addMarker(new MarkerOptions()
                        .position(a.getLocation())
                        .title(a.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
            }


            googleMap.setOnMarkerClickListener(marker -> {
                Toast.makeText(getContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), AlertRoomActivity.class));
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