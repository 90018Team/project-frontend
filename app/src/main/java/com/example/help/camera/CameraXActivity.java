package com.example.help.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.help.R;
/**
* Some of this implementation taken from Android CameraXActivity coretestapp
* Jetpack CameraX demo app
* Will use predominantly 2 use cases (1) preview and (2) image capture.
 * With minor use of (3) image analysis, (4) video capture
 */
public class CameraXActivity extends AppCompatActivity {
    // debug string
    private static final String TAG = "CameraXActivity";

    public CameraXActivity() {
        super(R.layout.activity_camera_xactivity);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.camerax_fragment, CameraXFragment.class, null)
                    .commit();
        }
    }




}