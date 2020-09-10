package com.hanmiit.app.rfid.blasterdemo.dialog;

import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.app.rfid.blasterdemo.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

public class PasswordDialog implements OnClickListener, OnCancelListener, OnShowListener {

	private static final String TAG = PasswordDialog.class.getSimpleName();
	
	private EditText edtPassword;
	
	private AlertDialog.Builder mBuilder;
	private Dialog mDialog;
	private InputMethodManager mImm;
	
	private IDialogResultListener mListener;
	
	public PasswordDialog(Context context, int title) {
		mListener = null;
		mImm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		LinearLayout root = (LinearLayout)LinearLayout.inflate(context, R.layout.dialog_password, null);
		edtPassword = (EditText)root.findViewById(R.id.password);
		edtPassword.setText("00000000");
		
		mBuilder = new AlertDialog.Builder(context);
		mBuilder.setTitle(title);
		mBuilder.setView(root);
		mBuilder.setPositiveButton(R.string.action_ok, this);
		mBuilder.setNegativeButton(R.string.action_cancel, this);
		mBuilder.setCancelable(true);
		mBuilder.setOnCancelListener(this);
		mDialog = mBuilder.create();
		mDialog.setOnShowListener(this);
		
		ATLog.i(TAG, "INFO. $Constructor()");
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mImm.hideSoftInputFromWindow(edtPassword.getWindowToken(), 0);
		switch (which) {
		case Dialog.BUTTON_POSITIVE:
			ATLog.i(TAG, "INFO. onClick(BUTTON_POSITIVE)");
			if (mListener != null) 
				mListener.onOkClick(0, dialog);
			break;
		case Dialog.BUTTON_NEGATIVE:
			ATLog.i(TAG, "INFO. onClick(BUTTON_NEGATIVE)");
			if (mListener != null)
				mListener.onCancelClick(0, dialog);
			break;
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		ATLog.i(TAG, "INFO. onCancel()");
		mImm.hideSoftInputFromWindow(edtPassword.getWindowToken(), 0);
		if (mListener != null)
			mListener.onCancelClick(0, dialog);
	}

	@Override
	public void onShow(DialogInterface dialog) {
		ATLog.i(TAG, "INFO. onShow()");
		edtPassword.selectAll();
		edtPassword.requestFocus();
		mImm.showSoftInput(edtPassword, InputMethodManager.SHOW_FORCED);
	}
	
	public void show() {
		mDialog.show();
		ATLog.i(TAG, "INFO. show()");
	}
	
	public Dialog getDialog() {
		ATLog.i(TAG, "INFO. getDialog()");
		return mDialog;
	}
	
	public void setResultListener(IDialogResultListener listener) {
		mListener = listener;
		ATLog.i(TAG, "INFO. setResultListener()");
	}
	
	public void setPassword(String password) {
		edtPassword.setText(password);
		ATLog.i(TAG, "INFO. setPassword([%s])", password);
	}
	
	public String getPassword() {
		String password = edtPassword.getText().toString();
		ATLog.i(TAG, "INFO. getPassword() - [%s]", password);
		return password;
	}
}
