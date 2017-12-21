package com.trung.karaokeapp.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.TokenManager;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.network.RetrofitBuilder;

import java.io.File;
import java.io.IOException;

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

public class PostRecordActivity extends AppCompatActivity {
    private static final String TAG = "PostRecordActivity";
    @BindView(R.id.ivCoverSong) ImageView ivCoverSong;
    @BindView(R.id.etContent) EditText etContent;
    @BindView(R.id.btnPost) AppCompatButton btnPost;

    TokenManager tokenManager;
    ApiService service;
    private Call<ResponseBody> callUploadRecord;
    private KaraokeSong karaokeSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_record);
        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Post");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //setup song for activity
        karaokeSong = new Gson().fromJson(getIntent().getStringExtra("song"), KaraokeSong.class);
        String folderName = karaokeSong.getBeat().substring(0, karaokeSong.getBeat().length() - 4);
        Glide.with(getBaseContext()).load(AppURL.baseUrlSongAndLyric + "/" + folderName + "/" + karaokeSong.getImage()).into(ivCoverSong);

    }

    @OnClick(R.id.btnPost)
    void post() {
        //upload file to server
        String filePath = getIntent().getStringExtra("record");
        Log.d(TAG, "filePath:" + filePath);

        final File file = new File(filePath);
        final RequestBody requestBody = RequestBody.create(MediaType.parse("audio/mpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("myfile", file.getName(), requestBody);
        RequestBody description = RequestBody.create(MultipartBody.FORM, "upload record");
        callUploadRecord = service.postAudioRecord(body, description);
        callUploadRecord.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, response.toString());
                //update success
                String fileName = "";
                int score = 10;
                try {
                    fileName = response.body().string();
                    fileName = fileName.substring( fileName.lastIndexOf("/") + 1 );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Call<Integer> callAddSR = service.addSharedRecord(karaokeSong.getId(), "audio", fileName, score, etContent.getText().toString());
                callAddSR.enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        Log.d(TAG, requestBody.toString());
                        if ( response.body() == 1) {
                            Toast.makeText(getBaseContext(), "Post completely!", Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            Toast.makeText(getBaseContext(), "Post fail!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        Log.d(TAG, "Failure:" + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "fail" + t.getMessage());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_post_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mn_close:
                finish();
                break;
            default:break;
        }
        return true;
    }
}
