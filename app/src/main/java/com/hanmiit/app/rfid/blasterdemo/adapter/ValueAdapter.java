package com.hanmiit.app.rfid.blasterdemo.adapter;

import java.util.ArrayList;

import com.hanmiit.app.rfid.blasterdemo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ValueAdapter<T> extends BaseAdapter {

	@SuppressWarnings("unused")
	private static final String TAG = "ValueAdapter<?>";

	private Context context;
	private LayoutInflater inflater;
	protected ArrayList<ValueItem<T>> list;

	// ------------------------------------------------------------------------
	// Constructor ValueAdapter<T>
	// ------------------------------------------------------------------------
	public ValueAdapter(Context context) {
		super();

		this.context = context;
		this.inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = new ArrayList<ValueItem<T>>();
	}

	public ValueAdapter(Context context, ValueItem<T>[] items) {
		super();

		this.context = context;
		this.inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = new ArrayList<ValueItem<T>>();
		add(items);
	}

	public void add(ValueItem<T> item) {
		this.list.add(item);
	}

	public void add(ValueItem<T>[] items) {
		for (ValueItem<T> item : items) {
			this.list.add(item);
		}
	}

	public int getPosition(T value) {
		for (int i = 0; i < this.list.size(); i++) {
			if (this.list.get(i).getValue().equals(value))
				return i;
		}
		return -1;
	}

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public T getItem(int position) {
		return this.list.get(position).getValue();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ValueViewHolder holder = null;

		if (null == convertView) {
			convertView = inflater.inflate(R.layout.item_single_line, parent,
					false);
			holder = new ValueViewHolder(convertView);
		} else {
			holder = (ValueViewHolder) convertView.getTag();
		}
		holder.setItem(this.list.get(position));
		return convertView;
	}

	// ------------------------------------------------------------------------
	// Internal class ValueViewHolder
	// ------------------------------------------------------------------------

	private class ValueViewHolder {

		private TextView text;

		public ValueViewHolder(View parent) {
			this.text = (TextView) parent.findViewById(R.id.text);
			parent.setTag(this);
		}

		public void setItem(ValueItem<T> item) {
			this.text.setText(item.getText());
		}
	}
}
