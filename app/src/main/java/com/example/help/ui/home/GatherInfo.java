package com.example.help.ui.home;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OutputFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.help.MainActivity;
import com.example.help.R;
import com.example.help.ui.chatRoom.ChatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class GatherInfo extends AppCompatActivity implements OnMapReadyCallback {

    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    File myFile;
    String path = "";
    String TAG = "GatherInfo";
    TextView time,location,audio,timeBox,country,locality,street;
    Button play,confirm;
    double latitude;
    double longitude;
    Uri mImageToSendUri;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_info);


        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mImageToSendUri = null;
            } else {
                mImageToSendUri = (Uri) extras.get("mImageToSendUri");
            }
        } else {
            mImageToSendUri = (Uri) savedInstanceState.getSerializable("mImageToSendUri");
        }

//        Button and TextView
        time = findViewById(R.id.Time);
        timeBox = findViewById(R.id.timeBox);
        location = findViewById(R.id.Location);
        country = findViewById(R.id.Country);
        locality = findViewById(R.id.Locality);
        street = findViewById(R.id.Street);
        play = findViewById(R.id.Play);
        confirm = findViewById(R.id.Confirm);
        play.setEnabled(false);

        //Setting confirm button
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmSending();
            }
        });

        //Setting current time
        String currentTime = Calendar.getInstance().getTime().toString();
        time.setText(currentTime);
        //Add map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (!checkAudioPermission()) {
            requestAudioPermission();
        }
        startRecording();
        if (path != ""){play.setEnabled(true);}
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(path);
                if (mediaPlayer == null){
                    mediaPlayer = MediaPlayer.create(GatherInfo.this, uri);
                    startPlayer();
                    stopPlayer();
                }else{
                    try {
                        mediaPlayer.setDataSource(path);
                        mediaPlayer.prepareAsync();
                        startPlayer();
                        stopPlayer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//        get.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (ActivityCompat.checkSelfPermission(GatherInfo.this,
//                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                        && ActivityCompat.checkSelfPermission(GatherInfo.this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//                    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(GatherInfo.this, new OnCompleteListener<Location>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Location> task) {
//                            Location location = task.getResult();
//                            if(location !=null){
//                                try {
//                                    Geocoder geocoder = new Geocoder(GatherInfo.this,
//                                            Locale.getDefault());
//                                    List<Address> addresses = geocoder.getFromLocation(
//                                            location.getLatitude(),location.getLongitude(),1
//                                    );
//                                    country.setText(addresses.get(0).getCountryName());
//                                    locality.setText(addresses.get(0).getLocality());
//                                    street.setText(addresses.get(0).getAddressLine(0));
//
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }else {
//                            }
//                        }
//                    });
//                }else{
//                    ActivityCompat.requestPermissions(GatherInfo.this,
//                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
//                }
//
//            }
//        });
    }

    //Confirm sending the messages to Chatroom
    private void confirmSending(){
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        intent.putExtra("audioUrl",path);
        startActivity(intent);

    }
    //get current location
    private void getLocation (){
        FusedLocationProviderClient fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(GatherInfo.this);
        if (checkLocationPermission()) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(GatherInfo.this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    if (location != null) {
                        try {
                            Geocoder geocoder = new Geocoder(GatherInfo.this,
                                    Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(
                                    latitude, longitude, 1
                            );
                            country.setText(addresses.get(0).getCountryName());
                            locality.setText(addresses.get(0).getLocality());
                            street.setText(addresses.get(0).getAddressLine(0));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(GatherInfo.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
        }
    }
    //Check if the device has access to location
    public boolean checkLocationPermission(){
        int fine_location_result = ContextCompat.checkSelfPermission(GatherInfo.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse_location_result = ContextCompat.checkSelfPermission(GatherInfo.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return fine_location_result == PackageManager.PERMISSION_GRANTED &&
                coarse_location_result == PackageManager.PERMISSION_GRANTED;
    }
    //Automatically start recording and stop recording after 5 seconds
    public void startRecording() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        myFile = getFile();
        path = myFile.getAbsolutePath();
        mediaRecorder.setOutputFile(path);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(GatherInfo.this, "Start recording", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mediaRecorder.stop();
                        mediaRecorder.reset();
                        mediaRecorder.release();
                        Log.d(TAG,"released");
                        Handler handler = new Handler();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GatherInfo.this, "Stop recording", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }, 5000); //<-- Execute code after 15000 ms i.e after 15 Seconds.

    }
    //check if the device has the audio permission
    private boolean checkAudioPermission() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        int read_external_storage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED &&
                read_external_storage == PackageManager.PERMISSION_GRANTED;
    }
    //Request the audio permission
    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, 1000);
    }
    //Create a file
    private File getFile(){

        File audioDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"TestingAudio");
        audioDir.mkdirs();
        String audioDirPath = audioDir.getAbsolutePath();
        File recordingFile = new File(audioDirPath+"_"+System.currentTimeMillis()+".3pg");

//        Log.d(TAG, "Created file: " + recordingFile.getName());
//        Log.d(TAG, "File Path " + recordingFile.getAbsolutePath());


        return recordingFile;
    }
    //Start the player
    private void startPlayer(){
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer1) {
                mediaPlayer.start();
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GatherInfo.this, "Playing recording", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    //Stop the player
    private void stopPlayer() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer1) {
                if (mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    Toast.makeText(GatherInfo.this, "player stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        getLocation();
        LatLng curPosition = new LatLng(latitude,longitude);
        googleMap.addMarker(new MarkerOptions()
                .position(curPosition)
                .title("Current Position"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(curPosition));
    }
}