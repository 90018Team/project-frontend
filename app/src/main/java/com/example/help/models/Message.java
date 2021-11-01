package com.example.help.models;

import android.location.Location;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.help.util.FirestoreUserHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Message {

    private String id;
    private Location location;
    private String text;
    private String name;
    private String photoUrl;
    private String imageUrl;
    private String voiceUrl;
    private String voiceTempPath;
    protected FirestoreUserHelper userHelper;
    private static String MESSAGES_CHILD = "/emergency_event/";

    String TAG = "Message";

    /**
     * Empty constructor required for Firebase auto data mapping
     */
    public Message() {
        userHelper = new FirestoreUserHelper(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    public Message(String text, String name, String photoUrl, String imageUrl, String voiceUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
        this.voiceUrl = voiceUrl;
        userHelper = new FirestoreUserHelper(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public String getVoiceTempPath() {
        return voiceTempPath;
    }

    public void setVoiceTempPath(String voiceTempPath) {
        this.voiceTempPath = voiceTempPath;
    }

    public void send(){
        FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD+getUserName())
                .push().setValue(this);
    }


    // TODO: Duplicated from chat function - should probably live in FirestoreUserHelper
    private String getUserName(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getDisplayName()==null?"ANONYMOUS":user.getDisplayName();
    }

}
