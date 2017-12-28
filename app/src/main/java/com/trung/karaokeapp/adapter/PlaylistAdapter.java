package com.trung.karaokeapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.activity.PlaylistDetailActivity;
import com.trung.karaokeapp.entities.Playlist;
import com.trung.karaokeapp.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by avc on 12/25/2017.
 */

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder> {
    private Context context;
    private List<Playlist> playlists;
    private ApiService service;

    public PlaylistAdapter(Context context, List<Playlist> playlists, ApiService service) {
        this.context = context;
        this.playlists = playlists;
        this.service = service;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    @Override
    public PlaylistAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_playlist, parent, false));
    }

    @Override
    public void onBindViewHolder(final PlaylistAdapter.MyViewHolder holder, final int position) {
        final Playlist playlist = playlists.get(position);
        holder.tvPlaylistName.setText(playlist.getName());
        holder.tvNumSongs.setText(playlist.getNumSongs() + "");


        holder.btnMorePl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, holder.btnMorePl);
                popupMenu.getMenuInflater().inflate(R.menu.menu_edit_playlist, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.mn_change_name_pl:
                                editNamePlaylist(playlist, position);
                                break;
                            case R.id.mn_delete_pl:
                                delPlaylist(playlist, position);
                                break;
                            default:break;
                        }
                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        holder.itemPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlaylistDetailActivity.class);
                intent.putExtra("playlist", new Gson().toJson(playlist));
                context.startActivity( intent );
            }
        });
    }

    private void delPlaylist(Playlist playlist, final int position) {
        Call<Integer> delPlaylist = service.deletePlaylist(playlist.getId());
        delPlaylist.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                Log.d("PlaylistAdapter", response.toString());
                if (response.body() == 1){
                    playlists.remove(position);
                    notifyItemRemoved(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Delete playlist success!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, "Delete playlist fail!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.d("PlaylistAdapter", "faill" + t.getMessage());
            }
        });

    }

    private void editNamePlaylist(final Playlist playlist, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_playlist, null, false);
        final TextInputLayout tilNamePlaylist = view.findViewById(R.id.tilNamePlaylist);
        builder.setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Editable editText = tilNamePlaylist.getEditText().getText();
                        if (editText != null && !editText.toString().equals("")) {
                            Call<Integer> callSavePl = service.savePlaylist(editText.toString(), playlist.getId());
                            callSavePl.enqueue(new Callback<Integer>() {
                                @Override
                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                    Log.d("PlaylistAdapter", response.toString());
                                    if ( response.body() == 1) {
                                        Toast.makeText(context, "Saving playlist success!", Toast.LENGTH_SHORT).show();
                                        playlist.setName( editText.toString() );
                                        notifyItemChanged(position);
                                        notifyDataSetChanged();
                                    }else {
                                        Toast.makeText(context, "Saving playlist fail!", Toast.LENGTH_LONG).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<Integer> call, Throwable t) {
                                    Log.d("PlaylistAdapter", "failure" + t.getMessage());
                                }
                            });
                        }
                    }
                });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                //alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCoverPlaylist;
        TextView tvPlaylistName;
        TextView tvNumSongs;
        ImageButton btnMorePl;
        ConstraintLayout itemPlaylist;
        public MyViewHolder(View itemView) {
            super(itemView);
            ivCoverPlaylist = itemView.findViewById(R.id.tvIndex);
            tvPlaylistName = itemView.findViewById(R.id.tvPlaylistName);
            tvNumSongs = itemView.findViewById(R.id.tvSinger);
            btnMorePl = itemView.findViewById(R.id.btnMorePl);
            itemPlaylist = itemView.findViewById(R.id.itemPlaylist);
        }
    }
}
