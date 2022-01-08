package com.example.assignment3.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

import javax.annotation.Nullable;

public class NotificationApp {
    private String message, type, targetId, userId;
    private Date timestamp;

    public NotificationApp() {
    }

    public NotificationApp(String message, String type, String targetId, String userId) {
        this.message = message;
        this.type = type;
        this.targetId = targetId;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
