package com.com.clone_spotify.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import static com.com.clone_spotify.util.Constants.LAST_ARTIST;
import static com.com.clone_spotify.util.Constants.LAST_ARTIST_IMAGE;
import static com.com.clone_spotify.util.Constants.LAST_CATEGORY;
import static com.com.clone_spotify.util.Constants.MEDIA_QUEUE_POSITION;
import static com.com.clone_spotify.util.Constants.NOW_PLAYING;
import static com.com.clone_spotify.util.Constants.PLAYLIST_ID;
import static com.com.clone_spotify.util.Constants.PLAYSONG_ID;

public class MyPreferenceManager {
    // SharedPreference 쓰는 이유 앱을 다시 켰을때 플레이리스트 불러오기, 재생중이던 노래가 저장되어
    // 다시 틀었을때 저장되어 있음!

    private static final String TAG = "MyPreferenceManager";

    private SharedPreferences mPreferences;

    public MyPreferenceManager(Context mContext) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void savePlaySongId(String playSongId){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PLAYSONG_ID, playSongId);
        editor.apply();
    }

    public String getPlaySongId(){
        return mPreferences.getString(PLAYSONG_ID, "");
    }

    public String getPlaylistId(){
        return mPreferences.getString(PLAYLIST_ID, "");
    }

    public void savePlaylistId(String playlistId){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PLAYLIST_ID, playlistId);
        editor.apply();
    }
    //queueposition은 순서대로 불러오기 위해 지정
    public void saveQueuePosition(int position){
        Log.d(TAG, "saveQueuePosition: SAVING QUEUE INDEX: " + position);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(MEDIA_QUEUE_POSITION, position);
        editor.apply();
    }

    public int getQueuePosition(){
        return mPreferences.getInt(MEDIA_QUEUE_POSITION, -1);
    }

    public String getLastPlayedArtistImage(){
        return  mPreferences.getString(LAST_ARTIST_IMAGE, "");
    }
    // 수정할 것 안쓰니까
    public String getLastPlayedArtist(){
        return  mPreferences.getString(LAST_ARTIST, "");
    }
    // 수정하기 안쓰니까
    public String getLastCategory(){
        return  mPreferences.getString(LAST_CATEGORY, "");
    }

    // 가장 마지막에 플레이 한 노래 저장
    public void saveLastPlayedMedia(String mediaId){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(NOW_PLAYING, mediaId);
        editor.apply();
    }

    public String getLastPlayedMedia(){
        return mPreferences.getString(NOW_PLAYING, "");
    }
}
