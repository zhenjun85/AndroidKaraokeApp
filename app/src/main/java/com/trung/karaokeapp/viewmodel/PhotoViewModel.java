package com.trung.karaokeapp.viewmodel;

import android.arch.lifecycle.ViewModel;

/**
 * Created by avc on 12/27/2017.
 */

public class PhotoViewModel extends ViewModel {
    public int getPosSelect() {
        return posSelect;
    }

    public void setPosSelect(int posSelect) {
        this.posSelect = posSelect;
    }

    private int posSelect = -1;


}
