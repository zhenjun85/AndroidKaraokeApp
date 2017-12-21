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
import com.trung.karaokeapp.Utils;
import com.trung.karaokeapp.activity.SongDetailActivity;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.network.AppURL;

import java.util.List;

/**
 * Created by avc on 12/14/2017.
 */

public class AllSongsAdapter extends RecyclerView.Adapter<AllSongsAdapter.MyViewHolder> {
    public List<KaraokeSong> getSongLists() {
        return songLists;
    }

    private final List<KaraokeSong> songLists;
    private final Context context;

    public AllSongsAdapter(List<KaraokeSong> songLists, Context context) {
        this.songLists = songLists;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_allsongs, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final KaraokeSong song = songLists.get(position);
        holder.tvSongName.setText(song.getName());
        holder.tvPlayNo.setText(song.getViewNo() + ((song.getViewNo() < 2) ? " view" : " views"));
        holder.tvSinger.setText(song.getArtist());


        if (Utils.dayBetweenPastAndNow(song.getCreatedAt()) <= 7) {
            Drawable leftDrawable = context.getResources().getDrawable(R.drawable.ic_stars_black_24dp);
            holder.tvSinger.setCompoundDrawablesRelativeWithIntrinsicBounds(leftDrawable, null, null, null);
        }
        else {
            holder.tvSinger.setCompoundDrawables(null, null, null, null);
        }
        //load cover image
        String folderPath = AppURL.baseUrlSongAndLyric + "/" + song.getLyric().substring(0, song.getLyric().length() - 4);
        Glide.with(this.context).load( folderPath + "/" + song.getImage() ).into(holder.ivCover);

        //listener
        holder.btnSing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onlccc", "1");
                Intent intent = new Intent(context, SongDetailActivity.class);
                String songJson = new Gson().toJson(song);
                intent.putExtra("song", songJson);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCover;
        private TextView tvSongName, tvSinger, tvPlayNo;
        private Button btnSing;
        public MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCoverSong);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSinger = itemView.findViewById(R.id.tvSinger);
            tvPlayNo = itemView.findViewById(R.id.tv_playno);
            btnSing = itemView.findViewById(R.id.btn_sing);
        }
    }
}
