package com.com.clone_spotify.exoplayer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.com.clone_spotify.R;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint // 인젝션 받을 객체로 손들기
public class MusicService extends MediaBrowserServiceCompat {

    private static final String SERVICE_TAG = "MusicService";

    @Inject // ServiceModule에서 선언한 덷이터소스팩토리 주입
    public DefaultDataSourceFactory dataSourceFactory;

    @Inject // ServiceModule 의 SimpleExoPlayer 주입
    public SimpleExoPlayer exoPlayer;

    private Context mContext = getApplicationContext();

    // 생명주기 설정 추가해야함

    private MediaSessionCompat mediaSession; // 미디어세션
    private MediaSessionConnector mediaSessionConnector; //

    @Override
    public void onCreate() {
        super.onCreate();
        Intent activityIntent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,activityIntent,0);

        mediaSession = new MediaSessionCompat(this, SERVICE_TAG);
        mediaSession.setSessionActivity(pendingIntent);

        setSessionToken(mediaSession.getSessionToken());
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(exoPlayer);

//        mediaSession.setCallback(new MediaSessionCompat.Callback() {
//            @Override
//            public void onPlay() {
//                super.onPlay();
//            }
//
//            @Override
//            public void onPause() {
//                super.onPause();
//            }
//
//            @Override
//            public void onStop() {
//                super.onStop();
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return null;
    }

    @Override // 여기서 parentId 는 노래리스트를 가져오기위한 id
    public void onLoadChildren(String parentId, Result<List<MediaBrowserCompat.MediaItem>> result) {

    }
}
