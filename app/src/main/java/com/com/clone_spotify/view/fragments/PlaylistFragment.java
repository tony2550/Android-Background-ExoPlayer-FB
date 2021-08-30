package com.com.clone_spotify.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.com.clone_spotify.R;
import com.com.clone_spotify.adapters.PlaylistRecyclerAdapter;
import com.com.clone_spotify.model.Artist;
import com.com.clone_spotify.view.InitActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaylistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaylistFragment extends Fragment implements PlaylistRecyclerAdapter.IMediaSelector {
    private static final String TAG = "PlaylistFragment";
    private RecyclerView mRecyclerView;


    // Vars
    private PlaylistRecyclerAdapter mAdapter;
    private List<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private InitActivity mIMainActivity;
    private MediaMetadataCompat mSelectedMedia;
    private String mSelectedCategory;
    private Artist mSelectArtist;

    public PlaylistFragment() {
    }

    public static PlaylistFragment newInstance(String category, Artist artist) {
        PlaylistFragment playlistfragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putString("category", category);
        args.putParcelable("artist", artist);
        playlistfragment.setArguments(args);
        return playlistfragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSelectedCategory = getArguments().getString("category");
            mSelectArtist = getArguments().getParcelable("artist");
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMediaSelected(int position) {

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

//    private void retrieveMedia(){
//        mIMainActivity.showPrgressBar();
//
//        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//
//        Query query = firestore
//                .collection()
//
//        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if(task.isSuccessful()){
//                    for(QueryDocumentSnapshot document: task.getResult()){
//                        addToMediaList(document);
//                    }
//                }
//                else{
//
//                    Log.d(TAG, "onComplete: ");
//                }
//                updateDataSet();
//            }
//        });
//    }

//    private void addToMediaList(QueryDocumentSnapshot document) {
//        MediaMetadataCompat media = new MediaMetadataCompat.Builder()
//                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, document.getString(getString(R.string.field_media_id)))
//                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, document.getString(getString(R.string.field_artist)))
//                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, document.getString(getString(R.string.field_title)))
//                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, document.getString(getString(R.string.field_media_url)))
//                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, document.getString(getString(R.string.field_description)))
//                .putString(MediaMetadataCompat.METADATA_KEY_DATE, document.getDate(getString(R.string.field_date_added)).toString())
//                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, mSelectArtist.getImage())
//                .build();
//
//
//        mMediaList.add(media);
//    }

//    private void updateDataSet(){
//        mIMainActivity.hideProgressBar();
//        mAdapter.notifyDataSetChanged();
//        if(mIMainActivity.getMyPreferenceManager().getLastPlayedArtist().equals(mSelectArtist.getArtist_id())){
//            getSelectedMediaItem(mIMainActivity.getMyPreferenceManager().getLastPlayedMedia());
//        }
//    }
//
//    private void initRecyclerView(View view){
//        mRecyclerView = view.findViewById(R.id.recycler_view);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mAdapter = new PlaylistRecyclerAdapter(getActivity(), mMediaList, this);
//        mRecyclerView.setAdapter(mAdapter);
//
//        if(mMediaList.size() == 0){
//            retrieveMedia();
//        }
//    }


}