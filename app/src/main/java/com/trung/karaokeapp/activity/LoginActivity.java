package com.trung.karaokeapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.entities.AccessToken;
import com.trung.karaokeapp.entities.ApiError;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.network.TokenManager;
import com.trung.karaokeapp.utils.Utils;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private final String PREFS = "prefs";

    @BindView(R.id.til_email) TextInputLayout til_email;
    @BindView(R.id.til_password) TextInputLayout til_password;
    @BindView(R.id.ivLogo) ImageView ivLogo;

    ApiService service;
    Call<AccessToken> call;
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        service = RetrofitBuilder.createService(ApiService.class);
        tokenManager = TokenManager.getInstance(getSharedPreferences(PREFS, MODE_PRIVATE));

        if (tokenManager.getToken().getAccessToken() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        //load Logo
        ivLogo.setBackground(getDrawable(R.drawable.logo));
    }

    @OnClick(R.id.btn_login)
    void login() {
        String email = til_email.getEditText().getText().toString();
        String password = til_password.getEditText().getText().toString();

        til_email.setError(null);
        til_password.setError(null);

        call = service.login(email, password);
        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                Log.d(TAG, response.toString());

                if (response.isSuccessful()) {
                    tokenManager.saveToken(response.body());
                    Toast.makeText(getApplicationContext(), "Login success!", Toast.LENGTH_SHORT ).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }else {
                    if (response.code() == 422) {
                        handleErrors(response.errorBody());
                    }
                    if (response.code() == 401) {
                        ApiError apiError = Utils.converErrors(response.errorBody());
                        Toast.makeText(getApplicationContext(), apiError.getMessage(), Toast.LENGTH_SHORT ).show();
                    }
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
            if(error.getKey().equals("email")){
                til_email.setError(error.getValue().get(0));
            }
            if(error.getKey().equals("password")){
                til_password.setError(error.getValue().get(0));
            }
        }
    }

    @OnClick(R.id.link_to_register)
    void goToRegister(){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
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
