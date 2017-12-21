package com.trung.karaokeapp.network;

import com.trung.karaokeapp.entities.AccessToken;
import com.trung.karaokeapp.entities.Genre;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.entities.RecordUserKs;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.entities.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

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

    @POST("songs/all")
    @FormUrlEncoded
    Call<List<KaraokeSong>> getAllSongs( @Field("sort") String sortType, @Field("genre") String genre );

    @GET("songs/new/0")
    Call<List<KaraokeSong>> getAllNewSongs();

    @GET("songs/new/{num}")
    Call<List<KaraokeSong>> getNewSongs(@Path("num") int number);

    @GET("songs/feature/0")
    Call<List<KaraokeSong>> getAllFeatureSongs();

    @GET("songs/feature/{num}")
    Call<List<KaraokeSong>> getFeatureSongs(@Path("num") int number);

    //update viewNo
    @POST("songs/upview")
    @FormUrlEncoded
    Call<Boolean> updateViewNo(@Field("ksid") int ksid);

    @GET("user/recent")
    Call<List<RecordUserKs>> getRecentSongs();

    @POST("user/recent/remove")
    @FormUrlEncoded
    Call<Integer> removeRecentSong(@Field("ksid") int ksid);

    @GET("songs/genre")
    Call<List<Genre>> getAllGenre();

    @GET("songs/{id}")
    Call<KaraokeSong> getOne(@Path("id") int id);

    @GET("user")
    Call<User> getUser();

    @GET("user/shared-records")
    Call<List<SharedRecord>> getSharedRecord();

    //Upload file
    @Multipart
    @POST("record/upload")
    Call<ResponseBody> postAudioRecord(@Part MultipartBody.Part file, @Part("name") RequestBody name);

    //new Shared Record
    @POST("record/add")
    @FormUrlEncoded
    Call<Integer> addSharedRecord( @Field("ksid") int ksid, @Field("type") String type, @Field("path") String path, @Field("score") int score ,@Field("content") String content );

    @GET("record/popular/{num}")
    Call<List<SharedRecord>> getPopularSr(@Path("num") int num);

    @GET("songs/{id}/rank")
    Call<List<SharedRecord>> getRank(@Path("id") int id);
}
