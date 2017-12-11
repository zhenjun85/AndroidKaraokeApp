package com.trung.karaokeapp.activity;

import android.arch.lifecycle.ViewModelProviders;
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
import com.trung.karaokeapp.model.MainActivityViewModel;

public class MainActivity extends AppCompatActivity
implements HomeFragment.OnFragmentInteractionListener,
        SongBookFragment.OnFragmentInteractionListener,
        FeedFragment.OnFragmentInteractionListener,
        NotificationFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener
{

    private BottomNavigationView bottomNavigation;
    private MainActivityViewModel activityViewModel;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //prepare
        bottomNavigation = findViewById(R.id.navigation);
        fragmentManager = getSupportFragmentManager();
        activityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        //assign HomeFragment for first load application
        activityViewModel.homeFragment = new HomeFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_container, activityViewModel.homeFragment).commit();

        subcribeBottomNavEvent();
    }

    private void subcribeBottomNavEvent() {
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        if (activityViewModel.homeFragment == null){
                            activityViewModel.homeFragment = new HomeFragment();
                        }
                        fragment = activityViewModel.homeFragment;
                        break;
                    case R.id.navigation_songbook:
                        if (activityViewModel.songBookFragment == null){
                            activityViewModel.songBookFragment = new SongBookFragment();
                        }
                        fragment = activityViewModel.songBookFragment;
                        break;

                    case R.id.navigation_feed:
                        if (activityViewModel.feedFragment == null){
                            activityViewModel.feedFragment = new FeedFragment();
                        }
                        fragment = activityViewModel.feedFragment;
                        break;
                    case R.id.navigation_notifications:
                        if (activityViewModel.notificationFragment == null){
                            activityViewModel.notificationFragment = new NotificationFragment();
                        }
                        fragment = activityViewModel.notificationFragment;
                        break;
                    case R.id.navigation_profile:
                        if (activityViewModel.profileFragment == null){
                            activityViewModel.profileFragment = new ProfileFragment();
                        }
                        fragment = activityViewModel.profileFragment;
                        break;
                    default:
                        return false;
                }

                FragmentTransaction transaction = fragmentManager.beginTransaction();
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
