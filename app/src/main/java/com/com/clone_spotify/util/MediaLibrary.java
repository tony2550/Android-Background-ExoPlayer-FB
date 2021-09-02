package com.com.clone_spotify.util;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class MediaLibrary {

    private static final String TAG = "MediaLibrary";

    public TreeMap<String, MediaMetadataCompat> mMediaMap = new TreeMap<>();
    public List<MediaMetadataCompat> mMediaList = new ArrayList<>();

    //TreeMap , List 초기화 컨스트럭터
    public MediaLibrary() {
        initMap();
    }

    private void initMap(){
        for(MediaMetadataCompat media : mMediaLibrary){
            String mediaId = media.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            mMediaMap.put(mediaId, media);
            mMediaList.add(media);
        }
    }

    public static List<MediaBrowserCompat.MediaItem> getPlaylistMedia(Set<String> mediaIds) {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();

        // VERY INEFFICIENT WAY TO DO THIS (BUT I NEED TO BECAUSE THE DATA STRUCTURE ARE NOT IDEAL)
        // RETRIEVING DATA FROM A SERVER WOULD NOT POSE THIS ISSUE
        for(String id: mediaIds){
            for (MediaMetadataCompat metadata : mMediaLibrary) {
                if(id.equals(metadata.getDescription().getMediaId())){
                    result.add(
                            new MediaBrowserCompat.MediaItem(
                                    metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
                }
            }
        }
        return result;
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (MediaMetadataCompat metadata : mMediaLibrary) {
            result.add(
                    new MediaBrowserCompat.MediaItem(
                            metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }

    public TreeMap<String, MediaMetadataCompat> getTreeMap(){
        return mMediaMap;
    }

    public MediaMetadataCompat[] getMediaLibrary(){
        return mMediaLibrary;
    }

    private static final MediaMetadataCompat[] mMediaLibrary = {
            new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11111")
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Post Malone")
                    .putString(
                            MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                            "https://firebasestorage.googleapis.com/v0/b/clone-spotify-4a15b.appspot.com/o/Post%20Malone%20-%20A%20Thousand%20Bad%20Times.jpeg?alt=media&token=1ea92288-8846-4b6b-8b98-f8042efb2dc8")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "A Thousand Bad Times")
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                            "https://firebasestorage.googleapis.com/v0/b/clone-spotify-4a15b.appspot.com/o/Post%20Malone%20-%20A%20Thousand%20Bad%20Times.mp3?alt=media&token=8481cb53-1c9b-494a-9fe2-6a08aa0fbf19")
                    .build(),

            new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11112")
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Post Malone")
                    .putString(
                            MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                            "https://firebasestorage.googleapis.com/v0/b/clone-spotify-4a15b.appspot.com/o/Post%20Malone%20-%20Circles.jpeg?alt=media&token=c76e1573-e0d3-47cf-80fc-8992f2dc01c3")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                            "Circles")
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                            "https://firebasestorage.googleapis.com/v0/b/clone-spotify-4a15b.appspot.com/o/Post%20Malone%20-%20Circles.mp3?alt=media&token=024b1189-48a8-469a-912b-240f46448e58")
                    .build(),

            new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11113")
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Post Malone")
                    .putString(
                            MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                            "https://firebasestorage.googleapis.com/v0/b/clone-spotify-4a15b.appspot.com/o/Post%20Malone%20-%20Congratulations%20ft.%20Quavo.png?alt=media&token=09580ff4-3fc0-4106-b61e-ca429e0bc1a0")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                            "Congratulations ft. Quavo")
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                            "https://firebasestorage.googleapis.com/v0/b/clone-spotify-4a15b.appspot.com/o/Post%20Malone%20-%20Congratulations%20ft.%20Quavo.mp3?alt=media&token=3b19f374-7c72-4215-b13d-d474855641c6")
                    .build(),

            new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11114")
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Post Malone, Swae Lee")
                    .putString(
                            MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                            "https://firebasestorage.googleapis.com/v0/b/clone-spotify-4a15b.appspot.com/o/Post%20Malone%2C%20Swae%20Lee%20-%20Sunflower%20(Spider-Man%20Into%20the%20Spider-Verse).jpeg?alt=media&token=34dc1050-4bd1-4416-b5b3-b221f54d8f29")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                            "Sunflower (Spider-Man Into the Spider-Verse)")
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                            "https://firebasestorage.googleapis.com/v0/b/clone-spotify-4a15b.appspot.com/o/Post%20Malone%2C%20Swae%20Lee%20-%20Sunflower%20(Spider-Man%20Into%20the%20Spider-Verse).mp3?alt=media&token=f12e079a-a675-4501-bdc3-558896531771")
                    .build(),


    };
}
