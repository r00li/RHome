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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.tunesremote.LibraryBrowseActivity.ConnectionListener;
import org.tunesremote.daap.Response;

import java.util.LinkedList;
import java.util.List;

public class ArtistsListFragment extends ListFragment implements
		ConnectionListener, OnItemClickListener {

	public static final String TAG = ArtistsListFragment.class.toString();

	LibraryBrowseActivity host;
	ArtistsAdapter adapter;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		if (adapter == null)
			adapter = new ArtistsAdapter(getActivity());
		setListAdapter(adapter);
	}

	@Override
	public void onServiceConnected() {
		host = (LibraryBrowseActivity) getActivity();
		if (host != null) {

			if (adapter.results.isEmpty()) {
				host.library.readArtists(adapter);
			}

		} else {
			// Not quite ready, snooze for a bit
			try {
				Thread.sleep(500);
				onServiceConnected();
			} catch (InterruptedException e) {
				Log.d(TAG, "Waiting for Activity connection interrupted");
			}
		}
	}

	@Override
	public void onServiceDisconnected() {
		// Nothing required
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		setListShown(false);
		getListView().setOnItemClickListener(this);
		host = (LibraryBrowseActivity) getActivity();

		registerForContextMenu(getListView());
	}

	public Handler resultsUpdated = new Handler() {
		@SuppressLint("NewApi")
		@Override
		public void handleMessage(Message msg) {
			adapter.notifyDataSetChanged();
			// our cached sections aren't valid any more, wipe and recreate them
			if (getView() != null) {
				getListView().setFastScrollEnabled(true);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					getListView().setFastScrollAlwaysVisible(true);
				}

				if (isVisible() && host.positionViewed == 0)
					setListShown(true);
				else
					setListShownNoAnimation(true);
			}
		}
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenu.ContextMenuInfo menuInfo) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		// create context menu to play entire artist
		try {
			Response resp = (Response) adapter.getItem(info.position);
			final String artist = resp.getString("mlit");
			menu.setHeaderTitle(artist);

			MenuItem play = menu.add(R.string.artists_menu_play);
			play.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					host.session.controlPlayArtist(artist, 0);
					host.setResult(Activity.RESULT_OK, new Intent());
					host.finish();
					return true;
				}
			});

			MenuItem queue = menu.add(R.string.artists_menu_queue);
			queue.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					host.session.controlQueueArtist(artist);
					host.setResult(Activity.RESULT_OK, new Intent());
					host.finish();
					return true;
				}
			});

			MenuItem browse = menu.add(R.string.artists_menu_browse);
			browse.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					Intent intent = new Intent(host, AlbumsActivity.class);
					intent.putExtra(Intent.EXTRA_TITLE, artist);
					host.startActivityForResult(intent, 1);
					return true;
				}
			});
		} catch (Exception e) {
			Log.w(TAG, "onCreateContextMenu:" + e.getMessage());
		}

	}

	public class ArtistsAdapter extends BaseAdapter implements TagListener {

		protected Context context;
		protected LayoutInflater inflater;

		protected List<Response> results = new LinkedList<Response>();

		// public List<String> nice = new ArrayList<String>();

		public ArtistsAdapter(Context context) {
			this.context = context;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		public void foundTag(String tag, final Response resp) {
			if (resp == null) {
				return;
			}

			getActivity().runOnUiThread(new Runnable() {

				public void run() {
					try {
						// add a found search result to our list
						if (resp.containsKey("mlit")) {
							String mlit = resp.getString("mlit");
							if (mlit.length() > 0 && !mlit.startsWith("mshc")) {
								results.add(resp);
							}
						}
					} catch (Exception e) {
						Log.w(TAG, "foundTag:" + e.getMessage());
					}
				}

			});
		}

		public void searchDone() {
			try {
				resultsUpdated.removeMessages(-1);
				resultsUpdated.sendEmptyMessage(-1);
			} catch (Exception e) {
				Log.w(TAG, "searchDone:" + e.getMessage());
			}
		}

		public Object getItem(int position) {
			return results.get(position);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		public int getCount() {
			return results.size();
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			try {
				if (convertView == null)
					convertView = inflater.inflate(R.layout.item_artist,
							parent, false);

				// otherwise show normal search result
				Response resp = (Response) this.getItem(position);

				String title = resp.getString("mlit");
				((TextView) convertView.findViewById(android.R.id.text1))
						.setText(title);
			} catch (Exception e) {
				Log.w(TAG, "getView:" + e.getMessage());
			}

			/*
			 * abro --+ mstt 4 000000c8 == 200 muty 1 00 == 0 mtco 4 000001ea ==
			 * 490 mrco 4 000001ea == 490 abar --+ mlit 11 Aaron Shust mlit 10
			 * Acceptance mlit 15 Acues & Elitist
			 */

			return convertView;

		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
	                        long arg3) {
		try {
			Response resp = (Response) adapter.getItem(position);
			final String artist = resp.getString("mlit");

			Intent intent = new Intent(host, AlbumsActivity.class);
			intent.putExtra(Intent.EXTRA_TITLE, artist);
			host.startActivityForResult(intent, 1);
		} catch (Exception e) {
			Log.w(TAG, "onItemClick:" + e.getMessage());
		}
	}

}
