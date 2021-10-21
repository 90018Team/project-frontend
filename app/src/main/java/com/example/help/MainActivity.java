package com.example.help;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.help.ui.chatRoom.ChatActivity;
import com.example.help.ui.signIn.SignInActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.help.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        if(!isCurrentUserSignedIn()){
            Log.d("Main activity", "onCreate: "+"user not signed in");
            startActivity(new Intent(this,SignInActivity.class));
            finish();
            return;
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_alerts, R.id.navigation_contacts, R.id.navigation_setting)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Connect to network (local simulator for debug)
        // When running in debug mode, connect to the Firebase Emulator Suite.
        // "10.0.2.2" is a special IP address which allows the Android Emulator
        // to connect to "localhost" on the host computer. The port values (9xxx)
        // must match the values defined in the firebase.json file.
//        if (BuildConfig.DEBUG) {
//            FirebaseDatabase.getInstance().useEmulator("10.0.2.2", 9000);
//            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
//            FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199);
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(isCurrentUserSignedIn()){
            reload();
        }
        else{
            // otherwise, login first.
            Log.d("Main activity", "onCreate: "+"user not signed in");
            startActivity(new Intent(this,SignInActivity.class));
            finish();
            return;
        }
    }

    public boolean isCurrentUserSignedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return true;
        } else {
            return false;
        }
    }



    // Caleb: I have no idea why this is needed.
    // just keep it here, will dig into this later
    private void reload() { }
}