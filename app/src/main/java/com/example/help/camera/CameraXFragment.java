package com.example.help.camera;



import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.camera.core.CameraSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.help.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CameraXFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraXFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     *
     * Originally from CameraXActivity
     */
    // debug string
    private static final String TAG = "CameraXActivity";
    // permissions to check on launch
    private final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

    // Camera Selectors for front and rear available
    static final CameraSelector BACK_SELECTOR =
            new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
    static final CameraSelector FRONT_SELECTOR =
            new CameraSelector.Builder().requireLensFacing(
                    CameraSelector.LENS_FACING_FRONT).build();

    // isAlert bool will ultimately receive this flag from outside this class
    private static final boolean ALERT_ACTIVATED = false;
    static boolean isAlert = ALERT_ACTIVATED;

    // Executors - need executors to run separate to main thread
    // ScheduledExecutors for timelapse
    private ExecutorService mImageCaptureExecutorService;

    ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;

    // sensors for orientation detection
    private SensorManager sensorManager;
    private Sensor sensor;

    private PreviewView mPreviewViewFinder;
    private ImageButton mCaptureImageButton;

    /**
     * Is a photo being taken in Emergency mode or manually? if is Alert
     * then use SurfaceTexture for preview else use normal preview view
     */



    private Context safeContext;

    public CameraXFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CameraXFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CameraXFragment newInstance(String param1, String param2) {
        CameraXFragment fragment = new CameraXFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        safeContext = context;
    }

    /**
     * method to declare and bind the CameraX use cases
     */
    private void bindCameraUseCases() {

    }

    /*public void showToast(final String toast) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, toast, Toast.LENGTH_LONG).show());
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera_x, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCameraProviderFuture = ProcessCameraProvider.getInstance(safeContext);
        mPreviewViewFinder = (PreviewView) view.findViewById(R.id.viewFinder);

        mCaptureImageButton = (ImageButton) view.findViewById(R.id.imageCapture);

        if(allPermissionsGranted()){
            setupCamera(); //setup camera if permission has been granted by user

            // set onclick listener to runnable take photo to capture image button
            mCaptureImageButton.setOnClickListener(takePhotoOnClickListener);
        } else{
            ActivityCompat.requestPermissions((Activity) safeContext,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS);
        }


    }


    /**
     * Initialise the camera, the use cases and extensions (if any), set to preview/
     * surface texture and bind to lifecycle
     */
    public void setupCamera() {

    }

    /**
     * Method to take single still photo
     * May need to take params for which lens to use
     *
     * Currently modelled as an onclick listener for the image capture button with image capture use case
     * "takePicture" runnable method contained herein
     */

    private View.OnClickListener takePhotoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // takePicture method of cameraX
            // save image to file

        }
    };

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(safeContext, permission)) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(allPermissionsGranted()){
            setupCamera();
        } else{
            Toast.makeText(safeContext, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

   /* @RequiresPermission(Manifest.permission.CAMERA)
    public boolean hasCameraWithLensFacing(@CameraSelector.LensFacing int lensFacing) {
        String cameraId;
        try {
            cameraId = CameraX.getCameraWithLensFacing(lensFacing);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to query lens facing.", e);
        }

        return cameraId != null;
    }*/
}