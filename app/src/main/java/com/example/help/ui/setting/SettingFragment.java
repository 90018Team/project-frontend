package com.example.help.ui.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.help.MainActivity;
import com.example.help.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class SettingFragment extends PreferenceFragmentCompat {
    private final String TAG = "Setting fragment";
    SharedPreferences sharedPrefs;
    private Preference messageToggle;
    //private Preference geolocationToggle;
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting_preference, rootKey);
        Preference logoutButton = findPreference("logout");
        logoutButton.setOnPreferenceClickListener(preference -> {
            signOut();
            return true;
        });

        messageToggle = findPreference("message");



        // get shared preferences
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //EditTextPreference defaultMessagePreference = findPreference("default message");
        // TODO: do something with default message preference
    }
    private void signOut() {
        AuthUI.getInstance().signOut(this.getContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: Sign out completed");
                startActivity(new Intent(getContext(), MainActivity.class));
                getActivity().finish();
            }
        });

    }



}