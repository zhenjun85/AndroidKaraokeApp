package com.trung.karaokeapp.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class AllSongsFragment extends Fragment {
    public AllSongsFragment() {
        // Required empty public constructor
    }
    private static final String TAG = "AllSongsFragment";
    TokenManager tokenManager;
    ApiService service;
    Call<List<KaraokeSong>> call;
    private AllSongsAdapter adapter;

    @BindView(R.id.recyclerView)  RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song_book_all_songs, container, false);
        ButterKnife.bind(this, view);
        tokenManager = TokenManager.getInstance(getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        getAllSongs();

        return view;
    }

    private void getAllSongs() {
        call = service.getAllSongs();
        call.enqueue(new Callback<List<KaraokeSong>>() {
            @Override
            public void onResponse(Call<List<KaraokeSong>> call, Response<List<KaraokeSong>> response) {
                Log.d("response", response.toString());
                if (response.isSuccessful()) {
                    adapter = new AllSongsAdapter(response.body(), getContext());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<KaraokeSong>> call, Throwable t) {
                Log.d(TAG, "failure" + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel();
            call = null;
        }
    }
}
