package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class NewSongsAdapter extends RecyclerView.Adapter<NewSongsAdapter.MyViewHolder> {
    private final List<KaraokeSong> songLists;
    private final Context context;

    public NewSongsAdapter(List<KaraokeSong> songLists, Context context) {
        this.songLists = songLists;
        this.context = context;
    }

    @Override
    public NewSongsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_newsong, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewSongsAdapter.MyViewHolder holder, int position) {
        final KaraokeSong song = songLists.get(position);
        holder.tvSongName.setText( song.getName() );
        holder.tvSinger.setText( song.getArtist() );

        //set onclick
        holder.wrapSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SongDetailActivity.class);
                intent.putExtra("song", new Gson().toJson(song));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        private final LinearLayout wrapSongs;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSinger = itemView.findViewById(R.id.tvSinger);
            ivCoverSong = itemView.findViewById(R.id.ivCoverSong);
            wrapSongs = itemView.findViewById(R.id.llWrapNewSongs);
        }
    }
}
