package com.example.help.ui.alert.alertRoom;

import androidx.appcompat.app.AppCompatActivity;
import com.example.help.R;
import com.example.help.databinding.ActivityChatBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.os.Bundle;

public class AlertRoomActivity extends AppCompatActivity {
    private ActivityChatBinding mBinding;
    private static final String TAG = "ChatActivity";
    private static String MESSAGES_CHILD = "/emergency_event/";
    public DatabaseReference mFirebaseDatabaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_room);
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD);
    }

}