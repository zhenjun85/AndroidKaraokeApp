package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.entities.CommentTb;
import com.trung.karaokeapp.network.AppURL;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by avc on 12/26/2017.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
    private Context context;
    private List<CommentTb> commentTbList;

    public List<CommentTb> getCommentTbList() {
        return commentTbList;
    }

    public CommentAdapter(Context context, List<CommentTb> commentTbList) {
        this.context = context;
        this.commentTbList = commentTbList;
    }

    @Override
    public CommentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_comment, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentAdapter.MyViewHolder holder, int position) {
        CommentTb commentTb = commentTbList.get(position);

        holder.tvUserName.setText(commentTb.getUser().getName());
        holder.tvCommentContent.setText(commentTb.getContent());
        holder.tvCommentTime.setText(commentTb.getCreatedAt());

        if (commentTb.getUser().getAvatar() != null) {
            Glide.with(context).load(AppURL.baseUrlPhotos + "/" + commentTb.getUser().getAvatar()).into(holder.ivUserAvatar);
        }else {
            holder.ivUserAvatar.setImageDrawable(context.getDrawable(R.drawable.ic_face_black_48px));
        }
    }

    @Override
    public int getItemCount() {
        return commentTbList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivUserAvatar;
        TextView tvUserName, tvCommentContent, tvCommentTime;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
        }
    }
}
