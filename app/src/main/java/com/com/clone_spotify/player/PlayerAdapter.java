package com.com.clone_spotify.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.v4.media.MediaMetadataCompat;

public abstract class PlayerAdapter {

    private static final float MEDIA_VOLUME_DEFAULT = 1.0f;
    private static final float MEDIA_VOLUME_DUCK = 0.2f;

    private static final IntentFilter AUDIO_NOISY_INTENT_FILTER =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private boolean mAudioNoisyReceiverRegistered = false;
    private final BroadcastReceiver mAudioNoisyReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                        if (isPlaying()) {
                            pause();
                        }
                    }
                }
            };

    private final Context mApplicationContext;
    private final AudioManager mAudioManager;

    private boolean mPlayOnAudioFocus = false;

    public PlayerAdapter(Context context) {
        mApplicationContext = context.getApplicationContext();
        mAudioManager = (AudioManager) mApplicationContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public abstract void playFromMedia(MediaMetadataCompat metadata);

    public abstract MediaMetadataCompat getCurrentMedia();

    public abstract boolean isPlaying();

    public final void play() {
            registerAudioNoisyReceiver();
            onPlay();
    }

    /**
     * Called when media is ready to be played and indicates the app has audio focus.
     */
    protected abstract void onPlay();

    public final void pause() {

        unregisterAudioNoisyReceiver();
        onPause();
    }

    /**
     * Called when media must be paused.
     */
    protected abstract void onPause();

    public final void stop() {
        unregisterAudioNoisyReceiver();
        onStop();
    }

    /**
     * Called when the media must be stopped. The player should clean up resources at this
     * point.
     */
    protected abstract void onStop();

    public abstract void seekTo(long position);

    public abstract void setVolume(float volume);

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mApplicationContext.registerReceiver(mAudioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mApplicationContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }

    }
}
