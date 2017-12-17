package com.trung.karaokeapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;


import com.trung.karaokeapp.R;
import com.trung.karaokeapp.activity.DuetManageActivity;
import com.trung.karaokeapp.activity.FriendActivity;
import com.trung.karaokeapp.activity.LocalSongsActivity;
import com.trung.karaokeapp.activity.PhotoManageActivity;
import com.trung.karaokeapp.activity.PlaylistActivity;
import com.trung.karaokeapp.activity.SettingsActivity;

public class ProfileFragment extends Fragment implements View.OnClickListener {


    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_profile);
        setUpToolbar(toolbar);

        //button
        RelativeLayout playlist = view.findViewById(R.id.btn_playlist_open);
        RelativeLayout local_song = view.findViewById(R.id.btn_local_song_open);
        RelativeLayout duet = view.findViewById(R.id.btn_duet_open);
        RelativeLayout photo = view.findViewById(R.id.btn_photo_open);
        RelativeLayout friend = view.findViewById(R.id.btn_friend_open);
        playlist.setOnClickListener(this);
        local_song.setOnClickListener(this);
        duet.setOnClickListener(this);
        photo.setOnClickListener(this);
        friend.setOnClickListener(this);

        //grid view
        GridView gridView = view.findViewById(R.id.gv_shared_songs);
        MyAdapter myAdapter = new MyAdapter(getContext());
        gridView.setAdapter(myAdapter);

        gridView.setMinimumHeight(1000);
        return view;
    }

    private void setUpToolbar(Toolbar toolbar) {
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.mn_settings:
                        startActivity(new Intent(getContext(), SettingsActivity.class));
                        break;
                    default:break;
                }

                return true;
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent= null;
        switch (view.getId()) {
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
            default:break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    //Adapter for grid view
    public class MyAdapter extends BaseAdapter {
        private Context context;

        MyAdapter(Context c) {
            this.context = c;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = getLayoutInflater();
                view = inflater.inflate(R.layout.item_rv_shared_song_profile, viewGroup, false);
            }

            return view;
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
