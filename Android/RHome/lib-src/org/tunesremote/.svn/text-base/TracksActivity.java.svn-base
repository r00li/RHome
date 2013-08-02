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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.tunesremote.daap.*;
import org.tunesremote.util.ThreadExecutor;

import java.util.LinkedList;
import java.util.List;

public class TracksActivity extends BaseBrowseActivity {

	public final static String TAG = TracksActivity.class.toString();
	protected BackendService backend;
	protected Session session;
	protected Library library;
	protected TracksAdapter adapter;
	protected boolean allAlbums;
	protected String artist, albumid, playlistId, playlistPersistentId;
	protected int albumCoverId = 0;
	protected int coverSize = 150;

	public ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, final IBinder service) {
			ThreadExecutor.runTask(new Runnable() {

				public void run() {
					try {
						backend = ((BackendService.BackendBinder) service).getService();
						session = backend.getSession();

						if (session == null)
							return;

						adapter.results.clear();

						// begin search now that we have a backend
						library = new Library(session);

						if (playlistId != null)
							library.readPlaylist(playlistId, adapter);
						else if (allAlbums)
							library.readAllTracks(artist, adapter);
						else {
							library.readTracks(albumid, adapter);
							TracksActivity.this.albumCoverId = getIntent().getIntExtra("miid", 0);
							new LoadPhotoTask().execute(getIntent().getIntExtra("miid", 0));
						}

					} catch (Exception e) {
						Log.e(TAG, "onServiceConnected:" + e.getMessage());
					}
				}

			});
		}

		public void onServiceDisconnected(ComponentName className) {
			backend = null;
			session = null;

		}
	};

	public Handler resultsUpdated = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			TracksActivity.this.runOnUiThread(new Runnable() {

				public void run() {
					if (adapter == null) {
						return;
					}
					adapter.notifyDataSetChanged();
				}

			});
		}
	};

	@Override
	public void onStart() {
		super.onStart();
		this.bindService(new Intent(this, BackendService.class), connection, Context.BIND_AUTO_CREATE);

	}

	@Override
	public void onStop() {
		super.onStop();
		this.unbindService(connection);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.albumid = this.getIntent().getStringExtra(Intent.EXTRA_TITLE);
		this.allAlbums = this.getIntent().getBooleanExtra("AllAlbums", false);
		this.artist = this.getIntent().getStringExtra("Artist");
		this.playlistId = this.getIntent().getStringExtra("Playlist");
		this.playlistPersistentId = this.getIntent().getStringExtra("PlaylistPersistentId");

		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		Log.w(TAG, "Screen Height and Width = " + outMetrics.heightPixels + " x " + outMetrics.widthPixels);
		if (outMetrics.heightPixels > 800) {
			// Tablet config, with 200dip imageView
			coverSize = outMetrics.densityDpi * 300;
			if (coverSize > 600) {
				coverSize = 600;
			}
		} else {
			coverSize = outMetrics.densityDpi * 150;
			if (coverSize > 300) {
				coverSize = 300;
			}
		}

		if (playlistId != null || allAlbums)
			setContentView(R.layout.gen_list);
		else {
			setContentView(R.layout.act_single_album);
			((TextView) findViewById(R.id.album_artist)).setText(artist);
			((TextView) findViewById(R.id.album_name)).setText(getIntent().getStringExtra("minm"));
		}

		((TextView) this.findViewById(android.R.id.empty)).setText(R.string.tracks_empty);

		// show tracklist for specified album
		// set out list adapter to albums found

		this.registerForContextMenu(this.getListView());

		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				try {
					// assuming track order, begin playing this track
					if (session != null) {
						Response resp = (Response) adapter.getItem(position);
						final long trackId = resp.getNumberLong("miid");
						final long currentTrackId = ControlActivity.status.getTrackId();
						if (trackId == currentTrackId) {
							TracksActivity.this.setResult(RESULT_OK, new Intent());
							TracksActivity.this.finish();
						}
						if (TracksActivity.this.playlistId != null) {
							Status.lastActivity = "playlist";
							Status.lastPlaylistId = TracksActivity.this.playlistId;
							Status.lastPlaylistPersistentId = TracksActivity.this.playlistPersistentId;
							String containerItemId = resp.getNumberHex("mcti");
							session.controlPlayPlaylist(TracksActivity.this.playlistPersistentId, containerItemId);
							TracksActivity.this.setResult(RESULT_OK, new Intent());
							TracksActivity.this.finish();
						} else if (TracksActivity.this.allAlbums) {
							session.controlPlayArtist(artist, position);
							TracksActivity.this.setResult(RESULT_OK, new Intent());
							TracksActivity.this.finish();
						} else {
							session.controlPlayAlbum(albumid, position);
							String[] albumInfo = {albumid, resp.getString("minm"), Integer.toString(TracksActivity.this.albumCoverId), resp.getString("asaa")};
							Status.lastActivity = "album";
							Status.lastAlbum = albumInfo;
							TracksActivity.this.setResult(RESULT_OK, new Intent());
							TracksActivity.this.finish();
						}
					}
				} catch (Exception e) {
					Log.w(TAG, "onItemClick:" + e.getMessage());
				}
			}
		});

		this.adapter = new TracksAdapter(this);
		this.setListAdapter(adapter);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		try {
			// create context menu to play entire artist
			final Response resp = (Response) adapter.getItem(info.position);
			menu.setHeaderTitle(resp.getString("minm"));
			final String trackId = resp.getNumberString("miid");
			final String containerItemId = resp.getNumberHex("mcti");
			if (TracksActivity.this.playlistId != null) {
				MenuItem play = menu.add(R.string.playlists_menu_play);
				play.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						session.controlPlayPlaylist(TracksActivity.this.playlistPersistentId, containerItemId);
						TracksActivity.this.setResult(RESULT_OK, new Intent());
						TracksActivity.this.finish();
						return true;
					}
				});
				final MenuItem queue = menu.add(R.string.tracks_menu_queue);
				queue.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						session.controlQueueTrack(trackId);
						return true;
					}
				});

			} else if (TracksActivity.this.allAlbums) {
				MenuItem play = menu.add(R.string.artists_menu_play);
				play.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						session.controlPlayArtist(artist, 0);
						TracksActivity.this.setResult(RESULT_OK, new Intent());
						TracksActivity.this.finish();
						return true;
					}
				});

				MenuItem queue = menu.add(R.string.artists_menu_queue);
				queue.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						session.controlQueueArtist(artist);
						return true;
					}
				});
			} else {
				final MenuItem play = menu.add(R.string.tracks_menu_play);
				play.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						session.controlPlayTrack(trackId);
						TracksActivity.this.setResult(RESULT_OK, new Intent());
						TracksActivity.this.finish();
						return true;
					}
				});

				final MenuItem queue = menu.add(R.string.tracks_menu_queue);
				queue.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						session.controlQueueTrack(trackId);
						return true;
					}
				});
			}

		} catch (Exception e) {
			Log.w(TAG, "onCreateContextMenu:" + e.getMessage());
		}

	}

	public class TracksAdapter extends BaseAdapter implements TagListener {

		protected Context context;
		protected LayoutInflater inflater;
		protected List<Response> results = new LinkedList<Response>();

		public TracksAdapter(Context context) {
			this.context = context;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		public void foundTag(String tag, final Response resp) {
			// Now that we do all the tag reading on a background thread, this must
			// performed on the UI thread
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// add a found search result to our list
					if (resp.containsKey("minm"))
						results.add(resp);
					TracksAdapter.this.searchDone();
				}

			});
		}

		public void searchDone() {
			resultsUpdated.removeMessages(-1);
			resultsUpdated.sendEmptyMessage(-1);
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

			if (convertView == null)
				convertView = inflater.inflate(R.layout.item_track, parent, false);

			try {

				// otherwise show normal search result
				final Response resp = (Response) this.getItem(position);

				final String title = resp.getString("minm");
				final String length = Response.convertTime(resp.getNumberLong("astm"));

				final long trackId = resp.getNumberLong("miid");
				final long currentTrackId = ControlActivity.status.getTrackId();

				TextView txtTitle = ((TextView) convertView.findViewById(android.R.id.text1));
				txtTitle.setText(title);

				TextView txtLength = ((TextView) convertView.findViewById(android.R.id.text2));
				txtLength.setText(length);

				// highlight the current track playing
				if (currentTrackId == trackId) {
					Log.e(TAG, "Track Ids match! = " + trackId);
					txtTitle.setTextColor(Color.BLUE);
					txtLength.setTextColor(Color.BLUE);
				} else {
					txtTitle.setTextColor(Color.WHITE);
					txtLength.setTextColor(Color.WHITE);
				}

			} catch (Exception e) {
				Log.d(TAG, String.format("onCreate Error: %s", e.getMessage()));
			}

			/*
			 * mlit --+ mikd 1 02 == 2 asal 12 Dance or Die asar 14 Family Force 5
			 * astm 4 0003d5d6 == 251350 astn 2 0001 miid 4 0000005b == 91 minm 12
			 * dance or die
			 */

			return convertView;

		}

	}

	private class LoadPhotoTask extends AsyncTask<Integer, Void, Bitmap> {

		@Override
		public Bitmap doInBackground(Integer... params) {

			Bitmap bitmap = null;
			try {

				// fetch the album cover from itunes
				byte[] raw = RequestHelper.request(String.format(
						"%s/databases/%d/groups/%d/extra_data/artwork?session-id=%s&mw=" + coverSize + "&mh=" + coverSize
								+ "&group-type=albums", session.getRequestBase(), session.databaseId, params[0],
						session.sessionId), false);
				bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.length);

				// if SOMEHOW (404, etc) this image was still null, then
				// save as
				// blank

			} catch (Exception e) {
				Log.w(TAG, "LoadPhotoTask:" + e.getMessage());
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			try {

				if (result != null) {
					ImageView v = (ImageView) findViewById(R.id.album_cover);
					v.setImageBitmap(result);
					Animation anim = AnimationUtils.loadAnimation(TracksActivity.this, R.anim.fade_up);
					v.startAnimation(anim);
				}

			} catch (Exception e) {
				// we probably ran into an item thats now collapsed, just ignore
				Log.d(TAG, "end:" + e.getMessage());
			}
		}
	}

	@Override
	public void onBackPressed() {
		Status.lastActivity = "";
		super.onBackPressed();
	}


}
