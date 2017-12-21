package com.trung.karaokeapp.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.TokenManager;
import com.trung.karaokeapp.adapter.AllSongsAdapter;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeeMoreSongsActivity extends AppCompatActivity {
    private static final String TAG = "SeeMoreSongsActivity";
    TokenManager tokenManager;
    ApiService service;
    Call<List<KaraokeSong>> call;

    @BindView(R.id.rvSongs)
    RecyclerView rvSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_more_songs);
        ButterKnife.bind(this);

        //Init
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        String title;
        String songType = getIntent().getStringExtra("songType");
        if (songType.equals("newSongs")) {
            title = "New Songs";
            call = service.getNewSongs(0);
        }else {
            title = "Feature Songs";
            call = service.getFeatureSongs(0);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get Songs
        call.enqueue(new Callback<List<KaraokeSong>>() {
            @Override
            public void onResponse(Call<List<KaraokeSong>> call, Response<List<KaraokeSong>> response) {
                Log.d(TAG, response.toString());
                rvSongs.setLayoutManager(new LinearLayoutManager(SeeMoreSongsActivity.this));
                AllSongsAdapter songsAdapter = new AllSongsAdapter(response.body(), getBaseContext());
                rvSongs.setAdapter(songsAdapter);
            }

            @Override
            public void onFailure(Call<List<KaraokeSong>> call, Throwable t) {
                Log.e(TAG, "Failure:" +  t.getMessage());
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:break;
        }
        return true;
    }
}
