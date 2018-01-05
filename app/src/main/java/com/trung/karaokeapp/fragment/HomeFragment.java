package com.trung.karaokeapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.activity.LoginActivity;
import com.trung.karaokeapp.activity.SearchActivity;
import com.trung.karaokeapp.activity.SeeMorePopularSrActivity;
import com.trung.karaokeapp.activity.SeeMoreSongsActivity;
import com.trung.karaokeapp.adapter.FeatureSongsAdapter;
import com.trung.karaokeapp.adapter.NewSongsAdapter;
import com.trung.karaokeapp.adapter.PopularSrAdapter;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.entities.SharedRecord;
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


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    TokenManager tokenManager;
    ApiService service;

    Call<List<KaraokeSong>> callNewSongs;
    NewSongsAdapter adapterNewSongs;

    Call<List<KaraokeSong>> callFeatureSongs;
    FeatureSongsAdapter adapterFeatureSongs;

    Call<List<SharedRecord>> callPopularSR;
    private PopularSrAdapter adapterPopularSr;

    @BindView(R.id.rvNewSongs) RecyclerView rvNewSongs;
    @BindView(R.id.rvFeatureSongs) RecyclerView rvFeatureSongs;
    @BindView(R.id.rvPopularSr)RecyclerView rvPopularSr;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Toolbar toolbar = view.findViewById(R.id.my_toolbar);
        setUpToolBar(toolbar);
        ButterKnife.bind(this, view);
        tokenManager = TokenManager.getInstance(getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        getNewSongs();
        return view;
    }
    private void getNewSongs() {
        callNewSongs = service.getNewSongs(5);
        callNewSongs.enqueue(new Callback<List<KaraokeSong>>() {
            @Override
            public void onResponse(Call<List<KaraokeSong>> call, Response<List<KaraokeSong>> response) {
                Log.d("response", response.toString());

                //Unauthorized 401
                if (response.code() == 401) {
                    tokenManager.deleteToken();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }

                if (response.isSuccessful()) {
                    adapterNewSongs = new NewSongsAdapter(response.body(), getContext());
                    rvNewSongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    rvNewSongs.setAdapter(adapterNewSongs);
                    getFeatureSongs();
                }
            }
            @Override
            public void onFailure(Call<List<KaraokeSong>> call, Throwable t) {
                Log.d(TAG, "failure" + t.getMessage());
            }
        });
    }

    private void getFeatureSongs() {
        callFeatureSongs = service.getFeatureSongs(5);
        callFeatureSongs.enqueue(new Callback<List<KaraokeSong>>() {
            @Override
            public void onResponse(Call<List<KaraokeSong>> call, Response<List<KaraokeSong>> response) {
                Log.d("response", response.toString());
                if (response.isSuccessful()) {
                    adapterFeatureSongs = new FeatureSongsAdapter(response.body(), getContext());
                    rvFeatureSongs.setLayoutManager(new LinearLayoutManager(getContext()));
                    rvFeatureSongs.setAdapter(adapterFeatureSongs);

                    getPopularSharedRecords();
                }
            }
            @Override
            public void onFailure(Call<List<KaraokeSong>> call, Throwable t) {
                Log.d(TAG, "failure" + t.getMessage());
            }
        });
    }

    private void getPopularSharedRecords() {
        //get user record
        callPopularSR = service.getPopularSr(6);
        callPopularSR.enqueue(new Callback<List<SharedRecord>>() {
            @Override
            public void onResponse(Call<List<SharedRecord>> call, Response<List<SharedRecord>> response) {
                Log.d(TAG, response.toString());

                //GridView
                rvPopularSr.setLayoutManager(new GridLayoutManager(getContext(), 2));
                adapterPopularSr = new PopularSrAdapter(getContext(), response.body());
                rvPopularSr.setAdapter(adapterPopularSr);
            }

            @Override
            public void onFailure(Call<List<SharedRecord>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void setUpToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.title_home);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_home);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.mn_search:
                        Intent intent = new Intent(getContext(), SearchActivity.class);
                        startActivity(intent);
                        break;
                    default:break;
                }

                return true;
            }
        });
    }

    @OnClick(R.id.btnSeeMorePopularSR)
    void seeMorePopularSongs() {
        Intent intent = new Intent(getContext(), SeeMorePopularSrActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btnSeeMoreNewSongs)
    void seeMoreNewSongs() {
        Intent intent = new Intent(getContext(), SeeMoreSongsActivity.class);
        intent.putExtra("songType", "newSongs");
        startActivity(intent);
    }

    @OnClick(R.id.btnSeeMoreFeatureSongs)
    void seeMoreFeatureSongs() {
        Intent intent = new Intent(getContext(), SeeMoreSongsActivity.class);
        intent.putExtra("songType", "featureSongs");
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (callNewSongs != null) {
            callNewSongs.cancel();
            callNewSongs = null;
        }
        if (callFeatureSongs != null) {
            callFeatureSongs.cancel();
            callFeatureSongs = null;
        }
        if (callPopularSR != null) {
            callPopularSR.cancel();
            callPopularSR = null;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
