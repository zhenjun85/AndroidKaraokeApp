package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.Utils;
import com.trung.karaokeapp.entities.RecordUserKs;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.viewmodel.RecentViewModel;

import java.util.List;

/**
 * Created by avc on 12/20/2017.
 */

public class RecentSongsAdapter extends RecyclerView.Adapter<RecentSongsAdapter.MyViewHolder> {

    private List<RecordUserKs> recentList;
    private final Context context;
    private RecentViewModel recentViewModel;

    public RecentSongsAdapter(List<RecordUserKs> songLists, Context context, RecentViewModel recentViewModel) {
        this.recentList = songLists;
        this.context = context;
        this.recentViewModel = recentViewModel;
    }

    @Override
    public RecentSongsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_recent_songs, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecentSongsAdapter.MyViewHolder holder, int position) {
        final RecordUserKs recordUserKs = recentList.get(position);
        holder.tvSongName.setText(recordUserKs.getSong().getName());
        holder.tvSinger.setText(recordUserKs.getSong().getArtist());

        holder.tvTimeAgo.setText(Utils.recentTime( recordUserKs.getUpdated_at() ));

        if (Utils.dayBetweenPastAndNow(recordUserKs.getSong().getCreatedAt()) <= 7) {
            Drawable leftDrawable = context.getResources().getDrawable(R.drawable.ic_stars_black_24dp);
            holder.tvSinger.setCompoundDrawablesRelativeWithIntrinsicBounds(leftDrawable, null, null, null);
        }
        else {
            holder.tvSinger.setCompoundDrawables(null, null, null, null);
        }
        //load cover image
        String folderPath = AppURL.baseUrlSongAndLyric + "/" + recordUserKs.getSong().getLyric().substring(0, recordUserKs.getSong().getLyric().length() - 4);
        Glide.with(this.context).load( folderPath + "/" + recordUserKs.getSong().getImage() ).into(holder.ivCoverSong);

        //listener clItemRecent
        holder.clItemRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<RecordUserKs> list = recentViewModel.getListSongsToDetele().getValue();
                if (!holder.cbSelect.isChecked()) {
                    list.add(recordUserKs);
                }else {
                    int i = 0;
                    for (; i < list.size(); i++) {
                        if(list.get(i).getKar_id() == recordUserKs.getKar_id()){
                            break;
                        }
                    }
                    list.remove(i);
                }
                recentViewModel.getListSongsToDetele().postValue(list);
                holder.cbSelect.setChecked(!holder.cbSelect.isChecked());
            }
        });
    }

    public List<RecordUserKs> getRecentList() {
        return recentList;
    }

    @Override
    public int getItemCount() {
        return recentList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCoverSong;
        private final TextView tvSongName;
        private final TextView tvSinger;
        private final TextView tvTimeAgo;
        private final CheckBox cbSelect;
        private final ConstraintLayout clItemRecent;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivCoverSong = itemView.findViewById(R.id.ivCoverSong);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSinger = itemView.findViewById(R.id.tvSinger);
            tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            clItemRecent = itemView.findViewById(R.id.clItemRecent);
        }
    }
}
