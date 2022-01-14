package com.example.assignment3.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class ChatRoom {
    private String roomID;
    private List<String> userIDs;
    private Date timestamp;

    /**
     * Empty constructor using for Firebase
     */
    public ChatRoom() {
    }

    public ChatRoom(String roomID, List<String> userIDs) {
        this.roomID = roomID;
        this.userIDs = userIDs;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public List<String> getUserIDs() {
        return userIDs;
    }

    public void setUserIDs(List<String> userIDs) {
        this.userIDs = userIDs;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
