package com.hanmiit.app.rfid.blasterdemo.widgets;

import java.util.ArrayList;
import java.util.Locale;

import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.StringAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressLint("NewApi")
public class SymbolLength extends LinearLayout implements
		OnItemSelectedListener {

	@SuppressWarnings("unused")
	private static final String TAG = "SymbolLength";

	private static final int MAX_LENGTH = 0x100;
	private static final int ONE_DISCRETE_LENGTH = 0;
	private static final int TWO_DISCRETE_LENGTH = 1;
	private static final int LENGTH_WITHIN_RANGE = 2;
	private static final int ANY_LENGTH = 3;

	private Spinner lengthType;
	private TextView length1Label;
	private Spinner length1;
	private TextView length2Label;
	private Spinner length2;;

	private StringAdapter adapterLengthType;
	private StringAdapter adapterLengthL1;
	private StringAdapter adapterLengthL2;

	public SymbolLength(Context context) {
		super(context);
		initWidget(context);
	}

	public SymbolLength(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWidget(context);
	}

	public SymbolLength(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWidget(context);
	}

	private void initWidget(Context context) {

		if (this.isInEditMode())
			return;

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.widget_symbol_length, this, true);

		ArrayList<String> numOfLen = new ArrayList<String>();
		for (int i = 0; i < MAX_LENGTH; i++) {
			numOfLen.add(String.format(Locale.US, "%d", i));
		}

		this.lengthType = (Spinner) findViewById(R.id.length_type);
		this.adapterLengthType = new StringAdapter(context, R.array.length_type);
		this.lengthType.setAdapter(this.adapterLengthType);
		this.lengthType.setOnItemSelectedListener(this);
		this.length1Label = (TextView) findViewById(R.id.length_l1_label);
		this.length1 = (Spinner) findViewById(R.id.length_l1);
		this.adapterLengthL1 = new StringAdapter(context, numOfLen);
		this.length1.setAdapter(this.adapterLengthL1);
		this.length2Label = (TextView) findViewById(R.id.length_l2_label);
		this.length2 = (Spinner) findViewById(R.id.length_l2);
		this.adapterLengthL2 = new StringAdapter(context, numOfLen);
		this.length2.setAdapter(this.adapterLengthL2);
	}

	public void setLength(int length1, int length2) {
		int position = ANY_LENGTH;
		
		if (length1 > 0 && length2 == 0) {
			position = ONE_DISCRETE_LENGTH;
			this.length1.setSelection(length1);
			this.length2.setSelection(0);
			this.length1Label.setEnabled(true);
			this.length1.setEnabled(true);
			this.length2Label.setEnabled(false);
			this.length2.setEnabled(false);
		} else if (length1 > 0 && length2 > 0 && length1 > length2) {
			position = TWO_DISCRETE_LENGTH;
			this.length1.setSelection(length2);
			this.length2.setSelection(length1);
			this.length1Label.setEnabled(true);
			this.length1.setEnabled(true);
			this.length2Label.setEnabled(true);
			this.length2.setEnabled(true);
		} else if (length1 > 0 && length2 > 0 && length1 < length2) {
			position = LENGTH_WITHIN_RANGE;
			this.length1.setSelection(length1);
			this.length2.setSelection(length2);
			this.length1Label.setEnabled(true);
			this.length1.setEnabled(true);
			this.length2Label.setEnabled(true);
			this.length2.setEnabled(true);
		} else {
			position = ANY_LENGTH;
			this.length1.setSelection(0);
			this.length2.setSelection(0);
			this.length1Label.setEnabled(false);
			this.length1.setEnabled(false);
			this.length2Label.setEnabled(false);
			this.length2.setEnabled(false);
		}
		this.lengthType.setSelection(position);
	}

	public int getLength1() {
		switch (this.lengthType.getSelectedItemPosition()) {
		case ONE_DISCRETE_LENGTH:
		case LENGTH_WITHIN_RANGE:
			return this.length1.getSelectedItemPosition();
		case TWO_DISCRETE_LENGTH:
			return this.length2.getSelectedItemPosition();
		}
		return 0;
	}

	public int getLength2() {
		switch (this.lengthType.getSelectedItemPosition()) {
		case TWO_DISCRETE_LENGTH:
			return this.length1.getSelectedItemPosition();
		case LENGTH_WITHIN_RANGE:
			return this.length2.getSelectedItemPosition();
		}
		return 0;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		
		//switch (view.getId()) {
		//case R.id.length_type:
			switch ((int) id) {
			case ONE_DISCRETE_LENGTH:
				this.length2.setSelection(0);
				this.length1Label.setEnabled(true);
				this.length1.setEnabled(true);
				this.length2Label.setEnabled(false);
				this.length2.setEnabled(false);
				break;
			case TWO_DISCRETE_LENGTH:
			case LENGTH_WITHIN_RANGE:
				this.length1Label.setEnabled(true);
				this.length1.setEnabled(true);
				this.length2Label.setEnabled(true);
				this.length2.setEnabled(true);
				break;
			case ANY_LENGTH:
				this.length1.setSelection(0);
				this.length2.setSelection(0);
				this.length1Label.setEnabled(false);
				this.length1.setEnabled(false);
				this.length2Label.setEnabled(false);
				this.length2.setEnabled(false);
				break;
			}
		//	break;
		//}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}
