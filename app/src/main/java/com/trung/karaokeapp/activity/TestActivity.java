package com.trung.karaokeapp.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;


import com.trung.karaokeapp.R;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";

    ApiService service;

    @BindView(R.id.btnButton)Button btnButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        service = RetrofitBuilder.createService(ApiService.class);

    }

    @OnClick(R.id.btnButton)
    void onClcc() {

        String filePath = "/storage/emulated/0/app_karaoke/records/4_I Gotta Feeling_201712210146_raw_10_00-05.mp3";

        File file = new File(filePath);

        final RequestBody requestBody = RequestBody.create(MediaType.parse("audio/mpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("myfile", file.getName(), requestBody);

        RequestBody description = RequestBody.create(MultipartBody.FORM, "hihihi");
        Call<ResponseBody> callable = service.postAudioRecord(body, description);
        callable.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, response.toString());

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "fail" + t.getMessage());
            }
        });
    }

}
