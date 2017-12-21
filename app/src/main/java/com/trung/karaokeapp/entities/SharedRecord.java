package com.trung.karaokeapp.entities;

import com.squareup.moshi.Json;

/**
 * Created by avc on 12/21/2017.
 */

public class SharedRecord {
    int id;
    String content;
    int score;
    String path;
    String type;
    @Json(name = "view_no")
    int viewNo;
    String duet;
    String created_at;
    String shared_at;
    @Json(name = "user_id")
    int userId;
    @Json(name = "kar_id")
    int karId;
    KaraokeSong karaoke;
    User user;
    public KaraokeSong getKaraoke() {
        return karaoke;
    }

    public void setKaraoke(KaraokeSong karaoke) {
        this.karaoke = karaoke;
    }

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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getViewNo() {
        return viewNo;
    }

    public void setViewNo(int viewNo) {
        this.viewNo = viewNo;
    }

    public String getDuet() {
        return duet;
    }

    public void setDuet(String duet) {
        this.duet = duet;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getShared_at() {
        return shared_at;
    }

    public void setShared_at(String shared_at) {
        this.shared_at = shared_at;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getKarId() {
        return karId;
    }

    public void setKarId(int karId) {
        this.karId = karId;
    }
}
