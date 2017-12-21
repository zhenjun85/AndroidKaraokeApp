package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.activity.PostRecordActivity;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.entities.LocalRecord;
import com.trung.karaokeapp.network.ApiService;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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

    @Override
    public LocalSongsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_local_songs, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LocalSongsAdapter.MyViewHolder holder, int position) {

        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        decimalFormat.setMaximumFractionDigits(2);

        final LocalRecord record = listLocalRecord.get(position);
        holder.tvSongName.setText(record.getName());
        holder.tvSizeOfFile.setText(decimalFormat.format(Float.parseFloat(record.getSize())) + "Kb");
        holder.tvDuration.setText(record.getDuration());
        holder.tvScore.setText(record.getScore() + "");

        holder.btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<KaraokeSong> callOne = service.getOne(record.getKsid());
                callOne.enqueue(new Callback<KaraokeSong>() {
                    @Override
                    public void onResponse(Call<KaraokeSong> call, Response<KaraokeSong> response) {
                        Log.d("LocalsongAdapter", record.toString());
                        Intent intent = new Intent(context.getApplicationContext(), PostRecordActivity.class);
                        intent.putExtra("song", new Gson().toJson(response.body()));
                        intent.putExtra("record", record.getPath());
                        intent.putExtra("score", record.getScore() + "");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<KaraokeSong> call, Throwable t) {
                        Log.d("fail", t.getMessage());
                    }
                });



            }
        });
    }

    @Override
    public int getItemCount() {
        return listLocalRecord.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSongName;
        private final TextView tvDuration;
        private final TextView tvSizeOfFile;
        private final TextView tvScore;
        private final TextView btnPost;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvSizeOfFile = itemView.findViewById(R.id.tvSizeOfFile);
            tvScore = itemView.findViewById(R.id.tvScore);
            btnPost = itemView.findViewById(R.id.btnPost);
        }
    }
}
