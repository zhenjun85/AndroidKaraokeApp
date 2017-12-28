package com.trung.karaokeapp.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.adapter.RankingAdapter;
import com.trung.karaokeapp.entities.HasPlaylistKs;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.entities.Playlist;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.network.TokenManager;

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
    Call<List<SharedRecord>> callUserRank;
    private KaraokeSong karaokeSong;
    private RankingAdapter rankingAdapter;
    private Call<List<Playlist>> callAllPlaylist;
    private List<Playlist> playlists;
    private CharSequence[] items;
    private boolean[] checkedItems;
    private Call<List<HasPlaylistKs>> callPlaylistOfSong;
    private Call<Integer> callAddOrRemoveSongInPlaylist;

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
        karaokeSong = new Gson().fromJson(getIntent().getStringExtra("song"), KaraokeSong.class);

        //setup SongDetail
        tvSongName.setText(karaokeSong.getName());
        String viewNo = karaokeSong.getViewNo() + ((karaokeSong.getViewNo() < 2) ? " view" : " views");
        tvSingerAndView.setText(karaokeSong.getArtist() + "    " + viewNo);
        //load coverSong
        String folderPath = AppURL.baseUrlSongAndLyric + "/" + karaokeSong.getLyric().substring(0, karaokeSong.getLyric().length() - 4);
        Glide.with(this).load(folderPath + "/" + karaokeSong.getImage()).into(ivCoverSong);

        //load ranking
        getUserRank();
    }
    void getUserRank() {
        callUserRank = service.getUserRank(karaokeSong.getId());
        callUserRank.enqueue(new Callback<List<SharedRecord>>() {
            @Override
            public void onResponse(Call<List<SharedRecord>> call, Response<List<SharedRecord>> response) {
                Log.d(TAG, response.toString());
                rankingAdapter = new RankingAdapter(getBaseContext(), response.body());
                rvRanking.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                rvRanking.setAdapter(rankingAdapter);

                getAllPlaylist();
            }

            @Override
            public void onFailure(Call<List<SharedRecord>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }
    void getAllPlaylist() {
        callAllPlaylist = service.getAllPlaylist();
        callAllPlaylist.enqueue(new Callback<List<Playlist>>() {
            @Override
            public void onResponse(Call<List<Playlist>> call, Response<List<Playlist>> response) {
                Log.d(TAG, response.toString());
                playlists = response.body();
                items = new CharSequence[playlists.size()];
                for (int i = 0; i < playlists.size(); i++) {
                    items[i] = playlists.get(i).getName();
                }
            }
            @Override
            public void onFailure(Call<List<Playlist>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void addSongToPlaylist() {
        checkedItems = new boolean[playlists.size()];

        callPlaylistOfSong = service.getPlaylistOfSong(karaokeSong.getId());
        callPlaylistOfSong.enqueue(new Callback<List<HasPlaylistKs>>() {
            @Override
            public void onResponse(Call<List<HasPlaylistKs>> call, Response<List<HasPlaylistKs>> response) {
                Log.d(TAG, response.toString());

                List<HasPlaylistKs> playlistOfSong = response.body();
                for (int j = 0; j < playlists.size(); j++){
                    boolean checkedItem = false;
                    for ( HasPlaylistKs item: playlistOfSong ){
                        if (item.getPlaylistId() == playlists.get(j).getId()){
                            checkedItem = true;
                        }
                    }
                    checkedItems[j] = checkedItem;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(SongDetailActivity.this);
                builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        checkedItems[i] = b;
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int k = 0; k < playlists.size(); k++) {
                            if (checkedItems[k]){
                                callAddOrRemoveSongInPlaylist = service.addSongToPlaylist(playlists.get(k).getId(), karaokeSong.getId());
                            }else {
                                callAddOrRemoveSongInPlaylist = service.delSongInPlaylist(playlists.get(k).getId(), karaokeSong.getId());
                            }
                            callAddOrRemoveSongInPlaylist.enqueue(new Callback<Integer>() {
                                @Override
                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                    Log.d(TAG, response.toString());
                                }

                                @Override
                                public void onFailure(Call<Integer> call, Throwable t) {
                                    Log.d(TAG, t.getMessage());
                                }
                            });
                        }
                        Toast.makeText(SongDetailActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                }).setTitle("Choose playlist:");
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                    }
                });
                alertDialog.show();
            }
            @Override
            public void onFailure(Call<List<HasPlaylistKs>> call, Throwable t) {
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
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.mn_add_song_to_playlist:
                addSongToPlaylist();
                break;
            default:break;
        }

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (callUserRank != null){
            callUserRank.cancel();
            callUserRank = null;
        }
        if (callAllPlaylist != null){
            callAllPlaylist.cancel();
            callAllPlaylist = null;
        }
        if (callPlaylistOfSong != null){
            callPlaylistOfSong.cancel();
            callPlaylistOfSong = null;
        }
        if (callAddOrRemoveSongInPlaylist != null){
            callAddOrRemoveSongInPlaylist.cancel();
            callAddOrRemoveSongInPlaylist = null;
        }
    }
}
