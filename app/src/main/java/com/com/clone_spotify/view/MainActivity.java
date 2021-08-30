package com.com.clone_spotify.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.com.clone_spotify.R;
import com.com.clone_spotify.SpotifyApplication;
import com.com.clone_spotify.adapters.PlaylistRecyclerAdapter;
import com.com.clone_spotify.adapters.SwipeAdapter;
import com.com.clone_spotify.service.MediaBrowserHelper;
import com.com.clone_spotify.service.MediaBrowserHelperCallback;
import com.com.clone_spotify.service.MusicLibrary;
import com.com.clone_spotify.service.MusicService;
import com.com.clone_spotify.view.fragments.HomeFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint // 안드로이드 컴포넌트(서비스 프레그먼트 포함)에 inject 하려면 필요한 어노테이션
public class MainActivity extends AppCompatActivity implements InitActivity, MediaBrowserHelperCallback {

    private static final String TAG = "MainActivity2";

    private MediaBrowserHelper mMediaBrowserHelper;
    private SpotifyApplication mMyApplication;
    private boolean mIsPlaying;
    private boolean mOnAppOpen;
    private boolean mWasConfigurationChange = false;

    private List<MediaMetadataCompat> swipeSongList = new ArrayList<>();

    private ViewPager2 ivSong;
    private ImageView ivCurSongImage, ivPlayPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        mMyApplication = SpotifyApplication.getInstance();

        mMediaBrowserHelper = new MediaBrowserHelper(this, MusicService.class);
        mMediaBrowserHelper.registerCallback(new MediaBrowserListener());


    }
    
    private void initLr() {
        ivPlayPause.setOnClickListener(v -> {
            if (mIsPlaying) {
                mMediaBrowserHelper.getTransportControls().pause();
            } else {
                mMediaBrowserHelper.getTransportControls().play();
            }
        });
    }
    
    private void init() {
        ivPlayPause = findViewById(R.id.ivPlayPause);
        ivCurSongImage = findViewById(R.id.ivCurSongImage);
        ivSong = findViewById(R.id.vpSong);
        ivSong.setAdapter(new SwipeAdapter(swipeSongList,this));
    }

    public void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();
    }

    public void onStop() {
        super.onStop();
        mMediaBrowserHelper.onStop();
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {

    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {

    }

    @Override
    public void onMediaControllerConnected(MediaControllerCompat mediaController) {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void showPrgressBar() {

    }

    @Override
    public void onCategorySelected(String category) {

    }

    @Override
    public void setActionBarTitle(String title) {

    }

    @Override
    public void playPause() {

    }

    @Override
    public SpotifyApplication getMyApplicationInstance() {
        return null;
    }

    @Override
    public void onMediaSelected(String playlistId, MediaMetadataCompat mediaItem, int position) {

    }

    private class MediaBrowserConnection extends MediaBrowserHelper {
        private MediaBrowserConnection(Context context) {
            super(context, MusicService.class);
        }

        @Override
        protected void onConnected(MediaControllerCompat mediaController) {

        }

        @Override
        protected void onChildrenLoaded(String parentId,
                                        List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

            final MediaControllerCompat mediaController = getMediaController();

            // Queue up all media items for this simple sample.
            for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                mediaController.addQueueItem(mediaItem.getDescription());
            }

            // Call prepare now so pressing play just works.
            mediaController.getTransportControls().prepare();
        }
    }

    private class MediaBrowserListener extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            mIsPlaying = playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
            ivPlayPause.setPressed(mIsPlaying);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {
                return;
            }

            int newItemIndex = swipeSongList.indexOf(mediaMetadata);
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.ic_launcher_background);
//            ivCurSongImage.setText(
//                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
//            mArtistTextView.setText(
//                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            ivSong.setCurrentItem(newItemIndex);
            Glide.with(ivCurSongImage.getContext())
                    .setDefaultRequestOptions(requestOptions)
                    .load(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI))
                    .into(ivCurSongImage);

        }
    }
}