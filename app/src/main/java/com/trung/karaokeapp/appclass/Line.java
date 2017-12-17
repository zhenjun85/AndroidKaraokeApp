package com.trung.karaokeapp.appclass;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by trung on 11/15/2017.
 */

public class Line {
    public int start; //ms (beat the line is displayed)
    public int end;	//ms (beat the end line)
    public String lyric; // full lyric of the line
    public List<Note> notes; // all note of the line
    public Line() {
        this.start = 0;
        this.end = 0;
        this.lyric = "";
        this.notes = new LinkedList<>();
    }
}
