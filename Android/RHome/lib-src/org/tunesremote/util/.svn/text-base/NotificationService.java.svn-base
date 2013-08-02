/*
    TunesRemote+ - http://code.google.com/p/tunesremote-plus/
    
    Copyright (C) 2008 Jeffrey Sharkey, http://jsharkey.org/
    Copyright (C) 2010 TunesRemote+, http://code.google.com/p/tunesremote-plus/
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    The Initial Developer of the Original Code is Jeffrey Sharkey.
    Portions created by Jeffrey Sharkey are
    Copyright (C) 2008. Jeffrey Sharkey, http://jsharkey.org/
    All Rights Reserved.
 */
package org.tunesremote.util;

import org.tunesremote.BackendService;
import org.tunesremote.ControlActivity;
import org.tunesremote.R;
import org.tunesremote.daap.Session;
import org.tunesremote.daap.Status;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Author: Alex Curran Date: 12/07/2012 Time: 20:14
 */
public class NotificationService extends Service implements AudioManager.OnAudioFocusChangeListener {

   public static final String TAG = NotificationService.class.toString();
   public static final String ACTION_END_SERVICE = "end";
   public static final String ACTION_NEXT_TRACK = "next";
   public static final String ACTION_PAUSE = "pause";

   private BackendService backend;
   private Session session;
   private Status status;
   private NotificationHelper nHelper;
   private boolean bound = false;

   private Intent endServiceIntent;
   private Intent pauseIntent;
   private Intent nextTrackIntent;
   private Intent launchIntent;

   protected PendingIntent endServicePending;
   protected PendingIntent pausePending;
   protected PendingIntent nextTrackPending;
   protected PendingIntent launchPending;

   public ServiceConnection connection = new ServiceConnection() {
      public void onServiceConnected(ComponentName className, final IBinder service) {

         try {
            backend = ((BackendService.BackendBinder) service).getService();

            Log.w(TAG, "onServiceConnected for NotificationService");

            session = backend.getSession();
            if (session == null) {
               if (nHelper != null)
                  nHelper.killNotification();
               return;
            }
            // for some reason we are not correctly disposing of the
            // session threads we create so we purge any existing ones
            // before creating a new one
            status = session.notificationStatus(statusUpdate);
            status.updateHandler(statusUpdate);

            // push update through to make sure we get updated
            statusUpdate.sendEmptyMessage(Status.UPDATE_TRACK);
            bound = true;

         } catch (Throwable e) {
            Log.e(TAG, "onServiceConnected:" + e);
         }

      }

      public void onServiceDisconnected(ComponentName className) {
         // make sure we clean up our handler-specific status
         Log.w(TAG, "onServiceDisconnected");
         status.updateHandler(null);
         backend = null;
         status = null;
         bound = false;
      }
   };

   protected Handler statusUpdate = new Handler() {

      @Override
      public void handleMessage(Message msg) {
         // update gui based on severity
         switch (msg.what) {
         case Status.UPDATE_TRACK:

         case Status.UPDATE_COVER:
            // For now, do nothing

         case Status.UPDATE_STATE:
            nHelper.updateNotification(status.getTrackName(), status.getTrackArtist(), status.getPlayStatus());
            // if (Build.VERSION.SDK_INT >=
            // Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // rcc.editMetadata(true)
            // .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
            // status.getTrackArtist())
            // .putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
            // status.getTrackName())
            // .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM,
            // status.getTrackAlbum()).apply();
            // }
         }

      }
   };

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {

      String action = intent.getAction();
      if (action != null) {

         if (action.equals(ACTION_END_SERVICE)) {
            Log.d(TAG, "Call to end NotificationService");
            stopSelf();
         } else if (action.equals(ACTION_NEXT_TRACK)) {
            Log.d(TAG, "NotificationService alerted to change track");
            if (session != null)
               session.controlNext();
         } else if (action.equals(ACTION_PAUSE)) {
            Log.d(TAG, "NotificationService alerted to pause");
            if (session != null)
               if (status.getPlayStatus() == Status.STATE_PAUSED)
                  session.controlPlay();
               else
                  session.controlPause();
         }

      } else { // standard initialisation

         Log.d(TAG, "Initialising NotificationService");

      }

      return START_STICKY;
   }

   @Override
   public void onCreate() {
      super.onCreate();

      nHelper = new NotificationHelper(this);

      // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      //
      // // Sets up the lockscreen stuff
      // AudioManager am = (AudioManager)
      // getSystemService(Context.AUDIO_SERVICE);
      // am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
      // AudioManager.AUDIOFOCUS_GAIN);
      // ComponentName lockscreenRec = new ComponentName(this,
      // LockscreenReceiver.class);
      // am.registerMediaButtonEventReceiver(lockscreenRec);
      //
      // Intent broadcast = new
      // Intent(Intent.ACTION_MEDIA_BUTTON).setComponent(lockscreenRec);
      // PendingIntent broadPender = PendingIntent.getBroadcast(this, 0,
      // broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
      //
      // rcc = new RemoteControlClient(broadPender);
      // rcc.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
      // RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE);
      // am.registerRemoteControlClient(rcc);
      //
      // }

      bindService(new Intent(this, BackendService.class), connection, BIND_AUTO_CREATE);

      endServiceIntent = new Intent(this, NotificationService.class).setAction(ACTION_END_SERVICE);
      pauseIntent = new Intent(this, NotificationService.class).setAction(ACTION_PAUSE);
      nextTrackIntent = new Intent(this, NotificationService.class).setAction(ACTION_NEXT_TRACK);
      launchIntent = new Intent(this, ControlActivity.class);

      endServicePending = PendingIntent.getService(this, 0, endServiceIntent, PendingIntent.FLAG_ONE_SHOT);
      pausePending = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      nextTrackPending = PendingIntent.getService(this, 0, nextTrackIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      launchPending = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);

   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      Log.d(TAG, "Destroying NotificationService");
      nHelper.killNotification();
      if (bound) {
         backend = null;
         if (session != null)
            session.purgeNotificationStatus();
         session = null;
      }

      // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      //
      // // Kill the lockscreen stuff
      // AudioManager am = (AudioManager)
      // getSystemService(Context.AUDIO_SERVICE);
      // ComponentName lockscreenRec = new ComponentName(this,
      // LockscreenReceiver.class);
      // am.unregisterMediaButtonEventReceiver(lockscreenRec);
      // am.unregisterRemoteControlClient(rcc);
      // am.abandonAudioFocus(this);
      //
      // }

      unbindService(connection);

   }

   @Override
   public IBinder onBind(Intent intent) {
      return null;
   }

   @Override
   public void onAudioFocusChange(int i) {
      // We don't actually care about this, but its required for lockscreen
      // controls
   }

   public class NotificationHelper {

      private final Context mContext;
      private final NotificationManager nManager;

      public final static int NOTIFICATION = 1;

      public NotificationHelper(Context context) {
         mContext = context;
         nManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
      }

      public void killNotification() {
         nManager.cancel(NOTIFICATION);
      }

      @TargetApi(16)
      public void updateNotification(String track, String artist, int playState) {

         if (Helper.canUseApi(Build.VERSION_CODES.JELLY_BEAN)) {

            int iconSize = getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
            Bitmap cover = null;
            if (status.coverCache != null)
               cover = Bitmap.createScaledBitmap(status.coverCache, iconSize, iconSize, false);
            // Use the new-style notifications. Because we can only have 2
            // actions, the disconnect action is performed by swiping
            // the notification away.
            Notification.Builder newBuilder = new Notification.Builder(mContext);
            newBuilder
                     .setSmallIcon(R.drawable.ic_notify)
                     .setContentTitle(track)
                     .setContentText(artist)
                     .setDeleteIntent(endServicePending)
                     .setContentIntent(launchPending)
                     .addAction(
                              playState == Status.STATE_PAUSED ? R.drawable.ic_notify_play : R.drawable.ic_notify_pause,
                              mContext.getString(playState == Status.STATE_PAUSED ? R.string.notify_play
                                       : R.string.notify_pause), pausePending)
                     .addAction(R.drawable.ic_notify_next, mContext.getString(R.string.notify_next), nextTrackPending);
            if (cover != null)
               newBuilder.setLargeIcon(cover);
            nManager.notify(NOTIFICATION, newBuilder.build());

         } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
            RemoteViews notification = new RemoteViews(getPackageName(), R.layout.notification);
            notification.setTextViewText(R.id.notification_title, track);
            notification.setTextViewText(R.id.notification_text, artist);
            notification.setImageViewResource(R.id.notify_play,
                     playState == Status.STATE_PAUSED ? R.drawable.ic_notify_play : R.drawable.ic_notify_pause);
            notification.setOnClickPendingIntent(R.id.notify_play, pausePending);
            notification.setOnClickPendingIntent(R.id.notify_next, nextTrackPending);
            notification.setOnClickPendingIntent(R.id.notify_end, endServicePending);
            builder.setContent(notification).setOngoing(true).setContentIntent(launchPending)
                     .setSmallIcon(R.drawable.ic_notify);
            nManager.notify(NOTIFICATION, builder.getNotification());

         }
      }

   }

}
