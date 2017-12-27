package com.trung.karaokeapp.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.trung.karaokeapp.entities.KaraokeSong;

/**
 * Created by avc on 12/24/2017.
 */

public class VideoViewModel extends ViewModel {
    private MutableLiveData<Boolean> isReady;
    private MutableLiveData<Boolean> isRecording;
    private String recordOnDevice;
    private KaraokeSong karaokeSong;

    public KaraokeSong getKaraokeSong() {
        return karaokeSong;
    }

    public void setKaraokeSong(KaraokeSong karaokeSong) {
        this.karaokeSong = karaokeSong;
    }

    public String getRecordOnDevice() {
        return recordOnDevice;
    }

    public void setRecordOnDevice(String recordOnDevice) {
        this.recordOnDevice = recordOnDevice;
    }

    public MutableLiveData<Boolean> getIsRecording() {
        if (isRecording == null) {
            isRecording = new MutableLiveData<>();
            isRecording.setValue(false);
        }
        return isRecording;
    }

    public MutableLiveData<Boolean> getIsReady() {
        if (isReady == null) {
            isReady = new MutableLiveData<>();
            isReady.setValue(false);
        }
        return isReady;
    }
}
