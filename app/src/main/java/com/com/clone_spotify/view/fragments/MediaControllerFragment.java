package com.com.clone_spotify.view.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.com.clone_spotify.MediaSeekBar;
import com.com.clone_spotify.R;
import com.com.clone_spotify.view.InitMainActivity;


public class MediaControllerFragment extends Fragment implements
        View.OnClickListener
{


    private static final String TAG = "MediaControllerFragment";


    // UI Components
    private TextView mSongTitle , mSongArtist;
    private ImageView mSongImage , mPlayPause;
    private MediaSeekBar mSeekBarAudio;


    // Vars
    private InitMainActivity mIMainActivity;
    private MediaMetadataCompat mSelectedMedia;
    private boolean mIsPlaying;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_controller, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mSongImage = view.findViewById(R.id.media_song_image);
        mSongTitle = view.findViewById(R.id.media_song_title);
        mSongArtist = view.findViewById(R.id.media_song_artist);
        mPlayPause = view.findViewById(R.id.play_pause);
        mSeekBarAudio = view.findViewById(R.id.seekbar_audio);

        mPlayPause.setOnClickListener(this);

        if(savedInstanceState != null){
            mSelectedMedia = savedInstanceState.getParcelable("selected_media");
            if(mSelectedMedia != null){
                setMediaInfo(mSelectedMedia);
                setIsPlaying(savedInstanceState.getBoolean("is_playing"));
            }
        }
    }

    public MediaSeekBar getMediaSeekBar(){
        return mSeekBarAudio;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.play_pause){
            mIMainActivity.playPause();
        }
    }

    public void setIsPlaying(boolean isPlaying){
        if(isPlaying){
            Glide.with(getActivity())
                    .load(R.drawable.ic_pause)
                    .into(mPlayPause);
        }
        else{
            Glide.with(getActivity())
                    .load(R.drawable.ic_play)
                    .into(mPlayPause);
        }
        mIsPlaying = isPlaying;
    }

    public void setMediaInfo(MediaMetadataCompat mediaItem){
        mSelectedMedia = mediaItem;
        Glide.with(getActivity())
                .load(mediaItem.getDescription().getIconUri())
                .into(mSongImage);
        mSongTitle.setText(mediaItem.getDescription().getTitle());
        mSongArtist.setText(mediaItem.getDescription().getSubtitle());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMainActivity = (InitMainActivity) getActivity();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("selected_media", mSelectedMedia);
        outState.putBoolean("is_playing", mIsPlaying);
    }
}

































