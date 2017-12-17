package com.trung.karaokeapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.TokenManager;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment extends PreferenceFragment {
    private static final String TAG = "SettingFragment";
    private TokenManager tokenManager;
    private ApiService service;
    private Call<String> call;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.xml_setting);

        tokenManager = TokenManager.getInstance(getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        Preference btnLogout = findPreference("key_logout");
        btnLogout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                logout();
                return true;
            }
        });

    }

    private void logout() {
        call = service.logout("12");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    tokenManager.deleteToken();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d(TAG, "failure" + t.getMessage());
            }
        });


    }
    private void finish(){
        getActivity().finish();
    }
}
