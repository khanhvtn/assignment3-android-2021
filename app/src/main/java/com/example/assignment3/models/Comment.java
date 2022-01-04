package com.example.assignment3.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Comment {
    private String userID;
    private String content;
    private Long reaction;
    private Date timeStamp;

    public Comment(String userID, String content) {
        this.userID = userID;
        this.content = content;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getReaction() {
        return reaction;
    }

    public void setReaction(Long reaction) {
        this.reaction = reaction;
    }

    @ServerTimestamp
    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }


}
