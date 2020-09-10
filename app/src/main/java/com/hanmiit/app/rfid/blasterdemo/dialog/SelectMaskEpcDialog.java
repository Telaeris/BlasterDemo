package com.hanmiit.app.rfid.blasterdemo.dialog;

import com.hanmiit.lib.rfid.params.SelectMaskEpcParam;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.filter.InputFilterMinMax;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SelectMaskEpcDialog implements OnClickListener, OnCancelListener {

	private static final int DEFAULT_OFFSET = 16;
	private static final String DEFAULT_MASK = "";
	private static final int DEFAULT_LENGTH = 0;
	private static final int NIBBLE_SIZE = 4;
	
	public static final int NEW_ITEM = 2001;
	public static final int MODIFY_ITEM = 2002;
	
	private EditText edtOffset;
	private EditText edtMask;
	private EditText edtLength;

	private AlertDialog.Builder mBuilder;
	private Dialog mDialog;
	
	private int mState;
	private IDialogResultListener mListener;

	public SelectMaskEpcDialog(Context context) {
		
		mState = NEW_ITEM;
		mListener = null;
		
		LinearLayout root = (LinearLayout) LinearLayout.inflate(context,
				R.layout.dialog_mask_item_epc, null);
		edtOffset = (EditText) root.findViewById(R.id.offset);
		InputFilter[] filters = new InputFilter[] { new InputFilterMinMax(0, 255) };
		edtOffset.setFilters(filters);
		edtMask = (EditText) root.findViewById(R.id.mask);
		edtMask.addTextChangedListener(mMaskChanged);
		edtLength = (EditText) root.findViewById(R.id.length);
		filters = new InputFilter[] { new InputFilterMinMax(1, 255) };
		edtLength.setFilters(filters);

		mBuilder = new AlertDialog.Builder(context);
		mBuilder.setTitle(R.string.mask_item_dialog);
		mBuilder.setView(root);
		mBuilder.setPositiveButton(R.string.action_ok, this);
		mBuilder.setNegativeButton(R.string.action_cancel, this);

		mDialog = mBuilder.create();
		mDialog.setCancelable(true);
		mDialog.setOnCancelListener(this);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case Dialog.BUTTON_POSITIVE:
			if (mListener != null) 
				mListener.onOkClick(mState, dialog);
			break;
		case Dialog.BUTTON_NEGATIVE:
			if (mListener != null)
				mListener.onCancelClick(mState, dialog);
			break;
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (mListener != null)
			mListener.onCancelClick(mState, dialog);
	}
	
	public Dialog getDialog() {
		return mDialog;
	}
	
	public void setResultListener(IDialogResultListener listener) {
		mListener = listener;
	}
	
	public void setState(int state) {
		mState = state;
	}
	
	public void clearItem() {
		edtOffset.setText("" + DEFAULT_OFFSET);
		edtMask.setText(DEFAULT_MASK);
		edtLength.setText("" + DEFAULT_LENGTH);
	}
	
	public void setItem(SelectMaskEpcParam item) {
		edtOffset.setText("" + item.getOffset());
		edtMask.setText(item.getMask());
		edtLength.setText("" + item.getLength());
	}
	
	public SelectMaskEpcParam getItem() {
		SelectMaskEpcParam item = new SelectMaskEpcParam();
		int offset = DEFAULT_OFFSET;
		String mask = DEFAULT_MASK;
		int length = DEFAULT_LENGTH;
		
		try {
			offset = Integer.parseInt(edtOffset.getText().toString());
		} catch (Exception e) {
			offset = DEFAULT_OFFSET;
		}
		mask = edtMask.getText().toString();
		try {
			length = Integer.parseInt(edtLength.getText().toString());
		} catch (Exception e) {
			length = DEFAULT_LENGTH;
		}
		item.setOffset(offset);
		item.setMask(mask);
		item.setLength(length);
		return item;
	}
	
	private TextWatcher mMaskChanged = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			int length = s.length() * NIBBLE_SIZE;
			edtLength.setText("" + length);
		}
		
	};
}
