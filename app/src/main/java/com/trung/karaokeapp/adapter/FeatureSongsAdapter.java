package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.activity.SongDetailActivity;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.network.AppURL;

import java.util.List;

/**
 * Created by avc on 12/20/2017.
 */

public class FeatureSongsAdapter extends RecyclerView.Adapter<FeatureSongsAdapter.MyViewHolder>{
    private final List<KaraokeSong> songLists;
    private final Context context;

    public FeatureSongsAdapter(List<KaraokeSong> songLists, Context context) {
        this.songLists = songLists;
        this.context = context;
    }

    @Override
    public FeatureSongsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_feature_songs, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FeatureSongsAdapter.MyViewHolder holder, int position) {
        final KaraokeSong song = songLists.get(position);
        holder.tvSongName.setText( song.getName() );
        holder.tvSinger.setText( song.getArtist() );
        holder.tvNumViews.setText( song.getViewNo() + (song.getViewNo() <= 1 ? " view" : " views"));

        //set onclick
        holder.btnSing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SongDetailActivity.class);
                intent.putExtra("song", new Gson().toJson(song));
                context.startActivity(intent);
            }
        });

        //load cover image
        String folderPath = AppURL.baseUrlSongAndLyric + "/" + song.getLyric().substring(0, song.getLyric().length() - 4);
        Glide.with(this.context).load( folderPath + "/" + song.getImage() ).into(holder.ivCoverSong);
    }

    @Override
    public int getItemCount() {
        return songLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSongName;
        private final TextView tvSinger;
        private final ImageView ivCoverSong;
        private final TextView tvNumViews;
        private final Button btnSing;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSinger = itemView.findViewById(R.id.tvSinger);
            tvNumViews = itemView.findViewById(R.id.tvNumViews);
            ivCoverSong = itemView.findViewById(R.id.ivCoverSong);
            btnSing = itemView.findViewById(R.id.btnSing);
        }
    }

}
