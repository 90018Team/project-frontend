package com.example.help.camera;


import static android.content.Context.SENSOR_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.help.R;
import com.example.help.databinding.FragmentCameraXBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CameraXFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraXFragment extends Fragment /**implements SensorEventListener*/ {


    /**
     *
     * Originally from CameraXActivity
     */
    // debug string
    private static final String TAG = "CameraXFragment";

    /**
     * to detect if camera is up or down
     */
    private Sensor mRotationOrientationSensor;
    private SensorManager mSensorManager;

    // permissions to check on launch
    private final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

    // Camera Selectors for front and rear available
    // Default starting assume back camera
    private int mLensFacingChoice = CameraSelector.LENS_FACING_BACK;


    // Executors - need executors to run separate to main thread
    // ScheduledExecutors for timelapse
    private ExecutorService mImageCaptureExecutorService = Executors.newFixedThreadPool(10);

    ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;
    ProcessCameraProvider mCameraProvider;

    private PreviewView mPreviewViewFinder;
    private ImageButton mCaptureImageButton;
    private ImageButton mSwitchCameraButton;

    // use cases
    private ImageCapture mImageCapture;
    private Preview mPreview;

    private Camera mCamera;

    private static final String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String PHOTO_EXTENSION = ".jpg";

    private boolean isFaceUp = false;


    /**
     * Is a photo being taken in Emergency mode or manually? if is Alert
     * then use SurfaceTexture for preview else use normal preview view
     *
     * Need to get value from Main Activity or Alert activity
     */
    // isAlert bool will ultimately receive this flag from outside this class
    private static final boolean ALERT_ACTIVATED = false;
    static boolean isAlert = ALERT_ACTIVATED;


    // View Binding
    private FragmentCameraXBinding fragmentCameraXBinding;

    private Context safeContext;

    public CameraXFragment() {
        // Required empty public constructor
        super(R.layout.fragment_camera_x);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CameraXFragment.
     */
    // TODO: Rename and change types and number of parameters
    public CameraXFragment newInstance() {
        CameraXFragment fragment = new CameraXFragment();

        Log.d(TAG, "Fragment New Instance launched");

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ask for permissions NOW!
        setupPermissions();
        // I don't know if this should go here, in example it goes in 'onResume()'
        mSensorManager = (SensorManager)getActivity().getBaseContext().getSystemService(SENSOR_SERVICE);
        mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        safeContext = context;
    }



    public void showToast(final String toast) {
        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), toast, Toast.LENGTH_LONG).show());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        /*ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_camera_x, container, false);
        mPreviewViewFinder = (PreviewView) root.findViewById(R.id.viewFinder);
        mCaptureImageButton = (ImageButton) root.findViewById(R.id.image_capture_button);
        mSwitchCameraButton = (ImageButton) root.findViewById(R.id.switch_camera_button);*/

        fragmentCameraXBinding = FragmentCameraXBinding.inflate(inflater, container, false);
        View view = fragmentCameraXBinding.getRoot();

        Log.d(TAG, "on create view complete");
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //newInstance();
        mCameraProviderFuture = ProcessCameraProvider.getInstance(safeContext);
        setupCamera(); //setup camera if permission has been granted by user

        // set onclick listener to runnable take photo to capture image button
        //mCaptureImageButton.setOnClickListener(takePhotoOnClickListener);
        fragmentCameraXBinding.imageCaptureButton.setOnClickListener(takePhotoOnClickListener);
        // set onclick listener to runnable to switch lens
        //mSwitchCameraButton.setOnClickListener(switchCameraOnClickListener);
        fragmentCameraXBinding.switchCameraButton.setOnClickListener(switchCameraOnClickListener);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentCameraXBinding = null;
    }

    /**
     * Initialise the camera, the use cases and extensions (if any), set to preview/
     * surface texture and bind to lifecycle
     */

    public void setupCamera() {
        mCameraProviderFuture.addListener(() -> {

            try {
                // Camera provider is now guaranteed to be available
                mCameraProvider = mCameraProviderFuture.get();

                /**
                 * check if front and/or back cameras exist,
                 * automatically choose back if available
                 */
                try {
                    if (hasBackCamera()) {
                        // already default
                        mLensFacingChoice = CameraSelector.LENS_FACING_BACK;
                    }
                    else if (hasFrontCamera()) {
                        mLensFacingChoice = CameraSelector.LENS_FACING_FRONT;
                    }
                    else { throw new IllegalStateException("No Camera Available"); }
                } catch (CameraInfoUnavailableException e) {
                    e.printStackTrace();
                }

                // enable switching between Front & Back?

                // bind use cases
                // use list of use cases?
                bindCameraUseCases();

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "setup camera complete");
        },ContextCompat.getMainExecutor(requireContext()));
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

            // get base directory
            File directory = getBaseFolder();
            // create a file
            File file = createFile(directory, FILENAME, PHOTO_EXTENSION);

            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(file).build();

            // image capture takePicture method
            mImageCapture.takePicture(outputFileOptions, mImageCaptureExecutorService, new ImageCapture.OnImageSavedCallback() {

                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    String msg = "Pic captured at " + file.getAbsolutePath();
                    //Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
                    showToast(msg);
                    Log.d(TAG, msg);
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    String msg = "Pic capture failed : " + exception.getMessage().toString();
                    //Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
                    showToast(msg);
                    exception.printStackTrace();
                }
            });
        }
    };

    /**
     * Method to switch between back & front + vice/versa camera source to view
     * and image capture
     */
    private View.OnClickListener switchCameraOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) { switchCamera(); }
    };

    // method to actually switch the camera
    private void switchCamera() {
        // first unbind camera
        mCameraProvider.unbindAll();

        // rebind with new CameraSelector
        try {
            // make sure front camera exists
            if (mLensFacingChoice == CameraSelector.LENS_FACING_BACK && hasFrontCamera()) {
                mLensFacingChoice = CameraSelector.LENS_FACING_FRONT;
            } else {
                mLensFacingChoice = CameraSelector.LENS_FACING_BACK;
            }
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        // rebind use cases
        bindCameraUseCases();
    }


    private void autoSwitchCameraOnAlertActivated() {
        // first unbind camera
        mCameraProvider.unbindAll();

        if (isFaceUp) {
            try {
                if (mLensFacingChoice == CameraSelector.LENS_FACING_BACK && hasFrontCamera()) {
                    mLensFacingChoice = CameraSelector.LENS_FACING_FRONT;
                }
            } catch (CameraInfoUnavailableException e) {
                e.printStackTrace();
            }
        } else {
            mLensFacingChoice = CameraSelector.LENS_FACING_BACK;
        }

        // rebind use cases
        bindCameraUseCases();
    }

    /**
     * method to declare and bind the CameraX use cases
     */
    private void bindCameraUseCases() {

        // preview

        //float rotation =  mPreviewViewFinder.getRotation();
        mPreview = new Preview.Builder().build();
        // set surface provider

        mPreview.setSurfaceProvider(fragmentCameraXBinding.viewFinder.getSurfaceProvider());

        // image capture
        mImageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // camera using back selector bind to lifecycle
        /**
         * will ultimately be able to select back or front
         */

        CameraSelector cameraSelector =
                new CameraSelector.Builder().requireLensFacing(mLensFacingChoice).build();

        /**
         * Extensions currently don't work with CameraX libraries
         * Issue: ProcessCameraProvider inherited from CameraProvider but
         * here the former cannot be passed as the latter
         */

        mCamera = mCameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                mPreview,
                mImageCapture);
        Log.d(TAG, "Binding use cases complete");
    }

    // helper method to return a Java file given the params
    private File createFile(File baseFolder, String format, String extension) {
        return new File(baseFolder, (new SimpleDateFormat(format, Locale.US))
                .format(System.currentTimeMillis()) + extension);
    }

    // get base folder for saving images?
    private File getBaseFolder() {
        ContextWrapper cw = new ContextWrapper(getActivity().getBaseContext());

        //String fullPath = cw.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString();
        File baseFolder = cw.getExternalFilesDir(Environment.DIRECTORY_DCIM);

        return baseFolder;
    }

    /**
     * Request permission if missing.
     *
     * Must further prompt user to explain why camera permission required
     */
    private void setupPermissions() {
        if (isPermissionMissing()) {
            ActivityResultLauncher<String[]> permissionLauncher =
                    registerForActivityResult(
                            new ActivityResultContracts.RequestMultiplePermissions(),
                            result -> {
                                for (String permission : REQUIRED_PERMISSIONS) {
                                    if (!Objects.requireNonNull(result.get(permission))) {
                                        Toast.makeText(getActivity().getApplicationContext(),
                                                "Camera permission denied.",
                                                Toast.LENGTH_SHORT)
                                                .show();
                                        getActivity().finish();
                                        //return;
                                    }
                                }
                                // permissions granted here - return flow success
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
            if (ContextCompat.checkSelfPermission(getActivity().getBaseContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    /**
     * Methods to determine if front and back facing cameras exist. Return true if they exist,
     * false otherwise
     */
    /** Front camera */
    private boolean hasFrontCamera() throws CameraInfoUnavailableException {
        return mCameraProvider != null && mCameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
    }
    /** Back camera */
    private boolean hasBackCamera() throws CameraInfoUnavailableException {
        return mCameraProvider != null && mCameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
    }

    /**
     * Sensor Event listener to determine whether device (when unattended) is laying face up or face down
     * in order to support decision to select back or front camera to take auto photo
     */
    private final SensorEventListener mRotationalSensorEventListener = new SensorEventListener() {

        /**
        * Method adapted from Professional Android Sensor Programming - Milette & Stroud 2012
         */
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // acquire measurements, determine is up / down
            float[] rotationMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(rotationMatrix,
                    sensorEvent.values);
            determineOrientation(rotationMatrix);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        /**
         * Methods to set member variable isFaceUp for use in auto select front/back camera
         * in Auto Alert Image Capture event
         */
        private void onFaceUp() {
            if (!isFaceUp) {
                String msg = "Device is face up";
                showToast(msg);
                isFaceUp = true;
            }
        }

        private void onFaceDown() {
            if (isFaceUp) {
                String msg = "Device is face down";
                showToast(msg);
                isFaceUp = false;
            }
        }
        /**
         * Method adapted from Professional Android Sensor Programming - Milette & Stroud 2012
         *
         * @param rotationMatrix The rotation matrix to use if the orientation
         * calculation
         */
        private void determineOrientation(float[] rotationMatrix) {
            float[] orientationValues = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationValues);

            // pitch & roll x & y angles determine whether device is flattish
            // values determine whether face up or down
            double pitch = Math.toDegrees(orientationValues[1]);
            double roll = Math.toDegrees(orientationValues[2]);

            if (pitch <= 10) {
                if (Math.abs(roll) >= 170) {
                    onFaceDown();
                }
                else if (Math.abs(roll) <= 10) {
                    onFaceUp();
                }
            }
        }

    };

    /**
     * Required methods to register and unregister sensor event listener
     */
    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mRotationalSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mRotationalSensorEventListener);
        super.onPause();
    }

}
