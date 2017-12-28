package com.trung.karaokeapp.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trung.karaokeapp.R;
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


public class RecommendedSongFragment extends Fragment {
    private static final String TAG = "RecommendedSongFragment";
    private TokenManager tokenManager;
    private ApiService service;
    @BindView(R.id.rvRecommend) RecyclerView rvRecommend;
    private Call<List<KaraokeSong>> callGetRecommend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_song_book_recommend, container, false);
        ButterKnife.bind(this, view);
        tokenManager = TokenManager.getInstance(getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        callGetRecommend = service.getRecommend(10);
        callGetRecommend.enqueue(new Callback<List<KaraokeSong>>() {
            @Override
            public void onResponse(Call<List<KaraokeSong>> call, Response<List<KaraokeSong>> response) {
                Log.d(TAG, response.toString());
                List<KaraokeSong> karaokeSongList = response.body();


            }

            @Override
            public void onFailure(Call<List<KaraokeSong>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (callGetRecommend != null){
            callGetRecommend.cancel();
            callGetRecommend = null;
        }
    }
}
