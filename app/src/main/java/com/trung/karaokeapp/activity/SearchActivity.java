package com.trung.karaokeapp.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.adapter.AllSongsAdapter;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.network.TokenManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    @BindView(R.id.toolbar) Toolbar toolbar;
    SearchView searchView;
    @BindView(R.id.rvSearchResult) RecyclerView rvSearchResult;
    private TokenManager tokenManager;
    private ApiService service;
    private Call<List<KaraokeSong>> callSearchKs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        toolbar.setTitle("Search");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        searchView = (SearchView) menu.findItem(R.id.mn_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearchKs = service.searchKs(query);
                callSearchKs.enqueue(new Callback<List<KaraokeSong>>() {
                    @Override
                    public void onResponse(Call<List<KaraokeSong>> call, Response<List<KaraokeSong>> response) {
                        Log.d(TAG, response.toString());
                        List<KaraokeSong> karaokeSongList = response.body();
                        AllSongsAdapter allSongsAdapter = new AllSongsAdapter(karaokeSongList, SearchActivity.this);
                        rvSearchResult.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        rvSearchResult.setAdapter(allSongsAdapter);
                    }

                    @Override
                    public void onFailure(Call<List<KaraokeSong>> call, Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
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

    @Override
    protected void onStop() {
        super.onStop();
        if (callSearchKs != null){
            callSearchKs.cancel();
            callSearchKs = null;
        }
    }
}
