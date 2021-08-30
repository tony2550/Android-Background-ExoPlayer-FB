package com.com.clone_spotify;

import android.app.Application;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import dagger.hilt.android.HiltAndroidApp;

public class SpotifyApplication extends Application {

    private static final String TAG = "MyApplication";

    private static SpotifyApplication mInstance;
    private List<MediaBrowserCompat.MediaItem> mMediaItems = new ArrayList<>();
    private TreeMap<String, MediaMetadataCompat> mTreeMap = new TreeMap<>();

    public static SpotifyApplication getInstance(){
        if(mInstance == null){
            mInstance = new SpotifyApplication();
        }
        return mInstance;
    }

    public List<MediaBrowserCompat.MediaItem> getMediaItems(){
        return mMediaItems;
    }

    public TreeMap<String, MediaMetadataCompat> getTreeMap(){
        return mTreeMap;
    }

    public void setMediaItems(List<MediaMetadataCompat> mediaItems){
        mMediaItems.clear();
        for(MediaMetadataCompat item: mediaItems){
            Log.d(TAG, "setMediaItems: called: adding media item: " + item.getDescription());
            mMediaItems.add(
                    new MediaBrowserCompat.MediaItem(
                            item.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
            );
            mTreeMap.put(item.getDescription().getMediaId(), item);
        }
    }

    public MediaMetadataCompat getMediaItem(String mediaId){
        return mTreeMap.get(mediaId);
    }
}
