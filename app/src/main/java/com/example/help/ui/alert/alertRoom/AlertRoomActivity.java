package com.example.help.ui.alert.alertRoom;

import androidx.appcompat.app.AppCompatActivity;
import com.example.help.R;
import com.example.help.databinding.ActivityChatBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AlertRoomActivity extends AppCompatActivity {
    private ActivityChatBinding mBinding;
    private static final String TAG = "AlertRoomActivity";
    private static String MESSAGES_CHILD = "/emergency_event/";
    private String imageUri = null;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public DatabaseReference mFirebaseDatabaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_room);
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // get intent
        Intent i = getIntent();
        String name = i.getStringExtra("name");
        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD + name);

    }

}