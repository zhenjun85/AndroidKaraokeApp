package com.trung.karaokeapp.network;

import com.trung.karaokeapp.entities.AccessToken;
import com.trung.karaokeapp.entities.CommentTb;
import com.trung.karaokeapp.entities.Genre;
import com.trung.karaokeapp.entities.HasPlaylistKs;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.entities.PhotoTb;
import com.trung.karaokeapp.entities.Playlist;
import com.trung.karaokeapp.entities.RecordUserKs;
import com.trung.karaokeapp.entities.RelationTb;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.entities.ToAnnUser;
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
    Call<List<KaraokeSong>> getAllSongs(@Field("sort") String sortType, @Field("genre") String genre);

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
    Call<Boolean> upViewKs(@Field("ksid") int ksid);

    @GET("user/recent")
    Call<List<RecordUserKs>> getRecentSongs();

    @POST("user/recent/remove")
    @FormUrlEncoded
    Call<Integer> removeRecentSong(@Field("ksid") int ksid);

    @GET("songs/genre")
    Call<List<Genre>> getAllGenre();

    @GET("songs/{id}")
    Call<KaraokeSong> getOne(@Path("id") int id);

    @POST("songs/playlist")
    @FormUrlEncoded
    Call<List<HasPlaylistKs>> getPlaylistOfSong(@Field("kar_id") int ksId);

    @POST("songs/report")
    @FormUrlEncoded
    Call<Integer> reportKaraokeSong(@Field("kar_id") int ksId, @Field("subject") int subject);

    @GET("songs/recommend/{num}")
    Call<List<KaraokeSong>> getRecommend(@Path("num") int num);

    @POST("songs/notlike")
    @FormUrlEncoded
    Call<Integer> sendNotLike(@Field("ks+id") int ksId);

    @GET("user")
    Call<User> getUser();

    @GET("user/shared-records/{num}")
    Call<List<SharedRecord>> getSharedRecord(@Path("num") int num);

    //Upload file
    @Multipart
    @POST("record/upload")
    Call<ResponseBody> postAudioRecord(@Part MultipartBody.Part file, @Part("name") RequestBody name);

    //new Shared Record
    @POST("record/add")
    @FormUrlEncoded
    Call<Integer> addSharedRecord(@Field("ksid") int ksid, @Field("type") String type, @Field("path") String path, @Field("score") int score, @Field("content") String content);

    @GET("record/popular/{num}")
    Call<List<SharedRecord>> getPopularSr(@Path("num") int num);

    @POST("record/islike")
    @FormUrlEncoded
    Call<Integer> getIsLike(@Field("sr_id") int srid);

    @POST("record/like")
    @FormUrlEncoded
    Call<Integer> likeRecord(@Field("sr_id") int srid);

    @POST("record/comment")
    @FormUrlEncoded
    Call<CommentTb> commentRecord(@Field("sr_id") int srid, @Field("content") String content);

    @GET("record/{id}/comments")
    Call<List<CommentTb>> getCommentsOfSr(@Path("id") int srid);

    @POST("record/report")
    @FormUrlEncoded
    Call<Integer> reportSharedRecord(@Field("sr_id") int srId, @Field("subject") int subject);

    @POST("record/upview")
    @FormUrlEncoded
    Call<Integer> upViewSr(@Field("sr_id") int srid);

    @GET("record/{id}")
    Call<SharedRecord> getSr(@Path("id") int id);


    @GET("songs/{id}/rank")
    Call<List<SharedRecord>> getUserRank(@Path("id") int id);

    @GET("playlist/all")
    Call<List<Playlist>> getAllPlaylist();

    @POST("playlist/add")
    @FormUrlEncoded
    Call<Playlist> addPlaylist(@Field("name") String name);

    @POST("playlist/save")
    @FormUrlEncoded
    Call<Integer> savePlaylist(@Field("name") String name, @Field("id") int id);

    @POST("playlist/delete")
    @FormUrlEncoded
    Call<Integer> deletePlaylist(@Field("id") int id);

    @POST("playlist/songs")
    @FormUrlEncoded
    Call<List<HasPlaylistKs>> getSongsInPlaylist(@Field("pl_id") int playlistId);

    @POST("playlist/delsong")
    @FormUrlEncoded
    Call<Integer> delSongInPlaylist(@Field("pl_id") int playlistId, @Field("kar_id") int ksId);

    @POST("playlist/addsong")
    @FormUrlEncoded
    Call<Integer> addSongToPlaylist(@Field("pl_id") int playlistId, @Field("kar_id") int ksId);

    //FEED
    @GET("newfeed/{num}")
    Call<List<SharedRecord>> getNewFeeds(@Path("num") int num);

    @GET("relation/{other_id}")
    Call<Integer> getRelationStatus(@Path("other_id") int otherId);

    @POST("relation/request")
    @FormUrlEncoded
    Call<Integer> requestFriend(@Field("other_id") int otherId);

    @GET("relation/all")
    Call<List<RelationTb>> getAllRelation();


    @GET("announcement/{num}/{sort}")
    Call<List<ToAnnUser>> getAnnouncement(@Path("num") int num, @Path("sort") String sort);

    //Upload file
    @Multipart
    @POST("photo/upload")
    Call<ResponseBody> postImage(@Part MultipartBody.Part file, @Part("name") RequestBody name);

    @POST("photo/add")
    @FormUrlEncoded
    Call<PhotoTb> addPhoto(@Field("file_name") String fileName);

    @POST("photo/delete")
    @FormUrlEncoded
    Call<Integer> delPhoto(@Field("photo_id") int photoId);

    @GET("photo/all")
    Call<List<PhotoTb>> getAllPhotos();


    @POST("user/update-avatar")
    @FormUrlEncoded
    Call<Integer> updateUserAvatar(@Field("path") String path);



}
