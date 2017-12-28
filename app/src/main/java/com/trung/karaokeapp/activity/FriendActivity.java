package com.trung.karaokeapp.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.adapter.FriendAdapter;
import com.trung.karaokeapp.entities.RelationTb;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.network.TokenManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendActivity extends AppCompatActivity {
    private static final String TAG = "FriendActivity";
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rvFriend) RecyclerView rvFriend;
    private TokenManager tokenManager;
    private ApiService service;
    private Call<List<RelationTb>> callAllRelation;
    private FriendAdapter friendAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        callAllRelation = service.getAllRelation();
        callAllRelation.enqueue(new Callback<List<RelationTb>>() {
            @Override
            public void onResponse(Call<List<RelationTb>> call, Response<List<RelationTb>> response) {
                Log.d(TAG, response.toString());
                List<RelationTb> tbList = response.body();
                friendAdapter = new FriendAdapter(getApplicationContext(), tbList, service);
                rvFriend.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                rvFriend.setAdapter(friendAdapter);
            }

            @Override
            public void onFailure(Call<List<RelationTb>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (callAllRelation != null){
            callAllRelation.cancel();
            callAllRelation = null;
        }
    }
}
