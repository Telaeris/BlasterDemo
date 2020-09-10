package com.hanmiit.app.rfid.blasterdemo.adapter;

import java.util.ArrayList;

import com.hanmiit.lib.barcode.ParamValue;
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
import android.widget.TextView;

public class BarcodeSymbolListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<BarcodeSymbolListItem> mList;
	
	public BarcodeSymbolListAdapter(Context context) {
		super();
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = new ArrayList<BarcodeSymbolListItem>();
	}
	
	public void clear() {
		mList.clear();
	}
	
	public void addItem(ParamName name, boolean enabled) {
		mList.add(new BarcodeSymbolListItem(name, enabled));
	}
	
	public ParamValue getParamValue(int position) {
		BarcodeSymbolListItem item = mList.get(position);
		return new ParamValue(item.getName(), item.isEnabled());
	}
	
	public void enableAll() {
		for(BarcodeSymbolListItem item : mList) {
			item.setEnabled(true);
		}
		notifyDataSetChanged();
	}
	
	public void disableAll() {
		for(BarcodeSymbolListItem item : mList) {
			item.setEnabled(false);
		}
		notifyDataSetChanged();
	}
	
	public void defaultAll() {
		for(BarcodeSymbolListItem item : mList) {
			switch (item.getName()) {
			case UPCA:
				item.setEnabled(true);
				break;
			case UPCE:
				item.setEnabled(true);
				break;
			case UPCE1:
				item.setEnabled(false);
				break;
			case EAN8:
				item.setEnabled(true);
				break;
			case EAN13:
				item.setEnabled(true);
				break;
			case Code128:
				item.setEnabled(true);
				break;
			case Code39:
				item.setEnabled(true);
				break;
			case Code93:
				item.setEnabled(false);
				break;
			case Code11:
				item.setEnabled(false);
				break;
			case I2of5:
				item.setEnabled(false);
				break;
			case D2of5:
				item.setEnabled(false);
				break;
			case Codabar:
				item.setEnabled(false);
				break;
			case MSI:
				item.setEnabled(false);
				break;
			case C2of5:
				item.setEnabled(false);
				break;
			case M2of5:
				item.setEnabled(false);
				break;
			case K3of5:
				item.setEnabled(false);
				break;
			case USPostnet:
				item.setEnabled(false);
				break;
			case USPlanet:
				item.setEnabled(false);
				break;
			case UKPostal:
				item.setEnabled(false);
				break;
			case JapanPostal:
				item.setEnabled(false);
				break;
			case AustraliaPost:
				item.setEnabled(false);
				break;
			case NetherlandsKIXCode:
				item.setEnabled(false);
				break;
			case USPS4CB:
				item.setEnabled(false);
				break;
			case UPUFICSPostal:
				item.setEnabled(false);
				break;
			case GS1Databar:
				item.setEnabled(true);
				break;
			case CompositeCCC:
				item.setEnabled(false);
				break;
			case CompositeCCAB:
				item.setEnabled(false);
				break;
			case CompositeTLC39:
				item.setEnabled(false);
				break;
			case PDF417:
				item.setEnabled(true);
				break;
			case MicroPDF417:
				item.setEnabled(false);
				break;
			case DataMatrix:
				item.setEnabled(false);
				break;
			case Maxicode:
				item.setEnabled(false);
				break;
			case QRCode:
				item.setEnabled(true);
				break;
			case MicroQR:
				item.setEnabled(true);
				break;
			case Aztec:
				item.setEnabled(true);
				break;
			default:
				return;
			}
		}
		notifyDataSetChanged();
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
		BarcodeSymbolListViewHolder holder = null;
		
		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.item_barcode_symbol_list, parent, false);
			holder = new BarcodeSymbolListViewHolder(convertView);
		} else {
			holder = (BarcodeSymbolListViewHolder)convertView.getTag();
		}
		holder.setItem(position, mList.get(position));
		return convertView;
	}

	private class BarcodeSymbolListViewHolder implements OnCheckedChangeListener {

		private int mPosition;
		private CheckBox chkState;
		private TextView txtName;

		private BarcodeSymbolListViewHolder(View parent) {
			mPosition = -1;
			chkState = (CheckBox) parent.findViewById(R.id.cb_checkbox);
			chkState.setOnCheckedChangeListener(this);
			txtName = (TextView) parent.findViewById(R.id.name);
			parent.setTag(this);
		}

		public void setItem(int position, BarcodeSymbolListItem item) {
			mPosition = position;
			chkState.setChecked(item.isEnabled());
			txtName.setText(item.getName().toString());
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			BarcodeSymbolListItem item = mList.get(mPosition);
			if (item != null) {
				item.setEnabled(isChecked);
			}
		}
	}

	private class BarcodeSymbolListItem {

		private ParamName mName;
		private boolean mIsEnabled;

		private BarcodeSymbolListItem(ParamName name, boolean enabled) {
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
}
