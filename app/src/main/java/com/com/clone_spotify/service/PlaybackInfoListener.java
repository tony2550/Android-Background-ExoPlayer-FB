package com.com.clone_spotify.service;

import android.support.v4.media.session.PlaybackStateCompat;

public abstract class PlaybackInfoListener {

    public abstract void onPlaybackStateChange(PlaybackStateCompat state);

    public void onPlaybackCompleted() {

    };

//    void onSeekTo(long progress, long max);
//
//    void updateUI(String mediaId);
}
