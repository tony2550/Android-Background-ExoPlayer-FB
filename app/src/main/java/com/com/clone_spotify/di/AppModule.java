package com.com.clone_spotify.di;

import android.app.Application;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.com.clone_spotify.R;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    //dagger hilt에게 어떤 field가 필요한지 알려줘야한다 어노테이션으로

    @Singleton
    @Provides
    public static RequestOptions provideRequestOptions() {
        return RequestOptions
                .placeholderOf(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .diskCacheStrategy(DiskCacheStrategy.DATA);
    }

    @Singleton
    @Provides
    public static RequestManager provideGlideInstance(@ApplicationContext Context context, RequestOptions requestOptions) {
        return Glide.with(context)
                .setDefaultRequestOptions(requestOptions);
    }

}
