package com.example.assignment3.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {
    private String name;
    private String message;
    private String uidSender;
    private Date timestamp;
    private String photoUrl;
    private String imageUrl;
    private Boolean seenStatus;

    /**
     * Empty constructor using for Firebase
     */
    public Message() {
    }

    public Message(String uidSender, String name, String message, String photoUrl, String imageUrl,
                   Boolean seenStatus) {
        this.name = name;
        this.message = message;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
        this.uidSender = uidSender;
        this.seenStatus = seenStatus;
    }

    public Boolean getSeenStatus() {
        return seenStatus;
    }

    public void setSeenStatus(Boolean seenStatus) {
        this.seenStatus = seenStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUidSender() {
        return uidSender;
    }

    public void setUidSender(String uidSender) {
        this.uidSender = uidSender;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
