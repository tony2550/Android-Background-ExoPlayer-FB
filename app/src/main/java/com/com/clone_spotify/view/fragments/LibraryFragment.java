package com.com.clone_spotify.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.com.clone_spotify.R;
import com.com.clone_spotify.adapters.LibraryAdapter;
import com.com.clone_spotify.view.InitMainActivity;
import com.com.clone_spotify.view.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class LibraryFragment extends Fragment implements LibraryAdapter.IMediaSelector{
    
    private static final String TAG = "LibraryFragment";

    private RecyclerView reLibrary;
    private LibraryAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private MediaMetadataCompat mSelectedMedia;
    private InitMainActivity mIMainActivity;

    public LibraryFragment() {

    }
//    private LibraryFragment mContext = LibraryFragment.this;

    public static LibraryFragment newInstance(){
        return new LibraryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: 온크레이트");
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_library, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        reLibrary = view.findViewById(R.id.reLibrary);
        layoutManager = new LinearLayoutManager(getActivity());

        reLibrary.setLayoutManager(layoutManager);

        mAdapter = new LibraryAdapter(getActivity(), mMediaList, this);
        reLibrary.setAdapter(mAdapter);
        if(mMediaList.size() == 0){
            retrieveMedia();
        }

        if(savedInstanceState != null){
            mAdapter.setSelectedIndex(savedInstanceState.getInt("selected_index"));
        }
    }

    private void getSelectedMediaItem(String mediaId){
        for(MediaMetadataCompat mediaItem: mMediaList){
            if(mediaItem.getDescription().getMediaId().equals(mediaId)){
                mSelectedMedia = mediaItem;
                mAdapter.setSelectedIndex(mAdapter.getIndexOfItem(mSelectedMedia));
                break;
            }
        }
    }


    @Override
    public void onMediaSelected(int position) {
        mIMainActivity.getMyApplicationInstance().setMediaItems(mMediaList);
        mSelectedMedia = mMediaList.get(position);
        mAdapter.setSelectedIndex(position);
        mIMainActivity.onMediaSelected(
                mMediaList.get(position),
                position);
        saveLastPlayedSongProperties();
    }

    private void retrieveMedia(){
        mIMainActivity.showPrgressBar();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference colRef = db.collection("songs");

        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@android.support.annotation.NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document: task.getResult()){
                        addToMediaList(document);
                    }
                }
                else{
                    Log.d(TAG, "onComplete: error getting documents: " + task.getException());
                }
                updateDataSet();
            }
        });
    }

    private void addToMediaList(QueryDocumentSnapshot document) {
        MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, document.getString(getString(R.string.field_media_id)))
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, document.getString(getString(R.string.field_artist)))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, document.getString(getString(R.string.field_title)))
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, document.getString(getString(R.string.field_media_url)))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, document.getString(getString(R.string.field_description)))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, document.getString(getString(R.string.field_title)))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, document.getString(getString(R.string.field_image_url)))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, document.getString(getString(R.string.field_artist)))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, document.getString(getString(R.string.field_image_url)))
                .build();


        mMediaList.add(media);
    }

    private void updateDataSet(){
        mIMainActivity.hideProgressBar();
        mAdapter.notifyDataSetChanged();
//        getSelectedMediaItem(mIMainActivity.getMyPreferenceManager().getLastPlayedMedia());
    }

//    private void initRecyclerView(View view){
//        reLibrary = (RecyclerView) view.findViewById(R.id.reLibrary);
//        reLibrary.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mAdapter = new LibraryAdapter(getActivity(), mMediaList, this);
//        reLibrary.setAdapter(mAdapter);
//
//        if(mMediaList.size() == 0){
//            retrieveMedia();
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMainActivity = (InitMainActivity) getActivity();
    }


    public void updateUI(MediaMetadataCompat mediaItem){
        mAdapter.setSelectedIndex(mAdapter.getIndexOfItem(mediaItem));
        mSelectedMedia = mediaItem;
//        saveLastPlayedSongProperties();
    }

    private void saveLastPlayedSongProperties(){
        // Save some properties for next time the app opens
        // NOTE: Normally you'd do this with a cache
//        mIMainActivity.getMyPreferenceManager().saveLastPlayedMedia(mSelectedMedia.getDescription().getMediaId());
    }

    @Override
    public void onSaveInstanceState(@android.support.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_index", mAdapter.getSelectedIndex());
    }
}