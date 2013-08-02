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
package org.tunesremote;

import org.tunesremote.LibraryBrowseActivity.ConnectionListener;
import org.tunesremote.daap.Playlist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class PlaylistsFragment extends ListFragment implements ConnectionListener, OnItemClickListener {

   public static final String TAG = PlaylistsFragment.class.toString();

   PlaylistsAdapter adapter;
   LibraryBrowseActivity host;

   @Override
   public void onCreate(Bundle saved) {
      super.onCreate(saved);
      if (adapter == null)
         adapter = new PlaylistsAdapter(getActivity(), resultsUpdated);
      setListAdapter(adapter);
   }

   @Override
   public void onServiceConnected() {
      host = (LibraryBrowseActivity) getActivity();
      if (host != null) {
         if (adapter.results.isEmpty()) {
            host.library.readPlaylists(adapter);
         }
      } else {
         // Not quite ready, snooze for a bit
         try {
            Thread.sleep(500);
            onServiceConnected();
         } catch (InterruptedException e) {
            Log.e(TAG, "Waiting for Activity connection interrupted " + e);
         }
      }
   }

   @Override
   public void onServiceDisconnected() {
   }

   @Override
   public void onActivityCreated(Bundle saved) {
      super.onActivityCreated(saved);
      setListShown(false);
      getListView().setOnItemClickListener(this);
      host = (LibraryBrowseActivity) getActivity();

   }

   public Handler resultsUpdated = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         adapter.notifyDataSetChanged();
         if (isResumed() && host.positionViewed == 2)
            setListShown(true);
         else
            setListShownNoAnimation(true);
      }
   };

   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

      final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

      try {
         // create context menu to play entire artist
         final Playlist ply = (Playlist) adapter.getItem(info.position);
         menu.setHeaderTitle(ply.getName());
         final String playlistid = Long.toString(ply.getID());

         final MenuItem browse = menu.add(R.string.albums_menu_browse);
         browse.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
               Intent intent = new Intent(host, TracksActivity.class);
               intent.putExtra(Intent.EXTRA_TITLE, "");
               intent.putExtra("Playlist", playlistid);
               intent.putExtra("PlaylistPersistentId", ply.getPersistentId());
               intent.putExtra("AllAlbums", false);
               host.startActivityForResult(intent, 1);

               return true;
            }

         });

         final MenuItem play = menu.add(R.string.playlists_menu_play);
         play.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
               host.session.controlPlayPlaylist(ply.getPersistentId(), "0");
               host.finish();
               return true;
            }

         });
      } catch (Exception e) {
         Log.w(TAG, "onCreateContextMenu:" + e.getMessage());
      }

   }

   @Override
   public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
      try {
         final Playlist ply = (Playlist) adapter.getItem(position);
         final String playlistid = Long.toString(ply.getID());

         Intent intent = new Intent(host, TracksActivity.class);
         intent.putExtra(Intent.EXTRA_TITLE, "");
         intent.putExtra("Playlist", playlistid);
         intent.putExtra("PlaylistPersistentId", ply.getPersistentId());
         intent.putExtra("AllAlbums", false);
         host.startActivityForResult(intent, 1);

      } catch (Exception e) {
         Log.w(TAG, "onCreate:" + e.getMessage());
      }
   }

}
