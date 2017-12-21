package com.trung.karaokeapp;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by avc on 12/13/2017.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
