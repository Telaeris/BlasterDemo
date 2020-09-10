package com.hanmiit.app.rfid.blasterdemo.dialog;

import com.hanmiit.app.rfid.blasterdemo.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OpenFileDialog implements OnPathChangedListener, OnFileSelectedListener {

	private Builder mDialog;
	private OnFileSelectedListener mListenerFileSelected;
	private OnNotifyEventListener mListenerCancel;

	private TextView txtPath;
	private EditText edtFileName;
	private FileList lstFiles;
	
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public OpenFileDialog(Context context) {
		String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		// Initialize Dialog Layout
		LinearLayout layoutRoot = (LinearLayout) LinearLayout.inflate(context,
				R.layout.dialog_open_file, null);
		txtPath = (TextView)layoutRoot.findViewById(R.id.path);
		edtFileName = (EditText)layoutRoot.findViewById(R.id.file_name);
		lstFiles = (FileList)layoutRoot.findViewById(R.id.file_list);
		lstFiles.setPath(rootPath);
		lstFiles.setOnPathChangedListener(this);
		lstFiles.setOnFileSelectedListener(this);
		
		mDialog = new Builder(context);
		mDialog.setTitle(R.string.open_file_dialog);
		mDialog.setView(layoutRoot);
		mDialog.setPositiveButton(R.string.action_ok, onPositiveClicked);
		mDialog.setNegativeButton(R.string.action_cancel, onNegativeClicked);
		mDialog.setCancelable(true);
		mDialog.setOnCancelListener(onCanceled);
	}
	
	public Dialog create() {
		return mDialog.create();
	}
	
	public void show() {
		mDialog.show();
	}
	
	public void setOnFileSelectedListener(OnFileSelectedListener listener) {
		mListenerFileSelected = listener;
	}
	
	public void setOnCancelListener(OnNotifyEventListener listener) {
		mListenerCancel = listener;
	}
	
	private DialogInterface.OnClickListener onPositiveClicked = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (mListenerFileSelected != null) {
				mListenerFileSelected.onSelected(lstFiles.getPath(), getFileName());
			}
		}
	};
	
	private DialogInterface.OnClickListener onNegativeClicked = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (mListenerCancel != null) {
				mListenerCancel.onNotify(OpenFileDialog.this);
			}
		}
	};
	
	private DialogInterface.OnCancelListener onCanceled = new DialogInterface.OnCancelListener() {
		
		@Override
		public void onCancel(DialogInterface dialog) {
			if (mListenerCancel != null) {
				mListenerCancel.onNotify(OpenFileDialog.this);
			}
		}
	};
	
	private String getPath() {
		return txtPath.getText().toString();
	}
	
	private String getFileName() {
		return edtFileName.getText().toString();
	}

	@Override
	public void onSelected(String path, String fileName) {
		edtFileName.setText(fileName);
	}

	@Override
	public void onChanged(String path) {
		txtPath.setText(path);
		edtFileName.setText("");
	}
}
