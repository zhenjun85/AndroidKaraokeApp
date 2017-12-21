package com.trung.karaokeapp.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.trung.karaokeapp.entities.RecordUserKs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avc on 12/20/2017.
 */

public class RecentViewModel extends ViewModel {

    private MutableLiveData<List<RecordUserKs>> listSongsToDetele;

    public MutableLiveData<List<RecordUserKs>> getListSongsToDetele() {
        if (listSongsToDetele == null){
            listSongsToDetele = new MutableLiveData<>();
            List<RecordUserKs> mList = new ArrayList<>();
            listSongsToDetele.postValue(mList);
        }
        return listSongsToDetele;
    }
}
