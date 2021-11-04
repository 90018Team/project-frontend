package com.example.help.models;

import android.location.Location;

import com.example.help.util.FirestoreUserHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Message {

    private String id;
    private Location location;
    private String text;
    private String name;
    private String photoUrl;
    private String imageUrl;
    private String voiceUrl;
    private String voiceTempPath;
    private Date timeStamp;
    protected FirestoreUserHelper userHelper = FirestoreUserHelper.getInstance();
    private static String MESSAGES_CHILD = "/emergency_event/";

    String TAG = "Message";

    /**
     * Empty constructor required for Firebase auto data mapping
     */
    public Message() {
        timeStamp = new Date();
        name = userHelper.getUserName();
    }

    public Message(String text, String name, String photoUrl, String imageUrl, String voiceUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
        this.voiceUrl = voiceUrl;
        timeStamp = new Date();
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

//    public void setTimeStamp(){
//        timeStamp = new Date();
//    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getTimeStampStr(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return formatter.format(timeStamp);
    }

    public void send(){
        FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD+name)
                .push().setValue(this);
    }

}
