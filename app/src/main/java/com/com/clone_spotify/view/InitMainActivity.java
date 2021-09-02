package com.com.clone_spotify.view;

import android.support.v4.media.MediaMetadataCompat;

import com.com.clone_spotify.SpotifyApplication;
import com.com.clone_spotify.util.MyPreferenceManager;

public interface InitMainActivity {

    void hideProgressBar();

    void showPrgressBar();

    //void onCategorySelected(String category);

    //void onArtistSelected(String category, Artist artist);

    void setActionBarTitle(String title);

    void playPause();

    SpotifyApplication getMyApplicationInstance();

    void onMediaSelected(MediaMetadataCompat mediaItem, int position);

    MyPreferenceManager getMyPreferenceManager();
}
