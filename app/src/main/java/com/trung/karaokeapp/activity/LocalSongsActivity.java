package com.trung.karaokeapp.activity;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;


import com.trung.karaokeapp.R;
import com.trung.karaokeapp.TokenManager;
import com.trung.karaokeapp.Utils;
import com.trung.karaokeapp.adapter.LocalSongsAdapter;
import com.trung.karaokeapp.entities.LocalRecord;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.network.RetrofitBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocalSongsActivity extends AppCompatActivity {
    private static final String TAG = "LocalSongsActivity";

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private LocalSongsAdapter localSongsAdapter;
    private TokenManager tokenManager;
    private ApiService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_songs);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Local Songs");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs",Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        //listLocalSongs
        List<LocalRecord> recordList = new ArrayList<>();

        //get all files in record folder
        File directory = new File(Environment.getExternalStorageDirectory().getPath() + "/" + AppURL.baseRecordsFolder);
        File[] listFiles = directory.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            //Log.d(TAG, listFiles[i].getName());
            String fileName = listFiles[i].getName();
            if (fileName.substring( fileName.length() - 4 ).equals(".mp3")){
                String[] strings = fileName.substring(0, fileName.length() - 4).split("_");
                if (strings.length >= 6) {

                    LocalRecord newRecord = new LocalRecord( Integer.parseInt(strings[0]),
                            strings[1], listFiles[i].length()/1024f + "",
                            strings[5].replace("-", ":"), Integer.parseInt(strings[4]), listFiles[i].getPath());
                    recordList.add(newRecord);
                }
            }
        }

        //set list to recycler view
        localSongsAdapter = new LocalSongsAdapter(recordList, getBaseContext(), service);
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        recyclerView.setAdapter(localSongsAdapter);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
