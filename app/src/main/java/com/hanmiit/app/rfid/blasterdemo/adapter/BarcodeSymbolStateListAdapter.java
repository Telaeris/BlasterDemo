package com.hanmiit.app.rfid.blasterdemo.adapter;

import java.util.ArrayList;

import com.hanmiit.lib.barcode.type.ParamName;
import com.hanmiit.app.rfid.blasterdemo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

public class BarcodeSymbolStateListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<BarcodeSymbolStateListItem> mList;

	public BarcodeSymbolStateListAdapter(Context context) {
		super();

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = new ArrayList<BarcodeSymbolStateListItem>();
	}

	public void clear() {
		mList.clear();
	}
	
	public void addItem(ParamName name, boolean enabled) {
		mList.add(new BarcodeSymbolStateListItem(name, enabled));
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public ParamName getItem(int position) {
		return mList.get(position).getName();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		BarcodeSymbolStateListViewHolder holder = null;

		if (null == convertView) {
			convertView = mInflater.inflate(
					R.layout.item_barcode_symbol_state_list, parent, false);
			holder = new BarcodeSymbolStateListViewHolder(convertView, new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					BarcodeSymbolStateListItem item = mList.get(position);
					item.setEnabled(isChecked);
				}
				
			});
		} else {
			holder = (BarcodeSymbolStateListViewHolder) convertView.getTag();
		}
		((ListView)parent).setItemChecked(position, mList.get(position).isEnabled());
		holder.setItem(mList.get(position));
		holder.setChecked(((ListView)parent).isItemChecked(position));
		return convertView;
	}

	private class BarcodeSymbolStateListItem {

		private ParamName mName;
		private boolean mIsEnabled;

		private BarcodeSymbolStateListItem(ParamName name, boolean enabled) {
			mName = name;
			mIsEnabled = enabled;
		}

		public ParamName getName() {
			return mName;
		}

		public boolean isEnabled() {
			return mIsEnabled;
		}
		
		public void setEnabled(boolean enabled) {
			mIsEnabled = enabled;
		}
	}

	private class BarcodeSymbolStateListViewHolder {

		private CheckBox chkState;
		private TextView txtName;

		private BarcodeSymbolStateListViewHolder(View parent, OnCheckedChangeListener listener) {
			chkState = (CheckBox) parent.findViewById(R.id.cb_checkbox);
			txtName = (TextView) parent.findViewById(R.id.name);
			chkState.setOnCheckedChangeListener(listener);
			parent.setTag(this);
		}

		public void setItem(BarcodeSymbolStateListItem item) {
//			chkState.setChecked(item.isEnabled());
			chkState.setChecked(false);
			txtName.setText(item.getName().toString());
		}
		
		public void setChecked(boolean checked) {
			chkState.setChecked(checked);
		}
	}
}
