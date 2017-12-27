package com.trung.karaokeapp.entities;

import com.squareup.moshi.Json;

/**
 * Created by avc on 12/26/2017.
 */

public class CommentTb {
    int id;
    String content;
    @Json(name = "user_id")
    int userId;
    @Json(name = "sr_id")
    int srId;
    @Json(name = "created_at")
    String createdAt;
    User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSrId() {
        return srId;
    }

    public void setSrId(int srId) {
        this.srId = srId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
