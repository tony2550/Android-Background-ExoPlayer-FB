package com.com.clone_spotify.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.MediaSessionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.com.clone_spotify.R;
import com.com.clone_spotify.SpotifyApplication;
import com.com.clone_spotify.notification.MediaNotificationManager;
import com.com.clone_spotify.player.MediaPlayerAdapter;
import com.com.clone_spotify.player.PlaybackInfoListener;
import com.com.clone_spotify.util.MyPreferenceManager;
import com.google.android.exoplayer2.audio.AudioAttributes;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.com.clone_spotify.util.Constants.MEDIA_QUEUE_POSITION;
import static com.com.clone_spotify.util.Constants.QUEUE_NEW_PLAYLIST;
import static com.com.clone_spotify.util.Constants.SEEK_BAR_MAX;
import static com.com.clone_spotify.util.Constants.SEEK_BAR_PROGRESS;
import static com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC;
import static com.google.android.exoplayer2.C.USAGE_MEDIA;

public class MusicService extends MediaBrowserServiceCompat {

    private static final String TAG = "MusicService";
    //미디어 세션과 플레이어를 MediaBrowserService의 서브클래스로 구현
    private MediaSessionCompat mSession;
    // 플레이 리스트 파싱해주는 어댑터
    private MediaPlayerAdapter mPlayback;
    private SpotifyApplication mMyApplication;
    private MyPreferenceManager mMyPreferenceManager;
    // 폰 닫았을때 , 화면 위에서 내렸을 때 노티피케이션 매니저
    private MediaNotificationManager mMediaNotificationManager;
    private boolean mIsServiceStarted; // 상태를 가지는 변수

    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    // Override 메소드 3개 onCreate() , onDestroy() , onTaskRemoved()
    @Override
    public void onCreate() {
        super.onCreate();

        mMyApplication = SpotifyApplication.getInstance();
        mMyPreferenceManager = new MyPreferenceManager(this);

        //Build the MediaSession
        mSession = new MediaSessionCompat(this, TAG);
        // 음악 제어하기위해 미디어 버튼과 연결
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |

                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);

        mSession.setCallback(new MediaSessionCallback());

        // A token that can be used to create a MediaController for this session
        setSessionToken(mSession.getSessionToken());


        mPlayback = new MediaPlayerAdapter(this, new MediaPlayerListener());
        mMediaNotificationManager = new MediaNotificationManager(this);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved: stopped");
        super.onTaskRemoved(rootIntent);
        mPlayback.stop();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        mSession.release();
        Log.d(TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released");
    }


    @Override
    public BrowserRoot onGetRoot(String s, int i, Bundle bundle) {

        Log.d(TAG, "onGetRoot: called. ");
        if(s.equals(getApplicationContext().getPackageName())){

            // Allowed to browse media
            return new BrowserRoot("some_real_playlist", null); // return no media
        }
        return new BrowserRoot("empty_media", null); // return no media
    }

    @Override
    public void onLoadChildren(@android.support.annotation.NonNull String s, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onLoadChildren: called: " + s + ", " + result);

        //  Browsing not allowed
        if (TextUtils.equals("empty_media", s)) {
            result.sendResult(null);
            return;
        }
        result.sendResult(mMyApplication.getMediaItems()); // return all available media
    }


    public class MediaSessionCallback extends MediaSessionCompat.Callback {

        private final List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia;

        private void resetPlaylist(){
            mPlaylist.clear();
            mQueueIndex = -1;
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(TAG, "onPlayFromMediaId: CALLED.");
            if(extras.getBoolean(QUEUE_NEW_PLAYLIST, false)){
                resetPlaylist();
            }

            mPreparedMedia = mMyApplication.getTreeMap().get(mediaId);
            mSession.setMetadata(mPreparedMedia);
            if (!mSession.isActive()) {
                mSession.setActive(true);
            }
            mPlayback.playFromMedia(mPreparedMedia);

            int newQueuePosition = extras.getInt(MEDIA_QUEUE_POSITION, -1);
            if(newQueuePosition == -1){
                mQueueIndex++;
            }
            else{
                mQueueIndex = extras.getInt(MEDIA_QUEUE_POSITION);
            }
            mMyPreferenceManager.saveQueuePosition(mQueueIndex);
            mMyPreferenceManager.saveLastPlayedMedia(mPreparedMedia.getDescription().getMediaId());
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            Log.d(TAG, "onAddQueueItem: CALLED: position in list: " + mPlaylist.size());
            mPlaylist.add(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
            mSession.setQueue(mPlaylist);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            mPlaylist.remove(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mPlaylist.isEmpty()) ? -1 : mQueueIndex;
            mSession.setQueue(mPlaylist);
        }

        @Override
        public void onPrepare() {
            if (mQueueIndex < 0 && mPlaylist.isEmpty()) {
                // Nothing to play.
                return;
            }

            String mediaId = mPlaylist.get(mQueueIndex).getDescription().getMediaId();
            mPreparedMedia = mMyApplication.getTreeMap().get(mediaId);
            mSession.setMetadata(mPreparedMedia);

            if (!mSession.isActive()) {
                mSession.setActive(true);
            }
        }

        @Override
        public void onPlay() {

            if (!isReadyToPlay()) {
                // Nothing to play.
                return;
            }

            if (mPreparedMedia == null) {
                onPrepare();
            }

            mPlayback.playFromMedia(mPreparedMedia);
            mMyPreferenceManager.saveQueuePosition(mQueueIndex);
            mMyPreferenceManager.saveLastPlayedMedia(mPreparedMedia.getDescription().getMediaId());
        }

        @Override
        public void onPause() {
            mPlayback.pause();
        }

        @Override
        public void onStop() {
            mPlayback.stop();
            mSession.setActive(false);
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "onSkipToNext: SKIP TO NEXT");
            // increment and then check using modulus
            mQueueIndex = (++mQueueIndex % mPlaylist.size());
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            Log.d(TAG, "onSkipToPrevious: SKIP TO PREVIOUS");
            mQueueIndex = mQueueIndex > 0 ? mQueueIndex - 1 : mPlaylist.size() - 1;
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.seekTo(pos);
        }

        private boolean isReadyToPlay() {
            return (!mPlaylist.isEmpty());
        }
    }


    public class MediaPlayerListener implements PlaybackInfoListener {

        private final ServiceManager mServiceManager;

        MediaPlayerListener() {
            mServiceManager = new ServiceManager();
        }

        @Override
        public void updateUI(String newMediaId) {
            Log.d(TAG, "updateUI: CALLED: " + newMediaId);
            Intent intent = new Intent();
            intent.setAction(getString(R.string.broadcast_update_ui));
            intent.putExtra(getString(R.string.broadcast_new_media_id), newMediaId);
            sendBroadcast(intent);
        }

        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            // Report the state to the MediaSession.
            mSession.setPlaybackState(state);


            // Manage the started state of this service.
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mServiceManager.updateNotification(
                            state,
                            mPlayback.getCurrentMedia().getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI)
                    );
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mServiceManager.updateNotification(
                            state,
                            mPlayback.getCurrentMedia().getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI)
                    );
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    Log.d(TAG, "onPlaybackStateChange: STOPPED.");
                    mServiceManager.moveServiceOutOfStartedState();
                    break;
            }
        }

        @Override
        public void onSeekTo(long progress, long max) {
//            Log.d(TAG, "onSeekTo: CALLED: updating seekbar: " + progress + ", max: " + max);
            Intent intent = new Intent();
            intent.setAction(getString(R.string.broadcast_seekbar_update));
            intent.putExtra(SEEK_BAR_PROGRESS, progress);
            intent.putExtra(SEEK_BAR_MAX, max);
            sendBroadcast(intent);
        }

        @Override
        public void onPlaybackComplete() {
            Log.d(TAG, "onPlaybackComplete: SKIPPING TO NEXT.");
            mSession.getController().getTransportControls().skipToNext();
        }


        class ServiceManager implements InitCallback {

            private String mDisplayImageUri;
            private Bitmap mCurrentArtistBitmap;
            private PlaybackStateCompat mState;
            private GetArtistBitmapAsyncTask mAsyncTask;

            public ServiceManager() {
            }

            public void updateNotification(PlaybackStateCompat state, String displayImageUri){
                mState = state;

                if(!displayImageUri.equals(mDisplayImageUri)){
                    // download new bitmap

                    mAsyncTask = new GetArtistBitmapAsyncTask(
                            Glide.with(MusicService.this)
                                    .asBitmap()
                                    .load(displayImageUri)
                                    .listener(new RequestListener<Bitmap>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                            return false;
                                        }
                                    }).submit(), this);

                    mAsyncTask.execute();

                    mDisplayImageUri = displayImageUri;
                }
                else{
                    displayNotification(mCurrentArtistBitmap);
                }
            }

            public void displayNotification(Bitmap bitmap){

                // Manage the started state of this service.
                Notification notification = null;
                switch (mState.getState()) {

                    case PlaybackStateCompat.STATE_PLAYING:
                        notification =
                                mMediaNotificationManager.buildNotification(
                                        mState, getSessionToken(), mPlayback.getCurrentMedia().getDescription(), bitmap);

                        if (!mIsServiceStarted) {
                            ContextCompat.startForegroundService(
                                    MusicService.this,
                                    new Intent(MusicService.this, MusicService.class));
                            mIsServiceStarted = true;
                        }

                        startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
                        break;

                    case PlaybackStateCompat.STATE_PAUSED:
                        stopForeground(false);
                        notification =
                                mMediaNotificationManager.buildNotification(
                                        mState, getSessionToken(), mPlayback.getCurrentMedia().getDescription(), bitmap);
                        mMediaNotificationManager.getNotificationManager()
                                .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
                        break;
                }
            }

            private void moveServiceOutOfStartedState() {
                stopForeground(true);
                stopSelf();
                mIsServiceStarted = false;
            }

            @Override
            public void getBitmap(Bitmap bitmap) {
                mCurrentArtistBitmap = bitmap;
                displayNotification(mCurrentArtistBitmap);
            }
        }

    }

    static class GetArtistBitmapAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        private FutureTarget<Bitmap> mBitmap;
        private InitCallback mICallback;


        public GetArtistBitmapAsyncTask(FutureTarget<Bitmap> bitmap, InitCallback iCallback) {
            this.mBitmap = bitmap;
            this.mICallback = iCallback;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mICallback.getBitmap(bitmap);
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {

            try {
                return mBitmap.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
