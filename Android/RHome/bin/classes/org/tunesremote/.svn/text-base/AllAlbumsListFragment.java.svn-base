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
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.support.v4.app.ListFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.tunesremote.LibraryBrowseActivity.ConnectionListener;
import org.tunesremote.daap.RequestHelper;
import org.tunesremote.daap.Response;
import org.tunesremote.util.Helper;

import java.lang.ref.SoftReference;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AllAlbumsListFragment extends ListFragment implements
		ConnectionListener, AdapterView.OnItemClickListener {

	public static final String TAG = AllAlbumsListFragment.class.toString();

	LibraryBrowseActivity host;
	AlbumsAdapter adapter;
	Bitmap blank;

	protected int imageSize = 66;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		if (adapter == null)
			adapter = new AlbumsAdapter(getActivity());
		setListAdapter(adapter);

	}

	@Override
	public void onServiceConnected() {
		host = (LibraryBrowseActivity) getActivity();
		if (host != null) {
			if (adapter.results.isEmpty()) {
				host.library.readAlbums(adapter);
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

		// blank = ((BitmapDrawable)
		// getResources().getDrawable(R.drawable.album_art_blank_thumb)).getBitmap();

		DisplayMetrics outMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(outMetrics);
		imageSize = outMetrics.densityDpi * 66;
		imageSize = imageSize > 132 ? 132 : imageSize;

		registerForContextMenu(getListView());

		host = (LibraryBrowseActivity) getActivity();
		if (host.isConnected)
			onServiceConnected();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenu.ContextMenuInfo menuInfo) {
		try {
			host.getMenuInflater().inflate(R.menu.context_albums, menu);
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			Response resp = (Response) adapter.getItem(info.position);
			menu.setHeaderTitle(resp.getString("minm"));

		} catch (Exception e) {
			Log.w(TAG, "onCreateContextMenu:" + e.getMessage());
		}
	}

	public Handler resultsUpdated = new Handler() {
		@SuppressLint("NewApi")
		@TargetApi(11)
		@Override
		public void handleMessage(Message msg) {
			adapter.notifyDataSetChanged();
			if (isVisible() && host.positionViewed == 1)
				setListShown(true);
			else
				setListShownNoAnimation(true);

			getListView().setFastScrollEnabled(true);
			if (Helper.canUseApi(Build.VERSION_CODES.HONEYCOMB)) {
				getListView().setFastScrollAlwaysVisible(true);
			}
		}
	};

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		int id = item.getItemId();
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		try {
			final Response resp = (Response) adapter.getItem(info.position);
			final String albumid = resp.getNumberString("mper");

			if (id == R.id.context_album_play) {
				host.session.controlPlayAlbum(albumid, 0);
				host.setResult(Activity.RESULT_OK, new Intent());
				host.finish();
				return true;
			} else if (id == R.id.context_album_queue) {
				host.session.controlQueueAlbum(albumid);
				host.setResult(Activity.RESULT_OK, new Intent());
				host.finish();
				return true;
			} else if (id == R.id.context_album_browse) {
				Intent intent = new Intent(host, TracksActivity.class);
				intent.putExtra(Intent.EXTRA_TITLE, albumid);
				intent.putExtra("minm", resp.getString("minm"));
				intent.putExtra("miid", Long
						.valueOf(resp.getNumberLong("miid")).intValue());
				intent.putExtra("Artist", resp.getString("asaa"));
				intent.putExtra("AllAlbums", false);
				host.startActivityForResult(intent, 1);
				return true;
			}
		} catch (Exception ex) {
			Log.w(TAG, "onContextItemSelected: " + ex.getMessage());
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		try {
			// launch activity to browse track details for this albums
			final Response resp = (Response) adapter.getItem(i);
			final String albumid = resp.getNumberString("mper");

			Intent intent = new Intent(host, TracksActivity.class);
			intent.putExtra(Intent.EXTRA_TITLE, albumid);
			intent.putExtra("minm", resp.getString("minm"));
			intent.putExtra("miid", Long.valueOf(resp.getNumberLong("miid")).intValue());
			intent.putExtra("Artist", resp.getString("asaa"));
			intent.putExtra("AllAlbums", false);
			startActivityForResult(intent, 1);
		} catch (Exception e) {
			Log.w(TAG, "onCreate:" + e.getMessage());
		}
	}

	public class AlbumsAdapter extends BaseAdapter implements TagListener {

		protected Context context;
		protected LayoutInflater inflater;
		protected final List<Response> results = new LinkedList<Response>();
		protected List<String> nice = new ArrayList<String>();

		public AlbumsAdapter(Context context) {
			this.context = context;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		public void foundTag(String tag, final Response resp) {
			if (resp == null) {
				return;
			}

			getActivity().runOnUiThread(new Runnable() {

				@TargetApi(9)
				public void run() {
					// add a found search result to our list
					try {
						if (resp.containsKey("minm")) {
							results.add(resp);
							nice.add(Normalizer.normalize(
									resp.getString("minm"),
									Normalizer.Form.NFKD).replaceAll(
									"\\p{InCombiningDiacriticalMarks}+", ""));
						}
					} catch (Exception ex) {
						Log.w(TAG, "foundTag:" + ex.getMessage());
					}
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

			try {
				if (convertView == null)
					convertView = this.inflater.inflate(R.layout.item_album,
							parent, false);
				Response child = (Response) this.getItem(position);
				String title = child.getString("minm");
				String caption = child.getString("asaa");

				((TextView) convertView.findViewById(android.R.id.text1))
						.setText(title);
				((TextView) convertView.findViewById(android.R.id.text2))
						.setText(caption);

				// go load image art
				((ImageView) convertView.findViewById(android.R.id.icon))
						.setImageBitmap(blank);
				new LoadPhotoTask().execute(Integer.valueOf(position),
						Integer.valueOf((int) child.getNumberLong("miid")));
			} catch (Exception e) {
				Log.w(TAG, "getView:" + e.getMessage());
			}

			return convertView;

		}

	}

	protected SparseArray<SoftReference<Bitmap>> memcache = new SparseArray<SoftReference<Bitmap>>();

	private class LoadPhotoTask extends AsyncTask<Object, Void, Object[]> {
		@Override
		public Object[] doInBackground(Object... params) {

			Integer position = (Integer) params[0];
			Integer itemid = (Integer) params[1];
			boolean stored = false;

			Bitmap bitmap = null;
			try {

				// first check if we have an in-memory cache of this bitmap
				if (memcache.get(itemid) != null) {
					bitmap = memcache.get(itemid).get();
					stored = true;
				}

				if (bitmap != null) {
					Log.d(TAG,
							String.format("MEMORY cache hit for %s",
									itemid.toString()));
				} else {

					// fetch the album cover from itunes
					byte[] raw = RequestHelper.request(String.format(
							"%s/databases/%d/groups/%d/extra_data/artwork?session-id=%s&mw="
									+ imageSize + "&mh=" + imageSize
									+ "&group-type=albums",
							host.session.getRequestBase(),
							host.session.databaseId, itemid,
							host.session.sessionId), false);
					bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.length);

					// if SOMEHOW (404, etc) this image was still null, then
					// save as
					// blank
					if (bitmap == null)
						bitmap = blank;

					// try removing any stale references
					memcache.remove(itemid);
					memcache.put(itemid, new SoftReference<Bitmap>(bitmap));
				}
			} catch (Exception e) {
				Log.w(TAG, "LoadPhotoTask:" + e.getMessage());
			}

			return new Object[]{position, bitmap, stored};
		}

		@Override
		protected void onPostExecute(Object[] result) {
			try {
				if (result == null) {
					return;
				}
				// update gui to show the newly-fetched albumart
				final int position = ((Integer) result[0]).intValue();
				final Bitmap bitmap = (Bitmap) result[1];
				final boolean stored = (Boolean) result[2];

				// skip if bitmap wasnt found
				if (bitmap == null)
					return;

				// skip updating this item if outside of bounds
				if (position < getListView().getFirstVisiblePosition()
						|| position > getListView().getLastVisiblePosition())
					return;

				// find actual position and update view
				int visible = position
						- getListView().getFirstVisiblePosition();
				View view = getListView().getChildAt(visible);
				ImageView v = (ImageView) view.findViewById(android.R.id.icon);
				if (!stored)
					v.setVisibility(View.INVISIBLE);
				v.setImageBitmap(bitmap);
				if (!stored)
					v.startAnimation(AnimationUtils.loadAnimation(
							getActivity(), R.anim.fade_up));

			} catch (Exception e) {
				// we probably ran into an item thats now collapsed, just ignore
				Log.d(TAG, "end:" + e.getMessage());
			}
		}
	}

}
