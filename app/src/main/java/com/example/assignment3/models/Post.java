package com.example.assignment3.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

import javax.annotation.Nullable;

public class Post {
    private String userId, postId, textContent,
            imageContentFileName;
    private Date timestamp;

    public Post() {
    }

    public Post(String userId,
                @Nullable String textContent, @Nullable String imageContentFileName) {
        this.userId = userId;
        this.textContent = textContent;
        this.imageContentFileName = imageContentFileName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }


    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(@Nullable String textContent) {
        this.textContent = textContent;
    }

    public String getImageContentFileName() {
        return imageContentFileName;
    }

    public void setImageContentFileName(@Nullable String imageContentFileName) {
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
