package com.example.help.ui.home;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.help.R;


public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.home_fragment, HomeFragment.class, null)
                    .commit();
        }

        Log.d(TAG, "Home Activity Launch completed");

    }
}