package com.trung.karaokeapp.viewmodel;

import android.arch.lifecycle.ViewModel;

/**
 * Created by avc on 12/20/2017.
 */

public class AllSongViewModel extends ViewModel {
    private String sort = "none";
    private String genre = "all";
    private int chooseIndex = 0;
    private boolean[] booleans;

    public boolean[] getBooleans() {
        return booleans;
    }

    public void setBooleans(boolean[] booleans) {
        this.booleans = booleans;
    }

    public int getChooseIndex() {
        return chooseIndex;
    }

    public void setChooseIndex(int chooseIndex) {
        this.chooseIndex = chooseIndex;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
