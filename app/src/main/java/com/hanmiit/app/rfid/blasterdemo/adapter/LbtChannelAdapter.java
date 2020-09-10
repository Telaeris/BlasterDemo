package com.hanmiit.app.rfid.blasterdemo.adapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.hanmiit.lib.rfid.type.LbtItem;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.listener.LbtChannelListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class LbtChannelAdapter extends BaseAdapter implements LbtChannelListener {

	private LayoutInflater mInflater;
	private ArrayList<LbtChannelItem> lstItems;
	private HashMap<Integer, LbtChannelItem> mapItems;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	@SuppressLint("UseSparseArrays")
	public LbtChannelAdapter(Context context) {
		super();

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		lstItems = new ArrayList<LbtChannelItem>();
		mapItems = new HashMap<Integer, LbtChannelItem>();
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	public void addItem(int slot, String name) {
		LbtChannelItem item = new LbtChannelItem(name, slot);

		lstItems.add(item);
		mapItems.put(slot, item);
	}

	public void clear() {
		lstItems.clear();
		mapItems.clear();
	}

	public void sortItem() {
		Collections.sort(lstItems, mComparator);
	}

	public LbtItem[] getTable() {
		LbtChannelItem item = null;
		LbtItem[] items = new LbtItem[lstItems.size()];

		for (int i = 0; i < lstItems.size(); i++) {
			item = lstItems.get(i);
			items[i] = new LbtItem(item.mSlot, item.isUsed());
		}
		return items;
	}

	public void setTable(LbtItem[] table) {
		for (LbtItem value : table) {
			mapItems.get(value.getSlot()).setUsed(value.isUsed());
		}
	}

	private final static Comparator<LbtChannelItem> mComparator = new Comparator<LbtChannelItem>() {

		private final Collator collator = Collator.getInstance();

		@Override
		public int compare(LbtChannelItem lhs, LbtChannelItem rhs) {
			return collator.compare(lhs.getName(), rhs.getName());
		}

	};

	// ------------------------------------------------------------------------
	// Override Methods
	// ------------------------------------------------------------------------

	@Override
	public int getCount() {
		return lstItems.size();
	}

	@Override
	public Object getItem(int position) {
		return lstItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return lstItems.get(position).getSlot();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LbtChannelViewHolder holder = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_lbt_channel,
					parent, false);
			holder = new LbtChannelViewHolder(convertView, this);
		} else {
			holder = (LbtChannelViewHolder) convertView.getTag();
		}
		holder.setItem(position, lstItems.get(position));
		return convertView;
	}

	@Override
	public void onLbtChannelCheckChanged(int position,
			CompoundButton buttonView, boolean isChecked) {
		lstItems.get(position).setUsed(isChecked);
	}

	// ------------------------------------------------------------------------
	// Class LbtChannelViewHolder
	// ------------------------------------------------------------------------

	private class LbtChannelViewHolder implements OnCheckedChangeListener {

		private int mPosition;
		private CheckBox chkUsed;
		private TextView txtName;
		private LbtChannelListener mListener;

		public LbtChannelViewHolder(View parent, LbtChannelListener listener) {
			mPosition = -1;
			mListener = listener;
			chkUsed = (CheckBox) parent.findViewById(R.id.used);
			txtName = (TextView) parent.findViewById(R.id.name);
			chkUsed.setOnCheckedChangeListener(this);
			parent.setTag(this);
		}

		public void setItem(int position, LbtChannelItem item) {
			mPosition = position;
			chkUsed.setChecked(item.isUsed());
			txtName.setText(item.getName());
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			mListener.onLbtChannelCheckChanged(mPosition, buttonView, isChecked);
		}
	}

	// ------------------------------------------------------------------------
	// Class LbtChannelItem
	// ------------------------------------------------------------------------

	private class LbtChannelItem {

		private String mName;
		private int mSlot;
		private boolean mIsUsed;

		public LbtChannelItem(String name, int slot) {
			mName = name;
			mSlot = slot;
			mIsUsed = false;
		}

		public String getName() {
			return mName;
		}

		public int getSlot() {
			return mSlot;
		}

		public boolean isUsed() {
			return mIsUsed;
		}

		public void setUsed(boolean used) {
			mIsUsed = used;
		}
	}

}
