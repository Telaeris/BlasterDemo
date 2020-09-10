package com.hanmiit.app.rfid.blasterdemo.adapter;

import java.util.ArrayList;

import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.lib.barcode.type.BarcodeType;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BarcodeListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<BarcodeListItem> mList;

	public BarcodeListAdapter(Context context) {
		super();
		
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mList = new ArrayList<BarcodeListItem>();
	}

	public void clear() {
		this.mList.clear();
		this.notifyDataSetChanged();
	}
	
	public void addItem(BarcodeType type, String codeId, String barcode) {
		BarcodeListItem item = new BarcodeListItem(type, codeId, barcode);
		mList.add(item);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return this.mList.size();
	}

	@Override
	public String getItem(int position) {
		return this.mList.get(position).getBarcode();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BarcodeListViewHolder holder = null;
		
		if (null == convertView) {
			convertView = this.mInflater.inflate(R.layout.item_barcode_list, parent, false);
			holder = new BarcodeListViewHolder(convertView);
		} else {
			holder = (BarcodeListViewHolder)convertView.getTag();
		}
		holder.setItem(this.mList.get(position));
		return convertView;
	}

	private class BarcodeListItem {
		private BarcodeType mType;
		private String mCodeId;
		private String mBarcode;

		private BarcodeListItem(BarcodeType type, String codeId, String barcode) {
			mType = type;
			mCodeId = codeId;
			mBarcode = barcode;
		}

		public BarcodeType getType() {
			return mType;
		}
		
		public String getCodeId() {
			return mCodeId;
		}
		
		public String getBarcode() {
			return mBarcode;
		}
	}

	private class BarcodeListViewHolder {
		private TextView txtType;
		private TextView txtCodeId;
		private TextView txtBarcode;

		private BarcodeListViewHolder(View parent) {
			txtType = (TextView) parent.findViewById(R.id.code_type);
			txtCodeId = (TextView)parent.findViewById(R.id.code_id);
			txtBarcode = (TextView) parent.findViewById(R.id.barcode);
			parent.setTag(this);
		}

		public void setItem(BarcodeListItem item) {
			txtType.setText(item.getType().toString());
			txtCodeId.setText(item.getCodeId());
			txtBarcode.setText(item.getBarcode());
		}
	}
}
