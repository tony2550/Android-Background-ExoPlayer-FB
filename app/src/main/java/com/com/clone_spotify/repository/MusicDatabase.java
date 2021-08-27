package com.com.clone_spotify.repository;

import android.util.Log;

import com.com.clone_spotify.repository.dto.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MusicDatabase {

    private static final String TAG = "MusicDatabase";

    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference songs = db.collection("songs");

    public List<Song> getAllSongs(){

        List<Song> songList = new ArrayList<>();

        songs.get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Song song = document.toObject(Song.class);
                            songList.add(song);
                        }

                        Log.d(TAG, "onComplete: "+songList.get(1).toString());
                        } else {
                        Log.d(TAG, "onComplete: fail");
                    }
            }
            });
     return songList;
    }
}
