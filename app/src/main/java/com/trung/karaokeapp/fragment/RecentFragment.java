package com.trung.karaokeapp.fragment;


import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.TokenManager;
import com.trung.karaokeapp.adapter.RecentSongsAdapter;
import com.trung.karaokeapp.entities.RecordUserKs;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.viewmodel.RecentViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecentFragment extends Fragment {
    private static final String TAG = "RecentFragment";
    @BindView(R.id.rvRecentSongs) RecyclerView rvRecentSongs;
    @BindView(R.id.tvStatusSelected) TextView tvStatusSelected;

    TokenManager tokenManager;
    ApiService service;
    Call<List<RecordUserKs>> call;
    RecentSongsAdapter recentSongsAdapter;
    RecentViewModel recentViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_book_recent, container, false);;
        ButterKnife.bind(this, view);
        tokenManager = TokenManager.getInstance(getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        recentViewModel = ViewModelProviders.of(RecentFragment.this).get(RecentViewModel.class);

        getRecentSongs();

        //observer listSongsDelete change
        recentViewModel.getListSongsToDetele().observe(this, new Observer<List<RecordUserKs>>() {
            @Override
            public void onChanged(@Nullable List<RecordUserKs> listRecordUserKs) {
                tvStatusSelected.setText( listRecordUserKs.size() + (listRecordUserKs.size() < 2 ? " item" : " items") + " selected" );
            }
        });

        return view;
    }

    private void getRecentSongs() {
        call = service.getRecentSongs();
        call.enqueue(new Callback<List<RecordUserKs>>() {
            @Override
            public void onResponse(Call<List<RecordUserKs>> call, Response<List<RecordUserKs>> response) {
                Log.d(TAG, response.toString());
                recentSongsAdapter = new RecentSongsAdapter(response.body(), getContext(), recentViewModel);
                rvRecentSongs.setLayoutManager(new LinearLayoutManager(getContext()));
                rvRecentSongs.setAdapter(recentSongsAdapter);
            }

            @Override
            public void onFailure(Call<List<RecordUserKs>> call, Throwable t) {
                Log.e(TAG, "failure" + t.getMessage());
            }
        });
    }

    @OnClick(R.id.tvStatusSelected)
    void clearSelected() {
        tvStatusSelected.setText( "0 item selected" );
        recentViewModel.getListSongsToDetele().getValue().clear();
        rvRecentSongs.setAdapter(recentSongsAdapter);
    }

    @OnClick(R.id.tvDeleteRecentSongs)
    void deleteSelected() {
        final List<RecordUserKs> listDeleting = recentViewModel.getListSongsToDetele().getValue();
        if (listDeleting.size() == 0) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to delete?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "yes");

                        for (final RecordUserKs item : listDeleting) {
                            Call<Integer> call = service.removeRecentSong(item.getKar_id());
                            call.enqueue(new Callback<Integer>() {
                                @Override
                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                    Log.d(TAG, response.toString());

                                    List<RecordUserKs> list = recentSongsAdapter.getRecentList();
                                    int i = 0;
                                    for (; i < list.size(); i++){
                                        if (list.get(i).getKar_id() == item.getKar_id()){
                                            break;
                                        }
                                    }
                                    list.remove(i);
                                    rvRecentSongs.setAdapter(recentSongsAdapter);
                                }

                                @Override
                                public void onFailure(Call<Integer> call, Throwable t) {
                                    Log.d(TAG, "failure: " + t.getMessage());
                                }
                            });
                        }
                        listDeleting.clear();
                        tvStatusSelected.setText("0 item selected");
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
        ;
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null){
            call.cancel();
            call = null;
        }
    }
}
