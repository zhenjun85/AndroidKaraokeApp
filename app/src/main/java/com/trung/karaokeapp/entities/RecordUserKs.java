package com.trung.karaokeapp.entities;

/**
 * Created by avc on 12/20/2017.
 */

public class RecordUserKs {
    int user_id;
    int kar_id;
    String created_at;
    String updated_at;
    int count;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getKar_id() {
        return kar_id;
    }

    public void setKar_id(int kar_id) {
        this.kar_id = kar_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public KaraokeSong getSong() {
        return song;
    }

    public void setSong(KaraokeSong song) {
        this.song = song;
    }

    KaraokeSong song;

}
