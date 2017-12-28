package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.activity.SrDetailActivity;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.utils.Utils;

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
        final SharedRecord sr = listSr.get(position);
        holder.tvSongName.setText(sr.getKaraoke().getName());
        holder.tvNumView.setText(sr.getViewNo() + (sr.getViewNo() > 1 ? " views" : " view"));
        holder.tvAuthor.setText(sr.getUser().getName());
        holder.tvPostContent.setText(sr.getContent());
        holder.tvSharedAt.setText(Utils.recentTime(sr.getShared_at()));
        holder.tvSrType.setText(sr.getType());

        String folderPath = sr.getKaraoke().getBeat().substring(0, sr.getKaraoke().getBeat().length() - 4);
        Glide.with(context).load( AppURL.baseUrlSongAndLyric + "/" + folderPath + "/" + sr.getKaraoke().getImage() ).into(holder.ivCoverSong);

        holder.clSrItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SrDetailActivity.class);
                intent.putExtra("sharedrecord", new Gson().toJson(sr));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listSr.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvSongName,tvNumView,tvAuthor,tvPostContent,tvSharedAt,tvSrType;
        ImageView ivCoverSong;
        ConstraintLayout clSrItem;

        public MyViewHolder(View view) {
            super(view);
            tvSongName = view.findViewById(R.id.tvSongName);
            tvNumView = view.findViewById(R.id.tv_num_views);
            tvAuthor = view.findViewById(R.id.tv_post_author);
            tvPostContent = view.findViewById(R.id.tv_post_content);
            ivCoverSong = view.findViewById(R.id.iv_song_cover);
            tvSharedAt = view.findViewById(R.id.tv_shared_at);
            clSrItem = view.findViewById(R.id.clSrItem);
            tvSrType = view.findViewById(R.id.tvSrType);
        }
    }
}
