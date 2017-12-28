package com.trung.karaokeapp.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";

    ApiService service;

    @BindView(R.id.btnButton)Button btnButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
        List<Integer> list = new ArrayList<>();
        list.add(4);
        list.add(2);


        list.remove(new Integer(4));
        Log.d(TAG, list.size() + "");
        Log.d(TAG, list.get(0) + "");
    }
}
