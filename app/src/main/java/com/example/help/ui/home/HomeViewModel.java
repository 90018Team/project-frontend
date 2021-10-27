package com.example.help.ui.home;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    private String TAG = HomeViewModel.class.getSimpleName();


    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    /*public LiveData<String> getText() {
        return mText;
    }

    public class SensorEventListener {

    }*/
}