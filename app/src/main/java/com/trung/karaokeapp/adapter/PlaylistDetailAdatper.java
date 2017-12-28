package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.entities.HasPlaylistKs;
import com.trung.karaokeapp.entities.Playlist;
import com.trung.karaokeapp.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by avc on 12/25/2017.
 */

public class PlaylistDetailAdatper extends RecyclerView.Adapter<PlaylistDetailAdatper.MyViewHolder> {
    private Context context;
    private List<HasPlaylistKs> hasPlaylistKsList;
    private ApiService service;
    private TextView tvNumSongs;
    private Playlist playlist;

    public PlaylistDetailAdatper(Context context, List<HasPlaylistKs> hasPlaylistKsList, ApiService service, TextView tvNumSongs, Playlist playlist) {
        this.context = context;
        this.hasPlaylistKsList = hasPlaylistKsList;
        this.service = service;
        this.tvNumSongs = tvNumSongs;
        this.playlist = playlist;
    }

    @Override
    public PlaylistDetailAdatper.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_songs_in_playlist, parent, false));
    }

    @Override
    public void onBindViewHolder(final PlaylistDetailAdatper.MyViewHolder holder, final int position) {
        final HasPlaylistKs hasPlaylistKs = hasPlaylistKsList.get(position);
        holder.tvIndex.setText((position + 1) + "");
        holder.tvSinger.setText(hasPlaylistKs.getKaraokeSong().getArtist());
        holder.tvSongName.setText(hasPlaylistKs.getKaraokeSong().getName());

        holder.btnMorePl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.getMenuInflater().inflate( R.menu.menu_item_playlist_detail, popupMenu.getMenu() );

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.mn_delete_song_in_pl:
                                deleteSongInPlaylist(hasPlaylistKs, position);
                                break;
                            default:break;
                        }
                        return true;
                    }
                });

                popupMenu.show();
            }
        });
    }

    private void deleteSongInPlaylist(HasPlaylistKs hasPlaylistKs, final int position) {
        Call<Integer> callDel = service.delSongInPlaylist(hasPlaylistKs.getPlaylistId(), hasPlaylistKs.getKaraokeSongId());
        callDel.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                Log.d("pldadapter", response.toString());
                if (response.body() == 1) {
                    hasPlaylistKsList.remove(position);
                    notifyItemRemoved(position);
                    notifyDataSetChanged();

                    playlist.setNumSongs( playlist.getNumSongs() - 1 );
                    tvNumSongs.setText( playlist.getNumSongs() + (playlist.getNumSongs() < 2 ? " song" : " songs") );

                    Toast.makeText(context, "Delete success!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, "Fail delete!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.d("pldtadatper", t.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return hasPlaylistKsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex, tvSongName, tvSinger;
        ImageButton btnMorePl;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvIndex);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSinger = itemView.findViewById(R.id.tvSinger);
            btnMorePl = itemView.findViewById(R.id.btnMorePl);
        }
    }
}
