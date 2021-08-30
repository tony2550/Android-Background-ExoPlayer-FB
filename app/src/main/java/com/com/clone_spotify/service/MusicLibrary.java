package com.com.clone_spotify.service;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.com.clone_spotify.R;
import com.com.clone_spotify.repository.MusicDatabase;
import com.com.clone_spotify.repository.dto.Song;
import com.google.android.exoplayer2.ext.mediasession.BuildConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class MusicLibrary {

    private static final TreeMap<String, MediaMetadataCompat> songMap = new TreeMap<>();
    public List<MediaMetadataCompat> mMediaList = new ArrayList<>();

    public MusicLibrary() {
        initMap();
    }
    private MusicDatabase musicDatabase;

    public List<MediaMetadataCompat> getSongList() {
        List<MediaMetadataCompat> tracks = new ArrayList<>();
        List<Song> songs = musicDatabase.getAllSongs();
        for (Song song : songs) {
            tracks.add(buildMedia(song));
        }
        return tracks;
    }

    // song 오브젝트 -> metadata 등록 함수
    private MediaMetadataCompat buildMedia(Song song) {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getSubtitle())
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.getMediaId())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, song.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, song.getImageUrl())
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.getSongUrl())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.getImageUrl())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, song.getSubtitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, song.getSubtitle())
                .build();
    }

    private void initMap(){
        List<MediaMetadataCompat> songs = getSongList();
        for (MediaMetadataCompat song : songs) {
            String mediaId = song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            songMap.put(mediaId, song);
            mMediaList.add(song);
        }
    }

    public TreeMap<String, MediaMetadataCompat> getTreeMap(){
        return songMap;
    }

    public static String getRoot() {
        return "root";
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (MediaMetadataCompat metadata : songMap.values()) {
            result.add(
                    new MediaBrowserCompat.MediaItem(
                            metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }

    public static List<MediaBrowserCompat.MediaItem> getPlaylistMedia(Set<String> mediaIds) {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();

        // VERY INEFFICIENT WAY TO DO THIS (BUT I NEED TO BECAUSE THE DATA STRUCTURE ARE NOT IDEAL)
        // RETRIEVING DATA FROM A SERVER WOULD NOT POSE THIS ISSUE
        for(String id: mediaIds){
            for (MediaMetadataCompat metadata : songMap.values()) {
                if(id.equals(metadata.getDescription().getMediaId())){
                    result.add(
                            new MediaBrowserCompat.MediaItem(
                                    metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
                }
            }
        }


        return result;
    }

}