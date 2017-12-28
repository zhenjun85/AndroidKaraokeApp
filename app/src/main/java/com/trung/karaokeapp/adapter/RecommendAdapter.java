package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.activity.SongDetailActivity;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by trung on 12/29/2017.
 */

public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.MyViewHolder> {
    private Context context;
    private List<KaraokeSong> karaokeSongList;
    private ApiService service;

    public RecommendAdapter(Context context, List<KaraokeSong> karaokeSongList, ApiService service) {
        this.context = context;
        this.karaokeSongList = karaokeSongList;
        this.service = service;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_recommend, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final KaraokeSong song = karaokeSongList.get(position);
        holder.tvSongName.setText(song.getName());
        holder.tvNumViews.setText(song.getViewNo() + ((song.getViewNo() < 2) ? " view" : " views"));
        holder.tvSinger.setText(song.getArtist());


        if (Utils.dayBetweenPastAndNow(song.getCreatedAt()) <= 7) {
            Drawable leftDrawable = context.getResources().getDrawable(R.drawable.ic_stars_black);
            holder.tvSinger.setCompoundDrawablesRelativeWithIntrinsicBounds(leftDrawable, null, null, null);
        }
        else {
            holder.tvSinger.setCompoundDrawables(null, null, null, null);
        }
        //load cover image
        String folderPath = AppURL.baseUrlSongAndLyric + "/" + song.getLyric().substring(0, song.getLyric().length() - 4);
        Glide.with(this.context).load( folderPath + "/" + song.getImage() ).into(holder.ivCoverSong);

        //listener
        holder.btnSing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onlccc", "1");
                Intent intent = new Intent(context, SongDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String songJson = new Gson().toJson(song);
                intent.putExtra("song", songJson);
                context.startActivity(intent);
            }
        });
        holder.btnNotLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                karaokeSongList.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();
                Call<Integer> callNotLike  = service.sendNotLike(song.getId());
                callNotLike.enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        Log.d("", response.toString());
                    }

                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        Log.d("dd", t.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return karaokeSongList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCoverSong;
        TextView tvSongName,tvSinger,tvNumViews, btnNotLike;
        Button btnSing;
        public MyViewHolder(View itemView) {
            super(itemView);
            ivCoverSong = itemView.findViewById(R.id.ivCoverSong);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSinger = itemView.findViewById(R.id.tvSinger);
            tvNumViews = itemView.findViewById(R.id.tvNumViews);
            btnNotLike = itemView.findViewById(R.id.btnNotLike);
            btnSing = itemView.findViewById(R.id.btnSing);
        }
    }
}
