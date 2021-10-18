package com.example.help.models;

public class Message {
    private String text,name,photoUrl,imageUrl;

    // Empty constructor needed for Firestore serialization
    public Message() { }

    public Message(String text, String name, String photoUrl, String imageUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
    }
    public String getText(){
        return text;
    }
    public String getName(){
        return name;
    }
    public String getPhotoUrl(){
        return photoUrl;
    }
    public String getImageUrl(){
        return imageUrl;
    }
}
