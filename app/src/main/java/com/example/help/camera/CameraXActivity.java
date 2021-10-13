package com.example.help.camera;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.help.R;

import java.util.Objects;

/**
* Some of this implementation taken from Android CameraXActivity coretestapp
* Jetpack CameraX demo app
* Will use predominantly 2 use cases (1) preview and (2) image capture.
 * With minor use of (3) image analysis, (4) video capture
 */
public class CameraXActivity extends AppCompatActivity {
    // debug string
    private static final String TAG = "CameraXActivity";
    private final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
    public CameraXActivity() {
        //super(R.layout.activity_camera_xactivity);
        //onRequestPermissionsResult(REQUEST_CODE_PERMISSIONS, REQUIRED_PERMISSIONS);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "CameraX Activity launched");
        //setupPermissions();
        setContentView(R.layout.activity_camera_xactivity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.camerax_fragment, CameraXFragment.class, null)
                    .commit();
        }

        Log.d(TAG, "CameraX Activity Launch completed");
    }

    // Function to check and request permission.
    /*public boolean checkPermissions() {

        for(String permission : REQUIRED_PERMISSIONS){
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission)) {
                // request permission
            }

        }


    }*/

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

   /* @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(MainActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }*/

    // need to put permissions temporarily back here
    /*private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, permission)) {
                continue;
            }
            return false;
        }
        return true;
    }*/

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(allPermissionsGranted()){
            //setupCamera();
        } else{
            Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/
    /**
     * Request permission if missing.
     */
    private void setupPermissions() {
        if (isPermissionMissing()) {
            ActivityResultLauncher<String[]> permissionLauncher =
                    registerForActivityResult(
                            new ActivityResultContracts.RequestMultiplePermissions(),
                            result -> {
                                for (String permission : REQUIRED_PERMISSIONS) {
                                    if (!Objects.requireNonNull(result.get(permission))) {
                                        Toast.makeText(getApplicationContext(),
                                                "Camera permission denied.",
                                                Toast.LENGTH_SHORT)
                                                .show();
                                        finish();
                                        return;
                                    }
                                }
                                return;
                                //tryBindUseCases();
                            });

            permissionLauncher.launch(REQUIRED_PERMISSIONS);
        } else {
            // Permissions already granted. Start camera.
            return;
            //tryBindUseCases();
        }
    }

    /** Returns true if any of the required permissions is missing. */
    private boolean isPermissionMissing() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }
}