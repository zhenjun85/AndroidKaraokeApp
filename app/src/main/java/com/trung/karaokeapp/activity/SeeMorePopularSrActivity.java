package com.trung.karaokeapp.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.TokenManager;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.fragment.HomeFragment;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.network.RetrofitBuilder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeeMorePopularSrActivity extends AppCompatActivity {
    private static final String TAG = "SeeMorePopularSrActivit";
    @BindView(R.id.gvPopularSharedSongs)
    GridView gvPopularSharedSongs;
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
                MyAdapter myAdapter = new MyAdapter(getBaseContext(), response.body());
                gvPopularSharedSongs.setAdapter(myAdapter);

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
            Glide.with(getBaseContext()).load( AppURL.baseUrlSongAndLyric + "/" + folderPath + "/" + sr.getKaraoke().getImage() ).into(ivCoverSong);

            return view;
        }
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
