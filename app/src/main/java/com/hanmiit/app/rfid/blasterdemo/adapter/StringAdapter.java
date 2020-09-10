package com.hanmiit.app.rfid.blasterdemo.adapter;

import java.util.ArrayList;

import com.hanmiit.app.rfid.blasterdemo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StringAdapter extends BaseAdapter {

	@SuppressWarnings("unused")
	private static final String TAG = "StringAdapter";

	private Context context;
	private LayoutInflater inflater;
	private ArrayList<String> list;

	// ------------------------------------------------------------------------
	// Constructor StringAdapter
	// ------------------------------------------------------------------------

	public StringAdapter(Context context) {
		super();

		this.context = context;
		this.inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = new ArrayList<String>();
	}

	public StringAdapter(Context context, int resId) {
		super();

		this.context = context;
		this.inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.list = new ArrayList<String>();
		add(resId);
	}

	public StringAdapter(Context context, ArrayList<String> list) {
		super();

		this.context = context;
		this.inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
	}

	public void add(String[] items) {
		this.list.clear();
		for (String item : items) {
			this.list.add(item);
		}
	}
	
	public void add(int resId) {
		add(this.context.getResources().getStringArray(resId));
	}
	
	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public String getItem(int position) {
		return this.list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		StringViewHolder holder = null;

		if (null == convertView) {
			convertView = inflater.inflate(R.layout.item_single_line, parent,
					false);
			holder = new StringViewHolder(convertView);
		} else {
			holder = (StringViewHolder)convertView.getTag();
		}
		holder.setItem(this.list.get(position));
		return convertView;
	}

	// ------------------------------------------------------------------------
	// Internal class StringViewHolder
	// ------------------------------------------------------------------------

	private class StringViewHolder {
		private TextView text;
		
		public StringViewHolder(View parent) {
			this.text = (TextView) parent.findViewById(R.id.text);
			parent.setTag(this);
		}
		
		public void setItem(String item) {
			this.text.setText(item);
		}
	}
}
