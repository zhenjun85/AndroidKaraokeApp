package com.trung.karaokeapp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.trung.karaokeapp.R;
import com.trung.karaokeapp.adapter.NotificationAdapter;
import com.trung.karaokeapp.entities.ToAnnUser;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.network.TokenManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {
    private static final String TAG = "NotificationFragment";
    @BindView(R.id.tvSortText) TextView tvSortText;
    @BindView(R.id.btnSort) TextView btnSort;
    @BindView(R.id.rvNotification) RecyclerView rvNotification;
    private TokenManager tokenManager;
    private ApiService service;
    private Call<List<ToAnnUser>> callGetAnnouncement;
    private NotificationAdapter notificationAdapter;
    private CharSequence[] items;
    private int checkedItem = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_notifications);
        toolbar.setTitleTextColor(Color.WHITE);
        ButterKnife.bind(this, view);
        tokenManager = TokenManager.getInstance(getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        callGetAnnouncement = service.getAnnouncement(0, "desc");
        callGetAnnouncement.enqueue(new Callback<List<ToAnnUser>>() {
            @Override
            public void onResponse(Call<List<ToAnnUser>> call, Response<List<ToAnnUser>> response) {
                Log.d(TAG, response.toString());

                notificationAdapter = new NotificationAdapter(getContext(), response.body());
                rvNotification.setLayoutManager(new LinearLayoutManager(getContext()));
                rvNotification.setAdapter(notificationAdapter);

            }
            @Override
            public void onFailure(Call<List<ToAnnUser>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
        return view;
    }

    @OnClick(R.id.btnSort)
    void sortAnn() {
        items = new CharSequence[]{ "Most Recent", "Most Last" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkedItem = i;
            }
        }).setPositiveButton("Sort", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String sort = (checkedItem == 0) ? "desc" : "asc";
                tvSortText.setText( items[checkedItem] );

                callGetAnnouncement = service.getAnnouncement(0, sort);
                callGetAnnouncement.enqueue(new Callback<List<ToAnnUser>>() {
                    @Override
                    public void onResponse(Call<List<ToAnnUser>> call, Response<List<ToAnnUser>> response) {
                        Log.d(TAG, response.toString());
                        List<ToAnnUser> newToAnnUserList = response.body();
                        notificationAdapter = new NotificationAdapter( getContext(),  newToAnnUserList );
                        rvNotification.setAdapter(notificationAdapter);
                    }

                    @Override
                    public void onFailure(Call<List<ToAnnUser>> call, Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }
                });
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            }
        });
        alertDialog.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (callGetAnnouncement != null){
            callGetAnnouncement.cancel();
            callGetAnnouncement = null;
        }
    }
}
