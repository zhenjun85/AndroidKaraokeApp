package com.trung.karaokeapp.activity;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.TokenManager;
import com.trung.karaokeapp.adapter.RankingAdapter;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.network.RetrofitBuilder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongDetailActivity extends AppCompatActivity {
    private static final String TAG = "SongDetailActivity";
    @BindView(R.id.tv_songname_sdetail)    TextView tvSongName;
    @BindView(R.id.tvSingerAndView)    TextView tvSingerAndView;
    @BindView(R.id.ivCoverSong)    ImageView ivCoverSong;
    @BindView(R.id.collapse_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.rvRanking)
    RecyclerView rvRanking;

    private String songJson;
    private TokenManager tokenManager;
    private ApiService service;
    Call<List<SharedRecord>> callSrRanking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);
        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        songJson = getIntent().getStringExtra("song");

        //convert json to object
        KaraokeSong song = new Gson().fromJson(getIntent().getStringExtra("song"), KaraokeSong.class);

        //setup SongDetail
        tvSongName.setText(song.getName());
        String viewNo = song.getViewNo() + ((song.getViewNo() < 2) ? " view" : " views");
        tvSingerAndView.setText(song.getArtist() + "    " + viewNo);
        //load coverSong
        String folderPath = AppURL.baseUrlSongAndLyric + "/" + song.getLyric().substring(0, song.getLyric().length() - 4);
        Glide.with(this).load(folderPath + "/" + song.getImage()).into(ivCoverSong);

        //load ranking
        callSrRanking = service.getRank(song.getId());
        callSrRanking.enqueue(new Callback<List<SharedRecord>>() {
            @Override
            public void onResponse(Call<List<SharedRecord>> call, Response<List<SharedRecord>> response) {
                Log.d(TAG, response.toString());
                RankingAdapter rankingAdapter = new RankingAdapter(getBaseContext(), response.body());
                rvRanking.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                rvRanking.setAdapter(rankingAdapter);
            }

            @Override
            public void onFailure(Call<List<SharedRecord>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
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
