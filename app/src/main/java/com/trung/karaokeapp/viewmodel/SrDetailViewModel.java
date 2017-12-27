package com.trung.karaokeapp.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 * Created by avc on 12/26/2017.
 */

public class SrDetailViewModel extends ViewModel {
    private MutableLiveData<Boolean> isReady;

    public MutableLiveData<Boolean> getIsReady() {
        if (isReady == null) {
            isReady = new MutableLiveData<>();
            isReady.setValue(false);
        }
        return isReady;
    }

}
