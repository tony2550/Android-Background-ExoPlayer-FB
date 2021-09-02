package com.com.clone_spotify.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.com.clone_spotify.R;
import com.com.clone_spotify.SpotifyApplication;
import com.com.clone_spotify.client.MediaBrowserHelper;
import com.com.clone_spotify.client.MediaBrowserHelperCallback;
import com.com.clone_spotify.service.MusicService;
import com.com.clone_spotify.util.MainActivityFragmentManager;
import com.com.clone_spotify.util.MyPreferenceManager;
import com.com.clone_spotify.view.fragments.LibraryFragment;
import com.com.clone_spotify.view.fragments.MediaControllerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.com.clone_spotify.util.Constants.MEDIA_QUEUE_POSITION;
import static com.com.clone_spotify.util.Constants.QUEUE_NEW_PLAYLIST;
import static com.com.clone_spotify.util.Constants.SEEK_BAR_MAX;
import static com.com.clone_spotify.util.Constants.SEEK_BAR_PROGRESS;
// 안드로이드 컴포넌트(서비스 프레그먼트 포함)에 inject 하려면 필요한 어노테이션
public class MainActivity extends AppCompatActivity implements InitMainActivity, MediaBrowserHelperCallback {

    private static final String TAG = "MainActivity2";

    private ProgressBar mProgressBar;

    private MediaBrowserHelper mMediaBrowserHelper;
    private SpotifyApplication mMyApplication;
    private MyPreferenceManager mMyPrefManager;
    private boolean mIsPlaying;
    private SeekBarBroadcastReceiver mSeekbarBroadcastReceiver;
    private UpdateUIBroadcastReceiver mUpdateUIBroadcastReceiver;
    private boolean mOnAppOpen;
    private boolean mWasConfigurationChange = false;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.progress_bar);

        mMyApplication = SpotifyApplication.getInstance();
        mMyPrefManager = new MyPreferenceManager(this);
        mMediaBrowserHelper = new MediaBrowserHelper(this, MusicService.class);
        mMediaBrowserHelper.setMediaBrowserHelperCallback(this);

        if(savedInstanceState == null){
            loadFragment(LibraryFragment.newInstance(), false);
        }
    }

    private void loadFragment(Fragment fragment, boolean lateralMovement){

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        String tag = getString(R.string.fragment_library);

        transaction.add(R.id.main_container, fragment, tag);
        transaction.commit();

        MainActivityFragmentManager.getInstance().addFragment(fragment);

        showFragment(fragment, false);
    }

    private void showFragment(Fragment fragment, boolean backswardsMovement) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.show(fragment);
        transaction.commit();

        for (Fragment f : MainActivityFragmentManager.getInstance().getFragments()) {
            if (f != null) {
                if (!f.getTag().equals(fragment.getTag())) {
                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                    t.hide(f);
                    t.commit();
                }
            }
        }
    }
    
    private void initLr() {

    }

    // 미디어 브라우저 연결
    private void init() {


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("active_fragments", MainActivityFragmentManager.getInstance().getFragments().size());
    }

    @Override // 미디어 컨트롤러 연결 (시크바에서 탐색기능 연결)
    public void onMediaControllerConnected(MediaControllerCompat mediaController) {
        getMediaControllerFragment().getMediaSeekBar().setMediaController(mediaController);
    }


    @Override
    protected void onResume() {
        super.onResume();
        initSeekBarBroadcastReceiver();
        initUpdateUIBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mSeekbarBroadcastReceiver != null){
            unregisterReceiver(mSeekbarBroadcastReceiver);
        }
        if(mUpdateUIBroadcastReceiver != null){
            unregisterReceiver(mUpdateUIBroadcastReceiver);
        }
    }

    private class SeekBarBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long seekProgress = intent.getLongExtra(SEEK_BAR_PROGRESS, 0);
            long seekMax = intent.getLongExtra(SEEK_BAR_MAX, 0);
            if(!getMediaControllerFragment().getMediaSeekBar().isTracking()){
                getMediaControllerFragment().getMediaSeekBar().setProgress((int)seekProgress);
                getMediaControllerFragment().getMediaSeekBar().setMax((int)seekMax);
            }
        }
    }

    private void initSeekBarBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_seekbar_update));
        mSeekbarBroadcastReceiver = new SeekBarBroadcastReceiver();
        registerReceiver(mSeekbarBroadcastReceiver, intentFilter);
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        Log.d(TAG, "onMetadataChanged: called");
        if(metadata == null){
            return;
        }

        // Do stuff with new Metadata
        getMediaControllerFragment().setMediaInfo(metadata);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged: called.");
        mIsPlaying = state != null &&
                state.getState() == PlaybackStateCompat.STATE_PLAYING;

        // update UI
        if(getMediaControllerFragment() != null){
            getMediaControllerFragment().setIsPlaying(mIsPlaying);
        }
    }

    private LibraryFragment getPlaylistFragment(){
        LibraryFragment playlistFragment = (LibraryFragment)getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.fragment_library));
        if(playlistFragment != null){
            return playlistFragment;
        }
        return null;
    }

    private MediaControllerFragment getMediaControllerFragment(){
        MediaControllerFragment mediaControllerFragment = (MediaControllerFragment)getSupportFragmentManager()
                .findFragmentById(R.id.bottom_media_controller);
        if(mediaControllerFragment != null){
            return mediaControllerFragment;
        }
        return null;
    }

    @Override
    public void onMediaSelected(MediaMetadataCompat mediaItem, int queuePosition) {
        if(mediaItem != null){
            Log.d(TAG, "onMediaSelected: CALLED: " + mediaItem.getDescription().getMediaId());

            String currentPlaySongId = getMyPreferenceManager().getLastPlayedMedia();

            Bundle bundle = new Bundle();
            bundle.putInt(MEDIA_QUEUE_POSITION, queuePosition);
            if(mediaItem.getDescription().getMediaId().equals(currentPlaySongId)){
                mMediaBrowserHelper.getTransportControls().playFromMediaId(mediaItem.getDescription().getMediaId(), bundle);
            }
            else{
                bundle.putBoolean(QUEUE_NEW_PLAYLIST, true); // let the player know this is a new playlist
                mMediaBrowserHelper.subscribeToNewSong(mediaItem.getDescription().getMediaId(), mediaItem.getDescription().getMediaId());
                mMediaBrowserHelper.getTransportControls().playFromMediaId(mediaItem.getDescription().getMediaId(), bundle);
            }

            mOnAppOpen = true;
        }
        else{
            Toast.makeText(this, "select something to play", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void showPrgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override // 마지막 재생 음악 저장 로직
    public void playPause() {
        if(mOnAppOpen){
            if (mIsPlaying) {
                mMediaBrowserHelper.getTransportControls().pause();
            }
            else {
                mMediaBrowserHelper.getTransportControls().play();

            }
        } else {
            if(!getMyPreferenceManager().getLastPlayedMedia().equals("")){
            onMediaSelected(
                        mMyApplication.getMediaItem(getMyPreferenceManager().getLastPlayedMedia()),
                        getMyPreferenceManager().getQueuePosition());
            } else {
                Toast.makeText(this, "select something to play", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override // 구성 변경 처리
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWasConfigurationChange = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!getMyPreferenceManager().getLastPlayedMedia().equals("")){
            prepareLastPlayedMedia();
        }
        else{
            mMediaBrowserHelper.onStart(mWasConfigurationChange);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaBrowserHelper.onStop();
        getMediaControllerFragment().getMediaSeekBar().disconnectController();
    }

    // 브로드캐스팅 UI 연동====
    private class UpdateUIBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String newMediaId = intent.getStringExtra(getString(R.string.broadcast_new_media_id));
            Log.d(TAG, "onReceive: CALLED: " + newMediaId);
            if (getPlaylistFragment() != null) {
                Log.d(TAG, "onReceive: " + mMyApplication.getMediaItem(newMediaId).getDescription().getMediaId());
                getPlaylistFragment().updateUI(mMyApplication.getMediaItem(newMediaId));
            }
        }
    }

    private void initUpdateUIBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_update_ui));
        mUpdateUIBroadcastReceiver = new UpdateUIBroadcastReceiver();
        registerReceiver(mUpdateUIBroadcastReceiver, intentFilter);
    }
    // 브로드캐스팅 UI 연동====

    // 마지막에 튼 노래 연동
    private void prepareLastPlayedMedia(){
        showPrgressBar();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference colRef = db.collection("songs");

        final List<MediaMetadataCompat> mediaItems = new ArrayList<>();
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@android.support.annotation.NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        MediaMetadataCompat mediaItem = addToMediaList(document);
                        mediaItems.add(mediaItem);
                        if(mediaItem.getDescription().getMediaId().equals(getMyPreferenceManager().getLastPlayedMedia())){
                            getMediaControllerFragment().setMediaInfo(mediaItem);
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
                onFinishedGettingPreviousSessionData(mediaItems);
            }
        });
    }

    private void onFinishedGettingPreviousSessionData(List<MediaMetadataCompat> mediaItems){
        mMyApplication.setMediaItems(mediaItems);
        mMediaBrowserHelper.onStart(mWasConfigurationChange);
        hideProgressBar();
    }


    private MediaMetadataCompat addToMediaList(QueryDocumentSnapshot document){

        MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, document.getString(getString(R.string.field_media_id)))
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, document.getString(getString(R.string.field_artist)))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, document.getString(getString(R.string.field_title)))
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, document.getString(getString(R.string.field_media_url)))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, document.getString(getString(R.string.field_description)))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, document.getString(getString(R.string.field_title)))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, document.getString(getString(R.string.field_image_url)))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, document.getString(getString(R.string.field_artist)))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, getMyPreferenceManager().getLastPlayedArtistImage())
                .build();

        return media;
    }
    // 파이어스토어 연동 ===

    @Override
    public SpotifyApplication getMyApplicationInstance() {
        return mMyApplication;
    }


    @Override
    public MyPreferenceManager getMyPreferenceManager() {
        return mMyPrefManager;
    }


}