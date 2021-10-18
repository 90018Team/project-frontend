package com.example.help.ui.setting;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.example.help.R;

public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingFragment())
                .commit();
    }
}
