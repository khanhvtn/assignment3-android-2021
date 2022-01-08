package com.example.assignment3.models;

import com.example.assignment3.utilities.Utility;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationApp {
    private String message, type;
    private Date timestamp;

    public NotificationApp() {
    }

    public NotificationApp(String message, String type) {
        this.message = message;
        this.type = type;
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
