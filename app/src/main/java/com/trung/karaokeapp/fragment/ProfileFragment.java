package com.trung.karaokeapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.activity.SeeMoreRecenRecordActivity;
import com.trung.karaokeapp.network.TokenManager;
import com.trung.karaokeapp.activity.DuetManageActivity;
import com.trung.karaokeapp.activity.FriendActivity;
import com.trung.karaokeapp.activity.LocalSongsActivity;
import com.trung.karaokeapp.activity.PhotoManageActivity;
import com.trung.karaokeapp.activity.PlaylistActivity;
import com.trung.karaokeapp.activity.SettingsActivity;
import com.trung.karaokeapp.adapter.PopularSrAdapter;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.entities.User;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.network.RetrofitBuilder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    @BindView(R.id.btnSettings) ImageButton btnSettings;
    @BindView(R.id.btn_playlist_open) RelativeLayout btnPlaylist;
    @BindView(R.id.btn_local_song_open) RelativeLayout btnLocalSong;
    @BindView(R.id.btn_duet_open) RelativeLayout btnDuet;
    @BindView(R.id.btn_photo_open) RelativeLayout btnPhoto;
    @BindView(R.id.btn_friend_open) RelativeLayout btnFriend;

    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.tvBirthday) TextView tvBirthday;
    @BindView(R.id.tvIntroduce) TextView tvIntroduce;
    @BindView(R.id.iv_user_avatar) CircleImageView ivUserAvatar;
    @BindView(R.id.rvShareSongs) RecyclerView rvSharedSongs;


    TokenManager tokenManager;
    ApiService service;
    Call<User> callGetUser;
    private User user;
    private Call<List<SharedRecord>> callGetSharedRecords;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        tokenManager = TokenManager.getInstance(getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        callGetUser = service.getUser();
        callGetUser.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, response.toString());

                //load user information
                user = response.body();
                tvName.setText(user.getName());
                tvBirthday.setText(user.getBirthday());
                tvIntroduce.setText(user.getIntroduce());

                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(getContext()).load(AppURL.baseUrlPhotos + "/" + user.getAvatar()).into(ivUserAvatar);
                }else {
                    ivUserAvatar.setImageDrawable(getActivity().getDrawable(R.drawable.ic_face_black_48px));
                }

                //get user record
                callGetSharedRecords = service.getSharedRecord(6);
                callGetSharedRecords.enqueue(new Callback<List<SharedRecord>>() {
                    @Override
                    public void onResponse(Call<List<SharedRecord>> call, Response<List<SharedRecord>> response) {
                        Log.d(TAG, response.toString());
                        //inflate to GridView

                        PopularSrAdapter adapter = new PopularSrAdapter(getContext(), response.body());
                        rvSharedSongs.setLayoutManager(new GridLayoutManager(getContext(), 2));
                        rvSharedSongs.setAdapter(adapter);

                    }

                    @Override
                    public void onFailure(Call<List<SharedRecord>> call, Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }
                });

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "failure" + t.getMessage());
            }
        });



//        gridView.setMinimumHeight(1000);
        return view;
    }
    @OnClick(R.id.btnSettings)
    void openSettings() {
        onMyClick(R.id.btnSettings);
    }
    @OnClick(R.id.btn_playlist_open)
    void openPlaylist() {
        onMyClick(R.id.btn_playlist_open);
    }
    @OnClick(R.id.btn_local_song_open)
    void openLocalSongs() {
        onMyClick(R.id.btn_local_song_open);
    }
    @OnClick(R.id.btn_duet_open)
    void openDuets() {
        onMyClick(R.id.btn_duet_open);
    }
    @OnClick(R.id.btn_photo_open)
    void openPhotos() {
        onMyClick(R.id.btn_photo_open);
    }
    @OnClick(R.id.btn_friend_open)
    void openFriends() {
        onMyClick(R.id.btn_friend_open);
    }

    @OnClick(R.id.tvMoreRecentRecord)
    void moreRecentRecord() {
        Intent intent = new Intent(getContext(), SeeMoreRecenRecordActivity.class);
        startActivity(intent);
    }

    public void onMyClick(int btnId) {
        Intent intent = null;
        switch (btnId) {
            case R.id.btn_playlist_open:
                intent = new Intent(getContext(), PlaylistActivity.class);
                break;
            case R.id.btn_local_song_open:
                intent = new Intent(getContext(), LocalSongsActivity.class);
                break;
            case R.id.btn_duet_open:
                intent = new Intent(getContext(), DuetManageActivity.class);
                break;
            case R.id.btn_photo_open:
                intent = new Intent(getContext(), PhotoManageActivity.class);
                break;
            case R.id.btn_friend_open:
                intent = new Intent(getContext(), FriendActivity.class);
                break;
            case R.id.btnSettings:
                intent = new Intent(getContext(), SettingsActivity.class);
                break;
            default:break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

}
