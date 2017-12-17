package com.trung.app02;

/**
 * Created by trung on 11/15/2017.
 */

public class Note {
    public int start; //ms( beat start)
    public int length; //ms duration of note
    public int pitch;	//
    public int noteType; //0 for freestyle, 1 for normal, 2 for golden
    public String text; // text of this fragment
    public Note() {
        this.start = 0;
        this.length = 0;
        this.pitch = 0;
        this. noteType = 0;
        this.text = "";
    }
    public Note(int s, int l, int p, int nT, String t) {
        this.start = s;
        this.length = l;
        this.pitch = p;
        this. noteType = nT;
        this.text = t;
    }

}
