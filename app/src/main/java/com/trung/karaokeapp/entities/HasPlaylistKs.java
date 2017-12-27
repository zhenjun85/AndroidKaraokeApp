package com.trung.karaokeapp.entities;

import com.squareup.moshi.Json;

/**
 * Created by avc on 12/25/2017.
 */

public class HasPlaylistKs {
    @Json(name = "pl_id")
    int playlistId;
    @Json(name = "kar_id")
    int karaokeSongId;
    String created_at;
    @Json(name = "karaokesong")
    KaraokeSong karaokeSong;


    public KaraokeSong getKaraokeSong() {
        return karaokeSong;
    }

    public void setKaraokeSong(KaraokeSong karaokeSong) {
        this.karaokeSong = karaokeSong;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public int getKaraokeSongId() {
        return karaokeSongId;
    }

    public void setKaraokeSongId(int karaokeSongId) {
        this.karaokeSongId = karaokeSongId;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
