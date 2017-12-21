package com.trung.karaokeapp.entities;

/**
 * Created by avc on 12/21/2017.
 */

public class LocalRecord {
    int ksid;
    String name;
    String size;
    String duration;
    int score;
    String path;

    public LocalRecord(int ksid, String name, String size, String duration, int score, String path) {
        this.ksid = ksid;
        this.name = name;
        this.size = size;
        this.duration = duration;
        this.score = score;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getKsid() {
        return ksid;
    }

    public void setKsid(int ksid) {
        this.ksid = ksid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
