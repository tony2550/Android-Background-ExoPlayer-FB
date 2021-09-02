package com.com.clone_spotify.client;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.NonNull;


import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.media.MediaBrowserServiceCompat;

import java.util.List;

public class MediaBrowserHelper {

    private static final String TAG = "MediaBrowserHelper";

    private final Context mContext;
    private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;

    private final MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
    private final MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;
    private final MediaControllerCallback mMediaControllerCallback;
    private MediaBrowserHelperCallback mMediaBrowserCallback;
    private boolean mWasConfigurationChange;

    public MediaBrowserHelper(Context context, Class<? extends MediaBrowserServiceCompat> serviceClass) {
        mContext = context;
        mMediaBrowserServiceClass = serviceClass;

        mMediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();
        mMediaControllerCallback = new MediaControllerCallback();
    }

    public void setMediaBrowserHelperCallback(MediaBrowserHelperCallback callback){
        mMediaBrowserCallback = callback;
    }

    // MediaController->  콜백수신 ->  UI 상태업데이트
    // 현재 항목이 재생 중인지 일시 중지 중인지
    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.d(TAG, "onMetadataChanged: Call");
            if(mMediaBrowserCallback != null){
                mMediaBrowserCallback.onMetadataChanged(metadata);
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d(TAG, "onPlaybackStateChanged: call");
            if(mMediaBrowserCallback != null){
                mMediaBrowserCallback.onPlaybackStateChanged(state);
            }
        }

        // 액티비티가 포그라운드에 onStart() 호출 -> MusicService 가 종료 되었을때
        @Override
        public void onSessionDestroyed() {
            onPlaybackStateChanged(null);
        }
    }

    public void subscribeToNewSong(String currentPlaySongId, String newPlatSongId){
        if(!currentPlaySongId.equals("")){
            mMediaBrowser.unsubscribe(currentPlaySongId);
        }
        mMediaBrowser.subscribe(newPlatSongId, mMediaBrowserSubscriptionCallback);
    }

//    public void subscribeToNewPlaylist(String currentPlaylistId, String newPlatlistId){
//        if(!currentPlaylistId.equals("")){
//            mMediaBrowser.unsubscribe(currentPlaylistId);
//        }
//        mMediaBrowser.subscribe(newPlatlistId, mMediaBrowserSubscriptionCallback);
//    }

    public void onStart(boolean wasConfigurationChange) {
        mWasConfigurationChange = wasConfigurationChange;
        if (mMediaBrowser == null) {
            mMediaBrowser =
                    new MediaBrowserCompat(
                            mContext,
                            new ComponentName(mContext, mMediaBrowserServiceClass),
                            mMediaBrowserConnectionCallback,
                            null);
            mMediaBrowser.connect();
        }
        Log.d(TAG, "onStart: CALLED: Creating MediaBrowser, and connecting");
    }

    public void onStop() {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
            mMediaController = null;
        }
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mMediaBrowser.disconnect();
            mMediaBrowser = null;
        }
        Log.d(TAG, "onStop: CALLED: Releasing MediaController, Disconnecting from MediaBrowser");
    }
    // MediaBrowser 가 MediaBrowserService 와 성공적으로 연결 후 콜백을 받는다
    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        //onStart() 의 결과
        @Override
        public void onConnected() {
            Log.d(TAG, "onConnected: CALLED");
            try {
                // Get a MediaController for the MediaSession.
                mMediaController =
                        new MediaControllerCompat(mContext, mMediaBrowser.getSessionToken());
                mMediaController.registerCallback(mMediaControllerCallback);


            } catch (RemoteException e) {
                Log.d(TAG, String.format("onConnected: Problem: %s", e.toString()));
                throw new RuntimeException(e);
            }

            mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mMediaBrowserSubscriptionCallback);
            Log.d(TAG, "onConnected: CALLED: subscribing to: " + mMediaBrowser.getRoot());

            mMediaBrowserCallback.onMediaControllerConnected(mMediaController);
        }
    }

    // 미디어브라우저서비스가 새 미디어 로드시 브라우저에서 콜백 수신
    // playback 준비
    public class MediaBrowserSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback {

        @Override
        public void onChildrenLoaded(@NonNull String parentId,
                                     @NonNull List<MediaBrowserCompat.MediaItem> children) {
            Log.d(TAG, "onChildrenLoaded: CALLED: " + parentId + ", " + children.toString());

            if(!mWasConfigurationChange){
                for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                    Log.d(TAG, "onChildrenLoaded: CALLED: queue item: " + mediaItem.getMediaId());
                    mMediaController.addQueueItem(mediaItem.getDescription());
                }
            }

        }
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        if (mMediaController == null) {
            Log.d(TAG, "getTransportControls: MediaController is null!");
            throw new IllegalStateException("MediaController is null!");
        }
        return mMediaController.getTransportControls();
    }


}
