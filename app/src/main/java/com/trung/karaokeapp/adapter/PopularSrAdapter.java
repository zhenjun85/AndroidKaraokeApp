package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.Utils;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.network.AppURL;

import java.util.List;

/**
 * Created by avc on 12/23/2017.
 */

public class PopularSrAdapter extends RecyclerView.Adapter<PopularSrAdapter.MyViewHolder> {
    private Context context;
    private List<SharedRecord> listSr;

    public PopularSrAdapter(Context context, List<SharedRecord> listSr) {
        this.context = context;
        this.listSr = listSr;
    }

    @Override
    public PopularSrAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_shared_song_profile, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PopularSrAdapter.MyViewHolder holder, int position) {
        SharedRecord sr = listSr.get(position);
        holder.tvSongName.setText(sr.getKaraoke().getName());
        holder.tvNumView.setText(sr.getViewNo() + (sr.getViewNo() > 1 ? " views" : " view"));
        holder.tvAuthor.setText(sr.getUser().getName());
        holder.tvPostContent.setText(sr.getContent());
        holder.tvSharedAt.setText(Utils.recentTime(sr.getShared_at()));

        String folderPath = sr.getKaraoke().getBeat().substring(0, sr.getKaraoke().getBeat().length() - 4);
        Glide.with(context).load( AppURL.baseUrlSongAndLyric + "/" + folderPath + "/" + sr.getKaraoke().getImage() ).into(holder.ivCoverSong);
    }

    @Override
    public int getItemCount() {
        return listSr.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvSongName;
        TextView tvNumView;
        TextView tvAuthor;
        TextView tvPostContent;
        ImageView ivCoverSong;
        TextView tvSharedAt;

        public MyViewHolder(View view) {
            super(view);
            tvSongName = view.findViewById(R.id.tvSongName);
            tvNumView = view.findViewById(R.id.tv_num_views);
            tvAuthor = view.findViewById(R.id.tv_post_author);
            tvPostContent = view.findViewById(R.id.tv_post_content);
            ivCoverSong = view.findViewById(R.id.iv_song_cover);
            tvSharedAt = view.findViewById(R.id.tv_shared_at);
        }
    }
}
