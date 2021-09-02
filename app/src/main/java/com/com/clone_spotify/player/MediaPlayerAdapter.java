package com.com.clone_spotify.player;


import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;


import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.jetbrains.annotations.NotNull;


public class MediaPlayerAdapter extends PlayerAdapter{

    private static final String TAG = "MediaPlayerAdapter";

    private final Context mContext;
    private MediaMetadataCompat mCurrentMedia;
    private boolean mCurrentMediaPlayedToCompletion;
    private int mState;
    private long mStartTime;
    private PlaybackInfoListener mPlaybackInfoListener;

    private SimpleExoPlayer exoPlayer;
    private TrackSelector mTrackSelector;
    private DefaultRenderersFactory mRenderersFactory;
    private DataSource.Factory mDataSourceFactory;
    private AudioAttributes audioAttributes;
    private LoadControl mLoadControl;
    private ExoPlayerEventListener mExoPlayerEventListener;


    public MediaPlayerAdapter(Context context, PlaybackInfoListener playbackInfoListener) {
        super(context);
        mContext = context.getApplicationContext();
        mPlaybackInfoListener = playbackInfoListener;
    }

    private void initializeExoPlayer() {
        if (exoPlayer == null) {
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build();

            mTrackSelector = new DefaultTrackSelector(mContext);
            mRenderersFactory = new DefaultRenderersFactory(mContext);

            exoPlayer = new SimpleExoPlayer.Builder(mContext,mRenderersFactory)
                    .setTrackSelector(mTrackSelector)
                    .build();
            exoPlayer.setAudioAttributes(audioAttributes,true);
            exoPlayer.setHandleAudioBecomingNoisy(true);

//            mTrackSelector = new DefaultTrackSelector(mContext);
//            mRenderersFactory = new DefaultRenderersFactory(mContext);
//            mLoadControl = new DefaultLoadControl();
//            exoPlayer = new SimpleExoPlayer.Builder(mContext,mRenderersFactory)
//                    .setTrackSelector(mTrackSelector)
//                    .setLoadControl(mLoadControl)
//                    .build();

            if (mExoPlayerEventListener == null) {
                mExoPlayerEventListener = new ExoPlayerEventListener();
            }
            exoPlayer.addListener(mExoPlayerEventListener);
        }
    }

    private void release() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    // 콜백 메소드 override
    @Override
    protected void onPlay() {
        exoPlayer.setPlayWhenReady(true);
        setNewState(PlaybackStateCompat.STATE_PLAYING);

    }

    @Override
    protected void onPause() {
        exoPlayer.setPlayWhenReady(false);
        setNewState(PlaybackStateCompat.STATE_PAUSED);
    }

    @Override
    public void playFromMedia(MediaMetadataCompat metadata) {
        startTrackingPlayback();
        playFile(metadata);
    }

    @Override
    public MediaMetadataCompat getCurrentMedia() {
        return mCurrentMedia;
    }

    @Override
    public boolean isPlaying() {
        return exoPlayer != null && exoPlayer.getPlayWhenReady();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: stop");
        setNewState(PlaybackStateCompat.STATE_STOPPED);
        exoPlayer.release();
    }

    @Override
    public void seekTo(long position) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(position);
            setNewState(mState);
        }
    }

    @Override
    public void setVolume(float volume) {
        if (exoPlayer != null) {
            exoPlayer.setVolume(volume);
        }
    }

    private void playFile(MediaMetadataCompat metaData) {
        String mediaId = metaData.getDescription().getMediaId();
        boolean mediaChanged = (mCurrentMedia == null || !mediaId.equals(mCurrentMedia.getDescription().getMediaId()));
        if (mCurrentMediaPlayedToCompletion) {
            // 마지막 음악이 끝나고 플레이어 해제 되었으니까 다시 강제로 로드!
            mediaChanged = true;
            mCurrentMediaPlayedToCompletion = false;
        }
        if (!mediaChanged) {
            if (!isPlaying()) {
                play();
            }
            return;
        } else {
            release();
        }

        mCurrentMedia = metaData;
        initializeExoPlayer();
        try {
            mDataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "Clone Spotify"));
            MediaSource audioSource =
                    new ProgressiveMediaSource.Factory(mDataSourceFactory)
                            .createMediaSource(Uri.parse(metaData.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
            exoPlayer.prepare(audioSource);

//            MediaSource audioSource =
//                    new ExtractorMediaSource.Factory(mDataSourceFactory)
//                            .createMediaSource(Uri.parse(metaData.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
//            exoPlayer.prepare(audioSource);
            Log.d(TAG, "onPlayerStateChanged: prepare");
        } catch (Exception e) {
            throw new RuntimeException("Failed to play media uri: "
                    + metaData.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI), e);
        }
        onPlay();


    }

    // 수정예정
    public void startTrackingPlayback() {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying()) {
                    mPlaybackInfoListener.onSeekTo(
                            exoPlayer.getContentPosition(), exoPlayer.getDuration()
                    );
                    handler.postDelayed(this, 100);
                }
                if (exoPlayer.getContentPosition() >= exoPlayer.getDuration()
                        && exoPlayer.getDuration() > 0) {
                    mPlaybackInfoListener.onPlaybackComplete();
                }
            }
        };
        handler.postDelayed(runnable, 100);
    }

    // 플레이어 상태
    private void setNewState(@PlaybackStateCompat.State int newPlayerState) {
        mState = newPlayerState;

        if (mState == PlaybackStateCompat.STATE_STOPPED) {
            mCurrentMediaPlayedToCompletion = true;
        }
        // 서비스에 재생 상태 정보 전송
        final long reportPosition = exoPlayer == null ? 0 : exoPlayer.getCurrentPosition();
        publishStateBuilder(reportPosition);
    }

    private void publishStateBuilder(long reportPosition) {
        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(getAvailableActions());
        stateBuilder.setState(mState,
                reportPosition,
                1.0f,
                SystemClock.elapsedRealtime());
        mPlaybackInfoListener.onPlaybackStateChange(stateBuilder.build());
        mPlaybackInfoListener.updateUI(mCurrentMedia.getDescription().getMediaId());
    }

    @PlaybackStateCompat.Actions
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        switch (mState) {
            case PlaybackStateCompat.STATE_STOPPED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                actions |= PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO;
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP;
                break;
            default:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    private class ExoPlayerEventListener implements Player.EventListener {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_ENDED: {
                    setNewState(PlaybackStateCompat.STATE_PAUSED);
                    break;
                }
                case Player.STATE_BUFFERING: {
                    Log.d(TAG, "onPlayerStateChanged: BUFFERING");
                    mStartTime = System.currentTimeMillis();
                    break;
                }
                case Player.STATE_IDLE: {

                    break;
                }
                case Player.STATE_READY: {
                    Log.d(TAG, "onPlayerStateChanged: READY");
                    Log.d(TAG, "onPlayerStateChanged: TIME ELAPSED: " + (System.currentTimeMillis() - mStartTime));
                    break;
                }
            }
        }
        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }
    }

}
