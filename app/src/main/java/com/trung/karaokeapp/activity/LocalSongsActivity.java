package com.trung.karaokeapp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;


import com.trung.karaokeapp.R;
import com.trung.karaokeapp.network.TokenManager;
import com.trung.karaokeapp.adapter.LocalSongsAdapter;
import com.trung.karaokeapp.utils.AppBaseCode;
import com.trung.karaokeapp.entities.LocalRecord;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.network.RetrofitBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocalSongsActivity extends AppCompatActivity {
    private static final String TAG = "LocalSongsActivity";

    @BindView(R.id.recyclerView)
    RecyclerView rvLocalSongs;
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
        if (!directory.exists()){
            if (!directory.mkdirs()) {
                Log.d(TAG, "open folder fail");
                finish();
            }
        }

        File[] listFiles = directory.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            String fileName = listFiles[i].getName();

            if (fileName.substring( fileName.length() - 4 ).equals(".mp3")){
                String[] strings = fileName.substring(0, fileName.length() - 4).split("_");
                if (strings.length >= 5) {
                    LocalRecord newRecord = new LocalRecord(
                            Integer.parseInt(strings[0]),               //ksid
                            strings[1],                                 //name
                            listFiles[i].length()/1024f + "",       //size
                            strings[4].replace("-", ":"),   //duration
                            Integer.parseInt(strings[3]), listFiles[i].getPath(),  //score
                            "audio");
                    recordList.add(newRecord);
                }
            }else if(fileName.substring( fileName.length() - 4 ).equals(".mp4")) {
                String[] strings = fileName.substring(0, fileName.length() - 4).split("_");
                if (strings.length >= 5) {
                    LocalRecord newRecord = new LocalRecord(
                            Integer.parseInt(strings[0]),               //ksid
                            strings[1],                                 //name
                            listFiles[i].length()/1024f + "",       //size
                            strings[4].replace("-", ":"),   //duration
                            Integer.parseInt(strings[3]), listFiles[i].getPath(),  //score
                            "video");
                    recordList.add(newRecord);
                }
            }
        }

        //set list to recycler view
        localSongsAdapter = new LocalSongsAdapter(recordList, LocalSongsActivity.this, service);
        rvLocalSongs.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        rvLocalSongs.setAdapter(localSongsAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppBaseCode.POST_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d(TAG, "returnnnnn");
            int position = data.getIntExtra("position", -1);
            if (position == -1){
                return;
            }
            List<LocalRecord> updateList =  localSongsAdapter.getListLocalRecord();
            updateList.remove(position);
            localSongsAdapter = new LocalSongsAdapter(updateList, LocalSongsActivity.this, service);
            rvLocalSongs.setAdapter(localSongsAdapter);
        }
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
