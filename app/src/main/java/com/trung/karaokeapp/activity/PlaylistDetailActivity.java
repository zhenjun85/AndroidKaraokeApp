package com.trung.karaokeapp.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.adapter.PlaylistDetailAdatper;
import com.trung.karaokeapp.entities.HasPlaylistKs;
import com.trung.karaokeapp.entities.Playlist;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.network.TokenManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaylistDetailActivity extends AppCompatActivity {
    private static final String TAG = "PlaylistDetailActivity";
    @BindView(R.id.tvNumsongs) TextView tvNumsongs;
    @BindView(R.id.rvSongsInPlaylist) RecyclerView rvSongsInPlaylist;


    TokenManager tokenManager;
    ApiService service;
    Call<List<HasPlaylistKs>> callHasPlKs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);
        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get
        final Playlist playlist = new Gson().fromJson(getIntent().getStringExtra("playlist"), Playlist.class);

        //set
        getSupportActionBar().setTitle(playlist.getName());
        tvNumsongs.setText( playlist.getNumSongs() + (playlist.getNumSongs() < 2 ? " song" : " songs") );

        //load data
        callHasPlKs = service.getSongsInPlaylist(playlist.getId());
        callHasPlKs.enqueue(new Callback<List<HasPlaylistKs>>() {
            @Override
            public void onResponse(Call<List<HasPlaylistKs>> call, Response<List<HasPlaylistKs>> response) {
                Log.d(TAG, response.toString());
                List<HasPlaylistKs> hasPlaylistKsList = response.body();
                PlaylistDetailAdatper playlistDetailAdatper =
                        new PlaylistDetailAdatper(PlaylistDetailActivity.this, hasPlaylistKsList, service, tvNumsongs, playlist);
                rvSongsInPlaylist.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                rvSongsInPlaylist.setAdapter(playlistDetailAdatper);
            }

            @Override
            public void onFailure(Call<List<HasPlaylistKs>> call, Throwable t) {
                Log.d(TAG, "fail" + t.getMessage());
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        /*MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_playlist, menu);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            default:break;
        }

        return true;
    }
}
