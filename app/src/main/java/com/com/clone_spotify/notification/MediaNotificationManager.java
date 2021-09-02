package com.com.clone_spotify.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.session.MediaSession;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.com.clone_spotify.R;
import com.com.clone_spotify.service.MusicService;
import com.com.clone_spotify.view.MainActivity;

public class MediaNotificationManager {

    private static final String TAG = "MediaNotification";

    private final MusicService mMusicService;
    private final NotificationManager mNotificationManager;
    private static final String CHANNEL_ID = "com.com.clone_spotify.musicService.channel";
    private static final int REQUEST_CODE = 101;
    public static final int NOTIFICATION_ID = 201;

    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauseAction;
    private final NotificationCompat.Action mNextAction;
    private final NotificationCompat.Action mPrevAction;

    public MediaNotificationManager(MusicService musicService) {
        mMusicService = musicService;


        mNotificationManager = (NotificationManager) mMusicService.getSystemService(Context.NOTIFICATION_SERVICE);

        mPlayAction =
                new NotificationCompat.Action(
                        R.drawable.ic_play_arrow_24dp,
                        mMusicService.getString(R.string.label_play),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mMusicService,
                                PlaybackStateCompat.ACTION_PLAY));
        mPauseAction =
                new NotificationCompat.Action(
                        R.drawable.ic_pause_24dp,
                        mMusicService.getString(R.string.label_pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mMusicService,
                                PlaybackStateCompat.ACTION_PAUSE));
        mNextAction =
                new NotificationCompat.Action(
                        R.drawable.ic_skip_next_24dp,
                        mMusicService.getString(R.string.label_next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mMusicService,
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        mPrevAction =
                new NotificationCompat.Action(
                        R.drawable.ic_skip_previous_24dp,
                        mMusicService.getString(R.string.label_previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mMusicService,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        // cancel all previously shown notifications
        mNotificationManager.cancelAll();
    }

    public NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    // Does nothing on versions of Android earlier than O.
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                // The user-visible name of the channel.
                CharSequence name = "MediaSession";
                // The user-visible description of the channel.
                String description = "MediaSession and MediaPlayer";
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                // Configure the notification channel.
                mChannel.setDescription(description);
                mChannel.enableLights(true);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                mChannel.setLightColor(Color.RED);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(
                        new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mNotificationManager.createNotificationChannel(mChannel);
                Log.d(TAG, "createChannel: New channel created");
            } else {
                Log.d(TAG, "createChannel: Existing channel reused");
            }
        }
    }

    private boolean isAndroidOOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }


    public Notification buildNotification(PlaybackStateCompat state,
                                          MediaSessionCompat.Token token,
//                                           boolean isPlaying,
                                          final MediaDescriptionCompat description,
                                          Bitmap bitmap) {

        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;

        // Create the (mandatory) notification channel when running on Android Oreo.
        if (isAndroidOOrHigher()) {
            createChannel();
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(mMusicService,CHANNEL_ID);
        builder.setStyle(
                new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token)
                        .setShowActionsInCompactView(0, 1, 2)
        )
                .setColor(ContextCompat.getColor(mMusicService, R.color.notification_bg))
                .setSmallIcon(R.drawable.ic_audiotrack_grey_24dp)
                // Pending intent that is fired when user clicks on notification.
                .setContentIntent(createContentIntent())
                // Title - Usually Song name.
                .setContentTitle(description.getTitle())
                // Subtitle - Usually Artist name.
                .setContentText(description.getSubtitle())
                .setLargeIcon(bitmap)
                // When notification is deleted (when playback is paused and notification can be
                // deleted) fire MediaButtonPendingIntent with ACTION_STOP.
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mMusicService, PlaybackStateCompat.ACTION_STOP))
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // If skip to previous action is enabled.
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            builder.addAction(mPrevAction);
        }

        builder.addAction(isPlaying ? mPauseAction : mPlayAction);

        // If skip to next action is enabled.
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            builder.addAction(mNextAction);
        }

        return builder.build();
    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mMusicService, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                mMusicService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
