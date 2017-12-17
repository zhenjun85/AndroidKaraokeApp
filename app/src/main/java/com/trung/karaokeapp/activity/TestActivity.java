package com.trung.karaokeapp.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import com.trung.karaokeapp.R;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_record);

        final String url1 = "http://10.0.2.2:8000/store/songs/Adele%20-%20Someone%20Like%20You/Adele%20-%20Someone%20Like%20You.mp3";
        final String url2 = "http://10.0.2.2:8000/store/songs/Adele - Someone Like You/Adele - Someone Like You.txt";
        final String url4 = "http://10.0.2.2:8000/store/1.mp3";
        final String url3 = "https://3.bp.blogspot.com/-EFwVj5ztKtQ/V8Qs6Viyl6I/AAAAAAAADWs/031SPYFrUnM-wreztTT4fgPe1yQj3LFhgCPcB/s1600/developer.jpg";

    }



}
