package com.example.help.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.LocationManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.example.help.models.Alert;
import com.example.help.models.Message;

import com.example.help.ui.chatRoom.ChatActivity;
import com.example.help.R;
import com.example.help.databinding.HomeFragmentBinding;

import com.example.help.util.AudioRecorderHelper;
import com.example.help.util.CameraHelper;
import com.example.help.util.FirebaseStorageHelper;
import com.example.help.util.GPSHelper;
import com.example.help.util.jsonUtil;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private HomeFragmentBinding binding;

    private View frameLayout1;
    private ImageView imageView2;
    private Handler mHandler = new Handler();
    private ViewGroup.LayoutParams params;
    private int mHeight;
    private boolean isClick;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private LocationManager locationManager;
    private FirebaseStorageHelper storageHelper;
    private CameraHelper cameraHelper;

    private final HandlerThread gpsHandlerThread = new HandlerThread("GPS Handler Thread");
    private final HandlerThread audioHandlerThread = new HandlerThread("Audio Handler Thread");
    private final HandlerThread cameraHandlerThread = new HandlerThread("Camera Handler Thread");
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());


    private static final String TAG = "HomeFragment";

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        imageView2 = root.findViewById(R.id.imageView2);
        frameLayout1 = root.findViewById(R.id.frameLayout1);
        params = frameLayout1.getLayoutParams();
        mHeight = params.height;

        cameraHelper = new CameraHelper(this);
        storageHelper = new FirebaseStorageHelper();
        gpsHandlerThread.start();
        audioHandlerThread.start();
        cameraHandlerThread.start();

        imageView2.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isClick = true;
                        startplay();
                        break;
                    case MotionEvent.ACTION_UP:
                        isClick = false;
                        endplay();
                        break;

                    default:
                        break;
                }

                return false;
            }

        });

        try {
            JSONObject object = new JSONObject(jsonUtil.getJSON(getContext(), "getData.json"));
            Log.d("gaga", "onCreateView: " + object.getString("message"));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return root;
    }

    private void endplay() {

        params.height = mHeight;
        frameLayout1.setLayoutParams(params);
    }

    private void startplay() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isClick) {
                    params.height += 10;
                    if (params.height>=imageView2.getHeight()) {
                        params.height=imageView2.getHeight();
                        Toast.makeText(getContext(), "Emergency alert activated", Toast.LENGTH_SHORT).show();
                        alertActivated();
                    }else{
                        mHandler.postDelayed(this, 5);
                    }
                    frameLayout1.setLayoutParams(params);
                }
            }
        });

    }

    private void alertActivated() {
        Alert emergency = new Alert();
        // take photo while this fragment is still open
        cameraHelper.takePhoto(new CameraHelper.UriCallback() {
            @Override
            public void onImageCaptured(Uri uri) {
                // then start chat activity
                startActivity(new Intent(getContext(), ChatActivity.class));
                // store and send photo in background thread
                sendPhoto(uri);

            }

            @Override
            public void onFailure() {
                Log.d(TAG, "onFailure: image capture failed.");
                Toast.makeText(getContext(), "Automatic image capture failed.", Toast.LENGTH_SHORT);
            }
        });

        // get and send gps and audio data in background thread
        sendGpsData(emergency);
        sendAudioData(emergency);

    }

    private void sendGpsData(Alert emergency) {
        GPSHelper gps = new GPSHelper(this.getActivity());
        Handler threadHandler = new Handler(gpsHandlerThread.getLooper());
        threadHandler.post(new Runnable() {
            @Override
            public void run() {
                gps.getLocation(new GPSHelper.LocationCallback() {
                    @Override
                    public void onCallback(Location location) {
                        Log.d(TAG, "onCallback: location retrieved");
                        // set location of alert and send sms to contacts with geolink
                        emergency.setLocation(location);
                        emergency.sendSMSToContacts();

                        // send location in chat
                        Message chatMessage = new Message();
                        String txt = "I am in distress and need assistance. ";
                        txt += "My location is " + gps.getAddress(location);
                        chatMessage.setText(txt);
                        chatMessage.send();
                    }
                });
            }
        });

    }

    private void sendAudioData(Alert emergency){
        AudioRecorderHelper audioRecorder = new AudioRecorderHelper();
        Handler threadHandler = new Handler(audioHandlerThread.getLooper());
        threadHandler.post(new Runnable() {
            @Override
            public void run() {
                audioRecorder.record(new AudioRecorderHelper.AudioRecordListener() {
                    @Override
                    public void onFinishRecord(Uri uri) {
                        Log.d(TAG, "onFinishRecord: ");
                        // store file in firebase
                        storageHelper.storeFile(uri, new FirebaseStorageHelper.StorageListener() {
                            @Override
                            public void onSuccess(String filePath) {
                                // Send audio recording in chat
                                Message chatMessage = new Message();
                                // TODO: the audio is not saving properly in firebase and so the filepath
                                // is not valid. Test url is being used currently.
                                // chatMessage.setVoiceUrl(filePath)
                                chatMessage.setVoiceUrl("https://firebasestorage.googleapis.com/v0/b/fir-back-63b50.appspot.com/o/On17gmv2bXduC6SlkAw3OnC0P3d2%2F-MnNemkN1Jw0mosn2DvP%2F21?alt=media&token=f13bdbe0-ab50-4059-ba01-0eec9d00c402");
                                chatMessage.send();
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(getContext(), "Automatic voice recording failed.", Toast.LENGTH_SHORT);
                                Log.d(TAG, "onFailure: Audio recording upload failed.");
                            }
                        });
                    }
                });
            }
        });
    }

    private void sendPhoto(Uri uri) {
        Handler threadHandler = new Handler(cameraHandlerThread.getLooper());
        threadHandler.post(new Runnable() {
            @Override
            public void run() {
                storageHelper.storeFile(uri, new FirebaseStorageHelper.StorageListener() {
                    @Override
                    public void onSuccess(String filePath) {
                        Message chatMessage = new Message();
                        chatMessage.setImageUrl(filePath);
                        chatMessage.send();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(getContext(), "Automatic image capture failed.", Toast.LENGTH_SHORT);
                        Log.d(TAG, "onFailure: Photo upload failed.");
                    }
                });

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        cameraHelper.onResume();

    }
    @Override
    public void onPause() {
        super.onPause();
        cameraHelper.onPause();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        gpsHandlerThread.quit();
        audioHandlerThread.quit();
        cameraHandlerThread.quit();
        binding = null;
    }
}