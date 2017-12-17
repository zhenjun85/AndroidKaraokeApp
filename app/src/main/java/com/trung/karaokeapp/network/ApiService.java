package com.trung.karaokeapp.network;

import com.trung.karaokeapp.entities.AccessToken;
import com.trung.karaokeapp.entities.KaraokeSong;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by avc on 12/13/2017.
 */

public interface ApiService {

    @POST("register")
    @FormUrlEncoded
    Call<AccessToken> register(@Field("name") String name, @Field("email") String email, @Field("password") String password);

    @POST("login")
    @FormUrlEncoded
    Call<AccessToken> login(@Field("email") String email, @Field("password") String password);

    @POST("logout")
    @FormUrlEncoded
    Call<String> logout(@Field("abc") String abc);

    @POST("refresh")
    @FormUrlEncoded
    Call<AccessToken> refresh(@Field("refresh_token") String refreshToken);

    @GET("all-songs")
    Call<List<KaraokeSong>> getAllSongs();
}
