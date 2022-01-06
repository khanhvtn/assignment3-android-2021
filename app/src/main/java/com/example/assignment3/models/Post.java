package com.example.assignment3.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Post {
    private String userId, posterId, textContent,
            imageContentFileName;
    private Date timestamp;

    public Post() {
    }

    public Post(String userId, String posterId,
                String textContent, String imageContentFileName, Date timestamp) {
        this.userId = userId;
        this.posterId = posterId;
        this.textContent = textContent;
        this.imageContentFileName = imageContentFileName;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }


    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getImageContentFileName() {
        return imageContentFileName;
    }

    public void setImageContentFileName(String imageContentFileName) {
        this.imageContentFileName = imageContentFileName;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
