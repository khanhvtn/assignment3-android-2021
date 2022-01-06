package com.example.assignment3.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Comment {
    private String userId, textContent, commentId;
    private Date timestamp;

    public Comment() {
    }

    public Comment(String userId, String textContent) {
        this.userId = userId;
        this.textContent = textContent;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
}
