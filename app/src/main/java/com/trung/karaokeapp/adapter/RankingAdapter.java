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
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.network.AppURL;

import java.util.List;

/**
 * Created by avc on 12/21/2017.
 */

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.MyViewHolder> {
    private Context context;
    private List<SharedRecord> listSrRanking;

    public RankingAdapter(Context context, List<SharedRecord> listSrRanking) {
        this.context = context;
        this.listSrRanking = listSrRanking;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_ranking, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SharedRecord sharedRecord = listSrRanking.get(position);
        holder.tvUserName.setText(sharedRecord.getUser().getName());
        holder.tvNumView.setText(sharedRecord.getViewNo() + (sharedRecord.getViewNo() < 2 ? " view" : " views"));
        holder.tvIndexUser.setText((position + 1) + "");

        if (sharedRecord.getUser().getAvatar() == null || sharedRecord.getUser().getAvatar() == "") {
            Glide.with(context).load(AppURL.baseUrl + "/store/avatar.png").into(holder.ivAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return listSrRanking.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIndexUser;
        private final TextView tvUserName;
        private final TextView tvNumView;
        private final ImageView ivAvatar;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvIndexUser = itemView.findViewById(R.id.tvIndexUser);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvNumView = itemView.findViewById(R.id.tv_numview_srsong);
            ivAvatar = itemView.findViewById(R.id.ivUserName);
        }
    }
}
