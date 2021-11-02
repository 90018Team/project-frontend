package com.example.help.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraHelper {
    private Sensor mRotationOrientationSensor;
    private SensorManager mSensorManager;
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
    private Fragment thisFragment;
    private static final String TAG = "CameraHelper";


    public CameraHelper(Fragment fragment) {
        thisFragment = fragment;
        Activity activity = fragment.getActivity();
        mSensorManager = (SensorManager)activity.getBaseContext().getSystemService(Context.SENSOR_SERVICE);
        mRotationOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mCameraProviderFuture = ProcessCameraProvider.getInstance(activity.getBaseContext());
        mRotationOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    /**
     * Initialise the camera, the use cases and extensions (if any), set to preview/
     * surface texture and bind to lifecycle
     */
    public void takePhoto(UriCallback callback) {

        // Set up camera
        mCameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                mCameraProvider = mCameraProviderFuture.get();
                /**
                 * check if front and/or back cameras exist,
                 * automatically choose back if available
                 */

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


                SurfaceTexture mSurfaceTexture = new SurfaceTexture(10);
                Preview.SurfaceProvider surfaceProvider = request -> {
                    Size resolution = request.getResolution();
                    mSurfaceTexture.setDefaultBufferSize(resolution.getWidth(), resolution.getHeight());
                    Surface surface = new Surface(mSurfaceTexture);
                    request.provideSurface(surface, ContextCompat.getMainExecutor(thisFragment.getContext()), result -> {

                    });
                };

                // using surfacetexture - no ui view needed
                mPreview = new Preview.Builder().build();
                mPreview.setSurfaceProvider(surfaceProvider);

                // image capture
                mImageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // select given lens choice
                CameraSelector cameraSelector =
                        new CameraSelector.Builder().requireLensFacing(mLensFacingChoice).build();

                mCamera = mCameraProvider.bindToLifecycle(thisFragment.getViewLifecycleOwner(),
                        cameraSelector,
                        mPreview,
                        mImageCapture);

                Log.d(TAG, "Binding use cases complete");

                captureImage(callback);
//                takePicture();
                //bindCameraUseCases();

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            } 
            Log.d(TAG, "setup camera complete");
        }, ContextCompat.getMainExecutor(thisFragment.requireContext()));
    }

    public void captureImage(UriCallback callback){
        Log.d(TAG, "takePicture: taking");
        // get base directory
        File directory = getImageFolder();
        // create a file
        File file = createFile(directory, FILENAME, PHOTO_EXTENSION);

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(file).build();

        mImageCapture.takePicture(outputFileOptions, mImageCaptureExecutorService, new ImageCapture.OnImageSavedCallback() {

            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                // uri created to send to gatherinfo and chatroom etc.
                Uri mImageToSendUri = FileProvider.getUriForFile(thisFragment.getContext(), "com.example.help.fileprovider", file);
                Log.d(TAG, "Pic captured at " + file.getAbsolutePath());
                callback.onImageCaptured(mImageToSendUri);

                // unbind use cases so camera can be used again
                thisFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCameraProvider.unbindAll();
                    }
                });


            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Pic capture failed : " + exception.getMessage());
                callback.onFailure();
                exception.printStackTrace();
            }

        });

    }

    public interface UriCallback{
        void onImageCaptured(Uri uri);
        void onFailure();
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
     * Returns home images folder as defined in file_paths.xml
     * @return
     */
    private File getImageFolder() {
        File imagePath = new File(thisFragment.getContext().getExternalMediaDirs()[0].toString() + File.separator + "HELP_images");
        if (!imagePath.exists()) {
            imagePath.mkdir();
        }
        return imagePath;
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
    public void onResume() {
        mSensorManager.registerListener(mRotationalSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        mSensorManager.unregisterListener(mRotationalSensorEventListener);
    }
}
