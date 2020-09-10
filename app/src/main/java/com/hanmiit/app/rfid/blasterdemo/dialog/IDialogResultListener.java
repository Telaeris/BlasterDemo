package com.hanmiit.app.rfid.blasterdemo.dialog;

import android.content.DialogInterface;

public interface IDialogResultListener {
	void onOkClick(int what, DialogInterface dialog);
	void onCancelClick(int what, DialogInterface dialog);
}
