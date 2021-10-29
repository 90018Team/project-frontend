package com.example.help.ui.home;

import static android.content.Context.SENSOR_SERVICE;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends AndroidViewModel {


    private MutableLiveData<String> mText;

    public MutableLiveData<Boolean> isFaceUp;

    private String TAG = HomeViewModel.class.getSimpleName();
    public OrientationSensorLiveData orientationSensorLiveData;

    public HomeViewModel(Application application) {
        super(application);

        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
        isFaceUp = new MutableLiveData<>();
        isFaceUp.setValue(false);
        orientationSensorLiveData = new OrientationSensorLiveData(application);
    }

    public LiveData<Boolean> getOrientation() {
        return isFaceUp;
    }

    public class OrientationSensorLiveData extends LiveData<SensorEvent> implements
            SensorEventListener {

        private SensorManager mSensorManager;
        private Sensor mRotationOrientationSensor;
        private boolean isDeviceFaceUp = false;


        public OrientationSensorLiveData(Context context) {
            mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            mRotationOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        @Override
        protected void onActive() {
            super.onActive();
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        protected void onInactive() {
            mSensorManager.unregisterListener(this);
            super.onInactive();

        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // acquire measurements, determine is up / down
            float[] rotationMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(rotationMatrix,
                    sensorEvent.values);
            determineOrientation(rotationMatrix);
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
                    if (isFaceUp.equals(false)) {
                        String msg = "Device is face up";
                        //showToast(msg);
                        isFaceUp.setValue(true);
                    }
                }
                else if (Math.abs(roll) <= 10) {
                    if (isFaceUp.equals(true)) {
                        String msg = "Device is face down";
                        //showToast(msg);
                        isFaceUp.setValue(false);
                    }
                }
            }
        }



        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}