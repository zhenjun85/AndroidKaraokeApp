package com.trung.karaokeapp.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.trung.karaokeapp.fragment.FeedFragment;
import com.trung.karaokeapp.fragment.HomeFragment;
import com.trung.karaokeapp.fragment.NotificationFragment;
import com.trung.karaokeapp.fragment.ProfileFragment;
import com.trung.karaokeapp.fragment.SongBookFragment;

/**
 * Created by avc on 12/8/2017.
 */

public class MainActivityViewModel extends ViewModel {
    public HomeFragment homeFragment;
    public SongBookFragment songBookFragment;
    public FeedFragment feedFragment;
    public ProfileFragment profileFragment;
    public NotificationFragment notificationFragment;

    public MainActivityViewModel() {
        homeFragment = null;
        songBookFragment = null;
        feedFragment = null;
        profileFragment = null;
        notificationFragment = null;
    }

}
