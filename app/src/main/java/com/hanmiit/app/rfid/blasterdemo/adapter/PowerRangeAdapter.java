package com.hanmiit.app.rfid.blasterdemo.adapter;

import java.util.ArrayList;
import java.util.Locale;

import com.hanmiit.lib.rfid.params.MinMaxValue;
import com.hanmiit.app.rfid.blasterdemo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PowerRangeAdapter extends BaseAdapter {

	@SuppressWarnings("unused")
	private static final String TAG = PowerRangeAdapter.class.getSimpleName();

	private static final int MAX_LIST_COUNT = 20;
	
	private LayoutInflater mInflater;
	private ArrayList<PowerRangeItem> mList;

	private int mRes;
	private int mDropDownRes;
	
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public PowerRangeAdapter(Context context, MinMaxValue value) {
		super();

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = new ArrayList<PowerRangeItem>();
		int count = 0;
		
		for (int i = value.getMax(); i >= value.getMin() && count < MAX_LIST_COUNT; i -= 10, count++) {
			mList.add(new PowerRangeItem(i));
		}
		mRes = mDropDownRes = R.layout.item_power_range;
	}

	public PowerRangeAdapter(Context context, MinMaxValue value, int res) {
		super();

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = new ArrayList<PowerRangeItem>();
	
		for (int i = value.getMax(); i >= value.getMin(); i -= 10) {
			mList.add(new PowerRangeItem(i));
		}
		mRes = mDropDownRes = res;
	}

	public PowerRangeAdapter(Context context, MinMaxValue value, int res, int dropDownRes) {
		super();

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = new ArrayList<PowerRangeItem>();
		int count = 0;
	
		for (int i = value.getMax(); i >= value.getMin() && count < MAX_LIST_COUNT; i -= 10, count++) {
			mList.add(new PowerRangeItem(i));
		}
		mRes = res;
		mDropDownRes = dropDownRes;
	}

	public int getItemValue(int position) {
		return mList.get(position).getValue();
	}
	
	public int getItemPosition(int value) {
		for (int i = 0; i < mList.size(); i++) {
			if (mList.get(i).getValue() == value) {
				return i;
			}
		}
		return 0;
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position).getValue();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PowerRangeViewHolder holder = null;
		
		if (null == convertView) {
			convertView = mInflater.inflate(mRes, parent, false);
			holder = new PowerRangeViewHolder(convertView);
		} else {
			holder = (PowerRangeViewHolder)convertView.getTag();
		}
		holder.setItem(mList.get(position));
		return convertView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		PowerRangeViewHolder holder;

		if (null == convertView) {
			convertView = mInflater.inflate(mDropDownRes, parent, false);
			holder = new PowerRangeViewHolder(convertView);
		} else {
			holder = (PowerRangeViewHolder) convertView.getTag();
		}
		holder.setItem(mList.get(position));
		return convertView;
	}

	// ------------------------------------------------------------------------
	// Internal PowerRangItem Class
	// ------------------------------------------------------------------------
	private class PowerRangeItem {
		private int mValue;

		public PowerRangeItem(int value) {
			mValue = value;
		}

		public int getValue() {
			return mValue;
		}

		public String toString() {
			return String.format(Locale.US, "%.1f dBm", mValue / 10.0F);
		}
	}

	// ------------------------------------------------------------------------
	// Internal PowerRangViewHolder Class
	// ------------------------------------------------------------------------
	private class PowerRangeViewHolder {
		private TextView txtValue;

		public PowerRangeViewHolder(View parent) {
			txtValue = (TextView) parent.findViewById(android.R.id.text1);
			parent.setTag(this);
		}

		public void setItem(PowerRangeItem item) {
			txtValue.setText(item.toString());
		}
	}
}
