package com.hanmiit.app.rfid.blasterdemo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.hanmiit.app.rfid.blasterdemo.R;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TagListAdapter extends BaseAdapter {

	private static final int UPDATE_TIME = 300;

	private LayoutInflater mInflater;
	private ArrayList<TagListItem> mList;
	private HashMap<String, TagListItem> mMap;
	private boolean mIsDisplayPc;
	private boolean mIsReportRSSI;
	private boolean mIsShowCount;
	private boolean mIsReportTID;
	private Context mContext;
	private Handler mHandler;
	private UpdateThread mUpdateThread;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	public TagListAdapter(Context context) {
		super();

		mContext = context;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = new ArrayList<TagListItem>();
		mMap = new HashMap<String, TagListItem>();
		mIsDisplayPc = true;
		mIsShowCount = true;
		mIsReportRSSI = false;
		mIsReportTID = false;
		mUpdateThread = null;
		mHandler = new Handler();
	}

	public void clear() {
		mList.clear();
		mMap.clear();
		notifyDataSetChanged();
	}

	public boolean getDisplayPc() {
		return mIsDisplayPc;
	}

	public void setDisplayPc(boolean enabled) {
		mIsDisplayPc = enabled;
		notifyDataSetChanged();
	}

	public boolean getShowCount() {
		return mIsShowCount;
	}

	public void setShowCount(boolean enabled) {
		mIsShowCount = enabled;
		notifyDataSetChanged();
	}
	
	public boolean getReportRSSI() {
		return mIsReportRSSI;
	}
	
	public void setReportRSSI(boolean enabled) {
		if (mIsReportRSSI == enabled)
			return;
		mIsReportRSSI = enabled;
		clear();
	}
	
	public boolean getReportTID() {
		return mIsReportTID;
	}
	
	public void setReportTID(boolean enabled) {
		if (mIsReportTID == enabled)
			return;
		mIsReportTID = enabled;
		clear();
	}

	public void start() {
		mUpdateThread = new UpdateThread();
		mUpdateThread.start();
	}

	public void shutDown() {
		if (mUpdateThread != null) {
			mUpdateThread.cancel();
			try {
				mUpdateThread.join();
			} catch (InterruptedException e) {
			}
			mUpdateThread = null;
		}
	}

	public void addTag(String tag, float rssi, float phase) {
		TagListItem item = null;

		if (null == (item = mMap.get(tag))) {
			item = new TagListItem(tag, rssi, phase);
			mList.add(item);
			mMap.put(tag, item);
		} else {
			item.updateItem(rssi, phase);
		}
	}

	public void addTag(String tag, String tid, float rssi, float phase) {
		TagListItem item = null;

		if (null == (item = mMap.get(tag))) {
			item = new TagListItem(tag, tid, rssi, phase);
			mList.add(item);
			mMap.put(tag, item);
		} else {
			if (item.getTid().equals(tid))
				item.updateItem(rssi, phase);
			else {
				item = new TagListItem(tag, tid, rssi, phase);
				mList.add(item);
				mMap.put(tag, item);
			}
		}
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public String getItem(int position) {
		return mList.get(position).getTag(true);
	}
	
	public String getItem(int position, boolean displayPc) {
		return mList.get(position).getTag(displayPc);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TagListViewHolder holder = null;

		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.item_tag_list, parent,
					false);
			holder = new TagListViewHolder(convertView);
		} else {
			holder = (TagListViewHolder) convertView.getTag();
		}
		holder.setItem(mList.get(position), mIsDisplayPc);
		return convertView;
	}

	// ------------------------------------------------------------------------
	// Internal TagListItem class
	// ------------------------------------------------------------------------

	private class TagListItem {

		private String mTag;
		private String mTid;
		private float mRSSI;
		private float mPhase;
		private int count;

		@SuppressWarnings("unused")
		public TagListItem(String tag) {
			mTag = tag;
			mTid = "";
			mRSSI = 0;
			mPhase = 0;
			count = 1;
		}

		public TagListItem(String tag, float rssi, float phase) {
			mTag = tag;
			mTid = "";
			mRSSI = rssi;
			mPhase = phase;
			count = 1;
		}

		public TagListItem(String tag, String tid, float rssi, float phase) {
			mTag = tag;
			mTid = tid;
			mRSSI = rssi;
			mPhase = phase;
			count = 1;
		}

		public String getTag(boolean displayPc) {
			return displayPc ? mTag : mTag.substring(4);
		}
		
		public String getTid() {
			return mTid;
		}
		
		public float getRSSI() {
			return mRSSI;
		}
		
		public float getPhase() {
			return mPhase;
		}

		public int getCount() {
			return count;
		}

		public void updateItem(float rssi, float phase) {
			mRSSI = rssi;
			mPhase = phase;
			count++;
		}
	}

	// ------------------------------------------------------------------------
	// Internal TagListViewHodler class
	// ------------------------------------------------------------------------
	private class TagListViewHolder {
		private TextView txtTag;
		private TextView txtTid;
		private TextView txtRssi;
		private TextView txtPhase;
		private TextView txtCount;
		private LinearLayout layoutSubItems;

		public TagListViewHolder(View parent) {
			txtTag = (TextView) parent.findViewById(R.id.tag_value);
			txtTid = (TextView) parent.findViewById(R.id.tid_value);
			txtTid.setVisibility(mIsReportTID ? View.VISIBLE : View.GONE);
			txtRssi = (TextView)parent.findViewById(R.id.tag_rssi);
			txtPhase = (TextView)parent.findViewById(R.id.tag_phase);
			txtCount = (TextView) parent.findViewById(R.id.tag_count);
			txtCount.setVisibility(mIsShowCount ? View.VISIBLE : View.GONE);
			layoutSubItems = (LinearLayout)parent.findViewById(R.id.sub_items);
			layoutSubItems.setVisibility(mIsReportRSSI ? View.VISIBLE : View.GONE);
			parent.setTag(this);
		}

		public void setItem(TagListItem item, boolean displayPc) {
			txtTag.setText(item.getTag(displayPc));
			txtTid.setText(item.getTid());
			txtRssi.setText(String.format(Locale.US, "%.1f dB", item.getRSSI()));
			txtPhase.setText(String.format(Locale.US, "%.1f˚", item.getPhase()));
			txtCount.setText("" + item.getCount());
			layoutSubItems.setVisibility(mIsReportRSSI ? View.VISIBLE : View.GONE);
		}
	}

	// ------------------------------------------------------------------------
	// Internal UpdateThread class
	// ------------------------------------------------------------------------
	private class UpdateThread extends Thread {

		private boolean mIsAlived;

		private UpdateThread() {
			mIsAlived = false;
		}

		@Override
		public void run() {
			mIsAlived = true;
			
			while (mIsAlived) {
				synchronized (TagListAdapter.this) {
					try {
						TagListAdapter.this.wait(UPDATE_TIME);
					} catch (InterruptedException e) {
					}
				}
				
				if (mIsAlived) {
					mHandler.post(procUpdate);
				}
			}
		}
		
		private Runnable procUpdate = new Runnable() {

			@Override
			public void run() {
				TagListAdapter.this.notifyDataSetChanged();
			}
			
		};

		public void cancel() {
			synchronized (TagListAdapter.this) {
				mIsAlived = false;
				TagListAdapter.this.notify();
			}
		}
	}
}
