package com.example.help.ui.setting;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.help.R;
import com.example.help.ui.signIn.SignInActivity;
import com.firebase.ui.auth.AuthUI;


public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting_preference, rootKey);
        Preference logoutButton = findPreference("logout");
        logoutButton.setOnPreferenceClickListener(preference -> {
            signOut();
            return true;
        });
        EditTextPreference defaultMessagePreference = findPreference("default message");
        // TODO: do something with default message preference
    }
    private void signOut() {
        AuthUI.getInstance().signOut(this.getContext());
        startActivity(new Intent(this.getContext(), SignInActivity.class));
    }
}