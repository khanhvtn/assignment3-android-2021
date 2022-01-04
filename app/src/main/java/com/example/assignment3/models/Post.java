package com.example.assignment3.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Post {
    private String content;
    private String uidUser;
    private String imgUrl;
    private Date timeStamp;
    private Long reaction;
    private List<Comment> comments;

    public Post(String content, String uidUser) {
        this.content = content;
        this.uidUser = uidUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUidUser() {
        return uidUser;
    }

    public void setUidUser(String uidUser) {
        this.uidUser = uidUser;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @ServerTimestamp
    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Long getReaction() {
        return reaction;
    }

    public void setReaction(Long reaction) {
        this.reaction = reaction;
    }

}
