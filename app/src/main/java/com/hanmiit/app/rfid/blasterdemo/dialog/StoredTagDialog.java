package com.hanmiit.app.rfid.blasterdemo.dialog;

import com.hanmiit.app.rfid.blasterdemo.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;

public class StoredTagDialog implements OnClickListener, OnCancelListener {

	public static final int NEW_TAG = 2001;
	public static final int EDIT_TAG = 2002;

	private EditText edtTag;

	private AlertDialog.Builder mBuilder;
	private Dialog mDialog;

	private int mState;
	private IDialogResultListener mListener;

	public StoredTagDialog(Context context, int title) {

		this.mState = NEW_TAG;
		this.mListener = null;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(context,
				R.layout.dialog_input_hex, null);
		this.edtTag = (EditText)root.findViewById(R.id.value);
		
		this.mBuilder = new AlertDialog.Builder(context);
		this.mBuilder.setTitle(title);
		this.mBuilder.setView(root);
		this.mBuilder.setPositiveButton(R.string.action_ok, this);
		this.mBuilder.setNegativeButton(R.string.action_cancel, this);

		this.mDialog = this.mBuilder.create();
		this.mDialog.setCancelable(true);
		this.mDialog.setOnCancelListener(this);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case Dialog.BUTTON_POSITIVE:
			if (this.mListener != null) 
				this.mListener.onOkClick(this.mState, dialog);
			break;
		case Dialog.BUTTON_NEGATIVE:
			if (this.mListener != null)
				this.mListener.onCancelClick(this.mState, dialog);
			break;
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (this.mListener != null)
			this.mListener.onCancelClick(this.mState, dialog);
	}
	
	public Dialog getDialog() {
		return this.mDialog;
	}
	
	public void setResultListener(IDialogResultListener listener) {
		this.mListener = listener;
	}
	
	public void setState(int state) {
		this.mState = state;
	}
	
	public void setTag(String tag) {
		this.edtTag.setText(tag);
	}
	
	public String getTag() {
		return this.edtTag.getText().toString();
	}
}
