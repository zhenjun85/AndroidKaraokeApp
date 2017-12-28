package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.entities.RelationTb;
import com.trung.karaokeapp.entities.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by avc on 12/28/2017.
 */

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.MyViewHolder> {
    private Context context;
    private List<RelationTb> relationTbList;

    public FriendAdapter(Context context, List<RelationTb> relationTbList) {
        this.context = context;
        this.relationTbList = relationTbList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_friend, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RelationTb  relationTb = relationTbList.get(position);
        holder.tvUserName.setText( relationTb.getOther().getName() );
        holder.tvBirthday.setText( relationTb.getOther().getBirthday() );

    }

    @Override
    public int getItemCount() {
        return relationTbList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivUserAvatar;
        TextView tvUserName, tvBirthday;
        ImageButton btnFriendAction;
        public MyViewHolder(View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvBirthday = itemView.findViewById(R.id.tvBirthday);
            btnFriendAction = itemView.findViewById(R.id.btnFriendAction);
        }
    }
}
