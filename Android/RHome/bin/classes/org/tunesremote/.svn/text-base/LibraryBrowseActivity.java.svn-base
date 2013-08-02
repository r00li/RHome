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

import java.util.Collections;
import java.util.HashSet;

import org.tunesremote.daap.Library;
import org.tunesremote.daap.Session;
import org.tunesremote.util.ThreadExecutor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class LibraryBrowseActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {

   public static final String TAG = LibraryBrowseActivity.class.toString();

   public BackendService backend;
   public Session session;
   public Library library;
   public ViewPager pager;
   public TabHandler handler;
   public int positionViewed;

   private SharedPreferences prefs;
   private ArtistsListFragment artists = null;
   private AllAlbumsListFragment albums = null;
   private PlaylistsFragment playlists = null;

   public boolean isConnected = false;
   public boolean isTablet = false;

   private final HashSet<ConnectionListener> listeners = new HashSet<ConnectionListener>();

   public ServiceConnection connection = new ServiceConnection() {
      public void onServiceConnected(ComponentName className, final IBinder service) {
         ThreadExecutor.runTask(new Runnable() {

            public void run() {
               backend = ((BackendService.BackendBinder) service).getService();
               if (backend == null)
                  return;

               session = backend.getSession();

               if (session == null)
                  return;

               // begin search now that we have a backend
               library = new Library(session);

               if (listeners.size() > 0)
                  for (ConnectionListener l : listeners)
                     l.onServiceConnected();
               isConnected = true;
            }
         });
      }

      @Override
      public void onServiceDisconnected(ComponentName arg0) {
         for (ConnectionListener l : listeners)
            l.onServiceDisconnected();
         backend = null;
         session = null;
         library = null;
      }
   };

   @SuppressLint("NewApi")
   @Override
   public void onCreate(Bundle saved) {
      super.onCreate(saved);
      this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
      if (this.prefs.getBoolean(this.getString(R.string.pref_fullscreen), true)
               && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
      }
      setContentView(R.layout.act_browse_library);

      artists = new ArtistsListFragment();
      albums = new AllAlbumsListFragment();
      playlists = new PlaylistsFragment();

      isTablet = findViewById(R.id.frame_artists) != null;

      if (!isTablet) {

         (pager = (ViewPager) findViewById(R.id.view_pager)).setAdapter(new LibraryPagerAdapter(
                  getSupportFragmentManager()));
         pager.setOnPageChangeListener(this);
         pager.setOffscreenPageLimit(2);
         findViewById(R.id.tab_artists).setSelected(true);

      } else {

         getSupportFragmentManager().beginTransaction().add(R.id.frame_artists, artists).add(R.id.frame_albums, albums)
                  .add(R.id.frame_playlists, playlists).commit();
         registerListener(artists, albums, playlists);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
         handler = new TabHandler();
         ActionBar ab = getActionBar();
         ab.setTitle(R.string.control_menu_library);

         if (!isTablet) {
            findViewById(R.id.legacy_tabs).setVisibility(View.GONE);
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            Tab artTab = ab.newTab().setText(R.string.control_menu_artists).setTabListener(handler);
            Tab albTab = ab.newTab().setText(R.string.control_menu_albums).setTabListener(handler);
            Tab plyTab = ab.newTab().setText(R.string.control_menu_playlists).setTabListener(handler);
            ab.addTab(artTab);
            ab.addTab(albTab);
            ab.addTab(plyTab);
         }

      }

   }

   @Override
   public void onStart() {
      super.onStart();
      bindService(new Intent(this, BackendService.class), connection, Context.BIND_AUTO_CREATE);
   }

   @Override
   public void onStop() {
      super.onStop();
      unbindService(connection);
   }

   @Override
   protected void onResume() {
      final boolean fullscreen = this.prefs.getBoolean(this.getString(R.string.pref_fullscreen), true);
      if (fullscreen) {
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
         getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
      } else {
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
         getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      }
      super.onResume();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.act_library_browse, menu);
      return true;
   }

   public void tabSelected(View v) {
      int id = v.getId();
      if (id == R.id.tab_album) {
         pager.setCurrentItem(1, true);
         findViewById(R.id.tab_album).setSelected(true);
         findViewById(R.id.tab_artists).setSelected(false);
         findViewById(R.id.tab_playlists).setSelected(false);
      }
      if (id == R.id.tab_artists) {
         pager.setCurrentItem(0, true);
         findViewById(R.id.tab_album).setSelected(false);
         findViewById(R.id.tab_artists).setSelected(true);
         findViewById(R.id.tab_playlists).setSelected(false);
      }
      if (id == R.id.tab_playlists) {
         pager.setCurrentItem(2, true);
         findViewById(R.id.tab_album).setSelected(false);
         findViewById(R.id.tab_artists).setSelected(false);
         findViewById(R.id.tab_playlists).setSelected(true);
      }
   }

   public void registerListener(ConnectionListener... l) {
      Collections.addAll(listeners, l);
   }

   public boolean unregisterListener(ConnectionListener key) {
      return listeners.remove(key);
   }

   public class LibraryPagerAdapter extends FragmentStatePagerAdapter {

      public LibraryPagerAdapter(FragmentManager fm) {
         super(fm);
         registerListener(artists, albums, playlists);
      }

      @Override
      public Fragment getItem(int position) {
         if (position == 0)
            return artists;
         if (position == 1)
            return albums;
         return playlists;
      }

      @Override
      public int getCount() {
         return 3;
      }

      @Override
      public CharSequence getPageTitle(int position) {
         return getString(position == 0 ? R.string.control_menu_artists : R.string.control_menu_albums);
      }

   }

   public interface ConnectionListener {
      public void onServiceConnected();

      public void onServiceDisconnected();
   }

   @Override
   public void onPageScrollStateChanged(int arg0) {
   }

   @Override
   public void onPageScrolled(int arg0, float arg1, int arg2) {
   }

   @TargetApi(11)
   @Override
   public void onPageSelected(int arg0) {
      positionViewed = arg0;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
         handler.fudge = true;
         getActionBar().setSelectedNavigationItem(arg0);

      } else {

         if (arg0 == 0) {
            findViewById(R.id.tab_album).setSelected(false);
            findViewById(R.id.tab_artists).setSelected(true);
            findViewById(R.id.tab_playlists).setSelected(false);
         } else if (arg0 == 1) {
            findViewById(R.id.tab_album).setSelected(true);
            findViewById(R.id.tab_artists).setSelected(false);
            findViewById(R.id.tab_playlists).setSelected(false);
         } else {
            findViewById(R.id.tab_album).setSelected(false);
            findViewById(R.id.tab_artists).setSelected(false);
            findViewById(R.id.tab_playlists).setSelected(true);
         }

      }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem arg0) {
      if (arg0.getItemId() == android.R.id.home) {
         finish();
      } else {
         playlists.setListShownNoAnimation(false);
         artists.setListShownNoAnimation(false);
         albums.setListShownNoAnimation(true);
         ThreadExecutor.runTask(new Runnable() {

            @Override
            public void run() {
               for (ConnectionListener l : listeners)
                  l.onServiceConnected();
            }

         });
      }
      return true;
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent intent) {
      super.onActivityResult(requestCode, resultCode, intent);

      // If canceled stay at current level
      if (resultCode == RESULT_CANCELED)
         return;

      // Otherwise pass this back up the chain
      this.setResult(resultCode, intent);
      this.finish();
   }

   @TargetApi(11)
   public class TabHandler implements ActionBar.TabListener {

      public boolean fudge = false;

      @Override
      public void onTabReselected(Tab tab, FragmentTransaction ft) {
         // Do nothing
      }

      @Override
      public void onTabSelected(Tab tab, FragmentTransaction ft) {
         if (!fudge)
            pager.setCurrentItem(tab.getPosition(), true);
         fudge = false;
      }

      @Override
      public void onTabUnselected(Tab tab, FragmentTransaction ft) {
         // Do nothing
      }

   }

}
