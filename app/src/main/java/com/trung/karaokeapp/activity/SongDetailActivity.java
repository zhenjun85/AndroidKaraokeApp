package com.trung.karaokeapp.activity;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.entities.KaraokeSong;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SongDetailActivity extends AppCompatActivity {
    @BindView(R.id.tv_songname_sdetail)    TextView tvSongName;
    @BindView(R.id.tv_singername_sdatail)    TextView tvSinger;
    @BindView(R.id.tv_numview_sdetail)    TextView tvNumView;
    @BindView(R.id.iv_avatar)    ImageView ivAvatar;

    private String songJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        songJson = getIntent().getStringExtra("song");

        KaraokeSong song = new Gson().fromJson(getIntent().getStringExtra("song"), KaraokeSong.class);
        setupSongDetail(song);
    }

    @OnClick(R.id.btn_record)
    void openRecord() {
        Intent intent = new Intent(getApplicationContext(), RecordActivity.class);
        intent.putExtra("song", songJson);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.btn_duet_open)
    void openDuet() {
        Intent intent = new Intent(getApplicationContext(), RecordDuetActivity.class);
        intent.putExtra("song", songJson);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.btn_video_open)
    void openVideo() {
        Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
        intent.putExtra("song", songJson);
        startActivity(intent);
        finish();
    }


    private void setupSongDetail(KaraokeSong song) {
        tvSongName.setText(song.getName());
        tvNumView.setText(song.getViewNo() + ((song.getViewNo() < 2) ? " view" : " views"));
        tvSinger.setText(song.getArtist());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_songdetail, menu);

        return super.onCreateOptionsMenu(menu);
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
