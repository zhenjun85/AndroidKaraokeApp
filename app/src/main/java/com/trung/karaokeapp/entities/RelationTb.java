package com.trung.karaokeapp.entities;

import com.squareup.moshi.Json;

/**
 * Created by avc on 12/28/2017.
 */

public class RelationTb {
    @Json(name = "user_id")
    int userId;
    @Json(name =  "other_id")
    int otherId;
    String status;
    @Json(name = "created_at")
    String createdAt;
    User other;

    public User getOther() {
        return other;
    }

    public void setOther(User other) {
        this.other = other;
    }



    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getOtherId() {
        return otherId;
    }

    public void setOtherId(int otherId) {
        this.otherId = otherId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
