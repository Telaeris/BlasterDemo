package com.hanmiit.app.rfid.blasterdemo.adapter;

import java.util.ArrayList;
import java.util.Locale;

import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.app.rfid.blasterdemo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MemoryListAdapter extends BaseAdapter {

	private static final String TAG = MemoryListAdapter.class.getSimpleName();

	private static final int MAX_COL = 4;
	private static final int WORD_LENGTH = 16;
	private static final int MAX_DISPLAY_LENGTH = 16;
	private static final int MAX_ROW_BIT_LENGTH = 4;

	private LayoutInflater mInflaster;
	private ArrayList<MemoryListItem> mList;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	public MemoryListAdapter(Context context) {
		super();

		this.mInflaster = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mList = new ArrayList<MemoryListItem>();
		this.clear();
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	// Clear Adapter List
	public void clear() {
		this.mList.clear();
		this.mList.add(new MemoryListItem());
		this.notifyDataSetChanged();
	}

	// Set Display Value
	public void setValue(int offset, String tag) {
		int i = 0;
		int row = (int) Math.ceil((double) tag.length()
				/ (double) MAX_DISPLAY_LENGTH);
		int address = 0;
		String value = "";

//		offset *= WORD_LENGTH;
		this.mList.clear();

		for (i = 0; i < row; i++) {
			address = (i * MAX_ROW_BIT_LENGTH) + offset;
			if (tag.length() > (i * MAX_DISPLAY_LENGTH) + MAX_DISPLAY_LENGTH) {
				value = tag.substring(i * MAX_DISPLAY_LENGTH,
						(i * MAX_DISPLAY_LENGTH) + MAX_DISPLAY_LENGTH);
			} else {
				value = tag.substring(i * MAX_DISPLAY_LENGTH);
			}
			this.mList.add(new MemoryListItem(address, value));
		}
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return this.mList.size();
	}

	@Override
	public Object getItem(int position) {
		return this.mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MemoryListViewHolder holder = null;
		
		if (null == convertView) {
			convertView = mInflaster.inflate(R.layout.item_memory_list, parent, false);
			holder = new MemoryListViewHolder(convertView);
		} else {
			holder = (MemoryListViewHolder)convertView.getTag();
		}
		holder.setItem(this.mList.get(position));
		return convertView;
	}

	// ------------------------------------------------------------------------
	// class MemoryListItem
	// ------------------------------------------------------------------------
	private class MemoryListItem {

		private static final int DISPLAY_VALUE_LENGTH = 4;

		private String[] mAddress;
		private String[] mValue;

		public MemoryListItem() {
			this.mAddress = new String[] { "0 WORD", "1 WORD", "2 WORD", "3 WORD" };
			this.mValue = new String[] { "0000", "0000", "0000", "0000" };
		}

		public MemoryListItem(int offset, String tag) {
			String data = padRight(tag, MAX_DISPLAY_LENGTH, '0');

			this.mAddress = new String[MAX_COL];
			this.mValue = new String[MAX_COL];

			for (int i = 0; i < MAX_COL; i++) {
				this.mAddress[i] = String.format(Locale.US, "%d WORD", offset
						+ i);
				this.mValue[i] = data.substring(i * MAX_COL, (i * MAX_COL)
						+ DISPLAY_VALUE_LENGTH);
			}
		}

		public String getAddress(int index) {
			return this.mAddress[index];
		}

		public String getValue(int index) {
			return this.mValue[index];
		}

		@Override
		public String toString() {
			return "{{" + this.mAddress[0] + ":" + this.mValue[0] + "}, {"
					+ this.mAddress[1] + ":" + this.mValue[1] + "}, {"
					+ this.mAddress[2] + ":" + this.mValue[2] + "}, {"
					+ this.mAddress[3] + ":" + this.mValue[3] + "}}";
		}

		private String padRight(String value, int length, char pad) {
			if (value.length() >= length) {
				return value;
			}
			int padLen = length - value.length();
			for (int i = 0; i < padLen; i++) {
				value += pad;
			}
			return value;
		}
	}

	// ------------------------------------------------------------------------
	// class MemoryListViewHolder
	// ------------------------------------------------------------------------
	private class MemoryListViewHolder {

		private TextView[] mAddress;
		private TextView[] mValue;

		public MemoryListViewHolder(View parent) {
			this.mAddress = new TextView[] {
					(TextView) parent.findViewById(R.id.address1),
					(TextView) parent.findViewById(R.id.address2),
					(TextView) parent.findViewById(R.id.address3),
					(TextView) parent.findViewById(R.id.address4) };
			this.mValue = new TextView[] {
					(TextView) parent.findViewById(R.id.value1),
					(TextView) parent.findViewById(R.id.value2),
					(TextView) parent.findViewById(R.id.value3),
					(TextView) parent.findViewById(R.id.value4) };
			parent.setTag(this);
		}

		public void setItem(MemoryListItem item) {
			ATLog.d(TAG, "DEBUG. setItem(%s)", item.toString());

			for (int i = 0; i < MAX_COL; i++) {
				this.mAddress[i].setText(item.getAddress(i));
				this.mValue[i].setText(item.getValue(i));
			}
		}

		@Override
		public String toString() {
			return "{{" + this.mAddress[0].getText() + ":"
					+ this.mValue[0].getText() + "}, {"
					+ this.mAddress[1].getText() + ":"
					+ this.mValue[1].getText() + "}, {"
					+ this.mAddress[2].getText() + ":"
					+ this.mValue[2].getText() + "}, {"
					+ this.mAddress[3].getText() + ":"
					+ this.mValue[3].getText() + "}}";
		}
	}
}
