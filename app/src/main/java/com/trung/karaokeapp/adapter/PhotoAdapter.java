package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.entities.PhotoTb;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.viewmodel.PhotoViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by avc on 12/27/2017.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyViewHolder> {
    private Context context;
    private List<PhotoTb> photoTbList;
    private Menu mOptionMenu;
    private PhotoViewModel photoViewModel;
    private ApiService service;

    public PhotoAdapter(Context context, List<PhotoTb> photoTbList, Menu mOptionMenu, PhotoViewModel photoViewModel, ApiService service) {
        this.context = context;
        this.photoTbList = photoTbList;
        this.mOptionMenu = mOptionMenu;
        this.photoViewModel = photoViewModel;
        this.service = service;
    }

    public List<PhotoTb> getPhotoTbList() {
        return photoTbList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_photo, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final PhotoTb photoTb = photoTbList.get(position);
        Glide.with(context).load(AppURL.baseUrlPhotos + "/" + photoTb.getPath()).into(holder.ivPhoto);

        //listener
        holder.btnCheckToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.btnCheckToggle.setVisibility(View.INVISIBLE);
                photoViewModel.setPosSelect(-1);
                mOptionMenu.getItem(0).setVisible(false);
                mOptionMenu.getItem(1).setVisible(false);
            }
        });
        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (photoViewModel.getPosSelect() == -1){
                    holder.btnCheckToggle.setVisibility(View.VISIBLE);
                    photoViewModel.setPosSelect(position);
                    mOptionMenu.getItem(0).setVisible(true);
                    mOptionMenu.getItem(1).setVisible(true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoTbList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageButton btnCheckToggle;
        public MyViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnCheckToggle = itemView.findViewById(R.id.btnCheckToggle);
        }
    }
}
