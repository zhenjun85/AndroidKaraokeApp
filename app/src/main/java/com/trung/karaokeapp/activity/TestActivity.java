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

        String txt =
                "INSERT INTO `record_user_ks` (`user_id`, `kar_id`, `created_at`, `updated_at`, `count`) VALUES ('6', '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '1');";
        Log.d(TAG, txt  );
    }
}
