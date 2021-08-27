package com.com.clone_spotify.exoplayer;

import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.com.clone_spotify.repository.MusicDatabase;
import com.com.clone_spotify.repository.dto.Song;
import com.google.android.exoplayer2.source.MediaSource;


import java.util.List;
import java.util.TreeMap;

import javax.inject.Inject;

public class FirebaseMusicSource {

    @Inject
    private MusicDatabase musicDatabase;

    private static final TreeMap<String, MediaMetadataCompat> songs = new TreeMap<>();

    @RequiresApi(api = Build.VERSION_CODES.N) // minSdkVersion 지정한 버전보다 낮으면 호출시 컴파일 에러
    public List<Song> fetchMediaData() {
        State state = State.STATE_INITIALIZING;
        List<Song> allSongs = musicDatabase.getAllSongs();
//        songs = allSongs.stream().map(song -> {
//            new MediaMetadataCompat.Builder()
//                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getSubtitle())
//                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.getMediaId())
//                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
//                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, song.getTitle())
//                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, song.getImageUrl())
//                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.getSongUrl())
//                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.getImageUrl())
//                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, song.getSubtitle())
//                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, song.getSubtitle())
//                    .build();
//        });
                state=State.STATE_INITIALIZED;
        return null;
    }

    private enum State {
        STATE_CREATED,
        STATE_INITIALIZING,
        STATE_INITIALIZED,
        STATE_ERROR
    }
}

