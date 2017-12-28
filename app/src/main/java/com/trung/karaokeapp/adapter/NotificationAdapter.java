package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.entities.ToAnnUser;

import java.util.List;

/**
 * Created by avc on 12/27/2017.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {
    private Context context;
    private List<ToAnnUser> toAnnUserList;

    public NotificationAdapter(Context context, List<ToAnnUser> toAnnUserList) {
        this.context = context;
        this.toAnnUserList = toAnnUserList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_notification, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ToAnnUser toAnnUser = toAnnUserList.get(position);
        holder.tvAnnContent.setText( toAnnUser.getAnnouncement().getContent() );
        holder.tvCreateAt.setText( toAnnUser.getCreated_at() );
    }

    @Override
    public int getItemCount() {
        return toAnnUserList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAnnLogo;
        TextView tvAnnContent, tvCreateAt;
        public MyViewHolder(View itemView) {
            super(itemView);
            ivAnnLogo = itemView.findViewById(R.id.ivAnnLogo);
            tvAnnContent = itemView.findViewById(R.id.tvAnnContent);
            tvCreateAt = itemView.findViewById(R.id.tvCreateAt);
        }
    }
}
