package com.trung.karaokeapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.TokenManager;
import com.trung.karaokeapp.activity.SeeMorePopularSrActivity;
import com.trung.karaokeapp.activity.SeeMoreSongsActivity;
import com.trung.karaokeapp.adapter.AllSongsAdapter;
import com.trung.karaokeapp.adapter.FeatureSongsAdapter;
import com.trung.karaokeapp.adapter.NewSongsAdapter;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.network.RetrofitBuilder;

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

    @BindView(R.id.rvNewSongs) RecyclerView rvNewSongs;
    @BindView(R.id.rvFeatureSongs) RecyclerView rvFeatureSongs;
    @BindView(R.id.gvPopularSharedSongs)
    GridView gvPopularSharedRecords;

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
        Call<List<SharedRecord>> callSR = service.getPopularSr(5);
        callSR.enqueue(new Callback<List<SharedRecord>>() {
            @Override
            public void onResponse(Call<List<SharedRecord>> call, Response<List<SharedRecord>> response) {
                Log.d(TAG, response.toString());

                //GridView
                MyAdapter myAdapter = new MyAdapter(getContext(), response.body());
                gvPopularSharedRecords.setAdapter(myAdapter);

            }

            @Override
            public void onFailure(Call<List<SharedRecord>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    public class MyAdapter extends BaseAdapter {
        private List<SharedRecord> listSr;
        private Context context;

        MyAdapter(Context c, List<SharedRecord> listSr) {
            this.context = c;
            this.listSr = listSr;
        }

        @Override
        public int getCount() {
            return listSr.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = getLayoutInflater();
                view = inflater.inflate(R.layout.item_rv_shared_song_profile, viewGroup, false);
            }
            SharedRecord sr = listSr.get(i);

            TextView tvSongName = view.findViewById(R.id.tvSongName);
            tvSongName.setText(sr.getKaraoke().getName());
            TextView tvNumView = view.findViewById(R.id.tv_num_views);
            tvNumView.setText(sr.getViewNo() + (sr.getViewNo() > 1 ? " views" : " view"));
            TextView tvAuthor = view.findViewById(R.id.tv_post_author);
            tvAuthor.setText(sr.getUser().getName());
            TextView tvPostContent = view.findViewById(R.id.tv_post_content);
            tvPostContent.setText(sr.getContent());
            ImageView ivCoverSong = view.findViewById(R.id.iv_song_cover);

            String folderPath = sr.getKaraoke().getBeat().substring(0, sr.getKaraoke().getBeat().length() - 4);
            Glide.with(getContext()).load( AppURL.baseUrlSongAndLyric + "/" + folderPath + "/" + sr.getKaraoke().getImage() ).into(ivCoverSong);

            return view;
        }
    }



    private void getNewSongs() {
        callNewSongs = service.getNewSongs(5);
        callNewSongs.enqueue(new Callback<List<KaraokeSong>>() {
            @Override
            public void onResponse(Call<List<KaraokeSong>> call, Response<List<KaraokeSong>> response) {
                Log.d("response", response.toString());
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

    private void setUpToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.title_home);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_home);

        Menu menu = toolbar.getMenu();

        final SearchView searchView = (SearchView) menu.findItem(R.id.mn_search).getActionView();
        //change icon search view
        int searchImgId = android.support.v7.appcompat.R.id.search_button; // I used the explicit layout ID of searchview's ImageView
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.ic_search_black_24dp);

        searchView.setQueryHint("hihi");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("searchclick", "1");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
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
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
