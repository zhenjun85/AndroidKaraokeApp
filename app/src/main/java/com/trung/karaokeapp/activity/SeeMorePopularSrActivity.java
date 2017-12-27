package com.trung.karaokeapp.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.network.TokenManager;
import com.trung.karaokeapp.adapter.PopularSrAdapter;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeeMorePopularSrActivity extends AppCompatActivity {
    private static final String TAG = "SeeMorePopularSrActivit";
    @BindView(R.id.rvPopularSr) RecyclerView rvPopularSr;
    private TokenManager tokenManager;
    private ApiService service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_more_popular_sr);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Popular Shared Record");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        getPopularSharedRecords();
    }

    private void getPopularSharedRecords() {
        //get user record
        Call<List<SharedRecord>> callSR = service.getPopularSr(0);
        callSR.enqueue(new Callback<List<SharedRecord>>() {
            @Override
            public void onResponse(Call<List<SharedRecord>> call, Response<List<SharedRecord>> response) {
                Log.d(TAG, response.toString());

                //GridView
                rvPopularSr.setLayoutManager(new GridLayoutManager(getBaseContext(), 2));
                PopularSrAdapter myAdapter = new PopularSrAdapter(getBaseContext(), response.body());
                rvPopularSr.setAdapter(myAdapter);
            }

            @Override
            public void onFailure(Call<List<SharedRecord>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:break;
        }
        return true;
    }
}
