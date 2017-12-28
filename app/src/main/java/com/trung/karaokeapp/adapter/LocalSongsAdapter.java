package com.trung.karaokeapp.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.activity.PostRecordActivity;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.entities.LocalRecord;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.utils.AppBaseCode;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by avc on 12/21/2017.
 */

public class LocalSongsAdapter extends RecyclerView.Adapter<LocalSongsAdapter.MyViewHolder> {
    private final ApiService service;

    private List<LocalRecord> listLocalRecord;
    private Context context;

    public LocalSongsAdapter(List<LocalRecord> listLocalRecord, Context context, ApiService service) {
        this.listLocalRecord = listLocalRecord;
        this.context = context;
        this.service = service;
    }

    public List<LocalRecord> getListLocalRecord() {
        return listLocalRecord;
    }

    @Override
    public LocalSongsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_local_songs, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LocalSongsAdapter.MyViewHolder holder, final int position) {

        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        decimalFormat.setMaximumFractionDigits(2);

        final LocalRecord record = listLocalRecord.get(position);
        holder.tvSongName.setText(record.getName());
        holder.tvSizeOfFile.setText(decimalFormat.format(Float.parseFloat(record.getSize())) + "Kb");
        holder.tvDuration.setText(record.getDuration());
        holder.tvScore.setText(record.getScore() + "");
        holder.tvType.setText(record.getType());

        holder.btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<KaraokeSong> callKaraokeSong = service.getOne(record.getKsid());
                callKaraokeSong.enqueue(new Callback<KaraokeSong>() {
                    @Override
                    public void onResponse(Call<KaraokeSong> call, Response<KaraokeSong> response) {
                        Log.d("LocalsongAdapter", response.toString());

                        Intent intent = new Intent(context.getApplicationContext(), PostRecordActivity.class);
                        intent.putExtra("song", new Gson().toJson(response.body()));
                        intent.putExtra("record", record.getPath() );
                        intent.putExtra("score", record.getScore());
                        intent.putExtra("isVideo", record.getType().equals("video"));
                        intent.putExtra("position", position);
                        ((Activity) context).startActivityForResult(intent, AppBaseCode.POST_REQUEST_CODE);
                    }

                    @Override
                    public void onFailure(Call<KaraokeSong> call, Throwable t) {
                        Log.d("fail", t.getMessage());
                    }
                });
            }
        });
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder =   new AlertDialog.Builder(context).setMessage("Do you want delete this record?")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                File delFile = new File(record.getPath());
                                if (delFile.exists()) {
                                    if (!delFile.delete()){
                                        Log.e("LocalSongsActivity", "delete record error");
                                    }
                                }
                                listLocalRecord.remove(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                final AlertDialog dialog =  builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        dialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                        dialog.getButton(Dialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listLocalRecord.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSongName,tvType,tvScore,tvDuration,tvSizeOfFile;
        private final TextView btnPost;
        private final ImageButton btnRemove;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvSizeOfFile = itemView.findViewById(R.id.tvSizeOfFile);
            tvScore = itemView.findViewById(R.id.tvScore);
            btnPost = itemView.findViewById(R.id.btnPost);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
}
