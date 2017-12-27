package com.trung.karaokeapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.network.TokenManager;
import com.trung.karaokeapp.utils.Utils;
import com.trung.karaokeapp.entities.AccessToken;
import com.trung.karaokeapp.entities.ApiError;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private final String PREFS = "prefs";

    @BindView(R.id.til_name) TextInputLayout til_name;
    @BindView(R.id.til_email) TextInputLayout til_email;
    @BindView(R.id.til_password) TextInputLayout til_password;
    @BindView(R.id.ivLogo) ImageView ivLogo;


    ApiService service;
    Call<AccessToken> call;
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        service = RetrofitBuilder.createService(ApiService.class);
        tokenManager = TokenManager.getInstance(getSharedPreferences(PREFS, MODE_PRIVATE));

        if (tokenManager.getToken().getAccessToken() != null) {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        }

        //load image logo
        ivLogo.setBackground(getDrawable(R.drawable.logo));
    }

    @OnClick(R.id.btn_register)
    void register() {
        String name = til_name.getEditText().getText().toString();
        String email = til_email.getEditText().getText().toString();
        String password = til_password.getEditText().getText().toString();

        til_name.setError(null);
        til_email.setError(null);
        til_password.setError(null);

        call = service.register(name, email, password);
        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                Log.d(TAG, response.toString());

                if (response.isSuccessful()) {
                    tokenManager.saveToken(response.body());
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                }else {
                    handleErrors(response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void handleErrors(ResponseBody response) {
        ApiError apiError = Utils.converErrors(response);

        for(Map.Entry<String, List<String>> error : apiError.getErrors().entrySet()){
            if(error.getKey().equals("name")){
                til_name.setError(error.getValue().get(0));
            }
            if(error.getKey().equals("email")){
                til_email.setError(error.getValue().get(0));
            }
            if(error.getKey().equals("password")){
                til_password.setError(error.getValue().get(0));
            }
        }
    }

    @OnClick(R.id.link_to_login)
    void goToLogin() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel();
            call = null;
        }
    }
}
