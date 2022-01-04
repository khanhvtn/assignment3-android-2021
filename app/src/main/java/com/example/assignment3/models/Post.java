package com.example.assignment3.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Post {
    private String posterId,posterImageFileName,posterName, textContent, imageContentFileName;
    private Date timestamp;

    public Post() {
    }

    public Post(String posterId, String posterImageFileName, String posterName,
                String textContent, String imageContentFileName, Date timestamp) {
        this.posterId = posterId;
        this.posterImageFileName = posterImageFileName;
        this.posterName = posterName;
        this.textContent = textContent;
        this.imageContentFileName = imageContentFileName;
        this.timestamp = timestamp;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public String getPosterImageFileName() {
        return posterImageFileName;
    }

    public void setPosterImageFileName(String posterImageFileName) {
        this.posterImageFileName = posterImageFileName;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
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
