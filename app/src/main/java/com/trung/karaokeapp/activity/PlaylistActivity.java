package com.trung.karaokeapp.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.adapter.PlaylistAdapter;
import com.trung.karaokeapp.entities.Playlist;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.network.TokenManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaylistActivity extends AppCompatActivity {
    private static final String TAG = "PlaylistActivity";
    @BindView(R.id.rvPlaylist) RecyclerView rvPlaylist;
    @BindView(R.id.fabAddPlaylist) FloatingActionButton fabAddPlaylist;

    TokenManager tokenManager;
    ApiService service;
    Call<List<Playlist>> callAllPl;
    private PlaylistAdapter playlistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        //toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(R.string.title_playlist);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        callAllPl = service.getAllPlaylist();
        callAllPl.enqueue(new Callback<List<Playlist>>() {
            @Override
            public void onResponse(Call<List<Playlist>> call, Response<List<Playlist>> response) {
                Log.d(TAG, response.toString());
                List<Playlist> playlists = response.body();
                playlistAdapter = new PlaylistAdapter(PlaylistActivity.this, playlists, service);
                rvPlaylist.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                rvPlaylist.setAdapter(playlistAdapter);
            }

            @Override
            public void onFailure(Call<List<Playlist>> call, Throwable t) {
                Log.d(TAG, "fail get pl" + t.getMessage());
            }
        });

    }

    @OnClick(R.id.fabAddPlaylist)
    void createPlaylist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistActivity.this);
        View view = LayoutInflater.from(PlaylistActivity.this).inflate(R.layout.dialog_add_playlist, null, false);
        final TextInputLayout tilNamePlaylist = view.findViewById(R.id.tilNamePlaylist);
        builder.setView(view)
            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Editable editText = tilNamePlaylist.getEditText().getText();
                    if (editText != null && !editText.toString().equals("")) {
                        Call<Playlist> callAddPl = service.addPlaylist(editText.toString());
                        callAddPl.enqueue(new Callback<Playlist>() {
                            @Override
                            public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                                Log.d(TAG, response.toString());
                                if ( response.body() != null) {
                                    List<Playlist> list = playlistAdapter.getPlaylists();
                                    list.add( response.body() );
                                    playlistAdapter = new PlaylistAdapter(PlaylistActivity.this, list, service);
                                    rvPlaylist.setAdapter(playlistAdapter);
                                    Toast.makeText(PlaylistActivity.this, "Adding playlist success!", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(PlaylistActivity.this, "Adding playlist fail!", Toast.LENGTH_LONG).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Playlist> call, Throwable t) {
                                Log.d(TAG, "failure" + t.getMessage());
                            }
                        });
                    }
                }
            });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                //alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            default: break;
        }

        return true;
    }
}
