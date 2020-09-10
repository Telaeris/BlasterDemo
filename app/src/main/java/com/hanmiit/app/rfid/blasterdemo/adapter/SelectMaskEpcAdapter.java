package com.hanmiit.app.rfid.blasterdemo.adapter;

import java.util.ArrayList;

import com.hanmiit.lib.rfid.params.SelectMaskEpcParam;
import com.hanmiit.app.rfid.blasterdemo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SelectMaskEpcAdapter extends BaseAdapter {

	@SuppressWarnings("unused")
	private static final String TAG = SelectMaskEpcAdapter.class.getSimpleName();

	private LayoutInflater mInflater;
	private ArrayList<SelectMaskEpcParam> lstItems;

	public SelectMaskEpcAdapter(Context context) {
		super();

		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.lstItems = new ArrayList<SelectMaskEpcParam>();
	}

	public void addItem(SelectMaskEpcParam item) {
		lstItems.add(item);
	}
	
	public void updateItem(int position, SelectMaskEpcParam item) {
		this.lstItems.set(position, item);
	}
	
	public void removeItem(int position) {
		this.lstItems.remove(position);
	}
	
	public void clear() {
		this.lstItems.clear();
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return this.lstItems.size();
	}

	@Override
	public SelectMaskEpcParam getItem(int position) {
		return this.lstItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SelectMaskEpcViewHolder holder = null;

		if (null == convertView) {
			convertView = this.mInflater.inflate(R.layout.item_select_mask_epc,
					parent, false);
			holder = new SelectMaskEpcViewHolder(convertView, this);
		} else {
			holder = (SelectMaskEpcViewHolder)convertView.getTag();
		}
		holder.setItem(this.lstItems.get(position));
		return convertView;
	}

	private class SelectMaskEpcViewHolder {
		private TextView txtOffset;
		private TextView txtLength;
		private TextView txtMask;

		protected SelectMaskEpcViewHolder(View parent,
				SelectMaskEpcAdapter adapter) {
			this.txtOffset = (TextView) parent.findViewById(R.id.offset);
			this.txtLength = (TextView) parent.findViewById(R.id.length);
			this.txtMask = (TextView) parent.findViewById(R.id.mask);
			parent.setTag(this);
		}

		public void setItem(SelectMaskEpcParam item) {
			this.txtOffset.setText("" + item.getOffset());
			this.txtLength.setText("" + item.getLength());
			this.txtMask.setText(item.getMask());
		}
	}
}
