package com.trung.karaokeapp.fragment;


import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.TokenManager;
import com.trung.karaokeapp.adapter.AllSongsAdapter;
import com.trung.karaokeapp.entities.Genre;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.viewmodel.AllSongViewModel;

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
public class AllSongsFragment extends Fragment {
    public AllSongsFragment() {
        // Required empty public constructor
    }
    private static final String TAG = "AllSongsFragment";
    TokenManager tokenManager;
    ApiService service;
    Call<List<KaraokeSong>> callAllSongs;
    Call<List<Genre>> callGenre;
    private AllSongsAdapter adapter;
    AllSongViewModel allSongViewModel;

    @BindView(R.id.recyclerView)  RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song_book_all_songs, container, false);
        ButterKnife.bind(this, view);
        tokenManager = TokenManager.getInstance(getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        allSongViewModel = ViewModelProviders.of(AllSongsFragment.this).get(AllSongViewModel.class);


        getAllSongs();
        return view;
    }

    private void getAllSongs() {
        callAllSongs = service.getAllSongs("none", "all");
        callAllSongs.enqueue(new Callback<List<KaraokeSong>>() {
            @Override
            public void onResponse(Call<List<KaraokeSong>> call, Response<List<KaraokeSong>> response) {
                Log.d("response", response.toString());
                if (response.isSuccessful()) {
                    adapter = new AllSongsAdapter(response.body(), getContext());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<KaraokeSong>> call, Throwable t) {
                Log.d(TAG, "failure" + t.getMessage());
            }
        });
    }

    @OnClick(R.id.btnSort)
    void sortAllSongs() {
        CharSequence[] items = new CharSequence[]{"None", "Time Descendant", "Time Ascendant", "Name Descendant", "Name Ascendant"};
        final String[] sortName = new String[]{ "none", "desc-time", "asc-time", "desc-name", "asc-name" };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sort by?").setSingleChoiceItems(items, allSongViewModel.getChooseIndex(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, i + "");
                allSongViewModel.setChooseIndex(i);
                allSongViewModel.setSort( sortName[i] );
            }
        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "OKE");
                callAllSongs = service.getAllSongs( sortName[allSongViewModel.getChooseIndex()], allSongViewModel.getGenre());
                callAllSongs.enqueue(new Callback<List<KaraokeSong>>() {
                    @Override
                    public void onResponse(Call<List<KaraokeSong>> call, Response<List<KaraokeSong>> response) {
                        Log.d(TAG, response.toString());
                        adapter = new AllSongsAdapter(response.body(), getContext());
                        recyclerView.setAdapter(adapter);
                    }
                    @Override
                    public void onFailure(Call<List<KaraokeSong>> call, Throwable t) {
                        Log.e(TAG, "failurrr" + t.getMessage());
                    }
                });

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
        alertDialog.show();
    }

    @OnClick(R.id.btnFilter)
    void filterAllSongs() {
        callGenre = service.getAllGenre();
        callGenre.enqueue(new Callback<List<Genre>>() {
            @Override
            public void onResponse(final Call<List<Genre>> call, Response<List<Genre>> response) {
                Log.d(TAG, response.toString());
                List<Genre> genreList = response.body();
                final CharSequence[] items = new CharSequence[genreList.size()];
                boolean[] booleans = new boolean[genreList.size()];

                int i = 0;
                for (Genre genre : genreList) {
                    items[i] = genre.getName();
                    booleans[i] = true;
                    i++;
                }
                if (allSongViewModel.getBooleans() == null) {
                    allSongViewModel.setBooleans(booleans);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Choose genre:")
                        .setMultiChoiceItems(items, allSongViewModel.getBooleans(), new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                //Log.d(TAG, i + "__" + (b ? 1 : 0));
                                allSongViewModel.getBooleans()[i] = b;
                            }
                        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < items.length; j++) {
                            if (allSongViewModel.getBooleans()[j]) {
                                stringBuilder.append("_" + items[j]);
                            }
                        }
                        callAllSongs = service.getAllSongs(allSongViewModel.getSort(), stringBuilder.toString());
                        callAllSongs.enqueue(new Callback<List<KaraokeSong>>() {
                            @Override
                            public void onResponse(Call<List<KaraokeSong>> call, Response<List<KaraokeSong>> response) {
                                Log.d(TAG, response.toString());
                                adapter = new AllSongsAdapter(response.body(), getContext());
                                recyclerView.setAdapter(adapter);
                            }

                            @Override
                            public void onFailure(Call<List<KaraokeSong>> call, Throwable t) {
                                Log.e(TAG, "fail" + t.getMessage());
                            }
                        });

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                ;
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                    }
                });
                alertDialog.show();
            }

            @Override
            public void onFailure(Call<List<Genre>> call, Throwable t) {
                Log.e(TAG, "failure:" + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callAllSongs != null) {
            callAllSongs.cancel();
            callAllSongs = null;
        }
        if (callGenre != null) {
            callGenre.cancel();
            callGenre = null;
        }
    }
}
