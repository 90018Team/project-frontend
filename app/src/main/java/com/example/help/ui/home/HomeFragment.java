package com.example.help.ui.home;

import static android.content.Context.SENSOR_SERVICE;
import static androidx.core.content.FileProvider.getUriForFile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.help.R;
import com.example.help.databinding.HomeFragmentBinding;
import com.example.help.util.jsonUtil;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    //private HomeViewModel homeViewModel;
    private HomeFragmentBinding binding;

    private View frameLayout1;
    private ImageView imageView2;
    private Handler mHandler=new Handler();
    private ViewGroup.LayoutParams params;
    private int mHeight;
    private boolean isClick;

    /**
     * Begin data for cameraX operation
     *
     */
    // debug string
    private static final String TAG = "HomeFragment";
    private Sensor mRotationOrientationSensor;
    private SensorManager mSensorManager;

    // permissions to check on launch
    private final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

    // Camera Selectors for front and rear available
    // Default starting assume back camera
    private int mLensFacingChoice = CameraSelector.LENS_FACING_BACK;
    private ExecutorService mImageCaptureExecutorService = Executors.newCachedThreadPool();
    private ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;
    private ProcessCameraProvider mCameraProvider;
    // use cases
    private ImageCapture mImageCapture;
    private Preview mPreview;
    private Camera mCamera;
    private static final String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String PHOTO_EXTENSION = ".jpg";
    private boolean isFaceUp = false;
    private SurfaceTexture mSurfaceTexture;
    private Uri mImageToSendUri;
    /**
     * End data for cameraX functions
     *
     */


    public HomeFragment() {
        super(R.layout.home_fragment);
    }

    public HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ask for permissions NOW!
        setupPermissions();
        mSensorManager = (SensorManager)getActivity().getBaseContext().getSystemService(SENSOR_SERVICE);
        mRotationOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = HomeFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        imageView2 = root.findViewById(R.id.imageView2);
        frameLayout1 = root.findViewById(R.id.frameLayout1);
        params = frameLayout1.getLayoutParams();
        mHeight = params.height;


        imageView2.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isClick=true;
                        /**
                         * CameraX takes picture NOW
                         */
                        view.setOnTouchListener(takePhotoOnTouchListener);
                        startplay();

                        break;
                    case MotionEvent.ACTION_UP:
                        isClick=false;
                        endplay();
                        break;
                    default:
                        break;
                }
                return false;
            }

        });
        ////c
        try {
            JSONObject object = new JSONObject(jsonUtil.getJSON(getContext(),"getData.json"));
            Log.d("gaga", "onCreateView: " + object.getString("message"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /**
         * Do Camera Setup
         */
        mCameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        setupCamera(); //setup camera if permission has been granted by user

    }

    private void endplay() {
        params.height=mHeight;
        frameLayout1.setLayoutParams(params);
    }

    private void startplay() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isClick) {
                    params.height+=10;
                    if (params.height>=imageView2.getHeight()) {

                        params.height=imageView2.getHeight();
                        /*Toast.makeText(getContext(), "start", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getContext(), GatherInfo.class);

                        intent.putExtra("mImageToSendUri", mImageToSendUri);

                        startActivity(intent);*/
                    }else{
                        mHandler.postDelayed(this, 5);
                    }
                    frameLayout1.setLayoutParams(params);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

   /* @Override
    public void onStop() {
        super.onStop();
        mCameraProvider.unbindAll();
    }*/

    /**
     * Toast method, primarily used for debugging
     * @param toast
     */
    public void showToast(final String toast) {
        getActivity().runOnUiThread(() ->
                Toast.makeText(getActivity(), toast, Toast.LENGTH_LONG).show());
    }


    /** ------ CameraX methods below ------------------------------------- **/

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

                // Set up the view finder use case to display camera preview
                mPreview = new Preview.Builder().build();

                // image capture
                mImageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // chose back or front camera based on device orientation
                try {
                    if (isFaceUp && hasFrontCamera()) {
                        Log.d(TAG, "choose front camera");
                        mLensFacingChoice = CameraSelector.LENS_FACING_FRONT;
                    }
                    else if (!isFaceUp && hasBackCamera()) {
                        Log.d(TAG, "choose Back camera");
                        mLensFacingChoice = CameraSelector.LENS_FACING_BACK;
                    }
                    else { throw new IllegalStateException("No Camera Available"); }
                } catch (CameraInfoUnavailableException e) {
                    e.printStackTrace();
                }

                // select given lens choice
                CameraSelector cameraSelector =
                        new CameraSelector.Builder().requireLensFacing(mLensFacingChoice).build();

                mCamera = mCameraProvider.bindToLifecycle(getViewLifecycleOwner(),
                        cameraSelector,
                        mPreview,
                        mImageCapture);

                // SurfaceTexture output for preview
                SurfaceTexture mSurfaceTexture = new SurfaceTexture(10);

                Preview.SurfaceProvider surfaceProvider = request -> {
                    Size resolution = request.getResolution();
                    mSurfaceTexture.setDefaultBufferSize(resolution.getWidth(), resolution.getHeight());
                    Surface surface = new Surface(mSurfaceTexture);
                    request.provideSurface(surface, ContextCompat.getMainExecutor(getContext()), result -> {

                    });
                };
                // using surfacetexture - no ui view needed
                mPreview.setSurfaceProvider(surfaceProvider);

                Log.d(TAG, "Binding use cases complete");

                //bindCameraUseCases();

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "setup camera complete");
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * Method to take single still photo
     * May need to take params for which lens to use
     *
     * Currently modelled as an onclick listener for the image capture button with image capture use case
     * "takePicture" runnable method contained herein
     */
    private View.OnTouchListener takePhotoOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //chooseCameraOnDeviceOrientation();
            // get base directory
            File directory = getImageFolder();
            // create a file
            File file = createFile(directory, FILENAME, PHOTO_EXTENSION);

            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(file).build();

            // image capture takePicture method
            mImageCapture.takePicture(outputFileOptions, mImageCaptureExecutorService, new ImageCapture.OnImageSavedCallback() {

                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    String msg = "Pic captured at " + file.getAbsolutePath();
                    // uri created to send to gatherinfo and chatroom etc.
                    mImageToSendUri = getUriForFile(getContext(), "com.example.help.fileprovider", file);

                    //showToast(msg);
                    Log.d(TAG, msg);

                }
                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    String msg = "Pic capture failed : " + exception.getMessage();
                    //showToast(msg);
                    Log.e(TAG, msg);
                    exception.printStackTrace();
                }
            });
            return true;
            //return false;
        }
    };

    /**
     * Method for choosing Back or Front camera in an automated sense ie the camera being context
     * aware, in that if it is laying face down, its preference is for the back camera,
     * and if laying face up, its preference is for the front camera
     * Use Case: Alert Activated, then the user may not have control of the phone, dropped it or other
     */
    @SuppressLint("RestrictedApi")
    private void chooseCameraOnDeviceOrientation() {
        // first unbind camera
        mCameraProvider.unbindAll();
        //mLensFacingChoice = CameraSelector.LENS_FACING_BACK;
        try {
            if (isFaceUp && hasFrontCamera()) {
                Log.d(TAG, "choose front camera");
                mLensFacingChoice = CameraSelector.LENS_FACING_FRONT;
            }
            else if (!isFaceUp && hasBackCamera()) {
                Log.d(TAG, "choose Back camera");
                mLensFacingChoice = CameraSelector.LENS_FACING_BACK;
            }
            else { throw new IllegalStateException("No Camera Available"); }
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Chosen Camera is: " + mLensFacingChoice);

        // rebind use cases
        bindCameraUseCases();
    }

    /**
     * method to declare and bind the CameraX use cases
     */
    private void bindCameraUseCases() {

        SurfaceTexture mSurfaceTexture = new SurfaceTexture(10);
        Preview.SurfaceProvider surfaceProvider = request -> {
            Size resolution = request.getResolution();
            mSurfaceTexture.setDefaultBufferSize(resolution.getWidth(), resolution.getHeight());
            Surface surface = new Surface(mSurfaceTexture);
            request.provideSurface(surface, ContextCompat.getMainExecutor(getContext()), result -> {

            });
        };
        // using surfacetexture - no ui view needed
        mPreview = new Preview.Builder().build();
        mPreview.setSurfaceProvider(surfaceProvider);

        mPreview = new Preview.Builder().build();
        mPreview.setSurfaceProvider(surfaceProvider);

        // image capture
        mImageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        // select given lens choice
        CameraSelector cameraSelector =
                new CameraSelector.Builder().requireLensFacing(mLensFacingChoice).build();

        mCamera = mCameraProvider.bindToLifecycle(getViewLifecycleOwner(),
                cameraSelector,
                mPreview,
                mImageCapture);

        Log.d(TAG, "Binding use cases complete");

    }

    /**
     * Method that returns a filename formatted as specified
     * @param baseFolder
     * @param format
     * @param extension
     * @return
     */
    private File createFile(File baseFolder, String format, String extension) {
        return new File(baseFolder, (new SimpleDateFormat(format, Locale.US))
                .format(System.currentTimeMillis()) + extension);
    }

    /**
     * Returns home images folder as defined in file_paths.xml
     * @return
     */
    private File getImageFolder() {
        File imagePath = new File(getContext().getExternalMediaDirs()[0].toString() + File.separator + "HELP_images");
        if (!imagePath.exists()) {
            imagePath.mkdir();
        }
        return imagePath;
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



    /**-------------End CameraX Methods & Begin Sensor Methods---------------------------------**/

    /**
     * Sensor Event listener to determine whether device (when unattended) is laying face up or
     * face down in order to support decision to select back or front camera to take auto photo
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

        /**
         * Methods to set member variable isFaceUp for use in auto select front/back camera
         * in Auto Alert Image Capture event
         */
        private void onFaceUp() {
            if (!isFaceUp) {
                String msg = "Device is face up";
                //showToast(msg);
                Log.d(TAG, msg);
                isFaceUp = true;
            }
        }

        private void onFaceDown() {
            if (isFaceUp) {
                String msg = "Device is face down";
                //showToast(msg);
                Log.d(TAG, msg);
                isFaceUp = false;
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