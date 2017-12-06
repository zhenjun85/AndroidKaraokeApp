package com.trung.karaokeapp.activity;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.fragment.FeedFragment;
import com.trung.karaokeapp.fragment.HomeFragment;
import com.trung.karaokeapp.fragment.NotificationFragment;
import com.trung.karaokeapp.fragment.ProfileFragment;
import com.trung.karaokeapp.fragment.SongBookFragment;

public class MainActivity extends AppCompatActivity
implements HomeFragment.OnFragmentInteractionListener,
        SongBookFragment.OnFragmentInteractionListener,
        FeedFragment.OnFragmentInteractionListener,
        NotificationFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener
{


    private BottomNavigationView bottomNavigation;
    private Fragment fragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        fragmentManager = getSupportFragmentManager();
        fragment = new HomeFragment();

        //init home fragment for first load application
        fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit();
        subcribeBottomNavEvent();
    }

    private void subcribeBottomNavEvent() {
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        fragment = new HomeFragment();
                        break;
                    case R.id.navigation_songbook:
                        fragment = new SongBookFragment();
                        break;
                    case R.id.navigation_feed:
                        fragment = new FeedFragment();
                        break;
                    case R.id.navigation_notifications:
                        fragment = new NotificationFragment();
                        break;
                    case R.id.navigation_profile:
                        fragment = new ProfileFragment();
                        break;
                }
                final FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_container, fragment).commit();

                return true;
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("uri,", uri.toString());
    }
}
