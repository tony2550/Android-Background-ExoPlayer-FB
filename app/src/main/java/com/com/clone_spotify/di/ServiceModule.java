package com.com.clone_spotify.di;

import android.app.Application;

import com.com.clone_spotify.repository.MusicDatabase;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ServiceComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.android.scopes.ServiceScoped;

@Module // 모듈입니다!
@InstallIn(ServiceComponent.class) // 생명주기 입력
public class ServiceModule {

    @ServiceScoped
    @Provides
    static MusicDatabase provideMusicDatabase() {
        return new MusicDatabase();
    }

    @ServiceScoped // service 에서만 쓸 싱글톤 손들기
    @Provides
    static AudioAttributes provideAudioAttributes(){
        return new AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC) // 우리는 앱에서 음악을 재생할꺼다
                .setUsage(C.USAGE_MEDIA) // 미디어 용도
                .build();
    }

    @ServiceScoped
    @Provides
    static SimpleExoPlayer provideExoPlayer(@ApplicationContext Application application, AudioAttributes audioAttributes) {
        SimpleExoPlayer simpleExoPlayer = new SimpleExoPlayer.Builder(application)
                .build();
        simpleExoPlayer.setAudioAttributes(audioAttributes,true);
        simpleExoPlayer.setHandleAudioBecomingNoisy(true);
        return simpleExoPlayer;
    }

    @ServiceScoped
    @Provides
    static DataSource.Factory provideDataSourceFactory(@ApplicationContext Application application) {
        return new DefaultDataSourceFactory(application, Util.getUserAgent(application, "Clone Spotify"));
    }

}
