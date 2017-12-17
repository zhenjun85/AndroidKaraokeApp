package com.trung.app02;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by trung on 11/15/2017.
 */

public class LyricFile {
    public String title;
    public String artist;
    public String mp3;
    public float bpm;
    public int gap;
    public String genre;
    public String language;
    public String cover;
    public String background;
    public String encoding;
    public String date;
    public String author;
    public List<Line> allLines; // song lyric is displayed by line

    public LyricFile() {
        this.title = "";
        this.artist = "";
        this.mp3 = "";
        this.bpm = 0;
        this.gap = 0;
        this.genre= "";
        this.language = "";
        this.cover = "";
        this.background = "";
        this.encoding = "";
        this.date = "";
        this.author = "";
        this.allLines = new LinkedList<>();
    }
}
