package com.trung.karaokeapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.activity.SrDetailActivity;
import com.trung.karaokeapp.entities.CommentTb;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by avc on 12/26/2017.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.MyViewHoder> {
    private Context context;
    private List<SharedRecord> recordList;
    private ApiService service;

    public FeedAdapter(Context context, List<SharedRecord> recordList, ApiService service) {
        this.context = context;
        this.recordList = recordList;
        this.service = service;
    }

    @Override
    public MyViewHoder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHoder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_newfeed, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHoder holder, int position) {
        final SharedRecord record = recordList.get(position);
        holder.tvUserName.setText( record.getUser().getName() );
        holder.tvTimePost.setText( record.getShared_at() );
        holder.tvPostContent.setText( record.getContent() );
        holder.tvSongName.setText( record.getKaraoke().getName() );
        holder.tvNumViews.setText( record.getViewNo() + ( record.getViewNo() < 2 ? " view" : " views" ) );
        holder.tvNumLikes.setText( record.getNumLikes() + "" );
        holder.tvNumComments.setText( record.getNumComments() + "" );
        holder.tvRecordType.setText( record.getType() );

        //load ivUserAvatar
        if (record.getUser().getAvatar() != null) {
            Glide.with(context).load(AppURL.baseUrlPhotos + "/" + record.getUser().getAvatar()).into(holder.ivUserAvatar);
        }else {
            holder.ivUserAvatar.setImageDrawable(context.getDrawable(R.drawable.ic_face_black_48px));
        }

        //load cover image
        String folderPath = AppURL.baseUrlSongAndLyric + "/" + record.getKaraoke().getBeat().substring(0, record.getKaraoke().getLyric().length() - 4);
        Glide.with(this.context).load( folderPath + "/" + record.getKaraoke().getImage() ).into(holder.ivCoverSong);

        final Call<Integer> callIsLike = service.getIsLike(record.getId());
        callIsLike.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                Log.d("feedAdatper", record.toString());
                if (response.body() == 1) {
                    holder.btnLikeToggle.setImageDrawable( context.getDrawable(R.drawable.ic_liked) );
                }else {
                    holder.btnLikeToggle.setImageDrawable( context.getDrawable(R.drawable.ic_like) );
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.d("feedAdapter", t.getMessage());
            }
        });

        //listener
        holder.btnLikeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<Integer> callLikeRecord = service.likeRecord(record.getId());
                callLikeRecord.enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        Log.d("feedAdapter", record.toString());
                        if (response.body() == 1) {
                            holder.btnLikeToggle.setImageDrawable( context.getDrawable(R.drawable.ic_liked) );
                            holder.tvNumLikes.setText( (Integer.parseInt(holder.tvNumLikes.getText().toString()) + 1) + "" );
                        }else {
                            holder.btnLikeToggle.setImageDrawable( context.getDrawable(R.drawable.ic_like) );
                            holder.tvNumLikes.setText( (Integer.parseInt(holder.tvNumLikes.getText().toString()) - 1) + "" );
                        }
                    }

                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        Log.d("feedAdapter", t.getMessage());
                    }
                });
            }
        });

        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View viewComment = LayoutInflater.from(context).inflate(R.layout.dialog_add_comment, null, false);
                final TextInputLayout tilComment = viewComment.findViewById(R.id.tilComment);
                builder.setView(viewComment)
                .setPositiveButton("Comment", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (tilComment.getEditText().getText().toString().length() != 0) {
                            Call<CommentTb> callComment = service.commentRecord(record.getId(), tilComment.getEditText().getText().toString());
                            callComment.enqueue(new Callback<CommentTb>() {
                                @Override
                                public void onResponse(Call<CommentTb> call, Response<CommentTb> response) {
                                    Log.d("feedAdapter", record.toString());
                                    if (response.body() != null) {
                                        Toast.makeText(context, "Comment has posted!", Toast.LENGTH_SHORT).show();
                                        holder.tvNumComments.setText( (Integer.parseInt(holder.tvNumComments.getText().toString()) + 1) + "" );

                                    }else {
                                        Toast.makeText(context, "Comment fail!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<CommentTb> call, Throwable t) {
                                    Log.d("feedAdapter", t.getMessage());
                                }
                            });
                        }
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        alertDialog.getButton( DialogInterface.BUTTON_POSITIVE ).setTextColor(Color.BLACK);
                    }
                });
                alertDialog.show();
            }
        });

        holder.btnOpenSharedRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SrDetailActivity.class);
                intent.putExtra("sharedrecord", new Gson().toJson(record));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public class MyViewHoder extends RecyclerView.ViewHolder {
        CircleImageView ivUserAvatar;
        TextView tvUserName, tvTimePost, tvPostContent;
        ImageView ivCoverSong;
        TextView tvSongName, tvNumViews;
        ImageButton btnLikeToggle, btnComment, btnReportSharedRecord;
        TextView tvNumLikes, tvNumComments, tvRecordType;
        ConstraintLayout btnOpenSharedRecord;
        public MyViewHoder(View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTimePost = itemView.findViewById(R.id.tvTimePost);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
            ivCoverSong = itemView.findViewById(R.id.ivCoverSong);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvNumViews = itemView.findViewById(R.id.tvNumViews);
            btnLikeToggle = itemView.findViewById(R.id.btnLikeToggle);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnReportSharedRecord = itemView.findViewById(R.id.btnReportSharedRecord);
            tvNumLikes = itemView.findViewById(R.id.tvNumLikes);
            tvNumComments = itemView.findViewById(R.id.tvNumComments);
            btnOpenSharedRecord = itemView.findViewById(R.id.btnOpenSharedRecord);
            tvRecordType = itemView.findViewById(R.id.tvRecordType);
        }
    }
}
