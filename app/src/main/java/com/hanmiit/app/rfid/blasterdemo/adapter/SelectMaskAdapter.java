package com.hanmiit.app.rfid.blasterdemo.adapter;

import java.util.ArrayList;

import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.params.SelectMaskParam;
import com.hanmiit.lib.rfid.type.MaskActionType;
import com.hanmiit.lib.rfid.type.MaskTargetType;
import com.hanmiit.lib.rfid.type.MemoryBank;
import com.hanmiit.app.rfid.blasterdemo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SelectMaskAdapter extends BaseAdapter {

	private static final String TAG = SelectMaskAdapter.class.getSimpleName();

	private LayoutInflater inflater;
	private ArrayList<SelectMaskItem> list;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	public SelectMaskAdapter(Context context) {
		super();

		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = new ArrayList<SelectMaskItem>();
	}

	public void addItemNoUpdate(SelectMaskParam param) {
		SelectMaskItem item = new SelectMaskItem();
		item.setTarget(param.getTarget());
		item.setAction(param.getAction());
		item.setBank(param.getBank());
		item.setOffset(param.getOffset());
		item.setMask(param.getMask());
		item.setLength(param.getLength());
		this.list.add(item);
		ATLog.d(TAG, "DEBUG. setItem([%s])", item.toString());
	}

	public void addItem(SelectMaskParam param) {
		addItemNoUpdate(param);
		this.notifyDataSetChanged();
	}

	public void updateItem(int index, SelectMaskParam param) {
		SelectMaskItem item = this.list.get(index);
		item.setTarget(param.getTarget());
		item.setAction(param.getAction());
		item.setBank(param.getBank());
		item.setOffset(param.getOffset());
		item.setMask(param.getMask());
		item.setLength(param.getLength());
		ATLog.d(TAG, "DEBUG. updateItem(%d, [%s])", index, item.toString());
		this.notifyDataSetChanged();
	}

	public void removeItem(int index) {
		this.list.remove(index);
		ATLog.d(TAG, "DEBUG. removeItem(%d)",index);
		this.notifyDataSetChanged();
	}
	
	public void clear() {
		this.list.clear();
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public SelectMaskParam getItem(int position) {
		SelectMaskItem item = null;
		try {
			item = this.list.get(position);
		} catch (Exception e) {
			return null;
		}
		if (item == null) return null;
		SelectMaskParam param = new SelectMaskParam(item.getTarget(),
				item.getAction(), item.getBank(), item.getOffset(),
				item.getMask(), item.getLength());
		return param;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SelectMaskViewHolder holder = null;

		if (null == convertView) {
			convertView = this.inflater.inflate(R.layout.item_select_mask,
					parent, false);
			holder = new SelectMaskViewHolder(convertView, this);
		} else {
			holder = (SelectMaskViewHolder) convertView.getTag();
		}
		holder.setItem(position, this.list.get(position));
		return convertView;
	}

	// ------------------------------------------------------------------------
	// Internal SelectMaskItem Class
	// ------------------------------------------------------------------------
	private class SelectMaskItem {
		private MaskTargetType target;
		private MaskActionType action;
		private MemoryBank bank;
		private int offset;
		private String mask;
		private int length;

		public SelectMaskItem() {
			this.target = MaskTargetType.SL;
			this.action = MaskActionType.AB;
			this.bank = MemoryBank.EPC;
			this.offset = 16;
			this.mask = "";
			this.length = 0;
		}

		public MaskTargetType getTarget() {
			return this.target;
		}

		public void setTarget(MaskTargetType target) {
			this.target = target;
		}

		public MaskActionType getAction() {
			return this.action;
		}

		public void setAction(MaskActionType action) {
			this.action = action;
		}

		public MemoryBank getBank() {
			return this.bank;
		}

		public void setBank(MemoryBank bank) {
			this.bank = bank;
		}

		public int getOffset() {
			return this.offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		public String getMask() {
			return this.mask;
		}

		public void setMask(String mask) {
			this.mask = mask;
		}

		public int getLength() {
			return this.length;
		}

		public void setLength(int length) {
			this.length = length;
		}

		public String toString() {
			return "{" + this.target + ", " + this.action.toString(this.target)
					+ ", " + this.bank + ", " + this.offset + ", " + this.mask
					+ ", " + this.length + "}";
		}
	}

	// ------------------------------------------------------------------------
	// Internal SelectMaskViewHolder Class
	// ------------------------------------------------------------------------
	private class SelectMaskViewHolder {
		private TextView txtTarget;
		private TextView txtAction;
		private TextView txtBank;
		private TextView txtOffset;
		private TextView txtMask;
		private TextView txtLength;

		public SelectMaskViewHolder(View parent, SelectMaskAdapter adapter) {
			this.txtTarget = (TextView) parent.findViewById(R.id.target);
			this.txtAction = (TextView) parent.findViewById(R.id.action);
			this.txtBank = (TextView) parent.findViewById(R.id.bank);
			this.txtOffset = (TextView) parent.findViewById(R.id.offset);
			this.txtMask = (TextView) parent.findViewById(R.id.mask);
			this.txtLength = (TextView) parent.findViewById(R.id.length);
			parent.setTag(this);
		}

		public void setItem(int position, SelectMaskItem item) {
			this.txtTarget.setText(item.getTarget().toString());
			this.txtAction.setText(item.getAction().toString(item.getTarget()));
			this.txtBank.setText(item.getBank().toString());
			this.txtOffset.setText("" + item.getOffset() + " bit");
			this.txtMask.setText(item.getMask());
			this.txtLength.setText("" + item.getLength() + " bit");
		}
	}
}
