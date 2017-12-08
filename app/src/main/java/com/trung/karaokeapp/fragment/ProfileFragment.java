package com.trung.karaokeapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.trung.karaokeapp.R;
import com.trung.karaokeapp.activity.LocalSongsActivity;

public class ProfileFragment extends Fragment implements View.OnClickListener {


    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_profile);

        setUpAppBar(view);

        ConstraintLayout playlist = view.findViewById(R.id.playlist_open);
        ConstraintLayout local_song = view.findViewById(R.id.local_songs_open);
        ConstraintLayout duet = view.findViewById(R.id.duet_open);
        ConstraintLayout photo = view.findViewById(R.id.photo_open);

        playlist.setOnClickListener(this);
        local_song.setOnClickListener(this);
        duet.setOnClickListener(this);
        photo.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        Intent intent= null;
        switch (view.getId()) {
            case R.id.playlist_open:

                break;
            case R.id.local_songs_open:
                intent = new Intent(getContext(), LocalSongsActivity.class);

                Log.d("___tr", "1");
                break;
            case R.id.duet_open:

                break;
            case R.id.photo_open:

                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void setUpAppBar(View view) {
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapse_toolbar);
        AppBarLayout appBarLayout = (AppBarLayout) view.findViewById(R.id.appBarLayout_profile);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle("Profile");
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
    }


    // TODO: Rename method, update argument and hook method into UI event
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
